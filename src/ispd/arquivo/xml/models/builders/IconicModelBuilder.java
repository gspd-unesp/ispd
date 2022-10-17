package ispd.arquivo.xml.models.builders;

import ispd.arquivo.xml.models.IconicModel;
import ispd.arquivo.xml.utils.WrappedDocument;
import ispd.arquivo.xml.utils.WrappedElement;
import ispd.gui.iconico.Edge;
import ispd.gui.iconico.Vertex;
import ispd.gui.iconico.grade.GridItem;
import ispd.gui.iconico.grade.Link;
import ispd.gui.iconico.grade.Machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Builds iconic models from a {@link WrappedDocument} representing the system.
 * Instantiate and call {@link #build()}
 */
public class IconicModelBuilder {
    private final List<Vertex> vertices = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final Map<Integer, Object> icons = new HashMap<>();

    public IconicModelBuilder(final WrappedDocument doc) {
        doc.clusters().forEach(this::processClusterElement);
        doc.internets().forEach(this::processInternetElement);
        doc.machines().forEach(this::processMachineElement);
        doc.masters().forEach(this::setMasterCharacteristics);
        doc.links().forEach(this::processLinkElement);
    }

    private void processClusterElement(final WrappedElement e) {
        final var cluster = IconBuilder.aCluster(e);

        this.vertices.add(cluster);
        this.icons.put(e.globalIconId(), cluster);
    }

    private void processInternetElement(final WrappedElement e) {
        final var net = IconBuilder.anInternet(e);

        this.vertices.add(net);
        this.icons.put(e.globalIconId(), net);
    }

    private void processMachineElement(final WrappedElement m) {
        final var machine = IconBuilder.aMachine(m);

        this.vertices.add(machine);
        this.icons.put(m.globalIconId(), machine);
    }

    private void setMasterCharacteristics(final WrappedElement e) {
        final var master = (Machine) this.icons.get(e.globalIconId());

        final var elem = e.master();

        master.setSchedulingAlgorithm(elem.scheduler());
        master.setVmmAllocationPolicy(elem.vmAlloc());
        master.setMaster(true);

        final var slaves = elem.slaves()
                .map(WrappedElement::id)
                .map(Integer::parseInt)
                .map(this.icons::get)
                .filter(Objects::nonNull)
                .map(GridItem.class::cast)
                .toList();

        master.setSlaves(slaves);
    }

    private void processLinkElement(final WrappedElement e) {
        this.edges.add(this.connectedLinkFromElement(e));
    }

    private Link connectedLinkFromElement(final WrappedElement e) {
        final var origination = this.getVertex(e.origination());
        final var destination = this.getVertex(e.destination());

        final var link = IconBuilder.aLink(e, origination, destination);

        IconicModelBuilder.connectLinkAndVertices(
                link, (GridItem) origination, (GridItem) destination);

        return link;
    }

    private Vertex getVertex(final int e) {
        return (Vertex) this.icons.get(e);
    }

    private static void connectLinkAndVertices(
            final GridItem link,
            final GridItem origination, final GridItem destination) {
        origination.getOutboundConnections().add(link);
        destination.getInboundConnections().add(link);
    }

    public IconicModel build() {
        return new IconicModel(this.vertices, this.edges);
    }
}