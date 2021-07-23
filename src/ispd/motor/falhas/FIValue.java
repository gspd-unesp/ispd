/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.motor.falhas;

import java.io.Serializable;
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
import ispd.motor.metricas.Metricas;
import ispd.motor.metricas.MetricasGlobais;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;


/**
 *
 * @author Fabio Amorim da Silva
 */
public class FIValue {
    
    private ArrayList<CS_VirtualMac> VMs;
    private LinkedList<CS_Processamento> PMs;
    
    
    /*
    * @param janela
    * @param redeDeFilas
    * @param global
    *
    */
    
    //Método para inserção de falha de resposta
    public void FIValue1(ProgressoSimulacao janela, RedeDeFilasCloud redeDeFilas, MetricasGlobais global){
        
        //Mensagens com a inserção da falha
        JOptionPane.showMessageDialog(null, "Response failure detected.");
        janela.println("Response fault created.");
        janela.println("->");
        
        //Criação de filas vazias para armazenamento das máquinas antes da falha
        this.VMs = new ArrayList<CS_VirtualMac>(); //Lista vazia de máquinas virtuais
        this.PMs = new LinkedList<CS_Processamento>(); //Lista vazia de máquians físicas
        
        //Processo de falha e tratamento
        if(redeDeFilas.getVMs() == null){
            System.out.println("---------------------------------------");
            System.out.println("Rede de filas é nula.");
        
        }// if(redeDeFilas.getVMs() == null)
        else if(redeDeFilas.getVMs() != null){
        
        //Variáveis para recuperação
        double OciosidadeComputacaoOri = global.getOciosidadeComputacao();
        double OciosidadeComunicacaoOri = global.getOciosidadeComunicacao();
        double SatisfacaoMediaOri = global.getSatisfacaoMedia();
        double EficienciaOri = global.getEficiencia();
        
        //Criação de números aleatórios para alterações das respostas para usuários
        Random cloudMachines    = new Random(); //Máquinas da nuvem
        
        double metricsCloud    = cloudMachines.nextInt(redeDeFilas.getMaquinasCloud().size());
        
        global.setOciosidadeComputacao(metricsCloud/100);
        global.setOciosidadeComunicacao(metricsCloud);
        global.setSatisfacaoMedia((metricsCloud/100)*(metricsCloud/100));
        global.setEficiencia(metricsCloud);
        
            //Recuperação via checkpoint
            if(OciosidadeComputacaoOri != global.getOciosidadeComputacao() ||
                    OciosidadeComunicacaoOri != global.getOciosidadeComunicacao() ||
                    SatisfacaoMediaOri != global.getSatisfacaoMedia() ||
                    EficienciaOri != global.getEficiencia()){

                global.setOciosidadeComputacao(OciosidadeComputacaoOri);
                global.setOciosidadeComunicacao(OciosidadeComunicacaoOri);
                global.setSatisfacaoMedia(SatisfacaoMediaOri);
                global.setEficiencia(EficienciaOri);


            }else {

            } //if(Checkpoint para recupreação de valores da simulação)


        } //else if(redeDeFilas.getVMs() != null)
        
                
    }
    
}
