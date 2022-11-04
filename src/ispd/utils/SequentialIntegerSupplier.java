package ispd.utils;

import java.util.function.Supplier;

/**
 * Simple class to generate a (practically) infinite number of sequential
 * integers. Simply call {@link #get()} to get the next integer in line. The
 * first generated integer after construction is {@code 0}.
 */
public class SequentialIntegerSupplier implements Supplier<Integer> {
    private int nextInt = 0;

    @Override
    public Integer get() {
        final var retVal = this.nextInt;
        this.nextInt++;
        return retVal;
    }
}