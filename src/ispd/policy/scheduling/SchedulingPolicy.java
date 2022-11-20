package ispd.policy.scheduling;

import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.metricas.MetricasUsuarios;
import ispd.policy.Policy;

import java.util.List;

public abstract class SchedulingPolicy <T extends SchedulingMaster> implements Policy<T> {
    protected List<List> filaEscravo = null;
    protected List<CS_Processamento> escravos = null;
    protected List<Tarefa> tarefas = null;
    protected MetricasUsuarios metricaUsuarios = null;
    protected List<List> caminhoEscravo = null;
    protected T mestre = null;

    public abstract Tarefa escalonarTarefa();
}
