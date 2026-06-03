ARG BASE_IMAGE=eclipse-temurin:21-jdk

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
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/reciplease-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./

# Launcher moved to the `.launch` package in Spring Boot 3.2+/4.
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "org.springframework.boot.loader.launch.JarLauncher"]
