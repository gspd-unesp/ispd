package ispd.gui.results;

import ispd.motor.metricas.Metricas;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * A {@link ResultsTasksPane} is a class that represents a pane containing the
 * information about processing and communication performed for resolve the
 * tasks.
 *
 * @see ResultsUsersPane for see the information about processing and communication
 *                       performed for resolve the tasks for each user
 */
/* package-private */ class ResultsTasksPane extends JScrollPane {

    /**
     * Constructor which creates a pane that contains information about the
     * processing and communication performed for the task completion.
     *
     * @param metrics the simulation metrics
     */
    public ResultsTasksPane(final Metricas metrics) {
        final var textArea = new JTextArea();

        this.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);
        this.setViewportView(textArea);

        textArea.setEditable(false);
        textArea.setColumns(20);
        textArea.setRows(5);
        textArea.setFont(ResultsDialog.COURIER_NEW_FONT_BOLD);
        textArea.setText(this.makeTasksResultsText(metrics));
    }

    /* Private Methods */
    /* Utility Tasks Pane Methods */

    /**
     * It creates the task results text, this text contains the general results
     * for processing and communication for the tasks.
     *
     * @return the task results text
     */
    private String makeTasksResultsText(final Metricas metrics) {
        final var sb = new StringBuilder();
        final var avgSystemCommunicationTime =
                metrics.getTempoMedioFilaComunicacao() + metrics.getTempoMedioComunicacao();
        final var avgSystemProcessingTime =
                metrics.getTempoMedioFilaProcessamento() + metrics.getTempoMedioProcessamento();

        /* Communication task information */
        sb.append("\n Communication \n");
        sb.append(String.format("    Queue average time: %g seconds.\n", metrics.getTempoMedioFilaComunicacao()));
        sb.append(String.format("    Communication average time: %g seconds.\n", metrics.getTempoMedioComunicacao()));
        sb.append(String.format("    System average time: %g seconds.\n", avgSystemCommunicationTime));

        /* Processing task information */
        sb.append("\n Processing \n");
        sb.append(String.format("    Queue average time: %g seconds.\n", metrics.getTempoMedioFilaProcessamento()));
        sb.append(String.format("    Processing average time: %g seconds.\n", metrics.getTempoMedioProcessamento()));
        sb.append(String.format("    System average time: %g seconds.\n", avgSystemProcessingTime));

        if (metrics.hasCancelledTasks()) {
            sb.append("\n Tasks Cancelled \n");
            sb.append(String.format("    Number: %d \n", metrics.getNumTarefasCanceladas()));
            sb.append(String.format("    Wasted processing: %g Mflops", metrics.getMflopsDesperdicio()));
        }

        return sb.toString();
    }
}
