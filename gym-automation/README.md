# bdd-tests

Component and integration BDD tests (Cucumber) for `gym-core-service` and `workload-service`.

Scenarios talk to both services purely over their public REST/JMS contracts, exactly like any
other real caller. There is **no Docker, no Testcontainers, and no in-process bootstrapping** —
this module assumes the services you want to test are **already running** somewhere reachable
(your local machine, a docker-compose stack, a CI environment), and simply points HTTP requests
at them.

## Prerequisites

- `gym-core-service` running and reachable (default: `http://localhost:8080`)
- `workload-service` running and reachable (default: `http://localhost:8082/workload-service`)
- Their usual dependencies (MySQL, MongoDB, Redis, ActiveMQ) up, exactly as when you run these
  services for local development — see the root `README.md`.

Start both services the same way you always do (IDE Run configuration, or
`mvn spring-boot:run -pl gym-core-service` / `-pl workload-service`) and wait until you see
`Started GymApplication` / `Started WorkloadService` in their logs before running these tests.

## Running the tests

These tests are **off by default** (`skipBddTests=true` in `pom.xml`) so they never block a
normal `mvn test` / `mvn verify` build — they only run when explicitly requested, since they
require a live environment.

### From the console (Maven)

```bash
mvn test -pl bdd-tests -DskipBddTests=false
```

### From the IDE

Run the class `com.gym.crm.bdd.CucumberTest` (right-click → Run) — it's a JUnit 5 Platform
Suite that discovers and runs every `.feature` file under `src/test/resources/features`.
Running it directly from the IDE bypasses the Surefire `skipBddTests` gate, so no extra flag is
needed there.

### Running a subset

Every scenario is tagged. Filter by tag with the standard Cucumber JUnit-platform property:

```bash
mvn test -pl bdd-tests -DskipBddTests=false -Dcucumber.filter.tags="@core"
```

Available tags: `@component`, `@core`, plus one per scenario (e.g. `@trainee-register`,
`@auth-login`, `@training-types`).

## Configuration

Defaults (base URLs, test-user credentials) live in
[`src/test/resources/bdd-test.yml`](src/test/resources/bdd-test.yml) — nothing is hardcoded in
Java. The nested YAML is flattened into dot-notation keys at load time (e.g.
`system.tests.core.base-url`), which is also the exact name of the system property that
overrides it. There are two ways to change a value, depending on how permanent the change is:

**Edit the file** if the new value should become the checked-in default for everyone (e.g. the
services' ports genuinely changed):

```yaml
system:
  tests:
    core:
      base-url: http://localhost:8080/api/v1
    workload:
      base-url: http://localhost:8082/workload-service/api/v1
    user:
      username: billy.herrington
      password: password
```

**Pass a system property** for a one-off run against a different environment — this always
wins over the file, no code or file changes needed:

```bash
mvn test -pl bdd-tests -DskipBddTests=false \
  -Dsystem.tests.core.base-url=http://staging-host:8080/api/v1 \
  -Dsystem.tests.workload.base-url=http://staging-host:8082/workload-service/api/v1
```

See `com.gym.crm.bdd.config.TestProperties` for how the two are merged (system property first,
then the flattened YAML file, with a hard failure if a key exists in neither).

## Module layout

| Package    | Responsibility                                                             |
|------------|-----------------------------------------------------------------------------|
| `config`   | `TestProperties` — resolves base URLs / credentials from the YAML file + system property overrides |
| `client`   | Thin HTTP wrapper (`ApiClient`) — the only place that knows about RestAssured |
| `support`  | Scenario-shared state (`TestContext`) and request-body builders (`Payloads`) |
| `steps`    | Cucumber step definitions, constructor-injected with `TestContext` via `cucumber-picocontainer` |
| `resources/features` | Gherkin scenarios (the tests themselves)                          |
| `resources/bdd-test.yml` | Externalized default configuration (nested YAML, flattened at load time) |

Step classes only ever call `ApiClient`, never RestAssured directly, and never hardcode a base
URL — always go through `TestProperties`.

## Troubleshooting

- **`Connection refused`** — the target service isn't running, or is running on a different
  port/context-path than the defaults in `bdd-test.yml`. Confirm it's up, or override
  the base URL as shown above.
- **`IllegalStateException: bdd-test.yml not found on the test classpath`** — the file
  isn't where `TestProperties` expects it (`src/test/resources/bdd-test.yml`), or a
  clean/rebuild is needed so it gets copied into `target/test-classes`.
- **`UndefinedStepException` / all steps undefined at once** — almost always a mismatch between
  the `GLUE_PROPERTY_NAME` value in `CucumberTest.java` and the actual `package` declared in the
  step classes under `steps/`. Both must be exactly `com.gym.crm.bdd.steps`.