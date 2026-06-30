# Driver
## Data Dictionary

## Vehicle

Represents a specific vehicle derivative.

| Field | Type | Required |
|---------|---------|---------|
| id | UUID | Yes |
| make | String | Yes |
| model | String | Yes |
| derivative | String | No |
| modelYear | Integer | No |
| bodyType | Enum | No |
| status | Enum | Yes |
| notes | String | No |

---

## Vehicle Status

| Value |
|---------|
| CANDIDATE |
| SHORTLISTED |
| TEST_DRIVEN |
| ELIMINATED |
| PURCHASED |
| ARCHIVED |

---

## Body Type

| Value |
|---------|
| HATCHBACK |
| SEDAN |
| WAGON |
| CROSSOVER |
| SUV |
| MPV |
| COUPE |
| CONVERTIBLE |
| BAKKIE |
| OTHER |

---

## Engine

| Field | Type |
|---------|---------|
| fuelType | Enum |
| displacementCc | Integer |
| cylinders | Integer |
| powerKw | Double |
| torqueNm | Double |
| aspiration | Enum |
| hybrid | Boolean |
| phev | Boolean |

---

## Fuel Type

| Value |
|---------|
| PETROL |
| DIESEL |
| HYBRID |
| PHEV |
| EV |

---

## Aspiration

| Value |
|---------|
| NATURALLY_ASPIRATED |
| TURBOCHARGED |
| SUPERCHARGED |

---

## Transmission

| Field | Type |
|---------|---------|
| type | Enum |
| gears | Integer |
| drivetrain | Enum |

---

## Drivetrain Type

| Value |
|---------|
| FWD |
| RWD |
| AWD |
| FOUR_WD |

---

## Performance

| Field | Type |
|---------|---------|
| zeroToHundredSeconds | Double |
| topSpeedKmh | Integer |

---

## Transmission Type

| Value |
|---------|
| MANUAL |
| IMT |
| AMT |
| AUTOMATIC |
| CVT |
| SEMI_AUTOMATIC |
| DCT |
| SEQUENTIAL |
| TIPTRONIC |

---

## Dimensions

| Field | Type |
|---------|---------|
| lengthMm | Integer |
| widthMm | Integer |
| heightMm | Integer |
| wheelbaseMm | Integer |
| groundClearanceMm | Integer |
| turningCircleM | Double |
| bootLitres | Integer |
| kerbWeightKg | Integer |
| seats | Integer |

---

## Towing

| Field | Type |
|---------|---------|
| towingBrakedKg | Integer |

---

## Wheels

| Field | Type |
|---------|---------|
| tyreSize | String |

---

## Infotainment

| Field | Type |
|---------|---------|
| infotainmentScreenSizeInches | Double |
| speakerCount | Integer |

---

## Economy

| Field | Type |
|---------|---------|
| fuelConsumptionCombined | Double |
| fuelTankLitres | Double |
| co2Gkm | Double |

---

## Safety

| Field | Type |
|---------|---------|
| ncapStars | Integer |
| airbags | Integer |
| abs | Boolean |
| esp | Boolean |
| tractionControl | Boolean |
| aeb | Boolean |
| laneAssist | Boolean |
| blindSpotMonitoring | Boolean |
| adaptiveCruiseControl | Boolean |
| rearCrossTrafficAlert | Boolean |

---

## Features

| Field | Type |
|---------|---------|
| androidAuto | Boolean |
| appleCarplay | Boolean |
| reverseCamera | Boolean |
| parkingSensorsFront | Boolean |
| parkingSensorsRear | Boolean |
| digitalCluster | Boolean |
| keylessEntry | Boolean |
| pushButtonStart | Boolean |
| wirelessCharging | Boolean |
| climateControl | Boolean |
| climateControlType | Enum |
| heatedSeats | Boolean |
| electricSeats | Boolean |
| sunroof | Boolean |
| premiumAudio | Boolean |

---

