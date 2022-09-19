package ispd.gui.auxiliar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.filechooser.FileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class TestAuxiliaryFileFilter {

    /**
     * Random-purpose character array.
     */
    private static final char[] CHARS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    /**
     * It represents the quantity of file extensions to be randomly
     * generated.
     */
    private static final int EXTENSION_LIST_QUANTITY = 100;

    /**
     * It represents the quantity of file name to be randomly generated.
     */
    private static final int FILENAME_LIST_QUANTITY = 200;

    @Test
    public void run() {
        final var extensionList
                = this.randomizeExtensions(EXTENSION_LIST_QUANTITY);
        final var filenameList =
                this.randomizeFilenames(FILENAME_LIST_QUANTITY, extensionList);

        final var wrapperOldFileFilter = new FileFilterWrapper(
                new MultipleExtensionFileFilter(null, extensionList.toArray(new String[0]), false));
        final var wrapperNewFileFilter = new FileFilterWrapper(
                new AuxiliaryFileFilter(null, extensionList, false));

        final var filteredOldFileFilter
                = this.filteredFilenameList(filenameList, wrapperOldFileFilter);
        final var filteredNewFileFilter
                = this.filteredFilenameList(filenameList, wrapperNewFileFilter);

        filteredOldFileFilter.sort(String::compareTo);
        filteredNewFileFilter.sort(String::compareTo);

        Assertions.assertEquals(filteredOldFileFilter,
                filteredNewFileFilter);
    }

    /* Private Methods */

    /**
     * It returns a list of the filtered file name list relative to
     * a specified {@link FileFilter}.
     *
     * @param filenameList the filename list to be filtered
     * @param fileFilterWrapper the file filter (wrapper)
     *
     * @return aa list of the filtered file name list
     */
    private List<String> filteredFilenameList(
            final List<String> filenameList,
            final FileFilterWrapper fileFilterWrapper) {
        return filenameList
                .stream()
                .filter(fileFilterWrapper::accept)
                .collect(Collectors.toList());
    }

    /**
     * It returns a random generated string with a specified length. This
     * method suppose a non-negative integer for length.
     *
     * @param length the length (a non-negative integer)
     *
     * @return a random generated string
     */
    private String randomizeString(final int length) {
        final var sb = new StringBuilder();

        for (int i = 0; i < length; i++)
            sb.append(CHARS[(int) (Math.random() * CHARS.length)]);

        return sb.toString();
    }

    /**
     * It returns a list of random generated file extensions with a
     * specified quantity. This method suppose a non-negative integer
     * for quantity.
     *
     * @param quantity the quantity (a non-negative integer)
     *
     * @return a list of random generated file extensions
     */
    private List<String> randomizeExtensions(final int quantity) {
        final var extensionList = new ArrayList<String>();

        for (int i = 0; i < quantity; i++)
            extensionList.add(this.randomizeString(3));

        return extensionList;
    }

    /**
     * It returns a list of random generated file names, with a specified
     * quantity (a non-negative integer) and a list of extensions.
     *
     * @param quantity the quantity (a non-negative integer)
     * @param extensions the list of extensions
     *
     * @return a list of random generated file names
     */
    private List<String> randomizeFilenames(final int quantity,
                                            final List<String> extensions) {
        final var filenameList = new ArrayList<String>();

        for (int i = 0; i < quantity; i++) {
            final var filename = this.randomizeString(8);
            final var extension = extensions
                    .get((int) (Math.random() * extensions.size()));

            filenameList.add(filename + "." + extension);
        }

        return filenameList;
    }

    private static final class FileFilterWrapper {

        private final FileFilter fileFilter;

        /**
         * Constructor of {@link FileFilterWrapper} which specifies
         * the file filter.
         *
         * @param fileFilter the file filter
         */
        /* package-private */ FileFilterWrapper(final FileFilter fileFilter) {
            this.fileFilter = fileFilter;
        }

        /**
         * It returns the {@code true} if the given filename is accepted
         * by the {@link FileFilter}; otherwise it returns {@code null}.
         *
         * @param filename the filename to be accepted
         *
         * @return {@code true} since the given filename is accepted;
         *         otherwise, it returns {@code null}.
         */
        public boolean accept(final String filename) {
            return this.fileFilter.accept(new File(filename));
        }
    }
}
