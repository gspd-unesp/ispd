package ispd.policy.loaders;

import ispd.gui.LogExceptions;
import ispd.policy.scheduling.grid.GridSchedulingPolicy;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Carrega as classes dos escalonadores dinamicamente
 *
 * @author denison
 */
public class Carregar {
    public static final File DIRETORIO_ISPD = Carregar.loadIspdDirectory();
    private static final String CLASS_PATH = "ispd.policy.externo.";
    private static final URLClassLoader loader = Carregar.makeLoaderSingleton();

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
            final var cls = Carregar.loader.loadClass(clsName);
            return (GridSchedulingPolicy) cls.getConstructor().newInstance();
        } catch (final NoSuchMethodException |
                       InvocationTargetException | InstantiationException |
                       IllegalAccessException | ClassNotFoundException ex) {
            Logger.getLogger(Carregar.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Localiza arquivo do programa iSPD
     *
     * @return diretório que fica o iSPD
     */
    private static File loadIspdDirectory() {
        final var dir = Carregar.getDirectory();

        if (dir.getName().endsWith(".jar")) {
            return dir.getParentFile();
        } else {
            return new File(".");
        }
    }

    private static File getDirectory() {
        final var location = LogExceptions.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation();

        try {
            return new File(location.toURI());
        } catch (final URISyntaxException ex) {
            return new File(location.getPath());
        }
    }

    private static URLClassLoader makeLoaderSingleton() {
        try {
            return URLClassLoader.newInstance(
                    new URL[] { Carregar.DIRETORIO_ISPD.toURI().toURL(), },
                    Carregar.class.getClassLoader()
            );
        } catch (final MalformedURLException ex) {
            Logger.getLogger(Carregar.class.getName())
                    .log(Level.SEVERE, null, ex);
            // TODO: Use caught exception in new one
            throw new ExceptionInInitializerError("Can't create the Loader!");
        }
    }
}