## Climate Control Type

| Value |
|---------|
| MANUAL |
| SINGLE_ZONE_AUTO |
| DUAL_ZONE_AUTO |
| TRI_ZONE_AUTO |

---

## Ownership

| Field | Type |
|---------|---------|
| warrantyYears | Integer |
| warrantyKm | Integer |
| servicePlanYears | Integer |
| servicePlanKm | Integer |
| serviceIntervalKm | Integer |
| maintenancePlanYears | Integer |
| maintenancePlanKm | Integer |
| partsSupportScore | Integer (0–100) |
| localProduction | Boolean |

`partsSupportScore` is manually maintained and represents parts availability, dealer network strength, repair turnaround expectations, and long-term support confidence in the South African market.

`localProduction` is a factual flag for vehicles assembled or manufactured locally. It is not scored directly; it informs assignment of `partsSupportScore`.

---

## Pricing

| Field | Type |
|---------|---------|
| priceZar | Decimal |
| priceDate | Date |

---

## Source

Tracks provenance of imported data.

| Field | Type |
|---------|---------|
| sourceType | Enum |
| sourceName | String |
| sourceUrl | String |
| importedDate | DateTime |

---

## Source Type

| Value |
|---------|
| BROCHURE |
| WEBSITE |
| MANUAL_ENTRY |
| DEALER |
| OTHER |

---

## Manual Score Overrides

Optional per-vehicle manual inputs for metrics that can otherwise be computed or left unset.

| Field | Type |
|---------|---------|
| reliabilityManualEstimate | Double (0–100) |
| prestigeScore | Double (0–100) |

Stored on `vehicle.manualScoreOverrides`. `reliabilityManualEstimate` is blended 50/50 with the computed heuristic when both are set. `prestigeScore` replaces the computed prestige score (Prestige is manual-only).

Legacy JSON alias: `reliabilityScore` deserializes to `reliabilityManualEstimate`.

---

## Derived Metrics

Calculated by the scoring engine.

Headline metric scores are stored as 0-100. `scorePer100k` is unbounded.

| Field | Type |
|---------|---------|
| safetyScore | Double |
| runningCostScore | Double |
| reliabilityHeuristic | Double |
| reliabilityScore | Double |
| comfortScore | Double |
| performanceScore | Double |
| dailyDriverScore | Double |
| technologyScore | Double |
| prestigeScore | Double |
| awesomenessScore | Double |
| reliabilityConfidence | Integer (0–100) |
| overallScore | Double |
| scorePer100k | Double |

---

## Scoring Profile

Defines metric weightings. Multiple profiles may exist; one is active at a time (see App Config).

| Field | Type | Required |
|---------|---------|---------|
| id | UUID | Yes |
| name | String | Yes |
| weights | List of ScoringWeight | Yes |
| aggregateName | String | No (defaults to `"Awesomeness"` on migration) |
| aggregateComponents | List of ScoringWeight | No (defaults to legacy composition on migration) |

`weights` contains exactly five entries: four chosen **top** base metrics plus one aggregate slot (`AWESOMENESS`). Profile-level weights must total 100.

`aggregateComponents` contains exactly four base metrics **not** chosen as top metrics, with weights totalling 100. These define how the aggregate score is calculated.

The eight **base metrics** are: `SAFETY`, `RUNNING_COST`, `RELIABILITY`, `COMFORT`, `PERFORMANCE`, `DAILY_DRIVER`, `TECHNOLOGY`, `PRESTIGE`. Each profile partitions them into four top and four aggregate components. Any of the eight may be chosen as top metrics (including Technology and Prestige).

Profiles are stored as JSON in `data/profiles/{uuid}.json`. Managed via the toolbar profile selector and **Config → Manage Profiles…** (create, duplicate, edit, delete).

---

## App Config

Application-level settings persisted across restarts.

| Field | Type | Required |
|---------|---------|---------|
| activeProfileId | UUID | No |

