package ispd.policy.loaders;

import ispd.arquivo.xml.ConfiguracaoISPD;
import ispd.policy.scheduling.cloud.CloudSchedulingPolicy;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Carrega as classes dos escalonadores dinamicamente
 */
public class CarregarCloud {
    private static final String CLASS_PATH =
            "ispd.policy.scheduling.cloud.impl.";
    private static final URLClassLoader loader =
            CarregarCloud.makeLoaderSingleton();

    /**
     * Recebe o nome de um algoritmo de escalonamento e retorna uma nova
     * instancia
     * de um objeto com este nome ou null caso não encontre ou ocorra um erro.
     *
     * @param name
     * @return Nova instancia do objeto Escalonador
     */
    public static CloudSchedulingPolicy getNewEscalonadorCloud(final String name) {
        try {
            final var clsName = CarregarCloud.CLASS_PATH + name;
            final var cls = CarregarCloud.loader.loadClass(clsName);
            return (CloudSchedulingPolicy) cls.getConstructor().newInstance();
        } catch (final InstantiationException | NoSuchMethodException |
                       InvocationTargetException |
                       IllegalAccessException | ClassNotFoundException ex) {
            Logger.getLogger(CarregarCloud.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private static URLClassLoader makeLoaderSingleton() {
        try {
            return URLClassLoader.newInstance(
                    new URL[] { ConfiguracaoISPD.DIRETORIO_ISPD.toURI().toURL(), },
                    CarregarCloud.class.getClassLoader()
            );
        } catch (final MalformedURLException ex) {
            Logger.getLogger(CarregarCloud.class.getName())
                    .log(Level.SEVERE, "Could not create the loader!", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
}
