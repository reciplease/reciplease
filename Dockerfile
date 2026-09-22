ARG BASE_IMAGE=eclipse-temurin:25-jdk

FROM ${BASE_IMAGE} as builder
WORKDIR /builder

ARG JAR_FILE=build/reciplease-dist.jar
COPY ${JAR_FILE} application.jar
# Spring Boot 4 removed the `layertools` jarmode; `tools ... extract --layers`
# is the replacement that reproduces the layered directory layout below.
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM ${BASE_IMAGE}
WORKDIR /application

COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/reciplease-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./

# Trains the JEP 483 AOT cache in place, after assembly: it fingerprints
# application.jar's mtime, so training against any other copy of it
# invalidates the cache at startup.
RUN (SPRING_PROFILES_ACTIVE=cloud,prod RECIPLEASE_JWT_SIGNING_SECRET=training-run-dummy-secret-not-used-in-production \
    java -XX:AOTCacheOutput=reciplease.aot -Dspring.context.exit=onRefresh -jar application.jar || true) \
    && test -s reciplease.aot

# The `tools` extractor emits a thin launcher (application/application.jar) whose
# manifest Class-Path references the sibling dependency layers, so run it with
# `-jar` rather than invoking JarLauncher on a hand-built classpath.
ENTRYPOINT ["java", "-XX:AOTCache=reciplease.aot", "-Djava.security.egd=file:/dev/./urandom", "-jar", "application.jar"]
