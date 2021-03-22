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
 * SimulacaoParalela.java
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
package ispd.motor;

import ispd.escalonador.Mestre;
import ispd.motor.filas.Cliente;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

/**
 *
 * @author denison
 */
public class SimulacaoParalela extends Simulacao {

    private int numThreads;
    private ExecutorService threadPool;
    private List<CentroServico> recursos;
    private HashMap<CentroServico, PriorityBlockingQueue<EventoFuturo>> threadFilaEventos;
    private HashMap<CentroServico, ThreadTrabalhador> threadTrabalhador;

    public SimulacaoParalela(ProgressoSimulacao janela, RedeDeFilas redeDeFilas, List<Tarefa> tarefas, int numThreads) throws IllegalArgumentException {
        super(janela, redeDeFilas, tarefas);
        threadPool = Executors.newFixedThreadPool(numThreads);
        threadFilaEventos = new HashMap<CentroServico, PriorityBlockingQueue<EventoFuturo>>();
        threadTrabalhador = new HashMap<CentroServico, ThreadTrabalhador>();
        //Cria lista com todos os recursos da grade
        recursos = new ArrayList<CentroServico>();//Collections.synchronizedList(new ArrayList<CentroServico>());
        recursos.addAll(redeDeFilas.getMaquinas());
        recursos.addAll(redeDeFilas.getLinks());
        recursos.addAll(redeDeFilas.getInternets());
        //Cria um trabalhador e uma fila de evento para cada recurso
        for (CentroServico rec : redeDeFilas.getMestres()) {
            threadFilaEventos.put(rec, new PriorityBlockingQueue<EventoFuturo>());
            if (((CS_Mestre) rec).getEscalonador().getTempoAtualizar() != null) {
                threadTrabalhador.put(rec, new ThreadTrabalhadorDinamico(rec, this));
            } else {
                threadTrabalhador.put(rec, new ThreadTrabalhador(rec, this));
            }
        }
        for (CentroServico rec : recursos) {
            threadFilaEventos.put(rec, new PriorityBlockingQueue<EventoFuturo>());
            threadTrabalhador.put(rec, new ThreadTrabalhador(rec, this));
        }
        recursos.addAll(redeDeFilas.getMestres());
        this.numThreads = numThreads;
        if (getRedeDeFilas() == null) {
            throw new IllegalArgumentException("The model has no icons.");
        } else if (getRedeDeFilas().getMestres() == null || getRedeDeFilas().getMestres().isEmpty()) {
            throw new IllegalArgumentException("The model has no Masters.");
        } else if (getRedeDeFilas().getLinks() == null || getRedeDeFilas().getLinks().isEmpty()) {
            janela.println("The model has no Networks.", Color.orange);
        }
        if (tarefas == null || tarefas.isEmpty()) {
            throw new IllegalArgumentException("One or more  workloads have not been configured.");
        }
    }

    @Override
    public void criarRoteamento() {
        for (CS_Processamento mst : getRedeDeFilas().getMestres()) {
            Mestre temp = (Mestre) mst;
            //Cede acesso ao mestre a fila de eventos futuros
            temp.setSimulacao(this);
            //Encontra menor caminho entre o mestre e seus escravos
            threadPool.execute(new determinarCaminho(mst));
        }
        if (getRedeDeFilas().getMaquinas() == null || getRedeDeFilas().getMaquinas().isEmpty()) {
            getJanela().println("The model has no processing slaves.", Color.orange);
        } else {
            for (CS_Maquina maq : getRedeDeFilas().getMaquinas()) {
                //Encontra menor caminho entre o escravo e seu mestre
                threadPool.execute(new determinarCaminho(maq));
            }
        }
        threadPool.shutdown();
        while (!threadPool.isTerminated()) {
        }
    }
    
    @Override
    public void simular() {
        System.out.println("Iniciando: " + numThreads + " threads");
        threadPool = Executors.newFixedThreadPool(numThreads);
        iniciarEscalonadores();
        //Adiciona tarefas iniciais
        for (CentroServico mestre : getRedeDeFilas().getMestres()) {
            threadPool.execute(new tarefasIniciais(mestre));
        }
        threadPool.shutdown();
        while (!threadPool.isTerminated()) {
        }
        System.out.println("Iniciando: " + numThreads + " threads");
        threadPool = Executors.newFixedThreadPool(numThreads);
        
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        //Realizar a simulação
        boolean fim = false;
        while (!fim) {
            fim = true;
            for (CentroServico rec : recursos) {
                if (!threadFilaEventos.get(rec).isEmpty() && !threadTrabalhador.get(rec).executando) {
                    //System.out.println("pai rec " + rec.getId() + " " + threadFilaEventos.get(rec).size());
                    threadTrabalhador.get(rec).executando = true;
                    threadPool.execute(threadTrabalhador.get(rec));
                    fim = false;
                } else if (!threadFilaEventos.get(rec).isEmpty()) {
                    fim = false;
                }
            }
            //try {
            //    threadPool.awaitTermination(10, TimeUnit.MILLISECONDS);
            //} catch (InterruptedException ex) {
            //    Logger.getLogger(SimulacaoParalela.class.getName()).log(Level.SEVERE, null, ex);
            //}
        }
        threadPool.shutdown();
        while (!threadPool.isTerminated()) {
        }
        //for (CentroServico rec : recursos) {
        //    System.out.println("Rec: " + rec.getId() + " " + threadFilaEventos.get(rec).size());
        //}
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
    }

