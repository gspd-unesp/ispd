package ispd.gui.auxiliar;

import ispd.gui.results.ResultsDialog;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.metricas.Metricas;
import ispd.motor.metricas.MetricasComunicacao;
import ispd.motor.metricas.MetricasProcessamento;
import ispd.utils.Pair;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link SimulationResultChartMaker} class is used to create charts of
 * multiples types containing information from simulation results.
 */
public final class SimulationResultChartMaker {

    /**
     * It represents the processing bar chart, that is, a chart containing
     * information about the performed processing for machines (including the
     * cluster ones) in a vertical bar view.
     */
    private final ChartPanel processingBarChart;

    /**
     * It represents the processing pie chart, that is, a chart containing
     * information about the performed processing for machines (including the
     * cluster ones) in a pie view.
     */
    private final ChartPanel processingPieChart;

    /**
     * It represents the communication bar chart, that is, a chart containing
     * information about the performed communication for machines (including the
     * cluster ones) and links in a vertical bar view.
     */
    private final ChartPanel communicationBarChart;

    /**
     * It represents the communication pie chart, that is, a chart containing
     * information about the performed communication for machines (including the
     * cluster ones) and links in a pie view.
     */
    private final ChartPanel communicationPieChart;

    /**
     * It represents the computing power per machine through time chart, that is,
     * a chart containing information about the computing power use through time
     * for each machine (including the cluster ones) in a vertical bar view.
     */
    private ChartPanel computingPowerPerMachineChart;

    /**
     * It represents the computing power per user through time chart, that is, a
     * chart containing information about the computing power use through time
     * for each user in a vertical bar view.
     */
    private ChartPanel computingPowerPerUserChart;

    /**
     * It represents the computing power per task through time chart, that is, a
     * chart containing information about the computing power use through time
     * for each task in a vertical bar view.
     */
    private ChartPanel computingPowerPerTaskChart;

    /**
     * Constructor which specifies the metrics. The processing and communication
     * charts are built while this constructor is initialized.
     *
     * @param metrics the metrics
     */
    public SimulationResultChartMaker(final Metricas metrics) {
        final var processingCharts
                = this.makeProcessingCharts(metrics.getMetricasProcessamento());
        final var communicationCharts
                = this.makeCommunicationCharts(metrics.getMetricasComunicacao());

        this.processingBarChart = processingCharts.getFirst();
        this.processingPieChart = processingCharts.getSecond();

        this.communicationBarChart = communicationCharts.getFirst();
        this.communicationPieChart = communicationCharts.getSecond();
    }

    /**
     * Constructor which specifies the metrics, queue network and the task list.
     * Moreover, all charts to be displayed at the results dialog will be built
     * in this constructor initialization.
     * <p>
     * It is supposed that metrics, queue network and task list are all not null.
     * Otherwise, {@link NullPointerException} instances may be thrown.
     *
     * @param metrics the metrics
     * @param queueNetwork the queue network
     * @param taskList the task list
     */
    public SimulationResultChartMaker(final Metricas metrics,
                                      final RedeDeFilas queueNetwork,
                                      final List<Tarefa> taskList) {
        this(metrics);

        final var computingPowerPerMachineChartInfo
                = this.makeComputingPowerPerMachineChart(queueNetwork);

        this.computingPowerPerMachineChart = computingPowerPerMachineChartInfo.getFirst();
        this.computingPowerPerUserChart = this.makeComputingPowerPerUserChart(taskList,
                computingPowerPerMachineChartInfo.getSecond(), queueNetwork);
        this.computingPowerPerTaskChart = this.makeComputingPowerPerTaskChart(taskList,
                computingPowerPerMachineChartInfo.getSecond());
    }

