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

import picocli.CommandLine;

import static com.github.tombentley.kafctl.Constants.EDITOR_ENV_VAR;

@CommandLine.Command(
        name = "edit",
        description = "Modify things like broker and topic configs, and broker loggers interactively using a text editor.\n" +
                "The text editor to use is taken from the $" + EDITOR_ENV_VAR + " or, if that's not set, the $EDITOR environment variable.",
        subcommands = {
                EditTopic.class,
                EditBroker.class,
                // TODO edit support for the admin client properties in the context
        }
)
public class Edit {
}
