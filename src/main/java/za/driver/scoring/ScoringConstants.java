package za.driver.scoring;

public final class ScoringConstants {

    public static final double FUEL_MIN = 4.0;
    public static final double FUEL_MAX = 12.0;
    public static final double PRICE_MIN = 200_000.0;
    public static final double PRICE_MAX = 800_000.0;
    public static final double WARRANTY_MIN = 0.0;
    public static final double WARRANTY_MAX = 7.0;
    public static final double WARRANTY_KM_MIN = 0.0;
    public static final double WARRANTY_KM_MAX = 200_000.0;
    public static final double SERVICE_PLAN_YEARS_MAX = 5.0;
    public static final double SERVICE_PLAN_KM_MAX = 100_000.0;
    public static final double MAINTENANCE_PLAN_YEARS_MAX = 5.0;
    public static final double MAINTENANCE_PLAN_KM_MAX = 120_000.0;
    public static final double TYRE_RIM_MIN = 15.0;
    public static final double TYRE_RIM_MAX = 20.0;
    public static final double TYRE_WIDTH_MIN = 185.0;
    public static final double TYRE_WIDTH_MAX = 255.0;
    public static final double SEATS_MIN = 2.0;
    public static final double SEATS_MAX = 7.0;
    public static final double WHEELBASE_MIN = 2400.0;
    public static final double WHEELBASE_MAX = 3000.0;
    public static final double TURNING_CIRCLE_MIN = 9.0;
    public static final double TURNING_CIRCLE_MAX = 12.5;
    public static final double LENGTH_MIN = 4000.0;
    public static final double LENGTH_MAX = 5000.0;

    public static final double[] PERFORMANCE_PWR_WEIGHT_BREAKPOINTS = {50.0, 80.0, 120.0};
    public static final double[] PERFORMANCE_PWR_WEIGHT_SCORES = {0.0, 50.0, 100.0};
    public static final double[] PERFORMANCE_TRQ_WEIGHT_BREAKPOINTS = {80.0, 150.0, 250.0};
    public static final double[] PERFORMANCE_TRQ_WEIGHT_SCORES = {0.0, 50.0, 100.0};
    public static final double[] PERFORMANCE_ACCEL_TIME_BREAKPOINTS = {6.0, 10.0, 15.0};
    public static final double[] PERFORMANCE_ACCEL_TIME_SCORES = {100.0, 50.0, 0.0};
    public static final double[] PERFORMANCE_EST_ACCEL_PWR_BREAKPOINTS = {50.0, 80.0, 120.0};
    public static final double[] PERFORMANCE_EST_ACCEL_TIME_SECONDS = {15.0, 10.0, 6.0};
    public static final double PERFORMANCE_TRANSMISSION_NEUTRAL_SCORE = 70.0;

    public static final double SAFETY_NCAP_WEIGHT = 40.0;
    public static final double SAFETY_AIRBAGS_WEIGHT = 15.0;
    public static final double SAFETY_ABS_WEIGHT = 5.0;
    public static final double SAFETY_STABILITY_WEIGHT = 10.0;
    public static final double SAFETY_AEB_WEIGHT = 10.0;
    public static final double SAFETY_LANE_ASSIST_WEIGHT = 8.0;
    public static final double SAFETY_BLIND_SPOT_WEIGHT = 7.0;
    public static final double SAFETY_REAR_CROSS_TRAFFIC_WEIGHT = 5.0;

    public static final double RUNNING_COST_FUEL_WEIGHT = 30.0;
    public static final double RUNNING_COST_WARRANTY_COVERAGE_WEIGHT = 15.0;
    public static final double RUNNING_COST_SERVICE_PLAN_WEIGHT = 12.0;
    public static final double RUNNING_COST_MAINTENANCE_PLAN_WEIGHT = 12.0;
    public static final double RUNNING_COST_PARTS_SUPPORT_WEIGHT = 20.0;
    public static final double RUNNING_COST_TYRE_COST_WEIGHT = 11.0;

    public static final double COMFORT_CLIMATE_WEIGHT = 25.0;
    public static final double COMFORT_HEATED_SEATS_WEIGHT = 20.0;
    public static final double COMFORT_ELECTRIC_SEATS_WEIGHT = 15.0;
    public static final double COMFORT_SEATS_WEIGHT = 15.0;
    public static final double COMFORT_WHEELBASE_WEIGHT = 25.0;

    public static final double PERFORMANCE_ACCELERATION_WEIGHT = 40.0;
    public static final double PERFORMANCE_POWER_TO_WEIGHT_WEIGHT = 30.0;
    public static final double PERFORMANCE_TORQUE_TO_WEIGHT_WEIGHT = 20.0;
    public static final double PERFORMANCE_TRANSMISSION_WEIGHT = 10.0;

    public static final double DAILY_DRIVER_TURNING_CIRCLE_WEIGHT = 25.0;
    public static final double DAILY_DRIVER_LENGTH_WEIGHT = 25.0;
    public static final double DAILY_DRIVER_FUEL_WEIGHT = 25.0;
    public static final double DAILY_DRIVER_PARKING_AIDS_WEIGHT = 25.0;

    public static final double TECH_ANDROID_AUTO_WEIGHT = 12.0;
    public static final double TECH_APPLE_CARPLAY_WEIGHT = 12.0;
    public static final double TECH_REVERSE_CAMERA_WEIGHT = 12.0;
    public static final double TECH_DIGITAL_CLUSTER_WEIGHT = 12.0;
    public static final double TECH_WIRELESS_CHARGING_WEIGHT = 12.0;
    public static final double TECH_KEYLESS_ENTRY_WEIGHT = 10.0;
    public static final double TECH_PUSH_BUTTON_START_WEIGHT = 10.0;
    public static final double TECH_ADAPTIVE_CRUISE_WEIGHT = 20.0;

    public static final double AWESOMENESS_PRESTIGE_WEIGHT = 55.0;
    public static final double AWESOMENESS_COMFORT_WEIGHT = 15.0;
    public static final double AWESOMENESS_DAILY_DRIVER_WEIGHT = 15.0;
    public static final double AWESOMENESS_TECHNOLOGY_WEIGHT = 15.0;

    public static final double RELIABILITY_BRAND_WEIGHT = 0.50;
    public static final double RELIABILITY_POWERTRAIN_WEIGHT = 0.20;
    public static final double RELIABILITY_PARTS_SUPPORT_WEIGHT = 0.30;

    public static final double POWERTRAIN_BASE_SCORE = 75.0;
    public static final double POWERTRAIN_NA_PETROL_ADJ = 10.0;
    public static final double POWERTRAIN_NA_DIESEL_ADJ = 8.0;
    public static final double POWERTRAIN_TURBO_PETROL_ADJ = -3.0;
    public static final double POWERTRAIN_TURBO_DIESEL_ADJ = 2.0;
    public static final double POWERTRAIN_HYBRID_ADJ = 3.0;
    public static final double POWERTRAIN_PHEV_ADJ = -10.0;
    public static final double POWERTRAIN_EV_ADJ = 5.0;
    public static final double POWERTRAIN_MANUAL_ADJ = 5.0;
    public static final double POWERTRAIN_AUTOMATIC_ADJ = 5.0;
    public static final double POWERTRAIN_CVT_ADJ = -5.0;
    public static final double POWERTRAIN_DCT_ADJ = -8.0;

    private ScoringConstants() {
    }
}
