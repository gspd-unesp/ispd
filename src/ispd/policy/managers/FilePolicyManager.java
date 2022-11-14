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
    private static final String POLICY_NAME_REPL = "__POLICY_NAME__";
    private static final String MOTOR_PKG_NAME = "motor";
    private final ArrayList<String> policies = new ArrayList<>();
    private final List<String> addedPolicies = new ArrayList<>();
    private final List<String> removedPolicies = new ArrayList<>();

    /* package-private */
    FilePolicyManager() {
        this.initialize();
    }

    private void initialize() {
        if (this.directory().exists()) {
            this.findDotClassPolicies();
            return;
        }

        try {
            FilePolicyManager.createDirectory(this.directory());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        final var executable = this.getClass()
                .getResource(this.className());

        Objects.requireNonNull(executable);

        if (executable.toString().startsWith("jar:")) {
            FilePolicyManager.executeFromJar(this.packageName());
        }
    }

    private static void executeFromJar(final String path) {
        final var jar = new File(System.getProperty("java.class.path"));
        try {
            FilePolicyManager.extractDirFromJar(path, jar);
            FilePolicyManager.extractDirFromJar(
                    FilePolicyManager.MOTOR_PKG_NAME, jar);
        } catch (final IOException ex) {
            Logger.getLogger(FilePolicyManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    private static void extractDirFromJar(
            final String dir, final File file) throws IOException {
        try (final var jar = new JarFile(file)) {
            final var entries = jar.stream()
                    .filter(e -> e.getName().contains(dir))
                    .toList();

            for (final var entry : entries) {
                FilePolicyManager.processZipEntry(entry, jar);
            }
        }
    }

    private static void processZipEntry(
            final ZipEntry entry, final ZipFile zip) throws IOException {
        final var file = new File(entry.getName());

        if (entry.isDirectory() && !file.exists()) {
            FilePolicyManager.createDirectory(file);
            return;
        }

        if (!file.getParentFile().exists()) {
            FilePolicyManager.createDirectory(file.getParentFile());
        }

        try (final var is = zip.getInputStream(entry);
             final var os = new FileOutputStream(file)) {
            is.transferTo(os);
        }
    }

    private static void createDirectory(final File dir) throws IOException {
        if (!dir.mkdirs()) {
            throw new IOException("Failed to create directory " + dir);
        }
    }

    private void findDotClassPolicies() {
        final FilenameFilter filter = (b, name) -> name.endsWith(".class");
        final var dotClassFiles =
                Objects.requireNonNull(this.directory().list(filter));

        Arrays.stream(dotClassFiles)
                .map(FilePolicyManager::removeDotClassSuffix)
                .forEach(this.policies::add);
    }

    private static String removeDotClassSuffix(final String s) {
        return s.substring(0, s.length() - ".class".length());
    }

    protected abstract String className();

    protected abstract String packageName();

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
                this.javaFileWithName(nome),
                StandardCharsets.UTF_8
        )) {
            fw.write(codigo);
            return true;
        } catch (final IOException ex) {
            Logger.getLogger(FilePolicyManager.class.getName())
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
        final var target = this.javaFileWithName(nome);
        final var err = FilePolicyManager.compile(target);

        try {
            Thread.sleep(1000);
        } catch (final InterruptedException ex) {
            Logger.getLogger(FilePolicyManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        if (this.checkIfDotClassExists(nome)) {
            this.addPolicy(nome);
        }

        return err.isEmpty() ? null : err;
    }

    private boolean checkIfDotClassExists(final String nome) {
        return new File(this.directory(), nome + ".class").exists();
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
                        this.javaFileWithName(policy),
                        StandardCharsets.UTF_8)
        )) {
            return br.lines().collect(Collectors.joining("\n"));
        } catch (final IOException ex) {
            Logger.getLogger(FilePolicyManager.class.getName())
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
                this.directory(), policy + ".class");

        final File javaFile = this.javaFileWithName(policy);

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
        FilePolicyManager.copyFile(target, arquivo);

        final var err = FilePolicyManager.compile(target);

        if (!err.isEmpty()) {
            return false;
        }

        final var nome = arquivo.getName()
                .substring(0, arquivo.getName().length() - ".java".length());

        if (!this.checkIfDotClassExists(nome)) {
            return false;
        }

        this.addPolicy(nome);

        return true;
    }

    private static void copyFile(final File dest, final File src) {
        if (dest.getPath().equals(src.getPath())) {
            return;
        }

        try (final var srcFs = new FileInputStream(src);
             final var destFs = new FileOutputStream(dest)) {
            srcFs.transferTo(destFs);
        } catch (final IOException ex) {
            Logger.getLogger(FilePolicyManager.class.getName())
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

    private File javaFileWithName(final String name) {
        return new File(this.directory(), name + ".java");
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
                    .map(this::compileWithTool)
                    .orElseGet(this::tryCompileWithJavac);
        }

        private String compileWithTool(final Tool tool) {
            final var err = new ByteArrayOutputStream();
            final var arg = this.target.getPath();
            tool.run(null, null, err, arg);
            return err.toString();
        }

        private String tryCompileWithJavac() {
            try {
                return this.compileWithJavac();
            } catch (final IOException ex) {
                Logger.getLogger(FilePolicyManager.class.getName())
                        .log(Level.SEVERE, null, ex);
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
}
