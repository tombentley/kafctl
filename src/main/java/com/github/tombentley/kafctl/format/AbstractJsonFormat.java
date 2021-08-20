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
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.config.ConfigResource;

public abstract class AbstractJsonFormat implements ListTopicsOutput, DescribeTopicsOutput, DescribeClusterOutput, DescribeConfigsOutput {

    protected abstract ObjectMapper mapper();

    @JsonPropertyOrder({"brokerId", "rackId", "host", "port"})
    static class Broker {
        private final Node node;

        public Broker(Node node) {
            this.node = node;
        }

        @JsonProperty
        public int brokerId() {
            return this.node.id();
        }

        @JsonProperty
        public String rackId() {
            return this.node.rack();
        }

        @JsonProperty
        public int port() {
            return this.node.port();
        }

        @JsonProperty
        public String host() {
            return this.node.host();
        }
    }

    @Override
    public String describeCluster(String clusterId, Node controller, Collection<Node> liveBrokers, List<AclOperation> authorizedOperations) {
        throw new RuntimeException("Impl");
    }

    @Override
    public String describeBrokers(Collection<Node> liveBrokers) {
        try {
            return mapper().writeValueAsString(liveBrokers.stream().map(Broker::new).collect(Collectors.toList()));
        } catch (JsonProcessingException e) {
            throw new OutputException(e);
        }
    }

    @Override
    public String describeTopics(Collection<TopicDescription> tds) {

        List<Td> list = tds.stream().map(Td::new).collect(Collectors.toList());

        try {
            return mapper().writeValueAsString(list.size() == 1 ? list.get(0) : list);
        } catch (JsonProcessingException e) {
            throw new OutputException(e);
        }
    }

    @Override
    public String listTopics(Collection<TopicListing> listing) {
        try {
            return mapper().writeValueAsString(listing.stream().map(TopicListing::name).collect(Collectors.toList()));
        } catch (JsonProcessingException e) {
            throw new OutputException(e);
        }
    }

    static class Cfg {
        private final ConfigResource cr;
        private final Config cfg;

        Cfg(ConfigResource cr, Config cfg) {
            this.cr = cr;
            this.cfg = cfg;
        }

        @JsonProperty
        public String topicName() {
            return cr.type() == ConfigResource.Type.TOPIC ? cr.name() : null;
        }

        @JsonProperty
        public String brokerName() {
            return cr.type() == ConfigResource.Type.BROKER || cr.type() == ConfigResource.Type.BROKER_LOGGER ? cr.name() : null;
        }

        @JsonProperty
        public C config() {
            return new C(cfg.entries());
        }

        static class C {
            private final Collection<ConfigEntry> cfg;

            public C(Collection<ConfigEntry> cfg) {
                this.cfg = cfg;
            }

            @JsonProperty
            public List<E> key() {
                return cfg.stream().map(E::new).sorted(Comparator.comparing(E::name)).collect(Collectors.toList());
            }
        }

        static class E {
            private final ConfigEntry e;

            public E(ConfigEntry e) {
                this.e = e;
            }

            @JsonProperty
            public String name() {
                return e.name();
            }

            @JsonProperty
            public String doc() {
                return e.documentation();
            }

            @JsonProperty
            public String value() {
                return e.value();
            }

            @JsonProperty
            public String type() {
                return e.type().name();
            }

            @JsonProperty
            public String source() {
                return e.source().name();
            }

            @JsonProperty
            public boolean isReadOnly() {
                return e.isReadOnly();
            }

            @JsonProperty
            public boolean isDefault() {
                return e.isDefault();
            }

            @JsonProperty
            public boolean isSensitive() {
                return e.isSensitive();
            }

            @JsonProperty
            public List<S> synonyms() {
                return e.synonyms().stream().map(S::new).collect(Collectors.toList());
            }
        }

        static class S {
            private final ConfigEntry.ConfigSynonym s;

            public S(ConfigEntry.ConfigSynonym s) {
                this.s = s;
            }

            @JsonProperty
            public String name() {
                return s.name();
            }

            @JsonProperty
            public String value() {
                return s.value();
            }

            @JsonProperty
            public String source() {
                return s.source().toString();
            }
        }
    }

    @Override
    public String describeConfigs(Map<ConfigResource, Config> configs) {
        try {
            return mapper().writeValueAsString(configs.entrySet().stream().map(e -> new Cfg(e.getKey(), e.getValue())).collect(Collectors.toList()));
        } catch (JsonProcessingException e) {
            throw new OutputException(e);
        }
    }

    /**
     * Json wrapper for TopicDescription
     */
    @JsonPropertyOrder({"topicName", "topicId", "partitions"})
    public static class Td {
        private final TopicDescription td;

        public Td(TopicDescription td) {
            this.td = td;
        }

        @JsonProperty
        public String topicName() {
            return td.name();
        }

        @JsonProperty
        public String topicId() {
            return td.topicId().toString();
        }

        @JsonProperty
        public List<Pd> partitions() {
            return td.partitions().stream().map(Pd::new).collect(Collectors.toList());
        }
    }

    /**
     * Json wrapper for TopicPartitionInfo
     */
    @JsonPropertyOrder({"partitionId", "leader", "replicas", "isr"})
    public static class Pd {
        private final TopicPartitionInfo pd;

        public Pd(TopicPartitionInfo pd) {
            this.pd = pd;
        }

        @JsonProperty
        public int partitionId() {
            return pd.partition();
        }

        @JsonProperty
        public int leader() {
            return pd.leader().id();
        }

        @JsonProperty
        public int[] replicas() {
            return pd.replicas().stream().mapToInt(Node::id).toArray();
        }

        @JsonProperty
        public int[] isr() {
            return pd.isr().stream().mapToInt(Node::id).toArray();
        }
    }
}
