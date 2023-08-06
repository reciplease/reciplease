ARG BASE_IMAGE=eclipse-temurin:17-jdk

FROM ${BASE_IMAGE} as builder

ARG JAR_FILE=build/reciplease-dist.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM ${BASE_IMAGE}

COPY --from=builder dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder reciplease-dependencies/ ./
COPY --from=builder application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher", "-Djava.security.egd=file:/dev/./urandom"]