package ispd.arquivo.xml.models.builders;

import ispd.arquivo.xml.utils.WrappedDocument;
import ispd.arquivo.xml.utils.WrappedElement;
import ispd.motor.carga.CollectionWorkloadGenerator;
import ispd.motor.carga.PerNodeWorkloadGenerator;
import ispd.motor.carga.GlobalWorkloadGenerator;
import ispd.motor.carga.TraceFileWorkloadGenerator;
import ispd.motor.carga.WorkloadGenerator;
import ispd.motor.carga.WorkloadGeneratorType;
import ispd.motor.carga.task.TaskSize;

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
     * @see WorkloadGenerator
     */
    public static Optional<? extends WorkloadGenerator> build(final WrappedDocument doc) {
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

    private static GlobalWorkloadGenerator randomLoadFromElement(final WrappedElement e) {
        final var computation = LoadBuilder.getTaskSizeFromElement(
                e, WrappedElement::isComputingType,
                WrappedElement::toTaskSize);

        final var communication = LoadBuilder.getTaskSizeFromElement(
                e, WrappedElement::isCommunicationType,
                WrappedElement::toTaskSize);

        return new GlobalWorkloadGenerator(
                e.tasks(), e.arrivalTime(), computation, communication);
    }

    private static Optional<CollectionWorkloadGenerator> nodeLoadsFromElement(final WrappedElement e) {
        final var nodeLoads = e.nodeLoads()
                .map(LoadBuilder::nodeLoadFromElement)
                .toList();

        if (nodeLoads.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new CollectionWorkloadGenerator(nodeLoads,
                WorkloadGeneratorType.PER_NODE));
    }

    private static TraceFileWorkloadGenerator traceLoadFromElement(final WrappedElement e) {
        final var file = new File(e.filePath());

        if (file.exists()) {
            return new TraceFileWorkloadGenerator(file, e.tasks(), e.format());
        }

        return null;
    }

    private static TaskSize getTaskSizeFromElement(
            final WrappedElement element,
            final Predicate<? super WrappedElement> predicate,
            final Function<? super WrappedElement, TaskSize> builder) {
        return element.sizes()
                .filter(predicate)
                .findFirst()
                .map(builder)
                .orElseGet(TaskSize::new);
    }

    private static PerNodeWorkloadGenerator nodeLoadFromElement(final WrappedElement e) {
        final var computation = LoadBuilder.getTaskSizeFromElement(
                e, WrappedElement::isComputingType,
                WrappedElement::toTaskSizeRange);

        final var communication = LoadBuilder.getTaskSizeFromElement(
                e, WrappedElement::isCommunicationType,
                WrappedElement::toTaskSizeRange);

        return new PerNodeWorkloadGenerator(
                e.application(), e.owner(), e.masterId(), e.tasks(),
                computation,
                communication
        );
    }

}