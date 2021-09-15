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
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.tombentley.kafctl.util.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.CreatePartitionsOptions;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.common.config.ConfigResource;
import picocli.CommandLine;

@CommandLine.Command(
        name = "topic",
        aliases = "topics",
        description = "Update an existing context.")
public class AlterTopic implements Runnable {

    @CommandLine.Parameters(index = "0..*", arity = "1..", description = "Topics to be altered")
    List<String> topicNames;

    @CommandLine.Option(names = {"-f", "--file"}, description = "The topic config to use.")
    File config;

    @CommandLine.Option(names = {"-c", "--set-config"}, description = "A key=value config property to set.")
    List<String> set;

    @CommandLine.Option(names = {"-a", "--append-config"}, description = "A key=value config property to append.")
    List<String> append;

    @CommandLine.Option(names = {"-d", "--delete"}, description = "A key=value config property to delete.")
    List<String> delete;

    @CommandLine.Option(names = {"-s", "--subtract"}, description = "A key=value config property to subtract.")
    List<String> subtract;

    @CommandLine.Option(names = {"-p", "--partitions"}, description = "The new number of partitions.")
    Integer partitions;

    @CommandLine.Option(names = {"-r", "--replicas"}, description = "The number of replicas.")
    Short replicas;

    @CommandLine.Option(names = {"--dry-run"}, description="Validate the change without actually performing it")
    boolean dryRun;

    @Inject
    AdminClient adminClient;

    @Override
    public void run() {
        if (replicas != null) {
            throw new RuntimeException("Changing replicas not yet supported"); // TODO support RF change
        }
        adminClient.withAdmin(ac -> {
            Map<String, NewPartitions> newPartitions = Optional.ofNullable(partitions).map(integer -> topicNames.stream().collect(Collectors.toMap(
                    Function.identity(),
                    name -> NewPartitions.increaseTo(integer)
            ))).orElseGet(Map::of);

            Map<String, String> m;
            if (this.config != null) {
                var p = new Properties();
                try (InputStream inStream = Files.newInputStream(config.toPath())) {
                    p.load(inStream);
                }
                m = (Map) p; // encapsulte this horror
            } else {
                m = Map.of();
            }

            // TODO check disjointness of set, append, subtract and delete.

            Map<ConfigResource, Collection<AlterConfigOp>> configs= topicNames.stream().collect(Collectors.toMap(
                    name -> new ConfigResource(ConfigResource.Type.TOPIC, name),
                    name -> {
                        Map<ConfigEntry, AlterConfigOp.OpType> collect = m.entrySet().stream().collect(Collectors.toMap(
                                entry -> new ConfigEntry(entry.getKey(), entry.getValue()),
                                entry -> AlterConfigOp.OpType.SET));
                        Map.of(set, AlterConfigOp.OpType.SET,
                                append, AlterConfigOp.OpType.APPEND,
                                subtract, AlterConfigOp.OpType.SUBTRACT,
                                delete, AlterConfigOp.OpType.DELETE).forEach((key, value) -> key.forEach(pair -> {
                            String[] split = pair.split("=", 1);
                            collect.put(new ConfigEntry(split[0], split[1]), value);
                        }));
                        return collect.entrySet().stream().map(e -> new AlterConfigOp(e.getKey(), e.getValue())).collect(Collectors.toList());
                    }));

            // TODO can the change to configs depend on the number of partitions (thus is a fixed order between the two operations always going to work?)

            if (!newPartitions.isEmpty()) {
                ac.createPartitions(newPartitions, new CreatePartitionsOptions().validateOnly(dryRun)).all().get();
            }
            if (!configs.isEmpty()) {
                ac.incrementalAlterConfigs(configs).all().get();
            }
            return null;
        });
    }

    // --set-config foo=bar
}
