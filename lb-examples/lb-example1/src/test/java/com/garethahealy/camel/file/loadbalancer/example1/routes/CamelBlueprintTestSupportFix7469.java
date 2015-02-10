/*
 * #%L
 * lb-example1
 * %%
 * Copyright (C) 2013 - 2015 Gareth Healy
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.garethahealy.camel.file.loadbalancer.example1.routes;

import java.util.Dictionary;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.test.blueprint.CamelBlueprintHelper;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.apache.camel.util.KeyValueHolder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.blueprint.container.BlueprintEvent;
import org.osgi.service.blueprint.container.BlueprintListener;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;

public class CamelBlueprintTestSupportFix7469 extends CamelBlueprintTestSupport {

    //https://issues.apache.org/jira/browse/CAMEL-7469

    private final Set<ServiceRegistration> services = new LinkedHashSet<ServiceRegistration>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected BundleContext createBundleContext() throws Exception {
        final String symbolicName = getClass().getSimpleName();
        final BundleContext answer = CamelBlueprintHelper.createBundleContext(symbolicName, getBlueprintDescriptor(),
                                                                              includeTestBundle(), getBundleFilter(), getBundleVersion(), getBundleDirectives());

        // must register override properties early in OSGi containers
        Properties extra = useOverridePropertiesWithPropertiesComponent();
        if (extra != null) {
            answer.registerService(PropertiesComponent.OVERRIDE_PROPERTIES, extra, null);
        }

        Map<String, KeyValueHolder<Object, Dictionary>> map = new LinkedHashMap<String, KeyValueHolder<Object, Dictionary>>();
        addServicesOnStartup(map);
        for (Map.Entry<String, KeyValueHolder<Object, Dictionary>> entry : map.entrySet()) {
            String clazz = entry.getKey();
            Object service = entry.getValue().getKey();
            Dictionary dict = entry.getValue().getValue();
            log.debug("Registering service {} -> {}", clazz, service);
            ServiceRegistration reg = answer.registerService(clazz, service, dict);
            if (reg != null) {
                services.add(reg);
            }
        }

        // must reuse props as we can do both load from .cfg file and override afterwards
        Dictionary props = new Properties();

        // load configuration file
        String[] file = loadConfigAdminConfigurationFile();
        if (file != null && file.length != 2) {
            throw new IllegalArgumentException("The returned String[] from loadConfigAdminConfigurationFile must be of length 2, was " + file.length);
        }

        if (file != null) {
            CamelBlueprintHelper.setPersistentFileForConfigAdmin(answer, file[1], file[0], props);
        }

        // allow end user to override properties
        String pid = useOverridePropertiesWithConfigAdmin(props);
        if (pid != null) {
            // we will update the configuration now. As OSGi is highly asynchronous, we need to make the tests as repeatable as possible
            // the problem is when blueprint container defines cm:property-placeholder with update-strategy="reload"
            // updating the configuration leads to (felix framework + aries blueprint):
            // 1. schedule org.apache.felix.cm.impl.ConfigurationManager.UpdateConfiguration object to run in config admin thread
            // 2. this thread calls org.apache.felix.cm.impl.ConfigurationImpl#tryBindLocation()
            // 3. org.osgi.service.cm.ConfigurationEvent#CM_LOCATION_CHANGED is send
            // 4. org.apache.aries.blueprint.compendium.cm.ManagedObjectManager.ConfigurationWatcher#updated() is invoked
            // 5. new Thread().start() is called
            // 6. org.apache.aries.blueprint.compendium.cm.ManagedObject#updated() is called
            // 7. org.apache.aries.blueprint.compendium.cm.CmPropertyPlaceholder#updated() is called
            // 8. new Thread().start() is called
            // 9. org.apache.aries.blueprint.services.ExtendedBlueprintContainer#reload() is called which destroys everything in BP container
            // 10. finally reload of BP container is scheduled (in yet another thread)
            //
            // if we start/use camel context between point 9 and 10 we may get many different errors described in https://issues.apache.org/jira/browse/ARIES-961

            // to synchronize this (main) thread of execution with the asynchronous series of events, we can register the following listener.
            // this way be sure that we got to point 3
            final CountDownLatch latch = new CountDownLatch(2);
            answer.registerService(ConfigurationListener.class, new ConfigurationListener() {
                @Override
                public void configurationEvent(ConfigurationEvent event) {
                    if (event.getType() == ConfigurationEvent.CM_LOCATION_CHANGED) {
                        latch.countDown();
                    }
                    // when we update the configuration, BP container will be reloaded as well
                    // hoping that we get the event after *second* restart, let's register the listener
                    answer.registerService(BlueprintListener.class, new BlueprintListener() {
                        @Override
                        public void blueprintEvent(BlueprintEvent event) {
                            if (event.getType() == BlueprintEvent.CREATED && event.getBundle().getSymbolicName().equals(symbolicName)) {
                                latch.countDown();
                            }
                        }
                    }, null);
                }
            }, null);

            ConfigurationAdmin configAdmin = CamelBlueprintHelper.getOsgiService(answer, ConfigurationAdmin.class);
            // passing null as second argument ties the configuration to correct bundle.
            // using single-arg method causes:
            // *ERROR* Cannot use configuration xxx.properties for [org.osgi.service.cm.ManagedService, id=N, bundle=N/jar:file:xyz.jar!/]: No visibility to configuration bound to file:pojosr
            Configuration config = configAdmin.getConfiguration(pid, null);
            if (config == null) {
                throw new IllegalArgumentException("Cannot find configuration with pid " + pid + " in OSGi ConfigurationAdmin service.");
            }
            log.info("Updating ConfigAdmin {} by overriding properties {}", config, props);
            config.update(props);

            latch.await(CamelBlueprintHelper.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        }

        return answer;
    }
}
