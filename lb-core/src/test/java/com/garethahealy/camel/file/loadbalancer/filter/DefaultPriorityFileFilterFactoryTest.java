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
package com.garethahealy.camel.file.loadbalancer.filter;

import org.junit.Assert;
import org.junit.Test;

public class DefaultPriorityFileFilterFactoryTest {

    @Test
    public void canCreateFactoryAndGetItem() {
        PriorityFileFilterFactory factory = new DefaultPriorityFileFilterFactory(1, 1);
        factory.init();

        PriorityFileFilter filter = factory.get();

        Assert.assertNotNull(filter);
        Assert.assertNotNull(filter.getPriorityName());

        Assert.assertEquals("0", filter.getPriorityName());
    }

    @Test
    public void canCreateFactoryAndGetMultipleItems() {
        PriorityFileFilterFactory factory = new DefaultPriorityFileFilterFactory(3, 1);
        factory.init();

        PriorityFileFilter filter1 = factory.get();
        PriorityFileFilter filter2 = factory.get();
        PriorityFileFilter filter3 = factory.get();

        Assert.assertNotNull(filter1);
        Assert.assertNotNull(filter1.getPriorityName());
        Assert.assertEquals("0", filter1.getPriorityName());

        Assert.assertNotNull(filter2);
        Assert.assertNotNull(filter2.getPriorityName());
        Assert.assertEquals("1", filter2.getPriorityName());

        Assert.assertNotNull(filter3);
        Assert.assertNotNull(filter3.getPriorityName());
        Assert.assertEquals("2", filter3.getPriorityName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void callGetTooManyTimesGetAnException() {
        PriorityFileFilterFactory factory = new DefaultPriorityFileFilterFactory(1, 1);
        factory.init();

        factory.get();
        factory.get();
    }

    @Test(expected = IllegalArgumentException.class)
    public void dontSetAmountOfWatchersGreaterThanZeroGetAnExpcetion() {
        PriorityFileFilterFactory factory = new DefaultPriorityFileFilterFactory(0, 1);
        factory.init();
    }

    @Test(expected = IllegalArgumentException.class)
    public void dontSetMaxMessagesPerPollGreaterThanZeroGetAnExpcetion() {
        PriorityFileFilterFactory factory = new DefaultPriorityFileFilterFactory(1, 0);
        factory.init();
    }

    @Test(expected = IllegalArgumentException.class)
    public void dontCallInitGetAnExpcetion() {
        PriorityFileFilterFactory factory = new DefaultPriorityFileFilterFactory(1, 1);
        factory.get();
    }

    @Test
    public void getMaxMessagesPerPollReturns() {
        PriorityFileFilterFactory factory = new DefaultPriorityFileFilterFactory(1, 1);
        int max = factory.getMaxMessagesPerPoll();

        Assert.assertEquals(1, max);
    }
}
