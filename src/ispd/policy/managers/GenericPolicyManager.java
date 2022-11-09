package ispd.policy.managers;

import ispd.policy.PolicyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

// TODO: Document
/* package-private */
abstract class GenericPolicyManager implements PolicyManager {
    public static final String MOTOR_PKG_NAME = "motor";
    protected final ArrayList<String> policies = new ArrayList<>(0);
    protected final List<String> addedPolicies = new ArrayList<>(0);
    protected final List<String> removedPolicies = new ArrayList<>(0);

    protected static void copyFile(final File dest, final File src) {
        if (dest.getPath().equals(src.getPath())) {
            return;
        }

        try (final var srcFs = new FileInputStream(src);
             final var destFs = new FileOutputStream(dest)) {
            srcFs.transferTo(destFs);
        } catch (final IOException ex) {
            Logger.getLogger(GenericPolicyManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    protected static String removeDotClassSuffix(final String s) {
        return s.substring(0, s.length() - ".class".length());
    }

    protected static void executeFromJar(final String path) {
        final var jar = new File(
                System.getProperty("java.class.path"));

        try {
            GenericPolicyManager.extractDirFromJar(path, jar);
            GenericPolicyManager.extractDirFromJar(
                    GenericPolicyManager.MOTOR_PKG_NAME, jar);
        } catch (final IOException ex) {
            Logger.getLogger(GenericPolicyManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Extracts given dir from jar file given by file.
     *
     * @param dir  Directory name to be extracted
     * @param file Jar file from which to extract the directory
     */
    protected static void extractDirFromJar(final String dir,
                                            final File file) throws IOException {
        try (final var jar = new JarFile(file)) {
            for (final var entry : new JarEntryIterable(jar)) {
                if (entry.getName().contains(dir)) {
                    GenericPolicyManager.processZipEntry(entry, jar);
                }
            }
        }
    }

    private static void processZipEntry(
            final ZipEntry entry, final ZipFile zip) throws IOException {
        final var file = new File(entry.getName());

        if (entry.isDirectory() && !file.exists()) {
            GenericPolicyManager.createDirectory(file);
            return;
        }

        if (!file.getParentFile().exists()) {
            GenericPolicyManager.createDirectory(file.getParentFile());
        }

        try (final var is = zip.getInputStream(entry);
             final var os = new FileOutputStream(file)) {
            is.transferTo(os);
        }
    }

    protected static void createDirectory(final File dir) throws IOException {
        if (!dir.mkdirs()) {
            throw new IOException("Failed to create directory " + dir);
        }
    }

    /**
     * Lists all available allocation policies.
     *
     * @return {@code ArrayList} with all allocation policies' names
     */
    @Override
    public ArrayList<String> listar() {
        return this.policies;
    }

    /**
     * @return added policies
     */
    @Override
    public List listarAdicionados() {
        return this.addedPolicies;
    }

    /**
     * @return remove policies
     */
    @Override
    public List listarRemovidos() {
        return this.removedPolicies;
    }

    /**
     * Add policy to the inner list of policies
     */
    protected void addPolicy(final String policyName) {
        if (this.policies.contains(policyName)) {
            return;
        }

        this.policies.add(policyName);
        this.addedPolicies.add(policyName);
    }

    /**
     * Remove policy of given name from the inner list of policies
     */
    protected void removePolicy(final String policyName) {
        if (!this.policies.contains(policyName)) {
            return;
        }

        this.policies.remove(policyName);
        this.removedPolicies.add(policyName);
    }
}
