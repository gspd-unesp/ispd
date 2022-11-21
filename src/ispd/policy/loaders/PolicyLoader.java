package ispd.policy.loaders;

import ispd.arquivo.xml.ConfiguracaoISPD;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class PolicyLoader {
    protected static final URLClassLoader classLoader =
            PolicyLoader.makeLoaderSingleton();

    private static URLClassLoader makeLoaderSingleton() {
        try {
            return URLClassLoader.newInstance(
                    new URL[] { ConfiguracaoISPD.DIRETORIO_ISPD.toURI().toURL(), },
                    PolicyLoader.class.getClassLoader()
            );
        } catch (final MalformedURLException ex) {
            Logger.getLogger(PolicyLoader.class.getName())
                    .log(Level.SEVERE, "Could not create the loader!", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    protected abstract String getClassPath();
}
