/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas;

import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;

/**
 *
 * @author Diogo Tavares
 */
public class TarefaVM extends Tarefa {
    
    //lista de atributos
    private CS_VirtualMac VM_enviada;
    
    public TarefaVM(CentroServico origem, CS_VirtualMac VM, double arquivoEnvio, double tempoCriacao){
        super(0,VM.getProprietario(),VM.getId(), origem, arquivoEnvio, 0, tempoCriacao);
        this.VM_enviada = VM;
    }

    public CS_VirtualMac getVM_enviada() {
        return VM_enviada;
    }

    public void setVM_enviada(CS_VirtualMac VM_enviada) {
        this.VM_enviada = VM_enviada;
    }
    
    
}
