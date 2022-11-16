package ispd.policy.managers;

import ispd.policy.PolicyManager;

import javax.tools.Tool;
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
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

// TODO: Document
/* package-private */
abstract class FilePolicyManager implements PolicyManager {
    private static final String JAR_PREFIX = "jar:";
    private static final String POLICY_NAME_REPL = "__POLICY_NAME__";
    private final ArrayList<String> policies = new ArrayList<>();
    private final List<String> addedPolicies = new ArrayList<>();
    private final List<String> removedPolicies = new ArrayList<>();

    /* package-private */
    FilePolicyManager() {
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

    private static void createDirectory(final File dir) throws IOException {
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

    private static void severeLog(final Throwable e) {
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

    private boolean checkIfDotClassExists(final String nome) {
        return this.policyDotClassFile(nome).exists();
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
     * alocador} and, if successful, removes the policy from the inner list.
     *
     * @param policy Name of the policy which files will be removed
     * @return {@code true} if removal is successful
     */
    @Override
    public boolean remover(final String policy) {
        final var classFile = this.policyDotClassFile(policy);
        final var javaFile = this.policyJavaFile(policy);

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

    private File policyJavaFile(final String name) {
        return this.fileWithExtension(name, ".java");
    }

    private File fileWithExtension(final String policyName, final String ext) {
        return new File(this.directory(), policyName + ext);
    }

    private static class CompilationHelper {
        private final Optional<Tool> compiler = Optional.ofNullable(
                ToolProvider.getSystemJavaCompiler());
        private final File target;

        private CompilationHelper(final File target) {
            this.target = target;
        }

        private String compile() {
            return this.compiler
                    .map(this::compileWithSystemTool)
                    .orElseGet(this::tryCompileWithJavac);
        }

        private String compileWithSystemTool(final Tool tool) {
            final var err = new ByteArrayOutputStream();
            final var arg = this.target.getPath();
            tool.run(null, null, err, arg);
            return err.toString();
        }

        private String tryCompileWithJavac() {
            try {
                return this.compileWithJavac();
            } catch (final IOException ex) {
                FilePolicyManager.severeLog(ex);
                return "Não foi possível compilar";
            }
        }

        private String compileWithJavac() throws IOException {
            final var command = "javac %s".formatted(this.target.getPath());
            final var process = Runtime.getRuntime().exec(command);

            try (final var err = new BufferedReader(new InputStreamReader(
                    process.getErrorStream(), StandardCharsets.UTF_8
            ))) {
                return err.lines().collect(Collectors.joining("\n"));
            }
        }
    }

    private static class JarExtractor {
        private static final String MOTOR_PKG_PATH = "motor";
        private final ZipFile jar = new JarFile(new File(
                System.getProperty("java.class.path")
        ));
        private final String targetPackage;

        private JarExtractor(final String targetPackage) throws IOException {
            this.targetPackage = targetPackage;
        }

        private void extractDirsFromJar() throws IOException {
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
}
