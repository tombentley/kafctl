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
import javax.inject.Inject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.common.KafkaException;

@ApplicationScoped
public class AdminClient {

    @FunctionalInterface
    public interface AdminConsumer<T> {
        T apply(Admin admin) throws Exception;
    }

    @Inject
    ContextDb context;

    public <T> T withAdmin(AdminConsumer<T> consumer) {
        Admin admin = Admin.create(context.current().properties());
        try {
            return consumer.apply(admin);
        } catch (ExecutionException e) {
            // TODO picocli is there a way to print a coloured error to stdout without a stacktrace?
            if (e.getCause() instanceof TimeoutException) {
                throw new RuntimeException("Timeout wait for server response");
            } else if (e.getCause() instanceof KafkaException) {
                throw new RuntimeException(e.getCause().getMessage());
            } else {
                throw new RuntimeException(e);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
