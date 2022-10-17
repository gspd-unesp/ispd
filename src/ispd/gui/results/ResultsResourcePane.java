package ispd.gui.results;

import ispd.motor.metricas.Metricas;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * A {@link ResultsResourcePane} is a class that represents a pane containing
 * the information about all the processing and communication performed of each
 * simulated component.
 */
/* package-private */ class ResultsResourcePane extends JScrollPane {

    /**
     * Constructor which creates a pane in which the performed results in total
     * for each machine and each network link are shown in a tabular view.
     *
     * @param metrics the simulation metrics
     */
    public ResultsResourcePane(final Metricas metrics) {
        final var table = new JTable();
        final var columns = new Object[] {
                "Label", "Owner", "Processing performed", "Communication performed"
        };

        this.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);
        this.setViewportView(table);

        table.setModel(new DefaultTableModel(metrics.makeResourceTable(), columns));
    }
}
