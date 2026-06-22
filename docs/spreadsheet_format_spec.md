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
| `id` | Vehicle UUID; required for import |
| `make` | |
| `model` | |
| `derivative` | |
| `pricing.priceZar` | Price in ZAR |
| `pricing.priceDate` | ISO-8601 date (`YYYY-MM-DD`) |

Remaining specification columns follow in schema order.

Included fields: all raw vehicle specification fields from the data dictionary, excluding:

- `derivedMetrics.*` (calculated by the app), **except** manually maintained scores listed below
- `source.importedDate` (stamped on save)

**Manual score exceptions** (after `ownership.localProduction`, before `source.sourceType`):

| Column | Notes |
|--------|-------|
| `derivedMetrics.reliabilityScore` | Manual override only; empty if no override set |
| `derivedMetrics.prestigeScore` | Manual override only; empty if no override set |

These columns set `vehicle.manualScoreOverrides` on import. They export override values only (not computed reliability scores). Empty cells preserve the existing override; computed reliability is recalculated on save.

Empty cells represent null/missing values.

---

## Cell formats

| Type | Export format | Import parsing |
|------|---------------|----------------|
| UUID | lowercase string | required for import rows |
| String | plain text | trimmed |
| Integer | decimal digits | `Integer` |
| Double | decimal number | `Double` |
| Boolean | `TRUE` or `FALSE` | case-insensitive |
| Enum | enum name (e.g. `SUV`) | exact enum name |
| Date | ISO-8601 (`YYYY-MM-DD`) | `LocalDate` |

Fields containing commas, quotes, or line breaks are quoted using standard CSV escaping (`"` doubled inside quoted fields).

---

## Import rules

1. Header row must match the expected schema exactly (name and order).
2. Each data row must include a valid `id` matching an existing vehicle in the app.
3. Empty cells are ignored on merge (existing values preserved).
4. `status` is not updated from CSV import.
5. Validation errors are reported per row before any changes are saved.

---

## Typical workflow

1. Export vehicles from **File → Export CSV…**
2. Edit values in any spreadsheet editor or text tool
3. Import updates via **File → Import CSV…**
4. Preview shows changed field counts per vehicle; confirm to merge and recalculate scores
