#!/usr/bin/env bash
# file: liquibase-changelog.sh
# description: Generate DB changelog using Liquibase.

${MVN} -pl modules/dist liquibase:diff