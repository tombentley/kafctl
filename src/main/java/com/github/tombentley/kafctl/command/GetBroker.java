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

import com.github.tombentley.kafctl.format.DescribeClusterOutput;
import com.github.tombentley.kafctl.util.AdminClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/*
 get broker # list brokers
 get brokers # list brokers
 get broker 0 # same as --node
 get broker 0 --node # Show the node info
 get broker 0 --config # Get the config
 get broker 0 --log-config # Get the log config
 get broker 0 --leading # Get the partitions this broker is leading
 get broker 0 --following # Get the partitions this broker is following
 get broker 0 1 2 --lagging # Get the partitions brokers 1 2 and 3 are a replica of, but not in the ISR
 When multiple of --node etc are given use a wrapper, or do the join
 */
@Command(
        name = "broker",
        aliases = "brokers",
        description = "Gets the named broker's state, configs or logger configs.",
        subcommands = {
                GetBrokerConfig.class,
                GetBrokerLoggers.class,
                GetBrokerState.class
        }
)
public class GetBroker implements Runnable {

    @Option(names = {"--output", "-o"},
            description = "The output format. Valid values: ${COMPLETION-CANDIDATES}",
            defaultValue = "json",
            converter = DescribeClusterOutput.OutputFormatConverter.class,
            completionCandidates = DescribeClusterOutput.OutputFormatConverter.class)
    DescribeClusterOutput output;

    @Inject
    AdminClient adminClient;

    @Override
    public void run() {
        adminClient.withAdmin(admin -> {
            System.out.println(output.describeBrokers(admin.describeCluster().nodes().get()));
            return null;
       });
    }
}
