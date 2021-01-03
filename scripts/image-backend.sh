#!/bin/bash
# file: image-backend.sh
# description: Build Docker image.

if [[ -z "${PROJECT_ID}" ]]; then
	echo '${PROJECT_ID} is not set.'
	exit 1
fi

JAR_FILE="./dist/target/reciplease-dist.jar"
if [[ ! -f "${JAR_FILE}" ]]; then
	echo "${JAR_FILE} does not exist."
	exit 1
fi

# Use Docker build kit to fix multiple COPYs in the Dockerfile
DOCKER_BUILDKIT=1
docker build \
	--build-arg JAR_FILE="./dist/target/reciplease-dist.jar" \
	-t gcr.io/${PROJECT_ID}/dist:latest .
# ${MVN} -pl dist \
# 	-am spring-boot:build-image \
# 	-D"spring-boot.build-image.imageName"=gcr.io/$(PROJECT_ID)/dist:latest \
# 	-DskipTests
