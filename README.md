# Driver

![Driver logo](docs/logo.png)

Personal decision-support desktop app for researching, comparing, scoring, and shortlisting vehicles for purchase. Local-first JSON persistence — no cloud, no database.

## Requirements

- Java 21 or later
- Maven 3.9+

## Build and Run

```bash
# Compile
mvn compile

# Run tests
mvn test

# Run from source
mvn exec:java -Dexec.mainClass="za.driver.Application"

# Build runnable fat JAR
mvn package
java -jar target/driver-1.1.jar
```

## Data Directory

On first run, Driver creates a `data/` directory in the current working directory (gitignored):

- `data/vehicles/{uuid}.json` — vehicle records
- `data/profiles/{uuid}.json` — scoring profiles (metric weights and aggregate composition)
- `data/app-config.json` — active scoring profile id
- `data/garage-config.json` — garage dimensions for clearance checks

Back up `data/` to preserve your research. Run the JAR from a directory where you want `data/` to live.

## Features

- Import vehicles from JSON or CSV; export fleet to CSV
- Export selected vehicles to an offline Reveal.js marketing presentation (File → Export Presentation…)
- Filter and sort by price, body type, fuel type, scores, and garage clearance
- Multiple scoring profiles with toolbar selector; create, duplicate, edit, and delete via **Config → Manage Profiles…**
- Configurable top metrics, aggregate name/weight, and aggregate composition per profile
- Star ratings and scatter plot for fleet comparison
- Freeform notes per vehicle

## Documentation

| Document | Purpose |
|----------|---------|
| [docs/design_spec.md](docs/design_spec.md) | Functional requirements and architecture |
| [docs/data_dictionary.md](docs/data_dictionary.md) | Entity fields, enums, import schema |
| [docs/scoring_spec.md](docs/scoring_spec.md) | Scoring formulae and weights |
| [docs/backlog.md](docs/backlog.md) | Deferred features and future work |

## Branding

| Asset | File | Usage |
|-------|------|-------|
| Logo | [docs/logo.png](docs/logo.png) | About dialog, documentation |
| Icon | [docs/icon.png](docs/icon.png) | Window and taskbar icon |

Branding files in `docs/` are copied into the application JAR at build time.

## License

Copyright (C) 2026 Alan McL

This project is licensed under the [GNU Affero General Public License v3.0](LICENSE) (AGPL-3.0).
See [https://www.gnu.org/licenses/agpl-3.0.html](https://www.gnu.org/licenses/agpl-3.0.html).
