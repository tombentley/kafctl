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

import com.github.tombentley.kafctl.format.CGroupsOutput;
import com.github.tombentley.kafctl.util.AdminClient;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "consumer-group",
        aliases = {"consumer-groups", "cg"},
        description = "When executed without a subcommand, lists the consumer group ids. " +
                "When the `state` subcommand is given, gets the state of the listed consumer groups.",
        subcommands = {
                GetConsumerGroupState.class
                // TODO `get consumer-group lag` command
        }
)
public class GetConsumerGroup implements Runnable {

    @CommandLine.Option(
            names = {"--output", "-o"},
            description = "The output format. Valid values: ${COMPLETION-CANDIDATES}",
            defaultValue = "json",
            converter = CGroupsOutput.OutputFormatConverter.class,
            completionCandidates = CGroupsOutput.OutputFormatConverter.class)
    CGroupsOutput output;

    @Inject
    AdminClient adminClient;

    @Override
    public void run() {
        adminClient.withAdmin(admin -> {
            System.out.println(output.listCGroups(admin.listConsumerGroups().all().get()));
            return null;
       });
    }
}
