/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas.servidores.implementacao;

import ispd.alocacaoVM.Alocacao;
import ispd.alocacaoVM.VMM;
import ispd.escalonadorCloud.CarregarCloud;
import ispd.escalonadorCloud.EscalonadorCloud;
import ispd.escalonadorCloud.MestreCloud;
import ispd.motor.FutureEvent;
import ispd.motor.Mensagens;
import ispd.motor.Simulation;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import java.util.ArrayList;
import java.util.List;
import ispd.alocacaoVM.CarregarAlloc;
import ispd.motor.filas.TarefaVM;

/**
 *
 * @author Diogo Tavares
 */
public class CS_VMM extends CS_Processamento implements VMM, MestreCloud, Mensagens, Vertice {

  private List<CS_Comunicacao> conexoesEntrada;
  private List<CS_Comunicacao> conexoesSaida;
  private EscalonadorCloud escalonador;
  private Alocacao alocadorVM;
  private List<Tarefa> filaTarefas;
  private boolean vmsAlocadas;
  private boolean escDisponivel;
  private boolean alocDisponivel;
  private int tipoEscalonamento;
  private int tipoAlocacao;
  private List<CS_VirtualMac> maquinasVirtuais;

  /**
   * Armazena os caminhos possiveis para alcançar cada escravo
   */
  private List<List> caminhoEscravo;
  private List<List> caminhoVMs;
  private Simulation simulacao;

  public CS_VMM(String id, String proprietario, double PoderComputacional, double memoria, double disco,
      double Ocupacao, String Escalonador, String Alocador) {
    super(id, proprietario, PoderComputacional, 1, Ocupacao, 0);
    // inicializar a pocalítica de alocação
    this.alocadorVM = CarregarAlloc.getNewAlocadorVM(Alocador);
    alocadorVM.setVMM(this);
    this.escalonador = CarregarCloud.getNewEscalonadorCloud(Escalonador);
    escalonador.setMestre(this);
    this.filaTarefas = new ArrayList<Tarefa>();
    this.vmsAlocadas = false;
    this.escDisponivel = false;
    this.alocDisponivel = true;
    this.maquinasVirtuais = new ArrayList<CS_VirtualMac>();
    this.conexoesEntrada = new ArrayList<CS_Comunicacao>();
    this.conexoesSaida = new ArrayList<CS_Comunicacao>();
    this.tipoEscalonamento = ENQUANTO_HOUVER_TAREFAS;
    this.tipoAlocacao = ENQUANTO_HOUVER_VMS;

  }

  // Métodos do centro de serviços
  @Override
  public void chegadaDeCliente(Simulation simulacao, Tarefa cliente) {
    System.out.println("------------------------------------------");
    System.out.println("Evento de chegada no vmm " + this.getId());
    if (cliente instanceof TarefaVM) {
      TarefaVM trf = (TarefaVM) cliente;
      CS_VirtualMac vm = trf.getVM_enviada();
      if (cliente.getCaminho().isEmpty()) {
        // trecho dbg
        if (this.maquinasVirtuais.contains(vm)) {
          System.out.println("VM duplicada");
          System.out.println("------------------------------------------");
        } else {
          System.out.println("vm " + vm.getId() + " adicionada no alocador do VMM " + this.getId());
          System.out.println("------------------------------------------");
          maquinasVirtuais.add(vm); // adiciona na lista de maquinas virtuais
          if (alocDisponivel) {
            this.alocDisponivel = false;
            alocadorVM.addVM(vm);
            escalonador.addEscravo(vm);
            executarAlocacao();
          } else {
            alocadorVM.addVM(vm);
            escalonador.addEscravo(vm);
          }
        }
      } else {// se não for ele a origem ele precisa encaminhá-la

        System.out.println(this.getId() + ": sou VMM intermediario, encamininhando " + vm.getId());
        FutureEvent evtFut = new FutureEvent(simulacao.getTime(this), FutureEvent.CHEGADA,
            cliente.getCaminho().remove(0), cliente);
        simulacao.addFutureEvent(evtFut);
      }
    } else { // cliente é tarefa comum
      System.out.println("cliente é a tarefa " + cliente.getIdentificador() + " com status " + cliente.getEstado());

      if (cliente.getEstado() != Tarefa.CANCELADO) {
        // Tarefas concluida possuem tratamento diferencial
        if (cliente.getEstado() == Tarefa.CONCLUIDO) {
          System.out.println("cliente é o retorno de tarefa " + cliente.getIdentificador());
          // se não for origem da tarefa ela deve ser encaminhada
          if (!cliente.getOrigem().equals(this)) {
            // encaminhar tarefa!
            // Gera evento para chegada da tarefa no proximo servidor
            FutureEvent evtFut = new FutureEvent(simulacao.getTime(this), FutureEvent.CHEGADA,
                cliente.getCaminho().remove(0), cliente);
            // Adicionar na lista de eventos futuros
            simulacao.addFutureEvent(evtFut);
          }
          // caso seja este o centro de serviço de origem
          System.out.println("Tarefa " + cliente.getIdentificador() + " adicionada na lista de concluídas");
          this.escalonador.addTarefaConcluida(cliente);

          if (tipoEscalonamento == QUANDO_RECEBE_RESULTADO || tipoEscalonamento == AMBOS) {
            if (this.escalonador.getFilaTarefas().isEmpty()) {
              this.escDisponivel = true;
            } else {
              executarEscalonamento();
            }
          }
        } // Caso a tarefa está chegando pra ser escalonada
        else {
          if (!(cliente.getCaminho() == null)) {
            FutureEvent evtFut = new FutureEvent(simulacao.getTime(this), FutureEvent.CHEGADA,
                cliente.getCaminho().remove(0), cliente);
            simulacao.addFutureEvent(evtFut);
          } else {
            if (escDisponivel) {
              System.out.println("Tarefa " + cliente.getIdentificador() + " chegando para ser escalonada");
              this.escDisponivel = false;
              // escalonador adiciona nova tarefa
              escalonador.adicionarTarefa(cliente);
              // como o escalonador está disponível vai executar o escalonamento diretamente
              executarEscalonamento();
            } else {
              // escalonador apenas adiciona a tarefa
              escalonador.adicionarTarefa(cliente);
            }
          }
        }
      }
    }
  }

