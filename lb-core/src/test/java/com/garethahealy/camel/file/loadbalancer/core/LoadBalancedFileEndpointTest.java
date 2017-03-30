/*
 * #%L
 * GarethHealy :: Camel File Loadbalancer :: Core
 * %%
 * Copyright (C) 2013 - 2017 Gareth Healy
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
package com.garethahealy.camel.file.loadbalancer.core;

import com.garethahealy.camel.file.loadbalancer.filter.DefaultPriorityFileFilterFactory;
import com.garethahealy.camel.file.loadbalancer.filter.PriorityFileFilterFactory;

import org.junit.Assert;
import org.junit.Test;

public class LoadBalancedFileEndpointTest extends BaseCamelBlueprintTestSupport {

    @Test
    public void canCreateEndpoint() throws Exception {
        String uri = "lb-file://" + rootDirectory + "?initialDelay=1s&delay=10s&priorityFileFilterFactory=#defaultPriorityFileFilterFactory&runLoggingLevel=INFO";

        PriorityFileFilterFactory factory = new DefaultPriorityFileFilterFactory(1, 1);
        factory.init();

        LoadBalancedFileEndpoint loadBalancedFileEndpoint = new LoadBalancedFileEndpoint(uri, new LoadBalancedFileComponent(context));
        loadBalancedFileEndpoint.setPriorityFileFilterFactory(factory);

        Assert.assertNotNull(loadBalancedFileEndpoint.getSortBy());
        Assert.assertNotNull(loadBalancedFileEndpoint.getReadLock());
        Assert.assertNotNull(loadBalancedFileEndpoint.getPriorityFileFilterFactory());

        Assert.assertEquals(1, loadBalancedFileEndpoint.getPriorityFileFilterFactory().getMaxMessagesPerPoll());
    }
}
