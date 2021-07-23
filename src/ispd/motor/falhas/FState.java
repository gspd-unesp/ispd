/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.motor.falhas;

import ispd.gui.JSelecionarFalhas;
import ispd.motor.*;
import javax.swing.*;
import javax.swing.JOptionPane.*;
import ispd.alocacaoVM.VMM;
import ispd.escalonador.Mestre;
import ispd.escalonadorCloud.MestreCloud;
import ispd.motor.filas.Cliente;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.RedeDeFilasCloud;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.motor.filas.RedeDeFilasCloud;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import ispd.gui.JSelecionarFalhas;
import javax.swing.JOptionPane;
import ispd.alocacaoVM.Alocacao;
import ispd.escalonadorCloud.EscalonadorCloud;
import ispd.motor.filas.servidores.CS_Comunicacao;
import static ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud.DESLIGADO;
import ispd.motor.metricas.MetricasAlocacao;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author Fabio Amorim da Silva
 * @param redeDeFilas
 * @param janela
 * 
 */
public class FState{
    
    private ArrayList<CS_VirtualMac> VMs;
    private LinkedList<CS_Processamento> PMs;
    
    public void FIState1(ProgressoSimulacao janela, RedeDeFilasCloud redeDeFilas){

    //Mensagens com a inserção de falha
     JOptionPane.showMessageDialog(null, "State transmission failure detected.");
     janela.println("State transmission fault created.");
     janela.println("->");
     
     //Criação de filas vazias para armazenamento das máquinas antes da falha
        
     //Processo de falha e tratamento
     if(redeDeFilas.getVMs() == null){
            System.out.println("---------------------------------------");
            System.out.println("Rede de filas é nula.");
        
     }// if(redeDeFilas.getVMs() == null)
     else if(redeDeFilas.getVMs() != null){
        
     //Criação de números aleatórios para impedir o estado de transmissão
     Random cloudMachines    = new Random(); //Máquinas da nuvem
     double machineDown = cloudMachines.nextInt(redeDeFilas.getMaquinasCloud().size());
     
     
     //Máquina desligada para impedir que a transmissão seja feita   
     machineDown = ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud.DESLIGADO;
     
     //Nova máquina para ser replicada
     double newMachine = machineDown;
     
     //Número de VMs na simulação
     double machineNumber = redeDeFilas.getMaquinasCloud().size();
     
     //Método de recuperação - Replicação
        for(int i = 0; i < machineNumber; i++){

           if(machineDown == 2){

               machineDown = newMachine;
               newMachine = ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud.DESLIGADO;

           }else {
           
              janela.println("Nenhuma máquina com falha encontrada!");
           
           }


        }
     
     
      } //else if(redeDeFilas.getVMs() != null)
       
    }// Fim falha de estado de transmissão
    
}
