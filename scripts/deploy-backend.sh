#!/bin/bash
# file: deploy-backend.sh
# description: Deploy Docker image to GCP.

if [[ -z "${GCP_REGION}" ]]; then
	echo '${PROJECT_ID} is not set.'
	exit 1
fi

if [[ -z "${PROJECT_ID}" ]]; then
	echo '${PROJECT_ID} is not set.'
	exit 1
fi

${GCLOUD} config set run/platform managed
${GCLOUD} config set run/region ${GCP_REGION}
${GCLOUD} run deploy dist --image gcr.io/${PROJECT_ID}/dist