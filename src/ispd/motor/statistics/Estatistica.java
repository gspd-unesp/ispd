package ispd.motor.statistics;

import java.util.ArrayList;
import java.util.List;

public class Estatistica {
    private double tempoInicial;
    private double tempoFinal;
    private double tempoTotal;
    private double satisfacao;
    private double ociosidade;
    private double eficiencia;
    private int numServidores;
    private int numCS;
    private int numTarefasTotal;
    private int numUsuarios;
    private List<MetricaCS> metCS;
    private List<TarefasFila> filas;
    private List<String> listaNomeId;

    public Estatistica(double tempoInicial) {
        tempoInicial = tempoInicial;
        tempoFinal = 0.0;
        tempoTotal = 0.0;
        satisfacao = 0.0;
        ociosidade = 0.0;
        eficiencia = 0.0;
        numServidores = 0;
        numUsuarios = 1;
        numCS = 0;
        metCS = new ArrayList<MetricaCS>();
        filas = new ArrayList<TarefasFila>();
    }

    public void addMetricaCS(int cs) {
        MetricaCS CS = new MetricaCS(cs);
        metCS.add(CS);
        numCS++;
    }

    public void imprimeRede() {
        for (MetricaCS temp : metCS) {
            temp.imprimeServ();
        }
        for (TarefasFila temp : filas) {
            temp.imprimeTarefas();
        }
    }

    public void imprimeTarefa() {
        for (TarefasFila temp : filas) {
            temp.imprimeTar();
        }
    }

    public MetricaCS descobreServidorCS(int cs) {
        MetricaCS retorno = null;
        for (MetricaCS temp : metCS) {
            if (temp.getCS() == cs) {
                return temp;
            }
        }
        return retorno;
    }

    public void addMedidasServidor(int cs, int id, int tipoServ) {
        for (MetricaCS temp : metCS) {
            if (temp.getCS() == cs) {
                temp.addMedidasServidor(id, tipoServ);
                break;
            }
        }
    }

    public void addNoMedidasServidor(int cs, int id, int estado, Double tempo) {
        for (MetricaCS temp : metCS) {
            if (temp.getCS() == cs) {
                temp.addNoMedidasServidor(id, estado, tempo);
                break;
            }
        }
    }

    public void addTarefasFila(int tipo) {
        TarefasFila fila = new TarefasFila(tipo);
        filas.add(fila);
    }

    public void addNoTarefasFila(int tipo, int tarefa, Double tam,
								 Double tempo) {
        for (TarefasFila temp : filas) {
            if (temp.getTipoMetrica() == tipo) {
                temp.addNoTarefasFila(tarefa, tam, tempo);
                break;
            }
        }
    }

    public void setTempoChegadaServidor(int tipo, int idTarefa, Double tempo) {
        for (TarefasFila no : filas) {
            if (no.getTipoMetrica() == tipo) {
                no.setTempoChegadaServidor(idTarefa, tempo);
            }
        }
    }

    public void setTempoSaidaSistema(int tipo, int idTarefa, Double tempo) {
        for (TarefasFila no : filas) {
            if (no.getTipoMetrica() == tipo) {
                no.setTempoSaidaSistema(idTarefa, tempo);
            }
        }
    }

