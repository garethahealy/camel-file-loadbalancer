/*
 * #%L
 * lb-example
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.garethahealy.camel.file.loadbalancer.core.LoadBalancedFileEndpoint;
import com.garethahealy.camel.file.loadbalancer.filter.PriorityFileFilter;

import org.apache.camel.Endpoint;
import org.apache.camel.component.file.GenericFileFilter;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.FromDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadThreeFilesWithThreeReadersTest extends BaseCamelBlueprintTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(ReadThreeFilesWithThreeReadersTest.class);

    private String rootDirectory = System.getProperty("user.dir") + "/target/files";

    @Override
    public boolean isCreateCamelContextPerClass() {
        return true;
    }

    @Override
    protected String useOverridePropertiesWithConfigAdmin(Dictionary props) throws Exception {
        props.put("lb.path", rootDirectory);

        return "com.garethahealy.camel.file.loadbalancer.example1";
    }

    @Override
    protected void doPreSetup() throws Exception {
        File directory = FileUtils.toFile(new URL("file:" + rootDirectory));
        directory.mkdir();

        URL file1 = ReadThreeFilesWithThreeReadersTest.class.getClassLoader().getResource("example-files/afile1.log");
        URL file2 = ReadThreeFilesWithThreeReadersTest.class.getClassLoader().getResource("example-files/bfile2.log");
        URL file3 = ReadThreeFilesWithThreeReadersTest.class.getClassLoader().getResource("example-files/cfile3.log");

        Assert.assertNotNull(file1);
        Assert.assertNotNull(file2);
        Assert.assertNotNull(file3);

        FileUtils.copyFileToDirectory(FileUtils.toFile(file1), directory);
        FileUtils.copyFileToDirectory(FileUtils.toFile(file2), directory);
        FileUtils.copyFileToDirectory(FileUtils.toFile(file3), directory);
    }

    /**
     * Get a map with the route id (key) and the priority (value) for 'from' endpoint
     *
     * @return
     */
    private Map<String, String> getRouteToEndpointPriority() {
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

    private void sleep() {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException ex) {
            //ignore
        }
    }

    @Test
    public void readThreeFilesWithThreeReaders() throws InterruptedException, MalformedURLException {
        Map<String, String> answer = getRouteToEndpointPriority();

        //Used for debugging purposes, in-case we need to know which endpoint has what priority
        LOG.info("EndpointSetup: " + answer.toString());

        MockEndpoint first = getMockEndpoint("mock:endFirst");
        first.setExpectedMessageCount(1);
        first.setResultWaitTime(TimeUnit.SECONDS.toMillis(15));
        first.setAssertPeriod(TimeUnit.SECONDS.toMillis(1));

        MockEndpoint second = getMockEndpoint("mock:endSecond");
        second.setExpectedMessageCount(1);
        second.setResultWaitTime(TimeUnit.SECONDS.toMillis(15));
        second.setAssertPeriod(TimeUnit.SECONDS.toMillis(1));

        MockEndpoint third = getMockEndpoint("mock:endThird");
        third.setExpectedMessageCount(1);
        third.setResultWaitTime(TimeUnit.SECONDS.toMillis(15));
        third.setAssertPeriod(TimeUnit.SECONDS.toMillis(1));

        //Wait for the files to be processed
        sleep();

        File firstDirectory = FileUtils.toFile(new URL("file:" + rootDirectory + "/.camel0"));
        File secondDirectory = FileUtils.toFile(new URL("file:" + rootDirectory + "/.camel1"));
        File thirdDirectory = FileUtils.toFile(new URL("file:" + rootDirectory + "/.camel2"));

        Assert.assertTrue(firstDirectory.exists());
        Assert.assertTrue(secondDirectory.exists());
        Assert.assertTrue(thirdDirectory.exists());

        Collection<File> firstFiles = FileUtils.listFiles(firstDirectory, FileFilterUtils.fileFileFilter(), null);
        Collection<File> secondFiles = FileUtils.listFiles(secondDirectory, FileFilterUtils.fileFileFilter(), null);
        Collection<File> thirdFiles = FileUtils.listFiles(thirdDirectory, FileFilterUtils.fileFileFilter(), null);

        Assert.assertNotNull(firstFiles);
        Assert.assertNotNull(secondFiles);
        Assert.assertNotNull(thirdFiles);

        Assert.assertEquals(new Integer(1), new Integer(firstFiles.size()));
        Assert.assertEquals(new Integer(1), new Integer(secondFiles.size()));
        Assert.assertEquals(new Integer(1), new Integer(thirdFiles.size()));

        //Assert the endpoints last, as there seems to be a strange bug where they fail but the files have been processed,
        //so that would suggest the MockEndpoints are reporting a false-positive
        first.assertIsSatisfied();
        second.assertIsSatisfied();
        third.assertIsSatisfied();
    }
}
