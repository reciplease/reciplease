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
	--build-arg JAR_FILE="${JAR_FILE}" \
	-t "gcr.io/${PROJECT_ID}/dist:latest" .