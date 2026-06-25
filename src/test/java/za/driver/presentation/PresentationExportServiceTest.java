package za.driver.presentation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import za.driver.scoring.ScoringService;
import za.driver.scoring.ScoringTestFixtures;

class PresentationExportServiceTest {

    private final PresentationExportService exportService = new PresentationExportService();
    private final ScoringService scoringService = new ScoringService();

    @Test
    void export_writesPresentationFolderWithAssetsAndManifest(@TempDir Path tempDir) throws Exception {
        var vehicle = ScoringTestFixtures.fullVehicle();
        vehicle.setDerivedMetrics(scoringService.calculate(vehicle, ScoringTestFixtures.familyFocusedProfile()));

        Path outputDir = tempDir.resolve("presentation");
        exportService.export(outputDir, List.of(vehicle), ScoringTestFixtures.familyFocusedProfile());

        assertTrue(Files.isRegularFile(outputDir.resolve("index.html")));
        assertTrue(Files.isRegularFile(outputDir.resolve("IMAGES.md")));
        assertTrue(Files.isRegularFile(outputDir.resolve("reveal/dist/reveal.js")));
        assertTrue(Files.isRegularFile(outputDir.resolve("reveal/dist/reveal.css")));
        assertTrue(Files.isRegularFile(outputDir.resolve("reveal/dist/theme/black.css")));
        assertTrue(Files.isRegularFile(outputDir.resolve("css/driver-presentation.css")));
        assertTrue(Files.isRegularFile(outputDir.resolve("images/_placeholder.svg")));
        assertTrue(Files.isRegularFile(outputDir.resolve("assets/logo.png")));

        String html = Files.readString(outputDir.resolve("index.html"));
        assertTrue(html.contains("images/toyota-corolla.jpg"));
        assertTrue(html.contains("reveal/dist/reveal.js"));

        String manifest = Files.readString(outputDir.resolve("IMAGES.md"));
        assertTrue(manifest.contains("toyota-corolla.jpg"));
        assertTrue(manifest.contains("Toyota Corolla"));
    }
}
