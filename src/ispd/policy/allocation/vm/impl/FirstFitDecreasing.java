/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.policy.allocation.vm.impl;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.policy.allocation.vm.Alocacao;
import ispd.policy.allocation.vm.util.ComparaRequisitos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Diogo Tavares
 */
public class FirstFitDecreasing extends Alocacao {

    private boolean fit;
    private int maqIndex;
    private List<CS_VirtualMac>VMsOrdenadas;
    private List<CS_Processamento>MaqsOrdenadas;
    private ComparaRequisitos comparaReq;
    

    public FirstFitDecreasing() {
        this.maquinasVirtuais = new ArrayList<CS_VirtualMac>();
        this.maquinasFisicas = new ArrayList<CS_Processamento>();
        this.VMsRejeitadas = new ArrayList<CS_VirtualMac>();
        this.comparaReq = new ComparaRequisitos();
        //this.VMsOrdenadas = new ArrayList<CS_VirtualMac>();
        //this.MaqsOrdenadas = new ArrayList<CS_Processamento>();
        
    }

    @Override
    public void iniciar() {
        fit = true;
        maqIndex = 0;
        VMsOrdenadas = new ArrayList<CS_VirtualMac>(maquinasVirtuais);
        for(CS_VirtualMac aux : VMsOrdenadas){
            System.out.println(aux.getId());
        }
        Collections.sort(VMsOrdenadas, comparaReq);
        System.out.println("Ordem crescente");
        for(CS_VirtualMac aux : VMsOrdenadas){
            System.out.println(aux.getId());
        }
        Collections.reverse(VMsOrdenadas);
        System.out.println("Ordem decrescente");
        for(CS_VirtualMac aux : VMsOrdenadas){
            System.out.println(aux.getId());
        }
        if(!maquinasFisicas.isEmpty() && !maquinasVirtuais.isEmpty()){
            escalonar();
        }
    }

    @Override
    public CS_VirtualMac escalonarVM() {
        return VMsOrdenadas.remove(0);
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        if (fit) {
            return maquinasFisicas.get(0);
        } else {
            return maquinasFisicas.get(maqIndex);
        }
    }

    @Override
    public List<CentroServico> escalonarRota(CentroServico destino) {
        int index = maquinasFisicas.indexOf(destino);
        return new ArrayList<CentroServico>((List<CentroServico>) caminhoMaquina.get(index));
    }

    @Override
    public void escalonar() {

         

        while (!(maquinasVirtuais.isEmpty())) {
            System.out.println("------------------------------------------");
            int num_escravos;
            num_escravos = maquinasFisicas.size();
            

            CS_VirtualMac auxVM = escalonarVM();

            while (num_escravos >= 0) {
                if (num_escravos > 0) {//caso existam máquinas livres
                    CS_Processamento auxMaq = escalonarRecurso(); //escalona o recurso
                    if (auxMaq instanceof CS_VMM) {
                        
                        System.out.println(auxMaq.getId() + " é um VMM, a VM será redirecionada");
                        auxVM.setCaminho(escalonarRota(auxMaq));
                        //salvando uma lista de VMMs intermediarios no caminho da vm e seus respectivos caminhos
                        //CS_VMM maq = (CS_VMM) auxMaq;
                        //auxVM.addIntermediario(maq);
                        //List<CS_VMM> inter = auxVM.getVMMsIntermediarios();
                        //int index = inter.indexOf((CS_VMM) auxMaq);
                        //ArrayList<CentroServico> caminhoInter = new ArrayList<CentroServico>(escalonarRota(auxMaq));
                        //auxVM.addCaminhoIntermediario(index, caminhoInter);
                        System.out.println( auxVM.getId() + " enviada para " + auxMaq.getId());
                        vmMaster.sendVm(auxVM);
                        System.out.println("---------------------------------------");
                        break;
                    } else {
                        System.out.println("Checagem de recursos:");
                        CS_MaquinaCloud maq = (CS_MaquinaCloud) auxMaq;
                        double memoriaMaq = maq.getMemoriaDisponivel();
                        System.out.println("memoriaMaq: " + memoriaMaq);
                        double memoriaNecessaria = auxVM.getMemoriaDisponivel();
                        System.out.println("memorianecessaria: " + memoriaNecessaria);
                        double discoMaq = maq.getDiscoDisponivel();
                        System.out.println("discoMaq: " + discoMaq);
                        double discoNecessario = auxVM.getDiscoDisponivel();
                        System.out.println("disconecessario: " + discoNecessario);
                        int maqProc = maq.getProcessadoresDisponiveis();
                        System.out.println("ProcMaq: " + maqProc);
                        int procVM = auxVM.getProcessadoresDisponiveis();
                        System.out.println("ProcVM: " + procVM);
                        //System.out.println("---------------------------------------");

                        if ((memoriaNecessaria <= memoriaMaq && discoNecessario <= discoMaq && procVM <= maqProc)) {
                            maq.setMemoriaDisponivel(memoriaMaq - memoriaNecessaria);
                             System.out.println("Realizando o controle de recurso:");
                            System.out.println("memoria atual da maq: " + (memoriaMaq - memoriaNecessaria));
                            maq.setDiscoDisponivel(discoMaq - discoNecessario);
                            System.out.println("disco atual maq: " + (discoMaq - discoNecessario));
                            maq.setProcessadoresDisponiveis(maqProc - procVM);
                            System.out.println("proc atual: " + (maqProc - procVM));
                            auxVM.setMaquinaHospedeira((CS_MaquinaCloud) auxMaq);
                            auxVM.setCaminho(escalonarRota(auxMaq));
                            System.out.println( auxVM.getId() + " enviada para " + auxMaq.getId());
                            vmMaster.sendVm(auxVM);
                             System.out.println("---------------------------------------");

                            break;

                        } else {
                            num_escravos--;
                        }
                    }
                } else {
                    System.out.println(auxVM.getId() + " foi rejeitada");
                    auxVM.setStatus(CS_VirtualMac.REJEITADA);
                    VMsRejeitadas.add(auxVM);
                    System.out.println("Adicionada na lista de rejeitadas");
                    num_escravos--;
                     System.out.println("---------------------------------------");
                }
            }
        }

    }

}

