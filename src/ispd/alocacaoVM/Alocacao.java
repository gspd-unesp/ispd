/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.alocacaoVM;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Diogo Tavares
 */
public abstract class Alocacao {
    protected List<CS_Processamento> maquinasFisicas; //lista de "escravos"
    protected List<List> infoMaquinas; // lista de informações armazenada sobre cada máquina física
    protected List<CS_VirtualMac> maquinasVirtuais; //lista de vms "tarefas"
    protected VMM VMM; //vmm responsável por implementar a política de alocação
    protected List<CS_VirtualMac> VMsRejeitadas;    
    protected List<List> caminhoMaquina;
    
    //iniciar a alocação
    public abstract void iniciar();
    //selecionar o critério de seleção da vm
    public abstract CS_VirtualMac escalonarVM();
    //selecionar o critério de seleção do recurso
    public abstract CS_Processamento escalonarRecurso();
    //implementar a rota até o recurso selecionado
    public abstract List<CentroServico> escalonarRota(CentroServico destino);
    //realiza o escalonamento de fato
    public abstract void escalonar();
    
    public abstract void migrarVM();
    
    public void addVM(CS_VirtualMac vm){
        maquinasVirtuais.add(vm);
    }

    public List<CS_Processamento> getMaquinasFisicas() {
        return maquinasFisicas;
    }

    public void setMaquinasFisicas(List<CS_Processamento> maquinasFisicas) {
        this.maquinasFisicas = maquinasFisicas;
    }
    
    public void addMaquinaFisica(CS_Processamento maq){
        this.maquinasFisicas.add(maq);
    }

    public List<CS_VirtualMac> getMaquinasVirtuais() {
        return maquinasVirtuais;
    }

    public void setMaquinasVirtuais(List<CS_VirtualMac> maquinasVirtuais) {
        this.maquinasVirtuais = maquinasVirtuais;
    }

    public VMM getVMM() {
        return VMM;
    }

    public void setVMM(CS_VMM hypervisor) {
        this.VMM = (ispd.alocacaoVM.VMM) hypervisor;
    }

    public List<List> getCaminhoMaquinas() {
        return caminhoMaquina;
    }

    public void setCaminhoMaquinas(List<List> caminhoMaquinas) {
        this.caminhoMaquina = caminhoMaquinas;
    }

    public List<CS_VirtualMac> getVMsRejeitadas() {
        return VMsRejeitadas;
    }
    
    
    
    
    /**
     * Indica o intervalo de tempo utilizado pelo escalonador para realizar atualização dos dados dos escravos
     * Retornar null para escalonadores estáticos, nos dinâmicos o método deve ser reescrito
     * @return Intervalo em segundos para atualização
     */
    public Double getTempoAtualizar(){
        return null;
    }
}


