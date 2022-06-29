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
 * Class which creates and configures charts for simulation results.
 */
public class SimulationResultChartMaker {
    private static final Double ZERO = 0.0;
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
        this.processingBarChart = SimulationResultChartMaker.makeBarChart(metrics);
        this.processingPieChart = SimulationResultChartMaker.makePieChart(metrics);
    }

    private static ChartPanel makeBarChart(
            final Map<String, ? extends MetricasProcessamento> metrics) {
        final var chart = ChartFactory.createBarChart(
                "Total processed on each " +
                        "resource",
                "Resource",
                "Mflops",
                SimulationResultChartMaker.makeBarChartData(metrics),
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );

        if (SimulationResultChartMaker.shouldInclineXAxis(metrics)) {
            SimulationResultChartMaker.inclineChartXAxis(chart);
        }

        return SimulationResultChartMaker.panelWithPreferredSize(chart);
    }

    private static ChartPanel makePieChart(
            final Map<String, ? extends MetricasProcessamento> metrics) {
        return SimulationResultChartMaker.panelWithPreferredSize(ChartFactory.createPieChart(
                "Total processed on each resource",
                SimulationResultChartMaker.makePieChartData(metrics),
                true,
                false,
                false
        ));
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
                SimulationResultChartMaker.makeKey(v)));
        return data;
    }

    private static boolean shouldInclineXAxis(
            final Map<String, ? extends MetricasProcessamento> metrics) {
        return metrics != null && metrics.size() > 10;
    }

    private static void inclineChartXAxis(final JFreeChart chart) {
        chart.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
    }

    private static ChartPanel panelWithPreferredSize(final JFreeChart chart) {
        final var panel = new ChartPanel(chart);
        panel.setPreferredSize(SimulationResultChartMaker.PREFERRED_CHART_SIZE);
        return panel;
    }

    private static DefaultPieDataset makePieChartData(
            final Map<String, ? extends MetricasProcessamento> metrics) {
        final var data = new DefaultPieDataset();
        if (metrics == null) {
            return data;
        }
        metrics.values().forEach(v -> data.insertValue(
                0,
                SimulationResultChartMaker.makeKey(v),
                v.getMFlopsProcessados())
        );
        return data;
    }

    private static String makeKey(final MetricasProcessamento mt) {
        final var id = mt.getId();
        final var machineNum = mt.getnumeroMaquina();
        return machineNum == 0 ? id
                : "%s node %d".formatted(id, machineNum);
    }

    public void criarComunicacao(
            final Map<String, ? extends MetricasComunicacao> metrics) {
        final var commsPieChartData = new DefaultPieDataset();

        if (metrics != null) {
            for (final var link : metrics.values()) {
                commsPieChartData.insertValue(
                        0, link.getId(), link.getMbitsTransmitidos());
            }
        }

        this.communicationPieChart =
                SimulationResultChartMaker.panelWithPreferredSize(ChartFactory.createPieChart(
                        "Total communication in each resource",
                        commsPieChartData,
                        true,
                        false,
                        false
                ));
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
            series.add(time, SimulationResultChartMaker.ZERO);
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
        return SimulationResultChartMaker.panelWithPreferredSize(ChartFactory.createXYAreaChart(
                "Use of computing power through time \nMachines",
                "Time (seconds)",
                "Rate of use of computing power for each node (%)",
                this.getMachineProcTimeChartData(qn),
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        ));
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
        this.poderComputacionalTotal += SimulationResultChartMaker.computationalPowerForMachine(machine);

        final List<ParesOrdenadosUso> list = machine.getListaProcessamento();

        if (list.isEmpty()) {
            return;
        }

        final var timeSeries = SimulationResultChartMaker.buildSeriesForMachine(machine, list);

        data.addSeries(timeSeries);
    }

    private static double computationalPowerForMachine(final CS_Processamento machine) {
        final var power = machine.getPoderComputacional();
        return power - (machine.getOcupacao() * power);
    }

    private static XYSeries buildSeriesForMachine(
            final CS_Processamento machine,
            final List<? extends ParesOrdenadosUso> list) {
        final Double usage = SimulationResultChartMaker.ONE_HUNDRED_PERCENT
                             - (machine.getOcupacao() * SimulationResultChartMaker.ONE_HUNDRED_PERCENT);

        final var title = SimulationResultChartMaker.isMachineCluster(machine)
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
                timeSeries.add(currInterval.getFim(), SimulationResultChartMaker.ZERO);
                timeSeries.add(nextInterval.getInicio(), SimulationResultChartMaker.ZERO);
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

        final var timeSeries1 = SimulationResultChartMaker.userSeries(qn);
        final var userUsage1 = new Double[userCount];
        final var chartData1 = new XYSeriesCollection();

        final var timeSeries2 = SimulationResultChartMaker.userSeries(qn);
        final var userUsage2 = new Double[userCount];
        final var chartData2 = new XYSeriesCollection();

        final var list =
                this.makeUserTimesList(tasks, SimulationResultChartMaker.makeUserMap(qn));

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

        final ChartPanel chartPanel1 = SimulationResultChartMaker.panelWithPreferredSize(chart1);
        this.userThroughTime1 = chartPanel1;

        final ChartPanel chartPanel2 = SimulationResultChartMaker.panelWithPreferredSize(chart2);
        this.UserThroughTime2 = chartPanel2;
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

    private void setPlotRenderer(final JFreeChart user2) {
        final var xyPlot = (XYPlot) user2.getPlot();
        xyPlot.setDomainPannable(true);
        final var xyStepAreaRenderer = new XYStepAreaRenderer(2);
        xyStepAreaRenderer.setDataBoundsIncludesVisibleSeriesOnly(false);
        xyStepAreaRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        xyStepAreaRenderer.setDefaultEntityRadius(6);
        xyPlot.setRenderer(xyStepAreaRenderer);
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
        return (task.getHistoricoProcessamento().get(i).getPoderComputacional() / this.poderComputacionalTotal) * SimulationResultChartMaker.ONE_HUNDRED_PERCENT;
    }

    public ChartPanel criarGraficoPorTarefa(
            final Collection<? extends Tarefa> tasks,
            final int taskId) {

        final var task = tasks.stream()
                .filter(t -> t.getIdentificador() == taskId)
                .findFirst()
                .orElse(null);

        if (task == null || task.getEstado() == Tarefa.CANCELADO) {
            return null;
        }

        final double totalProcessedMFlops =
                SimulationResultChartMaker.calculateTotalProcessedMFlops(task);

        final var chartData = new DefaultCategoryDataset();
        SimulationResultChartMaker.addTaskProcessingData(chartData, task.getTamProcessamento(),
                "Usefull processing", totalProcessedMFlops);
        SimulationResultChartMaker.addTaskProcessingData(chartData, task.getMflopsDesperdicados(),
                "Wasted processing", totalProcessedMFlops);

        final var chart = ChartFactory.createStackedBarChart(
                "MFlop usage for task " + taskId,
                "",
                "% of total MFlop executed for the task",
                chartData,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        final ChartPanel panel = SimulationResultChartMaker.panelWithPreferredSize(chart);
        return panel;
    }

    private static double calculateTotalProcessedMFlops(final Tarefa task) {
        final int historySize = task.getHistoricoProcessamento().size();
        double total = 0.0;
        for (int i = 0; i < historySize; i++) {
            total += task.getHistoricoProcessamento().get(i).getMflopsProcessados(
                    task.getTempoFinal().get(i) - task.getTempoInicial().get(i));
        }
        return total;
    }

    private static void addTaskProcessingData(
            final DefaultCategoryDataset data,
            final double processing,
            final String title,
            final double totalProcessing) {
        data.addValue(
                processing / totalProcessing * SimulationResultChartMaker.ONE_HUNDRED_PERCENT,
                title,
                "Task size :%s MFlop, total executed for task: %s MFlop"
                        .formatted(processing, totalProcessing)
        );
    }

    public ChartPanel criarGraficoAproveitamento(
            final Collection<? extends Tarefa> tasks) {
        return SimulationResultChartMaker.panelWithPreferredSize(ChartFactory.createStackedBarChart(
                "Processing efficiency",
                "",
                "% of total MFlop executed",
                SimulationResultChartMaker.makeUsageChartData(tasks),
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        ));
    }

    private static DefaultCategoryDataset makeUsageChartData(
            final Collection<? extends Tarefa> tasks) {

        final double useful = tasks.stream()
                .filter(t -> t.getEstado() != Tarefa.CANCELADO)
                .mapToDouble(Tarefa::getTamProcessamento)
                .sum();

        final double wasted = tasks.stream()
                .mapToDouble(Tarefa::getMflopsDesperdicados)
                .sum();

        final double total = wasted + useful;

        final var chartData = new DefaultCategoryDataset();
        SimulationResultChartMaker.addProcessingData(
                chartData, "Usefull Processing", useful, total);
        SimulationResultChartMaker.addProcessingData(
                chartData, "Wasted Processing", wasted, total);
        return chartData;
    }

    private static void addProcessingData(
            final DefaultCategoryDataset chartData,
            final String title,
            final double processing,
            final double totalProcessing) {
        chartData.addValue(processing / totalProcessing * SimulationResultChartMaker.ONE_HUNDRED_PERCENT, title, "MFlop Usage");
    }

    public ChartPanel criarGraficoNumTarefasAproveitamento(
            final Iterable<? extends Tarefa> tasks) {
        return SimulationResultChartMaker.panelWithPreferredSize(ChartFactory.createStackedBarChart(
                "Processing efficiency",
                "",
                "Number of tasks",
                SimulationResultChartMaker.makeChartDataForTaskCount(tasks),
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        ));
    }

    private static DefaultCategoryDataset makeChartDataForTaskCount(
            final Iterable<? extends Tarefa> tasks) {

        int cancelled = 0;
        int withoutWaste = 0;
        int withWaste = 0;

        for (final var task : tasks) {
            if (task.getEstado() == Tarefa.CANCELADO) {
                cancelled++;
            } else if (task.getMflopsDesperdicados() == 0.0) {
                withoutWaste++;
            } else {
                withWaste++;
            }
        }

        final var chartData = new DefaultCategoryDataset();

        chartData.addValue(
                withWaste, "Number of tasks", "Tasks with waste");
        chartData.addValue(
                withoutWaste, "Number of tasks", "Tasks without waste");
        chartData.addValue(
                cancelled, "Number of tasks", "Canceled Tasks");

        return chartData;
    }

    public void criarGraficoPreempcao(
            final RedeDeFilas qn,
            final Iterable<? extends Tarefa> tasks) {
        this.PreemptionPerUser =
                SimulationResultChartMaker.panelWithPreferredSize(ChartFactory.createStackedBarChart(
                        "Tasks preempted per user",
                        "",
                        "Number of tasks",
                        SimulationResultChartMaker.makePreemptionChartData(qn, tasks),
                        PlotOrientation.VERTICAL,
                        true,
                        true,
                        false
                ));
    }

    private static DefaultCategoryDataset makePreemptionChartData(
            final RedeDeFilas qn,
            final Iterable<? extends Tarefa> tasks) {

        final var preemptedTasks =
                SimulationResultChartMaker.calculatePreemptedTasksPerUser(qn, tasks);

        final var chartData = new DefaultCategoryDataset();

        final int userCount = qn.getUsuarios().size();
        for (int i = 0; i < userCount; i++) {
            chartData.addValue(
                    preemptedTasks.get(i),
                    "Number of tasks",
                    qn.getUsuarios().get(i)
            );
        }

        return chartData;
    }

    private static ArrayList<Integer> calculatePreemptedTasksPerUser(
            final RedeDeFilas qn,
            final Iterable<? extends Tarefa> tasks) {
        final var preemptedTasks = SimulationResultChartMaker.preemptiveTasksList(qn);

        for (final var task : tasks) {
            final int userIndex =
                    qn.getUsuarios().indexOf(task.getProprietario());

            if (SimulationResultChartMaker.taskIsPreempted(task)) {
                preemptedTasks.set(
                        userIndex, 1 + preemptedTasks.get(userIndex));
            }
        }

        return preemptedTasks;
    }

    private static ArrayList<Integer> preemptiveTasksList(final RedeDeFilas qn) {
        final int userCount = qn.getUsuarios().size();
        final var preemptiveTasks = new ArrayList<Integer>(userCount);
        for (int i = 0; i < userCount; i++) {
            preemptiveTasks.add(0);
        }
        return preemptiveTasks;
    }

    private static boolean taskIsPreempted(final Tarefa task) {
        return task.getMflopsDesperdicados() > 0.0 && task.getEstado() != Tarefa.CANCELADO;
    }

    public ChartPanel gerarGraficoPorMaquina(
            final List<Tarefa> tasks,
            final String machineId) {

        final var machine = this.rede.getMaquinas().stream()
                .filter(machine1 -> machine1.getId().equals(machineId))
                .findFirst()
                .orElse(null);

        if (machine == null) {
            return null;
        }

        return SimulationResultChartMaker.panelWithPreferredSize(ChartFactory.createStackedBarChart(
                "Processing efficiency for resource %s".formatted(machine.getId()),
                "",
                "% of total MFlop executed",
                SimulationResultChartMaker.makePerMachineChartData(machine),
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        ));
    }

    private static DefaultCategoryDataset makePerMachineChartData(final CS_Maquina machine) {
        double usedMFlops = 0.0;
        double lostMFlops = 0.0;

        for (final var task : machine.getHistorico()) {
            final var machines = task.getHistoricoProcessamento();
            final int count = machines.size();
            for (int i = 0; i < count; ++i) {
                if (!machine.getId().equals(machines.get(i).getId())) {
                    continue;
                }

                final double processed = machine.getMflopsProcessados(
                        task.getTempoFinal().get(i) - task.getTempoInicial().get(i));

                if (task.getMflopsDesperdicados() == 0.0) {
                    usedMFlops += processed;
                    continue;
                }

                final double checkpoint = task.getCheckPoint();

                if (checkpoint == 0.0) {
                    lostMFlops += processed;
                    continue;
                }

                final double quotient = processed / checkpoint;
                final double remainder = processed % checkpoint;
                usedMFlops += quotient - remainder;
                lostMFlops += remainder;
            }
        }

        final double total = lostMFlops + usedMFlops;
        final var data = new DefaultCategoryDataset();
        SimulationResultChartMaker.addProcessingData(
                data, "Usefull Processing", usedMFlops, total);
        SimulationResultChartMaker.addProcessingData(
                data, "Wasted Processing", lostMFlops, total);
        return data;
    }

    public void calculaPoderTotal(final RedeDeFilas rdf) {
        for (final CS_Processamento maq : rdf.getMaquinas()) {
            this.poderComputacionalTotal += SimulationResultChartMaker.computationalPowerForMachine(maq);
        }
    }
}