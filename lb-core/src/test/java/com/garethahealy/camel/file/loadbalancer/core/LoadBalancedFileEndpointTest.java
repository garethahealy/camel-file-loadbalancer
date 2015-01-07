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

import java.util.ArrayList;
import java.util.List;

import com.garethahealy.camel.file.loadbalancer.filter.PriorityFileFilter;

import org.junit.Assert;
import org.junit.Test;

public class LoadBalancedFileEndpointTest extends BaseCamelBlueprintTestSupport {

    @Test
    public void canCreateEndpoint() throws Exception {
        String uri = "lb-file://" + rootDirectory + "?initialDelay=1s&delay=10s&fileFilters=#fileFiltersList&filter=#firstLoadBalancedFileFilter&runLoggingLevel=INFO";

        List<PriorityFileFilter> filters = new ArrayList<PriorityFileFilter>();
        filters.add(new PriorityFileFilter(1));

        LoadBalancedFileEndpoint loadBalancedFileEndpoint = new LoadBalancedFileEndpoint(uri, new LoadBalancedFileComponent(context));
        loadBalancedFileEndpoint.setFileFilters(filters);

        Assert.assertNotNull(loadBalancedFileEndpoint.getSortBy());
        Assert.assertNotNull(loadBalancedFileEndpoint.getReadLock());
        Assert.assertNotNull(loadBalancedFileEndpoint.getFileFilters());

        Assert.assertEquals(1, loadBalancedFileEndpoint.getFileFilters().size());
    }
}
