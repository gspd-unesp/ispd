/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.externo.cloudAlloc;

import ispd.alocacaoVM.Alocacao;
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
public class FirstFit extends Alocacao {

    private boolean fit;
    private int maqIndex;

    public FirstFit() {
        this.maquinasVirtuais = new ArrayList<CS_VirtualMac>();
        this.maquinasFisicas = new ArrayList<CS_Processamento>();
        this.VMsRejeitadas = new ArrayList<CS_VirtualMac>();

    }

    @Override
    public void iniciar() {
        fit = true;
        maqIndex = 0;

        if (!maquinasFisicas.isEmpty() && !maquinasVirtuais.isEmpty()) {
            escalonar();
        }
    }

    @Override
    public CS_VirtualMac escalonarVM() {
        return maquinasVirtuais.remove(0);
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
            int num_escravos;
            num_escravos = maquinasFisicas.size();

            CS_VirtualMac auxVM = escalonarVM();

            while (num_escravos >= 0) {
                if (num_escravos > 0) { //caso existam máquinas livres
                    CS_Processamento auxMaq = escalonarRecurso(); //escalona o recurso
                    maqIndex++;

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
                        System.out.println(auxVM.getId() + " enviada para " + auxMaq.getId());
                        VMM.enviarVM(auxVM);
                        System.out.println("---------------------------------------");
                        break;
                    } else {
                        CS_MaquinaCloud maq = (CS_MaquinaCloud) auxMaq;
                        double memoriaMaq = maq.getMemoriaDisponivel();
                        double memoriaNecessaria = auxVM.getMemoriaDisponivel();
                        double discoMaq = maq.getDiscoDisponivel();
                        double discoNecessario = auxVM.getDiscoDisponivel();
                        int maqProc = maq.getProcessadoresDisponiveis();
                        int procVM = auxVM.getProcessadoresDisponiveis();

                        if ((memoriaNecessaria <= memoriaMaq && discoNecessario <= discoMaq && procVM <= maqProc)) {
                            maq.setMemoriaDisponivel(memoriaMaq - memoriaNecessaria);
                            maq.setDiscoDisponivel(discoMaq - discoNecessario);
                            maq.setProcessadoresDisponiveis(maqProc - procVM);
                            auxVM.setMaquinaHospedeira((CS_MaquinaCloud) auxMaq);
                            auxVM.setCaminho(escalonarRota(auxMaq));
                            VMM.enviarVM(auxVM);
                            maqIndex = 0;
                            fit = true;
                            break;
                        } else {
                            num_escravos--;
                            fit = false;
                        }
                    }
                } else {
                    auxVM.setStatus(CS_VirtualMac.REJEITADA);
                    VMsRejeitadas.add(auxVM);
                    maqIndex = 0;
                    num_escravos--;
                }

            }
        }

    }

    @Override
    public void migrarVM() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