  // o VMM não irá processar tarefas... apenas irá escaloná-las..
  @Override
  public void atendimento(Simulation simulacao, Tarefa cliente) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void saidaDeCliente(Simulation simulacao, Tarefa cliente) {
    // trecho de debbuging
    System.out.println("Evento de Saída: VMM " + this.getId());
    if (cliente instanceof TarefaVM) {
      TarefaVM trf = (TarefaVM) cliente;
      System.out.println("cliente é a vm " + trf.getVM_enviada().getId());
      FutureEvent evtFut = new FutureEvent(simulacao.getTime(this), FutureEvent.CHEGADA,
          cliente.getCaminho().remove(0), cliente);
      // Event adicionado a lista de evntos futuros
      simulacao.addFutureEvent(evtFut);
      if (tipoAlocacao == ENQUANTO_HOUVER_VMS || tipoAlocacao == DOISCASOS) {
        if (!alocadorVM.getMaquinasVirtuais().isEmpty()) {
          executarAlocacao();
        } else {
          this.alocDisponivel = true;
        }
      }
    } else {
      System.out.println("cliente é uma tarefa " + cliente.getIdentificador());
      FutureEvent evtFut = new FutureEvent(simulacao.getTime(this), FutureEvent.CHEGADA,
          cliente.getCaminho().remove(0), cliente);
      // Event adicionado a lista de evntos futuros
      simulacao.addFutureEvent(evtFut);
      if (tipoEscalonamento == ENQUANTO_HOUVER_TAREFAS || tipoEscalonamento == AMBOS) {
        // se fila de tarefas do servidor não estiver vazia escalona proxima tarefa
        if (!escalonador.getFilaTarefas().isEmpty()) {
          executarEscalonamento();
        } else {
          this.escDisponivel = true;
        }
      }
    }
  }

  @Override
  public void requisicao(Simulation simulacao, Mensagem mensagem, int tipo) {
    if (tipo == FutureEvent.ESCALONAR) {
      System.out.println("Iniciando escalonamento...");
      escalonador.escalonar();
    } else if (tipo == FutureEvent.ALOCAR_VMS) {
      System.out.println("Iniciando Alocação...");
      alocadorVM.escalonar();// realizar a rotina de alocar a máquina virtual
    } else if (mensagem != null) {
      if (mensagem.getTipo() == Mensagens.ATUALIZAR) {
        atenderAtualizacao(simulacao, mensagem);
      } else if (mensagem.getTipo() == Mensagens.ALOCAR_ACK) {
        atenderAckAlocacao(simulacao, mensagem);

      } else if (mensagem.getTarefa() != null && mensagem.getTarefa().getLocalProcessamento().equals(this)) {
        switch (mensagem.getTipo()) {
          case Mensagens.PARAR:
            atenderParada(simulacao, mensagem);
            break;
          case Mensagens.CANCELAR:
            atenderCancelamento(simulacao, mensagem);
            break;
          case Mensagens.DEVOLVER:
            atenderDevolucao(simulacao, mensagem);
            break;
          case Mensagens.DEVOLVER_COM_PREEMPCAO:
            atenderDevolucaoPreemptiva(simulacao, mensagem);
            break;

        }
      } else if (mensagem.getTipo() == Mensagens.RESULTADO_ATUALIZAR) {
        atenderRetornoAtualizacao(simulacao, mensagem);
      } else if (mensagem.getTarefa() != null) {
        // encaminhando mensagem para o destino
        this.enviarMensagem(mensagem.getTarefa(), (CS_Processamento) mensagem.getTarefa().getLocalProcessamento(),
            mensagem.getTipo());
      }
    }
    // deve incluir requisição para alocar..
  }

