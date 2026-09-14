#!/usr/bin/env bash
# file: ghcr-docker-build.sh
# description: Build Docker image and push to GitHub Container Registry.

# Fail loudly: a swallowed `docker build` error previously let CI report success
# while the image (and thus every deploy) stayed stale. -e propagates the build
# failure; pipefail catches failures in the docker-login pipe.
set -eo pipefail

if [[ -z "${JAR_FILE}" ]]; then
  JAR_FILE=build/reciplease-dist.jar
fi

if [[ ! -f "${JAR_FILE}" ]]; then
  echo "${JAR_FILE} does not exist."
  exit 1
fi

# Login to GHCR happens in the workflow step (GITHUB_TOKEN), not here; the
# script only pushes with whatever credentials docker already holds.
echo "Building ghcr.io/reciplease/reciplease:latest"

# Use Docker build kit to fix multiple COPYs in the Dockerfile
export DOCKER_BUILDKIT=1

# Build Docker image.
${DOCKER} build \
  --push \
  --build-arg JAR_FILE="${JAR_FILE}" \
  -t "ghcr.io/reciplease/reciplease:latest" .

echo
