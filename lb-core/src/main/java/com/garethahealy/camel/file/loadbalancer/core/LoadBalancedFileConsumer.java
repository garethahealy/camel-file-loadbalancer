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

import java.io.File;
import java.util.List;

import com.garethahealy.camel.file.loadbalancer.filter.PriorityFileFilter;
import com.garethahealy.camel.file.loadbalancer.filter.PriorityFileFilterFactory;

import org.apache.camel.Processor;
import org.apache.camel.component.file.FileConsumer;
import org.apache.camel.component.file.FileEndpoint;
import org.apache.camel.component.file.GenericFileOperations;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class LoadBalancedFileConsumer extends FileConsumer {

    public LoadBalancedFileConsumer(LoadBalancedFileEndpoint endpoint, Processor processor, GenericFileOperations<File> operations) {
        super(endpoint, processor, operations);
    }

    @Override
    protected boolean prePollCheck() throws Exception {
        FileEndpoint endpoint = getEndpoint();
        if (endpoint instanceof LoadBalancedFileEndpoint) {
            LoadBalancedFileEndpoint lbEndpoint = (LoadBalancedFileEndpoint)endpoint;

            PriorityFileFilterFactory factory = lbEndpoint.getPriorityFileFilterFactory();
            factory.resetCountersOnFilters();
        }

        return super.prePollCheck();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .toString();
    }
}
