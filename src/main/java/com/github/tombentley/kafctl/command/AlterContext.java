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
import java.io.File;

import com.github.tombentley.kafctl.util.ContextDb;
import picocli.CommandLine;

@CommandLine.Command(name = "context", description = "Update an existing context.")
class AlterContext implements Runnable {
    @CommandLine.Parameters(index = "0", description = "The name of the context to update.")
    String contextName;

    @CommandLine.Option(names = {"-f", "--file"}, description = "The admin client properties to use.", required = true)
    File file;

    @Inject
    ContextDb context;

    @Override
    public void run() {
        context.update(contextName, file);
    }
}