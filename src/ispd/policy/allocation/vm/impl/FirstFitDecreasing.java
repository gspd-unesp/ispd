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

@SuppressWarnings("unused")
public class FirstFitDecreasing extends Alocacao {
    private final ComparaRequisitos comparaReq;
    private boolean fit = false;
    private int maqIndex = 0;
    private List<CS_VirtualMac> VMsOrdenadas = null;
    private List<CS_Processamento> MaqsOrdenadas = null;

    public FirstFitDecreasing() {
        this.maquinasVirtuais = new ArrayList<>();
        this.maquinasFisicas = new ArrayList<>();
        this.VMsRejeitadas = new ArrayList<>();
        this.comparaReq = new ComparaRequisitos();
    }

    @Override
    public void iniciar() {
        this.fit = true;
        this.maqIndex = 0;
        this.VMsOrdenadas = new ArrayList<>(this.maquinasVirtuais);
        for (final CS_VirtualMac aux : this.VMsOrdenadas) {
            System.out.println(aux.getId());
        }
        this.VMsOrdenadas.sort(this.comparaReq);
        System.out.println("Ordem crescente");
        for (final CS_VirtualMac aux : this.VMsOrdenadas) {
            System.out.println(aux.getId());
        }
        Collections.reverse(this.VMsOrdenadas);
        System.out.println("Ordem decrescente");
        for (final CS_VirtualMac aux : this.VMsOrdenadas) {
            System.out.println(aux.getId());
        }
        if (!this.maquinasFisicas.isEmpty() && !this.maquinasVirtuais.isEmpty()) {
            this.escalonar();
        }
    }

    @Override
    public CS_VirtualMac escalonarVM() {
        return this.VMsOrdenadas.remove(0);
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        if (this.fit) {
            return this.maquinasFisicas.get(0);
        } else {
            return this.maquinasFisicas.get(this.maqIndex);
        }
    }

    @Override
    public List<CentroServico> escalonarRota(final CentroServico destino) {
        final int index = this.maquinasFisicas.indexOf(destino);
        return new ArrayList<>((List<CentroServico>) this.caminhoMaquina.get(index));
    }

    @Override
    public void escalonar() {
        while (!(this.maquinasVirtuais.isEmpty())) {
            System.out.println("------------------------------------------");
            int num_escravos = this.maquinasFisicas.size();

            final CS_VirtualMac auxVM = this.escalonarVM();

            while (num_escravos >= 0) {
                if (num_escravos > 0) {//caso existam máquinas livres
                    final var auxMaq = this.escalonarRecurso();
                    //escalona
                    // o recurso
                    if (auxMaq instanceof CS_VMM) {

                        System.out.println(auxMaq.getId() + " é um VMM, a VM " +
                                           "será redirecionada");
                        auxVM.setCaminho(this.escalonarRota(auxMaq));
                        //salvando uma lista de VMMs intermediarios no
                        // caminho da vm e seus respectivos caminhos
                        System.out.println(auxVM.getId() + " enviada para " + auxMaq.getId());
                        this.vmMaster.sendVm(auxVM);
                        System.out.println(
                                "---------------------------------------");
                        break;
                    } else {
                        System.out.println("Checagem de recursos:");
                        final CS_MaquinaCloud maq = (CS_MaquinaCloud) auxMaq;
                        final double memoriaMaq = maq.getMemoriaDisponivel();
                        System.out.println("memoriaMaq: " + memoriaMaq);
                        final double memoriaNecessaria =
                                auxVM.getMemoriaDisponivel();
                        System.out.println("memorianecessaria: " + memoriaNecessaria);
                        final double discoMaq = maq.getDiscoDisponivel();
                        System.out.println("discoMaq: " + discoMaq);
                        final double discoNecessario =
                                auxVM.getDiscoDisponivel();
                        System.out.println("disconecessario: " + discoNecessario);
                        final int maqProc = maq.getProcessadoresDisponiveis();
                        System.out.println("ProcMaq: " + maqProc);
                        final int procVM = auxVM.getProcessadoresDisponiveis();
                        System.out.println("ProcVM: " + procVM);
                        //System.out.println
                        // ("---------------------------------------");

                        if ((memoriaNecessaria <= memoriaMaq && discoNecessario <= discoMaq && procVM <= maqProc)) {
                            maq.setMemoriaDisponivel(memoriaMaq - memoriaNecessaria);
                            System.out.println("Realizando o controle de " +
                                               "recurso:");
                            System.out.println("memoria atual da maq: " + (memoriaMaq - memoriaNecessaria));
                            maq.setDiscoDisponivel(discoMaq - discoNecessario);
                            System.out.println("disco atual maq: " + (discoMaq - discoNecessario));
                            maq.setProcessadoresDisponiveis(maqProc - procVM);
                            System.out.println("proc atual: " + (maqProc - procVM));
                            auxVM.setMaquinaHospedeira((CS_MaquinaCloud) auxMaq);
                            auxVM.setCaminho(this.escalonarRota(auxMaq));
                            System.out.println(auxVM.getId() + " enviada para" +
                                               " " + auxMaq.getId());
                            this.vmMaster.sendVm(auxVM);
                            System.out.println(
                                    "---------------------------------------");

                            break;

                        } else {
                            num_escravos--;
                        }
                    }
                } else {
                    System.out.println(auxVM.getId() + " foi rejeitada");
                    auxVM.setStatus(CS_VirtualMac.REJEITADA);
                    this.VMsRejeitadas.add(auxVM);
                    System.out.println("Adicionada na lista de rejeitadas");
                    num_escravos--;
                    System.out.println(
                            "---------------------------------------");
                }
            }
        }
    }
}
