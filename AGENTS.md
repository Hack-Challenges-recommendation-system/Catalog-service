# catalog-service

The catalog of educational materials from the case data, with Russian full-text search. Read-only for clients.

## Project

Recommendation System is an entry for Hack&Change 2026, Alfa-Bank case «Альфа-Курс: знания в нужный момент», track 1: personal recommendations of educational materials for entrepreneurs. A vendor LLM ranks and explains materials; the services around it supply context, check its answers and record what users do.

| Repository | Role |
|---|---|
| `api-gateway` | Single entry point. Checks access tokens, routes `/api/v1/**`, sets `X-User-Id`. |
| `auth-service` | Registration, login, access and refresh JWT, logout denylist. |
| `profile-service` | Business profile of the user (onboarding answers). |
| `catalog-service` | Catalog of materials from the case data, with Russian full-text search. |
| `metric-service` | Raw user events and the metrics computed from them. |
| `recommendation-service` | Builds recommendations with the LLM. |
| `frontend` | Angular web app. |
| `Documentation` | Architecture docs. Only developers commit there; agents never do. |

All repositories live in the GitHub organization `Hack-Challenges-recommendation-system` and are **public**.

## Rules for every change

These come from the case. Breaking one can disqualify the submission.

1. **The case data never goes into git or a Docker image**: not the dump, not a copy, not an extract. Services read it from a path mounted at runtime; `/data/` is in `.gitignore`. Synthetic test data written by the team may be committed.
2. **No keys, tokens or passwords in git.** Read them from environment variables; `env.example` files list names only.
3. **External APIs, such as the LLM, get only the fragments they need.** No user ids, names, emails or other personal data, and no paid or closed materials.
4. **Paid materials:** show only a short benefit and the provided link, never their content.
5. **Everything must start with one `docker compose up` from the README.** A submission that doesn't start scores 0. Don't add infrastructure or a required external service without agreeing on it first.
6. **Simple UI, no intrusive notifications.**

## Commits

Every commit follows [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/), in the style the history already uses:

    <type>(<scope>): <what was changed>

- **Type:** `feat`, `fix`, `refactor`, `perf`, `test`, `docs`, `build`, `ci`, `chore`, `style` or `revert`.
- **Scope:** optional; the area that changed, e.g. `(jwt)`, `(kafka)`, `(migrations)`, `(controllers)`, `(CI/CD)`.
- **Description:** English, past tense, e.g. `fix(cookie): logout cookie name was fixed`.
- **Breaking change:** `!` after the type or scope, plus a `BREAKING CHANGE:` footer saying what callers must change.
- One logical change per commit. Commit to `develop`; `main` receives merges from `develop`.

## Code conventions

- Java 25, Spring Boot 4.1.2, Maven. Base package `team.nofantasy.recommendationsystem.<service>`, entry point `Main`.
- Three layers: `controller` → `service` → `repository`. Controllers never touch repositories or entities.
- Two DTO layers, both Java records:
  - `dto/controller/<feature>/*ControllerDto`: HTTP requests and responses, with Jakarta validation.
  - `dto/service/<feature>/*ServiceDto`: between controllers and services. Kafka messages live in `dto/service/kafka`, import rows in `dto/service/importer`.
- Mapping goes through MapStruct interfaces in `mapper/`. Unmapped source or target properties fail the build, so map every field or ignore it explicitly. Keep methods in pairs: controller-to-service first, then service-to-controller.
- Entities in `entity/` use Lombok: `@Getter`, `@Setter`, `@Builder` and a protected no-args constructor. Enums of an entity are nested in it.
- Domain exceptions live in `exception/`; `controller/advice/GlobalExceptionHandler` turns them into HTTP statuses.
- Schema changes go only into new Flyway files in `src/main/resources/database/migrations` (`V<n>__<description>.sql`). Never edit a migration that a shared database already ran. Hibernate runs with `ddl-auto: validate`.
- Endpoints sit under `/api/v1` (`spring.mvc.servlet.path`). Each service owns distinct top-level paths, so the gateway can route by prefix.
- The gateway puts the signed-in user's id into `X-User-Id`. Read it with `@RequestHeader("X-User-Id") UUID userId`, never from the body.
- Log with the SLF4J fluent API: `log.atInfo().setMessage(...).addKeyValue(...).log()`. Files go to `/logs/<service>.log` as ECS JSON.
- Configuration comes from environment variables, with local defaults in `application.yaml`.

## Build and test

- `mvn -B -ntp verify` runs all tests. Integration tests use Testcontainers, so Docker must be running; without Docker they are skipped.
- `mvn -B -ntp -DskipTests package` builds `target/app.jar`, which the `Dockerfile` copies.
- `docker compose up -d` starts the local infrastructure of this service (`docker-compose.yaml`).
- In MockMvc tests, request the full path and set the servlet path: `get("/api/v1/...").servletPath("/api/v1")`.
- CI: `.github/workflows/ci-cd.yml` tests every push and pull request, pushes the image to Docker Hub from `main`, and deploys on a manual run. Its header lists the secrets and variables it needs.

## How the catalog works

- At startup `CatalogImporter` loads the file at `CATALOG_IMPORT_PATH` (e.g. `file:/data/catalog.json`) and upserts materials by `id`. An empty path skips the import.
- The file is a JSON array of `MaterialImportDto`: `id`, `title`, `summary`, `format`, `durationMinutes`, `level`, `topics`, `businessStages`, `spheres`, `paid`, `url`. Rows without `id`, `title` or `url` are skipped.
- **The case data has its own layout, which nobody has mapped yet.** Convert it to this format or add an adapter in `importer/`. Don't guess the field mapping; ask.
- Paid materials: the importer cuts `summary` to 300 characters, so it can hold only a short benefit, and the API returns it with the provided `url`.
- Search uses a generated `search_vector` column (`russian` configuration; title weight A, topics B, summary C) with a GIN index. A query matches any of its words (`plainto_tsquery` with `&` replaced by `|`) and is ranked by `ts_rank_cd`, so materials matching more words come first. This is the topic-match baseline the case compares recommendations against.
- Anthropic has no embeddings API, so there is no vector search.

## Endpoints (under `/api/v1`, all public)

| Method and path | What it does |
|---|---|
| `GET /materials` | Filters `format`, `stage`, `sphere`, `paid`; pages `page` (from 0) and `size` (1–500) |
| `GET /materials/{id}` | One material; 404 if unknown |
| `GET /materials/by-ids?ids=a&ids=b` | Up to 200 ids; keeps their order and skips unknown ones |
| `GET /materials/search?q=...&stage=...&limit=...` | `q` up to 500 characters, `limit` 1–100; returns `{material, score}` |
| `GET /health` | 200 |

## Configuration

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5437/postgres` |
| `DB_USERNAME`, `DB_PASSWORD` | `admin`, `admin` |
| `CATALOG_IMPORT_PATH` | empty: no import |

`docker compose up -d` starts Postgres on `127.0.0.1:5437`.

## Tests

- `CatalogIntegrationTest` imports the synthetic `src/test/resources/fixtures/catalog.json` into real Postgres. It checks Russian word forms, any-word matching, ranking, the stage filter, the paid summary, the order of `by-ids`, paging and validation.
- `CatalogImporterTest` covers the import rules.
