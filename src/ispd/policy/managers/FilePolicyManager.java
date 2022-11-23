package ispd.policy.managers;

import ispd.policy.PolicyManager;
import ispd.policy.managers.util.CompilationHelper;
import ispd.policy.managers.util.JarExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class FilePolicyManager implements PolicyManager {
    private static final String JAR_PREFIX = "jar:";
    private static final String POLICY_NAME_REPL = "__POLICY_NAME__";
    private final ArrayList<String> policies = new ArrayList<>();
    private final List<String> addedPolicies = new ArrayList<>();
    private final List<String> removedPolicies = new ArrayList<>();

    public FilePolicyManager() {
        this.initialize();
    }

    private void initialize() {
        if (this.directory().exists()) {
            this.loadPoliciesFromFoundDotClassFiles();
            return;
        }

        try {
            FilePolicyManager.createDirectory(this.directory());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        if (this.isExecutingFromJar()) {
            try {
                new JarExtractor(this.packageName()).extractDirsFromJar();
            } catch (final IOException e) {
                FilePolicyManager.severeLog(e);
            }
        }
    }

    private boolean isExecutingFromJar() {
        return this.getExecutableName().startsWith(FilePolicyManager.JAR_PREFIX);
    }

    private String getExecutableName() {
        return Objects.requireNonNull(
                this.getClass().getResource(this.className())
        ).toString();
    }

    protected abstract String className();

    /* package-private */
    public static void createDirectory(final File dir) throws IOException {
        if (!dir.mkdirs()) {
            throw new IOException("Failed to create directory " + dir);
        }
    }

    private void loadPoliciesFromFoundDotClassFiles() {
        final FilenameFilter f = (b, name) -> name.endsWith(".class");

        /*
         * {@link File#list()} returns {@code null} on I/O error
         * (or if the given {@link File} is not a directory that exists,
         * but that situation has already been accounted for).
         */
        final var dotClassFiles =
                Objects.requireNonNull(this.directory().list(f));

        Arrays.stream(dotClassFiles)
                .map(FilePolicyManager::removeDotClassSuffix)
                .forEach(this.policies::add);
    }

    private static String removeDotClassSuffix(final String s) {
        return FilePolicyManager.removeSuffix(s, ".class");
    }

    private static String removeSuffix(final String str, final String suffix) {
        return str.substring(0, str.length() - suffix.length());
    }

    protected abstract String packageName();

    /* package-private */
    public static void severeLog(final Throwable e) {
        Logger.getLogger(FilePolicyManager.class.getName())
                .log(Level.SEVERE, null, e);
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
     * @return Basic template for writing a policy's source code
     */
    @Override
    public String getPolicyTemplate(final String policyName) {
        return this.getTemplate().replace(
                FilePolicyManager.POLICY_NAME_REPL,
                policyName
        );
    }

    protected abstract String getTemplate();

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
                this.policyJavaFile(nome),
                StandardCharsets.UTF_8
        )) {
            fw.write(codigo);
            return true;
        } catch (final IOException ex) {
            FilePolicyManager.severeLog(ex);
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
        final var target = this.policyJavaFile(nome);
        final var err = FilePolicyManager.compile(target);

        try {
            /*
              {@link Runtime#exec(String)} runs on a <i>separate</i>
              process, so we need to wait for potential compilation time.<br>
              However, this solution is <b>bad</b> because the compilation
              may easily take longer than {@code 1000} milliseconds
              (complicated target files or low-end systems running the
              application).<br>
              Thus, we need to look for an alternative in the future.
             */
            Thread.sleep(1000);
        } catch (final InterruptedException ex) {
            FilePolicyManager.severeLog(ex);
        }

        if (this.checkIfDotClassExists(nome)) {
            this.addPolicy(nome);
        }

        return err.isEmpty() ? null : err;
    }

    private boolean checkIfDotClassExists(final String policyName) {
        return this.policyDotClassFile(policyName).exists();
    }

    private File policyDotClassFile(final String policyName) {
        return this.fileWithExtension(policyName, ".class");
    }

    private static String compile(final File target) {
        return new CompilationHelper(target).compile();
    }

    private void addPolicy(final String policyName) {
        if (this.policies.contains(policyName)) {
            return;
        }

        this.policies.add(policyName);
        this.addedPolicies.add(policyName);
    }

    /**
     * Reads the source file from the policy {@code policy} and returns a
     * string with the file contents.
     *
     * @param policy Name of the policy which source file will be read
     * @return String contents of the file
     */
    @Override
    public String ler(final String policy) {
        try (final var br = new BufferedReader(
                new FileReader(
                        this.policyJavaFile(policy),
                        StandardCharsets.UTF_8)
        )) {
            return br.lines().collect(Collectors.joining("\n"));
        } catch (final IOException ex) {
            FilePolicyManager.severeLog(ex);
            return null;
        }
    }

    /**
     * Attempts to remove .java and .class files with the name in {@code
     * policy} and, if successful, removes the policy from the inner list.
     *
     * @param policy Name of the policy which files will be removed
     * @return {@code true} if removal is successful
     */
    @Override
    public boolean remover(final String policy) {
        // TODO: This logic is sus

        boolean deleted = FilePolicyManager
                .canDeleteFile(this.policyDotClassFile(policy));

        if (deleted) {
            this.removePolicy(policy);
        }

        deleted = deleted || FilePolicyManager
                .canDeleteFile(this.policyJavaFile(policy));

        return deleted;
    }

    private static boolean canDeleteFile(final File classFile) {
        return classFile.exists() && classFile.delete();
    }

    private void removePolicy(final String policyName) {
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
        // TODO: Merge this and static method compile into one
        final var target = new File(this.directory(), arquivo.getName());
        FilePolicyManager.transferFileContents(arquivo, target);

        final var err = FilePolicyManager.compile(target);

        if (!err.isEmpty()) {
            return false;
        }

        final var policyName = FilePolicyManager
                .removeSuffix(arquivo.getName(), ".java");

        if (!this.checkIfDotClassExists(policyName)) {
            return false;
        }

        this.addPolicy(policyName);

        return true;
    }

    private static void transferFileContents(final File src, final File dest) {
        if (src.getPath().equals(dest.getPath())) {
            return;
        }

        try (final var srcFs = new FileInputStream(src);
             final var destFs = new FileOutputStream(dest)) {
            srcFs.transferTo(destFs);
        } catch (final IOException ex) {
            FilePolicyManager.severeLog(ex);
        }
    }

    @Override
    public List listarAdicionados() {
        return this.addedPolicies;
    }

    @Override
    public List listarRemovidos() {
        return this.removedPolicies;
    }

    private File policyJavaFile(final String name) {
        return this.fileWithExtension(name, ".java");
    }

    private File fileWithExtension(final String policyName, final String ext) {
        return new File(this.directory(), policyName + ext);
    }
}
