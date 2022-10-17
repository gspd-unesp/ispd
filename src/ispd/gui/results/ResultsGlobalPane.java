package ispd.gui.results;

import ispd.arquivo.SalvarResultadosHTML;
import ispd.arquivo.xml.TraceXML;
import ispd.gui.auxiliar.HtmlPane;
import ispd.gui.auxiliar.MultipleExtensionFileFilter;
import ispd.gui.auxiliar.SimulationResultChartMaker;
import ispd.motor.filas.Tarefa;
import ispd.motor.metricas.Metricas;
import ispd.motor.metricas.MetricasGlobais;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * A {@link ResultsGlobalPane} is a class used to display the general results
 * from the performed simulation.
 */
/* package-private */ class ResultsGlobalPane extends JScrollPane {

    private final SimulationResultChartMaker charts;
    private final List<Tarefa> tasks;
    private final SalvarResultadosHTML html;

    /**
     * Constructor which creates a pane that contains information about the
     * general simulation results.
     *
     * @param metrics the simulation metrics
     */
    public ResultsGlobalPane(final Metricas metrics,
                             final SimulationResultChartMaker charts,
                             final List<Tarefa> tasks) {
        this.html = new SalvarResultadosHTML();
        this.charts = charts;
        this.tasks = tasks;

        this.html.setMetricasTarefas(metrics);
        this.html.setMetricasGlobais(metrics.getMetricasGlobais());
        this.html.setTabela(metrics.makeResourceTable());

        final var prePane = new JPanel();
        final var toolbar = new JToolBar();
        final var textArea = new JTextArea();

        this.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);
        this.setViewportView(prePane);

        prePane.setLayout(new BoxLayout(prePane, BoxLayout.Y_AXIS));
        prePane.add(toolbar);
        prePane.add(textArea);

        toolbar.setRollover(true);
        toolbar.setFloatable(false);
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
        toolbar.add(this.makeSaveHtmlButton());
        toolbar.add(this.makeSaveTracesButton());

        textArea.setEditable(false);
        textArea.setColumns(20);
        textArea.setRows(5);
        textArea.setFont(ResultsDialog.COURIER_NEW_FONT_BOLD);
        textArea.setText(this.makeGlobalResultsText(metrics.getMetricasGlobais()));
    }

    /* Private Methods */
    /* Utility Global Pane Methods */

    /**
     * It creates the global results text, this text contains the general simulation
     * results and is shown in the global pane.
     *
     * @param globalMetrics the simulation global metrics
     *
     * @return the global results text
     */
    private String makeGlobalResultsText(final MetricasGlobais globalMetrics) {
        final var sb = new StringBuilder();

        sb.append("\t\tSimulation Results\n\n");
        sb.append(String.format("\tTotal Simulated Time = %g\n", globalMetrics.getTempoSimulacao()));
        sb.append(String.format("\tSatisfaction = %g %%\n", globalMetrics.getSatisfacaoMedia()));
        sb.append(String.format("\tIdleness of processing resources = %g %%\n", globalMetrics.getOciosidadeComputacao()));
        sb.append(String.format("\tIdleness of communication resources = %g %%\n", globalMetrics.getOciosidadeComunicacao()));
        sb.append(String.format("\tEfficiency = %g %%\n", globalMetrics.getEficiencia()));

        final var efficiency = globalMetrics.getEficiencia();

        if (efficiency > 70.0d)
            sb.append("\tEfficiency GOOD\n");
        else if (efficiency > 40.0)
            sb.append("\tEfficiency MEDIUM\n");
        else
            sb.append("\tEfficiency BAD\n");

        return sb.toString();
    }

    /* Button Creation Methods */

    /**
     * It creates and initializes the button that is used to save the simulation
     * results in an HTML file.
     *
     * @return a button used to save the simulation results in an HTML file
     */
    private JButton makeSaveHtmlButton() {
        final var button = new JButton();

        //noinspection ConstantConditions
        button.setIcon(new ImageIcon(this.getClass().getResource("/ispd/gui/imagens/document-save_1.png")));
        button.setToolTipText("Save results as HTML");
        button.setFocusable(false);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.addActionListener(this.makeSaveHtmlAction());

        return button;
    }

    /**
     * It creates and initializes the traces button that is used to save the
     * simulation traces in a file.
     *
     * @return a button used to save the simulation traces in a file
     */
    private JButton makeSaveTracesButton() {
        final var button = new JButton();

        button.setText("Save traces");
        button.setToolTipText("Save a trace file of simulation");
        button.setFocusable(false);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.addActionListener(this.makeSaveTracesAction());

        return button;
    }

    /* Button Click Handling Methods */

    /**
     * It creates an instance of {@link ActionListener} to handle the click event
     * on the {@code Save HTML} button. This button when is clicked save all
     * simulation results in an HTML file.
     *
     * @return an instance of {@link ActionListener} to handle the click event
     *         on the {@code Save HTML} button.
     */
    private ActionListener makeSaveHtmlAction() {
        return (e) -> {
            final var fileChooser = new JFileChooser();
            final var stateChooser = fileChooser.showSaveDialog(this);

            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            switch (stateChooser) {
                case JFileChooser.APPROVE_OPTION -> {
                    final var selectedFile = fileChooser.getSelectedFile();
                    final URL url;

                    try {
                        url = new URL("file://" + selectedFile.getAbsolutePath() + "/result.html");
                    } catch (final MalformedURLException ex) {
                        ex.printStackTrace();
                        return;
                    }

                    this.html.saveHtml(selectedFile, this.charts);

                    /* Open the default browser to the selected URL */
                    HtmlPane.openDefaultBrowser(url);
                }
                case JFileChooser.ERROR_OPTION -> throw new IllegalStateException("An unexpected error has occurred." +
                        "Please contact the project administrators.");
            }
        };
    }

    /**
     * It creates an instance of {@link ActionListener} to handle the click event
     * on the {@code Save Traces} button. This button when is clicked save all
     * simulation traces in a file.
     *
     * @return an instance of {@link ActionListener} to handle the click event
     *         on the {@code Save Traces} button.
     */
    private ActionListener makeSaveTracesAction() {
        return (e) -> {
            final var extension = ".wmsx";
            final var fileChooser = new JFileChooser();
            final var stateChooser = fileChooser.showSaveDialog(this);

            switch (stateChooser) {
                case JFileChooser.APPROVE_OPTION -> {
                    var selectedFile = fileChooser.getSelectedFile();

                    if (!selectedFile.getName().endsWith(extension))
                        selectedFile = new File(selectedFile + extension);

                    new TraceXML(selectedFile.getAbsolutePath())
                            .geraTraceSim(this.tasks);
                }
                case JFileChooser.ERROR_OPTION -> throw new IllegalStateException("An unexpected error has occurred." +
                        " Please contact the project administrators.");
            }
        };
    }

    /* Utility Button Methods */

}
