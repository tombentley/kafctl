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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.common.config.ConfigResource;

public class PropertiesFormat implements DescribeConfigsOutput {
    static class FlatConfig {
        String name;
        Config config;

        public FlatConfig(String name, Config config) {
            this.name = name;
            this.config = config;
        }

        public String name() {
            return name;
        }

        public Config config() {
            return config;
        }

        public String sortedProperties() {
            Properties p = new Properties();
            StringWriter sw = new StringWriter();
            for (var ce: config().entries()) {
                String value = ce.value();
                if (value != null) {
                    p.put(ce.name(), value);
                }
            }
            try {
                p.store(sw, null);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return new BufferedReader(new StringReader(sw.toString())).lines()
                    .filter(line -> !line.startsWith("#"))
                    .sorted()
                    .collect(Collectors.joining(System.lineSeparator()));
        }
    }

    @Override
    public String describeConfigs(Map<ConfigResource, Config> configs) {
        if (configs.size() > 1) {
            throw new RuntimeException("Can't use properties output with multiple configs");
        }
        List<FlatConfig> collect = configs.entrySet().stream()
                .map(e -> new FlatConfig(e.getKey().name(), e.getValue()))
                .sorted(Comparator.comparing(FlatConfig::name))
                .collect(Collectors.toList());
        return collect.get(0).sortedProperties();
    }
}
