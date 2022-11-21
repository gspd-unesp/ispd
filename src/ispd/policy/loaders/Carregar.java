package ispd.policy.loaders;

import ispd.policy.scheduling.grid.GridSchedulingPolicy;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Carrega as classes dos escalonadores dinamicamente
 *
 * @author denison
 */
public class Carregar extends PolicyLoader {
    private static final String CLASS_PATH =
            "ispd.policy.scheduling.grid.impl.";

    /**
     * Recebe o nome de um algoritmo de escalonamento e retorna uma nova
     * instancia de um objeto com este nome ou null caso não encontre ou ocorra
     * um erro.
     *
     * @param name
     * @return Nova instancia do objeto Escalonador
     */
    public static GridSchedulingPolicy getNewEscalonador(final String name) {
        try {
            final var clsName = Carregar.CLASS_PATH + name;
            final var cls = PolicyLoader.classLoader.loadClass(clsName);
            return (GridSchedulingPolicy) cls.getConstructor().newInstance();
        } catch (final NoSuchMethodException |
                       InvocationTargetException | InstantiationException |
                       IllegalAccessException | ClassNotFoundException ex) {
            Logger.getLogger(Carregar.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
