package ispd.arquivo.xml.models.builders;

import ispd.arquivo.xml.utils.SwitchConnection;
import ispd.arquivo.xml.utils.UserPowerLimit;
import ispd.arquivo.xml.utils.WrappedDocument;
import ispd.arquivo.xml.utils.WrappedElement;
import ispd.escalonador.Escalonador;
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

/**
 * Class to build a queue network from a model in a {@link WrappedDocument}.
 * Usage should be as follows: <pre>{@code
 * new QueueNetworkBuilder()
 * .parseDocument(doc)
 * .build();
 * }</pre>
 * See methods {@link #parseDocument(WrappedDocument)} and {@link #build()}
 * for further details.
 *
 * @see ispd.arquivo.xml.IconicoXML
 */
public class QueueNetworkBuilder {
    /**
     * Map or {@link CentroServico}s parsed from the document, indexed by id.
     */
    protected final Map<Integer, CentroServico> serviceCenters =
            new HashMap<>();
    /**
     * Map of {@link CS_Internet}s parsed from the document.
     */
    protected final List<CS_Internet> internets = new ArrayList<>();
    /**
     * Map of {@link CS_Link}s parsed from the document.
     */
    protected final List<CS_Comunicacao> links = new ArrayList<>();
    private final Map<String, Double> powerLimits = new HashMap<>();
    private final List<CS_Maquina> machines = new ArrayList<>();
    private final List<CS_Processamento> masters = new ArrayList<>();
    private final Map<CentroServico, List<CS_Maquina>> clusterSlaves =
            new HashMap<>(0);
    /**
     * Whether this instance has already parsed a document successfully.
     * Each instance should be responsible for parsing <b>only one</b>
     * document.
     */
    private boolean hasParsedADocument = false;

    /**
     * Parse the required {@link CentroServico}s and user power limits from
     * the given {@link WrappedDocument}.
     *
     * @param doc the {@link WrappedDocument} to be processed. Must contain a
     *            valid model.
     * @return the called instance itself, so the call can be chained into a
     * {@link #build()} if so desired.
     * @throws IllegalStateException if this instance was already used to
     *                               parse a {@link WrappedDocument}.
     */
    public QueueNetworkBuilder parseDocument(final WrappedDocument doc) {
        if (this.hasParsedADocument) {
            throw new IllegalStateException(
                    ".parseDocument(doc) method called twice.");
        }

        doc.owners().forEach(o -> this.powerLimits.put(o.id(), 0.0));
        doc.machines().forEach(this::processMachineElement);
        doc.clusters().forEach(this::processClusterElement);
        doc.internets().forEach(this::processInternetElement);
        doc.links().forEach(this::processLinkElement);
        doc.masters().forEach(this::addSlavesToMachine);

        this.hasParsedADocument = true;

        return this;
    }

    private void processMachineElement(final WrappedElement e) {
        final var machine = this.makeAndAddMachine(e);

        this.serviceCenters.put(e.globalIconId(), machine);

        this.increaseUserPower(
                machine.getProprietario(),
                machine.getPoderComputacional()
        );
    }

