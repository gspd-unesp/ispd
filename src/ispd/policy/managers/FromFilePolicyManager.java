package ispd.policy.managers;

import ispd.policy.PolicyManager;

import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

// TODO: Document
/* package-private */
abstract class FromFilePolicyManager implements PolicyManager {
    private static final String POLICY_NAME_REPL = "__POLICY_NAME__";
    private static final String MOTOR_PKG_NAME = "motor";
    protected final ArrayList<String> policies = new ArrayList<>(0);
    private final List<String> addedPolicies = new ArrayList<>(0);
    private final List<String> removedPolicies = new ArrayList<>(0);

//    private FromFilePolicyManager(
//            final Class<? extends FromFilePolicyManager>  cls,
//            final String path) {
//        throw new UnsupportedOperationException("Not ready yet");
//    }

    protected static void executeFromJar(final String path) {
        final var jar = new File(
                System.getProperty("java.class.path"));
        try {
            FromFilePolicyManager.extractDirFromJar(path, jar);
            FromFilePolicyManager.extractDirFromJar(
                    FromFilePolicyManager.MOTOR_PKG_NAME, jar);
        } catch (final IOException ex) {
            Logger.getLogger(FromFilePolicyManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Extracts given dir from jar file given by file.
     *
     * @param dir  Directory name to be extracted
     * @param file Jar file from which to extract the directory
     */
    private static void extractDirFromJar(
            final String dir, final File file) throws IOException {
        try (final var jar = new JarFile(file)) {
            final var entries = jar.stream()
                    .filter(e -> e.getName().contains(dir))
                    .toList();

            for (final var entry : entries) {
                FromFilePolicyManager.processZipEntry(entry, jar);
            }
        }
    }

    private static void processZipEntry(
            final ZipEntry entry, final ZipFile zip) throws IOException {
        final var file = new File(entry.getName());

        if (entry.isDirectory() && !file.exists()) {
            FromFilePolicyManager.createDirectory(file);
            return;
        }

        if (!file.getParentFile().exists()) {
            FromFilePolicyManager.createDirectory(file.getParentFile());
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

    protected static String formatTemplate(
            final String template, final String policyName) {
        return template.replace(
                FromFilePolicyManager.POLICY_NAME_REPL,
                policyName
        );
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
     * @return Directory in which allocation policies sources are compiled
     * classes are saved
     */
    @Override
    public File getDiretorio() {
        return this.theDirectory();
    }

    /**
     * Writes the contents of {@code codigo} into the source file of the
     * policy given by {@code nome}.
     *
     * @param nome   Name of the policy which source file will be written to
     * @param codigo Contents to be written in the file
     * @return {@code true} if writing was successful
     */
    @Override
    public boolean escrever(final String nome, final String codigo) {
        try (final var fw = new FileWriter(
                new File(this.theDirectory(), nome + ".java"),
                StandardCharsets.UTF_8
        )) {
            fw.write(codigo);
            return true;
        } catch (final IOException ex) {
            Logger.getLogger(FromFilePolicyManager.class.getName())
                    .log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Attempts to compile the source file of the policy with given name,
     * returning the contents of stderr, if any, otherwise {@code null}.
     *
     * @param nome Name of the allocation policy to be compiled
     * @return A string with errors, if any, otherwise {@code null}
     */
    @Override
    public String compilar(final String nome) {
        final var target = new File(this.theDirectory(), nome + ".java");
        final var err = FromFilePolicyManager.compile(target);

        try {
            Thread.sleep(1000);
        } catch (final InterruptedException ex) {
            Logger.getLogger(FromFilePolicyManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        // Check if compilation worked, looking for a .class file
        if (new File(this.theDirectory(), nome + ".class").exists()) {
            this.addPolicy(nome);
        }

        return err.isEmpty() ? null : err;
    }

    protected static String compile(final File target) {
        final var compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler != null) {
            final var err = new ByteArrayOutputStream();
            compiler.run(null, null, err, target.getPath());
            return err.toString();
        } else {
            try {
                return FromFilePolicyManager.compileManually(target);
            } catch (final IOException ex) {
                Logger.getLogger(FromFilePolicyManager.class.getName())
                        .log(Level.SEVERE, null, ex);
                return "Não foi possível compilar";
            }
        }
    }

    private static String compileManually(final File target) throws IOException {
        final var proc = Runtime.getRuntime().exec("javac " + target.getPath());

        try (final var err = new BufferedReader(new InputStreamReader(
                proc.getErrorStream(), StandardCharsets.UTF_8))
        ) {
            return err.lines().collect(Collectors.joining("\n"));
        }
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
     * Reads the source file from the policy {@code alocador} and returns a
     * string with the file contents.
     *
     * @param policy Name of the policy which source file will be read
     * @return String contents of the file
     */
    @Override
    public String ler(final String policy) {
        try (final var br = new BufferedReader(
                new FileReader(
                        new File(this.theDirectory(), policy + ".java"),
                        StandardCharsets.UTF_8)
        )) {
            return br.lines().collect(Collectors.joining("\n"));
        } catch (final IOException ex) {
            Logger.getLogger(FromFilePolicyManager.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Attempts to remove .java and .class files with the name in {@code
     * alocador} and, if successful, removes the policy from the inner list.
     *
     * @param policy Name of the policy which files will be removed
     * @return {@code true} if removal is successful
     */
    @Override
    public boolean remover(final String policy) {
        final var classFile = new File(
                this.theDirectory(), policy + ".class");

        final File javaFile = new File(
                this.theDirectory(), policy + ".java");

        boolean deleted = false;

        if (classFile.exists()) {
            if (classFile.delete()) {
                this.removePolicy(policy);
                deleted = true;
            }
        }

        if (javaFile.exists()) {
            if (javaFile.delete()) {
                deleted = true;
            }
        }

        return deleted;
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

    /**
     * Adds allocation policy coded in file {@code arquivo} to the configured
     * directory, compiles it, and adds it to the list of allocation policies.
     *
     * @param arquivo Java source file containing the allocation policy
     * @return {@code true} if import occurred successfully and {@code false}
     * otherwise
     */
    @Override
    public boolean importJavaPolicy(final File arquivo) {
        final var target = new File(this.theDirectory(), arquivo.getName());
        FromFilePolicyManager.copyFile(target, arquivo);

        final var err = FromFilePolicyManager.compile(target);

        if (!err.isEmpty()) {
            return false;
        }

        final var nome = arquivo.getName()
                .substring(0, arquivo.getName().length() - ".java".length());

        if (!new File(this.theDirectory(), nome + ".class").exists()) {
            return false;
        }

        this.addPolicy(nome);

        return true;
    }

    protected static void copyFile(final File dest, final File src) {
        if (dest.getPath().equals(src.getPath())) {
            return;
        }

        try (final var srcFs = new FileInputStream(src);
             final var destFs = new FileOutputStream(dest)) {
            srcFs.transferTo(destFs);
        } catch (final IOException ex) {
            Logger.getLogger(FromFilePolicyManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
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

    protected abstract File theDirectory();

    public void findDotClassAllocators() {
        final FilenameFilter filter = (b, name) -> name.endsWith(".class");
        final var dotClassFiles =
                Objects.requireNonNull(this.theDirectory().list(filter));

        Arrays.stream(dotClassFiles)
                .map(FromFilePolicyManager::removeDotClassSuffix)
                .forEach(this.policies::add);
    }

    protected static String removeDotClassSuffix(final String s) {
        return s.substring(0, s.length() - ".class".length());
    }
}
