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

package org.apache.skywalking.apm.plugin.couchbase.v76.support;

import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Cluster;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;

public class CouchbaseSpanHelper {
    // Tag bucket name if available
    public static void tagBucketName(AbstractSpan span, String bucketName) {
        if (bucketName != null && !bucketName.isEmpty()) {
            span.tag("db.bucket", bucketName);
        }
    }

    // Tag scope name if available
    public static void tagScopeName(AbstractSpan span, String scopeName) {
        if (scopeName != null && !scopeName.isEmpty()) {
            span.tag("db.scope", scopeName);
        }
    }

    // Tag SDK version
    public static void tagSdkVersion(AbstractSpan span, String sdkVersion) {
        if (sdkVersion != null && !sdkVersion.isEmpty()) {
            span.tag("couchbase.sdk.version", sdkVersion);
        }
    }

    // Tag operation result status
    public static void tagResultStatus(AbstractSpan span, String status) {
        if (status != null && !status.isEmpty()) {
            span.tag("db.result.status", status);
        }
    }

    public static void tagAnalyticsQueryInfo(AbstractSpan span, String statement) {
        span.tag("db.type", "couchbase-analytics");
        span.tag("db.statement", truncateStatement(statement));
    }

    public static void tagSearchQueryInfo(AbstractSpan span, String statement) {
        span.tag("db.type", "couchbase-search");
        span.tag("db.statement", truncateStatement(statement));
    }

    public static String truncateStatement(String statement) {
        int maxLen = 512;
        if (statement != null && statement.length() > maxLen) {
            return statement.substring(0, maxLen) + "...";
        }
        return statement;
    }
    
    public static void tagCollectionInfo(AbstractSpan span, Collection collection) {
        try {
            // The Couchbase Java SDK 3.x does not provide direct access to bucket/scope from Collection
            // so we only tag the collection name here.
            span.tag("db.collection", collection.name());
        } catch (Exception ignored) { }
    }

    public static void tagClusterInfo(AbstractSpan span, Cluster cluster) {
        try {
            // The Couchbase Java SDK 3.x does not provide direct access to seed nodes from ClusterEnvironment
            // so we only tag the network resolution if available.
            String connStr = null;
            try {
                connStr = cluster.environment().ioConfig().networkResolution().name();
            } catch (Exception ignored) { }
            if (connStr != null) {
                span.tag("peer.address", connStr);
            }
        } catch (Exception ignored) { }
    }
}
