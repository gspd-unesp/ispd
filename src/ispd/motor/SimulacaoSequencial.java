/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import java.util.List;
import java.util.PriorityQueue;

/**
 *
 * @author denison_usuario
 */
public class SimulacaoSequencial extends Simulacao {

    private double time = 0;
    private PriorityQueue<EventoFuturo> eventos;
    
    public SimulacaoSequencial(ProgressoSimulacao janela, RedeDeFilas redeDeFilas, List<Tarefa> tarefas) throws IllegalArgumentException {
        super(janela, redeDeFilas,tarefas);
        this.time = 0;
        this.eventos = new PriorityQueue<EventoFuturo>();

        if (redeDeFilas == null) {
            throw new IllegalArgumentException("The model has no icons.");
        } else if (redeDeFilas.getMestres() == null || redeDeFilas.getMestres().isEmpty()) {
            throw new IllegalArgumentException("The model has no Masters.");
        } else if (redeDeFilas.getLinks() == null || redeDeFilas.getLinks().isEmpty()) {
            janela.println("The model has no Networks.", Color.orange);
        }
        if (tarefas == null || tarefas.isEmpty()) {
            throw new IllegalArgumentException("One or more  workloads have not been configured.");
        }
        janela.print("Creating routing.");
        janela.print(" -> ");
        /**
         * Trecho de código que implementa o roteamento entre os mestres e os seus respectivos escravos
         */
        for (CS_Processamento mst : redeDeFilas.getMestres()) {
            Mestre temp = (Mestre) mst;
            //Cede acesso ao mestre a fila de eventos futuros
            temp.setSimulacao(this);
            //Encontra menor caminho entre o mestre e seus escravos
            mst.determinarCaminhos(); //mestre encontra caminho para seus escravos
        }
        
        janela.incProgresso(5);
        janela.println("OK", Color.green);
        
        if (redeDeFilas.getMaquinas() == null || redeDeFilas.getMaquinas().isEmpty()) {
            janela.println("The model has no processing slaves.", Color.orange);
        } else {
            for (CS_Maquina maq : redeDeFilas.getMaquinas()) {
                //Encontra menor caminho entre o escravo e seu mestre
                maq.determinarCaminhos();//escravo encontra caminhos para seu mestre
            }
        }
        //fim roteamento
        janela.incProgresso(5);
    }

    @Override
    public void simular() {
        //inicia os escalonadores
        iniciarEscalonadores();
        //adiciona chegada das tarefas na lista de eventos futuros
        addEventos(getTarefas());
        if (atualizarEscalonadores()) {
            realizarSimulacaoAtualizaTime();
        } else {
            realizarSimulacao();
        }
        getJanela().incProgresso(30);
        getJanela().println("Simulation completed.", Color.green);
        //Centralizando métricas de usuários
        //for (CS_Processamento mestre : redeDeFilas.getMestres()) {
            //CS_Mestre mst = (CS_Mestre) mestre;
            //janela.println(mst.getId());
            //janela.println(mst.getEscalonador().getMetricaUsuarios().toString());
            //redeDeFilas.getMetricasUsuarios().addMetricasUsuarios(mst.getEscalonador().getMetricaUsuarios());
        //}
        //janela.println(redeDeFilas.getMetricasUsuarios().toString());
    }

    public void addEventos(List<Tarefa> tarefas) {
        for (Tarefa tarefa : tarefas) {
            EventoFuturo evt = new EventoFuturo(tarefa.getTimeCriacao(), EventoFuturo.CHEGADA, tarefa.getOrigem(), tarefa);
            eventos.add(evt);
        }
    }

    @Override
    public void addEventoFuturo(EventoFuturo ev) {
        eventos.offer(ev);
    }

    @Override
    public boolean removeEventoFuturo(int tipoEv, CentroServico servidorEv, Cliente clienteEv) {
        //remover evento de saida do cliente do servidor
        java.util.Iterator<EventoFuturo> interator = this.eventos.iterator();
        while (interator.hasNext()) {
            EventoFuturo ev = interator.next();
            if (ev.getTipo() == tipoEv
                    && ev.getServidor().equals(servidorEv)
                    && ev.getCliente().equals(clienteEv)) {
                this.eventos.remove(ev);
                return true;
            }
        }
        return false;
    }

