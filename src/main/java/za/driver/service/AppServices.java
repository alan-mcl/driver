package za.driver.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import za.driver.import_.ImportService;
import za.driver.model.GarageDimensions;
import za.driver.model.ScoringProfile;
import za.driver.persistence.AppConfigRepository;
import za.driver.persistence.BrandReliabilityConfigRepository;
import za.driver.persistence.GarageConfigRepository;
import za.driver.persistence.ScoringProfileRepository;
import za.driver.persistence.VehicleRepository;
import za.driver.presentation.PresentationExportService;
import za.driver.scoring.ScoringDataReportService;
import za.driver.scoring.ScoringService;
import za.driver.spreadsheet.SpreadsheetExportService;
import za.driver.spreadsheet.SpreadsheetImportService;

public final class AppServices {

    public final VehicleService vehicleService;
    public final ImportService importService;
    public final SpreadsheetExportService spreadsheetExportService;
    public final SpreadsheetImportService spreadsheetImportService;
    public final PresentationExportService presentationExportService;
    public final ScoringDataReportService scoringDataReportService;
    public final ScoringProfileService profileService;
    public final GarageConfigService garageConfigService;
    public final BrandReliabilityConfigService brandReliabilityConfigService;
    public final AppConfigService appConfigService;
    public ScoringProfile activeProfile;
    public final GarageDimensions garageDimensions;
    public final Path dataRoot;

    private AppServices(
            VehicleService vehicleService,
            ImportService importService,
            SpreadsheetExportService spreadsheetExportService,
            SpreadsheetImportService spreadsheetImportService,
            PresentationExportService presentationExportService,
            ScoringDataReportService scoringDataReportService,
            ScoringProfileService profileService,
            GarageConfigService garageConfigService,
            BrandReliabilityConfigService brandReliabilityConfigService,
            AppConfigService appConfigService,
            ScoringProfile activeProfile,
            Path dataRoot) {
        this.vehicleService = vehicleService;
        this.importService = importService;
        this.spreadsheetExportService = spreadsheetExportService;
        this.spreadsheetImportService = spreadsheetImportService;
        this.presentationExportService = presentationExportService;
        this.scoringDataReportService = scoringDataReportService;
        this.profileService = profileService;
        this.garageConfigService = garageConfigService;
        this.brandReliabilityConfigService = brandReliabilityConfigService;
        this.appConfigService = appConfigService;
        this.activeProfile = activeProfile;
        this.garageDimensions = garageConfigService.getGarageDimensions();
        this.dataRoot = dataRoot;
    }

    public void setActiveProfile(ScoringProfile profile) throws IOException {
        if (profile == null || profile.getId() == null) {
            throw new IllegalArgumentException("Active profile must have an id");
        }
        activeProfile = profile;
        appConfigService.setActiveProfileId(profile.getId());
    }

    public static AppServices create() throws IOException {
        Path dataRoot = Paths.get("data").toAbsolutePath().normalize();
        VehicleRepository vehicleRepository = new VehicleRepository(dataRoot);
        ScoringProfileRepository profileRepository = new ScoringProfileRepository(dataRoot);
        BrandReliabilityConfigService brandReliabilityConfigService =
                new BrandReliabilityConfigService(new BrandReliabilityConfigRepository(dataRoot));
        ScoringService scoringService = new ScoringService(brandReliabilityConfigService);
        ScoringDataReportService scoringDataReportService =
                new ScoringDataReportService(brandReliabilityConfigService);
        VehicleService vehicleService = new VehicleService(vehicleRepository, scoringService);
        ScoringProfileService profileService = new ScoringProfileService(
                profileRepository,
                vehicleRepository,
                scoringService);
        ImportService importService = new ImportService();
        SpreadsheetExportService spreadsheetExportService = new SpreadsheetExportService();
        SpreadsheetImportService spreadsheetImportService = new SpreadsheetImportService(vehicleService);
        PresentationExportService presentationExportService = new PresentationExportService();
        DefaultProfileSeeder.ensureDefaultProfiles(profileRepository);
        List<ScoringProfile> profiles = profileService.findAll();
        AppConfigService appConfigService = new AppConfigService(new AppConfigRepository(dataRoot));
        ScoringProfile activeProfile = resolveActiveProfile(profiles, appConfigService.getActiveProfileId());
        if (activeProfile.getId() != null
                && !Objects.equals(activeProfile.getId(), appConfigService.getActiveProfileId())) {
            appConfigService.setActiveProfileId(activeProfile.getId());
        }
        GarageConfigService garageConfigService = new GarageConfigService(new GarageConfigRepository(dataRoot));
        return new AppServices(
                vehicleService,
                importService,
                spreadsheetExportService,
                spreadsheetImportService,
                presentationExportService,
                scoringDataReportService,
                profileService,
                garageConfigService,
                brandReliabilityConfigService,
                appConfigService,
                activeProfile,
                dataRoot);
    }

    private static ScoringProfile resolveActiveProfile(List<ScoringProfile> profiles, UUID activeProfileId) {
        if (profiles.isEmpty()) {
            throw new IllegalStateException("No scoring profiles available");
        }
        if (activeProfileId != null) {
            for (ScoringProfile profile : profiles) {
                if (activeProfileId.equals(profile.getId())) {
                    return profile;
                }
            }
        }
        for (ScoringProfile profile : profiles) {
            if (DefaultProfileSeeder.FAMILY_FOCUSED_ID.equals(profile.getId())) {
                return profile;
            }
        }
        return profiles.get(0);
    }
}
