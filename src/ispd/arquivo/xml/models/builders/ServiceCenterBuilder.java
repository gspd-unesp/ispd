package ispd.arquivo.xml.models.builders;

import ispd.arquivo.xml.utils.WrappedElement;
import ispd.gui.iconico.grade.VirtualMachine;
import ispd.motor.filas.servidores.implementacao.CS_Internet;
import ispd.motor.filas.servidores.implementacao.CS_Link;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.filas.servidores.implementacao.CS_Switch;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;

/**
 * Utility class with static methods to build service centers for simulable
 * models and, exceptionally, virtual machines for cloud models
 */
public class ServiceCenterBuilder {
    /**
     * @return a master from the given {@link WrappedElement}
     */
    public static CS_Mestre aMaster(final WrappedElement e) {
        return new CS_Mestre(e.id(), e.owner(), e.power(), e.load(),
                e.master().scheduler(), e.energy());
    }

    /**
     * @return a machine (with one core) from the given {@link WrappedElement}
     */
    public static CS_Maquina aMachine(final WrappedElement e) {
        return new CS_Maquina(e.id(), e.owner(), e.power(), 1,
                e.load(), e.energy());
    }

    /**
     * @return a master (with load set to 0) from the given
     * {@link WrappedElement}
     */
    public static CS_Mestre aMasterWithNoLoad(final WrappedElement e) {
        return new CS_Mestre(e.id(), e.owner(), e.power(), 0.0,
                e.scheduler(), e.energy());
    }

    /**
     * @return a switch (with load set to 0) from the given
     * {@link WrappedElement}
     */
    public static CS_Switch aSwitch(final WrappedElement e) {
        return new CS_Switch(e.id(), e.bandwidth(), 0.0, e.latency());
    }

    /**
     * @return a machine with the given id from the given
     * {@link WrappedElement}. Its core count is set to 1 and its load
     * factor, to zero.
     */
    public static CS_Maquina aMachineWithId(
            final WrappedElement e, final int id) {
        return new CS_Maquina(e.id(), e.owner(), e.power(),
                1, 0.0, id + 1, e.energy());
    }

    /**
     * @return an internet from the given {@link WrappedElement}
     */
    public static CS_Internet anInternet(final WrappedElement e) {
        return new CS_Internet(e.id(), e.bandwidth(), e.load(), e.latency());
    }

    /**
     * @return a link from the given {@link WrappedElement}
     */
    public static CS_Link aLink(final WrappedElement e) {
        return new CS_Link(e.id(), e.bandwidth(), e.load(), e.latency());
    }

    /**
     * @return a cloud machine from the given {@link WrappedElement}
     */
    public static CS_MaquinaCloud aCloudMachine(final WrappedElement e) {
        final var characteristics = e.characteristics();
        final var costs = characteristics.costs();

        return new CS_MaquinaCloud(
                e.id(),
                e.owner(),
                characteristics.processor().power(),
                characteristics.processor().number(),
                e.load(),
                characteristics.memory().size(),
                characteristics.hardDisk().size(),
                costs.costProcessing(),
                costs.costMemory(),
                costs.costDisk()
        );
    }

    /**
     * @return a vmm from the given {@link WrappedElement}
     */
    public static CS_VMM aVirtualMachineMaster(final WrappedElement e) {
        final var characteristics = e.characteristics();

        return new CS_VMM(
                e.id(),
                e.owner(),
                characteristics.processor().power(),
                characteristics.memory().size(),
                characteristics.hardDisk().size(),
                e.load(),
                e.master().scheduler(),
                e.master().vmAlloc()
        );
    }

    /**
     * @return a vmm (with load set to 0) from the given {@link WrappedElement}
     */
    public static CS_VMM aVmmNoLoad(final WrappedElement e) {
        final var characteristics = e.characteristics();
        return new CS_VMM(
                e.id(),
                e.owner(),
                characteristics.processor().power(),
                characteristics.memory().size(),
                characteristics.hardDisk().size(),
                0.0,
                e.scheduler(),
                e.vmAlloc()
        );
    }

    /**
     * @return a cloud machine with the given id from the given
     * {@link WrappedElement}. Its load is set to 0.
     */
    public static CS_MaquinaCloud aCloudMachineWithId(
            final WrappedElement e, final int j) {
        final var characteristics = e.characteristics();
        final var costs = characteristics.costs();

        return new CS_MaquinaCloud(
                "%s.%d".formatted(e.id(), j),
                e.owner(),
                characteristics.processor().power(),
                characteristics.processor().number(),
                characteristics.memory().size(),
                characteristics.hardDisk().size(),
                costs.costProcessing(),
                costs.costMemory(),
                costs.costDisk(),
                0.0,
                j + 1
        );
    }

    /**
     * @return a virtual machine from the given {@link WrappedElement}. Its
     * power is treated as an integral value.
     */
    public static CS_VirtualMac aVirtualMachine(final WrappedElement e) {
        return new CS_VirtualMac(
                e.id(),
                e.owner(),
                (int) e.power(),
                e.memAlloc(),
                e.diskAlloc(),
                e.opSystem()
        );
    }

    /**
     * @return a virtual machine from the given {@link WrappedElement}. Its
     * power is treated as an integral value and it fetches its master's id.
     */
    public static VirtualMachine aVirtualMachineWithVmm(final WrappedElement e) {
        return new VirtualMachine(e.id(), e.owner(), e.vmm(), (int) e.power(),
                e.memAlloc(), e.diskAlloc(), e.opSystem());
    }
}
