# UDB

# User Database

## Start Full Couchbase Server and UDB API
```bash
cd docker && docker-compose down --volumes && docker image prune -f && docker-compose up --build
```

## Start Lite Couchbase Server and UDB API
```bash
cd docker && docker-compose -f docker-compose-lite.yml down --volumes && docker image prune -f && docker-compose -f docker-compose-lite.yml up --build
```
