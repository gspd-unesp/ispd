/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.AlocacaoVM;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import java.util.List;

/**
 *
 * @author Diogo Tavares
 */
public abstract class Alocacao {
    private List<CS_Processamento> maquinasFisicas; //lista de "escravos"
    private List<CS_VirtualMac> maquinasVirtuais; //lista de vms "tarefas"
    private CS_VMM hypervisor; //vmm responsável por implementar a política de alocação
    
    protected List<List> caminhoMaquinas;
    
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

    
    /**
     * Indica o intervalo de tempo utilizado pelo escalonador para realizar atualização dos dados dos escravos
     * Retornar null para escalonadores estáticos, nos dinâmicos o método deve ser reescrito
     * @return Intervalo em segundos para atualização
     */
    public Double getTempoAtualizar(){
        return null;
    }   
    
    

}

