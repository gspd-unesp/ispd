package ispd.gui;

import ispd.arquivo.xml.IconicoXML;
import ispd.gui.results.ResultsDialog;
import ispd.gui.utils.ButtonBuilder;
import ispd.motor.ProgressoSimulacao;
import ispd.motor.SimulacaoSequencial;
import ispd.motor.SimulacaoSequencialCloud;
import ispd.motor.Simulation;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.RedeDeFilasCloud;
import ispd.motor.filas.Tarefa;
import ispd.motor.metricas.Metricas;
import org.w3c.dom.Document;

import javax.swing.DebugGraphics;
import javax.swing.GroupLayout;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Makes calls to simulation engine.
 * Presents the steps taken so far and simulation progress (%).
 */
public class SimulationDialog extends JDialog implements Runnable {
    private static final Font ARIAL_FONT_BOLD =
            new Font("Arial", Font.BOLD, 12);
    private final MutableAttributeSet colorConfig = new SimpleAttributeSet();
    private final String modelAsText;
    private final Document model;
    private final ResourceBundle translator;
    private final ProgressoSimulacao progressTracker =
            new BasicProgressTracker();
    private final int gridOrCloud;
    private JProgressBar progressBar;
    private JTextPane notificationArea;
    private Thread simThread = null;
    private int progressPercent = 0;

    SimulationDialog(final Frame parent, final boolean modal, final Document model,
                     final String modelAsText, final ResourceBundle translator,
                     final int gridOrCloud) {
        super(parent, modal);
        this.translator = translator;
        this.gridOrCloud = gridOrCloud;
        this.model = model;
        this.modelAsText = modelAsText;
        this.initComponents();
        this.addWindowListener(new SomeWindowAdapter());
    }

    private void initComponents() {
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setTitle(this.translate("Running Simulation"));

        this.progressBar = new JProgressBar();
        this.progressBar.setDebugGraphicsOptions(DebugGraphics.NONE_OPTION);

        this.notificationArea = new JTextPane();
        this.notificationArea.setEditable(false);
        this.notificationArea.setFont(SimulationDialog.ARIAL_FONT_BOLD);

        this.makeLayoutAndPack();
    }

    private String translate(final String word) {
        return this.translator.getString(word);
    }

    private void onCancel(final ActionEvent evt) {
        if (this.simThread != null) {
            this.simThread = null;
        }
        this.dispose();
    }

    private void makeLayoutAndPack() {
        final var scrollPane = new JScrollPane(this.notificationArea);
        final var cancelButton = ButtonBuilder.basicButton(
                this.translate("Cancel"), this::onCancel);

        final var layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING,
                                layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                .addComponent(cancelButton,
                                                        GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                                                .addComponent(scrollPane,
                                                        GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                                                .addComponent(this.progressBar,
                                                        GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE))
                                        .addContainerGap())
        );

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING,
                                layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(scrollPane,
                                                GroupLayout.DEFAULT_SIZE,
                                                227, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(this.progressBar,
                                                GroupLayout.PREFERRED_SIZE,
                                                42, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cancelButton,
                                                GroupLayout.PREFERRED_SIZE,
                                                41, GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap())
        );

