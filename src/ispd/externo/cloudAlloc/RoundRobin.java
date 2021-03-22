/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.externo.cloudAlloc;

import ispd.alocacaoVM.Alocacao;
import ispd.motor.filas.TarefaVM;
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
 *
 * @author Diogo Tavares
 */
public class RoundRobin extends Alocacao {

    private ListIterator<CS_Processamento> maqFisica;
   

    public RoundRobin() {
        
        this.maquinasVirtuais = new ArrayList<CS_VirtualMac>();
        this.maquinasFisicas = new LinkedList<CS_Processamento>();
        this.VMsRejeitadas = new ArrayList<CS_VirtualMac>();
    }

    @Override
    public void iniciar() {
         System.out.println("---------------------------------------");
        System.out.println("Alocador RR iniciado");

        //trecho de teste.. excluir depois
        if (maquinasVirtuais.isEmpty()) {
            System.out.println("sem vms setadas");
        }
        System.out.println("Lista de VMs");
        for (CS_VirtualMac aux : maquinasVirtuais) {
            System.out.println(aux.getId());
        }

        //fim do trecho de teste
        maqFisica = maquinasFisicas.listIterator(0);
        if (!maquinasVirtuais.isEmpty()) {

            escalonar();
        }
    }

    @Override
    public CS_VirtualMac escalonarVM() {
        return maquinasVirtuais.remove(0);
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        if (maqFisica.hasNext()) {
            return maqFisica.next();
        } else {
            maqFisica = maquinasFisicas.listIterator(0);
            return maqFisica.next();
        }
    }

    @Override
    public List<CentroServico> escalonarRota(CentroServico destino) {
        //System.out.println("Escalonando rota da vm para hospedeiro");
        int index = maquinasFisicas.indexOf(destino);
        //System.out.println("indice da maquina é:" + index);
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
                        VMM.enviarVM(auxVM);
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
                            VMM.enviarVM(auxVM);
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

    @Override
    public void migrarVM() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
