# AGENTS.md — Reciplease backend

Quick reference for what test suites exist in this repo and how to run them.
This is a multi-module Maven project (hexagonal architecture: `core` = domain +
ports, `database` = Mongo adapters, `web` = HTTP controllers/security,
`features` = end-to-end Cucumber suite, `dist` = the deployable jar).

## Test suites

| Module               | What it tests                                                                 | Style                                  |
|-----------------------|--------------------------------------------------------------------------------|-----------------------------------------|
| `modules/core`        | Domain models, services (business rules, no I/O)                              | JUnit 5 + Mockito (mocked repositories) |
| `modules/database`    | Repository implementations against a real Mongo                               | JUnit 5 + `@DataMongoTest` (embedded Mongo via flapdoodle) |
| `modules/web`         | Controllers, DTOs, security config (`@PreAuthorize`, `HouseAccess`, etc.)     | JUnit 5 + `@WebMvcTest` + MockMvc, Mockito for service/repository beans |
| `modules/features`    | End-to-end: boots `core`+`web`+`database` together, drives them over HTTP     | Cucumber (`cucumber-spring`) + MockMvc, embedded Mongo |

There is no Playwright/browser-level suite in this repo — that lives in the
frontend repo (`reciplease-nextjs`), which talks to a real deployed backend.

## Running tests

```bash
mvn test          # runs every module above, including the features/Cucumber suite
make test         # same thing — what CI ("Run tests" step in .github/workflows/test.yml) runs
mvn -pl modules/features -am test   # just the Cucumber e2e suite
mvn -pl modules/web -am test        # just the web module
```

`mvn test` (and therefore `make test`) builds the whole reactor and runs every
module's tests, since `modules/features` is registered in the root `pom.xml`'s
`<modules>` list — there's no separate "e2e" CI step or Maven profile to remember.

## Code style

No comments. None — not "what" comments, not "why" comments, not javadocs.
Code must be self-documenting: if a comment feels necessary, that's a signal
to rename something, extract a well-named method/variable, or restructure the
logic instead of explaining it. Clean Code rules here. This applies to new
code and to code you touch; don't go out of your way to strip comments from
files you aren't otherwise editing.

## Writing new tests

- Domain/service logic → `modules/core/src/test/java`, mock the repository
  interfaces with Mockito (see `InviteServiceTest`).
- A new repository method → add a test in `modules/database/src/test/java`
  using `@DataMongoTest` + `@Import(YourRepositoryImpl.class)` (see
  `HouseRepositoryImplTest`).
- A new controller endpoint → `modules/web/src/test/java`, `@WebMvcTest`,
  stub `HouseAccess` manually with `@MockitoBean(name = "houseAccess")` (the
  `@WithHouseOwner`/`@WithHouseMember` annotations only set up the security
  context's role — they do **not** auto-stub house membership).
- A new cross-module user flow (e.g. "owner invites someone, they accept,
  they get the right role") → add a scenario to
  `modules/features/src/test/resources/features/*.feature` and a matching step
  class under `modules/features/src/test/java/org/reciplease/features/steps/`.
