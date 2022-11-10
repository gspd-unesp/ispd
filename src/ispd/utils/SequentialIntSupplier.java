package ispd.utils;

import java.util.function.IntSupplier;

/**
 * Simple class to generate a (practically) infinite number of sequential
 * integers. Simply call {@link #getAsInt()} to get the next integer in line.
 * The first generated integer after construction is {@code 0}.
 */
public class SequentialIntSupplier implements IntSupplier {
    private int nextInt = 0;

    @Override
    public int getAsInt() {
        final var retVal = this.nextInt;
        this.nextInt++;
        return retVal;
    }
}