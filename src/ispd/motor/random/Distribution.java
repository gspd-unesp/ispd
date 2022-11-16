package ispd.motor.random;

import java.util.Random;

/**
 * Distribution.java
 * This class generates various random variables for
 * distributions not directly supported in Java
 */
public class Distribution extends Random {

    public Distribution(final long seed) {
        super(seed);
    }

    public double nextExponential(final double beta) {
        return -1 * beta * Math.log(this.nextDouble());
    }

    // http://www.cs.huji.ac.il/labs/parallel/workload/m_lublin99/m_lublin99.c
    public double twoStageUniform(final double low, final double med,
                                  final double hi, final double prob) {
        if (this.nextDouble() <= prob) {
            return this.getUniform(low, med);
        } else {
            return this.getUniform(med, hi);
        }
    }

    private double getUniform(final double lb, final double hb) {
        return (this.nextDouble() * (hb - lb)) + lb;
    }
}