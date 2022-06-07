/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.alocacaoVM;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;


/**
 *
 * @author Diogo Tavares
 */
public class ComparaVolume implements java.util.Comparator {
    

    @Override
    public int compare(Object t1, Object t2) {
        CS_MaquinaCloud aux1 = (CS_MaquinaCloud) t1;
        CS_MaquinaCloud aux2 = (CS_MaquinaCloud) t2;
        int valor1 = aux1.getProcessadoresDisponiveis()*100 * (int) aux1.getMemoriaDisponivel()*100 * (int) aux1.getDiscoDisponivel()*100;
        int valor2 = aux2.getProcessadoresDisponiveis()*100 * (int) aux2.getMemoriaDisponivel()*100 * (int) aux2.getDiscoDisponivel()*100;
        return (valor1 - valor2);
    }
    
}
