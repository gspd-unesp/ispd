package ispd.gui.results;

import ispd.gui.auxiliar.SimulationResultChartMaker;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.metricas.Metricas;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.util.List;

/**
 * A {@link ResultsDialog} class is used to display a window containing all
 * types of information from the performed simulation.
 */
public class ResultsDialog extends JDialog {

    public static final Dimension CHART_PREFERRED_SIZE
            = new Dimension(600, 400);

    public static final Font COURIER_NEW_FONT_BOLD
            = new Font("Courier New", Font.BOLD, 14);

    private final Metricas metrics;
    private final RedeDeFilas queueNetwork;
    private final List<Tarefa> tasks;
    private final SimulationResultChartMaker charts;

    /**
     * Constructor which specifies the frame who is owner of this result dialog,
     * the metrics, the queue network and the task list, these three last variables
     * are used to construct the results to be displayed in this dialog.
     *
     * @param owner the owner
     * @param metrics the metrics
     * @param queueNetwork the queue network
     * @param tasks the task list
     *
     * @throws NullPointerException if metrics, queue network or task list are
     *                              {@code null}
     */
    public ResultsDialog(final Frame owner,
                         final Metricas metrics,
                         final RedeDeFilas queueNetwork,
                         final List<Tarefa> tasks) {
        super(owner, true);

        /* Ensure the metrics is not null */
        if (metrics == null)
            throw new NullPointerException("metrics is null. It was not possible to show the results.");

        /* Ensure the queue network is not null */
        if (queueNetwork == null)
            throw new NullPointerException("queue network is null. It was not possible to show the results.");

        /* Ensure the task list is not null */
        if (tasks == null)
            throw new NullPointerException("task list is null. It was not possible to show the results.");

        this.metrics = metrics;
        this.queueNetwork = queueNetwork;
        this.tasks = tasks;
        this.charts = new SimulationResultChartMaker(metrics, queueNetwork, tasks);

        this.initDialogComponents();
        this.pack();
    }

    /* Private Methods */
    /* Dialog Related Methods */

    /**
     * It initializes all components that is going to be displayed in this dialog.
     */
    private void initDialogComponents() {
        final var mainPane = new JTabbedPane();
        final var globalPane = new ResultsGlobalPane(this.metrics, this.charts, this.tasks);
        final var tasksPane = new ResultsTasksPane(this.metrics);
        final var usersPane = new ResultsUsersPane(this.queueNetwork);
        final var resourcesPane = new ResultsResourcePane(this.metrics);
        final var processingPane = new ResultsProcessingPane(this.charts);
        final var communicationPane = new ResultsCommunicationPane(this.charts);
        final var computingPowerPane = new ResultsComputingPowerPane(this.charts);

        mainPane.addTab("Global", globalPane);
        mainPane.addTab("Tasks", tasksPane);
        mainPane.addTab("Users", usersPane);
        mainPane.addTab("Resources", resourcesPane);
        mainPane.addTab("Chart of the processing", processingPane);
        mainPane.addTab("Chart of the communication", communicationPane);
        mainPane.addTab("Use of computing power through time", computingPowerPane);

        this.add(mainPane);
    }

}
