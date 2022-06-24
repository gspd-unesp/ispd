package ispd.alocacaoVM;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads Scheduler instances dynamically.
 */
public class CarregarAlloc {
    private static final String DIR = ".";
    private static final String CLASS_PATH = "ispd.externo.cloudAlloc.";
    private static URLClassLoader loader = null;

    /**
     * Receives the name of a scheduling and returns an instance of an object
     * of such name, or null on error.
     *
     * @param nome Name of the scheduling algorithm desired.
     * @return New instance of a Scheduler object.
     */
    public static Alocacao getNewAlocadorVM(final String nome) {
        CarregarAlloc.makeLoaderSingleton();

        try {
            final var clsName = CarregarAlloc.CLASS_PATH + nome;
            final var cls = CarregarAlloc.loader.loadClass(clsName);
            return (Alocacao) cls.getConstructor().newInstance();
        } catch (final IllegalArgumentException | NoSuchMethodException |
                       InvocationTargetException | SecurityException |
                       InstantiationException | IllegalAccessException |
                       ClassNotFoundException ex) {
            Logger.getLogger(CarregarAlloc.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private static void makeLoaderSingleton() {
        if (CarregarAlloc.loader != null) {
            return;
        }

        final var dir = new File(CarregarAlloc.DIR);

        if (!dir.exists()) {
            return;
        }

        try {
            final var ref = new CarregarAlloc();
            final URL[] aux = new URL[1];
            aux[0] = dir.toURI().toURL();
            CarregarAlloc.loader = URLClassLoader.newInstance(
                    aux, ref.getClass().getClassLoader());
        } catch (final MalformedURLException ex) {
            Logger.getLogger(CarregarAlloc.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}