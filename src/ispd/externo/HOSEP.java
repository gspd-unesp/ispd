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
 * M_OSEP.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Cássio Henrique Volpatto Forte;
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
import ispd.escalonador.Mestre;
import ispd.motor.Mensagens;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author cassio
 */
public class HOSEP extends Escalonador {

    ArrayList<StatusUser> status;
    List<ControleEscravos> controleEscravos;
    List<Tarefa> esperaTarefas;
    List<ControlePreempcao> controlePreempcao;
    List<List> processadorEscravos;
    
    public HOSEP() {

        this.tarefas = new ArrayList<>();
        this.escravos = new ArrayList<>();
        this.controleEscravos = new ArrayList<>();
        this.esperaTarefas = new ArrayList<>();
        this.controlePreempcao = new ArrayList<>();
        this.filaEscravo = new ArrayList<>();
        this.processadorEscravos = new ArrayList<>();
        this.status = new ArrayList();
    }

    @Override
    public void iniciar() {
        //Escalonamento quando chegam tarefas e quando tarefas são concluídas
        this.mestre.setTipoEscalonamento(Mestre.AMBOS);
        
        //Objetos de controle de uso e cota para cada um dos usuários
        for (int i = 0; i < metricaUsuarios.getUsuarios().size(); i++) {
            //Calcular o poder computacional da porção de cada usuário
            Double poderComp = 0.0;
            for (int j = 0; j < escravos.size(); j++) {
                //Se o nó corrente não é mestre e pertence ao usuário corrente
                if (!(escravos.get(j) instanceof Mestre) && escravos.get(j).getProprietario().equals(metricaUsuarios.getUsuarios().get(i))) {
                    //Calcular o poder total da porcao do usuário corrente
                    poderComp += escravos.get(j).getPoderComputacional();
                }
            }
            //Adiciona dados do usuário corrente à lista 
            status.add(new StatusUser(metricaUsuarios.getUsuarios().get(i), i, poderComp));
        }

        //Controle dos nós, com cópias das filas de cada um e da tarefa que executa em cada um
        for (int i = 0; i < escravos.size(); i++) {
            controleEscravos.add(new ControleEscravos(escravos.get(i).getId(),i,new ArrayList<Tarefa>(),new ArrayList<Tarefa>()));
        }
    }

    private int buscarTarefa(StatusUser usuario) {

        //Indice da tarefa na lista de tarefas
        int trf = -1;
        //Se o usuario tem demanda nao atendida e seu consumo nao chegou ao limite
        if (usuario.getDemanda() > 0) {
            //Procura pela menor tarefa nao atendida do usuario.
            for (int j = 0; j < tarefas.size(); j++) {
                if (tarefas.get(j).getProprietario().equals(usuario.getNome())) {
                    if (trf == -1) {
                        trf = j;
                    } else if (tarefas.get(j).getTamProcessamento() < tarefas.get(trf).getTamProcessamento()) {//Escolher a tarefa de menor tamanho do usuario
                        trf = j;
                    }
                }
            }
        }
        return trf;
    }
    
    private int buscarRecurso(StatusUser cliente) {

        /*++++++++++++++++++Buscando recurso livres++++++++++++++++++*/
        
        //Índice da máquina escolhida, na lista de máquinas
        int indexSelec = -1;
        
        for (int i = 0; i < escravos.size(); i++) {
            
            if (controleEscravos.get(i).getStatus().equals("Livre")) {
                if (indexSelec == -1) {
                    indexSelec = i;
                } else if (escravos.get(i).getPoderComputacional() > escravos.get(indexSelec).getPoderComputacional()) {
                    indexSelec = i;
                }
            }
        }

        if (indexSelec != -1) {
            return indexSelec;
        }
        
        /*+++++++++++++++++Busca por usuário para preempção+++++++++++++++++*/
        
        if( status.get(status.size() -1).getServedPerf() > status.get(status.size() -1).getPerfShare() && cliente.getServedPerf() < cliente.getPerfShare() ){
    
            for (int i = 0; i < escravos.size(); i++) {

                if (controleEscravos.get(i).getStatus().equals("Ocupado")) {
                    if (controleEscravos.get(i).GetProcessador().get(0).getProprietario().equals(status.get(status.size() -1).getNome())) {

                        if (indexSelec == -1) {

                            indexSelec = i;

                        } else {
                            if (escravos.get(i).getPoderComputacional() < escravos.get(indexSelec).getPoderComputacional()) {

                                indexSelec = i;
                            }
                        }
                    }
                }
            }
            
            if( indexSelec != -1 ){
                
                Double penalidaUserEsperaPosterior = (cliente.getServedPerf() + escravos.get(indexSelec).getPoderComputacional() - cliente.getPerfShare()) / cliente.getPerfShare();
                Double penalidaUserEscravoPosterior = (status.get(status.size() -1).getServedPerf() - escravos.get(indexSelec).getPoderComputacional() - status.get(status.size() -1).getPerfShare()) / status.get(status.size() -1).getPerfShare();
  
                if (penalidaUserEscravoPosterior >= penalidaUserEsperaPosterior || penalidaUserEscravoPosterior > 0) {
                    return indexSelec;
                } else {
                    return -1;
                }
            }
        }
        return indexSelec;
    }
    