    /**
     * It builds the bar and pie processing charts; these charts are returned in
     * an instance of {@link Pair} containing the bar and pie chart, respectively.
     *
     * @param processingMap the processing map containing the processing metrics
     *                      for each machine
     *
     * @return an instance of {@link Pair} containing the bar and pie processing
     *         charts, respectively
     *
     * @throws NullPointerException if processing map is {@code null}
     */
    private Pair<ChartPanel, ChartPanel> makeProcessingCharts(
            final Map<String, MetricasProcessamento> processingMap) {
        /* It ensures that the processing map is not null */
        if (processingMap == null)
            throw new NullPointerException("processingMap is null." +
                    " It could not be possible generate the processing charts.");

        final var barChartData = new DefaultCategoryDataset();
        final var pieChartData = new DefaultPieDataset();

        /* Add the processing metrics information to both bar and pie charts. */
        for (final var processingMetrics : processingMap.values()) {
            final String name;
            final var processedMFlops = processingMetrics.getMFlopsProcessados();

            if (processingMetrics.getnumeroMaquina() == 0)
                name = processingMetrics.getId();
            else
                name = processingMetrics.getId() + " node " + processingMetrics.getnumeroMaquina();

            barChartData.addValue(processedMFlops, "vermelho", name);
            pieChartData.insertValue(0, name, processedMFlops);
        }

        final var barChart = ChartFactory.createBarChart(
                "Total processed in each resource", // Title
                "Resource",                              // X-axis title
                "MFlops",                                // Y-axis title
                barChartData,                            // Chart data
                PlotOrientation.VERTICAL,                // Chart orientation
                false,                                   // Legend
                false,                                   // Tooltips
                false                                    // URL
        );

        final var pieChart = ChartFactory.createPieChart(
                "Total processed in each resource", // Title
                pieChartData,                            // Chart data
                true,                                    // Legend
                false,                                   // Tooltips
                false                                    // URL
        );

        return makeBarPieChartPair(barChart, pieChart, processingMap);
    }

    /**
     * It builds the bar and pie communication charts; these charts are returned
     * in an instance of {@link Pair} containing the bar and pie chart, respectively.
     *
     * @param communicationMap the communication map containing the communication
     *                         metrics for each network link
     *
     * @return an instance of {@link Pair} containing the bar and pie processing
     *         charts, respectively
     *
     * @throws NullPointerException if communication map is {@code null}
     */
    private Pair<ChartPanel, ChartPanel> makeCommunicationCharts(
            final Map<String, MetricasComunicacao> communicationMap) {
        /* It ensures that the processing map is not null */
        if (communicationMap == null)
            throw new NullPointerException("communicationMap is null." +
                    " It could not be possible generate the communication charts.");

        final var barChartData = new DefaultCategoryDataset();
        final var pieChartData = new DefaultPieDataset();

        for (final var communicationMetrics : communicationMap.values()) {
            final var transmittedMBits = communicationMetrics.getMbitsTransmitidos();
            final var id = communicationMetrics.getId();

            barChartData.addValue(transmittedMBits, "vermelho", id);
            pieChartData.insertValue(0, id, transmittedMBits);
        }

        final var barChart = ChartFactory.createBarChart(
                "Total communication in each resource", // Title
                "Resource",                                  // X-axis title
                "Mbits",                                     // Y-axis title
                barChartData,                                // Chart data
                PlotOrientation.VERTICAL,                    // Chart orientation
                false,                                       // Legend
                false,                                       // Tooltips
                false                                        // URL
        );

        final var pieChart = ChartFactory.createPieChart(
                "Total communication in each resource", // Title
                pieChartData,                                // Chart data
                true,                                        // Legend
                false,                                       // Tooltips
                false                                        // URL
        );

        return makeBarPieChartPair(barChart, pieChart, communicationMap);
    }

    /**
     * It builds the bar chart for the computing power per machine results and
     * calculates the total computational power; these values are returned in
     * an instance of {@link Pair}, respectively.
     *
     * @param queueNetwork the queue network
     *
     * @return an instance of {@link Pair} containing the bar chart for the
     *         computing power per machine results and the total computational
     *         power, respectively
     */
    private Pair<ChartPanel, Double> makeComputingPowerPerMachineChart(
            final RedeDeFilas queueNetwork) {
        final var chartData = new XYSeriesCollection();
        var totalComputationalPower = 0.0;

        /* It checks if the amount of machines is greater than or equal to 21, then */
        /* the computing power per machine chart is not going to be returned, just */
        /* the total computational power. */
        if (queueNetwork.getMaquinas().size() >= 21) {
            for (final var machine : queueNetwork.getMaquinas())
                totalComputationalPower += machine.getPoderComputacional()
                        - machine.getOcupacao() * machine.getPoderComputacional();
            return new Pair<>(null, totalComputationalPower);
        }

        for (final var machine : queueNetwork.getMaquinas()) {
            /* The interval list in which the intervals represent the period of time */
            /* in which the machine was executing. */
            final var useIntervals = machine.getListaProcessamento();

            totalComputationalPower += machine.getPoderComputacional()
                    - machine.getOcupacao() * machine.getPoderComputacional();

            /* Did the machine execute something? */
            if (useIntervals.isEmpty())
                continue;

            final XYSeries xySeries;

            if (machine.getnumeroMaquina() == 0)
                xySeries = new XYSeries(machine.getId());
            else
                xySeries = new XYSeries(machine.getId() + " node " + machine.getnumeroMaquina());

            for (final var interval : useIntervals) {
                final var usePercentage = 100.0 - (machine.getOcupacao() * 100.0);

                xySeries.add(interval.getInicio(), usePercentage);
                xySeries.add(interval.getFim(), usePercentage);
            }

            chartData.addSeries(xySeries);
        }

        final var barChart = ChartFactory.createXYAreaChart(
                "Use of total computing power through time \nMachines", // Title
                "Time (seconds)",                                      // X-axis title
                "Rate of use of computing power for each node (%)",    // Y-axis title
                chartData,                                             // Chart data
                PlotOrientation.VERTICAL,                              // Chart orientation
                true,                                                  // Legend
                true,                                                  // Tooltips
                false                                                  // URL
        );

        final var barChartPanel = new ChartPanel(barChart);

        barChartPanel.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);

