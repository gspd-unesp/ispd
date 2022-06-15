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
 * Graficos.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e
 * Distribuídos da Unesp (GSPD).
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
import ispd.motor.metricas.MetricasComunicacao;
import ispd.motor.metricas.MetricasProcessamento;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYStepAreaRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe criada para separear a criação e controle dos gráficos da janela de
 * exibição dos resultados da simulação
 */
public class Graficos {

    private static final Dimension PREFERRED_CHART_SIZE
            = new Dimension(600, 300);
    public ChartPanel PreemptionPerUser = null;
    public RedeDeFilas rede = null;
    private ChartPanel processingBarChart = null;
    private ChartPanel processingPieChart = null;
    private ChartPanel communicationPieChart = null;
    private ChartPanel userThroughTime1 = null;
    private ChartPanel UserThroughTime2 = null;
    private ChartPanel machineThroughTime = null;
    private ChartPanel TaskThroughTime = null;
    private double poderComputacionalTotal = 0;

    public ChartPanel getProcessingBarChart() {
        return this.processingBarChart;
    }

    public ChartPanel getCommunicationBarChart() {
        return null;
    }

    public ChartPanel getProcessingPieChart() {
        return this.processingPieChart;
    }

    public ChartPanel getCommunicationPieChart() {
        return this.communicationPieChart;
    }

    public ChartPanel getUserThroughTimeChart1() {
        return this.userThroughTime1;
    }

    public ChartPanel getUserThroughTimeChart2() {
        return this.UserThroughTime2;
    }

    public ChartPanel getMachineThroughTimeChart() {
        return this.machineThroughTime;
    }

    public ChartPanel getTaskThroughTimeChart() {
        return this.TaskThroughTime;
    }

    public void criarProcessamento(
            final Map<String, ? extends MetricasProcessamento> metrics) {

        this.processingBarChart = this.makeBarChart(metrics);
        final var pieChartData = this.makePieChartData(metrics);

        this.processingPieChart = Graficos.makePieChart(pieChartData);
    }

    private ChartPanel makeBarChart(
            final Map<String, ? extends MetricasProcessamento> metrics) {
        final var chart = ChartFactory.createBarChart(
                "Total processed on each " +
                        "resource",
                "Resource",
                "Mflops",
                this.makeBarChartData(metrics),
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );

        if (Graficos.shouldInclineXAxis(metrics)) {
            Graficos.inclineChartXAxis(chart);
        }

        final var panel = new ChartPanel(chart);
        panel.setPreferredSize(Graficos.PREFERRED_CHART_SIZE);
        return panel;
    }

    private DefaultPieDataset makePieChartData(
            final Map<String, ? extends MetricasProcessamento> metrics) {
        final var data = new DefaultPieDataset();
        if (metrics == null) {
            return data;
        }
        metrics.values().forEach(v -> data.insertValue(0, Graficos.makeKey(v),
                v.getMFlopsProcessados()));
        return data;
    }

    private static ChartPanel makePieChart(final PieDataset data) {
        final JFreeChart jfc2 = ChartFactory.createPieChart(
                "Total processed on each resource",
                data,
                true,
                false,
                false
        );
        final var v = new ChartPanel(jfc2);
        v.setPreferredSize(Graficos.PREFERRED_CHART_SIZE);
        return v;
    }

    private DefaultCategoryDataset makeBarChartData(
            final Map<String, ? extends MetricasProcessamento> metrics) {
        final var data = new DefaultCategoryDataset();
        if (metrics == null) {
            return data;
        }
        metrics.values().forEach(v -> data.addValue(v.getMFlopsProcessados(),
                "vermelho",
                Graficos.makeKey(v)));
        return data;
    }

    private static boolean shouldInclineXAxis(
            final Map<String, ? extends MetricasProcessamento> m) {
        return m != null && m.size() > 10;
    }

    private static void inclineChartXAxis(final JFreeChart chart) {
        chart.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
    }

    private static String makeKey(final MetricasProcessamento mt) {
        return mt.getnumeroMaquina() != 0
                ? "%s node %d".formatted(mt.getId(), mt.getnumeroMaquina())
                : mt.getId();
    }

