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
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.ConfigEntry;
import picocli.CommandLine.Help.Ansi;

public class PlainTextFormat implements DescribeConfigsOutput {

    @Override
    public String describeConfigs(Collection<ConfigEntry> configs) {
        return configs.stream().sorted(Comparator.comparing(ConfigEntry::name))
                .map(entry -> {
                    return Ansi.AUTO.string("@|green " + entry.name() + "|@")
                        + " (" + Ansi.AUTO.string("@|blue " + (entry.isReadOnly() ? "read-only " : "read-write ") + entry.type() + "|@") + "): "
                        + Ansi.AUTO.string(entry.documentation().replaceAll("\\<code\\>(.*?)</code>", "@|bold $1|@"));
                })
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
