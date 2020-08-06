FROM adoptopenjdk:11-jre-hotspot

COPY reciplease-dist.jar .

ENV PORT 8080
EXPOSE ${PORT}

CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "reciplease-dist.jar", "--server.port=${PORT}"]