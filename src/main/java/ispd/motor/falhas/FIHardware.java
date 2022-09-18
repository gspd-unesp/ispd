package ispd.motor.falhas;

import ispd.gui.PickSimulationFaultsDialog;
import ispd.motor.ProgressoSimulacao;
import ispd.motor.filas.RedeDeFilasCloud;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;

import javax.swing.JOptionPane;
import java.util.List;
import java.util.Random;

public class FIHardware {
    /**
     * @param janela
     * @param redeDeFilas
     * @param tarefas
     */
    public void FIHardware1(
            final ProgressoSimulacao janela,
            final RedeDeFilasCloud redeDeFilas,
            final List<Tarefa> tarefas) {
        FIHardware.selectFaults(janela);

        final var vms = redeDeFilas.getVMs();

        System.out.println("---------------------------------------");

        if (vms == null) {
            System.out.println("Rede de filas é nula na classe " +
                               "SimulacaoSequencialCloud.java");
            return;
        }

        final var machines = redeDeFilas.getMaquinasCloud();
        final int id = new Random().nextInt(machines.size());

        System.out.printf("""
                        Rede de filas das VMs não é nula na classe SimulacaoSequencialCloud.java
                        Listagem da rede de filas:
                        Rede de Filas: %s
                        Rede de Filas get VMs: %s
                        Há máquinas alocadas no redeDeFilas
                        Rede de Filas Cloud get PMs: %s
                        Rede de Filas Cloud: getMaquinasCloud: %s
                        Quantidade de Máquinas alocadas ao mestre: %d
                        Número da posição da maquina sorteada: %d
                        Máquina sorteada desligada: %d
                        Máquina que o status é igual a 2: %d
                        """,
                redeDeFilas, vms, vms, machines, machines.size(),
                id, id, CS_MaquinaCloud.DESLIGADO);

        FIHardware.printNewQueueNetwork(vms, machines);
    }

    private static void selectFaults(final ProgressoSimulacao janela) {
        new PickSimulationFaultsDialog();
        JOptionPane.showMessageDialog(null, "Hardware Failure selected.");
        janela.println("Hardware failure created.");
        janela.print(" -> ");
    }

    private static void printNewQueueNetwork(
            final List<CS_VirtualMac> vms,
            final List<CS_MaquinaCloud> machines) {
        final int qn = machines.size() - 1;
        System.out.printf("Novo redeDeFilas: %d\n", qn);
        for (int i = 0; i <= qn; i++) {
            System.out.printf("""
                    Novo Rede de Filas: %d
                    Novo Rede de Filas Cloud get VMs: %s
                    Novo Rede de Filas Cloud: getMaquinasCloud: %s
                    """, qn, vms, machines);
        }
    }
}