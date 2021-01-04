#/bin/make

SHELL := /bin/bash
RECIPLEASE_PATH := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
RECIPLEASE_NAME ?= "Reciplease (Backend)"
RECIPLEASE_VERSION ?= $(shell date +%Y.%m.%d)-$(shell git rev-parse --short HEAD)
RECIPLEASE_DESCRIPTION ?= "Making the world a better place through recipe and inventory management."

ENV := local
-include config/.env.${ENV}
export

.DEFAULT_GOAL := help-backend
.PHONY: help-backend #: Display a list of commands and exit.
help: help-backend # alias for quick access
help-backend:
	@cd ${RECIPLEASE_PATH} && awk 'BEGIN {FS = " ?#?: "; print ""$(RECIPLEASE_NAME)" "$(RECIPLEASE_VERSION)"\n"$(RECIPLEASE_DESCRIPTION)"\n\nUsage: make \033[36m<command>\033[0m\n\nCommands:"} /^.PHONY: ?[a-zA-Z_-]/ { printf "  \033[36m%-10s\033[0m %s\n", $$2, $$3 }' $(MAKEFILE_LIST)

.PHONY: docs-backend #: Run documentation.
docs: docs-backend # alias for quick access
docs-backend:
	@false

.PHONY: lint-backend #: Run static code analysis.
lint: lint-backend # alias for quick access
lint-backend:
	@false

.PHONY: init-backend #: Initialise maven
init: init-backend # alias for quick access
init-backend:
	@cd ${RECIPLEASE_PATH} && \
	${MVN} dependency:go-offline

.PHONY: test-backend #: Run tests
test: test-backend # alias for quick access
test-backend:
	@cd ${RECIPLEASE_PATH} && \
	${MVN} test

.PHONY: run-backend #: Run application
run: run-backend # alias for quick access
run-backend:
	@cd ${RECIPLEASE_PATH} && \
	${MVN} -pl dist -am spring-boot:run

# Run scripts using make
%:
	@cd ${RECIPLEASE_PATH} && \
	if [[ -f "scripts/${*}.sh" ]]; then \
	./scripts/${*}.sh; fi

.PHONY: build-backend #: Build executable JAR
build: build-backend # alias for quick access
build-backend:
	@cd ${RECIPLEASE_PATH} && \
	${MVN} package -DskipTests

.PHONY: release-backend #: Update version, create commit and tag
release: release-backend # alias for quick access
release-backend:
	@cd ${RECIPLEASE_PATH} && \
	./scripts/release-backend.sh

.PHONY: image-backend #: Create Docker image.
image: image-backend # alias for quick access
image-backend:
	@cd ${RECIPLEASE_PATH} && \
	./scripts/image-backend.sh

.PHONY: db-diff-backend #: Generate DB changelog
db-diff: db-diff-backend # alias for quick access
db-diff-backend:
	@cd ${RECIPLEASE_PATH} && \
	${MVN} install -DskipTests
	${MVN} -pl dist liquibase:diff \
		-Dliquibase.verbose=true \
		-Dliquibase.password=${DATABASE_PASSWORD}

.PHONY: deploy-backend #: Deploy Docker image.
deploy: deploy-backend # alias for quick access
deploy-backend:
	@cd ${RECIPLEASE_PATH} && \
	./scripts/deploy-backend.sh

.PHONY: open-backend
open: open-backend # alias for quick access
open-backend:
	@${OPEN} ${RECIPLEASE_URL}

.PHONY: clean-backend #: Clear build files.
clean: clean-backend # alias for quick access
clean-backend:
	@${MVN} clean
