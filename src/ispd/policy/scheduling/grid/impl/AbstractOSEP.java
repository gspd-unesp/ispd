package ispd.policy.scheduling.grid.impl;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.policy.PolicyConditions;
import ispd.policy.scheduling.grid.GridSchedulingPolicy;
import ispd.policy.scheduling.grid.impl.util.SlaveControl;
import ispd.policy.scheduling.grid.impl.util.UserProcessingControl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractOSEP <T extends UserProcessingControl> extends GridSchedulingPolicy {
    private static final double REFRESH_TIME = 15.0;
    protected final Map<CS_Processamento, SlaveControl> slaveControls =
            new HashMap<>();
    protected final Map<String, T> userControls = new HashMap<>();

    public AbstractOSEP() {
        this.tarefas = new ArrayList<>();
        this.escravos = new ArrayList<>();
        this.filaEscravo = new ArrayList<>();
    }

    @Override
    public void iniciar() {
        this.mestre.setSchedulingConditions(PolicyConditions.ALL);

        for (final var userId : this.metricaUsuarios.getUsuarios()) {
            final var uc = this.makeUserControlFor(userId);
            this.userControls.put(userId, uc);
        }

        for (final var slave : this.escravos) {
            this.slaveControls.put(slave, new SlaveControl());
        }
    }

    @Override
    public List<CentroServico> escalonarRota(final CentroServico destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<CentroServico>) this.caminhoEscravo.get(index));
    }

    @Override
    public Double getTempoAtualizar() {
        return AbstractOSEP.REFRESH_TIME;
    }

    protected T makeUserControlFor(final String userId) {
        return (T) new UserProcessingControl(userId, this.escravos);
    }
}
