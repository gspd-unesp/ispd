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
 * OSEP.java
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
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author cassio
 */
public class OSEP extends Escalonador {

    Tarefa tarefaSelec;
    HashMap<String,StatusUser> status;
    List<ControleEscravos> controleEscravos;
    List<Tarefa> esperaTarefas;
    List<ControlePreempcao> controlePreempcao;
    int contadorEscravos;
    List<List> processadorEscravos;
    
    public OSEP() {
        this.tarefas = new ArrayList<>();
        this.escravos = new ArrayList<>();
        this.controleEscravos = new ArrayList<>();
        this.esperaTarefas = new ArrayList<>();
        this.controlePreempcao = new ArrayList<>();
        this.filaEscravo = new ArrayList<>();
        this.processadorEscravos = new ArrayList<>();
        this.contadorEscravos = 0;
    }

    @Override
    public void iniciar() {
        this.mestre.setTipoEscalonamento(Mestre.AMBOS);//Escalonamento quando chegam tarefas e quando tarefas são concluídas
        status = new HashMap<>();

        for (int i = 0; i < metricaUsuarios.getUsuarios().size(); i++) {//Objetos de controle de uso e cota para cada um dos usuários
            status.put(metricaUsuarios.getUsuarios().get(i),new StatusUser(metricaUsuarios.getUsuarios().get(i), i, metricaUsuarios.getPoderComputacional(metricaUsuarios.getUsuarios().get(i))));
        }

        for (int i = 0; i < escravos.size(); i++) {//Contadores para lidar com a dinamicidade dos dados
            controleEscravos.add(new ControleEscravos(escravos.get(i).getId()));
            filaEscravo.add(new ArrayList<Tarefa>());
            processadorEscravos.add(new ArrayList<Tarefa>());
        }
    }

    @Override
    public Tarefa escalonarTarefa() {
        //Usuários com maior diferença entre uso e posse terão preferência
        int difUsuarioMinimo =  -1;
        int indexUsuarioMinimo = -1;
        String user;
        //Encontrar o usuário que está mais abaixo da sua propriedade 
        for (int i = 0; i < metricaUsuarios.getUsuarios().size(); i++) {
            user = metricaUsuarios.getUsuarios().get(i);
            
            //Caso existam tarefas do usuário corrente e ele esteja com uso menor que sua posse
            if ((status.get(user).getServedNum() < status.get(user).getOwnerShare()) && status.get(user).getDemanda() > 0) {

                if (difUsuarioMinimo == -1) {
                    difUsuarioMinimo = status.get(user).getOwnerShare() - status.get(user).getServedNum();
                    indexUsuarioMinimo = i;
                } else {
                    if (difUsuarioMinimo < status.get(user).getOwnerShare() - status.get(user).getServedNum()) {
                        difUsuarioMinimo = status.get(user).getOwnerShare() - status.get(user).getServedNum();
                        indexUsuarioMinimo = i;
                    }

                }

            }

        }

        if (indexUsuarioMinimo != -1) {
            int indexTarefa = -1;

            for (int i = 0; i < tarefas.size(); i++) {
                if (tarefas.get(i).getProprietario().equals(metricaUsuarios.getUsuarios().get(indexUsuarioMinimo))) {
                    if (indexTarefa == -1) {
                        indexTarefa = i;
                        break;
                    } 
                }
            }

            if (indexTarefa != -1) {
                return tarefas.remove(indexTarefa);
            }

        }

        if (tarefas.size() > 0) {
            return tarefas.remove(0);
        } else {
            return null;
        }

    }

