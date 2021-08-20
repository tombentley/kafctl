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

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.tombentley.kafctl.util.ContextDb;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "context",
        description = "Create, modifies or switches between configurations of this client",
        subcommands = {
                CommandLine.HelpCommand.class,
                Context.Create.class,
                Context.Update.class,
                Context.Delete.class,
                Context.Use.class,
                Context.List.class
        }
)
public class Context {

    @Command( name = "create", description = "Creates a new context.",
            subcommands = {CommandLine.HelpCommand.class} )
    static class Create implements Runnable {

        @Parameters(index = "0", description = "The name of the context to create.")
        private String contextName;

        @Option(names = { "-f", "--file"}, description = "The admin client properties to use.", required = true)
        private File file;

        @Inject
        ContextDb context;

        @Override
        public void run() {
            context.create(contextName, file);
        }
    }
    @Command( name = "update", description = "Update an existing context.")
    static class Update implements Runnable {
        @Parameters(index = "0", description = "The name of the context to update.")
        private String contextName;

        @Option(names = { "-f", "--file"}, description = "The admin client properties to use.", required = true)
        private File file;

        @Inject
        ContextDb context;

        @Override
        public void run() {
            context.update(contextName, file);
        }
    }
    @Command( name = "delete", description = "Deletes a context.")
    static class Delete implements Runnable {
        @Parameters(index = "0", description = "The name of the context to delete.")
        private String contextName;

        @Inject
        ContextDb context;

        @Override
        public void run() {
            context.delete(contextName);
        }
    }

    @Command( name = "use", description = "Changes the current context.")
    static class Use implements Runnable {

        @Parameters(index = "0", description = "The name of the context to use.")
        private String contextName;

        @Inject
        ContextDb context;

        @Override
        public void run() {
            context.use(contextName);
        }
    }

    @Command( name = "list", description = "List the contexts")
    static class List implements Runnable {

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
}
