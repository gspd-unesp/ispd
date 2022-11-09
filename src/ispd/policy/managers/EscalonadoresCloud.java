package ispd.policy.managers;

import ispd.policy.PolicyManager;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * Manages storing, retrieving and compiling cloud scheduling policies
 */
public class EscalonadoresCloud extends FromFilePolicyManager {
    public static final String[] ESCALONADORES = {
            PolicyManager.NO_POLICY,
            "RoundRobin"
    };
    private static final String CLOUD_PKG_NAME = "escalonadorCloud";
    private static final String CLOUD_DIR_PATH =
            "ispd.policy.externo.cloudSchedulers";
    private static final File CLOUD_DIRECTORY =
            new File(EscalonadoresCloud.CLOUD_DIR_PATH);

    public EscalonadoresCloud() {
        if (this.getDiretorio().exists()) {
            this.findDotClassPolicies();
        } else {

            try {
                FromFilePolicyManager.createDirectory(this.getDiretorio());
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }

            if (Objects.requireNonNull(this.getClass().getResource(
                    "EscalonadoresCloud.class")).toString().startsWith("jar:")) {
                FromFilePolicyManager.executeFromJar(EscalonadoresCloud.CLOUD_PKG_NAME);
            }
        }
    }

    @Override
    public File getDiretorio() {
        return EscalonadoresCloud.CLOUD_DIRECTORY;
    }

    /**
     * @return Basic template for writing a cloud scheduling policy's source
     * code
     */
    public static String getEscalonadorJava(final String escalonador) {
        return FromFilePolicyManager.formatTemplate(
                EscalonadoresCloud.getTemplate(),
                escalonador
        );
    }

    private static String getTemplate() {
        //language=JAVA
        return """
                package ispd.policy.externo;
                                
                import ispd.policy.escalonadorCloud.EscalonadorCloud;
                import ispd.motor.filas.Tarefa;
                import ispd.motor.filas.servidores.CS_Processamento;
                import ispd.motor.filas.servidores.CentroServico;
                                
                import java.util.List;
                                
                public class __POLICY_NAME__ extends EscalonadorCloud {
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
