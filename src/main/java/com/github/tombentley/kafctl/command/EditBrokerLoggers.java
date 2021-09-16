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

import com.github.tombentley.kafctl.util.ConfigService;
import org.apache.kafka.common.config.ConfigResource;
import picocli.CommandLine;

@CommandLine.Command(
        name = "config",
        description = "Edit a brokers' loggers.")
public class EditBrokerLoggers implements Runnable {

    @CommandLine.Parameters(index = "0", arity = "1", description = "Broker to be altered")
    int brokerId;

    @CommandLine.Option(names = {"--dry-run"}, description="Validate the change without actually performing it")
    boolean dryRun;

    @Inject
    ConfigService editor;

    @Override
    public void run() {
        editor.edit(new ConfigResource(ConfigResource.Type.BROKER_LOGGER, Integer.toString(brokerId)), false, dryRun);
    }
}
