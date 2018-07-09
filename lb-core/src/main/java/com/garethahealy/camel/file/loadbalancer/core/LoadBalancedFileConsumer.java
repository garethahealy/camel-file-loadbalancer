/*
 * #%L
 * GarethHealy :: Camel File Loadbalancer :: Core
 * %%
 * Copyright (C) 2013 - 2018 Gareth Healy
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
}