    @Override
    public double getTime(Object origem) {
        if (origem != null) {
            return threadTrabalhador.get(origem).getRelogioLocal();
        } else {
            double val = 0;
            for (CentroServico rec : recursos) {
                if (threadTrabalhador.get(rec).getRelogioLocal() > val) {
                    val = threadTrabalhador.get(rec).getRelogioLocal();
                }
            }
            return val;
        }
    }

    @Override
    public void addEventoFuturo(EventoFuturo ev) {
        if (ev.getTipo() == EventoFuturo.CHEGADA) {
            threadFilaEventos.get(ev.getServidor()).offer(ev);
        } else {
            threadFilaEventos.get(ev.getServidor()).offer(ev);
        }
    }

    @Override
    public boolean removeEventoFuturo(int tipoEv, CentroServico servidorEv, Cliente clienteEv) {
        //remover evento de saida do cliente do servidor
        java.util.Iterator<EventoFuturo> interator = this.threadFilaEventos.get(servidorEv).iterator();
        while (interator.hasNext()) {
            EventoFuturo ev = interator.next();
            if (ev.getTipo() == tipoEv
                    && ev.getServidor().equals(servidorEv)
                    && ev.getCliente().equals(clienteEv)) {
                this.threadFilaEventos.get(servidorEv).remove(ev);
                return true;
            }
        }
        return false;
    }

    private class ThreadTrabalhador implements Runnable {

        private double relogioLocal;
        private CentroServico recurso;
        private Simulacao simulacao;
        private boolean executando;

        public ThreadTrabalhador(CentroServico rec, Simulacao sim) {
            this.recurso = rec;
            this.simulacao = sim;
            this.relogioLocal = 0.0;
            this.executando = false;
        }

        public double getRelogioLocal() {
            return relogioLocal;
        }

        public Simulacao getSimulacao() {
            return simulacao;
        }

        protected void setRelogioLocal(double relogio) {
            relogioLocal = relogio;
        }

        protected void setExecutando(boolean executando) {
            this.executando = executando;
        }

        public CentroServico getRecurso() {
            return recurso;
        }

        @Override
        public void run() {
            //bloqueia este trabalhador
            synchronized (this) {
                //System.out.println("fio rec " + recurso.getId() + " " + threadFilaEventos.get(recurso).size());
                while (!threadFilaEventos.get(this.recurso).isEmpty()) {
                    //Verificando ocorencia de erro
                    //if (this.relogioLocal > threadFilaEventos.get(this.recurso).peek().getTempoOcorrencia()) {
                    //    System.err.println(recurso.getId() + " " + threadFilaEventos.get(this.recurso).peek().getServidor().getId() + " Ocorreu erro! "
                    //            + this.relogioLocal + " > " + threadFilaEventos.get(this.recurso).peek().getTempoOcorrencia()
                    //            + " tipo " + threadFilaEventos.get(this.recurso).peek().getTipo());
                    //    this.atenderEvento = 2;
                    //    throw new ExceptionInInitializerError();
                    //}
                    EventoFuturo eventoAtual = threadFilaEventos.get(this.recurso).poll();
                    //System.out.println(recurso.getId()+" vou executar: "+eventoAtual.toString()+" de "+threadFilaEventos.get(this.recurso).size());
                    if (eventoAtual.getTempoOcorrencia() > this.relogioLocal) {
                        this.relogioLocal = eventoAtual.getTempoOcorrencia();
                    }
                    //if (this.relogioLocal > this.ultimaExec && recurso instanceof CS_Comunicacao) {
                    //    System.err.println(recurso.getId() + " Ocorreu erro Grave! "
                    //            + this.relogioLocal + " > " + this.ultimaExec);
                    //    this.atenderEvento = 2;
                    //    throw new ExceptionInInitializerError();
                    //}
                    switch (eventoAtual.getTipo()) {
                        case EventoFuturo.CHEGADA:
                            eventoAtual.getServidor().chegadaDeCliente(simulacao, (Tarefa) eventoAtual.getCliente());
                            break;
                        case EventoFuturo.ATENDIMENTO:
                            //System.out.println(recurso.getId() + " " + eventoAtual.getServidor().getId() + " vou atender a tarefa " + eventoAtual.getTempoOcorrencia());
                            eventoAtual.getServidor().atendimento(simulacao, (Tarefa) eventoAtual.getCliente());
                            break;
                        case EventoFuturo.SAÍDA:
                            eventoAtual.getServidor().saidaDeCliente(simulacao, (Tarefa) eventoAtual.getCliente());
                            break;
                        case EventoFuturo.ESCALONAR:
                            eventoAtual.getServidor().requisicao(simulacao, null, EventoFuturo.ESCALONAR);
                            break;
                        default:
                            eventoAtual.getServidor().requisicao(simulacao, (Mensagem) eventoAtual.getCliente(), eventoAtual.getTipo());
                            break;
                    }
                } //else {
                //    System.out.println(recurso.getId() + " chamada sem evento!");
                //}
                executando = false;
            }
        }
    }

