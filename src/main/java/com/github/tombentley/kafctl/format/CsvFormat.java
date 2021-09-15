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
package com.github.tombentley.kafctl.format;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;

public class CsvFormat implements TopicsOutput, CGroupsOutput {
    @Override
    public String describeTopics(Collection<TopicDescription> tds) {
        var topics = tds.stream()
                .flatMap(t -> t.partitions().stream().map(p -> new Partition(t, p)))
                .sorted(Comparator.comparing(Partition::topicName))
                .collect(Collectors.toList());

        CsvMapper mapper = new CsvMapper();
        CsvSchema columns = mapper.schemaFor(Partition.class).withUseHeader(true);
        ObjectWriter objectWriter = mapper.writer(columns);
        return topics.stream().map(value -> {
            try {
                return objectWriter.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new OutputException(e);
            }
        }).collect(Collectors.joining("\n"));

//        return AsciiTable.getTable(
//                AsciiTable.NO_BORDERS,
//                topics,
//                List.of(
//                        new Column().header("TOPIC").dataAlign(HorizontalAlign.LEFT).with(Partition::topicName),
//                        new Column().header("TOPIC ID").dataAlign(HorizontalAlign.LEFT).with(Partition::topicId),
//                        new Column().header("PARTITION ID").dataAlign(HorizontalAlign.LEFT).with(Partition::partitionId),
//                        new Column().header("LEADER").dataAlign(HorizontalAlign.LEFT).with(Partition::leader),
//                        new Column().header("REPLIACS").dataAlign(HorizontalAlign.LEFT).with(Partition::replicas),
//                        new Column().header("ISR").dataAlign(HorizontalAlign.LEFT).with(Partition::isr)
//                ));
    }

    @Override
    public String listTopics(Collection<TopicListing> listing) {
        return listing.stream().map(TopicListing::name).collect(Collectors.joining(System.lineSeparator()));
    }

    @Override
    public String listCGroups(Collection<ConsumerGroupListing> listing) {
        return listing.stream().map(ConsumerGroupListing::groupId).collect(Collectors.joining(System.lineSeparator()));
    }

    static class Group {
        private final ConsumerGroupDescription grp;

        public Group(ConsumerGroupDescription grp) {
            this.grp = grp;
        }

        public String groupId() {
            return grp.groupId();
        }

        public String coordinator() {
            return grp.coordinator().idString();
        }

        // TODO the rest of this
        // TODO can we unify this with the Json output
    }

    @Override
    public String describeCGroups(Map<String, ConsumerGroupDescription> descriptions) {
        var topics = descriptions.values().stream()
                .map(Group::new)
                .sorted(Comparator.comparing(Group::groupId))
                .collect(Collectors.toList());
        return AsciiTable.getTable(
                AsciiTable.NO_BORDERS,
                topics,
                List.of(
                        new Column().header("GROUP ID").dataAlign(HorizontalAlign.LEFT).with(Group::groupId),
                        new Column().header("COORDINATOR").dataAlign(HorizontalAlign.LEFT).with(Group::coordinator)
                ));
    }

    @JsonPropertyOrder({"topicName", "topicId", "partitionId", "leader", "replicas", "isr"})
    public static class Partition {

        private final String topicName;
        private final TopicDescription td;
        private final TopicPartitionInfo p;

        public Partition(TopicDescription td, TopicPartitionInfo p) {
            this.topicName = td.name();
            this.td = td;
            this.p = p;
        }

        @JsonProperty
        public String topicName() {
            return topicName;
        }

        @JsonProperty
        public String topicId() {
            return td.topicId().toString();
        }

        @JsonProperty
        public String partitionId() {
            return Integer.toString(p.partition());
        }

        @JsonProperty
        public String leader() {
            return p.leader().idString();
        }

        @JsonProperty
        public String replicas() {
            return p.replicas().stream()
                    .map(Node::idString)
                    .collect(Collectors.joining(","));
        }

        @JsonProperty
        public String isr() {
            return p.isr().stream()
                    .map(Node::idString)
                    .collect(Collectors.joining(","));
        }
    }
}
