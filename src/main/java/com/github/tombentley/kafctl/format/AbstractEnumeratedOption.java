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
package com.github.tombentley.kafctl.format;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import picocli.CommandLine;

/**
 * Helper class, allowing subclasses to be used for both {@code converter} and {@code completionCandidates}
 * of picocli's {@code @Option}.
 * @param <T> The type that the ITypeConverter converts to.
 */
public abstract class AbstractEnumeratedOption<T> implements CommandLine.ITypeConverter<T>, Iterable<String> {
    protected abstract Map<String, T> map();

    @Override
    public T convert(String value) {
        T format = map().get(value);
        if (format == null) {
            throw new CommandLine.TypeConversionException("supported formats are: " + map().keySet().stream().sorted().collect(Collectors.joining(", ")));
        }
        return format;
    }

    @Override
    public Iterator<String> iterator() {
        return map().keySet().iterator();
    }
}