    private class ThreadTrabalhadorDinamico extends ThreadTrabalhador implements Runnable {

        /**
         * Atributo usado para enviar mensagens de atualização aos escravos
         */
        private Object[] item;

        public ThreadTrabalhadorDinamico(CentroServico rec, Simulacao sim) {
            super(rec, sim);
            if (rec instanceof CS_Mestre) {
                CS_Mestre mestre = (CS_Mestre) rec;
                if (mestre.getEscalonador().getTempoAtualizar() != null) {
                    item = new Object[3];
                    item[0] = mestre;
                    item[1] = mestre.getEscalonador().getTempoAtualizar();
                    item[2] = mestre.getEscalonador().getTempoAtualizar();
                }
            }
        }

        @Override
        public void run() {
            //bloqueia este trabalhador
            synchronized (this) {
                while (!threadFilaEventos.get(this.getRecurso()).isEmpty()) {
                    if ((Double) item[2] < threadFilaEventos.get(this.getRecurso()).peek().getTempoOcorrencia()) {
                        CS_Mestre mestre = (CS_Mestre) item[0];
                        for (CS_Processamento maq : mestre.getEscalonador().getEscravos()) {
                            mestre.atualizar(maq, (Double) item[2]);
                        }
                        item[2] = (Double) item[2] + (Double) item[1];
                    }
                    EventoFuturo eventoAtual = threadFilaEventos.get(this.getRecurso()).poll();
                    if (eventoAtual.getTempoOcorrencia() > this.getRelogioLocal()) {
                        this.setRelogioLocal(eventoAtual.getTempoOcorrencia());
                    }
                    switch (eventoAtual.getTipo()) {
                        case EventoFuturo.CHEGADA:
                            eventoAtual.getServidor().chegadaDeCliente(this.getSimulacao(), (Tarefa) eventoAtual.getCliente());
                            break;
                        case EventoFuturo.ATENDIMENTO:
                            eventoAtual.getServidor().atendimento(this.getSimulacao(), (Tarefa) eventoAtual.getCliente());
                            break;
                        case EventoFuturo.SAÍDA:
                            eventoAtual.getServidor().saidaDeCliente(this.getSimulacao(), (Tarefa) eventoAtual.getCliente());
                            break;
                        case EventoFuturo.ESCALONAR:
                            eventoAtual.getServidor().requisicao(this.getSimulacao(), null, EventoFuturo.ESCALONAR);
                            break;
                        default:
                            eventoAtual.getServidor().requisicao(this.getSimulacao(), (Mensagem) eventoAtual.getCliente(), eventoAtual.getTipo());
                            break;
                    }
                }
                this.setExecutando(false);
            }
        }
    }

    private class determinarCaminho implements Runnable {

        private CS_Processamento mst;

        public determinarCaminho(CS_Processamento mst) {
            this.mst = mst;
        }

        @Override
        public void run() {
            mst.determinarCaminhos();
        }
    }

    private class tarefasIniciais implements Runnable {

        private CentroServico mestre;

        private tarefasIniciais(CentroServico mestre) {
            this.mestre = mestre;
        }

        @Override
        public void run() {
            synchronized (threadFilaEventos.get(mestre)) {
                System.out.println("Nome: " + Thread.currentThread().getName() + " Vou criar tarefas do " + mestre.getId());
                for (Tarefa tarefa : getTarefas()) {
                    if (tarefa.getOrigem() == mestre) {
                        //criar evento...
                        EventoFuturo evt = new EventoFuturo(tarefa.getTimeCriacao(), EventoFuturo.CHEGADA, tarefa.getOrigem(), tarefa);
                        threadFilaEventos.get(mestre).add(evt);
                    }
                }
                System.out.println("Nome: " + Thread.currentThread().getName() + " foram criadas " + threadFilaEventos.get(mestre).size());
            }
        }
    }
}