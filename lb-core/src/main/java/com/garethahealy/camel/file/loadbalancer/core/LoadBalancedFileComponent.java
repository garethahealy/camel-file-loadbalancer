/*
 * #%L
 * GarethHealy :: Camel File Loadbalancer :: Core
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
package com.garethahealy.camel.file.loadbalancer.core;

import java.io.File;
import java.util.Map;

import com.garethahealy.camel.file.loadbalancer.filter.PriorityFileFilter;
import com.garethahealy.camel.file.loadbalancer.filter.PriorityFileFilterFactory;

import org.apache.camel.CamelContext;
import org.apache.camel.ResolveEndpointFailedException;
import org.apache.camel.component.file.FileComponent;
import org.apache.camel.component.file.GenericFileEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadBalancedFileComponent extends FileComponent {

    private static final Logger LOG = LoggerFactory.getLogger(LoadBalancedFileComponent.class);

    public LoadBalancedFileComponent(CamelContext context) {
        super(context);
        setEndpointClass(LoadBalancedFileEndpoint.class);
    }

    @Override
    protected GenericFileEndpoint<File> buildFileEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        //Use the base camel code to create the endpoint config, and then dump it into our endpoint
        GenericFileEndpoint<File> camelFileEndpoint = super.buildFileEndpoint(uri, remaining, parameters);

        LoadBalancedFileEndpoint answer = new LoadBalancedFileEndpoint(uri, this);
        answer.setFile(new File(remaining));
        answer.setConfiguration(camelFileEndpoint.getConfiguration());

        return answer;
    }

    @Override
    protected void afterPropertiesSet(GenericFileEndpoint<File> endpoint) throws Exception {
        super.afterPropertiesSet(endpoint);

        if (endpoint instanceof LoadBalancedFileEndpoint) {
            LoadBalancedFileEndpoint lbEndpoint = (LoadBalancedFileEndpoint)endpoint;

            PriorityFileFilterFactory factory = lbEndpoint.getPriorityFileFilterFactory();
            if (factory == null) {
                throw new ResolveEndpointFailedException(lbEndpoint.getEndpointUri(), "PriorityFileFilterFactory is null");
            }

            updateFilter(lbEndpoint, factory);
            updateMaxMessagesPerPoll(lbEndpoint, factory);
            updateMove(lbEndpoint);
        }
    }

    /**
     * Update the filter with one thats been created by the factory
     *
     * @param lbEndpoint
     * @param factory
     */
    private void updateFilter(LoadBalancedFileEndpoint lbEndpoint, PriorityFileFilterFactory factory) {
        //
        if (lbEndpoint.getFilter() == null) {
            LOG.debug("Updating Filter as not set for #{}", lbEndpoint.hashCode());

            lbEndpoint.setFilter(factory.get());
        } else {
            throw new ResolveEndpointFailedException(lbEndpoint.getEndpointUri(), "Filter is set, which stops the PriorityFileFilterFactory overriding");
        }

    }

    /**
     * Update the MaxMessagesPerPoll to match what the factory was setup with, so we don't get competing files
     *
     * @param lbEndpoint
     * @param factory
     */
    private void updateMaxMessagesPerPoll(LoadBalancedFileEndpoint lbEndpoint, PriorityFileFilterFactory factory) {
        if (lbEndpoint.getMaxMessagesPerPoll() <= 0) {
            LOG.debug("Updating MaxMessagesPerPoll from '{}' to '{}' for #{}", lbEndpoint.getMaxMessagesPerPoll(), factory.getMaxMessagesPerPoll(), lbEndpoint.hashCode());

            lbEndpoint.setMaxMessagesPerPoll(factory.getMaxMessagesPerPoll());
        } else {
            String message = String.format("MaxMessagesPerPoll is set as '%s' which does not match the factory '%s'", lbEndpoint.getMaxMessagesPerPoll(),
                                           factory.getMaxMessagesPerPoll());

            throw new ResolveEndpointFailedException(lbEndpoint.getEndpointUri(), message);
        }
    }

    /**
     * Update Move so we can easily track whats files were handled by what endpoint
     *
     * @param lbEndpoint
     */
    private void updateMove(LoadBalancedFileEndpoint lbEndpoint) {
        if (lbEndpoint.getFilter() instanceof PriorityFileFilter) {
            PriorityFileFilter filter = (PriorityFileFilter)lbEndpoint.getFilter();

            //Set the move directory to contain the priority so its easy to distinguish what moved the file
            String currentMove = lbEndpoint.getMove() == null ? "" : lbEndpoint.getMove().toString();
            String move = ".camel";
            if (currentMove.length() > 0) {
                String[] moveSplit = currentMove.split("/");
                if (moveSplit.length == 3) {
                    move = moveSplit[1];
                }
            }

            LOG.debug("Updating Move from '{}' to '{}' for #{}", currentMove, move + filter.getPriorityName(), lbEndpoint.hashCode());

            lbEndpoint.setMove(move + filter.getPriorityName());
        }
    }
}
