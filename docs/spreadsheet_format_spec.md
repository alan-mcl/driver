# Driver — CSV Format Specification

Specification for exporting and importing vehicle data as comma-separated values (`.csv`).

**Related documents:**

- [data_dictionary.md](data_dictionary.md) — canonical field definitions
- [import_format_spec.md](import_format_spec.md) — JSON import format

---

## File structure

Each file is a single UTF-8 CSV table:

- Row 1: fixed column headers matching `VehicleSpreadsheetSchema` in code
- Row 2 onward: one vehicle per row

There is no scoring sheet or config sheet. Export contains raw vehicle data only.

---

## Column order

Headers use dot notation (e.g. `pricing.priceZar`, `engine.powerKw`).

The first columns are:

| Column | Notes |
|--------|-------|
| `id` | Vehicle UUID; optional for new vehicles (auto-generated if blank) |
| `make` | Required |
| `model` | Required |
| `derivative` | |
| `pricing.priceZar` | Price in ZAR |
| `pricing.priceDate` | ISO-8601 date (`YYYY-MM-DD`) |

Remaining specification columns follow in schema order.

Included fields: all raw vehicle specification fields from the data dictionary, excluding:

- `derivedMetrics.*` (calculated by the app), **except** manually maintained scores listed below
- `source.importedDate` (stamped on save)

**Manual score columns** (after `ownership.localProduction`, before `source.sourceType`):

| Column | Import | Export |
|--------|--------|--------|
| `manualScoreOverrides.reliabilityManualEstimate` | Round-trips | Manual estimate only; empty if unset |
| `derivedMetrics.reliabilityHeuristic` | Ignored | Computed heuristic |
| `derivedMetrics.reliabilityScore` | Ignored | Final blended reliability score |
| `manualScoreOverrides.prestigeScore` | Round-trips | Manual prestige override only; empty if unset |
| `derivedMetrics.prestigeScore` | Ignored | Final prestige score |

Import reads manual estimate/override columns into `vehicle.manualScoreOverrides`. Derived metric columns are export-only and are recalculated on save.

Empty cells represent null/missing values.

---

## Cell formats

| Type | Export format | Import parsing |
|------|---------------|----------------|
| UUID | lowercase string | optional for new rows; auto-generated if blank |
| String | plain text | trimmed |
| Integer | decimal digits | `Integer` |
| Double | decimal number | `Double` |
| Boolean | `TRUE` or `FALSE` | case-insensitive |
| Enum | enum name (e.g. `SUV`) | exact enum name |
| Date | ISO-8601 (`YYYY-MM-DD`) | `LocalDate` |

Fields containing commas, quotes, or line breaks are quoted using standard CSV escaping (`"` doubled inside quoted fields).

---

## Vehicle identity and create vs update

Driver treats **make + model + derivative** as the natural key for a vehicle record (derivative may be empty). This matches JSON import behaviour.

| Scenario | Behaviour |
|----------|-----------|
| Identity matches an existing vehicle | Updates that record; CSV `id` is ignored |
| No identity match, blank `id` | Creates a new vehicle with a generated UUID |
| No identity match, valid unused `id` | Creates a new vehicle with that UUID |
| No identity match, `id` already in use by another vehicle | Error |
| Partial row on update | Only non-empty cells are merged; other stored fields are kept |
| `status` on update | Existing workflow status is preserved |
| `status` on create | Uses CSV value if present; defaults to `CANDIDATE` if blank |
| Duplicate identities within the same file | Error |

---

## Import rules

1. Header row must match the expected schema exactly (name and order).
2. Each data row must include `make` and `model`.
3. Empty cells are ignored on merge (existing values preserved on update).
4. Validation errors are reported per row before any changes are saved.

---

## Typical workflow

### Update existing vehicles

1. Export vehicles from **File → Export CSV…**
2. Edit values in any spreadsheet editor or text tool
3. Import via **File → Import CSV…**
4. Preview shows changed field counts per vehicle; confirm to merge and recalculate scores

### Add new vehicles

1. Start from an exported CSV (for correct headers) or copy the header row from an export
2. Add rows with `make` and `model` filled in; leave `id` blank unless you want a specific UUID
3. Fill in any known specification fields; leave others blank
4. Import via **File → Import CSV…** — preview shows new vs update counts
