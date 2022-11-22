package ispd.policy.scheduling.grid.impl;

import ispd.annotations.Policy;
import ispd.motor.Mensagens;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.policy.scheduling.grid.GridSchedulingPolicy;

import java.util.ArrayList;
import java.util.List;

@Policy
public class WQR extends GridSchedulingPolicy {
    private Tarefa ultimaTarefaConcluida = null;
    private List<Tarefa> tarefaEnviada = null;
    private int servidoresOcupados = 0;
    private int cont = 0;

    public WQR() {
        this.tarefas = new ArrayList<>();
        this.escravos = new ArrayList<>();
    }

    @Override
    public void iniciar() {
        this.tarefaEnviada = new ArrayList<>(this.escravos.size());
        for (int i = 0; i < this.escravos.size(); i++) {
            this.tarefaEnviada.add(null);
        }
    }

    @Override
    public Tarefa escalonarTarefa() {
        if (!this.tarefas.isEmpty()) {
            return this.tarefas.remove(0);
        }
        if (this.cont >= this.tarefaEnviada.size()) {
            this.cont = 0;
        }
        if (this.servidoresOcupados >= this.escravos.size()) {
            return null;
        }
        for (int i = this.cont; i < this.tarefaEnviada.size(); i++) {
            if (this.tarefaEnviada.get(i) != null) {
                this.cont = i;
                if (!this.tarefaEnviada.get(i).getOrigem().equals(this.mestre)) {
                    this.cont++;
                    return null;
                }
                return this.mestre.cloneTask(this.tarefaEnviada.get(i));
            }
        }
        return null;
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        final int index =
                this.tarefaEnviada.indexOf(this.ultimaTarefaConcluida);
        if (this.ultimaTarefaConcluida != null && index != -1) {
            return this.escravos.get(index);
        } else {
            for (int i = 0; i < this.tarefaEnviada.size(); i++) {
                if (this.tarefaEnviada.get(i) == null) {
                    return this.escravos.get(i);
                }
            }
        }
        for (int i = 0; i < this.tarefaEnviada.size(); i++) {
            if (this.tarefaEnviada.get(i) != null && this.tarefaEnviada.get(i).isCopy()) {
                return this.escravos.get(i);
            }
        }
        return null;
    }

    @Override
    public List<CentroServico> escalonarRota(final CentroServico destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<CentroServico>) this.caminhoEscravo.get(index));
    }

    @Override
    public void escalonar() {
        final CS_Processamento rec = this.escalonarRecurso();
        boolean sair = false;
        if (rec != null) {
            final Tarefa trf = this.escalonarTarefa();
            if (trf != null) {
                if (this.tarefaEnviada.get(this.escravos.indexOf(rec)) != null) {
                    this.mestre.sendMessage(this.tarefaEnviada.get(this.escravos.indexOf(rec)), rec, Mensagens.CANCELAR);
                } else {
                    this.servidoresOcupados++;
                }
                this.tarefaEnviada.set(this.escravos.indexOf(rec), trf);
                this.ultimaTarefaConcluida = null;
                trf.setLocalProcessamento(rec);
                trf.setCaminho(this.escalonarRota(rec));
                this.mestre.sendTask(trf);
            } else if (this.tarefas.isEmpty()) {
                sair = true;
            }
        }
        if (this.servidoresOcupados > 0 && this.servidoresOcupados < this.escravos.size() && this.tarefas.isEmpty() && !sair) {
            for (final Tarefa tar : this.tarefaEnviada) {
                if (tar != null && tar.getOrigem().equals(this.mestre)) {
                    this.mestre.executeScheduling();
                    break;
                }
            }
        }
    }

    @Override
    public void addTarefaConcluida(final Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);
        final int index = this.tarefaEnviada.indexOf(tarefa);
        if (index != -1) {
            this.servidoresOcupados--;
            this.tarefaEnviada.set(index, null);
        }
        for (int i = 0; i < this.tarefaEnviada.size(); i++) {
            if (this.tarefaEnviada.get(i) != null && this.tarefaEnviada.get(i).isCopyOf(tarefa)) {
                this.mestre.sendMessage(this.tarefaEnviada.get(i),
                        this.escravos.get(i), Mensagens.CANCELAR);
                this.servidoresOcupados--;
                this.tarefaEnviada.set(i, null);
            }
        }
        this.ultimaTarefaConcluida = tarefa;
        if ((this.servidoresOcupados > 0 && this.servidoresOcupados < this.escravos.size()) || !this.tarefas.isEmpty()) {
            this.mestre.executeScheduling();
        }
    }

}
