package eu.infolead.jtk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Enforces the module boundary that makes the artifact layout meaningful.
 *
 * <p>{@code jtk-lang} is the contract layer: it may use Jackson <em>annotations</em>, because they
 * are part of the type's published shape, but it must not depend on Jackson's databind mechanics or
 * on any framework. The checks below fail the build when that boundary rots, which is cheaper than
 * discovering it when a Jackson or Spring upgrade forces a core release.
 */
class ModuleBoundaryTest {

    private static final Path MAIN_SOURCES = Path.of("src", "main", "java");

    @Test
    void coreModuleDoesNotImportAFrameworkOrDatabindMechanics() throws IOException {
        final List<String> violations = new ArrayList<>();
        try (Stream<Path> sources = Files.walk(MAIN_SOURCES)) {
            sources.filter(path -> path.toString().endsWith(".java"))
                    .forEach(
                            path -> {
                                try {
                                    for (final String line : Files.readAllLines(path)) {
                                        final String trimmed = line.strip();
                                        if (trimmed.startsWith("import ")
                                                && (trimmed.contains(
                                                                "com.fasterxml.jackson.databind")
                                                        || trimmed.contains("tools.jackson")
                                                        || trimmed.contains("org.springframework")
                                                        || trimmed.contains("io.quarkus"))) {
                                            violations.add(path + ": " + trimmed);
                                        }
                                    }
                                } catch (final IOException failure) {
                                    throw new IllegalStateException(failure);
                                }
                            });
        }
        assertTrue(
                violations.isEmpty(),
                "jtk-lang must stay framework-free and annotation-only: " + violations);
    }

    @Test
    void coreModuleNeverReferencesSerializerClasses() throws IOException {
        final List<String> violations = new ArrayList<>();
        try (Stream<Path> sources = Files.walk(MAIN_SOURCES)) {
            sources.filter(path -> path.toString().endsWith(".java"))
                    .forEach(
                            path -> {
                                try {
                                    for (final String line : Files.readAllLines(path)) {
                                        if (line.contains("@JsonSerialize")
                                                || line.contains("@JsonDeserialize")) {
                                            violations.add(path + ": " + line.strip());
                                        }
                                    }
                                } catch (final IOException failure) {
                                    throw new IllegalStateException(failure);
                                }
                            });
        }
        assertTrue(
                violations.isEmpty(),
                "Custom (de)serializers belong to a Jackson module; use annotations only: "
                        + violations);
    }

    @Test
    void theBoundaryScanLooksAtRealSources() throws IOException {
        // The root package carries the cross-package requirements; this guard only
        // ensures the scan above is actually looking at real sources.
        try (Stream<Path> sources = Files.walk(MAIN_SOURCES)) {
            assertEquals(
                    true, sources.anyMatch(path -> path.toString().endsWith("package-info.java")));
        }
    }
}
