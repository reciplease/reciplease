#/bin/make

SHELL := /bin/bash
RECIPLEASE_NAME ?= "Reciplease"
RECIPLEASE_VERSION ?= $(shell date +%Y.%m.%d)-$(shell git rev-parse --short HEAD)
RECIPLEASE_DESCRIPTION ?= "Making the world a better place through recipe and inventory management."
ENV := local

-include .env
export

.ONESHELL:

%: %-backend
	@true

.DEFAULT_GOAL := help-backend
.PHONY: help-backend #: Display a list of commands and exit.
help-backend:
	@awk 'BEGIN {FS = " ?#?: "; print ""$(RECIPLEASE_NAME)" "$(RECIPLEASE_VERSION)"\n"$(RECIPLEASE_DESCRIPTION)"\n\nUsage: make \033[36m<command>\033[0m\n\nCommands:"} /^.PHONY: ?[a-zA-Z_-]/ { printf "  \033[36m%-10s\033[0m %s\n", $$2, $$3 }' $(MAKEFILE_LIST)

.PHONY: lint-backend #: Run static code analysis.
lint-backend:
	@false

.PHONY: jar-backend #: Build executable JAR
jar-backend:
	@mvn package -DskipTests

.PHONY: test-backend #: Run tests
test-backend:
	@mvn test

.PHONY: run-backend #: Run application
run-backend:
	@$(MAKE) build
	java -jar -Dspring.profiles.active=${ENV} dist/target/reciplease-dist.jar --server.port=${RECIPLEASE_PORT}

.PHONY: version-backend #: Update version
version-backend:
	@mvn versions:set -DnewVersion=$(RECIPLEASE_VERSION) -DgenerateBackupPoms=false

.PHONY: release-backend #: Update version, create commit and tag
release-backend:
	@$(MAKE) version
	git commit -am "[skip ci] Release version $(RECIPLEASE_VERSION)"
	git tag "v$(RECIPLEASE_VERSION)"

.PHONY: image-backend
image-backend:
	@$(MAKE) jar
ifeq (,$(wildcard ./reciplease-dist.jar))
	cp dist/target/reciplease-dist.jar .
	echo "Copied jar to root"
endif
	function tearDown {
		echo "Removing jar"
		rm reciplease-dist.jar
	}
	trap tearDown EXIT
	docker build -t gcr.io/$(PROJECT_ID)/dist:latest .
#	mvn -pl dist -am spring-boot:build-image -D"spring-boot.build-image.imageName"=gcr.io/$(PROJECT_ID)/dist:latest -DskipTests

.PHONY: clean-backend
clean-backend:
	@mvn clean

.PHONY: open-backend
open-backend:
	@open http://localhost:${RECIPLEASE_PORT}