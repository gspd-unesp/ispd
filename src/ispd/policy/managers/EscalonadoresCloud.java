package ispd.policy.managers;

import ispd.policy.PolicyManager;

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
 * Manages storing, retrieving and compiling cloud scheduling policies
 */
public class EscalonadoresCloud extends GenericPolicyManager {
    public static final String[] ESCALONADORES = {
            PolicyManager.NO_POLICY, "RoundRobin" };
    private static final String DIRECTORY_PATH =
            "ispd.policy.externo.cloudSchedulers";
    private static final File DIRECTORY =
            new File(EscalonadoresCloud.DIRECTORY_PATH);
    private static final String PKG_NAME = "escalonadorCloud";

    public EscalonadoresCloud() {
        if (EscalonadoresCloud.DIRECTORY.exists()) {
            this.findDotClassAllocators();
        } else {

            try {
                GenericPolicyManager.createDirectory(EscalonadoresCloud.DIRECTORY);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }

            if (Objects.requireNonNull(this.getClass().getResource(
                    "EscalonadoresCloud.class")).toString().startsWith("jar:")) {
                GenericPolicyManager.executeFromJar(EscalonadoresCloud.PKG_NAME);
            }
        }
    }

    private void findDotClassAllocators() {
        final FilenameFilter filter = (b, name) -> name.endsWith(".class");
        final var dotClassFiles =
                Objects.requireNonNull(EscalonadoresCloud.DIRECTORY.list(filter));

        Arrays.stream(dotClassFiles)
                .map(GenericPolicyManager::removeDotClassSuffix)
                .forEach(this.policies::add);
    }

    /**
     * @return Basic template for writing a cloud scheduling policy's source
     * code
     */
    public static String getEscalonadorJava(final String escalonador) {
        return """
                package ispd.policy.externo;
                import ispd.policy.escalonadorCloud.EscalonadorCloud;
                import ispd.motor.filas.Tarefa;
                import ispd.motor.filas.servidores.CS_Processamento;
                import ispd.motor.filas.servidores.CentroServico;
                import java.util.ArrayList;
                import java.util.List;

                public class %s extends EscalonadorCloud{

                    @Override
                    public void iniciar() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public Tarefa escalonarTarefa() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public CS_Processamento escalonarRecurso(String usuario) {
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

                }""".formatted(escalonador);
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
     * and classes are saved
     */
    @Override
    public File getDiretorio() {
        return EscalonadoresCloud.DIRECTORY;
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
                new File(EscalonadoresCloud.DIRECTORY, nome + ".java"),
                StandardCharsets.UTF_8
        )) {
            fw.write(codigo);
            return true;
        } catch (final IOException ex) {
            Logger.getLogger(EscalonadoresCloud.class.getName())
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
        final var target = new File(EscalonadoresCloud.DIRECTORY, nome +
                                                                  ".java");
        final var err = GenericPolicyManager.compile(target);

        try {
            Thread.sleep(1000);
        } catch (final InterruptedException ex) {
            Logger.getLogger(EscalonadoresCloud.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        // Check if compilation worked, looking for a .class file
        if (new File(EscalonadoresCloud.DIRECTORY, nome + ".class").exists()) {
            this.addPolicy(nome);
        }

        return err.isEmpty() ? null : err;
    }

    /**
     * Reads the source file from the policy {@code escalonador} and returns a
     * string with the file contents.
     *
     * @param escalonador Name of the policy which source file will be read
     * @return String contents of the file
     */
    @Override
    public String ler(final String escalonador) {
        try (final var br = new BufferedReader(
                new FileReader(
                        new File(EscalonadoresCloud.DIRECTORY,
                                escalonador + ".java"),
                        StandardCharsets.UTF_8)
        )) {
            return br.lines().collect(Collectors.joining("\n"));
        } catch (final IOException ex) {
            Logger.getLogger(EscalonadoresCloud.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Attempts to remove .java and .class files with the name in {@code
     * escalonador} and, if successful, removes the policy from the inner list.
     *
     * @param escalonador Name of the policy which files will be removed
     * @return {@code true} if removal is successful
     */
    @Override
    public boolean remover(final String escalonador) {
        final var classFile = new File(
                EscalonadoresCloud.DIRECTORY, escalonador + ".class");

        final File javaFile = new File(
                EscalonadoresCloud.DIRECTORY, escalonador + ".java");

        boolean deleted = false;

        if (classFile.exists()) {
            if (classFile.delete()) {
                this.removePolicy(escalonador);
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
     * Adds allocation policy coded in file {@code arquivo} to the configured
     * directory, compiles it, and adds it to the list of allocation policies.
     *
     * @param arquivo Java source file containing the allocation policy
     * @return {@code true} if import occurred successfully and {@code false}
     * otherwise
     */
    @Override
    public boolean importJavaPolicy(final File arquivo) {
        final var target = new File(
                EscalonadoresCloud.DIRECTORY, arquivo.getName());
        GenericPolicyManager.copyFile(target, arquivo);

        final var err = GenericPolicyManager.compile(target);

        if (!err.isEmpty()) {
            return false;
        }

        final var nome = arquivo.getName()
                .substring(0, arquivo.getName().length() - ".java".length());

        if (!new File(EscalonadoresCloud.DIRECTORY, nome + ".class").exists()) {
            return false;
        }

        this.addPolicy(nome);

        return true;
    }
}
