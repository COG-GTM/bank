# Outage Investigation: notification-service Failure

## Scenario

The `notification-service` is failing because `discovery-service` is unreachable.
This document traces the dependency chain, identifies the blast radius, explains the
evidence, and proposes configuration changes to add resilience.

---

## 1. Dependency Chain

Using the dependency graph built from `docker-compose.yml`, Eureka registration metadata,
and Spring Cloud Gateway routes, we trace how `notification-service` depends on
`discovery-service`:

```
notification-service --[EUREKA: registers with]--> discovery-service
```

`notification-service` registers itself with the Eureka registry hosted by
`discovery-service` (`eureka.client.service-url.defaultZone`). When `discovery-service`
becomes unreachable:

1. `notification-service` cannot register its instance, so other services cannot
   discover it via Eureka.
2. `notification-service` cannot fetch the service registry, so it cannot resolve
   any Feign client targets that rely on Eureka (if it needed to call other services).
3. Docker Compose `depends_on: discovery-service` means `notification-service` will
   not even start if `discovery-service` has not started.

Additionally, `notification-service` has a transitive `depends_on` on `gateway-service`,
which itself depends on `discovery-service`:

```
notification-service --[depends_on]--> gateway-service --[depends_on]--> discovery-service
```

---

## 2. Blast Radius

If `discovery-service` goes down, **every other service** in the system is affected,
because all services register with Eureka and use discovery-first routing:

| Affected Service          | Impact                                                        |
|---------------------------|---------------------------------------------------------------|
| **gateway-service**       | Cannot resolve downstream service locations; all API routing fails |
| **notification-service**  | Cannot register; callers (account-service, authentication-service) cannot find it |
| **authentication-service**| Cannot register; login/auth endpoints become unreachable via gateway |
| **customer-service**      | Cannot register; customer management endpoints unreachable    |
| **account-service**       | Cannot register; account/transaction endpoints unreachable    |

**Total blast radius: 5 services** — complete system outage.

`discovery-service` is a **single point of failure** for the entire bank platform.

---

## 3. Evidence

1. **Docker Compose `depends_on`**: All services (`gateway-service`,
   `notification-service`, `customer-service`, `account-service`) declare
   `depends_on: discovery-service`. This means Docker will not start them until
   `discovery-service` is running, but provides no health-check guarantee.

2. **Eureka client configuration**: Every service configures
   `eureka.client.service-url.defaultZone` pointing to a **single** discovery
   instance (`http://discovery-service:8761/eureka/`). There is no failover URL.

   - `notification-service/src/main/resources/application.properties:6`
   - `gateway-service/src/main/resources/application.yml:32`
   - `customer-service/src/main/resources/application.properties:11`
   - `account-service/src/main/resources/application.properties:8`
   - `authentication-service/src/main/resources/application.properties:16`

3. **Feign clients rely on Eureka**: `account-service` uses
   `@FeignClient(name = "NOTIFICATION-SERVICE")` and
   `@FeignClient(name = "CUSTOMER-SERVICE")`. `authentication-service` uses
   `@FeignClient(name = "NOTIFICATION-SERVICE")`. These resolve via Eureka — if
   discovery is down, Feign calls fail with `LoadBalancerNotFoundException`.

4. **No circuit breaker configured**: None of the services include Resilience4j or
   Hystrix dependencies. A transient discovery outage cascades immediately into
   hard failures on every inter-service call.

5. **Single Eureka instance**: `discovery-service` runs as a standalone instance
   (`eureka.client.register-with-eureka=false`, `eureka.client.fetch-registry=false`).
   There is no peer replication or HA setup.

---

## 4. Proposed Configuration Changes for Resilience

### 4.1 Run Multiple Discovery Instances (High Availability)

Deploy at least two Eureka server replicas that peer-replicate with each other.

**docker-compose.yml** — add a second discovery instance:

```yaml
discovery-service-1:
  image: discovery-service
  ports:
    - "8761:8761"
  environment:
    - EUREKA_INSTANCE_HOSTNAME=discovery-service-1
    - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://discovery-service-2:8762/eureka/
    - EUREKA_CLIENT_REGISTER_WITH_EUREKA=true
    - EUREKA_CLIENT_FETCH_REGISTRY=true

discovery-service-2:
  image: discovery-service
  ports:
    - "8762:8761"
  environment:
    - EUREKA_INSTANCE_HOSTNAME=discovery-service-2
    - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://discovery-service-1:8761/eureka/
    - EUREKA_CLIENT_REGISTER_WITH_EUREKA=true
    - EUREKA_CLIENT_FETCH_REGISTRY=true
```

Then update all services to point to **both** URLs:

```properties
eureka.client.service-url.defaultZone=http://discovery-service-1:8761/eureka/,http://discovery-service-2:8762/eureka/
```

### 4.2 Extend Eureka Lease Durations

On every client service, increase the lease window so instances are not
prematurely evicted during a brief discovery outage:

```properties
eureka.instance.lease-renewal-interval-in-seconds=10
eureka.instance.lease-expiration-duration-in-seconds=30
```

### 4.3 Enable Eureka Client Registry Caching

Ensure services retain a local cache of the registry so they can continue
making Feign calls even if discovery is temporarily unreachable:

```properties
eureka.client.registry-fetch-interval-seconds=5
eureka.client.disable-delta=false
```

### 4.4 Add Circuit Breakers with Resilience4j

Add `spring-cloud-starter-circuitbreaker-resilience4j` to each service's
`pom.xml` and configure fallback behavior for Feign clients:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
```

Configure default circuit breaker settings in `application.properties`:

```properties
resilience4j.circuitbreaker.instances.default.sliding-window-size=10
resilience4j.circuitbreaker.instances.default.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.default.wait-duration-in-open-state=10s
```

### 4.5 Add Health Checks to Docker Compose `depends_on`

Replace bare `depends_on` with health-check conditions so services wait
until discovery is actually accepting requests:

```yaml
discovery-service:
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
    interval: 10s
    timeout: 5s
    retries: 5

notification-service:
  depends_on:
    discovery-service:
      condition: service_healthy
```

---

## 5. Summary

| Finding                     | Detail                                              |
|-----------------------------|-----------------------------------------------------|
| **Root cause**              | Single Eureka instance is a single point of failure |
| **Immediate symptom**       | notification-service cannot register or resolve peers|
| **Blast radius**            | All 5 downstream services (complete outage)         |
| **Primary recommendation**  | Deploy HA Eureka (2+ replicas with peer replication) |
| **Secondary recommendations** | Circuit breakers, extended leases, health checks  |
