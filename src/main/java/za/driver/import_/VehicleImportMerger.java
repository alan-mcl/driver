package za.driver.import_;

import java.util.HashMap;
import java.util.Map;

import za.driver.model.DataQuality;
import za.driver.model.Dimensions;
import za.driver.model.Economy;
import za.driver.model.Engine;
import za.driver.model.Features;
import za.driver.model.Infotainment;
import za.driver.model.Ownership;
import za.driver.model.Performance;
import za.driver.model.Pricing;
import za.driver.model.Safety;
import za.driver.model.Source;
import za.driver.model.Towing;
import za.driver.model.Transmission;
import za.driver.model.Vehicle;
import za.driver.model.Wheels;

public final class VehicleImportMerger {

    private VehicleImportMerger() {
    }

    public static Vehicle merge(Vehicle existing, Vehicle imported) {
        if (imported.getMake() != null && !imported.getMake().isBlank()) {
            existing.setMake(imported.getMake().trim());
        }
        if (imported.getModel() != null && !imported.getModel().isBlank()) {
            existing.setModel(imported.getModel().trim());
        }
        if (imported.getDerivative() != null) {
            existing.setDerivative(imported.getDerivative().isBlank() ? null : imported.getDerivative().trim());
        }
        if (imported.getModelYear() != null) {
            existing.setModelYear(imported.getModelYear());
        }
        if (imported.getBodyType() != null) {
            existing.setBodyType(imported.getBodyType());
        }

        if (imported.getEngine() != null) {
            if (existing.getEngine() == null) {
                existing.setEngine(new Engine());
            }
            mergeEngine(existing.getEngine(), imported.getEngine());
        }
        if (imported.getTransmission() != null) {
            if (existing.getTransmission() == null) {
                existing.setTransmission(new Transmission());
            }
            mergeTransmission(existing.getTransmission(), imported.getTransmission());
        }
        if (imported.getPerformance() != null) {
            if (existing.getPerformance() == null) {
                existing.setPerformance(new Performance());
            }
            mergePerformance(existing.getPerformance(), imported.getPerformance());
        }
        if (imported.getDimensions() != null) {
            if (existing.getDimensions() == null) {
                existing.setDimensions(new Dimensions());
            }
            mergeDimensions(existing.getDimensions(), imported.getDimensions());
        }
        if (imported.getTowing() != null) {
            if (existing.getTowing() == null) {
                existing.setTowing(new Towing());
            }
            mergeTowing(existing.getTowing(), imported.getTowing());
        }
        if (imported.getWheels() != null) {
            if (existing.getWheels() == null) {
                existing.setWheels(new Wheels());
            }
            mergeWheels(existing.getWheels(), imported.getWheels());
        }
        if (imported.getInfotainment() != null) {
            if (existing.getInfotainment() == null) {
                existing.setInfotainment(new Infotainment());
            }
            mergeInfotainment(existing.getInfotainment(), imported.getInfotainment());
        }
        if (imported.getEconomy() != null) {
            if (existing.getEconomy() == null) {
                existing.setEconomy(new Economy());
            }
            mergeEconomy(existing.getEconomy(), imported.getEconomy());
        }
        if (imported.getSafety() != null) {
            if (existing.getSafety() == null) {
                existing.setSafety(new Safety());
            }
            mergeSafety(existing.getSafety(), imported.getSafety());
        }
        if (imported.getFeatures() != null) {
            if (existing.getFeatures() == null) {
                existing.setFeatures(new Features());
            }
            mergeFeatures(existing.getFeatures(), imported.getFeatures());
        }
        if (imported.getOwnership() != null) {
            if (existing.getOwnership() == null) {
                existing.setOwnership(new Ownership());
            }
            mergeOwnership(existing.getOwnership(), imported.getOwnership());
        }
        if (imported.getPricing() != null) {
            if (existing.getPricing() == null) {
                existing.setPricing(new Pricing());
            }
            mergePricing(existing.getPricing(), imported.getPricing());
        }
        if (imported.getSource() != null) {
            if (existing.getSource() == null) {
                existing.setSource(new Source());
            }
            mergeSource(existing.getSource(), imported.getSource());
        }

        return existing;
    }

    public static Map<String, DataQuality> mergeDataQuality(
            Map<String, DataQuality> existing,
            Map<String, DataQuality> imported) {
        if (imported == null || imported.isEmpty()) {
            return existing == null ? Map.of() : Map.copyOf(existing);
        }
        Map<String, DataQuality> merged = existing == null ? new HashMap<>() : new HashMap<>(existing);
        merged.putAll(imported);
        return merged;
    }

