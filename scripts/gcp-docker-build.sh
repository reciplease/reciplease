#!/usr/bin/env bash
# file: gcp-docker-build.sh
# description: Build Docker image.

if [[ -z "${GCP_PROJECT_ID}" ]]; then
  echo 'GCP_PROJECT_ID is not set.'
  exit 1
fi

if [[ -z "${JAR_FILE}" ]]; then
  JAR_FILE=build/reciplease-dist.jar
fi

if [[ ! -f "${JAR_FILE}" ]]; then
  echo "${JAR_FILE} does not exist."
  exit 1
fi

if [[ "${CI}" == true ]]; then
  if [[ -z "${GCP_DOCKER_TOKEN}" ]]; then
    echo 'GCP_DOCKER_TOKEN is not set.'
    exit 1
  fi

  # Login to GCP Docker Registry
  GCP_DOCKER_REGISTRY=https://gcr.io
  GCP_DOCKER_USERNAME=_json_key
  echo "Logging into GCP Docker Registry: ${GCP_DOCKER_REGISTRY}"
  echo "${GCP_DOCKER_TOKEN}" | ${DOCKER} login -u ${GCP_DOCKER_USERNAME} --password-stdin ${GCP_DOCKER_REGISTRY}
fi

echo "Building gcr.io/${GCP_PROJECT_ID}/dist:latest"

# Use Docker build kit to fix multiple COPYs in the Dockerfile
export DOCKER_BUILDKIT=1

# Build Docker image.
${DOCKER} build \
  --push \
  --build-arg JAR_FILE="${JAR_FILE}" \
  -t "gcr.io/${GCP_PROJECT_ID}/dist:latest" .

echo
