package ispd.motor.statistics;

public class NoTarefasFila {
    private int idTarefa;
    private Double tamanhoTarefa;
    private Double tempoChegadaFila;
    private Double tempoChegadaServidor;
    private Double tempoSaidaSistema;
    private Double tempoTotalSistema;
    private Double tempoTotalFila;
    private Double tempoTotalServidor;


    public NoTarefasFila(int id, Double tam, Double tempo) {
        setIdTarefa(id);
        this.tamanhoTarefa = tam;
        this.tempoChegadaFila = tempo;
        this.tempoChegadaServidor = 0.0;
        this.tempoSaidaSistema = 0.0;
        this.tempoTotalSistema = 0.0;
        this.tempoTotalFila = 0.0;
        this.tempoTotalServidor = 0.0;
    }

    public int getIdTarefa() {
        return idTarefa;
    }

    public void setIdTarefa(int id) {
        idTarefa = (id >= 0) ? id : 0;
    }

    public Double getTamanhoTarefa() {
        return tamanhoTarefa;
    }

    public void setTamanhoTarefa(Double tam) {
        tamanhoTarefa = (tam >= 0) ? tam : 0;
    }

    public Double getTempoChegadaFila() {
        return tempoChegadaFila;
    }

    public void setTempoChegadaFila(Double tempo) {
        tempoChegadaFila = tempo;
    }

    public Double getTempoChegadaServidor() {
        return tempoChegadaServidor;
    }

    public void setTempoChegadaServidor(Double tempo) {
        tempoChegadaServidor = tempo;
    }

    public Double getTempoSaidaSistema() {
        return tempoSaidaSistema;
    }

    public void setTempoSaidaSistema(Double tempo) {
        tempoSaidaSistema = tempo;
    }

    public void setTempoTotalSistema() {
        tempoTotalSistema = tempoSaidaSistema - tempoChegadaFila;
    }

    public Double getTempoTotalSistema() {
        return tempoTotalSistema;
    }

    public void setTempoTotalFila() {
        tempoTotalFila = tempoChegadaServidor - tempoChegadaFila;
    }

    public Double getTempoTotalFila() {
        return tempoTotalFila;
    }

    public void setTempoTotalServidor() {
        tempoTotalServidor = tempoSaidaSistema - tempoChegadaServidor;
    }

    public Double getTempoTotalServidor() {
        return tempoTotalServidor;
    }

} // fim de public class NoFila