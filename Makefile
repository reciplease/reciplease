#!/usr/bin/env make

RECIPLEASE_PATH := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
RECIPLEASE_NAME := Reciplease (Backend)
RECIPLEASE_VERSION := $(shell date +%Y.%m.%d)-$(shell git rev-parse --short HEAD)
RECIPLEASE_DESCRIPTION := Making the world a better place through recipe and inventory management.

ENV := local
-include config/.env.${ENV}
-include config/secrets/.env.*.${ENV}
export

.DEFAULT_GOAL := help
.PHONY: help #: Display a list of commands and exit.
help:
	@${AWK} 'BEGIN {FS = " ?#?: "; print "${RECIPLEASE_NAME} ${RECIPLEASE_VERSION}\n${RECIPLEASE_DESCRIPTION}\n\nUsage: make \033[36m<command>\033[0m\n\nCommands:"} /^.PHONY: ?[a-zA-Z_-]/ { printf "  \033[36m%-10s\033[0m %s\n", $$2, $$3 }' $(MAKEFILE_LIST)

.PHONY: docs #: Run documentation.
docs:
	@${MVN} -pl docs site:run

.PHONY: lint #: Run static code analysis.
lint:
	@false

.PHONY: tests #: Run tests
test: tests # alias for quick access
tests:
	@${MVN} test

.PHONY: run #: Run application
run: postgres
	@${MVN} -pl modules/dist -am spring-boot:run

# Run scripts using make
%:
	@if [[ -f "scripts/${*}.sh" ]]; then \
	${BASH} "scripts/${*}.sh"; fi

.PHONY: config #: Create config file based on ENV variable.
config: config/.env.${ENV}
config/.env.%:
	@cp -n config/.env.example config/.env.${ENV}

.PHONY: init #: Download Maven dependencies.
init:
	@${MVN} dependency:go-offline

.PHONY: build #: Build application JAR.
build:
	@${MVN} package -DskipTests
	@mkdir build && cp modules/dist/target/reciplease-dist.jar build/.

.PHONY: release #: Update version, create commit and tag
release:
	@${BASH} scripts/git-release.sh

.PHONY: image #: Create Docker image.
image:
	@${BASH} scripts/gcp-docker-build.sh

.PHONY: deploy #: Deploy Docker image.
deploy:
	@${BASH} scripts/gcp-docker-deploy.sh

.PHONY: open #: Open application.
open:
	@${OPEN} ${RECIPLEASE_URL}

.PHONY: clean #: Clear build files.
clean:
	@${MVN} clean
	@[[ ! -d build ]] || rm -r build
