#!/usr/bin/env bash
# file: git-release.sh
# description: Update version, create commit and tag release.

# set -x
# trap read debug

set -e

RELEASE_VERSION=${RECIPLEASE_VERSION//\"}

# If release already exists
if [[ $(git tag -l "v${RELEASE_VERSION}") ]]; then
    echo "Release ${RELEASE_VERSION} already exists. STOPPING."
    exit 0
fi

echo "Creating a git tag for the version: ${RELEASE_VERSION}"
# Version Format:  yyyy.mm.dd-abcdef

MVN_VERSIONS_GENERATE_BACKUP_POMS=false

${MVN} -f modules/parent/pom.xml versions:set-property \
  -Dproperty="revision"  \
  -DnewVersion=${RELEASE_VERSION} \
  -DgenerateBackupPoms=${MVN_VERSIONS_GENERATE_BACKUP_POMS}


if [[ "${CI}" != true ]]; then
    echo "Releases can only be created from a CI. STOPPING."
    exit 0  
fi

COMMIT_MESSAGE="[skip ci] Release version ${RELEASE_VERSION}"
echo "Creating commit: ${COMMIT_MESSAGE}"

${GIT} commit -am "${COMMIT_MESSAGE}"
${GIT} tag "v${RELEASE_VERSION}"
${GIT} push --tags
${GIT} push

echo