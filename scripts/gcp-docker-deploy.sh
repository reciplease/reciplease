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

echo "Deploying latest Docker image."
${GCLOUD} config set run/platform managed
${GCLOUD} config set run/region ${GCP_REGION}
${GCLOUD} run deploy dist --image gcr.io/${GCP_PROJECT_ID}/dist