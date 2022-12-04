package ispd.policy.allocation.vm.impl;

import ispd.annotations.Policy;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.policy.allocation.vm.VmAllocationPolicy;
import ispd.policy.allocation.vm.impl.util.util.ComparaRequisitos;
import ispd.policy.allocation.vm.impl.util.util.ComparaVolume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Policy
public class Volume extends VmAllocationPolicy {
    private final Comparator<CS_VirtualMac> comparaReq =
            new ComparaRequisitos();
    private final ComparaVolume comparaRec = new ComparaVolume();
    private boolean fit = false;
    private int maqIndex = 0;
    private List<CS_VirtualMac> VMsOrdenadas = null;
    private List<CS_Processamento> MaqsOrdenadas = null;


    public Volume() {
        this.maquinasVirtuais = new ArrayList<>();
        this.escravos = new ArrayList<>();
        this.VMsRejeitadas = new ArrayList<>();
    }

    @Override
    public void iniciar() {
        this.fit = true;
        this.maqIndex = 0;
        this.VMsOrdenadas = new ArrayList<>(this.maquinasVirtuais);
        this.VMsOrdenadas.sort(this.comparaReq); //ordena vms
        Collections.reverse(this.VMsOrdenadas);//deixa a ordenação decrescente
        this.MaqsOrdenadas = new ArrayList<>(this.escravos);
        this.MaqsOrdenadas.sort((Comparator) this.comparaRec);
        Collections.reverse(this.MaqsOrdenadas);

        if (!this.escravos.isEmpty() && !this.maquinasVirtuais.isEmpty()) {
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
            return this.escravos.get(0);
        } else {
            return this.escravos.get(this.maqIndex);
        }
    }

    @Override
    public List<CentroServico> escalonarRota(final CentroServico destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<CentroServico>) this.caminhoEscravo.get(index));
    }

    @Override
    public void escalonar() {
        while (!(this.maquinasVirtuais.isEmpty())) {
            System.out.println("------------------------------------------");
            int num_escravos = this.escravos.size();

            final CS_VirtualMac auxVM = this.escalonarVM();

            while (num_escravos >= 0) {
                if (num_escravos > 0) {//caso existam máquinas livres
                    final CS_Processamento auxMaq = this.escalonarRecurso();
                    //escalona
                    // o recurso
                    if (auxMaq instanceof CS_VMM) {

                        System.out.println(auxMaq.getId() + " é um VMM, a VM " +
                                           "será redirecionada");
                        auxVM.setCaminho(this.escalonarRota(auxMaq));
                        //salvando uma lista de VMMs intermediarios no 
                        // caminho da vm e seus respectivos caminhos
                        System.out.println(auxVM.getId() + " enviada para " + auxMaq.getId());
                        this.mestre.sendVm(auxVM);
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
                            this.mestre.sendVm(auxVM);
                            System.out.println(
                                    "---------------------------------------");
                            this.atualizarVolume();
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

    private void atualizarVolume() {
        this.MaqsOrdenadas.sort((Comparator) this.comparaRec);
        Collections.reverse(this.infoMaquinas);
    }
}

