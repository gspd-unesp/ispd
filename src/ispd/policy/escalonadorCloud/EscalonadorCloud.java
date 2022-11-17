package ispd.policy.escalonadorCloud;

import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.motor.metricas.MetricasUsuarios;

import java.util.LinkedList;
import java.util.List;

public abstract class EscalonadorCloud {

    protected List<CS_Processamento> escravos;
    protected List<CS_Processamento> maqFisicas;
    protected List<List> filaEscravo;
    protected List<Tarefa> tarefas;
    protected MetricasUsuarios metricaUsuarios;
    protected MestreCloud mestre;
    /**
     * Armazena os caminhos possiveis para alcançar cada escravo
     */
    protected List<List> caminhoEscravo;
    protected List<List> caminhoMaquinas;


    public abstract void iniciar();

    public abstract Tarefa escalonarTarefa();

    public abstract CS_Processamento escalonarRecurso();

    public abstract List<CentroServico> escalonarRota(CentroServico destino);

    public abstract void escalonar();

    public void adicionarTarefa(final Tarefa tarefa) {
        if (tarefa.getOrigem().equals(this.mestre)) {
            this.metricaUsuarios.incTarefasSubmetidas(tarefa);
            System.out.println("Submeter a metrica de usuários");
        }
        this.tarefas.add(tarefa);
    }

    public List<CS_Processamento> getEscravos() {
        return escravos;
        //System.out.println ("Retorna escravos");
    }

    public void setCaminhoEscravo(List<List> caminhoEscravo) {
        this.caminhoEscravo = caminhoEscravo;
    }

    public void addEscravo(final CS_Processamento vm) {
        this.escravos.add(vm);
    }

    public void addTarefaConcluida(final Tarefa tarefa) {
        if (tarefa.getOrigem().equals(this.mestre)) {
            this.metricaUsuarios.incTarefasConcluidas(tarefa);
        }
    }

    public List<Tarefa> getFilaTarefas() {
        return this.tarefas;
    }

    public MetricasUsuarios getMetricaUsuarios() {
        return this.metricaUsuarios;
    }

    public void setMetricaUsuarios(final MetricasUsuarios metricaUsuarios) {
        this.metricaUsuarios = metricaUsuarios;
    }

    public void setMestre(final MestreCloud mestre) {
        this.mestre = mestre;
    }

    public List<CS_Processamento> getMaqFisicas() {
        return maqFisicas;
    }

    public void setMaqFisicas(List<CS_Processamento> maqFisicas) {
        this.maqFisicas = maqFisicas;
    }

    public List<List> getCaminhoMaquinas() {
        return caminhoMaquinas;
    }

    public void setCaminhoMaquinas(List<List> caminhoMaquinas) {
        this.caminhoMaquinas = caminhoMaquinas;
    }





    public List<List> getCaminhoEscravo() {
        return caminhoEscravo;
    }

    protected List<CS_Processamento> getVMsAdequadas(
            final String usuario,
            final List<? extends CS_Processamento> slaves) {
        final var escravosUsuario = new LinkedList<CS_Processamento>();
        for (final var slave : slaves) {
            final var slaveVM = (CS_VirtualMac) slave;

            if (slave.getProprietario().equals(usuario) && slaveVM.getStatus() == CS_VirtualMac.ALOCADA) {
                escravosUsuario.add(slave);
            }
        }
        return escravosUsuario;
    }

    /**
     * Indica o intervalo de tempo utilizado pelo escalonador para realizar
     * atualização dos dados dos escravos Retornar null para escalonadores
     * estáticos, nos dinâmicos o método deve ser reescrito
     *
     * @return Intervalo em segundos para atualização
     */
    public Double getTempoAtualizar() {
        return null;
    }

    public void resultadoAtualizar(final Mensagem mensagem) {
        final int index = this.escravos.indexOf(mensagem.getOrigem());
        this.filaEscravo.set(index, mensagem.getFilaEscravo());
    }
}