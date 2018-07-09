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

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPriorityFileFilterFactory implements PriorityFileFilterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPriorityFileFilterFactory.class);

    private int amountOfWatchers;
    private int maxMessagesPerPoll;
    private AtomicBoolean inited = new AtomicBoolean(false);
    private AtomicInteger counter = new AtomicInteger(0);
    private ConcurrentMap<Integer, PriorityFileFilter<File>> holder;

    public DefaultPriorityFileFilterFactory(int amountOfWatchers, int maxMessagesPerPoll) {
        this.amountOfWatchers = amountOfWatchers;
        this.maxMessagesPerPoll = maxMessagesPerPoll;
    }

    @Override
    public void init() {
        if (!inited.get()) {
            inited.set(true);

            if (amountOfWatchers <= 0) {
                throw new IllegalArgumentException("AmountOfWatchers is less/equal to 0. Must be positive");
            }

            if (maxMessagesPerPoll <= 0) {
                throw new IllegalArgumentException("MaxMessagesPerPoll is less/equal to 0. Must be positive");
            }

            if (holder == null) {
                holder = new ConcurrentHashMap<Integer, PriorityFileFilter<File>>();
            }

            holder.clear();
            counter.set(0);

            LOG.info("Initializing {} watchers with {} max messages per poll", amountOfWatchers, maxMessagesPerPoll);

            for (int i = 0; i < amountOfWatchers; i++) {
                PriorityFileFilter<File> filter = new PriorityFileFilter<File>(i, amountOfWatchers, maxMessagesPerPoll);
                filter.init();

                holder.put(i, filter);
            }

            LOG.info("Created a holder of '{}' as amountOfWatchers is '{}'", holder.size(), amountOfWatchers);
        }
    }

    @Override
    public synchronized PriorityFileFilter<File> get() {
        if (holder == null || holder.size() <= 0) {
            throw new IllegalArgumentException("Have you called init?, because holder is null or empty");
        }

        int currentCount = counter.getAndIncrement();
        if (currentCount >= holder.size()) {
            throw new IllegalArgumentException("Got called too many times. Counter is '"
                                               + currentCount + "' but holder only has '" + holder.size()
                                               + "' with amount of watchers as '" + amountOfWatchers + "'");
        }

        return holder.get(currentCount);
    }

    @Override
    public synchronized int getMaxMessagesPerPoll() {
        return maxMessagesPerPoll;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("amountOfWatchers", amountOfWatchers)
            .append("maxMessagesPerPoll", maxMessagesPerPoll)
            .append("inited", inited)
            .append("counter", counter)
            .append("holder", holder)
            .toString();
    }
}
