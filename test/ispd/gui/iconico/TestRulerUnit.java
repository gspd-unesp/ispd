package ispd.gui.iconico;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class TestRulerUnit {

    /**
     * It tests the {@link RulerUnit#nextUnit()}.
     */
    @Test
    public void testNextUnit() {
        final var cmUnit = RulerUnit.CENTIMETERS;
        final var inUnit = RulerUnit.INCHES;

        Assertions.assertEquals(RulerUnit.INCHES,
                cmUnit.nextUnit());
        Assertions.assertEquals(RulerUnit.CENTIMETERS,
                inUnit.nextUnit());
    }
}