    /**
     * Process a {@link WrappedElement} that is representing a cluster of
     * {@link CentroServico}s. The {@link CS_Mestre}, {@link CS_Maquina}s and
     * {@link CS_Link}s in the cluster are differentiated and all processed
     * individually.
     *
     * @param e {@link WrappedElement} representing a cluster.
     */
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
                .forEach(sc -> this.addSlavesToProcessingCenter(master, sc));
    }

    /**
     * Build and process the machine (more specifically, the
     * {@link CS_Processamento} represented by the {@link WrappedElement}
     * {@code e}. Since the machine may or may not be a master, it can be
     * added to either the collection of {@link #masters} or {@link #machines}.
     *
     * @param e {@link WrappedElement} representing a {@link CS_Processamento}.
     * @return the interpreted {@link CS_Processamento} from the given
     * {@link WrappedElement}. May either be a {@link CS_Mestre} or a
     * {@link CS_Maquina}.
     */
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

    /**
     * Increase the power limit of the user with given id by the given amount.
     *
     * @param userId    id of the user whose power limit will be increased.
     * @param increment amount to increment the power limit by. Should be
     *                  <b>positive</b>.
     */
    protected void increaseUserPower(
            final String userId, final double increment) {
        final var oldValue = this.powerLimits.get(userId);
        this.powerLimits.put(userId, oldValue + increment);
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

    /**
     * Add {@link CentroServico} {@code slave} to the list of slaves of the
     * {@link CS_Processamento} {@code master} (which is always interpreted
     * as an instance of {@link CS_Mestre}. Note that {@code master} <b>is a
     * master</b>, and {@link CS_Processamento} may either be:
     * <ul>
     *      <li>another master</li>
     *      <li>a non-master machine</li>
     *      <li>a switch</li>
     * </ul>
     * In any case, the method process the element appropriately and updates
     * the necessary master-slave relations.
     *
     * @param master an instance of {@link CS_Mestre}.
     * @param slave  <b>slave</b> {@link CentroServico}.
     * @throws ClassCastException if the given {@link CS_Processamento}
     *                            {@code master} is not an instance of
     *                            {@link CS_Mestre}.
     * @apiNote the parameter {@code master} is typed as a
     * {@link CS_Processamento} to support overrides that deal with other
     * types of masters. See
     * {@link CloudQueueNetworkBuilder#addSlavesToProcessingCenter} for an
     * example.
     */
    protected void addSlavesToProcessingCenter(
            final CS_Processamento master, final CentroServico slave) {
        final var theMaster = (CS_Mestre) master;
        if (slave instanceof CS_Processamento proc) {
            theMaster.addEscravo(proc);
            if (slave instanceof CS_Maquina machine) {
                machine.addMestre(theMaster);
            }
        } else if (slave instanceof CS_Switch) {
            for (final var clusterSlave : this.clusterSlaves.get(slave)) {
                clusterSlave.addMestre(theMaster);
                theMaster.addEscravo(clusterSlave);
            }
        }
    }

    /**
     * Create a {@link RedeDeFilas} with the collections parsed from the
     * document. The method {@link #parseDocument(WrappedDocument)} must
     * already have been called on the instance.
     *
     * @return {@link RedeDeFilas} with the appropriate service centers,
     * links, and user configurations found in the document.
     */
    public RedeDeFilas build() {
        this.throwIfNoDocumentWasParsed();

        final var helper = new UserPowerLimit(this.powerLimits);
        this.setSchedulersUserMetrics(helper);

        final var queueNetwork = this.initQueueNetwork();
        queueNetwork.setUsuarios(helper.getOwners());
        return queueNetwork;
    }

    private void throwIfNoDocumentWasParsed() {
        if (!this.hasParsedADocument) {
            throw new IllegalStateException(
                    ".build() method called without a document parsed.");
        }
    }

    /**
     * For all {@link CS_Mestre}s parsed from the document, update its
     * {@link Escalonador}'s user metrics with the obtained user power limit
     * information.
     *
     * @param helper {@link UserPowerLimit} with the power limit information.
     */
    protected void setSchedulersUserMetrics(final UserPowerLimit helper) {
        this.masters.stream()
                .map(CS_Mestre.class::cast)
                .map(CS_Mestre::getEscalonador)
                .forEach(helper::setSchedulerUserMetrics);
    }

    /**
     * Construct a {@link RedeDeFilas} with the parsed {@link CentroServico}s
     * and user power limit information.
     *
     * @return initialized {@link RedeDeFilas}.
     */
    protected RedeDeFilas initQueueNetwork() {
        return new RedeDeFilas(
                this.masters, this.machines,
                this.links, this.internets,
                this.powerLimits
        );
    }
}