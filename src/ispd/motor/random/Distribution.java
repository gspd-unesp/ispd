/* ==========================================================
 * iSPD : iconic Simulator of Parallel and Distributed System
 * ==========================================================
 *
 * (C) Copyright 2010-2014, by Grupo de pesquisas em Sistemas Paralelos e
 * Distribuídos da Unesp (GSPD).
 *
 * Project Info:  http://gspd.dcce.ibilce.unesp.br/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 *  USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------
 * CargaRandom.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e
 * Distribuídos da Unesp (GSPD).
 *
 * Original Author:  -;
 * Contributor(s):   -;
 *
 * Changes
 * -------
 *
 * 09-Set-2014 : Version 2.0;
 *
 */
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

    public double nextExponential(final double b) {
        return -1 * b * Math.log(this.nextDouble());
    }

    // http://www.cs.huji.ac.il/labs/parallel/workload/m_lublin99/m_lublin99.c
    public double twoStageUniform(final double low, final double med,
                                  final double hi, final double prob) {
        final double a;
        final double b;

        if (this.nextDouble() <= prob) { /* uniform(low , med) */
            a = low;
            b = med;
        } else { /* uniform(med , hi) */
            a = med;
            b = hi;
        }

        //generate a value of a random variable from distribution uniform(a,b)
        return (this.nextDouble() * (b - a)) + a;
    }
}