    public static void migrateLegacyPricingDataQuality(Vehicle vehicle) {
        if (vehicle == null || vehicle.getDataQuality() == null) {
            return;
        }
        Map<String, DataQuality> dataQuality = vehicle.getDataQuality();
        if (!dataQuality.containsKey("pricing.priceZar") || dataQuality.containsKey("pricing.listPriceZar")) {
            return;
        }
        Map<String, DataQuality> migrated = new HashMap<>(dataQuality);
        migrated.put("pricing.listPriceZar", migrated.remove("pricing.priceZar"));
        vehicle.setDataQuality(migrated);
    }

    private static void mergeEngine(Engine target, Engine source) {
        if (source.getFuelType() != null) {
            target.setFuelType(source.getFuelType());
        }
        if (source.getDisplacementCc() != null) {
            target.setDisplacementCc(source.getDisplacementCc());
        }
        if (source.getCylinders() != null) {
            target.setCylinders(source.getCylinders());
        }
        if (source.getPowerKw() != null) {
            target.setPowerKw(source.getPowerKw());
        }
        if (source.getTorqueNm() != null) {
            target.setTorqueNm(source.getTorqueNm());
        }
        if (source.getAspiration() != null) {
            target.setAspiration(source.getAspiration());
        }
        if (source.getHybrid() != null) {
            target.setHybrid(source.getHybrid());
        }
        if (source.getPhev() != null) {
            target.setPhev(source.getPhev());
        }
    }

    private static void mergeTransmission(Transmission target, Transmission source) {
        if (source.getType() != null) {
            target.setType(source.getType());
        }
        if (source.getGears() != null) {
            target.setGears(source.getGears());
        }
        if (source.getDrivetrain() != null) {
            target.setDrivetrain(source.getDrivetrain());
        }
    }

    private static void mergePerformance(Performance target, Performance source) {
        if (source.getZeroToHundredSeconds() != null) {
            target.setZeroToHundredSeconds(source.getZeroToHundredSeconds());
        }
        if (source.getTopSpeedKmh() != null) {
            target.setTopSpeedKmh(source.getTopSpeedKmh());
        }
    }

    private static void mergeDimensions(Dimensions target, Dimensions source) {
        if (source.getLengthMm() != null) {
            target.setLengthMm(source.getLengthMm());
        }
        if (source.getWidthMm() != null) {
            target.setWidthMm(source.getWidthMm());
        }
        if (source.getHeightMm() != null) {
            target.setHeightMm(source.getHeightMm());
        }
        if (source.getWheelbaseMm() != null) {
            target.setWheelbaseMm(source.getWheelbaseMm());
        }
        if (source.getGroundClearanceMm() != null) {
            target.setGroundClearanceMm(source.getGroundClearanceMm());
        }
        if (source.getTurningCircleM() != null) {
            target.setTurningCircleM(source.getTurningCircleM());
        }
        if (source.getBootLitres() != null) {
            target.setBootLitres(source.getBootLitres());
        }
        if (source.getKerbWeightKg() != null) {
            target.setKerbWeightKg(source.getKerbWeightKg());
        }
        if (source.getSeats() != null) {
            target.setSeats(source.getSeats());
        }
    }

    private static void mergeTowing(Towing target, Towing source) {
        if (source.getTowingBrakedKg() != null) {
            target.setTowingBrakedKg(source.getTowingBrakedKg());
        }
    }

    private static void mergeWheels(Wheels target, Wheels source) {
        if (source.getTyreSize() != null) {
            target.setTyreSize(source.getTyreSize());
        }
    }

    private static void mergeInfotainment(Infotainment target, Infotainment source) {
        if (source.getInfotainmentScreenSizeInches() != null) {
            target.setInfotainmentScreenSizeInches(source.getInfotainmentScreenSizeInches());
        }
        if (source.getSpeakerCount() != null) {
            target.setSpeakerCount(source.getSpeakerCount());
        }
    }

    private static void mergeEconomy(Economy target, Economy source) {
        if (source.getFuelConsumptionCombined() != null) {
            target.setFuelConsumptionCombined(source.getFuelConsumptionCombined());
        }
        if (source.getFuelTankLitres() != null) {
            target.setFuelTankLitres(source.getFuelTankLitres());
        }
        if (source.getCo2Gkm() != null) {
            target.setCo2Gkm(source.getCo2Gkm());
        }
    }

