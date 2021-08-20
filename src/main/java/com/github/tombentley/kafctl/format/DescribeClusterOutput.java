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
import java.util.Iterator;
import java.util.List;

import org.apache.kafka.common.Node;
import org.apache.kafka.common.acl.AclOperation;
import picocli.CommandLine;

public interface DescribeClusterOutput {
    String describeBrokers(Collection<Node> liveBrokers);
    String describeCluster(String clusterId, Node controller, Collection<Node> liveBrokers, List<AclOperation> authorizedOperations);

    class OutputFormatConverter implements CommandLine.ITypeConverter<DescribeClusterOutput>, Iterable<String> {
        @Override
        public DescribeClusterOutput convert(String value) {
            switch (value) {
                case "json":
                    return new JsonFormat();
                case "yaml":
                    return new YamlFormat();
                default:
                    throw new IllegalArgumentException("Unknown output format: " + value);
            }

        }

        @Override
        public Iterator<String> iterator() {
            return List.of("json", "yaml").iterator();
        }
    }
}