Stored in `data/app-config.json`. When missing or pointing at a deleted profile, the first available scoring profile is used.

---

## Scoring Weight

| Field | Type |
|---------|---------|
| metric | Enum |
| weight | Double |

Weights should total 100.

---

## Metric

Base metrics (partitioned per profile into four top + four aggregate components): `SAFETY`, `RUNNING_COST`, `RELIABILITY`, `COMFORT`, `PERFORMANCE`, `DAILY_DRIVER`, `TECHNOLOGY`, `PRESTIGE`.

Aggregate slot (profile-level weight + custom display name): `AWESOMENESS`.

| Value |
|---------|
| SAFETY |
| RUNNING_COST |
| RELIABILITY |
| COMFORT |
| PERFORMANCE |
| DAILY_DRIVER |
| TECHNOLOGY |
| PRESTIGE |
| AWESOMENESS |

---

## Test Drive

| Field | Type |
|---------|---------|
| id | UUID |
| vehicleId | UUID |
| driveDate | Date |
| comfortRating | Integer |
| visibilityRating | Integer |
| handlingRating | Integer |
| spouseApprovalRating | Integer |
| overallImpression | Integer |
| notes | String |

Ratings are stored on a 1-10 scale.

---

## Elimination

| Field | Type |
|---------|---------|
| vehicleId | UUID |
| eliminatedDate | Date |
| reason | String |

---

## Data Quality

Used to track confidence in imported values.

| Value |
|---------|
| VERIFIED |
| ESTIMATED |
| MISSING |

Every imported field may optionally carry a data quality flag.

---

## Brochure Column Mapping

Each leaf field maps to a single spreadsheet column using dot notation (e.g. `performance.zeroToHundredSeconds`). Common brochure labels and their canonical model paths:

| Brochure / common label | Model path |
|-------------------------|------------|
| 0–100 km/h, 0–100 s | `performance.zeroToHundredSeconds` |
| Top speed | `performance.topSpeedKmh` |
| Drivetrain, FWD/RWD/AWD/4WD | `transmission.drivetrain` |
| Kerb weight | `dimensions.kerbWeightKg` |
| Ground clearance | `dimensions.groundClearanceMm` |
| Turning circle | `dimensions.turningCircleM` |
| Towing capacity (braked) | `towing.towingBrakedKg` |
| Tyre size | `wheels.tyreSize` |
| Screen size, infotainment display | `infotainment.infotainmentScreenSizeInches` |
| Speakers | `infotainment.speakerCount` |
| Climate control type | `features.climateControlType` |
| Lane keeping assist | `safety.laneAssist` |
| Parking camera, reverse camera | `features.reverseCamera` |
| Parking sensors (front) | `features.parkingSensorsFront` |
| Parking sensors (rear) | `features.parkingSensorsRear` |
| Wireless CarPlay / Apple CarPlay | `features.appleCarplay` |
| Sunroof | `features.sunroof` |
| Premium audio | `features.premiumAudio` |
| Maintenance plan years/km | `ownership.maintenancePlanYears`, `ownership.maintenancePlanKm` |
| Parts support score | `ownership.partsSupportScore` |
| Local production | `ownership.localProduction` |
| Reliability manual estimate | `manualScoreOverrides.reliabilityManualEstimate` |
| Reliability heuristic (export only) | `derivedMetrics.reliabilityHeuristic` |
| Reliability score (export only) | `derivedMetrics.reliabilityScore` |
| Prestige override | `manualScoreOverrides.prestigeScore` |
| Prestige score (export only) | `derivedMetrics.prestigeScore` |
| Prestige override | `derivedMetrics.prestigeScore` |

CSV export/import uses the same dot-path column headers. See [spreadsheet_format_spec.md](spreadsheet_format_spec.md).

---

## Import Schema

Root object:

```json
{
  "schemaVersion": 1,
  "vehicle": {}
}
```

Future schema versions must remain backwards compatible where possible.
