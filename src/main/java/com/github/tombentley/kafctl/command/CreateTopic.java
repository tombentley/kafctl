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
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import com.github.tombentley.kafctl.util.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsOptions;
import org.apache.kafka.clients.admin.NewTopic;
import picocli.CommandLine;

@CommandLine.Command(
        name = "topic",
        aliases = "topics",
        description = "Creates named topics.")
public class CreateTopic implements Runnable {

    @CommandLine.Option(names = {"--config-file"})
    InputStream configFile;

    // TODO support passing configs on the command line (overriding the file)

    @CommandLine.Parameters(index = "0..*", arity = "1..")
    List<String> topicNames;

    @CommandLine.Option(names = {"--partitions", "-p"})
    Integer numPartitions;

    @CommandLine.Option(names = {"--replicas", "-r"})
    Short numReplicas;

    @CommandLine.Option(names = {"--dry-run"})
    boolean dryRun;

    @Inject
    AdminClient adminClient;

    @Override
    public void run() {
        Properties p = null;
        if (configFile != null) {
            p = new Properties();
            try {
                p.load(configFile);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                try {
                    configFile.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        Map<String, String> configs = p != null ? new HashMap<>((Map) p) : null;
        List<NewTopic> toCreate = topicNames.stream().map(name -> new NewTopic(name, Optional.ofNullable(numPartitions), Optional.ofNullable(numReplicas)).configs(configs)).collect(Collectors.toList());
        adminClient.withAdmin(ac -> {
            ac.createTopics(toCreate, new CreateTopicsOptions().validateOnly(dryRun)).all().get();
            return null;
        });
    }
}
