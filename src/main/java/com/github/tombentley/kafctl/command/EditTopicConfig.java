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
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.tombentley.kafctl.util.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.CreatePartitionsOptions;
import org.apache.kafka.clients.admin.DescribeConfigsOptions;
import org.apache.kafka.common.config.ConfigResource;
import picocli.CommandLine;

@CommandLine.Command(
        name = "config",
        description = "Edit a topic config.")
public class EditTopicConfig implements Runnable {

    @CommandLine.Parameters(index = "0", arity = "1", description = "Topic to be altered")
    String topicName;

    @CommandLine.Option(names = {"--dry-run"}, description="Validate the change without actually performing it")
    boolean dryRun;

    @Inject
    AdminClient adminClient;

    @Override
    public void run() {
        adminClient.withAdmin(admin -> {
            ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
            System.err.println("Getting configs");
            Config configs = admin.describeConfigs(
                    List.of(configResource),
                    new DescribeConfigsOptions()
                            .includeDocumentation(true)
                            .includeSynonyms(false)).all().get().get(configResource);
            Path file = writeFile(configs);
            System.err.println("Wrote " + file);

            execEditor(file);
            System.err.println("Edited " + file);
            Map<String, String> original = configs.entries().stream().collect(Collectors.toMap(ConfigEntry::name, ConfigEntry::value));
            Properties edited = new Properties();
            try (var reader = Files.newBufferedReader(file)) {
                edited.load(reader);
            }
            // TODO the diff computation can be merged with the computation of configUpdate
            var added = new HashMap<String, String>((Map) edited);
            added.keySet().removeAll(original.keySet());
            System.err.println("Added " + added);
            var common = new HashMap<String, String>((Map) edited);
            common.keySet().retainAll(original.keySet());
            for (var k : Set.copyOf(common.keySet())) {
                common.remove(k, original.get(k));
            }
            System.err.println("Changed " + common);
            var deleted = new HashMap<>(original);
            deleted.keySet().removeAll(((Map) edited).keySet());
            System.err.println("Deleted " + deleted);


            // TODO check disjointness of set, append, subtract and delete.

            Map<ConfigResource, Collection<AlterConfigOp>> configUpdate = Map.of(
                    configResource,
                    common.entrySet().stream().map(entry2 -> new AlterConfigOp(new ConfigEntry(entry2.getKey(), entry2.getValue()), AlterConfigOp.OpType.SET)).collect(Collectors.toList()));
            if (!configUpdate.isEmpty()) {
                admin.incrementalAlterConfigs(configUpdate).all().get();
            }
            return null;
        });
    }

    private Path writeFile(Config configs) throws IOException {
        Path file = Files.createTempFile(null, ".properties");
        try (var writer = Files.newBufferedWriter(file)) {
            configs.entries().stream().sorted(Comparator.comparing(ConfigEntry::name)).forEach(c -> {
                try {
                    writer.append("# ").append(c.documentation()).append(System.lineSeparator());
                    writer.append(c.name()).append(": ").append(c.value()).append(System.lineSeparator()).append(System.lineSeparator());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
        return file;
    }

    private void execEditor(Path file) throws InterruptedException, IOException {
        String editor = System.getenv("KAFCTL_EDITOR");
        if (editor == null) {
            editor = System.getenv("EDITOR");
        }
        if (editor == null) {
            throw new RuntimeException("No EDITOR");
        }
        System.err.println("Editor " + editor);
        // TODO all sorts of improvement here
        ProcessBuilder pb = new ProcessBuilder(editor, file.toFile().getAbsolutePath());
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
        int i = pb.start().waitFor();
        if (i != 0) {
            throw new RuntimeException("Nonzero exit code");
        }
        if (!Files.exists(file)) {
            throw new RuntimeException("File doesn't exist after editing");
        }
    }

    // --set-config foo=bar
}