    @Override
    public List<CentroServico> escalonarRota(CentroServico destino) {
        int index = escravos.indexOf(destino);
        return new ArrayList<>((List<CentroServico>) caminhoEscravo.get(index));
    }

    @Override
    public void resultadoAtualizar(Mensagem mensagem) {
        //super.resultadoAtualizar(mensagem);
        //Localizar máquina que enviou estado atualizado
        int index = escravos.indexOf(mensagem.getOrigem());
        
        //Atualizar listas de espera e processamento da máquina
        controleEscravos.get(index).setProcessador((ArrayList<Tarefa>) mensagem.getProcessadorEscravo());
        controleEscravos.get(index).setFila((ArrayList<Tarefa>) mensagem.getFilaEscravo());

        //Tanto alocação para recurso livre como a preempção levam dois ciclos de atualização para que a máquina possa ser considerada para esacalonamento novamente 
        
        //Primeiro ciclo
        if (controleEscravos.get(index).getStatus().equals("Bloqueado")) {
            controleEscravos.get(index).setIncerto();
        //Segundo ciclo
        } else if (controleEscravos.get(index).getStatus().equals("Incerto")) {
            //Se não está executando nada
            if (controleEscravos.get(index).GetProcessador().isEmpty()) {

                controleEscravos.get(index).setLivre();
            //Se está executando uma tarefa
            } else if (controleEscravos.get(index).GetProcessador().size() == 1) {

                controleEscravos.get(index).setOcupado();
            //Se há mais de uma tarefa e a máquina tem mais de um núcleo
            } else if (controleEscravos.get(index).GetProcessador().size() > 1) {

                System.out.println("Houve Paralelismo");
            }
        }
        //Se há fila de tarefa na máquina
        if (controleEscravos.get(index).GetFila().size() > 0) {

            System.out.println("Houve Fila");
        }
    }

    @Override
    public void escalonar() {

        int indexTarefa;
        int indexEscravo;
        StatusUser cliente;

        //Ordenar os usuários em ordem crescente de Poder Remanescente
        Collections.sort(status);

        for (int i = 0; i < status.size(); i++) {
            cliente = status.get(i);

            //Buscar tarefa para execucao
            indexTarefa = buscarTarefa(cliente);

            if (indexTarefa != -1) {

                //Buscar máquina para executar a tarefa definida
                indexEscravo = buscarRecurso(cliente);
                
                if (indexEscravo != -1) {

                    //Se não é caso de preempção, a tarefa é configurada e enviada
                    if (controleEscravos.get(indexEscravo).getStatus().equals("Livre")) {

                        Tarefa tar = tarefas.remove(indexTarefa);
                        tar.setLocalProcessamento(escravos.get(indexEscravo));
                        tar.setCaminho(escalonarRota(escravos.get(indexEscravo)));
                        mestre.enviarTarefa(tar);

                        //Atualização dos dados sobre o usuário
                        cliente.rmDemanda();
                        cliente.addServedPerf(escravos.get(indexEscravo).getPoderComputacional());
                        
                        //Controle das máquinas
                        controleEscravos.get(indexEscravo).setBloqueado();
                        return;
                    }

                    //Se é caso de preempção, a tarefa configurada e colocada em espera
                    if (controleEscravos.get(indexEscravo).getStatus().equals("Ocupado")) {

                        Tarefa tar = tarefas.remove(indexTarefa);
                        tar.setLocalProcessamento(escravos.get(indexEscravo));
                        tar.setCaminho(escalonarRota(escravos.get(indexEscravo)));

                        //Controle de preempção para enviar a nova tarefa no momento certo
                        String userPreemp = ((Tarefa) controleEscravos.get(indexEscravo).GetProcessador().get(0)).getProprietario();
                        int idTarefaPreemp = ((Tarefa) controleEscravos.get(indexEscravo).GetProcessador().get(0)).getIdentificador();
                        controlePreempcao.add(new ControlePreempcao(userPreemp, idTarefaPreemp, tar.getProprietario(), tar.getIdentificador()));
                        esperaTarefas.add(tar);

                        //Solicitação de retorno da tarefa em execução e atualização da demanda do usuário
                        mestre.enviarMensagem((Tarefa) controleEscravos.get(indexEscravo).GetProcessador().get(0), escravos.get(indexEscravo), Mensagens.DEVOLVER_COM_PREEMPCAO);
                        controleEscravos.get(indexEscravo).setBloqueado();
                        cliente.rmDemanda();
                        return;
                    }
                }
            }
        }
   }

