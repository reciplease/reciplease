#!/usr/bin/env bash
# file: gcp-docker-deploy.sh
# description: Deploy Docker image to GCP.

if [[ -z "${GCP_REGION}" ]]; then
  echo '${GCP_REGION} is not set.'
  exit 1
fi

if [[ -z "${GCP_PROJECT_ID}" ]]; then
  echo '${GCP_PROJECT_ID} is not set.'
  exit 1
fi

${GCLOUD} config set run/platform managed
${GCLOUD} config set run/region ${GCP_REGION}

# Resolve the mutable :latest tag to its immutable digest and deploy THAT.
IMAGE="gcr.io/${GCP_PROJECT_ID}/dist"
DIGEST=$(${GCLOUD} container images describe "${IMAGE}:latest" \
  --format='value(image_summary.fully_qualified_digest)')

if [[ -z "${DIGEST}" ]]; then
  echo "Could not resolve digest for ${IMAGE}:latest."
  exit 1
fi

# Inject secrets as env vars. spring.data.mongodb.uri was renamed to
# spring.mongodb.uri in Spring Boot 4, so the env var is SPRING_MONGODB_URI.
echo "Deploying ${DIGEST}"
${GCLOUD} run deploy dist --image "${DIGEST}" \
  --update-secrets "SPRING_MONGODB_URI=reciplease-database-url:latest,SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_AUDIENCES=reciplease-google-client-id:latest"