    @Override
    public CS_Processamento escalonarRecurso() {
        String user;
        //Buscando recurso livre
        CS_Processamento selec = null;

        for (int i = 0; i < escravos.size(); i++) {

            if (controleEscravos.get(i).getStatus().equals("Livre")) {//Garantir que o escravo está de fato livre e que não há nenhuma tarefa em trânsito para o escravo
                if (selec == null) {

                    selec = escravos.get(i);
                    break;

                }

            }

        }

        if (selec != null) {

            //controleEscravos.get(escravos.indexOf(selec)).setBloqueado();//Inidcar que uma tarefa será enviada e que , portanto , este escravo deve ser bloqueada até a próxima atualização

            return selec;

        }

        String usermax = null;
        int diff = -1;

        for (int i = 0; i < metricaUsuarios.getUsuarios().size(); i++) {
            user = metricaUsuarios.getUsuarios().get(i);
            if (status.get(user).getServedNum() > status.get(user).getOwnerShare() && !user.equals(tarefaSelec.getProprietario())) {

                if (diff == -1) {

                    usermax = metricaUsuarios.getUsuarios().get(i);
                    diff = status.get(user).getServedNum() - status.get(user).getOwnerShare();

                } else {

                    if (status.get(user).getServedNum() - status.get(user).getOwnerShare() > diff) {

                        usermax = user;
                        diff = status.get(user).getServedNum() - status.get(user).getOwnerShare();

                    }

                }

            }

        }

        int index = -1;
        if (usermax != null) {

            for (int i = 0; i < escravos.size(); i++) {
                if (controleEscravos.get(i).getStatus().equals("Ocupado") && ((Tarefa) processadorEscravos.get(i).get(0)).getProprietario().equals(usermax)) {
                        index = i;
                        break;
                }

            }

        }

        //Fazer a preempção
        if (index != -1) {
            selec = escravos.get(index);
            int index_selec = escravos.indexOf(selec);
            //controleEscravos.get(escravos.indexOf(selec)).setBloqueado();
            mestre.enviarMensagem((Tarefa) processadorEscravos.get(index_selec).get(0), selec, Mensagens.DEVOLVER_COM_PREEMPCAO);
            return selec;
        }
        return null;
    }

    @Override
    public List<CentroServico> escalonarRota(CentroServico destino) {
        int index = escravos.indexOf(destino);
        return new ArrayList<>((List<CentroServico>) caminhoEscravo.get(index));
    }

    @Override
    public void resultadoAtualizar(Mensagem mensagem) {
        super.resultadoAtualizar(mensagem);
        int index = escravos.indexOf(mensagem.getOrigem());
        processadorEscravos.set(index, mensagem.getProcessadorEscravo());
        contadorEscravos++;
        if (contadorEscravos == escravos.size()) {
            boolean escalona = false;
            for (int i = 0; i < escravos.size(); i++) {
                if(controleEscravos.get(i).getStatus().equals("Bloqueado")){
                    controleEscravos.get(i).setIncerto();
                }
                if(controleEscravos.get(i).getStatus().equals("Incerto")){
                    if( processadorEscravos.get(i).isEmpty() ){
                        controleEscravos.get(i).setLivre();
                        escalona = true;
                    }
                    if( processadorEscravos.size() == 1){
                        controleEscravos.get(i).setOcupado();
                    }
                    if( processadorEscravos.size() > 1){
                        System.out.println("Houve Fila");
                    }
                }
            }
            contadorEscravos = 0;
            if (tarefas.size() > 0 && escalona) {
                mestre.executarEscalonamento();
            }
        }
    }

    @Override
    public void escalonar() {
        Tarefa trf = escalonarTarefa();
        if (trf != null) {
            tarefaSelec = trf;
            StatusUser estado = status.get(trf.getProprietario());
            CS_Processamento rec = escalonarRecurso();
            if (rec != null) {
                trf.setLocalProcessamento(rec);
                trf.setCaminho(escalonarRota(rec));
                //Verifica se não é caso de preempção
                if (controleEscravos.get(escravos.indexOf(rec)).getStatus().equals("Livre")) {
                    
                    estado.rmDemanda();
                    estado.addServedNum();
                    
                    controleEscravos.get(escravos.indexOf(rec)).setBloqueado();
                    mestre.enviarTarefa(trf);
                    
                } else {
                    
                    if(controleEscravos.get(escravos.indexOf(rec)).getStatus().equals("Ocupado")){
                        int index_rec = escravos.indexOf(rec);
                        esperaTarefas.add(trf);
                        controlePreempcao.add(new ControlePreempcao(((Tarefa) processadorEscravos.get(index_rec).get(0)).getProprietario(), ((Tarefa) processadorEscravos.get(index_rec).get(0)).getIdentificador(), trf.getProprietario(), trf.getIdentificador()));
                        controleEscravos.get(escravos.indexOf(rec)).setBloqueado();
                    }
                }
                
                //System.out.println("Tarefa " + trf.getIdentificador() + " do user " + trf.getProprietario() + " foi escalonado" + mestre.getSimulacao().getTime());
                //System.out.printf("Escravo %s executando %d\n", rec.getId(), rec.getInformacaoDinamicaProcessador().size());
                for (int i = 0; i < escravos.size(); i++) {
                    if (processadorEscravos.get(i).size() > 1) {
                        System.out.printf("Escravo %s executando %d\n", escravos.get(i).getId(), processadorEscravos.get(i).size());
                        System.out.println("PROBLEMA1");
                    }
                    if (filaEscravo.get(i).size() > 0) {
                        System.out.println("Tem Fila");
                    }
                }
                //mestre.enviarTarefa(trf);

            } else {
                tarefas.add(trf);
                tarefaSelec = null;
            }
        }
        //System.out.println("Tempo :" + mestre.getSimulacao().getTime());
        //for (int i = 0; i < status.size(); i++) {
        //    System.out.printf("Usuário %s : %f de %f\n", status.get(i).usuario, status.get(i).GetNumUso(), status.get(i).GetNumCota());
        //}

    }

