package ispd.policy.managers.util;

import ispd.policy.managers.FilePolicyManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarExtractor {
    private static final String MOTOR_PKG_PATH = "motor";
    private final ZipFile jar = new JarFile(new File(
            System.getProperty("java.class.path")
    ));
    private final String targetPackage;

    public JarExtractor(final String targetPackage) throws IOException {
        this.targetPackage = targetPackage;
    }

    public void extractDirsFromJar() throws IOException {
        this.extractDirFromJar(this.targetPackage);
        this.extractDirFromJar(JarExtractor.MOTOR_PKG_PATH);
    }

    private void extractDirFromJar(final String dir) throws IOException {
        final var entries = this.jar.stream()
                .filter(e -> e.getName().contains(dir))
                .toList();

        for (final var entry : entries) {
            this.processZipEntry(entry);
        }
    }

    private void processZipEntry(final ZipEntry entry) throws IOException {
        final var file = new File(entry.getName());

        if (entry.isDirectory() && !file.exists()) {
            FilePolicyManager.createDirectory(file);
            return;
        }

        // TODO: Idea; process ALL directories FIRST, and only THEN
        //  non-directory entries.
        //  In such a way, this logic (entry without parent directory
        //  created) may not be necessary anymore

        final var parent = file.getParentFile();

        if (!parent.exists()) {
            FilePolicyManager.createDirectory(parent);
        }

        try (final var is = this.jar.getInputStream(entry);
             final var os = new FileOutputStream(file)) {
            is.transferTo(os);
        }
    }
}
