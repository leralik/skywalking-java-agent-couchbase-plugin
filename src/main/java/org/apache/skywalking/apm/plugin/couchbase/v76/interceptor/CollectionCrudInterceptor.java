/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.plugin.couchbase.v76.interceptor;

import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.plugin.couchbase.v76.support.CouchbaseSpanHelper;
import com.couchbase.client.java.Collection;

import java.lang.reflect.Method;

public class CollectionCrudInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) {
        String operation = method.getName();
        String docId = allArguments.length > 0 && allArguments[0] != null ? org.apache.skywalking.apm.plugin.couchbase.v76.support.CouchbaseSpanHelper.truncateStatement(allArguments[0].toString()) : "";
        AbstractSpan span = ContextManager.createExitSpan("Couchbase/Collection/" + operation, null);
        span.tag("db.operation", operation);
        span.tag("db.document.id", docId);
        // Tag collection name if possible
        if (objInst instanceof Collection) {
            CouchbaseSpanHelper.tagCollectionInfo(span, (Collection) objInst);
        }
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) {
        ContextManager.stopSpan();
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        AbstractSpan span = ContextManager.activeSpan();
        span.log(t);
        span.tag("error", "true");
        if (t != null) {
            String msg = t.getMessage();
            if (msg != null && msg.toLowerCase().contains("timeout")) {
                span.tag("db.timeout", "true");
            }
        }
    }
}
