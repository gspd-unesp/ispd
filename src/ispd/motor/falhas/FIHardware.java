/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.falhas;

import ispd.gui.JSelecionarFalhas;
import ispd.motor.*;
import ispd.motor.filas.RedeDeFilasCloud;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import javax.swing.JOptionPane;

import ispd.escalonadorCloud.EscalonadorCloud;

import java.util.LinkedList;
import java.util.Random;

/**
 * @author Camila
 */
public class FIHardware {
   /* public int OH=1;//mesmo tipo do JSelecionarFalhas
    public int OH1;
    public int OHHH;*/
    private double time = 0;
    private EscalonadorCloud escalonador;//Camila
    private PriorityQueue<FutureEvent> eventos;
    private ArrayList<CS_VirtualMac> maquinasVirtuais;
    private LinkedList<CS_Processamento> maquinasFisicas;
    private ArrayList<CS_VirtualMac> VMsRejeitadas;
    private List<List> caminhoVMs; //Camila criou para referenciar SimulacaoSequencialCloud

    /**
     *
     * @param janela
     * @param redeDeFilas
     * @param tarefas
     */
    public void FIHardware1(ProgressoSimulacao janela, RedeDeFilasCloud redeDeFilas, List<Tarefa> tarefas) {
        //declaração das variáveis locais
        int X;
        //Criação de um instância para a classe JSelecionarFalhas
        JSelecionarFalhas sf = new JSelecionarFalhas();
        X = sf.OmissaoHardware;
        //Confirmação do tipo de falha selecionada: Falha por omissão de hardware
        JOptionPane.showMessageDialog(null, "Hardware Failure selected.");
        //RedeDeFilasCloud redeDeFilas = new RedeDeFilasCloud();
        
        //JOptionPane.showMessageDialog (null, "Hardware failure selected.");
            janela.println("Hardware failure created.");
            janela.print(" -> ");
            this.maquinasVirtuais = new ArrayList<CS_VirtualMac>();//está vazia
            this.maquinasFisicas = new LinkedList<CS_Processamento>(); //está vazia
            this.VMsRejeitadas = new ArrayList<CS_VirtualMac>();//está vazia
            
            //by Camila
            if (redeDeFilas.getVMs()==null){
                System.out.println("---------------------------------------");
                System.out.println("Rede de filas é nula na classe SimulacaoSequencialCloud.java");
            }//if (redeDeFilas.getVMs()==null)
            //Se a redeDeFilas for diferente de nulo, então
            else if(redeDeFilas.getVMs()!=null){
                System.out.println("---------------------------------------");
                System.out.println("Rede de filas das VMs não é nula na classe SimulacaoSequencialCloud.java");
                System.out.println("Listagem da rede de filas: ");
                System.out.println("Rede de Filas: " +redeDeFilas);
                System.out.println("Rede de Filas get VMs: " +redeDeFilas.getVMs());
                System.out.println("Há máquinas alocadas no redeDeFilas");
                System.out.println("Rede de Filas Cloud get PMs: " +redeDeFilas.getVMs());
                System.out.println("Rede de Filas Cloud: getMaquinasCloud: "+redeDeFilas.getMaquinasCloud());
                System.out.println("Quantidade de Máquinas alocadas ao mestre: "+redeDeFilas.getMaquinasCloud().size());
                //sorteio de máquina alocada ao mestre  Falha de omissão de hardware:
                Random random = new Random();
                int draw = random.nextInt(redeDeFilas.getMaquinasCloud().size());
                System.out.println("Número da posição da maquina sorteada: "+draw); //Exemplo: [27]
                System.out.println("Máquina sorteada desligada: "+draw);
                //tornar a posição sorteada ==Desligada
                draw = ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud.DESLIGADO;
                System.out.println("Máquina que o status é igual a 2: "+draw);
                int NovoRedeDeFilas = redeDeFilas.getMaquinasCloud().size()-1;
                System.out.println("Novo redeDeFilas: "+NovoRedeDeFilas);
                //Na codifição está getVMs na verdade ele retorna as PM (Physical machines)
                //escreva o vetor redeDeFilas com a posição [draw} com status == DESLIGADO
                for(int i=0; i<=NovoRedeDeFilas; i++){
                    System.out.println("Novo Rede de Filas: "+NovoRedeDeFilas);
                    System.out.println("Novo Rede de Filas Cloud get VMs: " +redeDeFilas.getVMs());
                    System.out.println("Novo Rede de Filas Cloud: getMaquinasCloud: "+redeDeFilas.getMaquinasCloud());
                }
                //Refazer o escalonamento: Técnica: Regate do Workflow??? ou Redistribuição das tarefas??
            }// else if(redeDeFilas.getVMs()!=null)
    }

    }//public class FIHardware