[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://travis-ci.org/jveverka/iam-service.svg?branch=master)](https://travis-ci.org/jveverka/iam-service)

# IAM service
Really simple IAM service, authentication and authorization server.

This project is __WIP__ !

## Architecture
![architecture](docs/IAM-service-architecture.svg)

### Components
* __iam-core__ - core implementation of IAM business logic.
* __iam-service__ - springboot IAM as microservice. 

## Data model
![data-model](docs/IAM-data-model.svg)

## Build and Run
```
gradle clean build test
```


