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
 * Graficos.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 14-Out-2014 : Create class. Version 2.0.1;
 *
 */
package ispd.gui.auxiliar;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.metricas.MetricasComunicacao;
import ispd.motor.metricas.MetricasProcessamento;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYStepAreaRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Classe criada para separear a criação e controle dos gráficos da janela de
 * exibição dos resultados da simulação
 *
 * @author denison
 */
public class Graficos {

    public ChartPanel ProcessingBarChart;
    public ChartPanel CommunicationBarChart;
    public ChartPanel ProcessingPieChart;
    public ChartPanel CommunicationPieChart;
    public ChartPanel UserThroughTimeChart1;
    public ChartPanel UserThroughTimeChart2;
    public ChartPanel MachineThroughTimeChart;
    public ChartPanel TaskThroughTimeChart;
    public ChartPanel PreemptionPerUser;
    private double poderComputacionalTotal = 0;
    public RedeDeFilas rede;

    public ChartPanel getProcessingBarChart() {
        return ProcessingBarChart;
    }

    public ChartPanel getCommunicationBarChart() {
        return CommunicationBarChart;
    }

    public ChartPanel getProcessingPieChart() {
        return ProcessingPieChart;
    }

    public ChartPanel getCommunicationPieChart() {
        return CommunicationPieChart;
    }

    public ChartPanel getUserThroughTimeChart1() {
        return UserThroughTimeChart1;
    }

    public ChartPanel getUserThroughTimeChart2() {
        return UserThroughTimeChart2;
    }

    public ChartPanel getMachineThroughTimeChart() {
        return MachineThroughTimeChart;
    }

    public ChartPanel getTaskThroughTimeChart() {
        return TaskThroughTimeChart;
    }

    public void criarProcessamento(Map<String, MetricasProcessamento> mProcess) {
        DefaultCategoryDataset dadosGraficoProcessamento = new DefaultCategoryDataset();
        DefaultPieDataset dadosGraficoPizzaProcessamento = new DefaultPieDataset();

        if (mProcess != null) {
            for (Map.Entry<String, MetricasProcessamento> entry : mProcess.entrySet()) {
                MetricasProcessamento mt = entry.getValue();
                if (mt.getnumeroMaquina() == 0) {
                    dadosGraficoProcessamento.addValue(mt.getMFlopsProcessados(), "vermelho", mt.getId());
                    dadosGraficoPizzaProcessamento.insertValue(0, mt.getId(), mt.getMFlopsProcessados());
                } else {
                    dadosGraficoProcessamento.addValue(mt.getMFlopsProcessados(), "vermelho", mt.getId() + " node " + mt.getnumeroMaquina());
                    dadosGraficoPizzaProcessamento.insertValue(0, mt.getId() + " node " + mt.getnumeroMaquina(), mt.getMFlopsProcessados());
                }
            }
        }

        JFreeChart jfc = ChartFactory.createBarChart(
                "Total processed on each resource", //Titulo
                "Resource", // Eixo X
                "Mflops", //Eixo Y
                dadosGraficoProcessamento, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                false, false, false); // exibir: legendas, tooltips, url
        //Inclina nome da barra em 45 graus
        if (mProcess != null && mProcess.size() > 10) {
            jfc.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }
        ProcessingBarChart = new ChartPanel(jfc);
        ProcessingBarChart.setPreferredSize(new Dimension(600, 300));

        jfc = ChartFactory.createPieChart(
                "Total processed on each resource", //Titulo
                dadosGraficoPizzaProcessamento, // Dados para o grafico
                true, false, false);
        ProcessingPieChart = new ChartPanel(jfc);
        ProcessingPieChart.setPreferredSize(new Dimension(600, 300));

    }

