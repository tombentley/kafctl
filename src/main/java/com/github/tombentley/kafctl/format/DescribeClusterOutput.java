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
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.kafka.common.Node;
import org.apache.kafka.common.acl.AclOperation;

public interface DescribeClusterOutput {
    String describeBrokers(Collection<Node> liveBrokers);
    String describeCluster(String clusterId, Node controller, Collection<Node> liveBrokers, List<AclOperation> authorizedOperations);

    class OutputFormatConverter extends AbstractEnumeratedOption<DescribeClusterOutput> {
        @Override
        protected Map<String, DescribeClusterOutput> map() {
            return Map.of(
                    "json", new JsonFormat(),
                    "yaml", new YamlFormat());
        }
    }
}