    public void fimSimulacao() {
        Double aux = 0.0;
        String texto = "\t\tSimulation Results\n\n";
        setTempoTotal();
        texto += String.format("Total Simulated Time = %g \n", getTempoTotal());
        setSatisfacao();
        texto += String.format("Satisfaction = %g %%\n", getSatisfacao());
        for (MetricaCS temp : metCS) {
            aux = temp.finalizaServProc();
            if (aux > 0.0) {
                texto += this.listaNomeId.get(temp.getCS()) + String.format(
						" spent %g second processing.\n", aux);
            }
            aux = temp.finalizaServCom();
            if (aux > 0.0) {
                texto += this.listaNomeId.get(temp.getCS()) + String.format(
						" spent %g second processing.\n", aux);
            }
        }
        setOciosidade();
        texto += String.format("Total Idleness = %g %%\n", getOciosidade());
        texto += String.format("Efficiency = %g %%\n", getEficiencia());
        if (getEficiencia() > 70.0) {
            texto += "Efficiency GOOD\n ";
        } else if (getEficiencia() > 40.0) {
            texto += "Efficiency MEDIA\n ";
        } else {
            texto += "Efficiency BAD\n ";
        }
        texto += "\n\n\t\tTASKS\n ";
        for (TarefasFila no : filas) {
            if (no.getTipoMetrica() == 0) {
                texto += "\n Communication \n";
                texto += String.format("    Queue average time: %g seconds" +
									   ".\n", no.getTempoMedioFila());
                texto += String.format("    Communication average time: %g " +
									   "seconds.\n",
						no.getTempoMedioServidor());
                texto += String.format("    System average time: %g seconds" +
									   ".\n", no.getTempoMedioSistema());
            } else {
                texto += "\n Processing \n";
                texto += String.format("    Queue average time: %g seconds" +
									   ".\n", no.getTempoMedioFila());
                texto += String.format("    Processing average time: %g " +
									   "seconds.\n",
						no.getTempoMedioServidor());
                texto += String.format("    System average time: %g seconds" +
									   ".\n", no.getTempoMedioSistema());
            }
        }
        CaixaTextoEstatistica janela = new CaixaTextoEstatistica("Metrics",
				texto);
        janela.setVisible(true);
    }

    public void setTempoTotal() {
        tempoTotal = tempoFinal - tempoInicial;
    }

    public double getTempoTotal() {
        return tempoTotal;
    }

    public void setSatisfacao() {
        satisfacao = 100.0;
    }

    public double getSatisfacao() {
        return satisfacao;
    }

    public void setOciosidade() {
        double aux = 0.0;
        double tempoLivre = 0.0;
        double razao = 0.0;
        double ociosidadeProc = 0.0;
        double ociosidadeCom = 0.0;
        for (MetricaCS temp : metCS) {
            aux = temp.finalizaServProc();
            if (aux > 0.0) {
                tempoLivre = (getTempoTotal() - aux);
                ociosidadeProc =
						ociosidadeProc + (tempoLivre / getTempoTotal());
            }
            aux = temp.finalizaServCom();
            if (aux > 0.0) {
                tempoLivre = (getTempoTotal() - aux);
                ociosidadeCom = ociosidadeCom + (tempoLivre / getTempoTotal());
            }
        }
        ociosidade = (ociosidadeProc + ociosidadeCom) / 2;
        ociosidade = 100 - ociosidade;
    }

    public double getOciosidade() {
        return ociosidade;
    }

    public double getEficiencia() {
        return eficiencia;
    }

    public double getTempoInicial() {
        return tempoInicial;
    }

    public void setTempoInicial(double tempoIni) {
        tempoInicial = (tempoIni >= 0.0) ? tempoIni : 0.0;
    }

    public double getTempoFinal() {
        return tempoFinal;
    }

    public void setTempoFinal(double tempoFin) {
        tempoFinal = (tempoFin >= 0.0) ? tempoFin : 0.0;
    }

    public void setEficiencia(double valor, double tempo) {
        double mediaTotal = 0.0;
        double capRecebida = 0.0;
        capRecebida = valor;
        for (TarefasFila no : filas) {
            if (no.getTipoMetrica() == 1) {
                numTarefasTotal = no.contaTarefa();
            }
        }
        System.out.printf("VALOR %f\n", valor);
        System.out.printf("TEMPO %f\n", tempo);
        eficiencia += valor / (capRecebida * tempo);
        System.out.printf("NUMERO DE TAREFAS %d\n", numTarefasTotal);
        eficiencia = eficiencia / numTarefasTotal;
        System.out.printf("Eficiencia %g%%\n", eficiencia);
        //System.out.printf("MEDIA DAS TAREFAS NORMALIZADAS %g\n",mediaTotal);
    }

    public void setNomeId(List<String> nomes) {
        this.listaNomeId = nomes;
    }


} // fim de public class Estatistica