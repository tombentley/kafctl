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
package com.github.tombentley.kafctl.util;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import com.github.tombentley.kafctl.Constants;
import org.apache.kafka.clients.admin.AdminClientConfig;

/**
 * A simple, file-based "database" for named {@link Context Contexts}.
 */
@ApplicationScoped
public class ContextDb {

    public static final String DOT_DIR_NAME = "." + Constants.CMD_NAME;

    public static class Context {
        private final String name;
        private final Properties properties;

        public Context(String name, Properties properties) {
            this.name = name;
            this.properties = properties;
        }

        public String name() {
            return name;
        }

        public String bootstrapServers() {
            return String.valueOf(properties.get(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG));
        }

        public Properties properties() {
            return properties;
        }
    }

    /** Lists the known contexts */
    public List<Context> list() {
        Path propsPath = Path.of(System.getProperty("user.home"), DOT_DIR_NAME);
        try {
            return Files.list(propsPath)
                    .map(p -> p.toFile())
                    .filter(file -> file.getName().startsWith("context-")
                            && file.getName().endsWith(".properties"))
                    .map(file -> {
                        String fileName = file.getName();
                        String contextName = fileName.substring("context-".length(),
                                fileName.length() - ".properties".length());
                        return context(contextName);
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Creates a context with the given {@code contextName} using properties from the give {@code file}. */
    public void create(String contextName, File file) {
        Path propsPath = propsPath(contextName);
        if (Files.exists(propsPath)) {
            throw new ContextException("Context already exists: " + contextName);
        }
        // TODO copy any references .crt .key etc
        // TODO validate the .properties is an admin properties
        try {
            Files.copy(file.toPath(), propsPath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        if (list().size() == 1) {
            use(contextName);
        }
    }

    /** Switches the current context to be the one with the given {@code contextName}. */
    public void use(String contextName) {
        Path contextPath = contextPath();
        try {
            Files.writeString(contextPath, contextName.trim());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Updates the context with the given {@code contextName} using properties from the give {@code file}. */
    public void update(String contextName, File file) {
        Path propsPath = propsPath(contextName);
        if (!Files.exists(propsPath)) {
            throw new ContextException("Context does not exist: " + contextName);
        }
        // TODO copy any references .crt .key etc
        try {
            Files.copy(file.toPath(), propsPath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Deletes the context with the given {@code contextName}. */
    public void delete(String contextName) {
        Path propsPath = propsPath(contextName);
        if (!Files.exists(propsPath)) {
            throw new ContextException("Context does not exist: " + contextName);
        }

        try {
            // If it's the current context then delete the context file
            if (contextName.equals(currentName())) {
                Path contextPath = contextPath();
                Files.delete(contextPath);
            }
            // TODO delete all the context-NAME.* files

            Files.delete(propsPath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    private Path contextPath() {
        return Path.of(System.getProperty("user.home"), DOT_DIR_NAME, "context");
    }

    private Path propsPath(String contextName) {
        return Path.of(System.getProperty("user.home"), DOT_DIR_NAME, "context-" + contextName + ".properties");
    }

    /** Gethe current context. */
    public Context current() {
        String contextName = currentName();
        return context(contextName);
    }

    private Context context(String contextName) {
        try {
            Path propsPath = propsPath(contextName);
            Properties result = new Properties();
            try (var reader = Files.newBufferedReader(propsPath)) {
                result.load(reader);
            }
            return new Context(contextName, result);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String currentName() {
        Path contextPath = contextPath();
        if (!Files.exists(contextPath)) {
            throw new ContextException("Current context is unset");
        }
        String contextName = null;
        try {
            contextName = Files.readString(contextPath).trim();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (contextName.isEmpty()) {
            throw new ContextException("Current context is unset");
        }
        return contextName;
    }
}
