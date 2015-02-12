/*
 * #%L
 * camel-activemq-transacted
 * %%
 * Copyright (C) 2013 - 2014 Gareth Healy
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.garethahealy.camel.file.loadbalancer.core.LoadBalancedFileEndpoint;
import com.garethahealy.camel.file.loadbalancer.filter.PriorityFileFilter;

import org.apache.camel.Endpoint;
import org.apache.camel.component.file.GenericFileFilter;
import org.apache.camel.model.FromDefinition;
import org.apache.camel.model.RouteDefinition;
import org.junit.Assert;

public class BaseCamelBlueprintTestSupport extends CamelBlueprintTestSupportFix7469 {

    @Override
    protected String getBlueprintDescriptor() {
        return "OSGI-INF/blueprint/lb-example1-context.xml";
    }

    @Override
    public boolean isCreateCamelContextPerClass() {
        return true;
    }

    /**
     * Get a map with the route id (key) and the priority (value) for 'from' endpoint
     *
     * @return
     */
    protected Map<String, String> getRouteToEndpointPriority() {
        Map<String, String> answer = new HashMap<String, String>();

        List<RouteDefinition> routes = context.getRouteDefinitions();

        Assert.assertNotNull(routes);

        for (RouteDefinition current : routes) {
            Assert.assertEquals(new Integer(1), new Integer(current.getInputs().size()));
            Assert.assertNotNull(current.getInputs().get(0));
            Assert.assertNotNull(current.getId());
            Assert.assertTrue(current.getId().trim().length() > 0);

            FromDefinition from = current.getInputs().get(0);
            Endpoint endpoint = getMandatoryEndpoint(from.getUri());

            Assert.assertNotNull(endpoint);
            Assert.assertTrue(endpoint instanceof LoadBalancedFileEndpoint);

            LoadBalancedFileEndpoint lbEndpoint = (LoadBalancedFileEndpoint)endpoint;
            GenericFileFilter filter = lbEndpoint.getFilter();

            Assert.assertNotNull(filter);
            Assert.assertTrue(filter instanceof PriorityFileFilter);

            PriorityFileFilter priorityFileFilter = (PriorityFileFilter)filter;

            Assert.assertNotNull(priorityFileFilter.getPriorityName());

            answer.put(current.getId(), priorityFileFilter.getPriorityName());
        }

        return answer;
    }

    protected void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException ex) {
            //ignore
        }
    }
}
