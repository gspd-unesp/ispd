package ispd.gui.auxiliar;

import javax.swing.filechooser.FileFilter;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class AuxiliaryFileFilter extends FileFilter {

    /**
     * It represents the description of this auxiliary file filter.
     * More generally, the description gives a concise context about
     * what files will be filtered.
     */
    private final String description;

    /**
     * It stores all file extensions to be filtered.
     */
    private final List<String> extensions;

    /**
     * It inform whether or not a file that is a directory must be
     * filtered, that is, if this variable is {@code true} then
     * directories will be filtered; otherwise, it will not.
     */
    private final boolean filterDirectory;

    /**
     * Constructor of {@link AuxiliaryFileFilter} specifying the
     * file filter description, extensions and whether  file filter
     * must filter directories as well.
     *
     * @param description the description
     * @param extensions the file extensions
     * @param filterDirectory whether or not directories must be
     *                        filtered
     */
    public AuxiliaryFileFilter(final String description,
                               final List<String> extensions,
                               final boolean filterDirectory) {
        this.description = description;
        this.extensions = Objects.requireNonNullElse(extensions,
                Collections.emptyList());
        this.filterDirectory = filterDirectory;
    }

    /**
     * Whether the given file must be accepted by this filter.
     *
     * @param file the file to be accepted
     *
     * @return {@code true} if this file is to be accepted; otherwise,
     *         {@code false} is returned.
     */
    @Override
    public boolean accept(final File file) {
        return (file.isDirectory() && this.filterDirectory) ||
                this.extensions.stream().anyMatch((extension)
                        -> file.getName().toLowerCase().endsWith(extension));
    }

    /**
     * Returns the file filter description.
     * @return the file filter description
     */
    @Override
    public String getDescription() {
        return this.description;
    }
}
