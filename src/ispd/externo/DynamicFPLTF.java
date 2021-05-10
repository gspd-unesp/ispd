/* ==========================================================
 * iSPD : iconic Simulator of Parallel and Distributed System
 * ==========================================================
 *
 * (C) Copyright 2010-2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Project Info:  http://gspd.dcce.ibilce.unesp.br/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------
 * DynamicFPLTF.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 
 * 09-Set-2014 : Version 2.0;
 *
 */
package ispd.externo;

import ispd.escalonador.Escalonador;
import ispd.motor.Mensagens;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author denison
 */
public class DynamicFPLTF extends Escalonador {

    private List<Double> tempoTornaDisponivel;
    private Tarefa tarefaSelecionada;

    public DynamicFPLTF() {
        this.tarefas = new ArrayList<Tarefa>();
        this.escravos = new ArrayList<CS_Processamento>();
        this.filaEscravo = new ArrayList<List>();
        this.tarefaSelecionada = null;
    }

    @Override
    public void iniciar() {
        tempoTornaDisponivel = new ArrayList<Double>(escravos.size());
        for (int i = 0; i < escravos.size(); i++) {
            tempoTornaDisponivel.add(0.0);
            this.filaEscravo.add(new ArrayList());
        }
    }

    @Override
    public Tarefa escalonarTarefa() {
        return tarefas.remove(0);
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        int index = 0;
        double menorTempo = escravos.get(index).tempoProcessar(tarefaSelecionada.getTamProcessamento());
        for (int i = 1; i < escravos.size(); i++) {
            double tempoEscravoI = escravos.get(i).tempoProcessar(tarefaSelecionada.getTamProcessamento());
            if (tempoTornaDisponivel.get(index) + menorTempo
                    > tempoTornaDisponivel.get(i) + tempoEscravoI) {
                menorTempo = tempoEscravoI;
                index = i;
            }
        }
        return escravos.get(index);
    }

    @Override
    public List<CentroServico> escalonarRota(CentroServico destino) {
        int index = escravos.indexOf(destino);
        return new ArrayList<CentroServico>((List<CentroServico>) caminhoEscravo.get(index));
    }

    @Override
    public void escalonar() {
        Tarefa trf = escalonarTarefa();
        tarefaSelecionada = trf;
        if (trf != null) {
            CS_Processamento rec = escalonarRecurso();
            int index = escravos.indexOf(rec);
            double custo = rec.tempoProcessar(trf.getTamProcessamento());
            tempoTornaDisponivel.set(index, tempoTornaDisponivel.get(index) + custo);
            trf.setLocalProcessamento(rec);
            trf.setCaminho(escalonarRota(rec));
            mestre.enviarTarefa(trf);
        }
    }

    @Override
    public void adicionarTarefa(Tarefa tarefa) {
        if(tarefa.getOrigem().equals(mestre)){
            this.metricaUsuarios.incTarefasSubmetidas(tarefa);
        }
        int k = 0;
        while (k < tarefas.size() && tarefas.get(k).getTamProcessamento() > tarefa.getTamProcessamento()) {
            k++;
        }
        tarefas.add(k, tarefa);
    }

    @Override
    public void addTarefaConcluida(Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);
        int index = escravos.indexOf(tarefa.getLocalProcessamento());
        if (index != -1) {
            double custo = escravos.get(index).tempoProcessar(tarefa.getTamProcessamento());
            if (tempoTornaDisponivel.get(index) - custo > 0) {
                tempoTornaDisponivel.set(index, tempoTornaDisponivel.get(index) - custo);
            }
        }
        for (int i = 0; i < escravos.size(); i++) {
            if (escravos.get(i) instanceof CS_Maquina) {
                CS_Processamento escravo = escravos.get(i);
                for (int j = 0; j < filaEscravo.get(i).size(); j++) {
                    Tarefa trf = (Tarefa) filaEscravo.get(i).get(j);
                    double custo = escravo.tempoProcessar(trf.getTamProcessamento());
                    if (tempoTornaDisponivel.get(i) - custo > 0) {
                        tempoTornaDisponivel.set(i, tempoTornaDisponivel.get(i) - custo);
                    }
                    mestre.enviarMensagem(trf, escravo, Mensagens.DEVOLVER);
                }
                filaEscravo.get(i).clear();
            }
        }
    }
    
    @Override
    public Double getTempoAtualizar(){
        return 60.0;
    }
}
