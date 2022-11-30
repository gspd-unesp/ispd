package ispd.policy.scheduling.grid.impl;

import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.policy.PolicyConditions;
import ispd.policy.scheduling.grid.GridMaster;
import ispd.policy.scheduling.grid.GridSchedulingPolicy;
import ispd.policy.scheduling.grid.impl.util.PreemptionEntry;
import ispd.policy.scheduling.grid.impl.util.SlaveControl;
import ispd.policy.scheduling.grid.impl.util.UserControl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Predicate;

public abstract class AbstractHOSEP extends GridSchedulingPolicy {
    protected static final double REFRESH_TIME = 15.0;
    protected final Map<String, UserControl> userControls = new HashMap<>();
    protected final Map<CS_Processamento, SlaveControl> slaveControls =
            new HashMap<>();
    protected final Collection<Tarefa> tasksToSchedule = new HashSet<>();
    protected final Collection<PreemptionEntry> preemptionEntries =
            new HashSet<>();

    @Override
    public void iniciar() {
        this.mestre.setSchedulingConditions(PolicyConditions.ALL);

        final var nonMasters = this.escravos.stream()
                .filter(Predicate.not(GridMaster.class::isInstance))
                .toList();

        for (final var userId : this.metricaUsuarios.getUsuarios()) {
            final var userOwnedMachines = nonMasters.stream()
                    .filter(machine -> userId.equals(machine.getProprietario()))
                    .toList();

            final var uc = this.makeUserControlFor(userId, userOwnedMachines);
            this.userControls.put(userId, uc);
        }

        for (final var s : this.escravos)
            this.slaveControls.put(s, new SlaveControl());
    }

    protected UserControl makeUserControlFor(
            final String userId,
            final Collection<? extends CS_Processamento> userOwnedMachines) {
        final double compPower = userOwnedMachines.stream()
                .mapToDouble(CS_Processamento::getPoderComputacional)
                .sum();

        return new UserControl(userId, compPower, this.escravos);
    }
}
