#!/bin/bash
# file: release-backend.sh
# description: Update version, create commit and tag release.

echo "New Release Version: ${RECIPLEASE_VERSION}"

# If release already exists
if [ $(git tag -l "v${RECIPLEASE_VERSION}") ]; then
    echo "Release Already Exists. SKIPPING."
    exit 0
fi

${MVN} versions:set \
  -DnewVersion=${RECIPLEASE_VERSION} \
  -DgenerateBackupPoms=false

if [[ "${CI}" = true ]]; then
  COMMIT_MESSAGE="[skip ci] Release version ${RECIPLEASE_VERSION}"
  echo "Creating commit: ${COMMIT_MESSAGE}"

  ${GIT} commit -am "${COMMIT_MESSAGE}"
  ${GIT} tag "v${RECIPLEASE_VERSION}"
  ${GIT} push --tags
  ${GIT} push
fi

echo