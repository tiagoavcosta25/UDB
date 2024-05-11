.PHONY: all help do-build build build-light do-start start start-light build-and-start build-and-start-light

DC_FULL_FILE = docker-compose.yml
DC_LITE_FILE = docker-compose-lite.yml

all: help

help:
	@echo "Usage:"
	@echo "  make build                	- Rebuild all containers"
	@echo "  make build-light          	- Rebuild lightweight containers"
	@echo "  make start                	- Start all containers"
	@echo "  make start-light          	- Start lightweight containers"
	@echo "  make build-and-start      	- Rebuild and start all containers"
	@echo "  make build-and-start-light - Rebuild and start lightweight containers"

do-build:
	@cd docker && \
	docker-compose -f $(DC_FILE) down --volumes && \
	docker image prune -f && \
	docker-compose -f $(DC_FILE) build

do-start:
	@cd docker && \
	docker-compose -f $(DC_FILE) up -d

build:
	$(MAKE) do-build DC_FILE=$(DC_FULL_FILE)

build-light:
	$(MAKE) do-build DC_FILE=$(DC_LITE_FILE)

start:
	$(MAKE) do-start DC_FILE=$(DC_FULL_FILE)

start-light:
	$(MAKE) do-start DC_FILE=$(DC_LITE_FILE)

build-and-start: build start

build-and-start-light: build-light start-light
