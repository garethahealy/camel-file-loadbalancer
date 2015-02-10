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
import java.util.concurrent.TimeUnit;

import org.apache.camel.component.mock.MockEndpoint;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.junit.Assert;
import org.junit.Test;

public class HandlesOneFileMultipleReadersTest extends BaseCamelBlueprintTestSupport {

    private String rootDirectory = System.getProperty("user.dir") + "/target/files1";

    @Override
    protected String useOverridePropertiesWithConfigAdmin(Dictionary props) throws Exception {
        props.put("lb.path", rootDirectory);

        return "com.garethahealy.camel.file.loadbalancer.example1";
    }

    protected void doPreSetup() throws Exception {
        File directory = FileUtils.toFile(new URL("file:" + rootDirectory));
        directory.mkdir();

        URL file1 = HandlesOneFileMultipleReadersTest.class.getClassLoader().getResource("example-files/file1.log");

        Assert.assertNotNull(file1);

        FileUtils.copyFileToDirectory(FileUtils.toFile(file1), directory);
    }

    private void sleep() {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ex) {
            //ignore
        }
    }

    @Test
    public void handlesOneFileMultipleReaders() throws InterruptedException, MalformedURLException {
        MockEndpoint first = getMockEndpoint("mock:endFirst");
        first.setExpectedMessageCount(1);
        first.expectedBodiesReceived("file1.log");

        MockEndpoint second = getMockEndpoint("mock:endSecond");
        second.setExpectedMessageCount(0);

        MockEndpoint third = getMockEndpoint("mock:endThird");
        third.setExpectedMessageCount(0);

        sleep();

        first.assertIsSatisfied();
        second.assertIsSatisfied();
        third.assertIsSatisfied();

        Collection<File> firstFiles = FileUtils.listFiles(FileUtils.toFile(new URL("file:" + rootDirectory + "/.camel0")), FileFilterUtils.prefixFileFilter("file1.log"), null);

        Assert.assertNotNull(firstFiles);
        Assert.assertFalse(FileUtils.toFile(new URL("file:" + rootDirectory + "/.camel1")).exists());
        Assert.assertFalse(FileUtils.toFile(new URL("file:" + rootDirectory + "/.camel2")).exists());

        Assert.assertEquals(new Integer(1), new Integer(firstFiles.size()));
    }
}
