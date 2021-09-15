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
import java.util.Map;

import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;

public interface CGroupsOutput {
    String listCGroups(Collection<ConsumerGroupListing> listing);

    String describeCGroups(Map<String, ConsumerGroupDescription> listing);

    class OutputFormatConverter extends AbstractEnumeratedOption<CGroupsOutput> {
        @Override
        protected Map<String, CGroupsOutput> map() {
            return Map.of(
                    "json", new JsonFormat(),
                    "yaml", new YamlFormat(),
                    "table", new TableFormat());
        }
    }
}
