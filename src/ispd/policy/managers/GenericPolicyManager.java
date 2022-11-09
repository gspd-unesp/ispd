package ispd.policy.managers;

import ispd.policy.PolicyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class GenericPolicyManager implements PolicyManager {
    /**
     * Copy contents of file from {@code dest} to {@code src}, if their paths
     * are not equal
     */
    protected static void copyFile(final File dest, final File src) {
        if (dest.getPath().equals(src.getPath())) {
            return;
        }

        try (final var srcFs = new FileInputStream(src);
             final var destFs = new FileOutputStream(dest)) {
            srcFs.transferTo(destFs);
        } catch (final IOException ex) {
            Logger.getLogger(GenericPolicyManager.class.getModule().getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}
