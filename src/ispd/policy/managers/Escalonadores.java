package ispd.policy.managers;

import ispd.policy.PolicyManager;
import ispd.policy.escalonador.Carregar;

import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Manages storing, retrieving and compiling scheduling policies
 */
public class Escalonadores extends GenericPolicyManager {
    /**
     * Scheduling policies available by default
     */
    public static final String[] ESCALONADORES = { PolicyManager.NO_POLICY,
            "RoundRobin", "Workqueue", "WQR",
            "DynamicFPLTF", "HOSEP", "OSEP", "EHOSEP" };
    private static final String DIRECTORY_PATH = "ispd/externo";
    private static final File DIRECTORY =
            new File(Carregar.DIRETORIO_ISPD, Escalonadores.DIRECTORY_PATH);

    public Escalonadores() {
        if (Escalonadores.DIRECTORY.exists()) {
            this.findDotClassSchedulers();
        } else {

            try {
                GenericPolicyManager.createDirectory(Escalonadores.DIRECTORY);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }

            if (Objects.requireNonNull(this.getClass().getResource(
                    "Escalonadores.class")).toString().startsWith("jar:")) {
                Escalonadores.executeFromJar();
            }
        }
    }

    private void findDotClassSchedulers() {
        final FilenameFilter filter = (b, name) -> name.endsWith(".class");
        final var dotClassFiles =
                Objects.requireNonNull(Escalonadores.DIRECTORY.list(filter));

        Arrays.stream(dotClassFiles)
                .map(GenericPolicyManager::removeDotClassSuffix)
                .forEach(this.policies::add);
    }

    private static void executeFromJar() {
        final File jar = new File(
                System.getProperty("java.class.path"));

        try {
            GenericPolicyManager.extractDirFromJar("escalonador", jar);
            GenericPolicyManager.extractDirFromJar("motor", jar);
        } catch (final IOException ex) {
            Logger.getLogger(Escalonadores.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return Basic template for writing a scheduling policy's source code
     */
    public static String getEscalonadorJava(final String policyName) {
        return """
                package ispd.policy.externo;
                import ispd.policy.escalonador.Escalonador;
                import ispd.motor.filas.Tarefa;
                import ispd.motor.filas.servidores.CS_Processamento;
                import ispd.motor.filas.servidores.CentroServico;
                import java.util.ArrayList;
                import java.util.List;

                public class %s extends Escalonador{

                    @Override
                    public void iniciar() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public Tarefa escalonarTarefa() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public CS_Processamento escalonarRecurso() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public List<CentroServico> escalonarRota(CentroServico destino) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void escalonar() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                }""".formatted(policyName);
    }

    /**
     * Lists all available scheduling policies.
     *
     * @return {@code ArrayList} with all scheduling policies' names.
     */
    @Override
    public ArrayList<String> listar() {
        return this.policies;
    }

    /**
     * @return Directory in which scheduling policies sources are compiled
     * classes are saved
     */
    @Override
    public File getDiretorio() {
        return Escalonadores.DIRECTORY;
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
                new File(Escalonadores.DIRECTORY, nome + ".java"),
                StandardCharsets.UTF_8
        )) {
            fw.write(codigo);
            return true;
        } catch (final IOException ex) {
            Logger.getLogger(Escalonadores.class.getName())
                    .log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Attemps to compile the source file of the policy with given name,
     * returning the contents of {@code stderr}, if any, otherwise {@code null}
     *
     * @param nome Name of the allocation policy to be compiled
     * @return A string with errors, if any, otherwise {@code null}
     */
    @Override
    public String compilar(final String nome) {
        final var target = new File(Escalonadores.DIRECTORY, nome + ".java");
        final var err = Escalonadores.compile(target);

        try {
            Thread.sleep(1000);
        } catch (final InterruptedException ex) {
            Logger.getLogger(Escalonadores.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        // Check if compilation worked, looking for a .class file
        if (new File(Escalonadores.DIRECTORY, nome + ".class").exists()) {
            this.addPolicy(nome);
        }

        return err.isEmpty() ? null : err;
    }

    private static String compile(final File target) {
        final var compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler != null) {
            final var err = new ByteArrayOutputStream();
            compiler.run(null, null, err, target.getPath());
            return err.toString();
        } else {
            try {
                return Escalonadores.compileManually(target);
            } catch (final IOException ex) {
                Logger.getLogger(Escalonadores.class.getName())
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
     * Reads the source file from the policy {@code escalonador} and returns
     * a string with the file contents.
     *
     * @param policy Name of the policy which source file will be read
     * @return String contents of the file
     */
    @Override
    public String ler(final String policy) {
        try (final var br = new BufferedReader(
                new FileReader(
                        new File(Escalonadores.DIRECTORY, policy +
                                                          ".java"),
                        StandardCharsets.UTF_8)
        )) {
            return br.lines().collect(Collectors.joining("\n"));
        } catch (final IOException ex) {
            Logger.getLogger(Escalonadores.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Attempts to remove .java and .class files with the name in {@code
     * escalonador} and, if successful, remove the policy from the inner list.
     *
     * @param policy Name of the policy which files will be removed
     * @return {@code true} if removal is successful
     */
    @Override
    public boolean remover(final String policy) {
        final var classFile = new File(
                Escalonadores.DIRECTORY, policy + ".class");

        final File javaFile = new File(
                Escalonadores.DIRECTORY, policy + ".java");

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
     * Adds scheduling policy coded in file {@code arquivo} to the configured
     * directory, compiles it, and adds it to the inner list of scheduling
     * policies.
     *
     * @param arquivo Java source file containing the allocation policy
     * @return {@code true} if import occurred successfully and {@code false}
     * otherwise
     */
    @Override
    public boolean importJavaPolicy(final File arquivo) {
        final var target = new File(Escalonadores.DIRECTORY, arquivo.getName());
        GenericPolicyManager.copyFile(target, arquivo);

        final var err = Escalonadores.compile(target);

        if (!err.isEmpty()) {
            return false;
        }

        final var nome = arquivo.getName()
                .substring(0, arquivo.getName().length() - ".java".length());

        if (!new File(Escalonadores.DIRECTORY, nome + ".class").exists()) {
            return false;
        }

        this.addPolicy(nome);

        return true;
    }
}
