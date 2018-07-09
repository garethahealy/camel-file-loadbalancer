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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PriorityFileFilter<T> implements GenericFileFilter<T> {

    private static final Logger LOG = LoggerFactory.getLogger(PriorityFileFilter.class);

    private AtomicInteger counter = new AtomicInteger(0);
    private Integer priority;
    private Integer amountOfWatchers;
    private Integer maxMessagesPerPoll;
    private Set<Integer> possiblePriorities;
    private String cachedPossiblePrioritiesString;

    public PriorityFileFilter(Integer priority, Integer amountOfWatchers, Integer maxMessagesPerPoll) {
        this.priority = priority;
        this.amountOfWatchers = amountOfWatchers;
        this.maxMessagesPerPoll = maxMessagesPerPoll;
    }

    /**
     * Build a list of priorities which this filter will accept
     * i.e.: we have 5 amountOfWatchers with 100 maxMessagesPerPoll and a priority starting at 0.
     * Keep adding 5 until we hit 100, so we end up with: 0, 5, 10, 15 and so on
     */
    public void init() {
        possiblePriorities = new LinkedHashSet<Integer>();

        int current = priority;
        while (current < maxMessagesPerPoll) {
            possiblePriorities.add(current);

            current += amountOfWatchers;
            if (current > maxMessagesPerPoll) {
                break;
            }
        }

        //Priority is greater than the maxMessagesPerPoll, so just add that value
        if (possiblePriorities.size() <= 0) {
            possiblePriorities.add(priority);
        }

        cachedPossiblePrioritiesString = StringUtils.join(possiblePriorities, ",");

        LOG.debug("Created possible priorities of '{}' for '{}'", cachedPossiblePrioritiesString, priority);
    }

    public Set<Integer> getPossiblePriorities() {
        return possiblePriorities;
    }

    public String getPriorityName() {
        return Integer.toString(priority);
    }

    /**
     * Only accept if the counter is the same as the priority
     * i.e: first priority gets first file, second priority gets second file, and so on
     *
     * @param file
     * @return
     */
    @Override
    public boolean accept(GenericFile<T> file) {
        if (possiblePriorities == null || possiblePriorities.size() <= 0) {
            throw new IllegalStateException("Possible priorities is null or empty. Have you called init?");
        }

        boolean isPastMaxMessagesPerPoll = counter.get() > maxMessagesPerPoll;
        if (isPastMaxMessagesPerPoll) {
            counter.set(0);
        }

        int currentCount = counter.getAndIncrement();
        boolean isMatched = possiblePriorities.contains(currentCount);

        LOG.debug("{}, isMatched: {}", toString(), isMatched);

        return isMatched;
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
            .append("counter", counter)
            .append("priority", priority)
            .append("amountOfWatchers", amountOfWatchers)
            .append("maxMessagesPerPoll", maxMessagesPerPoll)
            .append("possiblePriorities", possiblePriorities)
            .append("priorityName", getPriorityName())
            .toString();
    }
}
