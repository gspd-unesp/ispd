package ispd.motor.statistics;

public class NoMedidasServidor {
    private int estadoServidor;  // 0: livre, 1: ocupado;
    private Double tempoServidor;


    public NoMedidasServidor(int estado, Double tempo) {
        setEstado(estado);
        this.tempoServidor = tempo;   // Como � uma classe espec�fica
		// (Double), tem que ser manipulada assim (diretamente, sem um metodo
		// set)
    }

    public int getEstado() {
        return estadoServidor;
    }

    public void setEstado(int estado) {
        estadoServidor = (estado >= 0) ? estado : 0;
    }

    public Double getTempoServidor() {
        return tempoServidor;
    }

} // fim de public class NoServidor