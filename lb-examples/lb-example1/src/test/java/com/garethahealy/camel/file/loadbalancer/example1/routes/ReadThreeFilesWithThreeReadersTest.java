/*
 * #%L
 * GarethHealy :: Camel File Loadbalancer :: Examples :: Example1
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

public class ReadThreeFilesWithThreeReadersTest extends BaseCamelBlueprintTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(ReadThreeFilesWithThreeReadersTest.class);

    private String rootDirectory = System.getProperty("user.dir") + "/target/files-3";

    @Override
    protected String useOverridePropertiesWithConfigAdmin(Dictionary<String, String> props) throws Exception {
        props.put("lb.path", rootDirectory);
        props.put("lb.maxMessagesPerPoll", "3");

        return "com.garethahealy.camel.file.loadbalancer.example1";
    }

    @Override
    protected void doPreSetup() throws Exception {
        File directory = FileUtils.toFile(new URL("file:" + rootDirectory));
        FileUtils.deleteDirectory(directory);

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

        LOG.info("Moved files to: " + directory.getAbsolutePath());
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
        sleep(10);

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
        firstFiles.removeAll(secondFiles);
        firstFiles.removeAll(thirdFiles);

        secondFiles.removeAll(firstFiles);
        secondFiles.removeAll(thirdFiles);

        thirdFiles.removeAll(firstFiles);
        thirdFiles.removeAll(secondFiles);

        //Each directory should of only copied one file
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
