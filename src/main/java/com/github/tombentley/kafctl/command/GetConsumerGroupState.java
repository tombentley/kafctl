/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tombentley.kafctl.command;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.github.tombentley.kafctl.format.CGroupsOutput;
import com.github.tombentley.kafctl.util.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsOptions;
import org.apache.kafka.clients.admin.ListConsumerGroupsOptions;
import picocli.CommandLine;

@CommandLine.Command(
        name = "state",
        description = "Gets the named consumer-group state"
)
public class GetConsumerGroupState implements Runnable {

        @CommandLine.Option(
                names = {"--output", "-o"},
                description = "The output format. Valid values: ${COMPLETION-CANDIDATES}",
                defaultValue = "json",
                converter = CGroupsOutput.OutputFormatConverter.class,
                completionCandidates = CGroupsOutput.OutputFormatConverter.class)
        CGroupsOutput output;

//        static class OutputFormatConverter extends AbstractEnumeratedOption<ConsumerGroupState> {
//                @Override
//                protected Map<String, Supplier<ConsumerGroupState>> map() {
//                        // TODO this is a mess
//                        return Map.of("dead", () ->ConsumerGroupState.DEAD);
//                }
//        }
//
//        @CommandLine.Option(names = {"--in-states"},
//                converter = CGroupsOutput.OutputFormatConverter.class,
//                completionCandidates = CGroupsOutput.OutputFormatConverter.class)
//        Set<ConsumerGroupState> states;

        @CommandLine.Parameters(index = "0..*", arity = "0..")
        List<String> groupNames;

        @Inject
        AdminClient adminClient;

        @Override
        public void run() {
                // TODO table out with internal column
                // TODO sort by internal first, then name?

                adminClient.withAdmin(admin -> {
                        if (groupNames == null || groupNames.isEmpty()) {
                                ArrayList<ConsumerGroupListing> listing = new ArrayList<>(admin.listConsumerGroups(new ListConsumerGroupsOptions()).all().get());
                                listing.sort(Comparator.comparing(ConsumerGroupListing::groupId));
                                System.out.println(output.listCGroups(listing));
                        } else {
                                Map<String, ConsumerGroupDescription> listing = new TreeMap<>(admin.describeConsumerGroups(groupNames, new DescribeConsumerGroupsOptions()).all().get());
                                System.out.println(output.describeCGroups(listing));
                        }
                        return null;
                });
        }

}