    @Override
    public void addTarefaConcluida(Tarefa tarefa) {
        super.addTarefaConcluida(tarefa);
        CS_Processamento maq = (CS_Processamento) tarefa.getLocalProcessamento();
        StatusUser estado = status.get(tarefa.getProprietario());
        
        estado.rmServedNum();
        int index = escravos.indexOf(maq);
        controleEscravos.get(index).setLivre();
    }

    @Override
    public void adicionarTarefa(Tarefa tarefa) {
        super.adicionarTarefa(tarefa);
        CS_Processamento maq = (CS_Processamento) tarefa.getLocalProcessamento();
        StatusUser estadoUser = status.get(tarefa.getProprietario());
        //Em caso de preempção, é procurada a tarefa correspondente para ser enviada ao escravo agora desocupado
        if (tarefa.getLocalProcessamento() != null) {

            //contadores_escravos.get(escravos.indexOf(maq)).SetOcupado();
            //System.out.printf("Tarefa %d do usuário %s sofreu preempção\n", tarefa.getIdentificador(), tarefa.getProprietario());

            int j;
            int indexControle = -1;
            for (j = 0; j < controlePreempcao.size(); j++) {
                if (controlePreempcao.get(j).getPreempID() == tarefa.getIdentificador() && controlePreempcao.get(j).getUsuarioPreemp().equals(tarefa.getProprietario())) {
                    indexControle = j;
                    break;
                }
            }

            for (int i = 0; i < esperaTarefas.size(); i++) {
                if (esperaTarefas.get(i).getProprietario().equals(controlePreempcao.get(indexControle).getUsuarioAlloc()) && esperaTarefas.get(i).getIdentificador() == controlePreempcao.get(j).getAllocID()) {
                    
                    mestre.enviarTarefa(esperaTarefas.get(i));
                    
                    status.get(controlePreempcao.get(indexControle).getUsuarioAlloc()).addServedNum();
                    
                    status.get(controlePreempcao.get(indexControle).getUsuarioPreemp()).addDemanda();
                    status.get(controlePreempcao.get(indexControle).getUsuarioPreemp()).rmServedNum();
                    
                    controleEscravos.get( escravos.indexOf(maq) ).setBloqueado();
                    
                    esperaTarefas.remove(i);
                    controlePreempcao.remove(j);
                    break;
                }
            }

            //System.out.println("Tempo :" + mestre.getSimulacao().getTime());
            //for (int i = 0; i < status.size(); i++) {
            //System.out.printf("Usuário %s : %f de %f\n", status.get(i).usuario, status.get(i).GetUso(), status.get(i).GetCota());
            //}
        } else {
            //System.out.println("Tarefa " + tarefa.getIdentificador() + " do user " + tarefa.getProprietario() + " chegou " + mestre.getSimulacao().getTime());
            mestre.executarEscalonamento();
            estadoUser.addDemanda();
        }
    }

    @Override
    public Double getTempoAtualizar() {
        return 15.0;
    }

    private class StatusUser {

