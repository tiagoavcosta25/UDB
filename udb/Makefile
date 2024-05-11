.PHONY: build build-light start-light

build:
	@cd docker && \
	docker-compose down --volumes && \
	docker image prune -f && \
	docker-compose build

build-light:
	@cd docker && \
	docker-compose -f docker-compose-lite.yml down --volumes && \
	docker image prune -f && \
	docker-compose -f docker-compose-lite.yml build

start: build
	@cd docker && \
	docker-compose up

start-light: build-light
	@cd docker && \
	docker-compose -f docker-compose-lite.yml up
