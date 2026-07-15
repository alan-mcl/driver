# Driver — JSON Import Format Specification

Specification for producing vehicle import JSON from brochures, websites, datasheets, or other source material. Intended for **LLM extraction tasks** and human-authored imports into the [Driver](design_spec.md) application.

**Related documents:**

- [data_dictionary.md](data_dictionary.md) — canonical field definitions
- Import implementation: `za.driver.import_.ImportService`

---

## Output requirements

The LLM must output **a single JSON object** and nothing else:

- No markdown fences, commentary, or explanatory text
- No trailing commas
- Use `null` omission: **omit** fields that are unknown (do not use `null` unless explicitly marking unknown in a quality flag)
- Enum values must match exactly (case-sensitive, underscore-separated)
- Dates as ISO-8601 strings: `"2026-06-17"`
- UUIDs as standard lowercase strings: `"550e8400-e29b-41d4-a716-446655440000"`

---

## Root structure

Use **either** a single vehicle or a batch of vehicles.

### Single vehicle (backwards compatible)

```json
{
  "schemaVersion": 1,
  "vehicle": { },
  "dataQuality": { }
}
```

### Multiple vehicles

```json
{
  "schemaVersion": 1,
  "vehicles": [
    { "id": "...", "make": "Toyota", "model": "Corolla", "status": "CANDIDATE" },
    {
      "vehicle": { "id": "...", "make": "Honda", "model": "Civic", "status": "CANDIDATE" },
      "dataQuality": { "pricing.listPrice": "VERIFIED" }
    }
  ]
}
```

Each item in `vehicles` may be either:

- A **vehicle object** directly (fields at the top level of the array item), or
- A **wrapped entry** with `vehicle` and optional `dataQuality`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `schemaVersion` | integer | Yes | Must be `1` |
| `vehicle` | object | Yes* | Single-vehicle import only |
| `dataQuality` | object | No | Single-vehicle import only |
| `vehicles` | array | Yes* | Multi-vehicle import only |

\* Provide `vehicle` **or** a non-empty `vehicles` array — not both required, but at least one vehicle must be present.

Only `schemaVersion` **1** is accepted by the import engine.

---

## Validation rules

The import is **rejected** if any of these fail:

| Rule | Error |
|------|-------|
| `schemaVersion` is not `1` | Unsupported schema version |
| No `vehicle` and no `vehicles` | At least one vehicle is required |
| `vehicles` is empty | At least one vehicle is required |
| Any vehicle missing `id` | Vehicle id is required |
| Any vehicle missing `make` | Vehicle make is required |
| Any vehicle missing `model` | Vehicle model is required |
| Any vehicle missing `status` | Vehicle status is required |
| Duplicate make+model+derivative in batch | Duplicate identity error |
| Invalid enum string | JSON parse error |

All other fields are optional. **Partial records are valid** — include only fields you can extract from the source.

---

## Vehicle identity and updates

Driver treats **make + model + derivative** as the natural key for a vehicle record (derivative may be empty).

| Scenario | Behaviour |
|----------|-----------|
| No matching record | Creates a new vehicle using the imported `id` |
| Matching record exists | Updates the existing record; imported `id` is ignored |
| Partial import on update | Only fields present in the JSON are merged; other stored fields are kept |
| Status on update | Existing workflow status is preserved (not overwritten by import) |
| `importedDate` on update | Refreshed to the current date-time |

Matching is case-insensitive and ignores leading/trailing whitespace. Include `derivative` in extractions whenever the source distinguishes trim levels.

The import preview shows **Ready to import** or **Ready to update** accordingly.

---

## Vehicle object

### Required fields

| Field | Type | Notes |
|-------|------|-------|
| `id` | UUID string | Generate a new UUID v4 for each new vehicle |
| `make` | string | Manufacturer, e.g. `"Toyota"` |
| `model` | string | Model name, e.g. `"Corolla"` |
| `status` | enum | Use `"CANDIDATE"` for newly extracted vehicles |

### Optional top-level fields

