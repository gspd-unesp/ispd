package ispd.policy.scheduling.grid.impl;

import ispd.annotations.Policy;
import ispd.motor.Mensagens;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.policy.PolicyConditions;
import ispd.policy.scheduling.grid.GridMaster;
import ispd.policy.scheduling.grid.GridSchedulingPolicy;
import ispd.policy.scheduling.grid.impl.util.SlaveControl;
import ispd.policy.scheduling.grid.impl.util.UserControl;
import ispd.policy.scheduling.grid.impl.util.PreemptionControl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Policy
public class HOSEP extends GridSchedulingPolicy {
    private final List<UserControl> status;
    private final List<SlaveControl> controleEscravos;
    private final List<Tarefa> esperaTarefas;
    private final List<PreemptionControl> controlePreempcao;

    public HOSEP() {
        this.tarefas = new ArrayList<>();
        this.escravos = new ArrayList<>();
        this.controleEscravos = new ArrayList<>();
        this.esperaTarefas = new ArrayList<>();
        this.controlePreempcao = new ArrayList<>();
        this.filaEscravo = new ArrayList<>();
        final List<List> processadorEscravos = new ArrayList<>();
        this.status = new ArrayList<>();
    }

    @Override
    public void iniciar() {
        //Escalonamento quando chegam tarefas e quando tarefas são concluídas
        this.mestre.setSchedulingConditions(PolicyConditions.ALL);

        //Objetos de controle de uso e cota para cada um dos usuários
        for (int i = 0; i < this.metricaUsuarios.getUsuarios().size(); i++) {
            //Calcular o poder computacional da porção de cada usuário
            double poderComp = 0.0;
            for (final CS_Processamento escravo : this.escravos) {
                //Se o nó corrente não é mestre e pertence ao usuário corrente
                if (!(escravo instanceof GridMaster) && escravo.getProprietario().equals(this.metricaUsuarios.getUsuarios().get(i))) {
                    //Calcular o poder total da porcao do usuário corrente
                    poderComp += escravo.getPoderComputacional();
                }
            }
            //Adiciona dados do usuário corrente à lista 
            String user = this.metricaUsuarios.getUsuarios().get(i);
            double perfShare = poderComp;
            this.status.add(new UserControl(user, perfShare, escravos));
        }

        //Controle dos nós, com cópias das filas de cada um e da tarefa que
        // executa em cada um
        for (int i = 0; i < this.escravos.size(); i++) {
            String Ident = this.escravos.get(i).getId();
            int ind = i;
            this.controleEscravos.add(new SlaveControl());
        }
    }

