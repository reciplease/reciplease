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
# Deploying the bare `gcr.io/$PROJECT/dist` (:latest) reference let Cloud Run
# keep resolving to a stale digest, so freshly-pushed images never went live
# (every revision stayed pinned to the same old digest). Pinning the digest
# guarantees each new push rolls a new revision forward.
IMAGE="gcr.io/${GCP_PROJECT_ID}/dist"
DIGEST=$(${GCLOUD} container images describe "${IMAGE}:latest" \
  --format='value(image_summary.fully_qualified_digest)')

if [[ -z "${DIGEST}" ]]; then
  echo "Could not resolve digest for ${IMAGE}:latest."
  exit 1
fi

echo "Deploying ${DIGEST}"
${GCLOUD} run deploy dist --image "${DIGEST}"