    private static void mergeSafety(Safety target, Safety source) {
        if (source.getNcapStars() != null) {
            target.setNcapStars(source.getNcapStars());
        }
        if (source.getAirbags() != null) {
            target.setAirbags(source.getAirbags());
        }
        if (source.getAbs() != null) {
            target.setAbs(source.getAbs());
        }
        if (source.getEsp() != null) {
            target.setEsp(source.getEsp());
        }
        if (source.getTractionControl() != null) {
            target.setTractionControl(source.getTractionControl());
        }
        if (source.getAeb() != null) {
            target.setAeb(source.getAeb());
        }
        if (source.getLaneAssist() != null) {
            target.setLaneAssist(source.getLaneAssist());
        }
        if (source.getBlindSpotMonitoring() != null) {
            target.setBlindSpotMonitoring(source.getBlindSpotMonitoring());
        }
        if (source.getAdaptiveCruiseControl() != null) {
            target.setAdaptiveCruiseControl(source.getAdaptiveCruiseControl());
        }
        if (source.getRearCrossTrafficAlert() != null) {
            target.setRearCrossTrafficAlert(source.getRearCrossTrafficAlert());
        }
    }

    private static void mergeFeatures(Features target, Features source) {
        if (source.getAndroidAuto() != null) {
            target.setAndroidAuto(source.getAndroidAuto());
        }
        if (source.getAppleCarplay() != null) {
            target.setAppleCarplay(source.getAppleCarplay());
        }
        if (source.getReverseCamera() != null) {
            target.setReverseCamera(source.getReverseCamera());
        }
        if (source.getParkingSensorsFront() != null) {
            target.setParkingSensorsFront(source.getParkingSensorsFront());
        }
        if (source.getParkingSensorsRear() != null) {
            target.setParkingSensorsRear(source.getParkingSensorsRear());
        }
        if (source.getDigitalCluster() != null) {
            target.setDigitalCluster(source.getDigitalCluster());
        }
        if (source.getKeylessEntry() != null) {
            target.setKeylessEntry(source.getKeylessEntry());
        }
        if (source.getPushButtonStart() != null) {
            target.setPushButtonStart(source.getPushButtonStart());
        }
        if (source.getWirelessCharging() != null) {
            target.setWirelessCharging(source.getWirelessCharging());
        }
        if (source.getClimateControl() != null) {
            target.setClimateControl(source.getClimateControl());
        }
        if (source.getClimateControlType() != null) {
            target.setClimateControlType(source.getClimateControlType());
        }
        if (source.getHeatedSeats() != null) {
            target.setHeatedSeats(source.getHeatedSeats());
        }
        if (source.getElectricSeats() != null) {
            target.setElectricSeats(source.getElectricSeats());
        }
        if (source.getSunroof() != null) {
            target.setSunroof(source.getSunroof());
        }
        if (source.getPremiumAudio() != null) {
            target.setPremiumAudio(source.getPremiumAudio());
        }
    }

    private static void mergeOwnership(Ownership target, Ownership source) {
        if (source.getWarrantyYears() != null) {
            target.setWarrantyYears(source.getWarrantyYears());
        }
        if (source.getWarrantyKm() != null) {
            target.setWarrantyKm(source.getWarrantyKm());
        }
        if (source.getServicePlanYears() != null) {
            target.setServicePlanYears(source.getServicePlanYears());
        }
        if (source.getServicePlanKm() != null) {
            target.setServicePlanKm(source.getServicePlanKm());
        }
        if (source.getServiceIntervalKm() != null) {
            target.setServiceIntervalKm(source.getServiceIntervalKm());
        }
        if (source.getMaintenancePlanYears() != null) {
            target.setMaintenancePlanYears(source.getMaintenancePlanYears());
        }
        if (source.getMaintenancePlanKm() != null) {
            target.setMaintenancePlanKm(source.getMaintenancePlanKm());
        }
        if (source.getPartsSupportScore() != null) {
            target.setPartsSupportScore(source.getPartsSupportScore());
        }
        if (source.getLocalProduction() != null) {
            target.setLocalProduction(source.getLocalProduction());
        }
    }

    private static void mergePricing(Pricing target, Pricing source) {
        if (source.getListPriceZar() != null) {
            target.setListPriceZar(source.getListPriceZar());
        }
        if (source.getDealerOfferZar() != null) {
            target.setDealerOfferZar(source.getDealerOfferZar());
        }
        if (source.getPriceDate() != null) {
            target.setPriceDate(source.getPriceDate());
        }
    }

    private static void mergeSource(Source target, Source source) {
        if (source.getSourceType() != null) {
            target.setSourceType(source.getSourceType());
        }
        if (source.getSourceName() != null) {
            target.setSourceName(source.getSourceName());
        }
        if (source.getSourceUrl() != null) {
            target.setSourceUrl(source.getSourceUrl());
        }
    }
}