    @Override
    //Chegada de tarefa concluida
    public void addTarefaConcluida(Tarefa tarefa) {
        //Método herdado, obrigatório executar para obter métricas ao final da simulação
        super.addTarefaConcluida(tarefa);

        //Localizar informações sobre máquina que executou a tarefa e usuário proprietário da tarefa
        CS_Processamento maq = (CS_Processamento) tarefa.getLocalProcessamento();
        int maqIndex = escravos.indexOf(maq);

        if (controleEscravos.get(maqIndex).getStatus().equals("Ocupado")) {

            int statusIndex = -1;

            for (int i = 0; i < status.size(); i++) {
                if (status.get(i).getNome().equals(tarefa.getProprietario())) {
                    statusIndex = i;
                }
            }

            //Atualização das informações de estado do proprietario da tarefa terminada.
            status.get(statusIndex).rmServedPerf(maq.getPoderComputacional());
            controleEscravos.get(maqIndex).setLivre();
            
        } else if (controleEscravos.get(maqIndex).getStatus().equals("Bloqueado")) {

            int indexControlePreemp = -1;
            int indexStatusUserAlloc = -1;
            int indexStatusUserPreemp = -1;

            for (int j = 0; j < controlePreempcao.size(); j++) {
                if (controlePreempcao.get(j).getPreempID() == tarefa.getIdentificador() && controlePreempcao.get(j).getUsuarioPreemp().equals(tarefa.getProprietario())) {
                    indexControlePreemp = j;
                    break;
                }
            }

            for (int k = 0; k < status.size(); k++) {
                if (status.get(k).getNome().equals(controlePreempcao.get(indexControlePreemp).getUsuarioAlloc())) {
                    indexStatusUserAlloc = k;
                    break;
                }
            }

            for (int k = 0; k < status.size(); k++) {
                if (status.get(k).getNome().equals(controlePreempcao.get(indexControlePreemp).getUsuarioPreemp())) {
                    indexStatusUserPreemp = k;
                    break;
                }
            }

            //Localizar tarefa em espera designada para executar
            for (int i = 0; i < esperaTarefas.size(); i++) {
                if (esperaTarefas.get(i).getProprietario().equals(controlePreempcao.get(indexControlePreemp).getUsuarioAlloc()) && esperaTarefas.get(i).getIdentificador() == controlePreempcao.get(indexControlePreemp).getAllocID()) {

                    //Enviar tarefa para execução
                    mestre.enviarTarefa(esperaTarefas.remove(i));

                    //Atualizar informações de estado do usuário cuja tarefa será executada
                    status.get(indexStatusUserAlloc).addServedPerf(maq.getPoderComputacional());
                    
                    //Atualizar informações de estado do usuário cuja tarefa teve a execução interrompida
                    status.get(indexStatusUserPreemp).rmServedPerf(maq.getPoderComputacional());
                    
                    //Com a preempção feita, os dados necessários para ela são eliminados
                    controlePreempcao.remove(indexControlePreemp);
                    //Encerrar laço
                    break;
                }
            }
        }
    }

