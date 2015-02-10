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

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileFilter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PriorityFileFilter implements GenericFileFilter {

    private static final Logger LOG = LoggerFactory.getLogger(PriorityFileFilter.class);

    private AtomicInteger counter = new AtomicInteger(0);
    private int priority;

    public PriorityFileFilter(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public void resetCounter() {
        boolean isDirty = counter.get() > 0;
        if (isDirty) {
            //Resets the counter, as we are polling again so need to be fresh
            LOG.debug("Resetting counter for PriorityFileFilter '{}' for {}#{}", priority, PriorityFileFilter.class.getName(), hashCode());

            counter.set(0);
        }
    }

    @Override
    public boolean accept(GenericFile file) {
        //Only accept if the counter is the same as the priority
        //i.e: first priority gets first file, second priority gets second file, and so on

        int currentCount = counter.getAndIncrement();
        boolean isMatched = currentCount == priority;

        LOG.debug("file: {}, count: {}, priority: {}, isMatched: {}", file.getFileName(), currentCount, priority, isMatched);
        return isMatched;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("counter", counter)
            .append("priority", priority)
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
