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

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.tombentley.kafctl.util.ContextDb;
import picocli.CommandLine;

@CommandLine.Command(name = "contexts", description = "List the available contexts")
class GetContexts implements Runnable {

    @Inject
    ContextDb context;

    @Override
    public void run() {
        System.out.println(AsciiTable.getTable(
                AsciiTable.NO_BORDERS,
                context.list(),
                java.util.List.of(
                        new Column().header("NAME").with(ContextDb.Context::name),
                        new Column().header("BOOTSTRAP").with(ContextDb.Context::bootstrapServers)
                )));
    }
}