package ispd.policy.managers;

import ispd.policy.PolicyManager;

import java.io.File;

/**
 * Manages storing, retrieving and compiling allocation policies
 */
public class Alocadores extends FilePolicyManager {
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
    private static final String VM_DIR_PATH = "ispd/externo/cloudAlloc";
    private static final File VM_DIRECTORY = new File(Alocadores.VM_DIR_PATH);

    @Override
    protected String className() {
        return "Alocadores.class";
    }

    @Override
    protected String packageName() {
        return "alocacaoVM";
    }

    @Override
    protected String getTemplate() {
        //language=JAVA
        return """
                package ispd.policy.externo;
                
                import ispd.policy.allocation.vm.VmAllocationPolicy;
                import ispd.motor.filas.servidores.CentroServico;
                import ispd.motor.filas.servidores.CS_Processamento;
                import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
                
                import java.util.List;
                
                public class __POLICY_NAME__ extends VmAllocationPolicy {
                    @Override
                    public void iniciar() {
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
                
                    @Override
                    public CS_Processamento escalonarRecurso() {
                        throw new UnsupportedOperationException("Not Implemented Yet.");
                    }
                
                    @Override
                    public CS_VirtualMac escalonarVM() {
                        throw new UnsupportedOperationException("Not Implemented Yet.");
                    }
                }
                """;
    }

    @Override
    public File directory() {
        return Alocadores.VM_DIRECTORY;
    }
}
