package ispd.policy.loaders;

import ispd.policy.scheduling.cloud.CloudSchedulingPolicy;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Carrega as classes dos escalonadores dinamicamente
 */
public class CarregarCloud extends PolicyLoader {
    private static final String CLASS_PATH =
            "ispd.policy.scheduling.cloud.impl.";

    /**
     * Recebe o nome de um algoritmo de escalonamento e retorna uma nova
     * instancia
     * de um objeto com este nome ou null caso não encontre ou ocorra um erro.
     *
     * @param name
     * @return Nova instancia do objeto Escalonador
     */
    public CloudSchedulingPolicy loadPolicy(final String name) {
        try {
            final var clsName = getClassPath() + name;
            final var cls = PolicyLoader.classLoader.loadClass(clsName);
            return (CloudSchedulingPolicy) cls.getConstructor().newInstance();
        } catch (final InstantiationException | NoSuchMethodException |
                       InvocationTargetException |
                       IllegalAccessException | ClassNotFoundException ex) {
            Logger.getLogger(CarregarCloud.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }

    protected String getClassPath() {
        return CarregarCloud.CLASS_PATH;
    }
}
