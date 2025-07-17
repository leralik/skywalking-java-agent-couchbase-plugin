# Couchbase 7.6.x SkyWalking Plugin

This module provides SkyWalking APM instrumentation for Couchbase Java SDK 7.6.x, enabling distributed tracing and performance monitoring for Couchbase operations.

## Features
- Traces Couchbase cluster queries and collection CRUD operations.
- Captures operation names, document IDs, and query statements.
- Tags peer information for clusters.

## Instrumented Components
- `ClusterQueryInterceptor`: Intercepts and traces N1QL queries executed via the Couchbase `Cluster` class.
- `AsyncCollectionCrudInterceptor`: Intercepts and traces asynchronous CRUD operations on Couchbase collections.

## Usage
1. Build this module with Maven:
   ```bash
   mvn clean package
   ```
2. Copy the built JAR to the `plugins` directory of your SkyWalking agent.
3. Start your Java application with the SkyWalking agent attached.

## Development
- Interceptors are located in `src/main/java/org/apache/skywalking/apm/plugin/couchbase/v76/interceptor/`.
- Helper classes for tagging peer info are in `support/`.
- Follows SkyWalking's standard tagging conventions (e.g., `db.statement`, `db.operation`).

## License
This project is licensed under the Apache License 2.0. See the [LICENSE](http://www.apache.org/licenses/LICENSE-2.0) file for details.
