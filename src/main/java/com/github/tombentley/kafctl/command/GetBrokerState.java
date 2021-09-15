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
import java.util.List;
import java.util.stream.Collectors;

import com.github.tombentley.kafctl.format.DescribeClusterOutput;
import com.github.tombentley.kafctl.util.AdminClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "state", description = "Gets the named broker's state.")
public class GetBrokerState implements Runnable {

    @Option(names = {"--output", "-o"},
            description = "The output format. Valid values: ${COMPLETION-CANDIDATES}",
            defaultValue = "json",
            converter = DescribeClusterOutput.OutputFormatConverter.class,
            completionCandidates = DescribeClusterOutput.OutputFormatConverter.class)
    DescribeClusterOutput output;

    @Parameters(index = "0..*", arity = "1..")
    List<Integer> brokerIds;

    List<String> what; // what to display (what we're selecting)
    // --config --node (host, rack, id) --log-config
    // --replicas (leading and following) NO this would be `get partitions topic/1` `get partitions --on-broker`
    // or --leading, --following --lagging (not in ISR)

    List<String> where; // filter
    // get broker --where-broker-id=1 --show=config --show=node -oyaml
    // get broker --where-leading=topic/1      # get the broker which is leading topic/1
    // get broker --where-following=topic/1    # get the brokers which are following topic/1
    // get broker --where-in-isr-of=topic/1    # get the brokers which are in the isr of topic/1
    // get broker --where-replicating=topic/1  # get the brokers which are in the isr of topic/1
    // get broker --where-lagging=topic/1      # get the brokers which are replicating but not in isr of topic/1
    // get broker --where-coordinating=txn/1
    //
    // table BROKER ID, CONFIG_KEY, CONFIG_VALUE, HOSTNAME, PORT, RACK_ID

    // Then we can also use similar filter and sort on `get partitions`
    // get partitions --where-topic-name=foo   # get the partitions for topic foo
    // get partitions --where-leader=1         # get the partitions/replicas where broker 1 is the leader
    // get partitions --where-follower=1       # get the partitions/replicas where broker 1 is a follower
    // get partitions --where-no-leader        # get the partitions without a leader
    // get partitions --where-leader=1 --where-under-replicated # partitions on broker 1 which are under-replicated

    List<String> sort; // how to sort the results (default to the first where?)
    // --sort=broker_id,config_key

    @Inject
    AdminClient adminClient;

    @Override
    public void run() {
        adminClient.withAdmin(admin -> {
            // TODO output should be in the same order as brokerId
            System.out.println(output.describeBrokers(admin.describeCluster().nodes().get().stream()
                    .filter(node -> brokerIds.contains(node.id()))
                    .collect(Collectors.toList())));
            return null;
        });
    }
}
