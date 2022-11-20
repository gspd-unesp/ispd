package ispd.policy.managers;

import ispd.policy.PolicyManager;

import java.io.File;

/**
 * Manages storing, retrieving and compiling cloud scheduling policies
 */
public class EscalonadoresCloud extends FilePolicyManager {
    public static final String[] ESCALONADORES = {
            PolicyManager.NO_POLICY,
            "RoundRobin"
    };
    private static final String CLOUD_DIR_PATH =
            "ispd.policy.externo.cloudSchedulers";
    private static final File CLOUD_DIRECTORY =
            new File(EscalonadoresCloud.CLOUD_DIR_PATH);

    @Override
    public File directory() {
        return EscalonadoresCloud.CLOUD_DIRECTORY;
    }

    @Override
    protected String className() {
        return "EscalonadoresCloud.class";
    }

    @Override
    protected String packageName() {
        return "escalonadorCloud";
    }

    protected String getTemplate() {
        //language=JAVA
        return """
                package ispd.policy.externo;
                
                import ispd.policy.scheduling.cloud.CloudSchedulingPolicy;
                import ispd.motor.filas.Tarefa;
                import ispd.motor.filas.servidores.CS_Processamento;
                import ispd.motor.filas.servidores.CentroServico;
                
                import java.util.List;
                
                public class __POLICY_NAME__ extends CloudSchedulingPolicy {
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
