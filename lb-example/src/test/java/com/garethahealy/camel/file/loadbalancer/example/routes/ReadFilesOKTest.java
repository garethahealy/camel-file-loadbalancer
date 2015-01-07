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
package com.garethahealy.camel.file.loadbalancer.example.routes;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.camel.component.mock.MockEndpoint;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.junit.Assert;
import org.junit.Test;

public class ReadFilesOKTest extends BaseCamelBlueprintTestSupport {

    private String rootDirectory = System.getProperty("user.dir") + "/target/files";

    protected void doPreSetup() throws Exception {
        File directory = FileUtils.toFile(new URL("file:" + rootDirectory));
        directory.mkdir();

        URL file1 = ReadFilesOKTest.class.getClassLoader().getResource("example-files/file1.log");
        URL file2 = ReadFilesOKTest.class.getClassLoader().getResource("example-files/file2.log");
        URL file3 = ReadFilesOKTest.class.getClassLoader().getResource("example-files/file3.log");

        Assert.assertNotNull(file1);
        Assert.assertNotNull(file2);
        Assert.assertNotNull(file3);

        FileUtils.copyFileToDirectory(FileUtils.toFile(file1), directory);
        FileUtils.copyFileToDirectory(FileUtils.toFile(file2), directory);
        FileUtils.copyFileToDirectory(FileUtils.toFile(file3), directory);
    }

    private void sleep() {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException ex) {
            //ignore
        }
    }

    @Test
    public void canReadFile() throws InterruptedException, MalformedURLException {
        MockEndpoint first = getMockEndpoint("mock:endFirst");
        first.setExpectedMessageCount(1);
        first.expectedBodiesReceived("file1.log");

        MockEndpoint second = getMockEndpoint("mock:endSecond");
        second.setExpectedMessageCount(1);
        second.expectedBodiesReceived("file2.log");

        MockEndpoint third = getMockEndpoint("mock:endThird");
        third.setExpectedMessageCount(1);
        third.expectedBodiesReceived("file3.log");

        sleep();

        first.assertIsSatisfied();
        second.assertIsSatisfied();
        third.assertIsSatisfied();

        Collection<File> firstFiles = FileUtils.listFiles(FileUtils.toFile(new URL("file:" + rootDirectory + "/.camel0")), FileFilterUtils.prefixFileFilter("file1.log"), null);
        Collection<File> secondFiles = FileUtils.listFiles(FileUtils.toFile(new URL("file:" + rootDirectory + "/.camel1")), FileFilterUtils.prefixFileFilter("file2.log"), null);
        Collection<File> thirdFiles = FileUtils.listFiles(FileUtils.toFile(new URL("file:" + rootDirectory + "/.camel2")), FileFilterUtils.prefixFileFilter("file3.log"), null);

        Assert.assertNotNull(firstFiles);
        Assert.assertNotNull(secondFiles);
        Assert.assertNotNull(thirdFiles);
    }
}
