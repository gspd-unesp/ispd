package ispd.Simulacao;

public class Relogio
{   private Double relogioSimulacao;
    
	public Relogio( Double tempo )
	{   setRelogioSimulacao( tempo );
	}
	
	public void setRelogioSimulacao( Double tempo )
	{   relogioSimulacao = ( tempo >= 0.0 ) ? tempo : 0.0;
	}

	public void atualizaRelogioSimulacao( Double tempo )
	{   relogioSimulacao = tempo;
	}

    public void incrementaRelogioSimulacao( Double incremento )
    {   System.out.printf("incremento = %f\n", incremento);   
	    relogioSimulacao += incremento;
    }	
	
	public Double getRelogioSimulacao()
	{   return relogioSimulacao;
	}
	
}	