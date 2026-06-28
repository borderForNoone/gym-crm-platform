# Gym CRM application

![Build](https://github.com/borderForNoone/gym-crm-platform/actions/workflows/ci.yml/badge.svg?branch=develop)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=borderForNoone_gym-crm-platform&metric=coverage)](https://sonarcloud.io/summary/overall?id=borderForNoone_gym-crm-platform)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=borderForNoone_gym-crm-platform&metric=alert_status)](https://sonarcloud.io/summary/overall?id=borderForNoone_gym-crm-platform)

## 1. Prerequisites

Before running the application, make sure the following tools are installed:

```text
Java 21
Maven
PostgreSQL
Redis
ActiveMQ
```

## 2. Clone the project

```bash
git clone https://github.com/borderForNoone/gym-crm-application.git
cd gym-crm-application
```

## 3. Database Setup PostgreSQL

Before the first run, create a database and user with proper privileges:

```sql
CREATE DATABASE gym_db;
CREATE USER gym WITH PASSWORD 'gym';
GRANT ALL PRIVILEGES ON DATABASE gym_db TO gym;
```

## 4. Build the project

```bash
mvn clean compile
```

## 5. Run tests

Docker must be running before executing tests.

```bash
mvn test
```

## 6. Run the application from console

```bash
mvn spring-boot:run
```

If you want to run with a specific Spring profile, use:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

After startup, the application will be available at:

```text
http://localhost:8080/gym-crm-application
```

## 6.1 Messaging (ActiveMQ)

Communication between `gym-core-service` and `workload-service` is asynchronous, via ActiveMQ.

For the `local` profile, broker connection details are hardcoded (`tcp://localhost:61616`, user/password `admin`/`admin`) — just make sure a local ActiveMQ instance is running before starting the application.

For all other profiles (`dev`, `stg`, `prod`), the broker connection is read from environment variables and must be provided explicitly:

```text
ACTIVEMQ_BROKER_URL
ACTIVEMQ_USER
ACTIVEMQ_PASSWORD
```


## 7.  Actuator endpoints

The application exposes Spring Boot Actuator endpoints for health checks and Prometheus metrics.

Base local URL:

```text
http://localhost:8080/gym-crm-application
```

Available actuator endpoints:

```text
GET /actuator/health
GET /actuator/health/database
GET /actuator/health/trainee
GET /actuator/health/trainer
GET /actuator/health/trainingType
GET /actuator/health/userRepository
GET /actuator/metrics
GET /actuator/prometheus
```

### Examples

Check application health:

```bash
http://localhost:8080/gym-crm-application/actuator/health
```

Check database health:

```bash
http://localhost:8080/gym-crm-application/actuator/health/database
```

Check trainee health:

```bash
http://localhost:8080/gym-crm-application/actuator/health/trainee
```

Check trainer health:

```bash
http://localhost:8080/gym-crm-application/actuator/health/trainer
```

Check Prometheus metrics:

```bash
http://localhost:8080/gym-crm-application/actuator/prometheus
```

Metric descriptions:

```text
gym_trainees_total     - total number of created trainees since application startup
gym_trainers_total     - total number of created trainers since application startup
gym_trainings_total    - total number of created trainings since application startup
gym_trainees_active    - current number of active trainees
gym_trainers_active    - current number of active trainers
```