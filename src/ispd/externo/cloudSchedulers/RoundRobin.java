/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.externo.cloudSchedulers;

import ispd.escalonadorCloud.EscalonadorCloud;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Implementação do algoritmo de escalonamento Round-Robin
 * Atribui a proxima tarefa da fila (FIFO)
 * para o proximo recurso de uma fila circular de recursos
 * @author denison_usuario
 */
public class RoundRobin extends EscalonadorCloud{
    private ListIterator<CS_Processamento> recursos;
    private LinkedList<CS_Processamento> EscravosUsuario;
    private String usuario;
    
    public RoundRobin(){
        this.tarefas = new ArrayList<Tarefa>();
        this.escravos = new LinkedList<CS_Processamento>();
        
    }

    @Override
    public void iniciar() {
        System.out.println("iniciou escalonamento RR");
        this.EscravosUsuario = new LinkedList<CS_Processamento>();
        recursos = EscravosUsuario.listIterator(0);
       
        
    }

    @Override
    public Tarefa escalonarTarefa() {
        return tarefas.remove(0);
    }

    @Override
    public CS_Processamento escalonarRecurso() {
         if (recursos.hasNext()) {
            return recursos.next();
        }else{
            recursos = EscravosUsuario.listIterator(0);
            return recursos.next();
        }
    }

    @Override
    public void escalonar() {
        System.out.println("---------------------------");
        Tarefa trf = escalonarTarefa();
        usuario = trf.getProprietario();
        EscravosUsuario = (LinkedList<CS_Processamento>) getVMsAdequadas(usuario, escravos);
        if(!EscravosUsuario.isEmpty()){
        
        CS_Processamento rec = escalonarRecurso();
        System.out.println("escalonando tarefa " + trf.getIdentificador() + " para:" + rec.getId());
        trf.setLocalProcessamento(rec);
        trf.setCaminho(escalonarRota(rec));
        mestre.enviarTarefa(trf);
        }
        else{
        System.out.println("Não existem VMs alocadas ainda, devolvendo tarefa " + trf.getIdentificador());    
        adicionarTarefa(trf);
        mestre.liberarEscalonador();
        }
        System.out.println("---------------------------");
    }

    @Override
    public List<CentroServico> escalonarRota(CentroServico destino) {
        CS_VirtualMac auxVM = (CS_VirtualMac) destino;
        int index = escravos.indexOf(auxVM);
        
        System.out.println("traçando rota para a VM: "+ auxVM.getId());
        return new ArrayList<CentroServico>((List<CentroServico>) caminhoEscravo.get(index));
        
    }
}
