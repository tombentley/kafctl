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

import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.admin.TopicListing;

public interface TopicsOutput {

    String describeTopics(Collection<TopicDescription> tds);

    String listTopics(Collection<TopicListing> listing);

    class OutputFormatConverter extends AbstractEnumeratedOption<TopicsOutput> {
        @Override
        protected Map<String, TopicsOutput> map() {
            return Map.of(
                    "json", new JsonFormat(),
                    "yaml", new YamlFormat(),
                    "csv", new CsvFormat(),
                    "table", new TableFormat()
            );
        }
    }
}