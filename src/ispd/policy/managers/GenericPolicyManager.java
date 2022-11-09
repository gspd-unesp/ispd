package ispd.policy.managers;

import ispd.policy.PolicyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/* package-private */
abstract class GenericPolicyManager implements PolicyManager {
    protected final ArrayList<String> policies = new ArrayList<>(0);
    protected final List<String> addedPolicies = new ArrayList<>(0);
    protected final List<String> removedPolicies = new ArrayList<>(0);

    protected static void copyFile(final File dest, final File src) {
        if (dest.getPath().equals(src.getPath())) {
            return;
        }

        try (final var srcFs = new FileInputStream(src);
             final var destFs = new FileOutputStream(dest)) {
            srcFs.transferTo(destFs);
        } catch (final IOException ex) {
            Logger.getLogger(GenericPolicyManager.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return added policies
     */
    @Override
    public List listarAdicionados() {
        return this.addedPolicies;
    }

    /**
     * @return remove policies
     */
    @Override
    public List listarRemovidos() {
        return this.removedPolicies;
    }
}
