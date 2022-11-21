package ispd.policy.loaders;

import ispd.policy.allocation.vm.VmAllocationPolicy;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads Scheduler instances dynamically.
 */
public class CarregarAlloc extends PolicyLoader {
    private static final String CLASS_PATH = "ispd.policy.allocation.vm.impl.";

    /**
     * Receives the name of a scheduling and returns an instance of an object
     * of such name, or null on error.
     *
     * @param name Name of the scheduling algorithm desired.
     * @return New instance of a Scheduler object.
     */
    public VmAllocationPolicy loadPolicy(final String name) {
        try {
            final var clsName = getClassPath() + name;
            final var cls = PolicyLoader.classLoader.loadClass(clsName);
            return (VmAllocationPolicy) cls.getConstructor().newInstance();
        } catch (final NoSuchMethodException |
                       InvocationTargetException | InstantiationException |
                       IllegalAccessException | ClassNotFoundException ex) {
            Logger.getLogger(CarregarAlloc.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    protected String getClassPath() {
        return CarregarAlloc.CLASS_PATH;
    }
}