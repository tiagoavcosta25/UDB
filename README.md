# UDB (User Database)

## Introduction
UDB is a user management system designed to utilize high-performance technologies for efficient data handling and user management. This document outlines the technology stack, setup instructions, and commands for building and deploying the User Database (UDB).

## Technology Stack

### Java 17
- **Use in Project**: Serves as the core programming language for developing the UDB API, ensuring robustness and high performance.

### Spring Framework
- **Use in Project**: Utilized for dependency injection, configuration, and overall application context management, making the UDB API scalable and easier to manage.

### Couchbase
- **Use in Project**: Acts as the data store for UDB, offering rapid access and flexible data structures to support varied user data requirements.

### Docker
- **Use in Project**: Ensures consistent environments for development, testing, and production using containerization, simplifying deployment and scalability.

## Prerequisites
- Docker
- Java 17 SDK

## Build and Start Instructions 

### Full Setup
To build and start the full Couchbase server along with the UDB API, run the following command:
```bash
make build-and-start
```

### Lite Setup
For development or testing environments, a lighter version of the Couchbase server can be used to conserve resources:
```bash
make build-and-start-light
```

### Build UDB
```bash
make build-and-start-udb
```

### Build Couchbase
```bash
make build-and-start-couchbase
```

### Useful commands
Check if dependencies are ok
```bash
mvn dependency:analyze
```