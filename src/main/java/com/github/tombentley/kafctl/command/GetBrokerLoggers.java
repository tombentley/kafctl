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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.tombentley.kafctl.format.GetConfigsOutput;
import com.github.tombentley.kafctl.util.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.DescribeConfigsOptions;
import org.apache.kafka.common.config.ConfigResource;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static picocli.CommandLine.Parameters;

@Command(name = "loggers", description = "Gets the named broker configs.")
public class GetBrokerLoggers implements Runnable {
    @Option(names = {"--output", "-o"},
            description = "The output format. Valid values: ${COMPLETION-CANDIDATES}",
            defaultValue = "properties",
            converter = GetConfigsOutput.OutputFormatConverter.class,
            completionCandidates = GetConfigsOutput.OutputFormatConverter.class)
    GetConfigsOutput output;

    @Option(names = "--docs", defaultValue = "false")
    boolean includeDocs;

    @Option(names = "--synonyms", defaultValue = "false")
    boolean includeSynonyms;

    @Parameters(index = "0..*", arity = "1..")
    List<Integer> brokerIds;

    @Inject
    AdminClient adminClient;

    @Override
    public void run() {
        // TODO this is almost identical to the logger configs code: Encapsulate!
        adminClient.withAdmin(admin -> {
            Map<ConfigResource, Config> configs = admin.describeConfigs(
                    brokerIds.stream()
                            .map(name -> new ConfigResource(ConfigResource.Type.BROKER_LOGGER, Integer.toString(name)))
                            .collect(Collectors.toList()),
                    new DescribeConfigsOptions()
                            .includeDocumentation(includeDocs)
                            .includeSynonyms(includeSynonyms)).all().get();
            System.out.println(output.getConfigs(configs));
            return null;
        });

    }
}
