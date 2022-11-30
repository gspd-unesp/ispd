package ispd.policy.scheduling.grid.impl;

import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.policy.scheduling.grid.GridSchedulingPolicy;
import ispd.policy.scheduling.grid.impl.util.PreemptionEntry;
import ispd.policy.scheduling.grid.impl.util.SlaveControl;
import ispd.policy.scheduling.grid.impl.util.UserControl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class AbstractHOSEP extends GridSchedulingPolicy {
    protected static final double REFRESH_TIME = 15.0;
    protected final Map<String, UserControl> userControls = new HashMap<>();
    protected final Map<CS_Processamento, SlaveControl> slaveControls =
            new HashMap<>();
    protected final Collection<Tarefa> tasksToSchedule = new HashSet<>();
    protected final Collection<PreemptionEntry> preemptionEntries =
            new HashSet<>();
}
