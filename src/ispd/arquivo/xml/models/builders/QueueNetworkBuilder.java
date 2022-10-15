package ispd.arquivo.xml.models.builders;

import ispd.arquivo.xml.utils.SwitchConnection;
import ispd.arquivo.xml.utils.UserPowerLimit;
import ispd.arquivo.xml.utils.WrappedDocument;
import ispd.arquivo.xml.utils.WrappedElement;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Internet;
import ispd.motor.filas.servidores.implementacao.CS_Link;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.filas.servidores.implementacao.CS_Switch;
import ispd.motor.filas.servidores.implementacao.Vertice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class to build a queue network from a model in a {@link WrappedDocument}.
 * Construct an instance and call the method {@link #build()}
 *
 * @see ispd.arquivo.xml.IconicoXML
 */
public class QueueNetworkBuilder {
    protected final Map<Integer, CentroServico> serviceCenters =
            new HashMap<>();
    protected final List<CS_Comunicacao> links = new ArrayList<>();
    protected final List<CS_Internet> internets = new ArrayList<>();
    private final Map<CentroServico, List<CS_Maquina>> clusterSlaves =
            new HashMap<>(0);
    private final List<CS_Processamento> masters = new ArrayList<>();
    private final List<CS_Maquina> machines = new ArrayList<>();
    private final Map<String, Double> powerLimits;

    public QueueNetworkBuilder(final WrappedDocument doc) {
        this.powerLimits = doc.owners().collect(Collectors.toMap(
                WrappedElement::id, o -> 0.0,
                (prev, next) -> next, HashMap::new
        ));

        doc.machines().forEach(this::processMachineElement);
        doc.clusters().forEach(this::processClusterElement);
        doc.internets().forEach(this::processInternetElement);
        doc.links().forEach(this::processLinkElement);
        doc.masters().forEach(this::addSlavesToMachine);
    }

    private void processMachineElement(final WrappedElement e) {
        final var machine = this.makeAndAddMachine(e);

        this.serviceCenters.put(e.globalIconId(), machine);

        this.increaseUserPower(
                machine.getProprietario(),
                machine.getPoderComputacional()
        );
    }

    protected void processClusterElement(final WrappedElement e) {
        if (e.isMaster()) {
            final var cluster = ServiceCenterBuilder.aMasterWithNoLoad(e);

            this.masters.add(cluster);
            this.serviceCenters.put(e.globalIconId(), cluster);

            final int slaveCount = e.nodes();

            final double power =
                    cluster.getPoderComputacional() * (slaveCount + 1);

            this.increaseUserPower(cluster.getProprietario(), power);

            final var theSwitch = ServiceCenterBuilder.aSwitch(e);

            this.links.add(theSwitch);

            SwitchConnection.toMaster(theSwitch, cluster);

            for (int i = 0; i < slaveCount; i++) {
                final var machine =
                        ServiceCenterBuilder.aMachineWithId(e, i);
                SwitchConnection.toMachine(theSwitch, machine);

                machine.addMestre(cluster);
                cluster.addEscravo(machine);

                this.machines.add(machine);
            }
        } else {
            final var theSwitch = ServiceCenterBuilder.aSwitch(e);

            this.links.add(theSwitch);
            this.serviceCenters.put(e.globalIconId(), theSwitch);

            this.increaseUserPower(e.owner(), e.power() * e.nodes());

            final int slaveCount = e.nodes();

            final var slaves = new ArrayList<CS_Maquina>(slaveCount);

            for (int i = 0; i < slaveCount; i++) {
                final var machine =
                        ServiceCenterBuilder.aMachineWithId(e, i);
                SwitchConnection.toMachine(theSwitch, machine);
                slaves.add(machine);
            }

            this.machines.addAll(slaves);
            this.clusterSlaves.put(theSwitch, slaves);
        }
    }

    private void processInternetElement(final WrappedElement e) {
        final var net = ServiceCenterBuilder.anInternet(e);

        this.internets.add(net);
        this.serviceCenters.put(e.globalIconId(), net);
    }

    private void processLinkElement(final WrappedElement e) {
        final var link = ServiceCenterBuilder.aLink(e);

        QueueNetworkBuilder.connectLinkAndVertices(link,
                this.getVertex(e.origination()),
                this.getVertex(e.destination())
        );

        this.links.add(link);
    }

    private void addSlavesToMachine(final WrappedElement e) {
        final var master =
                (CS_Processamento) this.serviceCenters.get(e.globalIconId());

        e.master().slaves()
                .map(WrappedElement::id)
                .map(Integer::parseInt)
                .map(this.serviceCenters::get)
                .forEach(sc -> this.addServiceCenterSlaves(sc, master));
    }

    protected CS_Processamento makeAndAddMachine(final WrappedElement e) {
        final CS_Processamento machine;

        if (e.hasMasterAttribute()) {
            machine = ServiceCenterBuilder.aMaster(e);
            this.masters.add(machine);
        } else {
            machine = ServiceCenterBuilder.aMachine(e);
            this.machines.add((CS_Maquina) machine);
        }

        return machine;
    }

    protected void increaseUserPower(final String user,
                                     final double increment) {
        final var oldValue = this.powerLimits.get(user);
        this.powerLimits.put(user, oldValue + increment);
    }

    private static void connectLinkAndVertices(
            final CS_Link link,
            final Vertice origination, final Vertice destination) {
        link.setConexoesEntrada((CentroServico) origination);
        link.setConexoesSaida((CentroServico) destination);
        origination.addConexoesSaida(link);
        destination.addConexoesEntrada(link);
    }

    private Vertice getVertex(final int e) {
        return (Vertice) this.serviceCenters.get(e);
    }

    protected void addServiceCenterSlaves(
            final CentroServico serviceCenter, final CS_Processamento m) {
        final var master = (CS_Mestre) m;
        if (serviceCenter instanceof CS_Processamento proc) {
            master.addEscravo(proc);
            if (serviceCenter instanceof CS_Maquina machine) {
                machine.addMestre(master);
            }
        } else if (serviceCenter instanceof CS_Switch) {
            for (final var slave : this.clusterSlaves.get(serviceCenter)) {
                slave.addMestre(master);
                master.addEscravo(slave);
            }
        }
    }

    /**
     * Create a {@link RedeDeFilas} with the collections parsed from the
     * document.
     *
     * @return {@link RedeDeFilas} with the appropriate service centers,
     * links, and user configurations found in the document.
     */
    public RedeDeFilas build() {
        final var helper = new UserPowerLimit(this.powerLimits);
        this.setSchedulersUserMetrics(helper);

        final var queueNetwork = this.initQueueNetwork();
        queueNetwork.setUsuarios(helper.getOwners());
        return queueNetwork;
    }

    protected void setSchedulersUserMetrics(final UserPowerLimit helper) {
        this.masters.stream()
                .map(CS_Mestre.class::cast)
                .map(CS_Mestre::getEscalonador)
                .forEach(helper::setSchedulerUserMetrics);
    }

    protected RedeDeFilas initQueueNetwork() {
        return new RedeDeFilas(
                this.masters, this.machines,
                this.links, this.internets,
                this.powerLimits
        );
    }
}