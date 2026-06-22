package za.driver.scoring;

import static za.driver.scoring.CoverageScoreUtil.coverageScore;
import static za.driver.scoring.ScoreUtil.clamp;
import static za.driver.scoring.ScoreUtil.inverseScale;
import static za.driver.scoring.ScoreUtil.subScore;
import static za.driver.scoring.ScoreUtil.weightedAverage;
import static za.driver.scoring.ScoringConstants.FUEL_MAX;
import static za.driver.scoring.ScoringConstants.FUEL_MIN;
import static za.driver.scoring.ScoringConstants.MAINTENANCE_PLAN_KM_MAX;
import static za.driver.scoring.ScoringConstants.MAINTENANCE_PLAN_YEARS_MAX;
import static za.driver.scoring.ScoringConstants.RUNNING_COST_FUEL_WEIGHT;
import static za.driver.scoring.ScoringConstants.RUNNING_COST_MAINTENANCE_PLAN_WEIGHT;
import static za.driver.scoring.ScoringConstants.RUNNING_COST_PARTS_SUPPORT_WEIGHT;
import static za.driver.scoring.ScoringConstants.RUNNING_COST_SERVICE_PLAN_WEIGHT;
import static za.driver.scoring.ScoringConstants.RUNNING_COST_TYRE_COST_WEIGHT;
import static za.driver.scoring.ScoringConstants.RUNNING_COST_WARRANTY_COVERAGE_WEIGHT;
import static za.driver.scoring.ScoringConstants.SERVICE_PLAN_KM_MAX;
import static za.driver.scoring.ScoringConstants.SERVICE_PLAN_YEARS_MAX;
import static za.driver.scoring.ScoringConstants.WARRANTY_KM_MAX;
import static za.driver.scoring.ScoringConstants.WARRANTY_MAX;
import static za.driver.scoring.TyreCostUtil.tyreCostScore;

import java.util.ArrayList;
import java.util.List;

import za.driver.model.Economy;
import za.driver.model.Metric;
import za.driver.model.Ownership;
import za.driver.model.Vehicle;
import za.driver.model.Wheels;

public class RunningCostCalculator implements MetricCalculator {

    @Override
    public Metric metric() {
        return Metric.RUNNING_COST;
    }

    @Override
    public Double calculate(Vehicle vehicle) {
        List<ScoreUtil.SubScore> subScores = new ArrayList<>();

        Economy economy = vehicle.getEconomy();
        if (economy != null && economy.getFuelConsumptionCombined() != null) {
            subScores.add(subScore(
                    inverseScale(economy.getFuelConsumptionCombined(), FUEL_MIN, FUEL_MAX),
                    RUNNING_COST_FUEL_WEIGHT));
        }

        Ownership ownership = vehicle.getOwnership();
        if (ownership != null) {
            Double warrantyCoverage = coverageScore(
                    ownership.getWarrantyYears(),
                    ownership.getWarrantyKm(),
                    WARRANTY_MAX,
                    WARRANTY_KM_MAX);
            if (warrantyCoverage != null) {
                subScores.add(subScore(warrantyCoverage, RUNNING_COST_WARRANTY_COVERAGE_WEIGHT));
            }

            Double servicePlanCoverage = coverageScore(
                    ownership.getServicePlanYears(),
                    ownership.getServicePlanKm(),
                    SERVICE_PLAN_YEARS_MAX,
                    SERVICE_PLAN_KM_MAX);
            if (servicePlanCoverage != null) {
                subScores.add(subScore(servicePlanCoverage, RUNNING_COST_SERVICE_PLAN_WEIGHT));
            }

            Double maintenancePlanCoverage = coverageScore(
                    ownership.getMaintenancePlanYears(),
                    ownership.getMaintenancePlanKm(),
                    MAINTENANCE_PLAN_YEARS_MAX,
                    MAINTENANCE_PLAN_KM_MAX);
            if (maintenancePlanCoverage != null) {
                subScores.add(subScore(maintenancePlanCoverage, RUNNING_COST_MAINTENANCE_PLAN_WEIGHT));
            }

            if (ownership.getPartsSupportScore() != null) {
                subScores.add(subScore(
                        clamp(ownership.getPartsSupportScore().doubleValue()),
                        RUNNING_COST_PARTS_SUPPORT_WEIGHT));
            }
        }

        Wheels wheels = vehicle.getWheels();
        if (wheels != null) {
            Double tyreScore = tyreCostScore(wheels.getTyreSize());
            if (tyreScore != null) {
                subScores.add(subScore(tyreScore, RUNNING_COST_TYRE_COST_WEIGHT));
            }
        }

        return weightedAverage(subScores);
    }
}
