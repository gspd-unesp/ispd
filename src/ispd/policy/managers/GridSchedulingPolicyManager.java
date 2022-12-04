package ispd.policy.managers;

import ispd.arquivo.xml.ConfiguracaoISPD;
import ispd.policy.PolicyManager;

import java.io.File;
import java.util.List;

/**
 * Manages storing, retrieving and compiling scheduling policies
 */
public class GridSchedulingPolicyManager extends FilePolicyManager {
    /**
     * Scheduling policies available by default
     */
    public static final List<String> NATIVE_POLICIES = List.of(
            PolicyManager.NO_POLICY,
            "RoundRobin",
            "Workqueue",
            "WQR",
            "DynamicFPLTF",
            "HOSEP",
            "OSEP",
            "EHOSEP"
    );
    private static final String GRID_DIR_PATH =
            String.join(File.separator, "policies", "scheduling", "grid");
    private static final File GRID_DIRECTORY =
            new File(ConfiguracaoISPD.DIRETORIO_ISPD,
                    GridSchedulingPolicyManager.GRID_DIR_PATH);

    @Override
    public File directory() {
        return GridSchedulingPolicyManager.GRID_DIRECTORY;
    }

    @Override
    protected String className() {
        return "GridSchedulingPolicyManager.class";
    }

    @Override
    protected String packageName() {
        return "escalonador";
    }

    protected String getTemplate() {
        //language=JAVA
        return """
                package ispd.policy.externo;
                                
                import ispd.motor.filas.Tarefa;
                import ispd.motor.filas.servidores.CS_Processamento;
                import ispd.motor.filas.servidores.CentroServico;
                import ispd.policy.scheduling.grid.GridSchedulingPolicy;
                                
                import java.util.List;
                                
                public class __POLICY_NAME__ extends GridSchedulingPolicy {
                    @Override
                    public void iniciar() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                                
                    @Override
                    public List<CentroServico> escalonarRota(final CentroServico destino) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                                
                    @Override
                    public void escalonar() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                                
                    @Override
                    public CS_Processamento escalonarRecurso() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                                
                    @Override
                    public Tarefa escalonarTarefa() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                }
                """;
    }
}
