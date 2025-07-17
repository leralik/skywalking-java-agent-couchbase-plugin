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

package org.apache.skywalking.apm.plugin.couchbase.v76.define;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.apache.skywalking.apm.agent.core.plugin.match.NameMatch.byName;

/**
 * Instrumentation for com.couchbase.client.java.AsyncCollection CRUD methods
 */
public class AsyncCollectionCrudInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    @Override
    public org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint[0];
    }


    private static final String ENHANCE_CLASS = "com.couchbase.client.java.AsyncCollection";
    private static final String INTERCEPTOR_CLASS = "org.apache.skywalking.apm.plugin.couchbase.v76.interceptor.AsyncCollectionCrudInterceptor";
    // For future enrichment: DB layer/component tagging, bucket/scope/collection extraction


    @Override
    protected ClassMatch enhanceClass() {
        return byName(ENHANCE_CLASS);
    }


    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[] {
            new InstanceMethodsInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    // Instrument CRUD methods for AsyncCollection
                    return named("get").or(named("upsert")).or(named("insert")).or(named("replace")).or(named("remove"));
                }

                @Override
                public String getMethodsInterceptor() {
                    // Interceptor will be responsible for tagging DB layer/component and extracting bucket/scope/collection
                    return INTERCEPTOR_CLASS;
                }

                @Override
                public boolean isOverrideArgs() {
                    return false;
                }
            }
        };
    }
    // TODO: In AsyncCollectionCrudInterceptor, ensure tagging:
    //   - DB_INSTANCE (bucket)
    //   - DB_COLLECTION (collection)
    //   - DB_SCOPE (scope, if available)
    //   - DB_BIND_VARIABLES (document ID, truncated)
    //   - SpanLayer.asDB(span)
    //   - span.setComponent(CouchbaseComponent)
    //   - SDK version, result status, error, timeout
    // See MongoSpanHelper for reference
}
