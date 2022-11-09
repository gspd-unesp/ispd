package ispd.policy.managers;

import ispd.policy.PolicyManager;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

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

    @Override
    protected File theDirectory() {
        return Alocadores.VM_DIRECTORY;
    }

    /**
     * @return Basic template for writing an allocation policy's source code
     */
    public static String getAlocadorJava(final String policyName) {
        return FromFilePolicyManager.formatTemplate(
                Alocadores.getTemplate(),
                policyName
        );
    }

    private static String getTemplate() {
        return """
                package ispd.policy.externo;
                                
                import ispd.policy.alocacaoVM.Alocacao;
                import ispd.motor.filas.servidores.CentroServico;
                import ispd.motor.filas.servidores.CS_Processamento;
                import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
                                
                import java.util.List;
                                
                public class __POLICY_NAME__ extends Alocacao {
                                
                    @Override
                    public void iniciar() {
                        throw new UnsupportedOperationException("Not Implemented Yet.");
                    }
                                
                    @Override
                    public CS_VirtualMac escalonarVM() {
                        throw new UnsupportedOperationException("Not Implemented Yet.");
                    }
                                
                    @Override
                    public CS_Processamento escalonarRecurso() {
                        throw new UnsupportedOperationException("Not Implemented Yet.");
                    }
                                
                    @Override
                    public List<CentroServico> escalonarRota(final CentroServico destino) {
                        throw new UnsupportedOperationException("Not Implemented Yet.");
                    }
                                
                    @Override
                    public void escalonar() {
                        throw new UnsupportedOperationException("Not Implemented Yet.");
                    }
                }""";
    }
}
