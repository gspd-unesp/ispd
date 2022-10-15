package ispd.arquivo.xml.models.builders;

import ispd.arquivo.xml.utils.SizeInfo;
import ispd.arquivo.xml.utils.WrappedDocument;
import ispd.arquivo.xml.utils.WrappedElement;
import ispd.motor.carga.CargaForNode;
import ispd.motor.carga.CargaList;
import ispd.motor.carga.CargaRandom;
import ispd.motor.carga.CargaTrace;
import ispd.motor.carga.GerarCarga;

import java.io.File;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Converts XML docs with simulation load information into objects usable in
 * the simulation motor.
 */
public class LoadBuilder {

    /**
     * Attempts to convert xml document given as param to a simulation load.
     *
     * @param doc {@link WrappedDocument}, possibly with load information
     * @return {@link Optional} containing parsed load, if there was a valid
     * one in the document. Otherwise, an empty Optional.
     * @see ispd.arquivo.xml.IconicoXML
     * @see GerarCarga
     */
    public static Optional<? extends GerarCarga> build(final WrappedDocument doc) {
        final var load = doc.loads().findFirst();

        if (load.isEmpty()) {
            return Optional.empty();
        }

        final var c = load.get();

        final var randomLoad = c.randomLoads()
                .findFirst()
                .map(LoadBuilder::randomLoadFromElement);

        if (randomLoad.isPresent()) {
            return randomLoad;
        }

        final var nodeLoad = LoadBuilder.nodeLoadsFromElement(c);

        if (nodeLoad.isPresent()) {
            return nodeLoad;
        }

        return c.traceLoads()
                .findFirst()
                .map(LoadBuilder::traceLoadFromElement);
    }

    private static CargaRandom randomLoadFromElement(final WrappedElement e) {
        final var computation = LoadBuilder.getSizeInfoFromElement(
                e, WrappedElement::isComputingType, SizeInfo::new);

        final var communication = LoadBuilder.getSizeInfoFromElement(
                e, WrappedElement::isCommunicationType, SizeInfo::new);

        return new CargaRandom(
                e.tasks(),
                (int) computation.minimum(), (int) computation.maximum(),
                (int) computation.average(), computation.probability(),
                (int) communication.minimum(), (int) communication.maximum(),
                (int) communication.average(), communication.probability(),
                e.arrivalTime()
        );
    }

    private static Optional<CargaList> nodeLoadsFromElement(final WrappedElement e) {
        final var nodeLoads = e.nodeLoads()
                .map(LoadBuilder::nodeLoadFromElement)
                .toList();

        if (nodeLoads.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new CargaList(nodeLoads, GerarCarga.FORNODE));
    }

    private static CargaTrace traceLoadFromElement(final WrappedElement e) {
        final var file = new File(e.filePath());

        if (file.exists()) {
            return new CargaTrace(file, e.tasks(), e.format());
        }

        return null;
    }

    private static SizeInfo getSizeInfoFromElement(
            final WrappedElement element,
            final Predicate<? super WrappedElement> predicate,
            final Function<? super WrappedElement, SizeInfo> builder) {
        return element.sizes()
                .filter(predicate)
                .findFirst()
                .map(builder)
                .orElseGet(SizeInfo::new);
    }

    private static CargaForNode nodeLoadFromElement(final WrappedElement e) {
        final var computation = LoadBuilder.getSizeInfoFromElement(
                e, WrappedElement::isComputingType, SizeInfo::rangeFrom);

        final var communication = LoadBuilder.getSizeInfoFromElement(
                e, WrappedElement::isCommunicationType, SizeInfo::rangeFrom);

        return new CargaForNode(e.application(),
                e.owner(), e.masterId(), e.tasks(),
                computation.maximum(), computation.minimum(),
                communication.maximum(), communication.minimum()
        );
    }

}