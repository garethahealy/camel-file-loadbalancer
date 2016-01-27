/*
 * #%L
 * GarethHealy :: Camel File Loadbalancer :: Examples :: Example1
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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.camel.component.mock.MockEndpoint;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Read99FilesWithThreeReadersTest extends BaseCamelBlueprintTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(Read99FilesWithThreeReadersTest.class);

    private String rootDirectory = System.getProperty("user.dir") + "/target/files-99";

    @Override
    protected String useOverridePropertiesWithConfigAdmin(Dictionary props) throws Exception {
        props.put("lb.path", rootDirectory);
        props.put("lb.maxMessagesPerPoll", 100);

        return "com.garethahealy.camel.file.loadbalancer.example1";
    }

    @Override
    protected void doPreSetup() throws Exception {
        File directory = FileUtils.toFile(new URL("file:" + rootDirectory));
        FileUtils.deleteDirectory(directory);

        directory.mkdir();

        for (int i = 0; i < 99; i++) {
            FileUtils.writeStringToFile(FileUtils.toFile(new URL("file:" + rootDirectory + "/file" + Integer.toString(i) + ".log")), "file" + Integer.toString(i));
        }

        LOG.info("Wrote files to: " + directory.getAbsolutePath());
    }

    @Test
    public void readThreeFilesWithThreeReaders() throws InterruptedException, MalformedURLException {
        Map<String, String> answer = getRouteToEndpointPriority();

        //Used for debugging purposes, in-case we need to know which endpoint has what priority
        LOG.info("EndpointSetup: " + answer.toString());

        MockEndpoint first = getMockEndpoint("mock:endFirst");
        first.setExpectedMessageCount(33);
        first.setResultWaitTime(TimeUnit.SECONDS.toMillis(15));
        first.setAssertPeriod(TimeUnit.SECONDS.toMillis(1));

        MockEndpoint second = getMockEndpoint("mock:endSecond");
        second.setExpectedMessageCount(33);
        second.setResultWaitTime(TimeUnit.SECONDS.toMillis(15));
        second.setAssertPeriod(TimeUnit.SECONDS.toMillis(1));

        MockEndpoint third = getMockEndpoint("mock:endThird");
        third.setExpectedMessageCount(33);
        third.setResultWaitTime(TimeUnit.SECONDS.toMillis(15));
        third.setAssertPeriod(TimeUnit.SECONDS.toMillis(1));

        //Wait for the files to be processed
        sleep(30);

        File firstDirectory = FileUtils.toFile(new URL("file:" + rootDirectory + "/.camel0"));
        File secondDirectory = FileUtils.toFile(new URL("file:" + rootDirectory + "/.camel1"));
        File thirdDirectory = FileUtils.toFile(new URL("file:" + rootDirectory + "/.camel2"));

        Assert.assertTrue(".camel0 doesnt exist", firstDirectory.exists());
        Assert.assertTrue(".camel1 doesnt exist", secondDirectory.exists());
        Assert.assertTrue(".camel2 doesnt exist", thirdDirectory.exists());

        Collection<File> firstFiles = FileUtils.listFiles(firstDirectory, FileFilterUtils.fileFileFilter(), null);
        Collection<File> secondFiles = FileUtils.listFiles(secondDirectory, FileFilterUtils.fileFileFilter(), null);
        Collection<File> thirdFiles = FileUtils.listFiles(thirdDirectory, FileFilterUtils.fileFileFilter(), null);

        Assert.assertNotNull(firstFiles);
        Assert.assertNotNull(secondFiles);
        Assert.assertNotNull(thirdFiles);

        //Check the files are unique, and we haven't copied the same file twice
        int firstSize = firstFiles.size();
        int secondSize = secondFiles.size();
        int thirdSize = thirdFiles.size();

        firstFiles.removeAll(secondFiles);
        firstFiles.removeAll(thirdFiles);

        secondFiles.removeAll(firstFiles);
        secondFiles.removeAll(thirdFiles);

        thirdFiles.removeAll(firstFiles);
        thirdFiles.removeAll(secondFiles);

        //If these numbers don't match, we duplicated a file
        Assert.assertEquals("duplicate copy in .camel0", new Integer(firstSize), new Integer(firstFiles.size()));
        Assert.assertEquals("duplicate copy in .camel1", new Integer(secondSize), new Integer(secondFiles.size()));
        Assert.assertEquals("duplicate copy in .camel2", new Integer(thirdSize), new Integer(thirdFiles.size()));

        //Check the expected copied amount is correct
        Assert.assertEquals(new Integer(33), new Integer(firstFiles.size()));
        Assert.assertEquals(new Integer(33), new Integer(secondFiles.size()));
        Assert.assertEquals(new Integer(33), new Integer(thirdFiles.size()));
        Assert.assertEquals(new Integer(99), new Integer(firstFiles.size() + secondFiles.size() + thirdFiles.size()));

        //Assert the endpoints last, as there seems to be a strange bug where they fail but the files have been processed,
        //so that would suggest the MockEndpoints are reporting a false-positive
        first.assertIsSatisfied();
        second.assertIsSatisfied();
        third.assertIsSatisfied();
    }
}
