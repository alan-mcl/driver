# Driver
## Design Specification

![Driver logo](logo.png)

### Overview

Driver is a personal decision-support application for researching, comparing, scoring, and shortlisting vehicles for purchase.

The system is designed to support a long-term buying horizon where:

- Candidate vehicles may change over time.
- Evaluation criteria may evolve.
- Market conditions may change.
- The user may periodically import new vehicle data.
- The "keep current vehicle" option remains a valid competitor.

The system is intentionally designed around storing raw vehicle data and deriving scores from configurable scoring models.

---

## Goals

### Primary Goals

- Maintain a structured database of candidate vehicles.
- Support comparison across multiple vehicle classes.
- Calculate consistent summary metrics.
- Generate ranked shortlists.
- Support future automation of data ingestion.

### Secondary Goals

- Track test-drive impressions.
- Track elimination decisions.
- Support multiple scoring profiles.
- Support market monitoring.

### Non-Goals

- Vehicle financing calculations.
- Dealer inventory management.
- Vehicle maintenance tracking.
- Trade-in valuation.
- Vehicle recommendation using machine learning.

---

## System Architecture

```text
Vehicle Datasheet
        |
        V
LLM Extraction
        |
        V
Vehicle JSON
        |
        V
Import Engine
        |
        V
Vehicle Database
        |
        +--> Comparison Views
        |
        +--> Derived Metrics
        |
        +--> Scoring Profiles
        |
        +--> Shortlists
        |
        +--> Test Drive Tracking
```

---

## Core Principles

### Raw Data First

The system stores factual vehicle data.

Derived scores are calculated and never manually edited.

### Scoring Is Configurable

Scoring models must be configurable without requiring re-import of vehicle data.

### Source Preservation

Every imported vehicle record should retain provenance information.

### Human Decisions Remain Important

The system supports decision-making.

It does not replace test drives and subjective judgement.

---

## Functional Requirements

### Vehicle Management

The system shall:

- Create vehicles
- Edit vehicles
- Archive vehicles
- Delete vehicles
- Import vehicles from JSON
- Store free-form notes per vehicle

#### Vehicle Status

Vehicles may be:

- Candidate
- Shortlisted
- Test Driven
- Eliminated
- Purchased
- Archived

---

### Data Import

The system shall support importing structured JSON.

Imported data shall:

- Validate against schema
- Record import date
- Record source information
- Preserve original values

Import must support partial records.

Missing values are permitted.

---

### Vehicle Comparison

The system shall support:

- Side-by-side comparison
- Raw specification comparison
- Derived metric comparison
- Filtering
- Sorting

---

### Scoring Engine

The system shall generate derived metrics automatically.

Metrics shall be recalculated whenever:

- Vehicle data changes
- Scoring profiles change

---

### Shortlisting

Users shall be able to:

- Filter vehicles
- Rank vehicles
- Mark favourites
- Maintain a shortlist

---

### Test Drive Tracking

Users shall be able to record:

- Test drive date
- Subjective impressions
- Comfort rating
- Visibility rating
- Handling rating
- Spouse approval rating
- Free-form notes

---

## Derived Metrics

The following headline metrics will be calculated.

Each metric is represented internally as a score between 0 and 100.

**Implementation detail:** formulae, sub-metric weights, scale constants, profile weighting, and worked examples are documented in [scoring_spec.md](scoring_spec.md).

Displayed values may be rendered as stars.

### Safety

Measures occupant protection and active safety systems.

Potential inputs:

- NCAP rating
- Airbags
- AEB
- Lane Assist
- Blind Spot Monitoring
- Stability Control

### Running Cost

Measures expected ownership cost over several years of ownership.

Inputs:

- Fuel consumption (`economy.fuelConsumptionCombined`)
- Warranty coverage (`ownership.warrantyYears`, `ownership.warrantyKm`)
- Service plan coverage (`ownership.servicePlanYears`, `ownership.servicePlanKm`)
- Maintenance plan coverage (`ownership.maintenancePlanYears`, `ownership.maintenancePlanKm`)
- Parts support (`ownership.partsSupportScore`, manually maintained)
- Local production (`ownership.localProduction`, factual; informs parts support scoring)
- Tyre cost (derived from `wheels.tyreSize`)

Not included: purchase price (captured separately via Score/R100k), service interval (factual data only).

### Reliability

Measures expected dependability, repairability, and ease of ownership over 5–10 years (reputation-based, not failure-rate prediction).

Computed from:

- Brand reliability lookup (`config/brand-reliability.json` via `vehicle.make`)
- Powertrain heuristics (`engine.*`, `transmission.type`)
- Parts support (`ownership.partsSupportScore`, manually maintained)

Confidence (`derivedMetrics.reliabilityConfidence`) is displayed separately and does not affect the score.

Optional manual estimate via `manualScoreOverrides.reliabilityManualEstimate`, blended 50/50 with the computed heuristic.

