package ispd.NumerosAleatorios;

public class GeracaoNumAleatorios
{   
    private double semente;

    public GeracaoNumAleatorios( int s )
    {   semente = ( s >= 0 ) ? s : 0;
    }
 
	public double exponencial( double media )
	{   double retorno;
	   
		retorno = -media * Math.log( Math.random() );
		
        return ( retorno );
    }

	public double normal( double media, double desvioPadrao )
	{   double soma12 = 0.0;
	   
		for( int i = 0 ; i < 12 ; i++ )
		   soma12 = soma12 + Math.random();
		
        return ( media + ( desvioPadrao * (soma12 - 6.0) )  );
    }
	
//http://www.cs.huji.ac.il/labs/parallel/workload/m_lublin99/m_lublin99.c	
    public double twoStageUniform(double low, double med, double hi, double prob)
    {   double a;
        double b;
        double tsu;
        double u;
 
        u = Math.random();

        if (u <= prob) 
        { /* uniform(low , med) */
            a = low;
            b = med;
        }
        else  
        { /* uniform(med , hi) */
            a = med;
            b = hi;
        }
  
  //generate a value of a random variable from distribution uniform(a,b) 
        tsu = (Math.random() * (b-a) ) + a;
        return tsu;
    }
}