  // métodos do Mestre
  @Override
  public void enviarTarefa(Tarefa tarefa) {
    // Gera evento para atender proximo cliente da lista
    System.out.println(
        "Tarefa:" + tarefa.getIdentificador() + "escalonada para vm:" + tarefa.getLocalProcessamento().getId());
    FutureEvent evtFut = new FutureEvent(simulacao.getTime(this), FutureEvent.SAIDA, this, tarefa);
    // Event adicionado a lista de evntos futuros
    simulacao.addFutureEvent(evtFut);
  }

  @Override
  public void processarTarefa(Tarefa tarefa) {
    throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                   // | Templates.
  }

  @Override
  public void executarEscalonamento() {
    System.out.println(this.getId() + " solicitando escalonamento");
    FutureEvent evtFut = new FutureEvent(simulacao.getTime(this), FutureEvent.ESCALONAR, this, null);
    // Event adicionado a lista de evntos futuros
    simulacao.addFutureEvent(evtFut);
  }

  @Override
  public void enviarVM(CS_VirtualMac vm) {
    System.out.println("Enviar VM: alocando VM " + vm.getId());
    System.out.println("------------------------------------------");
    TarefaVM tarefa = new TarefaVM(vm.getVmmResponsavel(), vm, 300.0, 0.0);
    tarefa.setCaminho(vm.getCaminho());
    FutureEvent evtFut = new FutureEvent(simulacao.getTime(this), FutureEvent.SAIDA, this, tarefa);
    // Event adicionado a lista de evntos futuros
    simulacao.addFutureEvent(evtFut);
  }

  @Override
  public void executarAlocacao() {
    FutureEvent evtFut = new FutureEvent(simulacao.getTime(this), FutureEvent.ALOCAR_VMS, this, null);
    // Event adicionado a lista de evntos futuros
    simulacao.addFutureEvent(evtFut);
  }

  @Override
  public void enviarMensagem(Tarefa tarefa, CS_Processamento escravo, int tipo) {
    Mensagem msg = new Mensagem(this, tipo, tarefa);
    msg.setCaminho(escalonador.escalonarRota(escravo));
    FutureEvent evtFut = new FutureEvent(simulacao.getTime(this), FutureEvent.MENSAGEM, msg.getCaminho().remove(0),
        msg);
    // Event adicionado a lista de evntos futuros
    simulacao.addFutureEvent(evtFut);
  }

  @Override
  public void atualizar(CS_Processamento escravo) {
    Mensagem msg = new Mensagem(this, 0.011444091796875, Mensagens.ATUALIZAR);
    msg.setCaminho(escalonador.escalonarRota(escravo));
    FutureEvent evtFut = new FutureEvent(simulacao.getTime(this), FutureEvent.MENSAGEM, msg.getCaminho().remove(0),
        msg);
    // Event adicionado a lista de evntos futuros
    simulacao.addFutureEvent(evtFut);
  }

  public void atualizar(CS_Processamento escravo, Double time) {
    Mensagem msg = new Mensagem(this, 0.011444091796875, Mensagens.ATUALIZAR);
    msg.setCaminho(escalonador.escalonarRota(escravo));
    FutureEvent evtFut = new FutureEvent(time, FutureEvent.MENSAGEM, msg.getCaminho().remove(0), msg);
    // Event adicionado a lista de evntos futuros
    simulacao.addFutureEvent(evtFut);
  }

  @Override
  public void setSimulacao(Simulation simulacao) {
    this.simulacao = simulacao;
  }

  public EscalonadorCloud getEscalonador() {
    return escalonador;
  }

  public Alocacao getAlocadorVM() {
    return alocadorVM;
  }

  @Override
  public void addConexoesSaida(CS_Link link) {
    conexoesSaida.add(link);
  }