    public void criarComunicacao(Map<String, MetricasComunicacao> mComunicacao) {
        DefaultCategoryDataset dadosGraficoComunicacao = new DefaultCategoryDataset();
        DefaultPieDataset dadosGraficoPizzaComunicacao = new DefaultPieDataset();

        if (mComunicacao != null) {
            for (Map.Entry<String, MetricasComunicacao> entry : mComunicacao.entrySet()) {
                MetricasComunicacao link = entry.getValue();
                dadosGraficoComunicacao.addValue(link.getMbitsTransmitidos(), "vermelho", link.getId());
                dadosGraficoPizzaComunicacao.insertValue(0, link.getId(), link.getMbitsTransmitidos());
            }
        }

        JFreeChart jfc = ChartFactory.createBarChart(
                "Total communication in each resource", //Titulo
                "Resource", // Eixo X
                "Mbits", //Eixo Y
                dadosGraficoComunicacao, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                false, false, false); // exibir: legendas, tooltips, url
        //Inclina nome da barra em 45 graus
        if (mComunicacao != null && mComunicacao.size() > 10) {
            jfc.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }
        CommunicationBarChart = new ChartPanel(jfc);
        CommunicationBarChart.setPreferredSize(new Dimension(600, 300));

        jfc = ChartFactory.createPieChart(
                "Total communication in each resource", //Titulo
                dadosGraficoPizzaComunicacao, // Dados para o grafico
                true, false, false);
        CommunicationPieChart = new ChartPanel(jfc);
        CommunicationPieChart.setPreferredSize(new Dimension(600, 300));
    }

    public void criarProcessamentoTempoTarefa(List<Tarefa> tarefas) {

        XYSeriesCollection dadosGrafico = new XYSeriesCollection();
        if (!tarefas.isEmpty()) {
            for (Tarefa task : tarefas) {
                XYSeries tmp_series;
                tmp_series = new XYSeries("task " + task.getIdentificador());
                CS_Processamento temp = (CS_Processamento) task.getLocalProcessamento();
                if (temp != null) {
                    Double uso = (temp.getPoderComputacional() / this.poderComputacionalTotal) * 100;
                    for (int j = 0; j < task.getTempoInicial().size(); j++) {
                        tmp_series.add(task.getTempoInicial().get(j), (Double) 0.0);
                        tmp_series.add(task.getTempoInicial().get(j), uso);
                        tmp_series.add(task.getTempoFinal().get(j), uso);
                        tmp_series.add(task.getTempoFinal().get(j), (Double) 0.0);
                    }
                    dadosGrafico.addSeries(tmp_series);
                }
            }

        }

        JFreeChart jfc = ChartFactory.createXYAreaChart(
                "Use of total computing power through time "
                + "\nTasks", //Titulo
                "Time (seconds)", // Eixo X
                "Rate of total use of computing power (%)", //Eixo Y
                dadosGrafico, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false); // exibir: legendas, tooltips, url
        TaskThroughTimeChart = new ChartPanel(jfc);
    }

    //Cria o gráfico que demonstra o uso de cada recurso do sistema através do tempo.
    //Ele recebe como parâmetro a lista com as maquinas que processaram durante a simulação.
    public void criarProcessamentoTempoMaquina(RedeDeFilas rdf) {
        XYSeriesCollection dadosGrafico = new XYSeriesCollection();
        //Se tiver alguma máquina na lista.
        if (rdf.getMaquinas() != null) {
            //Laço foreach que percorre as máquinas.
            for (CS_Processamento maq : rdf.getMaquinas()) {
                //Lista que recebe os pares de intervalo de tempo em que a máquina executou.
                List<ParesOrdenadosUso> lista = maq.getListaProcessamento();
                poderComputacionalTotal += (maq.getPoderComputacional() - (maq.getOcupacao() * maq.getPoderComputacional()));
                //Se a máquina tiver intervalos.
                if (!lista.isEmpty()) {
                    //Cria o objeto do tipo XYSeries.
                    XYSeries tmp_series;
                    //Se o atributo numeroMaquina for 0, ou seja, não for um nó de um cluster.
                    if (maq.getnumeroMaquina() == 0) //Estancia com o nome puro.
                    {
                        tmp_series = new XYSeries(maq.getId());
                    } //Se for 1 ou mais, ou seja, é um nó de cluster.
                    else //Estancia tmp_series com o nome concatenado com a palavra node e seu numero.
                    {
                        tmp_series = new XYSeries(maq.getId() + " node " + maq.getnumeroMaquina());
                    }
                    int i;
                    //Laço que vai adicionando os pontos para a criação do gráfico.
                    for (i = 0; i < lista.size(); i++) {
                        //Calcula o uso, que é 100% - taxa de ocupação inicial.
                        Double uso = 100 - (maq.getOcupacao() * 100);
                        //Adiciona ponto inicial.
                        tmp_series.add(lista.get(i).getInicio(), uso);
                        //Adiciona ponto final.
                        tmp_series.add(lista.get(i).getFim(), uso);
                        if (i + 1 != lista.size()) {
                            uso = 0.0000;
                            tmp_series.add(lista.get(i).getFim(), uso);
                            tmp_series.add(lista.get(i + 1).getInicio(), uso);
                        }
                    }
                    //Add no gráfico.
                    dadosGrafico.addSeries(tmp_series);
                }
            }
        }

        JFreeChart jfc = ChartFactory.createXYAreaChart(
                "Use of computing power through time "
                + "\nMachines", //Titulo
                "Time (seconds)", // Eixo X
                "Rate of use of computing power for each node (%)", //Eixo Y
                dadosGrafico, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false); // exibir: legendas, tooltips, url
        MachineThroughTimeChart = new ChartPanel(jfc);
        MachineThroughTimeChart.setPreferredSize(new Dimension(600, 300));
    }

