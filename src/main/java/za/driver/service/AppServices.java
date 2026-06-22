package za.driver.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import za.driver.import_.ImportService;
import za.driver.model.GarageDimensions;
import za.driver.model.ScoringProfile;
import za.driver.persistence.GarageConfigRepository;
import za.driver.persistence.ScoringProfileRepository;
import za.driver.persistence.VehicleRepository;
import za.driver.scoring.ScoringDataReportService;
import za.driver.scoring.ScoringService;
import za.driver.spreadsheet.SpreadsheetExportService;
import za.driver.spreadsheet.SpreadsheetImportService;

public final class AppServices {

    public final VehicleService vehicleService;
    public final ImportService importService;
    public final SpreadsheetExportService spreadsheetExportService;
    public final SpreadsheetImportService spreadsheetImportService;
    public final ScoringDataReportService scoringDataReportService;
    public final ScoringProfileService profileService;
    public final GarageConfigService garageConfigService;
    public final ScoringProfile activeProfile;
    public final GarageDimensions garageDimensions;
    public final Path dataRoot;

    private AppServices(
            VehicleService vehicleService,
            ImportService importService,
            SpreadsheetExportService spreadsheetExportService,
            SpreadsheetImportService spreadsheetImportService,
            ScoringDataReportService scoringDataReportService,
            ScoringProfileService profileService,
            GarageConfigService garageConfigService,
            ScoringProfile activeProfile,
            Path dataRoot) {
        this.vehicleService = vehicleService;
        this.importService = importService;
        this.spreadsheetExportService = spreadsheetExportService;
        this.spreadsheetImportService = spreadsheetImportService;
        this.scoringDataReportService = scoringDataReportService;
        this.profileService = profileService;
        this.garageConfigService = garageConfigService;
        this.activeProfile = activeProfile;
        this.garageDimensions = garageConfigService.getGarageDimensions();
        this.dataRoot = dataRoot;
    }

    public static AppServices create() throws IOException {
        Path dataRoot = Paths.get("data").toAbsolutePath().normalize();
        VehicleRepository vehicleRepository = new VehicleRepository(dataRoot);
        ScoringProfileRepository profileRepository = new ScoringProfileRepository(dataRoot);
        ScoringService scoringService = new ScoringService();
        ScoringDataReportService scoringDataReportService = new ScoringDataReportService();
        VehicleService vehicleService = new VehicleService(vehicleRepository, scoringService);
        ScoringProfileService profileService = new ScoringProfileService(
                profileRepository,
                vehicleRepository,
                scoringService);
        ImportService importService = new ImportService();
        SpreadsheetExportService spreadsheetExportService = new SpreadsheetExportService();
        SpreadsheetImportService spreadsheetImportService = new SpreadsheetImportService(vehicleRepository);
        ScoringProfile activeProfile = DefaultProfileSeeder.ensureDefaultProfile(profileRepository);
        activeProfile = profileService.ensureMigratedProfile(activeProfile);
        GarageConfigService garageConfigService = new GarageConfigService(new GarageConfigRepository(dataRoot));
        return new AppServices(
                vehicleService,
                importService,
                spreadsheetExportService,
                spreadsheetImportService,
                scoringDataReportService,
                profileService,
                garageConfigService,
                activeProfile,
                dataRoot);
    }
}