    public void criarComunicacao(final Map<String, MetricasComunicacao> mComunicacao) {
        final DefaultCategoryDataset dadosGraficoComunicacao =
                new DefaultCategoryDataset();
        final DefaultPieDataset dadosGraficoPizzaComunicacao =
                new DefaultPieDataset();

        if (mComunicacao != null) {
            for (final Map.Entry<String, MetricasComunicacao> entry :
                    mComunicacao.entrySet()) {
                final MetricasComunicacao link = entry.getValue();
                dadosGraficoComunicacao.addValue(link.getMbitsTransmitidos(),
                        "vermelho", link.getId());
                dadosGraficoPizzaComunicacao.insertValue(0, link.getId(),
                        link.getMbitsTransmitidos());
            }
        }

        final var chart = ChartFactory.createBarChart(
                "Total communication in each resource",
                "Resource",
                "Mbits",
                dadosGraficoComunicacao,
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );

        if (mComunicacao != null && mComunicacao.size() > 10) {
            Graficos.inclineChartXAxis(chart);
        }

        final var panel = new ChartPanel(chart);
        panel.setPreferredSize(Graficos.PREFERRED_CHART_SIZE);
        final JFreeChart jfc;

        jfc = ChartFactory.createPieChart(
                "Total communication in each resource", //Titulo
                dadosGraficoPizzaComunicacao, // Dados para o grafico
                true, false, false);
        this.communicationPieChart = new ChartPanel(jfc);
        this.communicationPieChart.setPreferredSize(Graficos.PREFERRED_CHART_SIZE);
    }

    public void criarProcessamentoTempoTarefa(final List<Tarefa> tarefas) {

        final XYSeriesCollection dadosGrafico = new XYSeriesCollection();
        if (!tarefas.isEmpty()) {
            for (final Tarefa task : tarefas) {
                final XYSeries tmp_series;
                tmp_series = new XYSeries("task " + task.getIdentificador());
                final CS_Processamento temp =
                        (CS_Processamento) task.getLocalProcessamento();
                if (temp != null) {
                    final Double uso =
                            (temp.getPoderComputacional() / this.poderComputacionalTotal) * 100;
                    for (int j = 0; j < task.getTempoInicial().size(); j++) {
                        tmp_series.add(task.getTempoInicial().get(j),
                                (Double) 0.0);
                        tmp_series.add(task.getTempoInicial().get(j), uso);
                        tmp_series.add(task.getTempoFinal().get(j), uso);
                        tmp_series.add(task.getTempoFinal().get(j),
                                (Double) 0.0);
                    }
                    dadosGrafico.addSeries(tmp_series);
                }
            }

        }

        final JFreeChart jfc = ChartFactory.createXYAreaChart(
                "Use of total computing power through time "
                        + "\nTasks", //Titulo
                "Time (seconds)", // Eixo X
                "Rate of total use of computing power (%)", //Eixo Y
                dadosGrafico, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false); // exibir: legendas, tooltips, url
        this.TaskThroughTime = new ChartPanel(jfc);
    }