    @Override
    //Receber nova tarefa submetida ou tarefa que sofreu preemoção
    public void adicionarTarefa(Tarefa tarefa) {

        //Método herdado, obrigatório executar para obter métricas ao final da slimuação
        super.adicionarTarefa(tarefa);
        
        //Atualização da demanda do usuário proprietário da tarefa
        for (int i = 0; i < status.size(); i++) {
            if (status.get(i).getNome().equals(tarefa.getProprietario())) {
                status.get(i).addDemanda();
                break;
            }
        }

        //Em caso de preempção
        if ((CS_Processamento) tarefa.getLocalProcessamento() != null) {
            
            //Localizar informações de estado de máquina que executou a tarefa (se houver)
            CS_Processamento maq = (CS_Processamento) tarefa.getLocalProcessamento();
            
            //Localizar informações armazenadas sobre a preempção em particular

            int indexControlePreemp = -1;
            int indexStatusUserAlloc = -1;
            int indexStatusUserPreemp = -1 ;
            
            for (int j = 0; j < controlePreempcao.size(); j++) {
                if (controlePreempcao.get(j).getPreempID() == tarefa.getIdentificador() && controlePreempcao.get(j).getUsuarioPreemp().equals(tarefa.getProprietario())) {
                    indexControlePreemp = j;
                    break;
                }
            }
            
            for( int k = 0; k < status.size(); k++ ){
                if(status.get(k).getNome().equals(controlePreempcao.get(indexControlePreemp).getUsuarioAlloc())){
                    indexStatusUserAlloc = k;
                    break;
                }
            }
            
            for( int k = 0; k < status.size(); k++ ){
                if(status.get(k).getNome().equals(controlePreempcao.get(indexControlePreemp).getUsuarioPreemp())){
                    indexStatusUserPreemp = k;
                    break;
                }
            }
            
            //Localizar tarefa em espera deseignada para executar
            for (int i = 0; i < esperaTarefas.size(); i++) {

                if (esperaTarefas.get(i).getProprietario().equals(controlePreempcao.get(indexControlePreemp).getUsuarioAlloc()) && esperaTarefas.get(i).getIdentificador() == controlePreempcao.get(indexControlePreemp).getAllocID()) {

                    //Enviar tarefa para execução
                    mestre.enviarTarefa(esperaTarefas.remove(i));

                    //Atualizar informações de estado do usuário cuja tarefa será executada
                    status.get(indexStatusUserAlloc).addServedPerf(maq.getPoderComputacional());
                    
                    //Atualizar informações de estado do usuáro cuja tarefa foi interrompida
                    status.get(indexStatusUserPreemp).rmServedPerf(maq.getPoderComputacional());
                    
                    //Com a preempção feita, os dados necessários para ela são eliminados
                    controlePreempcao.remove(indexControlePreemp);
                    //Encerrar laço
                    break;
                }
            }
        }
    }

    //Definir o intervalo de tempo, em segundos, em que as máquinas enviarão dados de atualização para o escalonador
    @Override
    public Double getTempoAtualizar() {
        return 15.0;
    }

    //Metodo necessario para implementar interface. Não é usado.
    @Override
    public Tarefa escalonarTarefa() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //Metodo necessario para implementar interface. Não é usado.
    @Override
    public CS_Processamento escalonarRecurso() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //Classe para dados de estado dos usuários
    private class StatusUser implements Comparable<StatusUser> {

        private String user;//Nome do usuario;
        private int indexUser;//Índice do usuário;
        private int demanda;//Número de tarefas na fila
        private int ownerShare;//Número de máquinas do usuario
        private double perfShare;//Desempenho total das máquinas do usuário
        private double powerShare;//Consumo de energia total das máquinas do usuário
        private int servedNum;//Número de máquinas que atendem ao usuário
        private double servedPerf;//Desempenho total que atende ao usuário
        private double servedPower;//Consumo de energia total que atende ao usuario
        private double limiteConsumo;//Limite de consumo definido pelo usuario;
        private double relacaoEficienciaSistemaPorcao;//Nova métrica para decisão de preempção

        public StatusUser(String user, int indexUser, double perfShare) {
            this.user = user;
            this.indexUser = indexUser;
            this.demanda = 0;
            this.perfShare = perfShare;
            this.powerShare = 0.0;
            this.servedNum = 0;
            this.servedPerf = 0.0;
            this.servedPower = 0.0;
            this.limiteConsumo = 0.0;
        }

