package ispd.arquivo.xml.models.builders;

import ispd.arquivo.xml.utils.WrappedDocument;
import ispd.arquivo.xml.utils.WrappedElement;
import ispd.motor.workload.WorkloadGenerator;
import ispd.motor.workload.WorkloadGeneratorType;
import ispd.motor.workload.impl.CollectionWorkloadGenerator;
import ispd.motor.workload.impl.GlobalWorkloadGenerator;
import ispd.motor.workload.impl.PerNodeWorkloadGenerator;
import ispd.motor.workload.impl.TraceFileWorkloadGenerator;
import ispd.utils.SequentialIntSupplier;

import java.io.File;
import java.util.Optional;
import java.util.function.IntSupplier;

/**
 * Converts XML docs with simulation workload configuration into
 * {@link WorkloadGenerator} objects, usable in the simulation motor.
 */
public class LoadBuilder {

    /**
     * Attempts to find and convert workload configuration info from the given
     * {@link WrappedDocument xml document}, instancing a
     * {@link WorkloadGenerator}.
     *
     * @param doc {@link WrappedDocument}, possibly with workload configuration
     * @return {@link Optional} containing parsed {@link WorkloadGenerator},
     * if there was valid configuration for one in the document. Otherwise,
     * an empty {@link Optional}.
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
        final var computation = e.makeTwoStageFromInnerSizes(
                WrappedElement::isComputingType,
                WrappedElement::toTwoStageUniform
        );

        final var communication = e.makeTwoStageFromInnerSizes(
                WrappedElement::isCommunicationType,
                WrappedElement::toTwoStageUniform
        );

        return new GlobalWorkloadGenerator(
                e.tasks(), e.arrivalTime(),
                computation, communication
        );
    }

    private static Optional<CollectionWorkloadGenerator> nodeLoadsFromElement(final WrappedElement e) {
        final var idSupplier = new SequentialIntSupplier();

        final var nodeLoads = e.nodeLoads()
                .map(el -> LoadBuilder.nodeLoadFromElement(el, idSupplier))
                .map(WorkloadGenerator.class::cast)
                .toList();

        if (nodeLoads.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new CollectionWorkloadGenerator(
                WorkloadGeneratorType.PER_NODE,
                nodeLoads
        ));
    }

    private static TraceFileWorkloadGenerator traceLoadFromElement(final WrappedElement e) {
        final var file = new File(e.filePath());

        if (file.exists()) {
            return new TraceFileWorkloadGenerator(file, e.tasks(), e.format());
        }

        return null;
    }

    private static PerNodeWorkloadGenerator nodeLoadFromElement(
            final WrappedElement e, final IntSupplier idSupplier) {
        final var computation =
                e.makeTwoStageFromInnerSizes(
                        WrappedElement::isComputingType,
                        WrappedElement::toUniformDistribution
                );

        final var communication =
                e.makeTwoStageFromInnerSizes(
                        WrappedElement::isCommunicationType,
                        WrappedElement::toUniformDistribution
                );

        return new PerNodeWorkloadGenerator(
                e.application(), e.owner(),
                e.masterId(), e.tasks(),
                computation, communication,
                idSupplier
        );
    }
}