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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;

/**
 * Json wrapper for TopicPartitionInfo
 */
@JsonPropertyOrder({"partitionId", "leader", "replicas", "isr"})
public class Pd {
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
