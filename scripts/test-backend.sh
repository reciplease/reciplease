#!/bin/bash
# file: test-backend.sh
# description: Test maven and image build

${MVN} package

if [[ ! $? ]]; then
  echo "Maven build failed"
  exit 1
fi

make image

if [[ ! $? ]] || [[ "$(${DOCKER} images -q gcr.io/${PROJECT_ID}/dist:latest 2>/dev/null)" == "" ]]; then
  echo "Image build failed"
  exit 1
fi
