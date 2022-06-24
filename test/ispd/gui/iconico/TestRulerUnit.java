package ispd.gui.iconico;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class TestRulerUnit {

    /**
     * It tests the {@link Ruler.RulerUnit#nextUnit(Ruler.RulerUnit)}.
     */
    @Test
    public void testNextUnit() {
        final var cmUnit = Ruler.RulerUnit.CENTIMETERS;
        final var inUnit = Ruler.RulerUnit.INCHES;

        Assertions.assertEquals(Ruler.RulerUnit.INCHES,
                cmUnit.nextUnit());
        Assertions.assertEquals(Ruler.RulerUnit.CENTIMETERS,
                inUnit.nextUnit());
    }
}
