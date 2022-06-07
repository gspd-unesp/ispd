/* ==========================================================
 * iSPD : iconic Simulator of Parallel and Distributed System
 * ==========================================================
 *
 * (C) Copyright 2010-2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------
 * CargaRandom.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
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

    public Distribution() {
        super();
    }

    public Distribution(long seed) {
        super(seed);
    }
    
    //http://www.math.csusb.edu/faculty/stanton/probstat/poisson/Distribution.java
    public int nextPoisson(double lambda) {
        double elambda = Math.exp(-1 * lambda);
        double product = 1;
        int count = 0;
        int result = 0;
        while (product >= elambda) {
            product *= nextDouble();
            result = count;
            count++; // keep result one behind
        }
        return result;
    }

    public double nextExponential(double b) {
        double randx;
        double result;
        randx = nextDouble();
        result = -1 * b * Math.log(randx);
        return result;
    }

    //Code iSPD 1.0
    public double nextNormal(double media, double desvioPadrao) {
        double soma12 = 0.0;
        for (int i = 0; i < 12; i++) {
            soma12 = soma12 + this.nextDouble();
        }
        return (media + (desvioPadrao * (soma12 - 6.0)));
    }

    //http://www.cs.huji.ac.il/labs/parallel/workload/m_lublin99/m_lublin99.c
    public double twoStageUniform(double low, double med, double hi, double prob) {
        double a;
        double b;
        double tsu;
        double u;

        u = this.nextDouble();
        if (u <= prob) { /* uniform(low , med) */
            a = low;
            b = med;
        } else { /* uniform(med , hi) */
            a = med;
            b = hi;
        }

        //generate a value of a random variable from distribution uniform(a,b) 
        tsu = (this.nextDouble() * (b - a)) + a;
        return tsu;
    }

    public double nextLogNormal(double media, double desvioPadrao) {
        double y = Math.exp(this.nextNormal(media, desvioPadrao));
        return y;
    }

    public double nextWeibull(double scale, double shape) {
        return scale * Math.pow(-Math.log(1 - this.nextDouble()), 1 / shape);
    }
}