| Field | Type | Notes |
|-------|------|-------|
| `derivative` | string | Trim/variant, e.g. `"1.8 XS CVT"` |
| `modelYear` | integer | Model year, e.g. `2024` |
| `bodyType` | enum | See [Body type](#body-type) |

### Nested groups

Omit entire nested objects if no fields within them are known.

| Group | JSON key | Fields |
|-------|----------|--------|
| Engine | `engine` | See [Engine](#engine) |
| Transmission | `transmission` | See [Transmission](#transmission) |
| Performance | `performance` | See [Performance](#performance) |
| Dimensions | `dimensions` | See [Dimensions](#dimensions) |
| Towing | `towing` | See [Towing](#towing) |
| Wheels | `wheels` | See [Wheels](#wheels) |
| Infotainment | `infotainment` | See [Infotainment](#infotainment) |
| Economy | `economy` | See [Economy](#economy) |
| Safety | `safety` | See [Safety](#safety) |
| Features | `features` | See [Features](#features) |
| Ownership | `ownership` | See [Ownership](#ownership) |
| Pricing | `pricing` | See [Pricing](#pricing) |
| Source | `source` | See [Source provenance](#source-provenance) |

### Do not include

| Field | Reason |
|-------|--------|
| `derivedMetrics` | Calculated automatically by Driver on import; any values are discarded |
| `dataQuality` on vehicle | Use root-level `dataQuality` map instead |

---

## Engine

```json
"engine": {
  "fuelType": "PETROL",
  "displacementCc": 1798,
  "cylinders": 4,
  "powerKw": 103.0,
  "torqueNm": 173.0,
  "aspiration": "NATURALLY_ASPIRATED",
  "hybrid": false,
  "phev": false
}
```

| Field | Type | Unit / format |
|-------|------|---------------|
| `fuelType` | enum | See [Fuel type](#fuel-type) |
| `displacementCc` | integer | Cubic centimetres |
| `cylinders` | integer | Count |
| `powerKw` | number | Kilowatts (convert from PS/hp if needed) |
| `torqueNm` | number | Newton-metres |
| `aspiration` | enum | See [Aspiration](#aspiration) |
| `hybrid` | boolean | Non-plug-in hybrid |
| `phev` | boolean | Plug-in hybrid |

**LLM guidance:** Convert power to kW (`1 PS ≈ 0.7355 kW`, `1 hp ≈ 0.7457 kW`). If only one power figure is given, populate `powerKw` and flag as `ESTIMATED` in `dataQuality`.

---

## Transmission

```json
"transmission": {
  "type": "CVT",
  "gears": 7,
  "drivetrain": "FWD"
}
```

| Field | Type | Values |
|-------|------|--------|
| `type` | enum | `MANUAL`, `IMT`, `AMT`, `AUTOMATIC`, `CVT`, `SEMI_AUTOMATIC`, `DCT`, `SEQUENTIAL`, `TIPTRONIC` |
| `gears` | integer | Gear count (omit for CVT if not stated) |
| `drivetrain` | enum | `FWD`, `RWD`, `AWD`, `FOUR_WD` |

---

## Performance

```json
"performance": {
  "zeroToHundredSeconds": 8.4,
  "topSpeedKmh": 210
}
```

| Field | Type | Unit |
|-------|------|------|
| `zeroToHundredSeconds` | number | seconds (0–100 km/h) |
| `topSpeedKmh` | integer | km/h |

**LLM guidance:** Convert manufacturer times to seconds. Omit if not stated in source.

---

## Dimensions

All distances in **millimetres** unless noted.

```json
"dimensions": {
  "lengthMm": 4630,
  "widthMm": 1780,
  "heightMm": 1435,
  "wheelbaseMm": 2700,
  "groundClearanceMm": 140,
  "turningCircleM": 10.8,
  "bootLitres": 470,
  "kerbWeightKg": 1300,
  "seats": 5
}
```

| Field | Type | Unit |
|-------|------|------|
| `lengthMm`, `widthMm`, `heightMm`, `wheelbaseMm`, `groundClearanceMm` | integer | mm |
| `turningCircleM` | number | metres (diameter or curb-to-curb — note in `dataQuality` if ambiguous) |
| `bootLitres` | integer | litres |
| `kerbWeightKg` | integer | kg |
| `seats` | integer | count |

**LLM guidance:** Convert cm/m to mm/kg as needed. Do not mix units in output.

---

## Towing

```json
"towing": {
  "towingBrakedKg": 1500
}
```

| Field | Type | Unit |
|-------|------|------|
| `towingBrakedKg` | integer | kg (braked trailer capacity) |

---

## Wheels

```json
"wheels": {
  "tyreSize": "215/55 R17"
}
```

| Field | Type | Notes |
|-------|------|-------|
| `tyreSize` | string | As stated in source, e.g. `"215/55 R17"` |

---

## Infotainment

```json
"infotainment": {
  "infotainmentScreenSizeInches": 10.25,
  "speakerCount": 8
}
```

| Field | Type | Unit |
|-------|------|------|
| `infotainmentScreenSizeInches` | number | inches (diagonal screen size) |
| `speakerCount` | integer | count |

---

## Economy

```json
"economy": {
  "fuelConsumptionCombined": 6.5,
  "fuelTankLitres": 50.0,
  "co2Gkm": 152.0
}
```

| Field | Type | Unit |
|-------|------|------|
| `fuelConsumptionCombined` | number | L/100 km |
| `fuelTankLitres` | number | litres |
| `co2Gkm` | number | g/km |

For EVs, omit `fuelConsumptionCombined` if not applicable; do not invent values.

---

## Safety

```json
"safety": {
  "ncapStars": 5,
  "airbags": 7,
  "abs": true,
  "esp": true,
  "tractionControl": true,
  "aeb": true,
  "laneAssist": true,
  "blindSpotMonitoring": false,
  "adaptiveCruiseControl": true,
  "rearCrossTrafficAlert": true
}
```

| Field | Type | Notes |
|-------|------|-------|
| `ncapStars` | integer | 0–5 Euro NCAP or equivalent |
| `airbags` | integer | Total count if stated |
| All others | boolean | `true` if confirmed present, `false` if confirmed absent, omit if unknown |

Map manufacturer feature names to booleans (e.g. "Toyota Safety Sense" may imply `aeb` + `laneAssist` — flag as `ESTIMATED`).

---

## Features

```json
"features": {
  "androidAuto": true,
  "appleCarplay": true,
  "reverseCamera": true,
  "parkingSensorsFront": false,
  "parkingSensorsRear": true,
  "digitalCluster": false,
  "keylessEntry": true,
  "pushButtonStart": true,
  "wirelessCharging": false,
  "climateControl": true,
  "climateControlType": "DUAL_ZONE_AUTO",
  "heatedSeats": false,
  "electricSeats": false,
  "sunroof": true,
  "premiumAudio": false
}
```

| Field | Type | Notes |
|-------|------|-------|
| Boolean fields | boolean | `true` if confirmed present, `false` if confirmed absent, omit if unknown |
| `climateControlType` | enum | `MANUAL`, `SINGLE_ZONE_AUTO`, `DUAL_ZONE_AUTO`, `TRI_ZONE_AUTO` |

**Brochure aliases:** use `safety.laneAssist` for lane keeping assist, `features.reverseCamera` for parking camera, `features.parkingSensorsFront` / `features.parkingSensorsRear` for parking sensors, `features.appleCarplay` for Apple CarPlay.

All boolean fields: only set `false` when the source explicitly states absence; otherwise omit unknown fields.

---

## Ownership

```json
"ownership": {
  "warrantyYears": 3,
  "warrantyKm": 100000,
  "servicePlanYears": 3,
  "servicePlanKm": 100000,
  "serviceIntervalKm": 15000,
  "maintenancePlanYears": 5,
  "maintenancePlanKm": 90000
}
```

Distances in **kilometres**, durations in **years**.

---

## Pricing

South African Rand pricing values stored as plain numbers (no currency unit field).

```json
"pricing": {
  "listPrice": 350000,
  "dealerOffer": 335000,
  "listPriceDate": "2026-06-17",
  "dealerOfferDate": "2026-06-20"
}
```

| Field | Type | Notes |
|-------|------|-------|
| `listPrice` | number | Integer list/asking price, no cents required |
| `dealerOffer` | number | Optional quoted or negotiated price |
| `listPriceDate` | string | ISO date when list price was observed |
| `dealerOfferDate` | string | Optional ISO date when dealer offer was quoted |

**Legacy import (accepted on read):** `listPriceZar`, `priceZar` → `listPrice`; `dealerOfferZar` → `dealerOffer`; `priceDate` → `listPriceDate`. New exports use `listPrice` and `listPriceDate` only.

**LLM guidance:** Strip currency symbols and thousands separators. Use the published list/asking price for `listPrice`, not monthly payment figures. Use `dealerOffer` only when a specific dealer quote or negotiated price is known.

---

## Source provenance

Populate when extracting from a known document or URL. `importedDate` is set automatically by Driver if omitted.

```json
"source": {
  "sourceType": "BROCHURE",
  "sourceName": "Toyota Corolla brochure June 2024",
  "sourceUrl": "https://www.example.com/brochure.pdf"
}
```

| Field | Type | Values |
|-------|------|--------|
| `sourceType` | enum | `BROCHURE`, `WEBSITE`, `MANUAL_ENTRY`, `DEALER`, `OTHER` |
| `sourceName` | string | Human-readable source label |
| `sourceUrl` | string | URL or file reference if available |
| `importedDate` | string | ISO date-time; optional (app stamps on save) |

For LLM extraction from a brochure PDF, use `sourceType: "BROCHURE"`. From a web page, use `"WEBSITE"`.

---

## Data quality flags

Optional root-level map recording confidence per field. Keys use **dot notation** matching the vehicle JSON path.

```json
"dataQuality": {
  "pricing.listPrice": "VERIFIED",
  "pricing.dealerOffer": "VERIFIED",
  "engine.powerKw": "ESTIMATED",
  "safety.blindSpotMonitoring": "MISSING"
}
```

| Value | Meaning |
|-------|---------|
| `VERIFIED` | Stated explicitly in source |
| `ESTIMATED` | Inferred, converted, or approximated |
| `MISSING` | Sought but not found in source (field omitted from `vehicle`) |

**LLM guidance:**

- Flag every field you **convert** (units, kW from hp, etc.) as `ESTIMATED`
- Flag every field you **infer** from marketing names as `ESTIMATED`
- Do not add `dataQuality` entries for fields you simply omit without attempting extraction

---

## Enum reference

### Body type

`HATCHBACK`, `SEDAN`, `WAGON`, `CROSSOVER`, `SUV`, `MPV`, `COUPE`, `CONVERTIBLE`, `BAKKIE`, `OTHER`

### Vehicle status

`CANDIDATE`, `SHORTLISTED`, `TEST_DRIVEN`, `ELIMINATED`, `PURCHASED`, `ARCHIVED`

**New extractions:** always use `CANDIDATE`.

### Fuel type

`PETROL`, `DIESEL`, `HYBRID`, `PHEV`, `EV`

### Aspiration

`NATURALLY_ASPIRATED`, `TURBOCHARGED`, `SUPERCHARGED`

### Transmission type

`MANUAL`, `IMT`, `AMT`, `AUTOMATIC`, `CVT`, `SEMI_AUTOMATIC`, `DCT`, `SEQUENTIAL`, `TIPTRONIC`

### Drivetrain type

`FWD`, `RWD`, `AWD`, `FOUR_WD`

### Climate control type

`MANUAL`, `SINGLE_ZONE_AUTO`, `DUAL_ZONE_AUTO`, `TRI_ZONE_AUTO`

### Source type

`BROCHURE`, `WEBSITE`, `MANUAL_ENTRY`, `DEALER`, `OTHER`

---

## Examples

### Minimal valid import

```json
{
  "schemaVersion": 1,
  "vehicle": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "make": "Toyota",
    "model": "Corolla",
    "status": "CANDIDATE"
  }
}
```

### Full extraction example

```json
{
  "schemaVersion": 1,
  "vehicle": {
    "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "make": "Toyota",
    "model": "Corolla",
    "derivative": "1.8 XS",
    "modelYear": 2024,
    "bodyType": "SEDAN",
    "status": "CANDIDATE",
    "engine": {
      "fuelType": "PETROL",
      "displacementCc": 1798,
      "cylinders": 4,
      "powerKw": 103.0,
      "torqueNm": 173.0,
      "aspiration": "NATURALLY_ASPIRATED",
      "hybrid": false,
      "phev": false
    },
    "transmission": {
      "type": "CVT",
      "gears": 7,
      "drivetrain": "FWD"
    },
    "performance": {
      "zeroToHundredSeconds": 10.2,
      "topSpeedKmh": 190
    },
    "dimensions": {
      "lengthMm": 4630,
      "widthMm": 1780,
      "heightMm": 1435,
      "wheelbaseMm": 2700,
      "turningCircleM": 10.8,
      "bootLitres": 470,
      "kerbWeightKg": 1300,
      "seats": 5
    },
    "towing": {
      "towingBrakedKg": 750
    },
    "wheels": {
      "tyreSize": "205/55 R16"
    },
    "infotainment": {
      "infotainmentScreenSizeInches": 8.0,
      "speakerCount": 6
    },
    "economy": {
      "fuelConsumptionCombined": 6.5,
      "fuelTankLitres": 50.0,
      "co2Gkm": 152.0
    },
    "safety": {
      "ncapStars": 5,
      "airbags": 7,
      "abs": true,
      "esp": true,
      "tractionControl": true,
      "aeb": true,
      "laneAssist": true,
      "adaptiveCruiseControl": true,
      "rearCrossTrafficAlert": true
    },
    "features": {
      "androidAuto": true,
      "appleCarplay": true,
      "reverseCamera": true,
      "parkingSensorsRear": true,
      "climateControl": true,
      "climateControlType": "SINGLE_ZONE_AUTO",
      "sunroof": false,
      "keylessEntry": true,
      "pushButtonStart": true
    },
    "ownership": {
      "warrantyYears": 3,
      "warrantyKm": 100000,
      "serviceIntervalKm": 15000,
      "maintenancePlanYears": 5,
      "maintenancePlanKm": 90000
    },
    "pricing": {
      "listPrice": 389900,
      "listPriceDate": "2026-06-17"
    },
    "source": {
      "sourceType": "WEBSITE",
      "sourceName": "Toyota South Africa",
      "sourceUrl": "https://www.toyota.co.za/ranges/corolla"
    }
  },
  "dataQuality": {
    "engine.powerKw": "VERIFIED",
    "safety.blindSpotMonitoring": "MISSING",
    "pricing.listPrice": "VERIFIED"
  }
}
```

### Batch import example

```json
{
  "schemaVersion": 1,
  "vehicles": [
    {
      "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
      "make": "Toyota",
      "model": "Corolla",
      "derivative": "1.8 XS",
      "status": "CANDIDATE",
      "pricing": { "listPrice": 389900, "listPriceDate": "2026-06-17" }
    },
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "make": "Honda",
      "model": "Civic",
      "derivative": "1.5T Sport",
      "status": "CANDIDATE",
      "pricing": { "listPrice": 429900, "listPriceDate": "2026-06-17" }
    }
  ]
}
```

---

## LLM extraction prompt template

Copy and adapt the following when running extraction:

```text
You are extracting structured vehicle data for the Driver application.

Read the source material and output a single JSON object conforming to
docs/import_format_spec.md (schemaVersion 1).

Rules:
- Output JSON only. No markdown, no explanation.
- Use a single vehicle object or a vehicles array for multiple variants from the same source.
- Generate a new UUID v4 for each vehicle.id.
- Set each vehicle.status to "CANDIDATE".
- Include only fields supported by the source; omit unknown fields.
- Do not include derivedMetrics (scores are calculated by the app).
- Use exact enum strings from the spec.
- Convert all measurements to the units specified (mm, kg, kW, L/100km, ZAR).
- Populate source with sourceType, sourceName, and sourceUrl where known.
- Add dataQuality flags for ESTIMATED or MISSING fields using dot-path keys.
- Use false for booleans only when explicitly absent; otherwise omit.

Source type: {BROCHURE|WEBSITE|DEALER}
Source name: {name}
Source URL: {url if applicable}

--- SOURCE MATERIAL ---
{paste brochure text, HTML, or extracted PDF content here}
--- END SOURCE ---
```

---

## Post-extraction workflow

1. In Driver: **File → Import JSON…**
2. Paste the JSON directly into the editor, or save as a `.json` file and use **Browse…** to load it
3. Click **Preview** to validate; fix errors if rejected
4. Click **Import** — scores are calculated and vehicle(s) are saved to `data/vehicles/`

---

## Version history

| Version | Date | Changes |
|---------|------|---------|
| 1 | 2026-06-17 | Initial spec matching import engine schemaVersion 1 |
