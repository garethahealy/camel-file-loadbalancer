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
package com.garethahealy.camel.file.loadbalancer.filter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPriorityFileFilterFactory implements PriorityFileFilterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPriorityFileFilterFactory.class);

    private int amountOfWatchers;
    private AtomicInteger counter = new AtomicInteger(0);
    private ConcurrentMap<Integer, PriorityFileFilter> holder;

    public DefaultPriorityFileFilterFactory(int amountOfWatchers) {
        this.amountOfWatchers = amountOfWatchers;
    }

    @Override
    public void init() {
        if (amountOfWatchers <= 0) {
            throw new IllegalArgumentException("AmountOfWatchers is less/equal to 0. Must be positive");
        }

        if (holder == null) {
            holder = new ConcurrentHashMap<Integer, PriorityFileFilter>();
        }

        holder.clear();
        counter.set(0);

        for (int i = 0; i < amountOfWatchers; i++) {
            holder.put(i, new PriorityFileFilter(i));
        }

        LOG.info("Created a holder of '{}' as amountOfWatchers is '{}'", holder.size(), amountOfWatchers);
    }

    @Override
    public PriorityFileFilter get() {
        if (holder == null || holder.size() <= 0) {
            throw new IllegalArgumentException("Have you called init?, because holder is null");
        }

        if (counter.get() >= holder.size()) {
            throw new IllegalArgumentException("Get called too many times. Counter is '"
                                               + counter.get() + "' but holder only has '" + holder.size()
                                               + "' with amount of watchers as '" + amountOfWatchers + "'");
        }

        return holder.get(counter.getAndIncrement());
    }

    @Override
    public int getAmountOfWatchers() {
        return amountOfWatchers;
    }

    @Override
    public void resetCountersOnFilters() {
        if (holder != null) {
            for (PriorityFileFilter current : holder.values()) {
                current.resetCounter();
            }
        }
    }
}
