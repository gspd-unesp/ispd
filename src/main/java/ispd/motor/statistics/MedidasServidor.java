package ispd.motor.statistics;

import java.util.ArrayList;
import java.util.List;

public class MedidasServidor {
    private Double tempoTotalServOcupadoProc;
    private Double tempoTotalServOcupadoCom;
    private int ID;
    private int tipo; // 0 - Processamento     1 - Comunicacao
    private List<NoMedidasServidor> servidor;


    public MedidasServidor(int ID, int tipoServ) {
        servidor = new ArrayList<NoMedidasServidor>();
        this.ID = ID;
        tipo = tipoServ;
    }

    public void addNoMedidasServidor(int estado, Double tempo) {
        NoMedidasServidor no = new NoMedidasServidor(estado, tempo);
        servidor.add(no);
    }

    public void imprimeValores() {
        System.out.printf("\nSERVIDOR ID %d DO TIPO", getID());
        if (getTipo() == 0)
            System.out.printf(" PROCESSAMENTO", getID());
        else
            System.out.printf(" COMUNICACAO", getID());
        for (NoMedidasServidor temp : servidor) {
            System.out.printf("\nServidor Estado: %d -> Tempo: %g\n",
					temp.getEstado(), temp.getTempoServidor());
        }
    }

    public int getID() {
        return ID;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTTSOcupadoProc() {
        Double aux = 0.0;
        for (NoMedidasServidor no : servidor) {
            aux += no.getTempoServidor();
        }
        tempoTotalServOcupadoProc = aux;
    }

    public Double getTTSOcupadoProc() {
        return tempoTotalServOcupadoProc;
    }

    public void setTTSOcupadoCom() {
        Double aux = 0.0;
        for (NoMedidasServidor no : servidor) {
            aux += no.getTempoServidor();
        }
        tempoTotalServOcupadoCom = aux;
    }

    public Double getTTSOcupadoCom() {
        return tempoTotalServOcupadoCom;
    }

} // fim de public class Servidores