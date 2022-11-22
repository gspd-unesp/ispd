package ispd.policy.allocation.vm.impl;

import ispd.annotations.Policy;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.policy.allocation.vm.VmAllocationPolicy;

import java.util.ArrayList;
import java.util.List;

@Policy
public class FirstFit extends VmAllocationPolicy {
    private boolean fit;
    private int maqIndex;

    public FirstFit() {
        this.maquinasVirtuais = new ArrayList<>();
        this.escravos = new ArrayList<>();
        this.VMsRejeitadas = new ArrayList<>();
    }

    @Override
    public void iniciar() {
        this.fit = true;
        this.maqIndex = 0;

        if (!this.escravos.isEmpty() && !this.maquinasVirtuais.isEmpty()) {
            this.escalonar();
        }
    }

    @Override
    public CS_VirtualMac escalonarVM() {
        return this.maquinasVirtuais.remove(0);
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        return this.escravos.get(this.fit ? 0 : this.maqIndex);
    }

    @Override
    public List<CentroServico> escalonarRota(final CentroServico destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<CentroServico>) this.caminhoEscravo.get(index));
    }

    @Override
    public void escalonar() {
        while (!(this.maquinasVirtuais.isEmpty())) {
            var slaveCount = this.escravos.size();
            final var auxVM = this.escalonarVM();

            do {
                if (slaveCount == 0) {
                    auxVM.setStatus(CS_VirtualMac.REJEITADA);
                    this.VMsRejeitadas.add(auxVM);
                    this.maqIndex = 0;
                    slaveCount--;
                    continue;
                }

                final var auxMaq = this.escalonarRecurso();
                this.maqIndex++;

                if (auxMaq instanceof CS_VMM) {
                    System.out.printf(
                            "%s é um VMM, a VM será redirecionada\n",
                            auxMaq.getId());
                    auxVM.setCaminho(this.escalonarRota(auxMaq));
                    System.out.printf("%s enviada para %s\n",
                            auxVM.getId(), auxMaq.getId());
                    this.mestre.sendVm(auxVM);
                    System.out.println(
                            "---------------------------------------");
                    break;
                } else {
                    final var maq = (CS_MaquinaCloud) auxMaq;
                    if (FirstFit.canMachineFitVm(maq, auxVM)) {
                        FirstFit.makeMachineHostVm(maq, auxVM);
                        auxVM.setCaminho(this.escalonarRota(auxMaq));

                        this.mestre.sendVm(auxVM);
                        this.maqIndex = 0;
                        this.fit = true;

                        break;
                    } else {
                        slaveCount--;
                        this.fit = false;
                    }
                }
            } while (slaveCount >= 0);
        }
    }

    private static boolean canMachineFitVm(
            final CS_MaquinaCloud machine, final CS_VirtualMac vm) {
        return vm.getMemoriaDisponivel() <= machine.getMemoriaDisponivel()
               && vm.getDiscoDisponivel() <= machine.getDiscoDisponivel()
               && vm.getProcessadoresDisponiveis() <= machine.getProcessadoresDisponiveis();
    }

    private static void makeMachineHostVm(
            final CS_MaquinaCloud machine, final CS_VirtualMac vm) {
        machine.setMemoriaDisponivel(machine.getMemoriaDisponivel() - vm.getMemoriaDisponivel());
        machine.setDiscoDisponivel(machine.getDiscoDisponivel() - vm.getDiscoDisponivel());
        machine.setProcessadoresDisponiveis(machine.getProcessadoresDisponiveis() - vm.getProcessadoresDisponiveis());
        vm.setMaquinaHospedeira(machine);
    }

}
