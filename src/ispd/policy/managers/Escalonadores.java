package ispd.policy.managers;

import ispd.policy.PolicyManager;
import ispd.policy.escalonador.Carregar;

import java.io.File;

/**
 * Manages storing, retrieving and compiling scheduling policies
 */
public class Escalonadores extends FilePolicyManager {
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
    private static final String GRID_DIR_PATH = "ispd/externo";
    private static final File GRID_DIRECTORY =
            new File(Carregar.DIRETORIO_ISPD, Escalonadores.GRID_DIR_PATH);

    @Override
    public File directory() {
        return Escalonadores.GRID_DIRECTORY;
    }

    @Override
    protected String className() {
        return "Escalonadores.class";
    }

    @Override
    protected String packageName() {
        return "escalonador";
    }

    protected String getTemplate() {
        //language=JAVA
        return """
                package ispd.policy.externo;
                                
                import ispd.policy.escalonador.Escalonador;
                import ispd.motor.filas.Tarefa;
                import ispd.motor.filas.servidores.CS_Processamento;
                import ispd.motor.filas.servidores.CentroServico;
                                
                import java.util.List;
                                
                public class __POLICY_NAME__ extends Escalonador {
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
                    public List<CentroServico> escalonarRota(final CentroServico destino) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                                
                    @Override
                    public void escalonar() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                }
                """;
    }
}
