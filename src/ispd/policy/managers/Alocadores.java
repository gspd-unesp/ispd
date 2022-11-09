package ispd.policy.managers;

import ispd.policy.PolicyManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Manages storing, retrieving and compiling allocation policies
 */
public class Alocadores extends FromFilePolicyManager {
    /**
     * Allocation policies available by default
     */
    public static final String[] ALOCACAO = {
            PolicyManager.NO_POLICY,
            "RoundRobin",
            "FirstFit",
            "FirstFitDecreasing",
            "Volume",
    };
    private static final String VM_PKG_NAME = "alocacaoVM";
    private static final String VM_DIR_PATH = "ispd/externo/cloudAlloc";
    private static final File VM_DIRECTORY = new File(Alocadores.VM_DIR_PATH);

    public Alocadores() {
        if (this.theDirectory().exists()) {
            this.findDotClassAllocators();
        } else {

            try {
                FromFilePolicyManager.createDirectory(this.theDirectory());
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }

            if (Objects.requireNonNull(this.getClass().getResource(
                    "Alocadores.class")).toString().startsWith("jar:")) {
                FromFilePolicyManager.executeFromJar(Alocadores.VM_PKG_NAME);
            }
        }
    }

    protected File theDirectory() {
        return Alocadores.VM_DIRECTORY;
    }

    private void findDotClassAllocators() {
        final FilenameFilter filter = (b, name) -> name.endsWith(".class");
        final var dotClassFiles =
                Objects.requireNonNull(this.theDirectory().list(filter));

        Arrays.stream(dotClassFiles)
                .map(FromFilePolicyManager::removeDotClassSuffix)
                .forEach(this.policies::add);
    }

    /**
     * @return Basic template for writing an allocation policy's source code
     */
    public static String getAlocadorJava(final String policyName) {
        return """
                package ispd.policy.externo;
                import ispd.policy.alocacaoVM.Alocacao;
                import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
                import ispd.motor.filas.servidores.CS_Processamento;
                import ispd.motor.filas.servidores.CentroServico;
                import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
                import ispd.motor.filas.servidores.implementacao.CS_VMM;
                import java.util.List;

                public abstract class %s extends Alocacao{

                    @Override
                    public void iniciar() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public CS_VirtualMac escalonarVM() {
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
                    @Override
                    public void migrarVM() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                }""".formatted(policyName);
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
            Logger.getLogger(Alocadores.class.getName())
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
            Logger.getLogger(Alocadores.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        // Check if compilation worked, looking for a .class file
        if (new File(this.theDirectory(), nome + ".class").exists()) {
            this.addPolicy(nome);
        }

        return err.isEmpty() ? null : err;
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
            Logger.getLogger(Alocadores.class.getName())
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
}