    @Override
    public double getTime(Object origem) {
        return time;
    }

    private boolean atualizarEscalonadores() {
        for (CS_Processamento mst : getRedeDeFilas().getMestres()) {
            CS_Mestre mestre = (CS_Mestre) mst;
            if (mestre.getEscalonador().getTempoAtualizar() != null) {
                return true;
            }
        }
        return false;
    }

    
    private void realizarSimulacao() {
        while (!eventos.isEmpty()) {
        //recupera o próximo evento e o executa.
            //executa estes eventos de acordo com sua ordem de chegada
            //de forma a evitar a execução de um evento antes de outro
            //que seria criado anteriormente
            EventoFuturo eventoAtual = eventos.poll();
            time = eventoAtual.getTempoOcorrencia();
            switch (eventoAtual.getTipo()) {
                case EventoFuturo.CHEGADA:
                    eventoAtual.getServidor().chegadaDeCliente(this, (Tarefa) eventoAtual.getCliente());
                    break;
                case EventoFuturo.ATENDIMENTO:
                    eventoAtual.getServidor().atendimento(this, (Tarefa) eventoAtual.getCliente());
                    break;
                case EventoFuturo.SAÍDA:
                    eventoAtual.getServidor().saidaDeCliente(this, (Tarefa) eventoAtual.getCliente());
                    break;
                case EventoFuturo.ESCALONAR:
                    eventoAtual.getServidor().requisicao(this, null, EventoFuturo.ESCALONAR);
                    break;
                default:
                    eventoAtual.getServidor().requisicao(this, (Mensagem) eventoAtual.getCliente(), eventoAtual.getTipo());
                    break;
            }
        }
    }

    /**
     * Executa o laço de repetição responsavel por atender todos eventos da
     * simulação, e adiciona o evento para atualizar os escalonadores.
     */
    private void realizarSimulacaoAtualizaTime() {
        List<Object[]> Arrayatualizar = new ArrayList<Object[]>();
        for (CS_Processamento mst : getRedeDeFilas().getMestres()) {
            CS_Mestre mestre = (CS_Mestre) mst;
            if (mestre.getEscalonador().getTempoAtualizar() != null) {
                Object[] item = new Object[3];
                item[0] = mestre;
                item[1] = mestre.getEscalonador().getTempoAtualizar();
                item[2] = mestre.getEscalonador().getTempoAtualizar();
                Arrayatualizar.add(item);
            }
        }
        while (!eventos.isEmpty()) {
            //recupera o próximo evento e o executa.
            //executa estes eventos de acordo com sua ordem de chegada
            //de forma a evitar a execução de um evento antes de outro
            //que seria criado anteriormente
            for (Object[] ob : Arrayatualizar) {
                if ((Double) ob[2] < eventos.peek().getTempoOcorrencia()) {
                    CS_Mestre mestre = (CS_Mestre) ob[0];
                    for (CS_Processamento maq : mestre.getEscalonador().getEscravos()) {
                        mestre.atualizar(maq, (Double) ob[2]);
                    }
                    ob[2] = (Double) ob[2] + (Double) ob[1];
                }
            }
            EventoFuturo eventoAtual = eventos.poll();
            time = eventoAtual.getTempoOcorrencia();
            switch (eventoAtual.getTipo()) {
                case EventoFuturo.CHEGADA:
                    eventoAtual.getServidor().chegadaDeCliente(this, (Tarefa) eventoAtual.getCliente());
                    break;
                case EventoFuturo.ATENDIMENTO:
                    eventoAtual.getServidor().atendimento(this, (Tarefa) eventoAtual.getCliente());
                    break;
                case EventoFuturo.SAÍDA:
                    eventoAtual.getServidor().saidaDeCliente(this, (Tarefa) eventoAtual.getCliente());
                    break;
                case EventoFuturo.ESCALONAR:
                    eventoAtual.getServidor().requisicao(this, null, EventoFuturo.ESCALONAR);
                    break;
                default:
                    eventoAtual.getServidor().requisicao(this, (Mensagem) eventoAtual.getCliente(), eventoAtual.getTipo());
                    break;
            }
        }
    }
}