    @Override
    public List<CentroServico> escalonarRota(final CentroServico destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<CentroServico>) this.caminhoEscravo.get(index));
    }

    @Override
    public void escalonar() {

        int indexTarefa;
        int indexEscravo;
        UserControl cliente;

        //Ordenar os usuários em ordem crescente de Poder Remanescente
        Collections.sort(this.status);

        for (final UserControl userControl : this.status) {
            cliente = userControl;

            //Buscar tarefa para execucao
            indexTarefa = this.buscarTarefa(cliente);

            if (indexTarefa != -1) {

                //Buscar máquina para executar a tarefa definida
                indexEscravo = this.buscarRecurso(cliente);

                if (indexEscravo != -1) {

                    //Se não é caso de preempção, a tarefa é configurada e
                    // enviada
                    if (this.controleEscravos.get(indexEscravo).isFree()) {

                        final Tarefa tar = this.tarefas.remove(indexTarefa);
                        tar.setLocalProcessamento(this.escravos.get(indexEscravo));
                        tar.setCaminho(this.escalonarRota(this.escravos.get(indexEscravo)));
                        this.mestre.sendTask(tar);

                        //Atualização dos dados sobre o usuário
                        cliente.decreaseTaskDemand();
                        cliente.increaseAvailableProcessingPower(this.escravos.get(indexEscravo).getPoderComputacional());

                        //Controle das máquinas
                        this.controleEscravos.get(indexEscravo).setAsBlocked();
                        return;
                    }

                    //Se é caso de preempção, a tarefa configurada e colocada
                    // em espera
                    if (this.controleEscravos.get(indexEscravo).isOccupied()) {

                        final Tarefa tar = this.tarefas.remove(indexTarefa);
                        tar.setLocalProcessamento(this.escravos.get(indexEscravo));
                        tar.setCaminho(this.escalonarRota(this.escravos.get(indexEscravo)));

                        //Controle de preempção para enviar a nova tarefa no
                        // momento certo
                        final String userPreemp =
                                this.controleEscravos.get(indexEscravo).getTasksInProcessing().get(0).getProprietario();
                        final int idTarefaPreemp =
                                this.controleEscravos.get(indexEscravo).getTasksInProcessing().get(0).getIdentificador();
                        String user1 = userPreemp;
                        int pID = idTarefaPreemp;
                        String user2 = tar.getProprietario();
                        int aID = tar.getIdentificador();
                        this.controlePreempcao.add(new PreemptionControl(user1, pID, user2, aID));
                        this.esperaTarefas.add(tar);

                        //Solicitação de retorno da tarefa em execução e
                        // atualização da demanda do usuário
                        this.mestre.sendMessage(this.controleEscravos.get(indexEscravo).getTasksInProcessing().get(0), this.escravos.get(indexEscravo), Mensagens.DEVOLVER_COM_PREEMPCAO);
                        this.controleEscravos.get(indexEscravo).setAsBlocked();
                        cliente.decreaseTaskDemand();
                        return;
                    }
                }
            }
        }
    }

    //Metodo necessario para implementar interface. Não é usado.
    @Override
    public CS_Processamento escalonarRecurso() {
        throw new UnsupportedOperationException("Not supported yet."); //To
        // change body of generated methods, choose Tools | Templates.
    }

    //Definir o intervalo de tempo, em segundos, em que as máquinas enviarão
    // dados de atualização para o escalonador
    @Override
    public Double getTempoAtualizar() {
        return 15.0;
    }

    private int buscarTarefa(final UserControl usuario) {

        //Indice da tarefa na lista de tarefas
        int trf = -1;
        //Se o usuario tem demanda nao atendida e seu consumo nao chegou ao
        // limite
        if (usuario.currentTaskDemand() > 0) {
            //Procura pela menor tarefa nao atendida do usuario.
            for (int j = 0; j < this.tarefas.size(); j++) {
                if (this.tarefas.get(j).getProprietario().equals(usuario.getUserId())) {
                    if (trf == -1) {
                        trf = j;
                    } else if (this.tarefas.get(j).getTamProcessamento() < this.tarefas.get(trf).getTamProcessamento()) {//Escolher a tarefa de menor tamanho do usuario
                        trf = j;
                    }
                }
            }
        }
        return trf;
    }

    private int buscarRecurso(final UserControl cliente) {

        /*++++++++++++++++++Buscando recurso livres++++++++++++++++++*/

        //Índice da máquina escolhida, na lista de máquinas
        int indexSelec = -1;

        for (int i = 0; i < this.escravos.size(); i++) {

            if (this.controleEscravos.get(i).isFree()) {
                if (indexSelec == -1) {
                    indexSelec = i;
                } else if (this.escravos.get(i).getPoderComputacional() > this.escravos.get(indexSelec).getPoderComputacional()) {
                    indexSelec = i;
                }
            }
        }

        if (indexSelec != -1) {
            return indexSelec;
        }

        /*+++++++++++++++++Busca por usuário para preempção+++++++++++++++++*/

        if (this.status.get(this.status.size() - 1).currentlyAvailableProcessingPower() > this.status.get(this.status.size() - 1).getOwnedMachinesProcessingPower() && cliente.currentlyAvailableProcessingPower() < cliente.getOwnedMachinesProcessingPower()) {

            for (int i = 0; i < this.escravos.size(); i++) {

                if (this.controleEscravos.get(i).isOccupied()) {
                    if (this.controleEscravos.get(i).getTasksInProcessing().get(0).getProprietario().equals(this.status.get(this.status.size() - 1).getUserId())) {

                        if (indexSelec == -1) {

                            indexSelec = i;

                        } else {
                            if (this.escravos.get(i).getPoderComputacional() < this.escravos.get(indexSelec).getPoderComputacional()) {

                                indexSelec = i;
                            }
                        }
                    }
                }
            }

            if (indexSelec != -1) {

                final double penalidaUserEsperaPosterior =
                        (cliente.currentlyAvailableProcessingPower() + this.escravos.get(indexSelec).getPoderComputacional() - cliente.getOwnedMachinesProcessingPower()) / cliente.getOwnedMachinesProcessingPower();
                final double penalidaUserEscravoPosterior =
                        (this.status.get(this.status.size() - 1).currentlyAvailableProcessingPower() - this.escravos.get(indexSelec).getPoderComputacional() - this.status.get(this.status.size() - 1).getOwnedMachinesProcessingPower()) / this.status.get(this.status.size() - 1).getOwnedMachinesProcessingPower();

                if (penalidaUserEscravoPosterior >= penalidaUserEsperaPosterior || penalidaUserEscravoPosterior > 0) {
                    return indexSelec;
                } else {
                    return -1;
                }
            }
        }
        return indexSelec;
    }

    //Metodo necessario para implementar interface. Não é usado.
    @Override
    public Tarefa escalonarTarefa() {
        throw new UnsupportedOperationException("Not supported yet."); //To
        // change body of generated methods, choose Tools | Templates.
    }

    @Override
    //Chegada de tarefa concluida
    public void addTarefaConcluida(final Tarefa tarefa) {
        //Method herdado, obrigatório executar para obter métricas ao final
        // da simulação
        super.addTarefaConcluida(tarefa);

        //Localizar informações sobre máquina que executou a tarefa e usuário
        // proprietário da tarefa
        final CS_Processamento maq =
                (CS_Processamento) tarefa.getLocalProcessamento();
        final int maqIndex = this.escravos.indexOf(maq);

        if (this.controleEscravos.get(maqIndex).isOccupied()) {

            int statusIndex = -1;

            for (int i = 0; i < this.status.size(); i++) {
                if (this.status.get(i).getUserId().equals(tarefa.getProprietario())) {
                    statusIndex = i;
                }
            }

            //Atualização das informações de estado do proprietario da tarefa
            // terminada.
            this.status.get(statusIndex).decreaseAvailableProcessingPower(maq.getPoderComputacional());
            this.controleEscravos.get(maqIndex).setAsFree();

        } else if (this.controleEscravos.get(maqIndex).isBlocked()) {

            int indexControlePreemp = -1;
            int indexStatusUserAlloc = -1;
            int indexStatusUserPreemp = -1;

            for (int j = 0; j < this.controlePreempcao.size(); j++) {
                if (this.controlePreempcao.get(j).preemptedTaskId() == tarefa.getIdentificador() && this.controlePreempcao.get(j).preemptedTaskUser().equals(tarefa.getProprietario())) {
                    indexControlePreemp = j;
                    break;
                }
            }

            for (int k = 0; k < this.status.size(); k++) {
                if (this.status.get(k).getUserId().equals(this.controlePreempcao.get(indexControlePreemp).allocatedTaskUser())) {
                    indexStatusUserAlloc = k;
                    break;
                }
            }

            for (int k = 0; k < this.status.size(); k++) {
                if (this.status.get(k).getUserId().equals(this.controlePreempcao.get(indexControlePreemp).preemptedTaskUser())) {
                    indexStatusUserPreemp = k;
                    break;
                }
            }

            //Localizar tarefa em espera designada para executar
            for (int i = 0; i < this.esperaTarefas.size(); i++) {
                if (this.esperaTarefas.get(i).getProprietario().equals(this.controlePreempcao.get(indexControlePreemp).allocatedTaskUser()) && this.esperaTarefas.get(i).getIdentificador() == this.controlePreempcao.get(indexControlePreemp).allocatedTaskId()) {

                    //Enviar tarefa para execução
                    this.mestre.sendTask(this.esperaTarefas.remove(i));

                    //Atualizar informações de estado do usuário cuja tarefa
                    // será executada
                    this.status.get(indexStatusUserAlloc).increaseAvailableProcessingPower(maq.getPoderComputacional());

                    //Atualizar informações de estado do usuário cuja tarefa
                    // teve a execução interrompida
                    this.status.get(indexStatusUserPreemp).decreaseAvailableProcessingPower(maq.getPoderComputacional());

                    //Com a preempção feita, os dados necessários para ela
                    // são eliminados
                    this.controlePreempcao.remove(indexControlePreemp);
                    //Encerrar laço
                    break;
                }
            }
        }
    }

    @Override
    public void resultadoAtualizar(final Mensagem mensagem) {
        //super.resultadoAtualizar(mensagem);
        //Localizar máquina que enviou estado atualizado
        final int index = this.escravos.indexOf(mensagem.getOrigem());

        //Atualizar listas de espera e processamento da máquina
        this.controleEscravos.get(index).setTasksInProcessing((ArrayList<Tarefa>) mensagem.getProcessadorEscravo());
        this.controleEscravos.get(index).setTasksOnHold((ArrayList<Tarefa>) mensagem.getFilaEscravo());

        //Tanto alocação para recurso livre como a preempção levam dois
        // ciclos de atualização para que a máquina possa ser considerada
        // para esacalonamento novamente

        //Primeiro ciclo
        if (this.controleEscravos.get(index).isBlocked()) {
            this.controleEscravos.get(index).setAsUncertain();
            //Segundo ciclo
        } else if (this.controleEscravos.get(index).isUncertain()) {
            //Se não está executando nada
            if (this.controleEscravos.get(index).getTasksInProcessing().isEmpty()) {

                this.controleEscravos.get(index).setAsFree();
                //Se está executando uma tarefa
            } else if (this.controleEscravos.get(index).getTasksInProcessing().size() == 1) {

                this.controleEscravos.get(index).setAsOccupied();
                //Se há mais de uma tarefa e a máquina tem mais de um núcleo
            } else if (this.controleEscravos.get(index).getTasksInProcessing().size() > 1) {

                System.out.println("Houve Paralelismo");
            }
        }
        //Se há fila de tarefa na máquina
        if (!this.controleEscravos.get(index).getTasksOnHold().isEmpty()) {

            System.out.println("Houve Fila");
        }
    }

    @Override
    //Receber nova tarefa submetida ou tarefa que sofreu preemoção
    public void adicionarTarefa(final Tarefa tarefa) {

        //Method herdado, obrigatório executar para obter métricas ao final
        // da slimuação
        super.adicionarTarefa(tarefa);

        //Atualização da demanda do usuário proprietário da tarefa
        for (final UserControl userControl : this.status) {
            if (userControl.getUserId().equals(tarefa.getProprietario())) {
                userControl.increaseTaskDemand();
                break;
            }
        }

        //Em caso de preempção
        if (tarefa.getLocalProcessamento() != null) {

            //Localizar informações de estado de máquina que executou a
            // tarefa (se houver)
            final CS_Processamento maq =
                    (CS_Processamento) tarefa.getLocalProcessamento();

            //Localizar informações armazenadas sobre a preempção em particular

            int indexControlePreemp = -1;
            int indexStatusUserAlloc = -1;
            int indexStatusUserPreemp = -1;

            for (int j = 0; j < this.controlePreempcao.size(); j++) {
                if (this.controlePreempcao.get(j).preemptedTaskId() == tarefa.getIdentificador() && this.controlePreempcao.get(j).preemptedTaskUser().equals(tarefa.getProprietario())) {
                    indexControlePreemp = j;
                    break;
                }
            }

            for (int k = 0; k < this.status.size(); k++) {
                if (this.status.get(k).getUserId().equals(this.controlePreempcao.get(indexControlePreemp).allocatedTaskUser())) {
                    indexStatusUserAlloc = k;
                    break;
                }
            }

            for (int k = 0; k < this.status.size(); k++) {
                if (this.status.get(k).getUserId().equals(this.controlePreempcao.get(indexControlePreemp).preemptedTaskUser())) {
                    indexStatusUserPreemp = k;
                    break;
                }
            }

            //Localizar tarefa em espera deseignada para executar
            for (int i = 0; i < this.esperaTarefas.size(); i++) {

                if (this.esperaTarefas.get(i).getProprietario().equals(this.controlePreempcao.get(indexControlePreemp).allocatedTaskUser()) && this.esperaTarefas.get(i).getIdentificador() == this.controlePreempcao.get(indexControlePreemp).allocatedTaskId()) {

                    //Enviar tarefa para execução
                    this.mestre.sendTask(this.esperaTarefas.remove(i));

                    //Atualizar informações de estado do usuário cuja tarefa
                    // será executada
                    this.status.get(indexStatusUserAlloc).increaseAvailableProcessingPower(maq.getPoderComputacional());

                    //Atualizar informações de estado do usuáro cuja tarefa
                    // foi interrompida
                    this.status.get(indexStatusUserPreemp).decreaseAvailableProcessingPower(maq.getPoderComputacional());

                    //Com a preempção feita, os dados necessários para ela
                    // são eliminados
                    this.controlePreempcao.remove(indexControlePreemp);
                    //Encerrar laço
                    break;
                }
            }
        }
    }


}