        return new Pair<>(barChartPanel, totalComputationalPower);
    }

    /**
     * It builds the bar chart for the computing power per task results.
     *
     * @param tasks the tasks used to calculate the results
     * @param totalComputingPower the total computational power
     *
     * @return the bar chart for the computing power per task results
     */
    private ChartPanel makeComputingPowerPerTaskChart(
            final Collection<? extends Tarefa> tasks,
            final double totalComputingPower) {
        /* Ensure the tasks is not null */
        if (tasks == null)
            throw new NullPointerException("tasks is null. " +
                    "It was not possible to build the computing power through time per task chart.");

        /* It checks if the amount of tasks is greater than or equal to 50, then */
        /* the computing power through time per task chart is not going to be returned. */
        if (tasks.size() >= 50)
            return null;

        final var chartData = new XYSeriesCollection();

        for (final var task : tasks) {
            final var xySeries = new XYSeries("task " + task.getIdentificador());
            final var serviceCenterProcessing = (CS_Processamento) task.getLocalProcessamento();

            if (serviceCenterProcessing == null)
                continue;

            final var userPercentage = serviceCenterProcessing.getPoderComputacional()
                    / totalComputingPower * 100.0;

            for (int j = 0; j < task.getTempoInicial().size(); j++) {
                final double startTime = task.getTempoInicial().get(j);
                final double endTime = task.getTempoFinal().get(j);

                xySeries.add(startTime, 0.0);
                xySeries.add(startTime, userPercentage);

                xySeries.add(endTime, userPercentage);
                xySeries.add(endTime, 0.0);
            }

            chartData.addSeries(xySeries);
        }

        final var barChart = ChartFactory.createXYAreaChart(
                "Use of total computing power through time \nTasks", // Title
                "Time (seconds)",                                         // X-axis title
                "Rate of use of computing power for each task (%)",       // Y-axis title
                chartData,                                                // Chart data
                PlotOrientation.VERTICAL,                                 // Chart orientation
                true,                                                     // Legend
                true,                                                     // Tooltips
                false                                                     // URL
        );

        final var barChartPanel = new ChartPanel(barChart);

        barChartPanel.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);

        return barChartPanel;
    }

    /**
     * It builds the bar chart for the computing power per user results.
     *
     * @param tasks the tasks used to calculate the results
     * @param totalComputationalPower the total computational power
     * @param queueNetwork the queue network
     *
     * @return the bar chart for the computing power per user results
     */
    private ChartPanel makeComputingPowerPerUserChart(
            final Collection<? extends Tarefa> tasks,
            final double totalComputationalPower,
            final RedeDeFilas queueNetwork) {
        final var userOperationTimeList = new ArrayList<UserOperationTime>();
        final var usersNumber = queueNetwork.getUsuarios().size();
        final var users = new HashMap<String, Integer>();

        final var usersXYSeries = new XYSeries[usersNumber];
        final var usersUtilization = new double[usersNumber];

        final var chartData = new XYSeriesCollection();

        /* Initialization of the above declared variables */
        for (int i = 0; i < usersNumber; i++) {
            final var user = queueNetwork.getUsuarios().get(i);

            users.put(user, i);

            usersUtilization[i] = 0;
            usersXYSeries[i] = new XYSeries(user);
        }

        /* Add each task as two points in the user operation time list */
        for (final var task : tasks) {
            final var serviceCenterProcessing
                    = (CS_Processamento) task.getLocalProcessamento();

            if (serviceCenterProcessing == null)
                continue;

            for (int i = 0; i < task.getTempoInicial().size(); i++) {
                final var startTime = task.getTempoInicial().get(i);
                final var endTime = task.getTempoFinal().get(i);

                final var usePercentage = task.getHistoricoProcessamento()
                        .get(i).getPoderComputacional() / totalComputationalPower * 100.0;
                final var owner = users.get(task.getProprietario());

                final var startUserOperationTime
                        = new UserOperationTime(startTime, true, usePercentage, owner);
                final var endUserOperationTime
                        = new UserOperationTime(endTime, false, usePercentage, owner);

                userOperationTimeList.add(startUserOperationTime);
                userOperationTimeList.add(endUserOperationTime);
            }
        }

        Collections.sort(userOperationTimeList);

        for (final var userOperationTime : userOperationTimeList) {
            final var userId = userOperationTime.getUserId();

            for (int i = userId; i < usersNumber; i++) {
                final var xySeries = usersXYSeries[i];
                var utilization = usersUtilization[i];

                /* Save previous values */
                xySeries.add(userOperationTime.getTime(), utilization);

                if (userOperationTime.isStartTime())
                    utilization += userOperationTime.getNodeUse();
                else
                    utilization -= userOperationTime.getNodeUse();

                /* Save the new value */
                xySeries.add(userOperationTime.getTime(), utilization);

                /* Update the user utilization */
                usersUtilization[i] = utilization;
            }
        }

        /* Add the series to the chart data for each user */
        for (int i = 0; i < usersNumber; i++)
            chartData.addSeries(usersXYSeries[i]);

        final var barChart = ChartFactory.createXYAreaChart(
                "Use of total computing power through time \nUsers", // Title
                "Time (seconds)",                                         // X-axis title
                "Rate of use of computing power for each user (%)",       // Y-axis title
                chartData,                                                // Chart data
                PlotOrientation.VERTICAL,                                 // Chart orientation
                true,                                                     // Legend
                true,                                                     // Tooltips
                false                                                     // URL
        );

        final var barChartPanel = new ChartPanel(barChart);

        barChartPanel.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);

        return barChartPanel;
    }

    /**
     * It returns an instance of {@link Pair} containing chart panels representing
     * the bar and pie chart, respectively. Moreover, for both charts the chart
     * preferred size is already set. Finally, if the amount of labels in the
     * bar chart domain axis is greater than {@code 10}, then the labels is
     * rotated {@code 45} degrees counterclockwise.
     *
     * @param barChart the bar chart
     * @param pieChart the pie chart
     * @param barChartMap the bar chart map containing the information that is
     *                    used to build the bar chart
     *
     * @return an instance of {@link Pair} containing chart panels representing
     *         the bar and pie chart, respectively
     */
    private static Pair<ChartPanel, ChartPanel> makeBarPieChartPair(final JFreeChart barChart,
                                                             final JFreeChart pieChart,
                                                             final Map<String, ?> barChartMap) {
        /* It rotates the bar chart's X-axis labels in 45 degrees. */
        if (barChartMap.size() > 10)
            barChart.getCategoryPlot().getDomainAxis()
                    .setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        final var barChartPanel = new ChartPanel(barChart);
        final var pieChartPanel = new ChartPanel(pieChart);

        barChartPanel.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);
        pieChartPanel.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);

        return new Pair<>(barChartPanel, pieChartPanel);
    }

    /* Getters */

    /**
     * It returns the processing bar chart. This chart contains the processing
     * results for each machine in a bar representation.
     *
     * @return the processing bar chart
     */
    public ChartPanel getProcessingBarChart() {
        return this.processingBarChart;
    }

    /**
     * It returns the processing pie chart. This chart contains the processing
     * results for each machine in a pie representation.
     *
     * @return the processing pie chart
     */
    public ChartPanel getProcessingPieChart() {
        return this.processingPieChart;
    }

    /**
     * It returns the communication bar chart. This chart contains the
     * communication results for each network link in a bar representation.
     *
     * @return the communication bar chart
     */
    public ChartPanel getCommunicationBarChart() {
        return this.communicationBarChart;
    }

    /**
     * It returns the communication pie chart. This chart contains the
     * communication results for each network link in a pie representation.
     *
     * @return the communication pie chart
     */
    public ChartPanel getCommunicationPieChart() {
        return this.communicationPieChart;
    }

    /**
     * It returns the computing power per machine chart. This chart contains the
     * computational power usage through time for each machine in a bar representation.
     *
     * @return the computing power per machine chart
     */
    public ChartPanel getComputingPowerPerMachineChart() {
        return this.computingPowerPerMachineChart;
    }

    /**
     * It returns the computing power per user chart. This chart contains the
     * computational power usage through time for each user in a bar representation.
     *
     * @return the computing power per user chart
     */
    public ChartPanel getComputingPowerPerUserChart() {
        return this.computingPowerPerUserChart;
    }

    /**
     * It returns the computing power per task chart. This chart contains the
     * computational power usage through time for each task in a bar representation.
     *
     * @return the computing power per task chart
     */
    public ChartPanel getComputingPowerPerTaskChart() {
        return this.computingPowerPerTaskChart;
    }
}
