#!/bin/bash
# file: release-backend.sh
# description: Update version, create commit and tag release.

CURRENT_VERSION=`mvn -q \
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec`
echo "Current Version: ${CURRENT_VERSION}"
echo "New Release Version: ${RECIPLEASE_VERSION}"
echo

echo "Bumping project version."
mvn versions:set \
	-DnewVersion=${RECIPLEASE_VERSION} \
	-DgenerateBackupPoms=false
echo

COMMIT_MESSAGE="[skip ci] Release version ${RECIPLEASE_VERSION}"
echo "Creating commit: ${COMMIT_MESSAGE}"

if [[ "${CI}"=="true" ]]; then
	git commit -am "${COMMIT_MESSAGE}"
	git tag "v${RECIPLEASE_VERSION}"
	git push --tags
fi