  @Override
  public void addConexoesEntrada(CS_Link link) {
    conexoesEntrada.add(link);
  }

  public void addConexoesSaida(CS_Switch Switch) {
    conexoesSaida.add(Switch);
  }

  public void addConexoesEntrada(CS_Switch Switch) {
    conexoesEntrada.add(Switch);
  }

  public void addEscravo(CS_Processamento maquina) {
    alocadorVM.addMaquinaFisica(maquina);

  }

  public void addVM(CS_VirtualMac vm) {
    maquinasVirtuais.add(vm);
    alocadorVM.addVM(vm);
    escalonador.addEscravo(vm);
  }

  @Override
  public List<CS_Comunicacao> getConexoesSaida() {
    return this.conexoesSaida;
  }

  /**
   * Encontra caminhos para chegar até um escravo e adiciona no caminhoEscravo
   */
  @Override
  public void determinarCaminhos() throws LinkageError {
    List<CS_Processamento> escravos = alocadorVM.getMaquinasFisicas(); // lista de maquinas fisicas
    // Instancia objetos
    caminhoEscravo = new ArrayList<List>(escravos.size());
    // Busca pelo melhor caminho
    for (int i = 0; i < escravos.size(); i++) {
      caminhoEscravo.add(i, CS_VMM.getMenorCaminho(this, escravos.get(i)));
    }
    // verifica se todos os escravos são alcansaveis
    for (int i = 0; i < escravos.size(); i++) {
      if (caminhoEscravo.get(i).isEmpty()) {
        throw new LinkageError();
      }
    }

    alocadorVM.setCaminhoMaquinas(caminhoEscravo);
    escalonador.setMaqFisicas(escravos);
    escalonador.setCaminhoMaquinas(caminhoEscravo);
  }

  @Override
  public int getTipoEscalonamento() {
    return tipoEscalonamento;
  }

  @Override
  public void setTipoEscalonamento(int tipo) {
    tipoEscalonamento = tipo;
  }

  @Override
  public Tarefa criarCopia(Tarefa get) {
    Tarefa tarefa = new Tarefa(get);
    simulacao.addJob(tarefa);
    return tarefa;
  }

  @Override
  public Simulation getSimulacao() {
    return simulacao;
  }

  @Override
  public void atenderCancelamento(Simulation simulacao, Mensagem mensagem) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void atenderParada(Simulation simulacao, Mensagem mensagem) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void atenderDevolucao(Simulation simulacao, Mensagem mensagem) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void atenderDevolucaoPreemptiva(Simulation simulacao, Mensagem mensagem) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void atenderAtualizacao(Simulation simulacao, Mensagem mensagem) {
    // atualiza metricas dos usuarios globais
    // simulacao.getRedeDeFilas().getMetricasUsuarios().addMetricasUsuarios(escalonador.getMetricaUsuarios());
    // enviar resultados
    List<CentroServico> caminho = new ArrayList<CentroServico>(
        CS_MaquinaCloud.getMenorCaminhoIndireto(this, (CS_Processamento) mensagem.getOrigem()));
    Mensagem novaMensagem = new Mensagem(this, mensagem.getTamComunicacao(), Mensagens.RESULTADO_ATUALIZAR);
    // Obtem informações dinâmicas
    // novaMensagem.setProcessadorEscravo(new ArrayList<Tarefa>(tarefaEmExecucao));
    novaMensagem.setFilaEscravo(new ArrayList<Tarefa>(filaTarefas));
    novaMensagem.getFilaEscravo().addAll(escalonador.getFilaTarefas());
    novaMensagem.setCaminho(caminho);
    FutureEvent evtFut = new FutureEvent(simulacao.getTime(this), FutureEvent.MENSAGEM,
        novaMensagem.getCaminho().remove(0), novaMensagem);
    // Event adicionado a lista de evntos futuros
    simulacao.addFutureEvent(evtFut);
  }

  @Override
  public void atenderRetornoAtualizacao(Simulation simulacao, Mensagem mensagem) {
    escalonador.resultadoAtualizar(mensagem);
  }

  @Override
  public void atenderFalha(Simulation simulacao, Mensagem mensagem) {
    throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                   // | Templates.
  }

  @Override
  public Integer getCargaTarefas() {
    return (escalonador.getFilaTarefas().size() + filaTarefas.size());
  }

  @Override
  public void enviarMensagemAlloc(Tarefa tarefa, CS_Processamento maquina, int tipo) {
    throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                   // | Templates.
  }

  @Override
  public void atualizarAlloc(CS_Processamento maquina) {
    throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                   // | Templates.
  }

