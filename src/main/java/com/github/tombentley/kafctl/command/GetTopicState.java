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
import java.util.ArrayList;
import java.util.List;

import com.github.tombentley.kafctl.format.TopicsOutput;
import com.github.tombentley.kafctl.util.AdminClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "state", description = "Gets the named topic states.")
public class GetTopicState implements Runnable {

    @Option(names = {"--output", "-o"},
            description = "The output format. Valid values: ${COMPLETION-CANDIDATES}",
            defaultValue = "table",
            converter = TopicsOutput.OutputFormatConverter.class,
            completionCandidates = TopicsOutput.OutputFormatConverter.class)
    TopicsOutput output;

    @Parameters(index = "0..*", arity = "1..")
    List<String> topicNames;

    @Inject
    AdminClient adminClient;

    @Override
    public void run() {
        adminClient.withAdmin(admin -> {
            var tds = new ArrayList<>(admin.describeTopics(this.topicNames).all().get().values());
            System.out.println(output.describeTopics(tds));
            return null;
        });
    }
}
