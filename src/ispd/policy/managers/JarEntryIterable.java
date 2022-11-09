package ispd.policy.managers;

import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public record JarEntryIterable(JarFile jar) implements Iterable<JarEntry> {
    @Override
    public Iterator<JarEntry> iterator() {
        return this.jar.entries().asIterator();
    }
}
