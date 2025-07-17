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
import java.lang.reflect.Method;

public class AsyncCollectionCrudInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) {
        String operation = method.getName();
        String docId = allArguments.length > 0 && allArguments[0] != null ? CouchbaseSpanHelper.truncateStatement(allArguments[0].toString()) : "";
        AbstractSpan span = ContextManager.createExitSpan("Couchbase/AsyncCollection/" + operation, null);
        // Tag DB layer and component
        span.tag("db.type", "couchbase");
        span.tag("component", "Couchbase");
        // Tag operation and statement
        span.tag("db.operation", operation);
        span.tag("db.statement", docId);
        // Tag collection info
        if (objInst instanceof com.couchbase.client.java.Collection) {
            CouchbaseSpanHelper.tagCollectionInfo(span, (com.couchbase.client.java.Collection) objInst);
        }
        // Tag bucket and scope if available (requires custom logic or enhanced instance fields)
        // Example: If objInst has getBucketName()/getScopeName(), use them
        try {
            java.lang.reflect.Method getBucket = objInst.getClass().getMethod("bucketName");
            String bucketName = (String) getBucket.invoke(objInst);
            CouchbaseSpanHelper.tagBucketName(span, bucketName);
        } catch (Exception ignored) { }
        try {
            java.lang.reflect.Method getScope = objInst.getClass().getMethod("scopeName");
            String scopeName = (String) getScope.invoke(objInst);
            CouchbaseSpanHelper.tagScopeName(span, scopeName);
        } catch (Exception ignored) { }
        // Tag SDK version if available
        CouchbaseSpanHelper.tagSdkVersion(span, "3.7.9"); // hardcoded, or extract dynamically if possible
        // Tag result status if available (not possible before method, but can be added after)
        // Mark as DB layer (SpanLayer.asDB equivalent)
        span.tag("span.layer", "db");
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
