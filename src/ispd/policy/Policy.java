package ispd.policy;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;

import java.util.List;

public interface Policy <T extends PolicyMaster> {
    void iniciar();

    List<CentroServico> escalonarRota(CentroServico destino);

    void escalonar();

    CS_Processamento escalonarRecurso();

    default Double getTempoAtualizar() {
        return null;
    }
}
