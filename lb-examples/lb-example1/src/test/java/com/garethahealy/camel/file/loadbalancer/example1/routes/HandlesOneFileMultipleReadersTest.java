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
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.junit.Assert;
import org.junit.Test;

public class HandlesOneFileMultipleReadersTest extends BaseCamelBlueprintTestSupport {

    private String rootDirectory = System.getProperty("user.dir") + "/target/files1";

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

        URL file1 = HandlesOneFileMultipleReadersTest.class.getClassLoader().getResource("example-files/afile1.log");

        Assert.assertNotNull(file1);

        FileUtils.copyFileToDirectory(FileUtils.toFile(file1), directory);
    }

    @Test
    public void handlesOneFileMultipleReaders() throws InterruptedException, MalformedURLException {
        MockEndpoint first = getMockEndpoint("mock:endFirst");
        first.setExpectedMessageCount(1);
        first.expectedBodiesReceived("afile1.log");
        first.setResultWaitTime(TimeUnit.SECONDS.toMillis(15));
        first.setAssertPeriod(TimeUnit.SECONDS.toMillis(1));

        MockEndpoint second = getMockEndpoint("mock:endSecond");
        second.setExpectedMessageCount(0);
        second.setResultWaitTime(TimeUnit.SECONDS.toMillis(15));
        second.setAssertPeriod(TimeUnit.SECONDS.toMillis(1));

        MockEndpoint third = getMockEndpoint("mock:endThird");
        third.setExpectedMessageCount(0);
        third.setResultWaitTime(TimeUnit.SECONDS.toMillis(15));
        third.setAssertPeriod(TimeUnit.SECONDS.toMillis(1));

        first.assertIsSatisfied();
        second.assertIsSatisfied();
        third.assertIsSatisfied();

        File firstDirectory = FileUtils.toFile(new URL("file:" + rootDirectory + "/.camel0"));
        File secondDirectory = FileUtils.toFile(new URL("file:" + rootDirectory + "/.camel1"));
        File thirdDirectory = FileUtils.toFile(new URL("file:" + rootDirectory + "/.camel2"));

        Assert.assertTrue(firstDirectory.exists());
        Assert.assertFalse(secondDirectory.exists());
        Assert.assertFalse(thirdDirectory.exists());

        Collection<File> firstFiles = FileUtils.listFiles(firstDirectory, FileFilterUtils.prefixFileFilter("afile1.log"), null);

        Assert.assertNotNull(firstFiles);
        Assert.assertFalse(FileUtils.toFile(new URL("file:" + rootDirectory + "/.camel1")).exists());
        Assert.assertFalse(FileUtils.toFile(new URL("file:" + rootDirectory + "/.camel2")).exists());

        Assert.assertEquals(new Integer(1), new Integer(firstFiles.size()));
    }
}
