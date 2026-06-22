# Agent Guide — Driver

![Driver logo](docs/logo.png)

Personal decision-support desktop app for researching, comparing, scoring, and shortlisting vehicles for purchase. Local-first, JSON persistence, no cloud.

## Key Documents

| Document | Purpose |
|----------|---------|
| [docs/design_spec.md](docs/design_spec.md) | Functional requirements, architecture, derived metrics, non-goals |
| [docs/scoring_spec.md](docs/scoring_spec.md) | Scoring formulae, weights, scale constants — **source of truth for score calculations** |
| [docs/data_dictionary.md](docs/data_dictionary.md) | Entity fields, enums, import schema — **source of truth for model shapes** |
| [docs/backlog.md](docs/backlog.md) | Deferred features and future work |

Read `design_spec.md` and `data_dictionary.md` before changing behaviour. See `backlog.md` for deferred features — do not implement backlog items unless explicitly requested.

## Stack

| Component | Choice |
|-----------|--------|
| Language | Java 21 |
| Build | Maven (single module) |
| UI | Swing |
| JSON | Jackson (`jackson-databind`, `jackson-datatype-jsr310`) |
| Testing | JUnit 5 |
| Persistence | File-based JSON in `data/` (gitignored) |

## Directory Layout

```text
driver/
├── pom.xml
├── AGENTS.md
├── README.md
├── src/main/java/za/driver/
│   ├── Application.java               Entry point
│   ├── model/                         Domain entities and enums
│   ├── persistence/                   JSON repositories
│   ├── scoring/                       Metric calculators (no Swing)
│   ├── import_/                       JSON import and validation
│   ├── service/                       Business logic orchestration
│   └── ui/                            Swing panels and dialogs
├── src/test/java/za/driver/
│   ├── scoring/
│   └── import_/
├── data/                              Runtime state (gitignored)
│   ├── vehicles/{uuid}.json
│   ├── profiles/{uuid}.json
│   └── test-drives/{uuid}.json
└── docs/
    ├── design_spec.md
    ├── data_dictionary.md
    ├── backlog.md
    ├── logo.png                         Brand wordmark (About dialog, docs)
    └── icon.png                         App window/taskbar icon
```

## Architecture

```text
Swing UI  →  Service layer  →  Repository layer  →  JSON files
                  ↓
            Scoring engine (pure Java, unit-tested)
```

### Rules

1. **Raw data first** — store factual vehicle specs; never manually edit derived scores in the UI.
2. **Derived scores** — calculated by `ScoringService`; recalculated when vehicle data or active profile changes.
3. **Configurable scoring** — profile weight changes must not require re-importing vehicles.
4. **Source preservation** — every imported vehicle retains `Source` provenance.
5. **Layer separation** — UI calls services; services call repositories; `scoring/` and `import_/` have no Swing imports.
6. **EDT safety** — file I/O and scoring run off the Event Dispatch Thread via `SwingWorker`.

## Build and Run

```bash
# Compile
mvn compile

# Run tests
mvn test

# Run application (once exec plugin is configured in Phase 1)
mvn exec:java -Dexec.mainClass="za.driver.Application"

# Package fat JAR (Phase 9)
mvn package
java -jar target/driver-1.0-SNAPSHOT.jar
```

## Phase Discipline

The implementation roadmap is complete. New work should align with `design_spec.md` and `data_dictionary.md`. Check [docs/backlog.md](docs/backlog.md) for deferred items; only implement those when explicitly requested.

## Testing Expectations

| Layer | Testing |
|-------|---------|
| `scoring/` | JUnit 5 unit tests required |
| `import_/` | JUnit 5 validation tests required |
| `persistence/` | Round-trip serialization tests |
| `ui/` | Manual testing; no automated UI tests initially |

Run `mvn test` before marking any phase complete.

## Data Conventions

- Entity IDs: `java.util.UUID`
- Dates: `java.time.LocalDate`; date-times: `java.time.LocalDateTime`
- Pricing: `priceZar` as `BigDecimal`
- Enum names in Java match data dictionary values exactly (e.g. `VehicleStatus.CANDIDATE`)
- JSON field names use camelCase matching the data dictionary (e.g. `modelYear`, `fuelConsumptionCombined`)
- One JSON file per entity; filename = `{uuid}.json`

## What Not To Do

Per [design_spec.md](docs/design_spec.md) non-goals:

- No vehicle financing calculations
- No dealer inventory management
- No maintenance tracking or trade-in valuation
- No ML-based recommendations
- No Spring Boot or other heavy frameworks
- No embedded database (H2, SQLite) — JSON files only
- No cloud services or network dependencies
- No JavaFX — Swing only
- Do not invent fields, enums, or metrics not defined in the data dictionary

## Import Format

```json
{
  "schemaVersion": 1,
  "vehicle": { }
}
```

Partial records are valid. Missing values are permitted. See [data_dictionary.md](docs/data_dictionary.md) for the full schema.
