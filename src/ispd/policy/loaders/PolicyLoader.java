package ispd.policy.loaders;

import ispd.arquivo.xml.ConfiguracaoISPD;
import ispd.policy.Policy;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class PolicyLoader <T extends Policy<?>> {
    private static final URLClassLoader classLoader =
            PolicyLoader.makeClassLoader();

    private static URLClassLoader makeClassLoader() {
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

    public T loadPolicy(final String policyName) {
        final var clsName = this.getClassPath() + policyName;
        try {
            final var cls = PolicyLoader.classLoader.loadClass(clsName);
            return (T) cls.getConstructor().newInstance();
        } catch (final ClassNotFoundException | InvocationTargetException |
                       InstantiationException | IllegalAccessException |
                       NoSuchMethodException | ClassCastException e) {
            Logger.getLogger(PolicyLoader.class.getName()).log(
                    Level.SEVERE,
                    "Could not load policy '%s'!\n".formatted(policyName),
                    e
            );

            throw new RuntimeException(e);
        }
    }

    protected abstract String getClassPath();
}
