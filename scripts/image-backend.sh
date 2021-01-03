#!/bin/bash
# file: image-backend.sh
# description: Build Docker image.

if [[ -z "${PROJECT_ID}" ]]; then
	echo '${PROJECT_ID} is not set.'
	exit 1
fi

if [[ -z "${JAR_FILE}" ]]; then
	echo '${JAR_FILE} is not set.'
	exit 1
fi

if [[ ! -f "${JAR_FILE}" ]]; then
	echo "${JAR_FILE} does not exist."
	exit 1
fi

echo "Building gcr.io/${PROJECT_ID}/dist:latest"

# Use Docker build kit to fix multiple COPYs in the Dockerfile
DOCKER_BUILDKIT=1

${DOCKER} build \
	--build-arg JAR_FILE="${JAR_FILE}" \
	-t "gcr.io/${PROJECT_ID}/dist:latest" .

if [[ "${CI}" = true ]]; then
	if [[ -z "${DOCKER_TOKEN}" ]]; then
    echo '${DOCKER_TOKEN} is not set.'
    exit 1
  fi

  echo "${DOCKER_TOKEN}" | ${DOCKER} login -u _json_key --password-stdin https://gcr.io

  ${DOCKER} push gcr.io/${PROJECT_ID}/dist
fi

echo