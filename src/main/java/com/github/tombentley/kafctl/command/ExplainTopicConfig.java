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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.github.tombentley.kafctl.format.DescribeConfigsOutput;
import com.github.tombentley.kafctl.util.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.DescribeConfigsOptions;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.common.config.ConfigResource;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "config", description = "Describes topic configs.")
public class ExplainTopicConfig implements Runnable {
    @Option(names = {"--output", "-o"},
            description = "The output format. Valid values: ${COMPLETION-CANDIDATES}",
            defaultValue = "plain",
            converter = DescribeConfigsOutput.OutputFormatConverter.class,
            completionCandidates = DescribeConfigsOutput.OutputFormatConverter.class)
    DescribeConfigsOutput output;

    // TODO config name arguments to specify an || filter

    @Inject
    AdminClient adminClient;

    @Override
    public void run() {
        adminClient.withAdmin(admin -> {
            // TODO This is a horrible hack around the fact that Kafka only exposes config descriptions by describing existing entities
            // I.e. you need a topic to be able to query a broker for topic configs.
            Set<String> topicNames = admin.listTopics(new ListTopicsOptions().listInternal(true)).names().get();
            ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, topicNames.iterator().next());
            Config config = admin.describeConfigs(
                    List.of(configResource),
                    new DescribeConfigsOptions()
                            .includeDocumentation(true)
                            .includeSynonyms(true)).values().get(configResource).get();
            System.out.println(output.describeConfigs(config.entries()));


            return null;
        });

    }
}
