package ispd.arquivo.xml.models.builders;

import ispd.arquivo.xml.utils.SwitchConnection;
import ispd.arquivo.xml.utils.UserPowerLimit;
import ispd.arquivo.xml.utils.WrappedDocument;
import ispd.arquivo.xml.utils.WrappedElement;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.RedeDeFilasCloud;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_Switch;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to build a cloud queue network from a model in a
 * {@link WrappedDocument}. Construct an instance and call the method
 * {@link #build()}
 *
 * @see ispd.arquivo.xml.IconicoXML
 * @see QueueNetworkBuilder
 */
public class CloudQueueNetworkBuilder extends QueueNetworkBuilder {
    private final Map<CentroServico, List<CS_MaquinaCloud>> clusterSlaves =
            new HashMap<>();
    private final List<CS_MaquinaCloud> cloudMachines = new ArrayList<>();
    private final List<CS_VirtualMac> virtualMachines = new ArrayList<>();
    private final List<CS_Processamento> virtualMachineMasters =
            new ArrayList<>();

    public CloudQueueNetworkBuilder(final WrappedDocument doc) {
        super(doc);
        doc.virtualMachines().forEach(this::processVirtualMachineElement);
    }

    private void processVirtualMachineElement(final WrappedElement e) {
        final var virtualMachine =
                ServiceCenterBuilder.aVirtualMachine(e);

        final var masterId = e.vmm();

        this.virtualMachineMasters.stream()
                .filter(cs -> cs.getId().equals(masterId))
                .map(CS_VMM.class::cast)
                .forEach(master -> {
                    virtualMachine.addVMM(master);
                    master.addVM(virtualMachine);
                });

        this.virtualMachines.add(virtualMachine);
    }

    @Override
    protected void processClusterElement(final WrappedElement e) {
        if (e.isMaster()) {
            final var clust = ServiceCenterBuilder.aVmmNoLoad(e);

            this.virtualMachineMasters.add(clust);
            this.serviceCenters.put(e.globalIconId(), clust);

            final int slaveCount = e.nodes();

            final double power =
                    clust.getPoderComputacional() * (slaveCount + 1);

            this.increaseUserPower(clust.getProprietario(), power);

            final var theSwitch = ServiceCenterBuilder.aSwitch(e);

            this.links.add(theSwitch);

            SwitchConnection.toVirtualMachineMaster(theSwitch, clust);

            for (int j = 0; j < slaveCount; j++) {
                final var machine =
                        ServiceCenterBuilder.aCloudMachineWithId(e, j);
                SwitchConnection.toCloudMachine(theSwitch, machine);

                machine.addMestre(clust);
                clust.addEscravo(machine);

                this.cloudMachines.add(machine);
            }
        } else {
            final var theSwitch = ServiceCenterBuilder.aSwitch(e);

            this.links.add(theSwitch);
            this.serviceCenters.put(e.globalIconId(), theSwitch);

            this.increaseUserPower(e.owner(), e.power() * e.nodes());

            final int slaveCount = e.nodes();

            final List<CS_MaquinaCloud> slaves = new ArrayList<>(slaveCount);

            for (int j = 0; j < slaveCount; j++) {
                final var machine =
                        ServiceCenterBuilder.aCloudMachineWithId(e, j);
                SwitchConnection.toCloudMachine(theSwitch, machine);
                slaves.add(machine);
            }

            this.cloudMachines.addAll(slaves);
            this.clusterSlaves.put(theSwitch, slaves);
        }
    }

    @Override
    protected CS_Processamento makeAndAddMachine(final WrappedElement e) {
        final CS_Processamento machine;

        if (e.hasMasterAttribute()) {
            machine = ServiceCenterBuilder.aVirtualMachineMaster(e);
            this.virtualMachineMasters.add(machine);
        } else {
            machine = ServiceCenterBuilder.aCloudMachine(e);
            this.cloudMachines.add((CS_MaquinaCloud) machine);
        }

        return machine;
    }

    @Override
    protected void addServiceCenterSlaves(
            final CentroServico serviceCenter, final CS_Processamento m) {
        final var master = (CS_VMM) m;
        if (serviceCenter instanceof CS_Processamento proc) {
            master.addEscravo(proc);
            if (serviceCenter instanceof CS_MaquinaCloud machine) {
                machine.addMestre(master);
            }
        } else if (serviceCenter instanceof CS_Switch) {
            for (final var slave : this.clusterSlaves.get(serviceCenter)) {
                slave.addMestre(master);
                master.addEscravo(slave);
            }
        }
    }

    @Override
    protected void setSchedulersUserMetrics(final UserPowerLimit helper) {
        this.virtualMachineMasters.stream()
                .map(CS_VMM.class::cast)
                .map(CS_VMM::getEscalonador)
                .forEach(helper::setSchedulerUserMetrics);
    }

    @Override
    protected RedeDeFilas initQueueNetwork() {
        return new RedeDeFilasCloud(
                this.virtualMachineMasters,
                this.cloudMachines, this.virtualMachines,
                this.links, this.internets
        );
    }
}
