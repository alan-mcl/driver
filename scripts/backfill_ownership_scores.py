#!/usr/bin/env python3
"""Backfill ownership.partsSupportScore and ownership.localProduction for vehicle fleet."""

import json
from pathlib import Path

VEHICLES_DIR = Path(__file__).resolve().parent.parent / "data" / "vehicles"

MAKE_DEFAULTS = {
    "Toyota": (92, False),
    "Volkswagen": (90, False),
    "Suzuki": (78, False),
    "Mazda": (80, False),
    "Nissan": (80, False),
    "Hyundai": (78, False),
    "Honda": (75, False),
    "HONDA": (75, False),
    "BMW": (82, False),
    "Audi": (80, False),
    "Mercedes-Benz": (78, False),
    "MINI": (75, False),
    "Peugeot": (70, False),
    "Chery": (55, False),
    "HAVAL": (58, False),
    "MG": (50, False),
    "JAECOO": (45, False),
    "OMODA": (45, False),
    "Changan": (48, False),
}

MODEL_OVERRIDES = {
    ("Volkswagen", "Polo Vivo"): (95, True),
    ("Volkswagen", "Polo"): (82, False),
    ("Toyota", "Corolla Cross"): (92, True),
    ("Toyota", "Starlet"): (90, True),
    ("Toyota", "Starlet Cross"): (90, True),
    ("Toyota", "Corolla"): (88, False),
    ("Suzuki", "Fronx"): (78, False),
    ("Chery", "Tiggo 4 Cross"): (55, False),
    ("Chery", "Tiggo 4 Pro"): (55, False),
    ("Chery", "Tiggo 7"): (55, False),
    ("Chery", "Tiggo 7 Pro"): (55, False),
    ("MG", "HS"): (50, False),
    ("JAECOO", "J5"): (45, False),
}


def score_for(make: str, model: str) -> tuple[int, bool]:
    key = (make, model)
    if key in MODEL_OVERRIDES:
        return MODEL_OVERRIDES[key]
    return MAKE_DEFAULTS.get(make, (60, False))


def main() -> None:
    updated = 0
    for path in sorted(VEHICLES_DIR.glob("*.json")):
        with path.open(encoding="utf-8") as f:
            data = json.load(f)

        make = data.get("make", "")
        model = data.get("model", "")
        parts_score, local = score_for(make, model)

        ownership = data.get("ownership")
        if ownership is None:
            ownership = {}
            data["ownership"] = ownership
        ownership["partsSupportScore"] = parts_score
        ownership["localProduction"] = local

        with path.open("w", encoding="utf-8") as f:
            json.dump(data, f, indent=2)
            f.write("\n")
        updated += 1

    print(f"Updated {updated} vehicles")


if __name__ == "__main__":
    main()