    //Cria o gráfico que demonstra o uso de cada recurso do sistema através
    // do tempo.
    //Ele recebe como parâmetro a lista com as maquinas que processaram
    // durante a simulação.
    public void criarProcessamentoTempoMaquina(final RedeDeFilas rdf) {
        final XYSeriesCollection dadosGrafico = new XYSeriesCollection();
        //Se tiver alguma máquina na lista.
        if (rdf.getMaquinas() != null) {
            //Laço foreach que percorre as máquinas.
            for (final CS_Processamento maq : rdf.getMaquinas()) {
                //Lista que recebe os pares de intervalo de tempo em que a
                // máquina executou.
                final List<ParesOrdenadosUso> lista =
                        maq.getListaProcessamento();
                this.poderComputacionalTotal += (maq.getPoderComputacional() - (maq.getOcupacao() * maq.getPoderComputacional()));
                //Se a máquina tiver intervalos.
                if (!lista.isEmpty()) {
                    //Cria o objeto do tipo XYSeries.
                    final XYSeries tmp_series;
                    //Se o atributo numeroMaquina for 0, ou seja, não for um
                    // nó de um cluster.
                    if (maq.getnumeroMaquina() == 0) //Estancia com o nome puro.
                    {
                        tmp_series = new XYSeries(maq.getId());
                    } //Se for 1 ou mais, ou seja, é um nó de cluster.
                    else //Estancia tmp_series com o nome concatenado com a
                    // palavra node e seu numero.
                    {
                        tmp_series =
                                new XYSeries(maq.getId() + " node " + maq.getnumeroMaquina());
                    }
                    int i;
                    //Laço que vai adicionando os pontos para a criação do
                    // gráfico.
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

        final JFreeChart jfc = ChartFactory.createXYAreaChart(
                "Use of computing power through time "
                        + "\nMachines", //Titulo
                "Time (seconds)", // Eixo X
                "Rate of use of computing power for each node (%)", //Eixo Y
                dadosGrafico, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false); // exibir: legendas, tooltips, url
        this.machineThroughTime = new ChartPanel(jfc);
        this.machineThroughTime.setPreferredSize(Graficos.PREFERRED_CHART_SIZE);
    }

    public void criarProcessamentoTempoUser(final List<Tarefa> tarefas,
                                            final RedeDeFilas rdf) {
        final ArrayList<UserOperationTime> lista =
                new ArrayList<UserOperationTime>();
        final int numberUsers = rdf.getUsuarios().size();
        final Map<String, Integer> users = new HashMap<String, Integer>();
        final XYSeries[] tmp_series = new XYSeries[numberUsers];
        final XYSeries[] tmp_series1 = new XYSeries[numberUsers];
        final Double[] utilizacaoUser = new Double[numberUsers];
        final Double[] utilizacaoUser1 = new Double[numberUsers];
        final XYSeriesCollection dadosGrafico = new XYSeriesCollection();
        final XYSeriesCollection dadosGrafico1 = new XYSeriesCollection();
        for (int i = 0; i < numberUsers; i++) {
            users.put(rdf.getUsuarios().get(i), i);
            utilizacaoUser[i] = 0.0;
            tmp_series[i] = new XYSeries(rdf.getUsuarios().get(i));
            utilizacaoUser1[i] = 0.0;
            tmp_series1[i] = new XYSeries(rdf.getUsuarios().get(i));
        }
        if (!tarefas.isEmpty()) {
            //Insere cada tarefa como dois pontos na lista
            for (final Tarefa task : tarefas) {
                final CS_Processamento local =
                        (CS_Processamento) task.getLocalProcessamento();
                if (local != null) {

                    for (int i = 0; i < task.getTempoInicial().size(); i++) {
                        final Double uso =
                                (task.getHistoricoProcessamento().get(i).getPoderComputacional() / this.poderComputacionalTotal) * 100;
                        final UserOperationTime provisorio1 =
                                new UserOperationTime(task.getTempoInicial().get(i), true, uso, users.get(task.getProprietario()));
                        lista.add(provisorio1);
                        final UserOperationTime provisorio2 =
                                new UserOperationTime(task.getTempoFinal().get(i), false, uso, users.get(task.getProprietario()));
                        lista.add(provisorio2);
                    }
                }
            }
            //Ordena lista
            Collections.sort(lista);
        }
        for (int i = 0; i < lista.size(); i++) {
            final UserOperationTime temp = lista.get(i);
            final int usuario = temp.getUserId();
            //Altera os valores do usuario atual e todos acima dele
            for (int j = usuario; j < numberUsers; j++) {
                //Salva valores anteriores
                tmp_series[j].add(temp.getTime(), utilizacaoUser[j]);
                if (temp.getType()) {
                    utilizacaoUser[j] += temp.getNodeUse();
                } else {
                    utilizacaoUser[j] -= temp.getNodeUse();
                }
                //Novo valor
                tmp_series[j].add(temp.getTime(), utilizacaoUser[j]);
            }
            //Grafico1
            tmp_series1[usuario].add(temp.getTime(),
                    utilizacaoUser1[usuario]);
            if (temp.getType()) {
                utilizacaoUser1[usuario] += temp.getNodeUse();
            } else {
                utilizacaoUser1[usuario] -= temp.getNodeUse();
            }
            tmp_series1[usuario].add(temp.getTime(),
                    utilizacaoUser1[usuario]);
        }
        for (int i = 0; i < numberUsers; i++) {
            dadosGrafico.addSeries(tmp_series[i]);
            dadosGrafico1.addSeries(tmp_series1[i]);
        }
        final JFreeChart user1 = ChartFactory.createXYAreaChart(
                "Use of total computing power through time"
                        + "\nUsers", //Titulo
                "Time (seconds)", // Eixo X
                "Rate of total use of computing power (%)", //Eixo Y
                dadosGrafico1, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false); // exibir: legendas, tooltips, url

        final JFreeChart user2 = ChartFactory.createXYLineChart(
                "Use of total computing power through time"
                        + "\nUsers", //Titulo
                "Time (seconds)", // Eixo X
                "Rate of total use of computing power (%)", //Eixo Y
                dadosGrafico, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false); // exibir: legendas, tooltips, url
        final XYPlot xyplot = (XYPlot) user2.getPlot();
        xyplot.setDomainPannable(true);
        final XYStepAreaRenderer xysteparearenderer = new XYStepAreaRenderer(2);
        xysteparearenderer.setDataBoundsIncludesVisibleSeriesOnly(false);
        xysteparearenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        xysteparearenderer.setDefaultEntityRadius(6);
        xyplot.setRenderer(xysteparearenderer);

        this.userThroughTime1 = new ChartPanel(user1);
        this.userThroughTime1.setPreferredSize(Graficos.PREFERRED_CHART_SIZE);
        this.UserThroughTime2 = new ChartPanel(user2);
        this.UserThroughTime2.setPreferredSize(Graficos.PREFERRED_CHART_SIZE);
    }

    public ChartPanel criarGraficoPorTarefa(final List<Tarefa> tarefas,
                                            final int idTarefa) {
        final DefaultCategoryDataset dadosMflopProcessados =
                new DefaultCategoryDataset();
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

                final JFreeChart jfc = ChartFactory.createStackedBarChart(
                        "MFlop usage for task " + idTarefa, //Titulo
                        "", // Eixo X
                        "% of total MFlop executed for the task", //Eixo Y
                        dadosMflopProcessados, // Dados para o grafico
                        PlotOrientation.VERTICAL, //Orientacao do grafico
                        true, true, false); // exibir: legendas, tooltips, url
                final ChartPanel graficoPorTarefa = new ChartPanel(jfc);
                graficoPorTarefa.setPreferredSize(Graficos.PREFERRED_CHART_SIZE);
                return graficoPorTarefa;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public ChartPanel criarGraficoAproveitamento(final List<Tarefa> tarefas) {

        final DefaultCategoryDataset dadosMflopProcessados =
                new DefaultCategoryDataset();
        double mflopDesperdicado = 0.0, tamanhoTotal = 0.0;
        int i, j;

        for (i = 0; i < tarefas.size(); i++) {

            if (tarefas.get(i).getEstado() != Tarefa.CANCELADO) {

                tamanhoTotal += tarefas.get(i).getTamProcessamento();

            }

            mflopDesperdicado += tarefas.get(i).getMflopsDesperdicados();

        }

        dadosMflopProcessados.addValue((tamanhoTotal / (mflopDesperdicado + tamanhoTotal)) * 100.0, "Usefull Processing", "MFlop Usage");
        dadosMflopProcessados.addValue((mflopDesperdicado / (mflopDesperdicado + tamanhoTotal)) * 100.0, "Wasted Processing", "MFlop Usage");

        final JFreeChart jfc = ChartFactory.createStackedBarChart(
                "Processing efficiency", //TituloUsage#
                "", // Eixo X
                "% of total MFlop executed", //Eixo Y
                dadosMflopProcessados, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false); // exibir: legendas, tooltips, url
        final ChartPanel graficoAproveitamentoPorcentagem = new ChartPanel(jfc);
        graficoAproveitamentoPorcentagem.setPreferredSize(Graficos.PREFERRED_CHART_SIZE);
        return graficoAproveitamentoPorcentagem;
    }

    public ChartPanel criarGraficoNumTarefasAproveitamento(final List<Tarefa> tarefas) {

        final DefaultCategoryDataset dadosMflopProcessados =
                new DefaultCategoryDataset();
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

        dadosMflopProcessados.addValue(numExcesso, "Number of tasks", "Tasks " +
                "with waste");
        dadosMflopProcessados.addValue(numOK, "Number of tasks", "Tasks " +
                "without waste");
        dadosMflopProcessados.addValue(numCanceladas, "Number of tasks",
                "Canceled Tasks");


        final JFreeChart jfc = ChartFactory.createStackedBarChart(
                "Processing efficiency", //Titulo
                "", // Eixo X
                "Number of tasks", //Eixo Y
                dadosMflopProcessados, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false); // exibir: legendas, tooltips, url
        final ChartPanel graficoAproveitamentoNumero = new ChartPanel(jfc);
        graficoAproveitamentoNumero.setPreferredSize(Graficos.PREFERRED_CHART_SIZE);
        return graficoAproveitamentoNumero;

    }

    public void criarGraficoPreempcao(final RedeDeFilas rdf,
                                      final List<Tarefa> tarefas) {

        final DefaultCategoryDataset preempPorUsuario =
                new DefaultCategoryDataset();
        double mflopTotal = 0.0, tamanhoTotal;
        final ArrayList<Integer> tarefasPreemp;
        tarefasPreemp = new ArrayList();
        int i, j, indexUsuario;

        for (i = 0; i < rdf.getUsuarios().size(); i++) {

            tarefasPreemp.add(0);

        }

        for (i = 0; i < tarefas.size(); i++) {

            indexUsuario =
                    rdf.getUsuarios().indexOf(tarefas.get(i).getProprietario());

            if (tarefas.get(i).getMflopsDesperdicados() > 0.0 && tarefas.get(i).getEstado() != Tarefa.CANCELADO) {
                tarefasPreemp.set(indexUsuario,
                        1 + tarefasPreemp.get(indexUsuario));
            }

            mflopTotal = 0.0;

        }

        for (i = 0; i < rdf.getUsuarios().size(); i++) {

            preempPorUsuario.addValue(tarefasPreemp.get(i), "Number of tasks"
                    , rdf.getUsuarios().get(i));

        }

        final JFreeChart jfc = ChartFactory.createStackedBarChart(
                "Tasks preempted per user", //Titulo
                "", // Eixo X
                "Number of tasks", //Eixo Y
                preempPorUsuario, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false); // exibir: legendas, tooltips, url
        this.PreemptionPerUser = new ChartPanel(jfc);
        this.PreemptionPerUser.setPreferredSize(Graficos.PREFERRED_CHART_SIZE);
    }

    public ChartPanel gerarGraficoPorMaquina(final List<Tarefa> tarefas,
                                             final String maq) {
        final DefaultCategoryDataset dadosMflopProcessados =
                new DefaultCategoryDataset();
        int i;
        int j;
        final int histIndex = -1;
        CS_Maquina alvo = null;
        final Tarefa task = null;
        double mflopUsado = 0.0;
        double mflopPerdido = 0.0;
        Double tempo;

        for (i = 0; i < this.rede.getMaquinas().size(); i++) {
            if (this.rede.getMaquinas().get(i).getId().equals(maq)) {
                alvo = this.rede.getMaquinas().get(i);
                break;
            }
        }

        if (alvo != null) {

            for (i = 0; i < alvo.getHistorico().size(); i++) {

                if (alvo.getHistorico().get(i).getMflopsDesperdicados() > 0.0) {

                    for (j = 0; j < alvo.getHistorico().get(i).getHistoricoProcessamento().size(); j++) {

                        if (alvo.getHistorico().get(i).getHistoricoProcessamento().get(j).getId().equals(alvo.getId())) {

                            tempo = alvo.getHistorico().get(i).getTempoFinal().get(j) - alvo.getHistorico().get(i).getTempoInicial().get(j);
                            if (alvo.getHistorico().get(i).getCheckPoint() != 0.0) {
                                mflopUsado += alvo.getMflopsProcessados(tempo) / alvo.getHistorico().get(i).getCheckPoint() - alvo.getMflopsProcessados(tempo) % alvo.getHistorico().get(i).getCheckPoint();
                                mflopPerdido += alvo.getMflopsProcessados(tempo) % alvo.getHistorico().get(i).getCheckPoint();
                            } else {
                                mflopPerdido += alvo.getMflopsProcessados(tempo);
                            }

                        }

                    }

                } else {

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

            final JFreeChart jfc = ChartFactory.createStackedBarChart(
                    "Processing efficiency for resource " + alvo.getId(),
                    //Titulo
                    "", // Eixo X
                    "% of total MFlop executed", //Eixo Y
                    dadosMflopProcessados, // Dados para o grafico
                    PlotOrientation.VERTICAL, //Orientacao do grafico
                    true, true, false); // exibir: legendas, tooltips, url
            final ChartPanel graficoAproveitamentoMaquina = new ChartPanel(jfc);
            graficoAproveitamentoMaquina.setPreferredSize(Graficos.PREFERRED_CHART_SIZE);
            return graficoAproveitamentoMaquina;
        }

        return null;
    }


    public void calculaPoderTotal(final RedeDeFilas rdf) {
        for (final CS_Processamento maq : rdf.getMaquinas()) {
            this.poderComputacionalTotal += (maq.getPoderComputacional() - (maq.getOcupacao() * maq.getPoderComputacional()));
        }
    }

    protected static class UserOperationTime implements Comparable<UserOperationTime> {
        // TODO: make this should be a record

        private final Double time;
        private final Double nodeUse;
        private final Boolean type;
        private final Integer userId;

        private UserOperationTime(final double time, final boolean type,
                                  final Double uso,
                                  final Integer userId) {
            this.userId = userId;
            this.time = time;
            this.nodeUse = uso;
            this.type = type;
        }

        Integer getUserId() {
            return this.userId;
        }

        Boolean getType() {
            return this.type;
        }

        Double getNodeUse() {
            return this.nodeUse;
        }

        @Override
        public int compareTo(final UserOperationTime o) {
            return this.time.compareTo(o.getTime());
        }

        Double getTime() {
            return this.time;
        }
    }
}