        public void calculaRelacaoEficienciaEficienciaSisPor(Double poderSis, Double consumoSis) {
            this.relacaoEficienciaSistemaPorcao = ((poderSis / consumoSis) / (this.perfShare / this.powerShare));
        }

        public void addDemanda() {
            this.demanda++;
        }

        public void rmDemanda() {
            this.demanda--;
        }

        public void setLimite(Double lim) {
            this.limiteConsumo = lim;
        }

        public void setPowerShare(Double power) {
            this.powerShare = power;
        }

        public void addServedNum() {
            this.servedNum++;
        }

        public void rmServedNum() {
            this.servedNum--;
        }

        public void addServedPerf(Double perf) {
            this.servedPerf += perf;
        }

        public void rmServedPerf(Double perf) {
            this.servedPerf -= perf;
        }

        public void addServedPower(Double power) {
            this.servedPower += power;
        }

        public void rmServedPower(Double power) {
            this.servedPower -= power;
        }

        public String getNome() {
            return this.user;
        }

        public int getIndexUser() {
            return this.indexUser;
        }

        public int getDemanda() {
            return this.demanda;
        }

        public Double getLimite() {
            return this.limiteConsumo;
        }

        public int getOwnerShare() {
            return this.ownerShare;
        }

        public double getPerfShare() {
            return this.perfShare;
        }

        public double getPowerShare() {
            return this.powerShare;
        }

        public int getServedNum() {
            return this.servedNum;
        }

        public double getServedPerf() {
            return this.servedPerf;
        }

        public double getServedPower() {
            return servedPower;
        }

        //public double getEficienciaRelativa() {
        //    return this.eficienciaEnergeticaRelativa;
        //}

        //Comparador para ordenação
        @Override
        public int compareTo(StatusUser o) {
            if (((this.servedPerf - this.perfShare) / this.perfShare) < ((o.getServedPerf() - o.getPerfShare()) / o.getPerfShare())) {
                return -1;
            }
            if (((this.servedPerf - this.perfShare) / this.perfShare) > ((o.getServedPerf() - o.getPerfShare()) / o.getPerfShare())) {
                return 1;
            }
            if (this.perfShare >= o.getPerfShare()) {
                return -1;
            } else {
                return 1;
            }
        }
    }
   

    //Classe para arnazenar o estado das máquinas no sistema
    private class ControleEscravos {

        private String status;//Estado da máquina
        private String ID;//Id da máquina escravo
        private int index;//Índice na lista de escravos
        private ArrayList<Tarefa> fila;
        private ArrayList<Tarefa> processador;

        public ControleEscravos(String Ident, int ind, ArrayList<Tarefa> F, ArrayList<Tarefa> P) {
            this.status = "Livre";
            this.ID = Ident;
            this.index = ind;
            this.fila = F;
            this.processador = P;
        }

        public String getID() {
            return ID;
        }
        
        public int GetIndex(){
            return index;
        }
        
        public ArrayList<Tarefa> GetFila(){
            return fila;
        }
        
        public ArrayList<Tarefa> GetProcessador(){
            return processador;
        }

        public String getStatus() {
            return status;
        }
        
        public void setFila(ArrayList<Tarefa> F){
            this.fila = F;
        }
        
        public void setProcessador(ArrayList<Tarefa> P){
            this.processador = P;
        }

        public void setOcupado() {
            this.status = "Ocupado";
        }

        public void setLivre() {
            this.status = "Livre";
        }

        public void setBloqueado() {
            this.status = "Bloqueado";
        }

        public void setIncerto() {
            this.status = "Incerto";
        }
    }

    //Classe para armazenar dados sobre as preempções que ainda não terminaram
    public class ControlePreempcao {

        private final String usuarioPreemp;
        private final String usuarioAlloc;
        private final int preempID;//ID da tarefa que sofreu preempção
        private final int allocID;//ID da tarefa alocada

        public ControlePreempcao(String user1, int pID, String user2, int aID) {
            this.usuarioPreemp = user1;
            this.preempID = pID;
            this.usuarioAlloc = user2;
            this.allocID = aID;
        }

        public String getUsuarioPreemp() {
            return this.usuarioPreemp;
        }

        public int getPreempID() {
            return this.preempID;
        }

        public String getUsuarioAlloc() {
            return this.usuarioAlloc;
        }

        public int getAllocID() {
            return this.allocID;
        }
    }
}
