# Driver
## Backlog

![Driver logo](logo.png)

Outstanding features and polish items that were planned but not implemented. The app is feature-complete for day-to-day use; this list is for future work if needed.

**Specifications (source of truth):**

- [design_spec.md](design_spec.md) — functional requirements, architecture, derived metrics
- [data_dictionary.md](data_dictionary.md) — entity fields, enums, import schema

Some items below are still described in `design_spec.md` but were deliberately deferred.

---

## Scoring profiles

- Additional seed profiles: Budget Focused, Executive, Daily Commuter (Family Focused is seeded today)
- Historical score tracking across profile changes
- Per-profile shortlists

---

## Test drives and elimination

Vehicle-level freeform **Notes** (detail tab) replaces most of this scope.

- Test drive panel or dialog per vehicle (`TestDrive` entity exists; no UI)
  - drive date, comfort/visibility/handling/spouse ratings, overall impression (1–10), notes
- Multiple test drives per vehicle
- Elimination dialog — record reason and date; set status to ELIMINATED
- Vehicle list toggle to show/hide eliminated vehicles (hidden by default)
- Integrate test-drive impressions into derived scoring (explicitly out of scope originally)

---

## Comparison and shortlisting

- Side-by-side comparison view (`ComparePanel`) — raw specs and derived metrics
- Favourite / shortlist flag (distinct from status workflow)
- Excel-style per-column filters on the vehicle table (FilterBar covers main filters today)

---

## Import and export

- LLM extraction or brochure parsing (JSON/CSV import exists)
- Export filtered list to PDF or print layout
- Saved filter presets

---

## Polish and packaging

- Sample seed vehicles bundled with the repo (1–2 examples in `data/vehicles/`)
- Comprehensive input validation on all detail form fields
- `jpackage` script for native OS installers (fat JAR is supported via `mvn package`)

---

## Permanent non-goals

Do not add these to the backlog; they are excluded by [design_spec.md](design_spec.md):

- Vehicle financing calculations
- Dealer inventory management
- Maintenance tracking or trade-in valuation
- ML-based recommendations
- Auto-update, cloud sync, market price tracking