    public void criarProcessamentoTempoUser(List<Tarefa> tarefas, RedeDeFilas rdf) {
        ArrayList<tempo_uso_usuario> lista = new ArrayList<tempo_uso_usuario>();
        int numberUsers = rdf.getUsuarios().size();
        Map<String, Integer> users = new HashMap<String, Integer>();
        XYSeries tmp_series[] = new XYSeries[numberUsers];
        XYSeries tmp_series1[] = new XYSeries[numberUsers];
        Double utilizacaoUser[] = new Double[numberUsers];
        Double utilizacaoUser1[] = new Double[numberUsers];
        XYSeriesCollection dadosGrafico = new XYSeriesCollection();
        XYSeriesCollection dadosGrafico1 = new XYSeriesCollection();
        for (int i = 0; i < numberUsers; i++) {
            users.put(rdf.getUsuarios().get(i), i);
            utilizacaoUser[i] = 0.0;
            tmp_series[i] = new XYSeries(rdf.getUsuarios().get(i));
            utilizacaoUser1[i] = 0.0;
            tmp_series1[i] = new XYSeries(rdf.getUsuarios().get(i));
        }
        if (!tarefas.isEmpty()) {
            //Insere cada tarefa como dois pontos na lista
            for (Tarefa task : tarefas) {
                CS_Processamento local = (CS_Processamento) task.getLocalProcessamento();
                if (local != null) {

                    for (int i = 0; i < task.getTempoInicial().size(); i++) {
                        Double uso = (task.getHistoricoProcessamento().get(i).getPoderComputacional() / poderComputacionalTotal) * 100;
                        tempo_uso_usuario provisorio1 = new tempo_uso_usuario(task.getTempoInicial().get(i), true, uso, users.get(task.getProprietario()));
                        lista.add(provisorio1);
                        tempo_uso_usuario provisorio2 = new tempo_uso_usuario(task.getTempoFinal().get(i), false, uso, users.get(task.getProprietario()));
                        lista.add(provisorio2);
                    }
                }
            }
            //Ordena lista
            Collections.sort(lista);
        }
        for (int i = 0; i < lista.size(); i++) {
            tempo_uso_usuario temp = (tempo_uso_usuario) lista.get(i);
            int usuario = temp.get_user();
            //Altera os valores do usuario atual e todos acima dele
            for (int j = usuario; j < numberUsers; j++) {
                //Salva valores anteriores
                tmp_series[j].add(temp.get_tempo(), utilizacaoUser[j]);
                if (temp.get_tipo()) {
                    utilizacaoUser[j] += temp.get_uso_no();
                } else {
                    utilizacaoUser[j] -= temp.get_uso_no();
                }
                //Novo valor
                tmp_series[j].add(temp.get_tempo(), utilizacaoUser[j]);
            }
            //Grafico1
            tmp_series1[usuario].add(temp.get_tempo(), utilizacaoUser1[usuario]);
            if (temp.get_tipo()) {
                utilizacaoUser1[usuario] += temp.get_uso_no();
            } else {
                utilizacaoUser1[usuario] -= temp.get_uso_no();
            }
            tmp_series1[usuario].add(temp.get_tempo(), utilizacaoUser1[usuario]);
        }
        for (int i = 0; i < numberUsers; i++) {
            dadosGrafico.addSeries(tmp_series[i]);
            dadosGrafico1.addSeries(tmp_series1[i]);
        }
        JFreeChart user1 = ChartFactory.createXYAreaChart(
                "Use of total computing power through time"
                + "\nUsers", //Titulo
                "Time (seconds)", // Eixo X
                "Rate of total use of computing power (%)", //Eixo Y
                dadosGrafico1, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false); // exibir: legendas, tooltips, url

        JFreeChart user2 = ChartFactory.createXYLineChart(
                "Use of total computing power through time"
                + "\nUsers", //Titulo
                "Time (seconds)", // Eixo X
                "Rate of total use of computing power (%)", //Eixo Y
                dadosGrafico, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false); // exibir: legendas, tooltips, url
        XYPlot xyplot = (XYPlot) user2.getPlot();
        xyplot.setDomainPannable(true);
        XYStepAreaRenderer xysteparearenderer = new XYStepAreaRenderer(2);
        xysteparearenderer.setDataBoundsIncludesVisibleSeriesOnly(false);
        xysteparearenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        xysteparearenderer.setDefaultEntityRadius(6);
        xyplot.setRenderer(xysteparearenderer);

        UserThroughTimeChart1 = new ChartPanel(user1);
        UserThroughTimeChart1.setPreferredSize(new Dimension(600, 300));
        UserThroughTimeChart2 = new ChartPanel(user2);
        UserThroughTimeChart2.setPreferredSize(new Dimension(600, 300));
    }

