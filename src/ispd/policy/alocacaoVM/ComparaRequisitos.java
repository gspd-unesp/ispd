/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.policy.alocacaoVM;

import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;


/**
 *
 * @author Diogo Tavares
 */
public class ComparaRequisitos implements java.util.Comparator {
    

    @Override
    public int compare(Object t1, Object t2) {
        CS_VirtualMac aux1 = (CS_VirtualMac) t1;
        CS_VirtualMac aux2 = (CS_VirtualMac) t2;
        int valor1 = aux1.getProcessadoresDisponiveis()*100000 + (int) aux1.getMemoriaDisponivel()*100000 + (int) aux1.getDiscoDisponivel()*100000;
        int valor2 = aux2.getProcessadoresDisponiveis()*100000 + (int) aux2.getMemoriaDisponivel()*100000 + (int) aux2.getDiscoDisponivel()*100000;
        return (valor1 - valor2);
    }
    
}
