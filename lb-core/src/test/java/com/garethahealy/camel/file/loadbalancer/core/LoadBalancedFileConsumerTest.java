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

import org.apache.camel.builder.ExpressionBuilder;
import org.apache.camel.component.file.FileOperations;
import org.apache.camel.processor.LogProcessor;
import org.apache.camel.util.CamelLogger;
import org.junit.Assert;
import org.junit.Test;

public class LoadBalancedFileConsumerTest extends BaseCamelBlueprintTestSupport {

    @Test
    public void canCreateConsumer() {
        String uri = "lb-file://" + rootDirectory + "?initialDelay=1s&delay=10s&priorityFileFilterFactory=#defaultPriorityFileFilterFactory&runLoggingLevel=INFO";

        LogProcessor processor = new LogProcessor(ExpressionBuilder.simpleExpression("${body}"), new CamelLogger());
        LoadBalancedFileEndpoint loadBalancedFileEndpoint = new LoadBalancedFileEndpoint(uri, new LoadBalancedFileComponent(context));

        LoadBalancedFileConsumer consumer = new LoadBalancedFileConsumer(loadBalancedFileEndpoint, processor, new FileOperations());

        Assert.assertNotNull(consumer);
    }
}
