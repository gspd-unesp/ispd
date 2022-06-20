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
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Classe criada para separear a criação e controle dos gráficos da janela de
 * exibição dos resultados da simulação
 */
public class Graficos {

    public static final Double ZERO = 0.0;
    private static final double ONE_HUNDRED_PERCENT = 100.0;
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

        this.processingBarChart = Graficos.makeBarChart(metrics);
        this.processingPieChart = Graficos.makePieChart(metrics);
    }

    private static ChartPanel makeBarChart(
            final Map<String, ? extends MetricasProcessamento> metrics) {
        final var chart = ChartFactory.createBarChart(
                "Total processed on each " +
                        "resource",
                "Resource",
                "Mflops",
                Graficos.makeBarChartData(metrics),
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

    private static ChartPanel makePieChart(
            final Map<String, ? extends MetricasProcessamento> metrics) {
        final JFreeChart jfc2 = ChartFactory.createPieChart(
                "Total processed on each resource",
                Graficos.makePieChartData(metrics),
                true,
                false,
                false
        );
        final var v = new ChartPanel(jfc2);
        v.setPreferredSize(Graficos.PREFERRED_CHART_SIZE);
        return v;
    }

    private static DefaultCategoryDataset makeBarChartData(
            final Map<String, ? extends MetricasProcessamento> metrics) {
        final var data = new DefaultCategoryDataset();
        if (metrics == null) {
            return data;
        }
        metrics.values().forEach(v -> data.addValue(
                v.getMFlopsProcessados(),
                "vermelho",
                Graficos.makeKey(v)));
        return data;
    }

    private static boolean shouldInclineXAxis(
            final Map<String, ? extends MetricasProcessamento> metrics) {
        return metrics != null && metrics.size() > 10;
    }

    private static void inclineChartXAxis(final JFreeChart chart) {
        chart.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
    }

    private static DefaultPieDataset makePieChartData(
            final Map<String, ? extends MetricasProcessamento> metrics) {
        final var data = new DefaultPieDataset();
        if (metrics == null) {
            return data;
        }
        metrics.values().forEach(v -> data.insertValue(
                0,
                Graficos.makeKey(v),
                v.getMFlopsProcessados())
        );
        return data;
    }

    private static String makeKey(final MetricasProcessamento mt) {
        return mt.getnumeroMaquina() != 0
                ? "%s node %d".formatted(mt.getId(), mt.getnumeroMaquina())
                : mt.getId();
    }

    public void criarComunicacao(
            final Map<String, ? extends MetricasComunicacao> metrics) {
        final DefaultPieDataset commsPieChartData = new DefaultPieDataset();

        if (metrics != null) {
            for (final var link : metrics.values()) {
                commsPieChartData.insertValue(0, link.getId(),
                        link.getMbitsTransmitidos());
            }
        }

        final var jfc = ChartFactory.createPieChart(
                "Total communication in each resource",
                commsPieChartData,
                true,
                false,
                false);
        final ChartPanel cpc = new ChartPanel(jfc);
        cpc.setPreferredSize(Graficos.PREFERRED_CHART_SIZE);
        this.communicationPieChart = cpc;
    }

    public void criarProcessamentoTempoTarefa(
            final Iterable<? extends Tarefa> tasks) {
        this.TaskThroughTime = this.makeTaskThroughTimeChart(tasks);
    }

    private ChartPanel makeTaskThroughTimeChart(
            final Iterable<? extends Tarefa> tasks) {
        return new ChartPanel(ChartFactory.createXYAreaChart(
                "Use of total computing power through time \nTasks",
                "Time (seconds)",
                "Rate of total use of computing power (%)",
                this.makeTTTChartData(tasks),
                PlotOrientation.VERTICAL,
                true,
                true,
                false)
        );
    }

    private XYSeriesCollection makeTTTChartData(
            final Iterable<? extends Tarefa> tasks) {

        final var data = new XYSeriesCollection();

        tasks.forEach(task -> this.addTaskToChartData(task, data));

        return data;
    }

    private void addTaskToChartData(
            final Tarefa task, final XYSeriesCollection chartData) {
        final var series = this.makeTaskTimeSeries(task);
        if (series == null) return;
        chartData.addSeries(series);
    }

    private XYSeries makeTaskTimeSeries(final Tarefa task) {
        final var temp = (CS_Processamento) task.getLocalProcessamento();

        if (temp == null) {
            return null;
        }

        final var series =
                new XYSeries("task %d".formatted(task.getIdentificador()));

        final Double usage =
                temp.getPoderComputacional() / this.poderComputacionalTotal * 100;

        Stream.concat(
                task.getTempoInicial().stream(),
                task.getTempoFinal().stream()
        ).forEach(time -> {
            series.add(time, Graficos.ZERO);
            series.add(time, usage);
        });

        return series;
    }

    /**
     * Cria o gráfico que demonstra o uso de cada recurso do sistema através
     * do tempo.
     * Ele recebe como parâmetro a lista com as maquinas que processaram
     * durante a simulação.
     */
    public void criarProcessamentoTempoMaquina(final RedeDeFilas qn) {
        this.machineThroughTime = this.makeMachineProcTimeChart(qn);
    }

    private ChartPanel makeMachineProcTimeChart(final RedeDeFilas qn) {
        final ChartPanel chart = new ChartPanel(ChartFactory.createXYAreaChart(
                "Use of computing power through time \nMachines",
                "Time (seconds)",
                "Rate of use of computing power for each node (%)",
                this.getMachineProcTimeChartData(qn),
                PlotOrientation.VERTICAL,
                true,
                true,
                false)
        );
        chart.setPreferredSize(Graficos.PREFERRED_CHART_SIZE);
        return chart;
    }

    private XYSeriesCollection getMachineProcTimeChartData(final RedeDeFilas qn) {
        final var data = new XYSeriesCollection();
        if (qn.getMaquinas() == null) {
            return data;
        }
        qn.getMaquinas().forEach(machine ->
                this.addMachineStatsToChartData(data, machine));
        return data;
    }

    private void addMachineStatsToChartData(
            final XYSeriesCollection data, final CS_Processamento machine) {
        this.poderComputacionalTotal += Graficos.computationalPowerForMachine(machine);

        final List<ParesOrdenadosUso> list = machine.getListaProcessamento();

        if (list.isEmpty()) {
            return;
        }

        final var timeSeries = Graficos.buildSeriesForMachine(machine, list);

        data.addSeries(timeSeries);
    }

    private static double computationalPowerForMachine(final CS_Processamento machine) {
        final var power = machine.getPoderComputacional();
        return power - (machine.getOcupacao() * power);
    }

    private static XYSeries buildSeriesForMachine(
            final CS_Processamento machine,
            final List<? extends ParesOrdenadosUso> list) {
        final Double usage = Graficos.ONE_HUNDRED_PERCENT
                - (machine.getOcupacao() * Graficos.ONE_HUNDRED_PERCENT);

        final var title = Graficos.isMachineCluster(machine)
                ? "%s node %d".formatted(
                machine.getId(), machine.getnumeroMaquina())
                : machine.getId();
        final var timeSeries = new XYSeries(title);

        final var count = list.size();
        for (int i = 0; i < count; ++i) {
            final var currInterval = list.get(i);
            timeSeries.add(currInterval.getInicio(), usage);
            timeSeries.add(currInterval.getFim(), usage);
            if (i + 1 < count) {
                final var nextInterval = list.get(i + 1);
                timeSeries.add(currInterval.getFim(), Graficos.ZERO);
                timeSeries.add(nextInterval.getInicio(), Graficos.ZERO);
            }
        }

        return timeSeries;
    }

    private static boolean isMachineCluster(final CS_Processamento machine) {
        return machine.getnumeroMaquina() != 0;
    }

    public void criarProcessamentoTempoUser(
            final Collection<? extends Tarefa> tasks, final RedeDeFilas qn) {
        final int userCount = qn.getUsuarios().size();

        final var timeSeries1 = Graficos.userSeries(qn);
        final var userUsage1 = new Double[userCount];
        final var chartData1 = new XYSeriesCollection();

        final var timeSeries2 = Graficos.userSeries(qn);
        final var userUsage2 = new Double[userCount];
        final var chartData2 = new XYSeriesCollection();

        final var list =
                this.makeUserTimesList(tasks, Graficos.makeUserMap(qn));

        for (final var useTime : list) {
            final int userId = useTime.getUserId();
            for (int id = userId; id < userCount; id++) {
                this.updateUserUsage(timeSeries1[id], userUsage1, useTime, id);
            }
            this.updateUserUsage(timeSeries2[userId], userUsage2, useTime,
                    userId);
        }

        for (int i = 0; i < userCount; i++) {
            chartData1.addSeries(timeSeries1[i]);
        }

        for (int i = 0; i < userCount; i++) {
            chartData2.addSeries(timeSeries2[i]);
        }

        final JFreeChart chart1 = ChartFactory.createXYAreaChart(
                "Use of total computing power through time\nUsers",
                "Time (seconds)",
                "Rate of total use of computing power (%)",
                chartData2,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        final JFreeChart chart2 = ChartFactory.createXYLineChart(
                "Use of total computing power through time\nUsers",
                "Time (seconds)",
                "Rate of total use of computing power (%)",
                chartData1,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        this.setPlotRenderer(chart2);

        final var chartPanel1 = new ChartPanel(chart1);
        chartPanel1.setPreferredSize(Graficos.PREFERRED_CHART_SIZE);
        this.userThroughTime1 = chartPanel1;

        final var chartPanel2 = new ChartPanel(chart2);
        chartPanel2.setPreferredSize(Graficos.PREFERRED_CHART_SIZE);
        this.UserThroughTime2 = chartPanel2;
    }

    private void setPlotRenderer(final JFreeChart user2) {
        final var xyPlot = (XYPlot) user2.getPlot();
        xyPlot.setDomainPannable(true);
        final var xyStepAreaRenderer = new XYStepAreaRenderer(2);
        xyStepAreaRenderer.setDataBoundsIncludesVisibleSeriesOnly(false);
        xyStepAreaRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        xyStepAreaRenderer.setDefaultEntityRadius(6);
        xyPlot.setRenderer(xyStepAreaRenderer);
    }

    private static XYSeries[] userSeries(final RedeDeFilas qn) {
        final int userCount = qn.getUsuarios().size();
        final var series = new XYSeries[userCount];
        for (int i = 0; i < userCount; i++) {
            series[i] = new XYSeries(qn.getUsuarios().get(i));
        }
        return series;
    }

    private ArrayList<UserOperationTime> makeUserTimesList(
            final Collection<? extends Tarefa> tasks,
            final Map<String, Integer> users) {
        final var list = new ArrayList<UserOperationTime>(0);

        if (tasks.isEmpty()) {
            return list;
        }

        for (final var task : tasks) {
            this.addTaskStatsToList(task, list, users);
        }

        Collections.sort(list);
        return list;
    }

    private static Map<String, Integer> makeUserMap(final RedeDeFilas qn) {
        final int userCount = qn.getUsuarios().size();
        final Map<String, Integer> users = new HashMap<>(userCount);
        for (int i = 0; i < userCount; i++) {
            users.put(qn.getUsuarios().get(i), i);
        }
        return users;
    }

    private void updateUserUsage(
            final XYSeries timeSeries,
            final Double[] usages,
            final UserOperationTime userTime,
            final int index) {
        timeSeries.add(userTime.getTime(), usages[index]);
        if (userTime.isStartTime()) {
            usages[index] += userTime.getNodeUse();
        } else {
            usages[index] -= userTime.getNodeUse();
        }
        timeSeries.add(userTime.getTime(), usages[index]);
    }

    private void addTaskStatsToList(
            final Tarefa task,
            final Collection<? super UserOperationTime> list,
            final Map<String, Integer> users) {
        if (task.getLocalProcessamento() == null) {
            return;
        }

        final int intervalCount = task.getTempoInicial().size();
        for (int i = 0; i < intervalCount; i++) {
            final var usage = this.calculateUsage(task, i);
            final var ownerId = users.get(task.getProprietario());
            list.add(new UserOperationTime(
                    task.getTempoInicial().get(i),
                    true,
                    usage,
                    ownerId
            ));
            list.add(new UserOperationTime(
                    task.getTempoFinal().get(i),
                    false,
                    usage,
                    ownerId
            ));
        }
    }

    private double calculateUsage(final Tarefa task, final int i) {
        return (task.getHistoricoProcessamento().get(i).getPoderComputacional() / this.poderComputacionalTotal) * Graficos.ONE_HUNDRED_PERCENT;
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

                dadosMflopProcessados.addValue(((job.getTamProcessamento() / mflopProcessadoTotal) * Graficos.ONE_HUNDRED_PERCENT), "Usefull processing", "Task size :" + job.getTamProcessamento() + " MFlop" + ", total executed for task: " + mflopProcessadoTotal + " MFlop");
                dadosMflopProcessados.addValue(((job.getMflopsDesperdicados()) / mflopProcessadoTotal) * Graficos.ONE_HUNDRED_PERCENT, "Wasted processing", "Task size :" + job.getTamProcessamento() + " MFlop" + ", total executed for task: " + mflopProcessadoTotal + " MFlop");

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

        dadosMflopProcessados.addValue((tamanhoTotal / (mflopDesperdicado + tamanhoTotal)) * Graficos.ONE_HUNDRED_PERCENT, "Usefull Processing", "MFlop Usage");
        dadosMflopProcessados.addValue((mflopDesperdicado / (mflopDesperdicado + tamanhoTotal)) * Graficos.ONE_HUNDRED_PERCENT, "Wasted Processing", "MFlop Usage");

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
                                             final String machineId) {
        final var target = this.findMachineWithIdOrNull(machineId);

        if (target == null) {
            return null;
        }

        double lostMFlops = 0.0;
        double usedMFlops = 0.0;

        for (int i = 0; i < target.getHistorico().size(); i++) {

            final Tarefa task = target.getHistorico().get(i);

            if (task.getMflopsDesperdicados() > 0.0) {

                for (int j = 0; j < task.getHistoricoProcessamento().size(); j++) {

                    if (task.getHistoricoProcessamento().get(j).getId().equals(target.getId())) {

                        final double tempo =
                                task.getTempoFinal().get(j) - task.getTempoInicial().get(j);
                        if (task.getCheckPoint() == 0.0) {
                            lostMFlops += target.getMflopsProcessados(tempo);
                        } else {
                            final double remainder =
                                    target.getMflopsProcessados(tempo) % task.getCheckPoint();
                            usedMFlops += target.getMflopsProcessados(tempo) / task.getCheckPoint() - remainder;
                            lostMFlops += remainder;
                        }

                    }

                }

            } else {

                for (int j = 0; j < task.getHistoricoProcessamento().size(); j++) {

                    if (task.getHistoricoProcessamento().get(j).getId().equals(target.getId())) {

                        final double tempo =
                                task.getTempoFinal().get(j) - task.getTempoInicial().get(j);
                        usedMFlops += target.getMflopsProcessados(tempo);

                    }

                }

            }

        }

        final var mFlopsData = Graficos.makeMFlopsData(usedMFlops, lostMFlops);

        final var jfc = ChartFactory.createStackedBarChart(
                "Processing efficiency for resource %s".formatted(target.getId()),
                "",
                "% of total MFlop executed",
                mFlopsData,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        final var machineUseChart = new ChartPanel(jfc);
        machineUseChart.setPreferredSize(Graficos.PREFERRED_CHART_SIZE);
        return machineUseChart;
    }

    private CS_Maquina findMachineWithIdOrNull(final String id) {
        return this.rede.getMaquinas().stream().filter(machine -> machine.getId().equals(id)).findFirst().orElse(null);
    }

    private static DefaultCategoryDataset makeMFlopsData(final double usedMFlops,
                                                         final double lostMFlops) {
        final var data = new DefaultCategoryDataset();
        final double total = lostMFlops + usedMFlops;
        data.addValue((usedMFlops / total) * Graficos.ONE_HUNDRED_PERCENT,
                "Usefull Processing", "MFlop Usage");
        data.addValue((lostMFlops / total) * Graficos.ONE_HUNDRED_PERCENT,
                "Wasted Processing", "MFlop Usage");
        return data;
    }

    public void calculaPoderTotal(final RedeDeFilas rdf) {
        for (final CS_Processamento maq : rdf.getMaquinas()) {
            this.poderComputacionalTotal += Graficos.computationalPowerForMachine(maq);
        }
    }

    protected static class UserOperationTime implements Comparable<UserOperationTime> {
        // TODO: make this should be a record

        private final Double time;
        private final Boolean isStartTime;
        private final Double nodeUse;
        private final Integer userId;

        private UserOperationTime(
                final double time,
                final boolean isStartTime,
                final Double nodeUse,
                final Integer userId) {
            this.time = time;
            this.isStartTime = isStartTime;
            this.nodeUse = nodeUse;
            this.userId = userId;
        }

        Integer getUserId() {
            return this.userId;
        }

        Boolean isStartTime() {
            return this.isStartTime;
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