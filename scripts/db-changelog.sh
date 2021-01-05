#!/bin/bash
# file: db-changelog.sh
# description: Generate DB changelog using Liquibase.

${MVN} install -DskipTests
${MVN} -pl dist liquibase:diff