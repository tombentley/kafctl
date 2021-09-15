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

import com.github.tombentley.kafctl.util.AdminClient;
import org.apache.kafka.clients.admin.DeleteRecordsOptions;
import org.apache.kafka.clients.admin.RecordsToDelete;
import org.apache.kafka.common.TopicPartition;
import picocli.CommandLine;

@CommandLine.Command(
        name = "records",
        description = "Deletes records from a topic.")
public class DeleteRecords implements Runnable {

    @CommandLine.Parameters(index = "0", description = "The name of the topic to delete records from")
    String topicName;

    @CommandLine.Parameters(index = "1", description = "The partition to delete records from")
    int partition;

    @CommandLine.Parameters(index = "2", description = "The oldest offset to retain.")
    int beforeOffset;

    // TODO Kafka itself doesn't support a dry-run
//    @CommandLine.Option(names = {"--dry-run"})
//    boolean dryRun;

    @Inject
    AdminClient adminClient;

    @Override
    public void run() {
        adminClient.withAdmin(ac -> {
            ac.deleteRecords(Map.of(new TopicPartition(topicName, partition), RecordsToDelete.beforeOffset(beforeOffset)), new DeleteRecordsOptions()).all().get();
            return null;
        });
    }
}