### Comfort

Measures passenger comfort and refinement.

Potential inputs:

- Ride quality
- Noise insulation
- Climate control
- Seat quality

May initially be partially manual.

### Performance

Measures drivability and acceleration.

Inputs:

- 0–100 km/h time (`performance.zeroToHundredSeconds`, or estimated from power-to-weight)
- Power-to-weight and torque-to-weight (derived from engine and kerb weight)
- Transmission type (`transmission.type`)

### Daily Driver

Measures suitability for commuting and school-run duties.

Potential inputs:

- Turning circle
- Fuel consumption
- Dimensions
- Visibility
- Parking aids

### Technology

Measures onboard technology.

Potential inputs:

- Android Auto
- Apple CarPlay
- Reverse Camera
- Adaptive Cruise Control
- Digital Cluster

### Prestige

Measures brand and perceived quality.

Initially maintained manually.

Potential inputs:

- Brand score
- Interior quality score
- Design score

---

## Personal Fit Metrics

Metrics that depend on user-specific constraints rather than vehicle specs alone. These are calculated at display/filter time and are **not** scoring metrics — they are not stored on `Vehicle` and do not affect derived scores.

### Garage Clearance

Estimates whether a vehicle fits through an arched garage entry.

**Configuration** (Config → Garage Dimensions…, persisted in `data/garage-config.json`):

| Field | Meaning |
|-------|---------|
| `garageWidthMm` | Opening width at and below the spring line |
| `arcRadiusMm` | Radius of the semicircular arch above the spring line |
| `arcStartHeightMm` | Height above ground where the arch curve begins |

**Vehicle inputs:** `dimensions.widthMm`, `dimensions.heightMm` (body width, overall height).

**Formula:** At the vehicle's overall height, compute the opening width. Clearance is the total lateral gap assuming centred entry:

```text
clearanceMm = openingWidth(heightMm) - widthMm
```

Opening width below the spring line equals `garageWidthMm`. Above the spring line it follows the semicircle: `2 × sqrt(R² − (height − arcStart)²)`.

Negative clearance means the body is wider than the opening at that height. Missing width or height yields no value.

**Display:** "Garage Clearance" column on the vehicle list table (millimetres).

**Filter:** Minimum clearance (mm) on the filter bar; vehicles with unknown clearance are excluded when the filter is active.

---

## Scoring Profiles

Users may define multiple scoring profiles. Each profile selects four top base metrics (from the eight available), assigns profile-level weights totalling 100, and defines a fifth aggregate metric (custom name, profile weight, and composition from the remaining four base metrics).

**Implemented:**

- Toolbar profile selector — switch active profile; scores reload live
- **Config → Manage Profiles…** — create, duplicate, edit, and delete profiles
- Active profile persisted in `data/app-config.json`
- Family Focused profile seeded on first run

**Examples** (not seeded; users create their own via Manage Profiles):

- Budget Focused
- Executive
- Daily Commuter

Overall score:

```text
sum(metricScore × profileWeight) / sum(profileWeight)
```

Renormalizes over metrics with non-null scores. See [scoring_spec.md](scoring_spec.md) for formulae.

---

## Filtering

Supported filters include:

- Price
- Body Type
- Fuel Type
- Safety Score
- Running Cost Score
- Reliability Score
- Total Score
- Garage Clearance (minimum mm)

### Scatter Plot

View → Scatter Plot opens a modeless chart of the **currently filtered** vehicle list. Users choose X and Y axes from price, overall score, key metric scores, and Score/R100k. Points are labelled by vehicle and coloured by make; clicking a point selects that vehicle in the list. Display-only — does not affect scoring.

---

## Elimination Tracking

Vehicles may be marked as eliminated.

The system shall record:

- Elimination date
- Elimination reason

Examples:

- Too expensive
- Poor safety
- Too similar to existing SUV
- Poor test drive experience

---

## Future Enhancements

Potential future capabilities:

- Market price tracking
- Dealer listing imports
- Reliability database integration
- Automatic brochure extraction
- Automated watchlists
- Historical score tracking
- Notification system

---

## Technology Constraints

The design should remain simple.

Preferred characteristics:

- Local-first
- JSON persistence
- Minimal dependencies
- Human-readable data
- Easy backup
- Easy export

The application should function without requiring cloud services.

---

## Success Criteria

The system is successful if it enables:

1. Maintenance of a vehicle candidate pool.
2. Rapid comparison of candidates.
3. Consistent scoring.
4. Identification of a test-drive shortlist.
5. Easy addition of new vehicles over time.

---

## Branding

Branding assets live in `docs/` and are bundled into the application JAR at build time.

| Asset | File | Usage |
|-------|------|-------|
| Logo | [logo.png](logo.png) | About dialog, documentation headers |
| Icon | [icon.png](icon.png) | Window and taskbar icon |

The wide logo includes the application wordmark; the shield icon is used where a compact square mark is required.