        this.pack();
    }

    void iniciarSimulacao() {
        this.simThread = new Thread(this);
        this.simThread.start();
    }

    @Override
    public void run() {
        this.progressTracker.println("Simulation Initiated.");
        try {
            //0%
            //Verifica se foi construido modelo na area de desenho
            this.progressTracker.validarInicioSimulacao(this.model);//[5%] --> 5%
            //Constrói e verifica modelos icônicos e simuláveis
            this.progressTracker.AnalisarModelos(this.modelAsText);//[20%] --> 25%
            //criar grade
            this.progressTracker.print("Mounting network queue.");
            this.progressTracker.print(" -> ");
            List<Tarefa> tasks = null;
            if (this.gridOrCloud == PickModelTypeDialog.GRID) {
                final RedeDeFilas queueNetwork =
                        IconicoXML.newRedeDeFilas(this.model);
                this.incrementProgress(10);//[10%] --> 35%
                this.progressTracker.println("OK", Color.green);
                //criar tarefas
                this.progressTracker.print("Creating tasks.");
                this.progressTracker.print(" -> ");
                tasks =
                        IconicoXML.newGerarCarga(this.model).makeTaskList(queueNetwork);
                this.incrementProgress(10);//[10%] --> 45%
                this.progressTracker.println("OK", Color.green);
                //Verifica recursos do modelo e define roteamento
                final Simulation sim = new SimulacaoSequencial(this.progressTracker,
                        queueNetwork, tasks);//[10%] --> 55 %
                //Realiza asimulação
                this.progressTracker.println("Simulating.");
                //recebe instante de tempo em milissegundos ao iniciar a
                // simulação
                final double t1 = System.currentTimeMillis();

                sim.simulate();//[30%] --> 85%

                //Recebe instnte de tempo em milissegundos ao fim da execução
                // da simulação
                final double t2 = System.currentTimeMillis();
                //Calcula tempo de simulação em segundos
                final double tempototal = (t2 - t1) / 1000;
                //Obter Resultados
                final Metricas metrica = sim.getMetrics();
                //[5%] --> 90%
                //Apresentar resultados
                this.progressTracker.print("Showing results.");
                this.progressTracker.print(" -> ");
                final Window janelaResultados = new ResultsDialog(null, metrica, queueNetwork, tasks);
                this.incrementProgress(10);//[10%] --> 100%
                this.progressTracker.println("OK", Color.green);
                this.progressTracker.println("Simulation Execution Time = " + tempototal + "seconds");
                janelaResultados.setLocationRelativeTo(this);
                janelaResultados.setVisible(true);

            } else if (this.gridOrCloud == PickModelTypeDialog.IAAS) {
                final RedeDeFilasCloud cloudQueueNetwork =
                        IconicoXML.newRedeDeFilasCloud(this.model);
                this.incrementProgress(10);//[10%] --> 35%
                this.progressTracker.println("OK", Color.green);
                //criar tarefas
                this.progressTracker.print("Creating tasks.");
                this.progressTracker.print(" -> ");
                tasks =
                        IconicoXML.newGerarCarga(this.model).makeTaskList(cloudQueueNetwork);
                this.incrementProgress(10);//[10%] --> 45%
                this.progressTracker.println("OK", Color.green);
                //Verifica recursos do modelo e define roteamento
                final Simulation sim =
                        new SimulacaoSequencialCloud(this.progressTracker,
                                cloudQueueNetwork, tasks);//[10%]
                // --> 55 %
                //Realiza asimulação
                this.progressTracker.println("Simulating.");
                //recebe instante de tempo em milissegundos ao iniciar a
                // simulação
                final double t1 = System.currentTimeMillis();

                sim.simulate();//[30%] --> 85%

                //Recebe instnte de tempo em milissegundos ao fim da execução
                // da simulação
                final double t2 = System.currentTimeMillis();
                //Calcula tempo de simulação em segundos
                final double tempototal = (t2 - t1) / 1000;
                //Obter Resultados
                final Metricas metrica = sim.getCloudMetrics();
                //[5%] --> 90%
                //Apresentar resultados
                this.progressTracker.print("Showing results.");
                this.progressTracker.print(" -> ");
                final Window janelaResultados = new CloudResultsDialog(null
                        , metrica, cloudQueueNetwork, tasks);
                this.incrementProgress(10);//[10%] --> 100%
                this.progressTracker.println("OK", Color.green);
                this.progressTracker.println("Simulation Execution Time = " + tempototal + "seconds");
                janelaResultados.setLocationRelativeTo(this);
                janelaResultados.setVisible(true);
            }
        } catch (final IllegalArgumentException erro) {

            Logger.getLogger(SimulationDialog.class.getName()).log(Level.SEVERE,
                    null, erro);
            this.progressTracker.println(erro.getMessage(), Color.red);
            this.progressTracker.print("Simulation Aborted", Color.red);
            this.progressTracker.println("!", Color.red);
        }
    }

    private void incrementProgress(final int add) {
        this.progressPercent += add;
        this.progressBar.setValue(this.progressPercent);
    }

    private class SomeWindowAdapter extends WindowAdapter {
        @Override
        public void windowClosing(final WindowEvent e) {
            
            SimulationDialog.this.simThread = null;
            SimulationDialog.this.dispose();
        }
    }

    private class BasicProgressTracker extends ProgressoSimulacao {
        @Override
        public void incProgresso(final int n) {
            SimulationDialog.this.incrementProgress(n);
        }

        @Override
        public void print(final String text, final Color cor) {
            try {
                final var doc = SimulationDialog.this.notificationArea.getDocument();
                final var config = SimulationDialog.this.colorConfig;

                StyleConstants.setForeground(
                        config,
                        Optional.ofNullable(cor).orElse(Color.black)
                );

                doc.insertString(
                        doc.getLength(),
                        this.tryTranslate(text),
                        config
                );

            } catch (final BadLocationException ex) {
                Logger.getLogger(SimulationDialog.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }

        private String tryTranslate(final String text) {
            if (!SimulationDialog.this.translator.containsKey(text))
                return text;
            return SimulationDialog.this.translate(text);
        }
    }
}