        private String user;//Nome do usuario;
        private int indexUser;//Índice do usuário;
        private int demanda;//Número de tarefas na fila
        private int indexTarefaMax;//Índice da maior tarefa na fila
        private int indexTarefaMin;//Índice da menor tarefa na fila
        private int ownerShare;//Número de máquinas do usuario
        private double perfShare;//Desempenho total das máquinas do usuário
        private double powerShare;//Consumo de energia total das máquinas do usuário
        private int servedNum;//Número de máquinas que atendem ao usuário
        private double servedPerf;//Desempenho total que atende ao usuário
        private double servedPower;//Consumo de energia total que atende ao usuario
        //private double eficienciaMedia;//Eficiência média das máquinas do usuário
        //private double desvioEficienciaMedia;

        public StatusUser( String user, int indexUser, double perfShare) {
            this.user = user;
            this.indexUser = indexUser;
            this.demanda = 0;
            this.indexTarefaMax = -1;
            this.indexTarefaMin = -1;
            this.ownerShare = 0;
            this.perfShare = perfShare;
            this.powerShare = 0.0;
            this.servedNum = 0;
            this.servedPerf = 0.0;
            this.servedPower = 0.0;
            //this.eficienciaMedia = 0.0;
            //this.desvioEficienciaMedia = 0.0;
            
           int i;
            int j = 0;
            for( i=0 ; i < escravos.size(); i++){
                if( escravos.get(i).getProprietario().equals(user) ){
                    j++;
                    //this.eficienciaMedia += escravos.get(i).getPoderComputacional()/escravos.get(i).getConsumoEnergia();
                }
            }
            this.ownerShare = j;
            //this.eficienciaMedia = this.eficienciaMedia/j;
            
            
            /*
            for(i=0; i< escravos.size(); i++){
                if( escravos.get(i).getProprietario().equals(user) ){
                    this.desvioEficienciaMedia += Math.pow((escravos.get(i).getPoderComputacional()/escravos.get(i).getConsumoEnergia())-this.eficienciaMedia, 2);
                }
            }
            this.desvioEficienciaMedia = this.desvioEficienciaMedia/(j-1);
            this.desvioEficienciaMedia = Math.sqrt(this.desvioEficienciaMedia);
            */
        }
        
        public void addDemanda(){
            this.demanda++;
        }
        
        public void rmDemanda(){
            this.demanda--;
        }
        
        public void setTarefaMinima(int index){
            this.indexTarefaMin = index;
        }
        
        public void setTarefaMaxima(int index){
            this.indexTarefaMax = index;
        }
        
        public void addShare(){
            this.ownerShare++;
        }
        
        public void addPowerShare( Double power ){
            this.powerShare += power;
        }
        
        public void addServedNum(){
            this.servedNum++;
        }
        
        public void rmServedNum(){
            this.servedNum--;
        }
        
        public void addServedPerf( Double perf ){
            this.servedPerf += perf;
        }
        
        public void rmServedPerf( Double perf ){
            this.servedPerf -= perf;
        }
  
        public void addServedPower( Double power ){
            this.servedPerf += power;
        }
        
        public void rmServedPower( Double power ){
            this.servedPerf -= power;
        }
        
        public String getUser() {
            return user;
        }

        public int getIndexUser() {
            return indexUser;
        }

        public int getDemanda() {
            return demanda;
        }

        public int getIndexTarefaMax() {
            return indexTarefaMax;
        }

        public int getIndexTarefaMin() {
            return indexTarefaMin;
        }
        
        /*public Double getEficienciaMedia(){
            return this.eficienciaMedia;
        }
        
        public Double getDesvioEficiencia(){
            return this.desvioEficienciaMedia;
        }*/

        public int getOwnerShare() {
            return ownerShare;
        }

        public double getPerfShare() {
            return perfShare;
        }

        public double getPowerShare() {
            return powerShare;
        }

        public int getServedNum() {
            return servedNum;
        }

        public double getServedPerf() {
            return servedPerf;
        }

        public double getServedPower() {
            return servedPower;
        }
   }

    private class ControleEscravos {

        private String status;//Estado da máquina
        private String ID;//Id da máquina escravo

        public ControleEscravos( String ID ) {
            this.status = "Livre";
            this.ID = ID;
        }

        public String getID() {
            return ID;
        }

        public String getStatus() {
            return status;
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

    public class ControlePreempcao {

        private String usuarioPreemp;
        private String usuarioAlloc;
        private int preempID;//ID da tarefa que sofreu preempção 
        private int allocID;//ID da tarefa alocada

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
