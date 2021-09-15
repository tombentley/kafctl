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

import com.github.tombentley.kafctl.format.TopicsOutput;
import com.github.tombentley.kafctl.util.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.TopicListing;
import picocli.CommandLine;

@CommandLine.Command(
        name = "topic",
        aliases = "topics",
        description = "Gets the named topics' state or config.",
        subcommands = {
                GetTopicState.class,
                GetTopicConfig.class
        }
)
public class GetTopic implements Runnable {

        @CommandLine.Option(names = {"--output", "-o"},
                description = "The output format. Valid values: ${COMPLETION-CANDIDATES}",
                defaultValue = "table",
                converter = TopicsOutput.OutputFormatConverter.class,
                completionCandidates = TopicsOutput.OutputFormatConverter.class)
        TopicsOutput output;

        @Inject
        AdminClient adminClient;

        @CommandLine.Option(names = {"--show-internal"}, defaultValue = "false",
                description = "Whether to show internal topics like __consumer_offsets.")
        boolean showInternal;

        @Override
        public void run() {
                // TODO table out with internal column
                // TODO sort by internal first, then name?

                adminClient.withAdmin(admin -> {
                        ArrayList<TopicListing> listing = new ArrayList<>(admin.listTopics(new ListTopicsOptions().listInternal(showInternal)).listings().get());
                        listing.sort(Comparator.comparing(t -> t.name()));
                        System.out.println(output.listTopics(listing));
                        return null;
                });
        }

}
