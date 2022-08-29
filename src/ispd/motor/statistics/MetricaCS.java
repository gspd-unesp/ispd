package ispd.motor.statistics;

import java.util.ArrayList;
import java.util.List;

public class MetricaCS {
    private int idCS;
    private List<MedidasServidor> servidores;
    private int numServ;


    public MetricaCS(int cs) {
        idCS = cs;
        servidores = new ArrayList<MedidasServidor>();
        numServ = 0;
    }

    public int getCS() {
        return idCS;
    }

    public void addMedidasServidor(int id, int tipoServ) {
        MedidasServidor serv = new MedidasServidor(id, tipoServ);
        servidores.add(serv);
        numServ++;
    }

    public void addNoMedidasServidor(int id, int estado, Double tempo) {
        for (MedidasServidor temp : servidores) {
            if (temp.getID() == id) {
                temp.addNoMedidasServidor(estado, tempo);
                break;
            }
        }
    }

    public Double finalizaServProc() {
        Double aux = 0.0;
        for (MedidasServidor temp : servidores) {
            if (temp.getTipo() == 0) {
                temp.setTTSOcupadoProc();
                aux = temp.getTTSOcupadoProc();
            }
        }
        return aux;
    }

    public Double finalizaServCom() {
        Double aux = 0.0;
        for (MedidasServidor temp : servidores) {
            if (temp.getTipo() == 1) {
                temp.setTTSOcupadoCom();
                aux = temp.getTTSOcupadoCom();
            }
        }
        return aux;
    }

    public void imprimeServ() {
        for (MedidasServidor temp : servidores) {
            temp.imprimeValores();
        }
    }

    public int getNumServ() {
        return numServ;
    }

} // fim de public class MetricaCS