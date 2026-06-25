package za.driver.presentation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import za.driver.model.ScoringProfile;
import za.driver.model.Vehicle;

public final class PresentationExportService {

    private static final String RESOURCE_ROOT = "presentation/";

    public void export(Path outputDir, List<Vehicle> vehicles, ScoringProfile profile) throws IOException {
        export(outputDir, vehicles, profile, LocalDateTime.now());
    }

    public void export(Path outputDir, List<Vehicle> vehicles, ScoringProfile profile, LocalDateTime exportedAt)
            throws IOException {
        if (vehicles == null || vehicles.isEmpty()) {
            throw new IllegalArgumentException("At least one vehicle is required.");
        }
        Files.createDirectories(outputDir);

        copyResourceTree(RESOURCE_ROOT + "reveal", outputDir.resolve("reveal"));
        copyResourceTree(RESOURCE_ROOT + "css", outputDir.resolve("css"));
        copyResourceTree(RESOURCE_ROOT + "images", outputDir.resolve("images"));
        copyResource(RESOURCE_ROOT + "assets/logo.png", outputDir.resolve("assets/logo.png"));

        List<BodyTypeSection> sections = ModelGroup.groupByBodyType(vehicles, profile);
        String html = RevealPresentationBuilder.build(sections, profile, exportedAt);
        Files.writeString(outputDir.resolve("index.html"), html);
        Files.writeString(outputDir.resolve("IMAGES.md"), buildImageManifest(ModelGroup.flattenSections(sections)));
    }

    private static String buildImageManifest(List<ModelGroup> groups) {
        StringBuilder manifest = new StringBuilder();
        manifest.append("# Vehicle images\n\n");
        manifest.append("Drop hero images into the `images/` folder using these exact filenames.\n");
        manifest.append("Reload `index.html` in your browser after adding files.\n\n");
        manifest.append("| Model | Filename |\n");
        manifest.append("|-------|----------|\n");
        for (ModelGroup group : groups) {
            manifest.append("| ")
                    .append(group.displayName())
                    .append(" | `images/")
                    .append(group.imageFilename())
                    .append("` |\n");
        }
        return manifest.toString();
    }

    private void copyResourceTree(String resourcePrefix, Path targetDir) throws IOException {
        String normalizedPrefix = resourcePrefix.endsWith("/") ? resourcePrefix : resourcePrefix + "/";
        List<String> resources = listResourcePaths(normalizedPrefix);
        if (resources.isEmpty()) {
            throw new IOException("Missing presentation resources at " + resourcePrefix);
        }
        for (String resourcePath : resources) {
            if (resourcePath.endsWith("/")) {
                continue;
            }
            String relative = resourcePath.substring(normalizedPrefix.length());
            Path target = targetDir.resolve(relative);
            copyResource(resourcePath, target);
        }
    }

    private static List<String> listResourcePaths(String prefix) throws IOException {
        ClassLoader loader = PresentationExportService.class.getClassLoader();
        String lookupPrefix = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
        java.net.URL url = loader.getResource(lookupPrefix);
        if (url == null) {
            return List.of();
        }
        String normalizedPrefix = prefix.endsWith("/") ? prefix : prefix + "/";
        if ("file".equals(url.getProtocol())) {
            Path root;
            try {
                root = Path.of(url.toURI());
            } catch (java.net.URISyntaxException ex) {
                throw new IOException("Invalid resource URI: " + url, ex);
            }
            if (!Files.isDirectory(root)) {
                return List.of(normalizedPrefix.substring(0, normalizedPrefix.length() - 1));
            }
            try (Stream<Path> paths = Files.walk(root)) {
                return paths
                        .filter(Files::isRegularFile)
                        .map(path -> normalizedPrefix + root.relativize(path).toString().replace('\\', '/'))
                        .sorted()
                        .toList();
            }
        }
        if ("jar".equals(url.getProtocol())) {
            String jarPath = url.getPath();
            int separator = jarPath.indexOf("!/");
            String jarFile = jarPath.substring(5, separator);
            String entryPrefix = jarPath.substring(separator + 2);
            final String normalizedEntryPrefix = entryPrefix.endsWith("/") ? entryPrefix : entryPrefix + "/";
            try (java.util.jar.JarFile jar = new java.util.jar.JarFile(java.net.URLDecoder.decode(jarFile, "UTF-8"))) {
                return jar.stream()
                        .map(java.util.jar.JarEntry::getName)
                        .filter(name -> name.startsWith(normalizedEntryPrefix) && !name.endsWith("/"))
                        .sorted()
                        .toList();
            }
        }
        throw new IOException("Unsupported resource URL: " + url);
    }

    private static void copyResource(String resourcePath, Path target) throws IOException {
        ClassLoader loader = PresentationExportService.class.getClassLoader();
        try (InputStream input = loader.getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IOException("Missing resource: " + resourcePath);
            }
            Files.createDirectories(target.getParent());
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