    public ChartPanel criarGraficoPorTarefa(List<Tarefa> tarefas, int idTarefa) {
        DefaultCategoryDataset dadosMflopProcessados = new DefaultCategoryDataset();
        Tarefa job = null;
        int i;
        Double mflopProcessadoTotal = 0.0;

        for (i = 0; i < tarefas.size(); i++) {
            if (tarefas.get(i).getIdentificador() == idTarefa) {
                job = tarefas.get(i);
                break;
            }
        }

        if (job != null) {

            if (job.getEstado() != Tarefa.CANCELADO) {

                for (i = 0; i < job.getHistoricoProcessamento().size(); i++) {

                    mflopProcessadoTotal += job.getHistoricoProcessamento().get(i).getMflopsProcessados(job.getTempoFinal().get(i) - job.getTempoInicial().get(i));

                }

                dadosMflopProcessados.addValue(((job.getTamProcessamento() / mflopProcessadoTotal) * 100.0), "Usefull processing", "Task size :" + job.getTamProcessamento() + " MFlop" + ", total executed for task: " + mflopProcessadoTotal + " MFlop");
                dadosMflopProcessados.addValue(((job.getMflopsDesperdicados()) / mflopProcessadoTotal) * 100.0, "Wasted processing", "Task size :" + job.getTamProcessamento() + " MFlop" + ", total executed for task: " + mflopProcessadoTotal + " MFlop");

                JFreeChart jfc = ChartFactory.createStackedBarChart(
                        "MFlop usage for task " + idTarefa, //Titulo
                        "", // Eixo X
                        "% of total MFlop executed for the task", //Eixo Y
                        dadosMflopProcessados, // Dados para o grafico
                        PlotOrientation.VERTICAL, //Orientacao do grafico
                        true, true, false); // exibir: legendas, tooltips, url
                ChartPanel graficoPorTarefa = new ChartPanel(jfc);
                graficoPorTarefa.setPreferredSize(new Dimension(600, 300));
                return graficoPorTarefa;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public ChartPanel criarGraficoAproveitamento(List<Tarefa> tarefas) {

        DefaultCategoryDataset dadosMflopProcessados = new DefaultCategoryDataset();
        double mflopDesperdicado= 0.0, tamanhoTotal = 0.0;
        int i, j;

        for (i = 0; i < tarefas.size(); i++) {

            if (tarefas.get(i).getEstado() != Tarefa.CANCELADO) {

                tamanhoTotal += tarefas.get(i).getTamProcessamento();
                
            }

            mflopDesperdicado += tarefas.get(i).getMflopsDesperdicados();

        }

        dadosMflopProcessados.addValue((tamanhoTotal / (mflopDesperdicado + tamanhoTotal)) * 100.0, "Usefull Processing", "MFlop Usage");
        dadosMflopProcessados.addValue((mflopDesperdicado / (mflopDesperdicado + tamanhoTotal)) * 100.0, "Wasted Processing", "MFlop Usage");

        JFreeChart jfc = ChartFactory.createStackedBarChart(
                "Processing efficiency", //TituloUsage#
                "", // Eixo X
                "% of total MFlop executed", //Eixo Y
                dadosMflopProcessados, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false); // exibir: legendas, tooltips, url
        ChartPanel graficoAproveitamentoPorcentagem = new ChartPanel(jfc);
        graficoAproveitamentoPorcentagem.setPreferredSize(new Dimension(600, 300));
        return graficoAproveitamentoPorcentagem;
    }

    public ChartPanel criarGraficoNumTarefasAproveitamento(List<Tarefa> tarefas) {

        DefaultCategoryDataset dadosMflopProcessados = new DefaultCategoryDataset();
        int numExcesso = 0, numOK = 0, numCanceladas = 0;
        int i;

        for (i = 0; i < tarefas.size(); i++) {

            if (tarefas.get(i).getEstado() != Tarefa.CANCELADO) {
                if (tarefas.get(i).getMflopsDesperdicados() != 0.0) {
                    numExcesso++;
                } else {
                    numOK++;
                }
            } else {
                numCanceladas++;
            }

        }
        
        dadosMflopProcessados.addValue(numExcesso, "Number of tasks", "Tasks with waste");
        dadosMflopProcessados.addValue(numOK, "Number of tasks", "Tasks without waste");
        dadosMflopProcessados.addValue(numCanceladas, "Number of tasks", "Canceled Tasks");
        

        JFreeChart jfc = ChartFactory.createStackedBarChart(
                "Processing efficiency", //Titulo
                "", // Eixo X
                "Number of tasks", //Eixo Y
                dadosMflopProcessados, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false); // exibir: legendas, tooltips, url
        ChartPanel graficoAproveitamentoNumero = new ChartPanel(jfc);
        graficoAproveitamentoNumero.setPreferredSize(new Dimension(600, 300));
        return graficoAproveitamentoNumero;

    }

    public void criarGraficoPreempcao(RedeDeFilas rdf, List<Tarefa> tarefas) {

        DefaultCategoryDataset preempPorUsuario = new DefaultCategoryDataset();
        double mflopTotal = 0.0, tamanhoTotal;
        ArrayList<Integer> tarefasPreemp;
        tarefasPreemp = new ArrayList();
        int i, j, indexUsuario;

        for (i = 0; i < rdf.getUsuarios().size(); i++) {

            tarefasPreemp.add(0);

        }

        for (i = 0; i < tarefas.size(); i++) {

            indexUsuario = rdf.getUsuarios().indexOf(tarefas.get(i).getProprietario());

            if (tarefas.get(i).getMflopsDesperdicados() > 0.0 && tarefas.get(i).getEstado() != Tarefa.CANCELADO) {
                tarefasPreemp.set(indexUsuario, 1 + tarefasPreemp.get(indexUsuario));
            }

            mflopTotal = 0.0;

        }

        for (i = 0; i < rdf.getUsuarios().size(); i++) {

            preempPorUsuario.addValue(tarefasPreemp.get(i), "Number of tasks", rdf.getUsuarios().get(i));

        }

        JFreeChart jfc = ChartFactory.createStackedBarChart(
                "Tasks preempted per user", //Titulo
                "", // Eixo X
                "Number of tasks", //Eixo Y
                preempPorUsuario, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false); // exibir: legendas, tooltips, url
        PreemptionPerUser = new ChartPanel(jfc);
        PreemptionPerUser.setPreferredSize(new Dimension(600, 300));
    }

    public ChartPanel gerarGraficoPorMaquina(List<Tarefa> tarefas, String maq) {
        DefaultCategoryDataset dadosMflopProcessados = new DefaultCategoryDataset();
        int i, j, histIndex = -1;
        CS_Maquina alvo = null;
        Tarefa task = null;
        double mflopUsado = 0.0;
        double mflopPerdido = 0.0;
        Double tempo;

        for (i = 0; i < rede.getMaquinas().size(); i++) {
            if (rede.getMaquinas().get(i).getId().equals(maq)) {
                alvo = rede.getMaquinas().get(i);
                break;
            }
        }

        if (alvo != null) {

            for( i = 0 ; i < alvo.getHistorico().size() ; i++ ){
                
                if( alvo.getHistorico().get(i).getMflopsDesperdicados() > 0.0 ){
                    
                    for( j = 0; j < alvo.getHistorico().get(i).getHistoricoProcessamento().size() ; j++){
                        
                        if (alvo.getHistorico().get(i).getHistoricoProcessamento().get(j).getId().equals(alvo.getId())) {

                            tempo = alvo.getHistorico().get(i).getTempoFinal().get(j) - alvo.getHistorico().get(i).getTempoInicial().get(j);
                            if (alvo.getHistorico().get(i).getCheckPoint() != 0.0) {
                                mflopUsado += alvo.getMflopsProcessados(tempo) / alvo.getHistorico().get(i).getCheckPoint() - alvo.getMflopsProcessados(tempo) % alvo.getHistorico().get(i).getCheckPoint();
                                mflopPerdido += alvo.getMflopsProcessados(tempo) % alvo.getHistorico().get(i).getCheckPoint();
                            }
                            else{
                                mflopPerdido += alvo.getMflopsProcessados(tempo);
                            }

                        }

                    }
                    
                }
                else {

                    for (j = 0; j < alvo.getHistorico().get(i).getHistoricoProcessamento().size(); j++) {

                        if (alvo.getHistorico().get(i).getHistoricoProcessamento().get(j).getId().equals(alvo.getId())) {

                            tempo = alvo.getHistorico().get(i).getTempoFinal().get(j) - alvo.getHistorico().get(i).getTempoInicial().get(j);
                            mflopUsado += alvo.getMflopsProcessados(tempo);

                        }

                    }

                }
                  
            }

            dadosMflopProcessados.addValue((mflopUsado / (mflopPerdido + mflopUsado)) * 100.0, "Usefull Processing", "MFlop Usage");
            dadosMflopProcessados.addValue((mflopPerdido / (mflopPerdido + mflopUsado)) * 100.0, "Wasted Processing", "MFlop Usage");

            JFreeChart jfc = ChartFactory.createStackedBarChart(
                    "Processing efficiency for resource " + alvo.getId(), //Titulo
                    "", // Eixo X
                    "% of total MFlop executed", //Eixo Y
                    dadosMflopProcessados, // Dados para o grafico
                    PlotOrientation.VERTICAL, //Orientacao do grafico
                    true, true, false); // exibir: legendas, tooltips, url
            ChartPanel graficoAproveitamentoMaquina = new ChartPanel(jfc);
            graficoAproveitamentoMaquina.setPreferredSize(new Dimension(600, 300));
            return graficoAproveitamentoMaquina;
        }

        return null;
    }


    public void calculaPoderTotal(RedeDeFilas rdf) {
        for (CS_Processamento maq : rdf.getMaquinas()) {
            poderComputacionalTotal += (maq.getPoderComputacional() - (maq.getOcupacao() * maq.getPoderComputacional()));
        }
    }

    protected class tempo_uso_usuario implements Comparable<tempo_uso_usuario> {

        private Double tempo;
        private Double uso_no;
        private Boolean tipo;
        private Integer user;

        private tempo_uso_usuario(double tempo, boolean tipo, Double uso, Integer user) {
            this.user = user;
            this.tempo = tempo;
            this.uso_no = uso;
            this.tipo = tipo;
        }

        public Double get_tempo() {
            return this.tempo;
        }

        public Integer get_user() {
            return this.user;
        }

        public Boolean get_tipo() {
            return this.tipo;
        }

        public Double get_uso_no() {
            return this.uso_no;
        }

        @Override
        public int compareTo(tempo_uso_usuario o) {
            return tempo.compareTo(o.tempo);
        }
    }

    //graficos experimentais...
    private JFreeChart criarGraficoEstadoTarefa2(List<Tarefa> tarefas, RedeDeFilas rdf) {
        DefaultCategoryDataset dados = new DefaultCategoryDataset();
        for (CS_Processamento maq : rdf.getMaquinas()) {
            dados.addValue(0, "Canceled", maq.getId());
            dados.addValue(0, "Completed", maq.getId());
            dados.addValue(0, "Not executed", maq.getId());
            dados.addValue(0, "Failures", maq.getId());
        }
        for (CS_Processamento maq : rdf.getMestres()) {
            if (maq instanceof CS_Mestre) {
                dados.addValue(0, "Canceled", maq.getId());
                dados.addValue(0, "Completed", maq.getId());
                dados.addValue(0, "Not executed", maq.getId());
                dados.addValue(0, "Failures", maq.getId());
            }
        }
        for (Tarefa tarefa : tarefas) {
            Double val;
            switch (tarefa.getEstado()) {
                case Tarefa.PARADO:
                    val = (Double) dados.getValue("Not executed", tarefa.getOrigem().getId());
                    dados.setValue(val + 1, "Not executed", tarefa.getOrigem().getId());
                    break;
                case Tarefa.CONCLUIDO:
                    val = (Double) dados.getValue("Completed", tarefa.getLocalProcessamento().getId());
                    dados.setValue(val + 1, "Completed", tarefa.getLocalProcessamento().getId());
                    break;
                case Tarefa.CANCELADO:
                    val = (Double) dados.getValue("Canceled", tarefa.getLocalProcessamento().getId());
                    dados.setValue(val + 1, "Canceled", tarefa.getLocalProcessamento().getId());
                    break;
                case Tarefa.FALHA:
                    val = (Double) dados.getValue("Failures", tarefa.getLocalProcessamento().getId());
                    dados.setValue(val + 1, "Failures", tarefa.getLocalProcessamento().getId());
                    break;
            }
        }
        JFreeChart jfc = ChartFactory.createBarChart(
                "State of tasks per resource", //Titulo
                "Resource", // Eixo X
                "Numbers of tasks", //Eixo Y
                dados, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, false, false); // exibir: legendas, tooltips, url
        return jfc;
    }

    private JFreeChart criarGraficoEstadoTarefa(List<Tarefa> tarefas) {
        XYSeries canceladas = new XYSeries("Canceled");
        XYSeries concluidas = new XYSeries("Completed");
        XYSeries paradas = new XYSeries("Not executed");
        XYSeries falhas = new XYSeries("Failures");
        for (Tarefa tarefa : tarefas) {
            switch (tarefa.getEstado()) {
                case Tarefa.PARADO:
                    paradas.add(tarefa.getTimeCriacao(), 4);
                    break;
                case Tarefa.CONCLUIDO:
                    concluidas.add(
                            tarefa.getTempoFinal().get(tarefa.getTempoFinal().size() - 1), (Double) 1.0);
                    break;
                case Tarefa.CANCELADO:
                    if (!tarefa.getTempoFinal().isEmpty()) {
                        canceladas.add(tarefa.getTempoFinal().get(tarefa.getTempoFinal().size() - 1), (Double) 2.0);
                    }
                    break;
                case Tarefa.FALHA:
                    falhas.add(tarefa.getTempoFinal().get(tarefa.getTempoFinal().size() - 1), (Double) 3.0);
                    break;
            }
        }
        XYSeriesCollection data = new XYSeriesCollection();
        data.addSeries(falhas);
        data.addSeries(canceladas);
        data.addSeries(concluidas);
        data.addSeries(paradas);
        JFreeChart chart = ChartFactory.createScatterPlot(
                "State of tasks",
                "Time (seconds)",
                "Y", data,
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false);
        NumberAxis domainAxis = (NumberAxis) chart.getXYPlot().getDomainAxis();
        domainAxis.setAutoRangeIncludesZero(false);
        return chart;
    }
}
