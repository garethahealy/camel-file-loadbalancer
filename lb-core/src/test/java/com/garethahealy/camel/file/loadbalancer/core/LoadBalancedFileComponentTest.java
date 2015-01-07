/*
 * #%L
 * lb-core
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
package com.garethahealy.camel.file.loadbalancer.core;

import com.garethahealy.camel.file.loadbalancer.filter.PriorityFileFilter;

import org.apache.camel.Endpoint;
import org.apache.camel.ResolveEndpointFailedException;
import org.junit.Assert;
import org.junit.Test;

public class LoadBalancedFileComponentTest extends BaseCamelBlueprintTestSupport {

    @Test
    public void canCreateComponent() {
        LoadBalancedFileComponent component = new LoadBalancedFileComponent(context);

        Assert.assertNotNull(component);
        Assert.assertNotNull(component.getEndpointClass());
        Assert.assertEquals(LoadBalancedFileEndpoint.class, component.getEndpointClass());
    }

    @Test
    public void createEndpointWorks() throws Exception {
        String uri = "lb-file://" + rootDirectory + "?initialDelay=1s&delay=10s&fileFilters=#fileFiltersList&filter=#firstLoadBalancedFileFilter&runLoggingLevel=INFO";
        LoadBalancedFileComponent component = new LoadBalancedFileComponent(context);
        Endpoint endpoint = component.createEndpoint(uri);

        Assert.assertNotNull(endpoint);
        Assert.assertEquals(LoadBalancedFileEndpoint.class, endpoint.getClass());

        LoadBalancedFileEndpoint loadBalancedFileEndpoint = (LoadBalancedFileEndpoint)endpoint;

        Assert.assertNotNull(loadBalancedFileEndpoint.getSortBy());
        Assert.assertNotNull(loadBalancedFileEndpoint.getReadLock());
        Assert.assertNotNull(loadBalancedFileEndpoint.getFileFilters());
        Assert.assertNotNull(loadBalancedFileEndpoint.getFilter());
        Assert.assertNotNull(loadBalancedFileEndpoint.getMove());
        Assert.assertNotNull(loadBalancedFileEndpoint.getMaxMessagesPerPoll());

        Assert.assertEquals("markerFile", loadBalancedFileEndpoint.getReadLock());
        Assert.assertEquals(1, loadBalancedFileEndpoint.getFileFilters().size());
        Assert.assertEquals(PriorityFileFilter.class, loadBalancedFileEndpoint.getFilter().getClass());
        Assert.assertEquals("${file:parent}/.camel0/${file:onlyname}", loadBalancedFileEndpoint.getMove().toString());
        Assert.assertEquals(1, loadBalancedFileEndpoint.getMaxMessagesPerPoll());
    }

    @Test
    public void createEndpointHonoursMove() throws Exception {
        String uri = "lb-file://" + rootDirectory + "?initialDelay=1s&delay=10s&fileFilters=#fileFiltersList&filter=#firstLoadBalancedFileFilter&runLoggingLevel=INFO&move=.camel";
        LoadBalancedFileComponent component = new LoadBalancedFileComponent(context);
        Endpoint endpoint = component.createEndpoint(uri);

        Assert.assertNotNull(endpoint);
        Assert.assertEquals(LoadBalancedFileEndpoint.class, endpoint.getClass());

        LoadBalancedFileEndpoint loadBalancedFileEndpoint = (LoadBalancedFileEndpoint)endpoint;

        Assert.assertNotNull(loadBalancedFileEndpoint.getSortBy());
        Assert.assertNotNull(loadBalancedFileEndpoint.getReadLock());
        Assert.assertNotNull(loadBalancedFileEndpoint.getFileFilters());
        Assert.assertNotNull(loadBalancedFileEndpoint.getFilter());
        Assert.assertNotNull(loadBalancedFileEndpoint.getMove());
        Assert.assertNotNull(loadBalancedFileEndpoint.getMaxMessagesPerPoll());

        Assert.assertEquals("markerFile", loadBalancedFileEndpoint.getReadLock());
        Assert.assertEquals(1, loadBalancedFileEndpoint.getFileFilters().size());
        Assert.assertEquals(PriorityFileFilter.class, loadBalancedFileEndpoint.getFilter().getClass());
        Assert.assertEquals("${file:parent}/.camel0/${file:onlyname}", loadBalancedFileEndpoint.getMove().toString());
        Assert.assertEquals(1, loadBalancedFileEndpoint.getMaxMessagesPerPoll());
    }

    @Test(expected = ResolveEndpointFailedException.class)
    public void createEndpointThrowsExceptionIfMaxMessagesPerPollSet() throws Exception {
        String uri =
            "lb-file://" + rootDirectory + "?initialDelay=1s&delay=10s&fileFilters=#fileFiltersList&filter=#firstLoadBalancedFileFilter&runLoggingLevel=INFO&maxMessagesPerPoll=10";
        LoadBalancedFileComponent component = new LoadBalancedFileComponent(context);
        component.createEndpoint(uri);
    }
}