  @Override
  public void setSimulacaoAlloc(Simulation simulacao) {
    this.simulacao = simulacao;
  }

  @Override
  public int getTipoAlocacao() {
    return this.tipoAlocacao;
  }

  @Override
  public void setTipoAlocacao(int tipo) {
    this.tipoAlocacao = tipo;
  }

  @Override
  public Simulation getSimulacaoAlloc() {
    return this.simulacao;
  }

  public void instanciarCaminhosVMs() {
    caminhoVMs = new ArrayList<List>(escalonador.getEscravos().size());
    for (int i = 0; i < escalonador.getEscravos().size(); i++) {
      caminhoVMs.add(i, new ArrayList());
    }
  }

  public void determinarCaminhoVM(CS_VirtualMac vm, ArrayList<CentroServico> caminhoVM) {

    int indVM = escalonador.getEscravos().indexOf(vm);
    System.out.println("indice da vm: " + indVM);
    if (indVM >= caminhoVMs.size()) {
      caminhoVMs.add(indVM, caminhoVM);
    } else {
      caminhoVMs.set(indVM, caminhoVM);
    }
    System.out.println("Lista atualizada de caminho para as vms:");
    for (int i = 0; i < caminhoVMs.size(); i++) {
      System.out.println(this.escalonador.getEscravos().get(i).getId());
      System.out.println(caminhoVMs.get(i).toString());
    }
    escalonador.setCaminhoEscravo(caminhoVMs);
    System.out.println("------------------------------");

  }

  @Override
  public void liberarEscalonador() {
    escDisponivel = true;
  }

  @Override
  public void atenderAckAlocacao(Simulation simulacao, Mensagem mensagem) {
    // se este VMM for o de origem ele deve atender senão deve encaminhar a mensagem
    // para frente
    System.out.println("--------------------------------------");
    TarefaVM trf = (TarefaVM) mensagem.getTarefa();
    CS_VirtualMac auxVM = trf.getVM_enviada();
    CS_MaquinaCloud auxMaq = auxVM.getMaquinaHospedeira();
    System.out.println("Atendendo ACK de alocação da vm " + auxVM.getId() + " na máquina " + auxMaq.getId());
    if (auxVM.getVmmResponsavel().equals(this)) {// se o VMM responsável da VM for este..
      // tratar o ack
      // primeiro encontrar o caminho pra máquina onde a vm está alocada

      int index = alocadorVM.getMaquinasFisicas().indexOf(auxMaq); // busca índice da maquina na lista de máquinas
                                                                   // físicas do vmm
      ArrayList<CentroServico> caminho;
      if (index == -1) {
        caminho = new ArrayList<CentroServico>(getMenorCaminhoIndiretoCloud(this, auxMaq));
      } else {
        caminho = new ArrayList<CentroServico>(caminhoEscravo.get(index));
      }
      System.out.println(this.getId() + ": Caminho encontrado para a vm com tamanho: " + caminho.size());
      determinarCaminhoVM(auxVM, caminho);
      System.out.println(auxVM.getId() + " Alocada");
      auxVM.setStatus(CS_VirtualMac.ALOCADA);
      auxVM.setInstanteAloc(simulacao.getTime(this));
      if (this.vmsAlocadas == false) {
        this.vmsAlocadas = true;
        this.escDisponivel = true;
      }
    } else {// passar adiante, encontrando antes o caminho intermediário para poder
            // escalonar tarefas desse VMM tbm para a vm hierarquica
      System.out.println(this.getId() + ": VMM intermediário, definindo caminho intermediário para " + auxVM.getId());
      if (this.escalonador.getEscravos().contains(auxVM)) {
        int index = alocadorVM.getMaquinasFisicas().indexOf(auxMaq);
        ArrayList<CentroServico> caminho;
        if (index == -1) {
          caminho = new ArrayList<CentroServico>(getMenorCaminhoIndiretoCloud(this, auxMaq));
        } else {
          caminho = new ArrayList<CentroServico>(caminhoEscravo.get(index));
        }
        System.out.println("Caminho encontrado para a vm com tamanho: " + caminho.size());
        determinarCaminhoVM(auxVM, caminho);
      }
      FutureEvent evt = new FutureEvent(simulacao.getTime(this), FutureEvent.MENSAGEM,
          mensagem.getCaminho().remove(0), mensagem);
      simulacao.addFutureEvent(evt);
    }
  }

  @Override
  public void atenderDesligamento(Simulation simulacao, Mensagem mensagem) {
    throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools
                                                                   // | Templates.
  }

}
