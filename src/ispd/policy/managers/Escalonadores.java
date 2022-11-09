package ispd.policy.managers;

import ispd.policy.PolicyManager;
import ispd.policy.escalonador.Carregar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
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
public class Escalonadores extends FromFilePolicyManager {
    /**
     * Scheduling policies available by default
     */
    public static final String[] ESCALONADORES = {
            PolicyManager.NO_POLICY,
            "RoundRobin",
            "Workqueue",
            "WQR",
            "DynamicFPLTF",
            "HOSEP",
            "OSEP",
            "EHOSEP",
    };
    private static final String GRID_PKG_NAME = "escalonador";
    private static final String GRID_DIR_PATH = "ispd/externo";
    private static final File GRID_DIRECTORY =
            new File(Carregar.DIRETORIO_ISPD, Escalonadores.GRID_DIR_PATH);

    public Escalonadores() {
        if (this.theDirectory().exists()) {
            this.findDotClassSchedulers();
        } else {

            try {
                FromFilePolicyManager.createDirectory(this.theDirectory());
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }

            if (Objects.requireNonNull(this.getClass().getResource(
                    "Escalonadores.class")).toString().startsWith("jar:")) {
                FromFilePolicyManager.executeFromJar(Escalonadores.GRID_PKG_NAME);
            }
        }
    }

    protected File theDirectory() {
        return Escalonadores.GRID_DIRECTORY;
    }

    private void findDotClassSchedulers() {
        final FilenameFilter filter = (b, name) -> name.endsWith(".class");
        final var dotClassFiles =
                Objects.requireNonNull(this.theDirectory().list(filter));

        Arrays.stream(dotClassFiles)
                .map(FromFilePolicyManager::removeDotClassSuffix)
                .forEach(this.policies::add);
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
        final var target = new File(this.theDirectory(), nome +
                                                         ".java");
        final var err = FromFilePolicyManager.compile(target);

        try {
            Thread.sleep(1000);
        } catch (final InterruptedException ex) {
            Logger.getLogger(Escalonadores.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        // Check if compilation worked, looking for a .class file
        if (new File(this.theDirectory(), nome + ".class").exists()) {
            this.addPolicy(nome);
        }

        return err.isEmpty() ? null : err;
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
                        new File(this.theDirectory(), policy +
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
}
