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

public class RoundRobin extends Alocacao {
    private ListIterator<CS_Processamento> physicalMachine = null;

    public RoundRobin() {
        this.maquinasVirtuais = new ArrayList<>(0);
        this.maquinasFisicas = new LinkedList<>();
        this.VMsRejeitadas = new ArrayList<>(0);
    }

    @Override
    public void iniciar() {
        System.out.println("---------------------------------------");
        System.out.println("Alocador RR iniciado");

        this.test(); // FIXME: Remove this

        this.physicalMachine = this.maquinasFisicas.listIterator(0);

        if (this.maquinasVirtuais.isEmpty()) {
            return;
        }

        this.escalonar();
    }

    private void test() {
        if (this.maquinasVirtuais.isEmpty()) {
            System.out.println("sem vms setadas");
        }
        System.out.println("Lista de VMs");
        this.maquinasVirtuais.stream()
                .map(CS_Processamento::getId)
                .forEach(System.out::println);
    }

    private void findMachineForTask(final CS_VirtualMac vm) {
        int machines = this.maquinasFisicas.size();
        while (machines >= 0) {
            if (machines == 0) {
                this.rejectVm(vm);
                machines--;
                continue;
            }

            final var resource = this.escalonarRecurso();

            if (resource instanceof CS_VMM) {
                this.redirectVm(vm, resource);
                return;
            } else {

                System.out.println("Checagem de recursos:");
                final var maq = (CS_MaquinaCloud) resource;
                final double memory = maq.getMemoriaDisponivel();
                System.out.println("memoriaMaq: " + memory);
                final double neededMemory = vm.getMemoriaDisponivel();
                System.out.println("memorianecessaria: " + neededMemory);
                final double disk = maq.getDiscoDisponivel();
                System.out.println("discoMaq: " + disk);
                final double neededDisk = vm.getDiscoDisponivel();
                System.out.println("disconecessario: " + neededDisk);
                final int processors = maq.getProcessadoresDisponiveis();
                System.out.println("ProcMaq: " + processors);
                final int procVM = vm.getProcessadoresDisponiveis();
                System.out.println("ProcVM: " + procVM);

                if ((neededMemory > memory || neededDisk > disk || procVM > processors)) {
                    machines--;
                    continue;
                }

                System.out.println("Realizando o controle de recurso:");
                final double newMemory = memory - neededMemory;
                maq.setMemoriaDisponivel(newMemory);
                System.out.printf("memoria atual da maq: %s%n", newMemory);
                final double newDisk = disk - neededDisk;
                maq.setDiscoDisponivel(newDisk);
                System.out.printf("disco atual maq: %s%n", newDisk);
                final int newProcessors = processors - procVM;
                maq.setProcessadoresDisponiveis(newProcessors);
                System.out.printf("proc atual: %d%n", newProcessors);
                vm.setMaquinaHospedeira((CS_MaquinaCloud) resource);
                vm.setCaminho(this.escalonarRota(resource));
                System.out.printf(
                        "%s enviada para %s%n", vm.getId(), resource.getId());
                this.VMM.enviarVM(vm);
                System.out.println("---------------------------------------");

                return;
            }
        }
    }

    private void rejectVm(final CS_VirtualMac auxVm) {
        System.out.printf("%s foi rejeitada%n", auxVm.getId());
        auxVm.setStatus(CS_VirtualMac.REJEITADA);
        this.VMsRejeitadas.add(auxVm);
        System.out.println("Adicionada na lista de rejeitadas");
        System.out.println("---------------------------------------");
    }

    private void redirectVm(
            final CS_VirtualMac vm,
            final CS_Processamento resource) {
        System.out.printf(
                "%s é um VMM, a VM será redirecionada%n", resource.getId());
        vm.setCaminho(this.escalonarRota(resource));
        System.out.printf("%s enviada para %s%n", vm.getId(), resource.getId());
        this.VMM.enviarVM(vm);
        System.out.println("---------------------------------------");
    }

    @Override
    public CS_VirtualMac escalonarVM() {
        return this.maquinasVirtuais.remove(0);
    }

    @Override
    public CS_Processamento escalonarRecurso() {
        if (!this.physicalMachine.hasNext()) {
            this.physicalMachine = this.maquinasFisicas.listIterator(0);
        }
        return this.physicalMachine.next();
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
            this.findMachineForTask(this.escalonarVM());
        }
    }
}