package ispd.policy.loaders;

import ispd.policy.scheduling.cloud.EscalonadorCloud;

import java.io.File;
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
    private static final String DIR = ".";
    private static final String CLASS_PATH = "ispd.policy.externo.cloudSchedulers.";
    private static URLClassLoader loader = null;

    /**
     * Recebe o nome de um algoritmo de escalonamento e retorna uma nova
     * instancia
     * de um objeto com este nome ou null caso não encontre ou ocorra um erro.
     *
     * @param name
     * @return Nova instancia do objeto Escalonador
     */
    public static EscalonadorCloud getNewEscalonadorCloud(final String name) {
        CarregarCloud.makeLoaderSingleton();

        try {
            final var clsName = CarregarCloud.CLASS_PATH + name;
            final var cls = CarregarCloud.loader.loadClass(clsName);
            return (EscalonadorCloud) cls.getConstructor().newInstance();
        } catch (final InstantiationException | NoSuchMethodException |
                       InvocationTargetException |
                       IllegalAccessException | ClassNotFoundException ex) {
            Logger.getLogger(CarregarCloud.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private static void makeLoaderSingleton() {
        if (CarregarCloud.loader != null) {
            return;
        }

        final var dir = new File(CarregarCloud.DIR);

        if (!dir.exists()) {
            return;
        }

        CarregarCloud.loader = CarregarCloud.getLoaderFromDir(dir);
    }

    private static URLClassLoader getLoaderFromDir(final File dir) {
        try {
            return URLClassLoader.newInstance(
                    new URL[] { dir.toURI().toURL(), },
                    CarregarCloud.class.getClassLoader()
            );
        } catch (final MalformedURLException ex) {
            Logger.getLogger(CarregarCloud.class.getName())
                    .log(Level.SEVERE, null, ex);
            return CarregarCloud.loader;
        }
    }
}