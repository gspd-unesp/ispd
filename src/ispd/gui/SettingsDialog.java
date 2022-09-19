package ispd.gui;

import ispd.arquivo.xml.ConfiguracaoISPD;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;

class SettingsDialog extends JDialog {
    private final ConfiguracaoISPD config;
    private JCheckBox jCheckBoxBarChartProcessing;
    private JCheckBox jCheckBoxPieChartCommunication;
    private JCheckBox jCheckBoxTimeChartMachine;
    private JCheckBox jCheckBoxTimeChartTask;
    private JCheckBox jCheckBoxTimeChartUser;
    private JLabel jLabelNumSim;
    private JLabel jLabelThread;
    private JRadioButton jRadioButtonDefault;
    private JRadioButton jRadioButtonGraphical;
    private JRadioButton jRadioButtonOptimistic;
    private JFormattedTextField jTextFieldNumSim;
    private JFormattedTextField jTextFieldThread;

    SettingsDialog(final Frame parent, final boolean modal,
                   final ConfiguracaoISPD config) {
        super(parent, modal);
        this.config = config;
        this.initComponents();
        this.setSimulationModel(config);
        this.jTextFieldNumSim.setValue(config.getNumberOfSimulations());
        this.jTextFieldThread.setValue(config.getNumberOfThreads());
        this.jCheckBoxBarChartProcessing.setSelected(config.getCreateProcessingChart());
        this.jCheckBoxPieChartCommunication.setSelected(config.getCreateCommunicationChart());
        this.jCheckBoxTimeChartUser.setSelected(config.getCreateUserThroughTimeChart());
        this.jCheckBoxTimeChartMachine.setSelected(config.getCreateMachineThroughTimeChart());
        this.jCheckBoxTimeChartTask.setSelected(config.getCreateTaskThroughTimeChart());
    }

    private void setSimulationModel(final ConfiguracaoISPD configuration) {
        switch (configuration.getSimulationMode()) {
            case ConfiguracaoISPD.DEFAULT ->
                    this.jRadioButtonDefaultActionPerformed(null);
            case ConfiguracaoISPD.OPTIMISTIC ->
                    this.jRadioButtonOptimisticActionPerformed(null);
            case ConfiguracaoISPD.GRAPHICAL ->
                    this.jRadioButtonGraphicalActionPerformed(null);
        }
    }

    private void initComponents() {
        this.jRadioButtonDefault = new JRadioButton();
        this.jRadioButtonOptimistic = new JRadioButton();
        this.jRadioButtonGraphical = new JRadioButton();
        this.jLabelThread = new JLabel();
        this.jLabelNumSim = new JLabel();
        this.jTextFieldThread = new JFormattedTextField();
        this.jTextFieldNumSim = new JFormattedTextField();
        final JPanel jPanelResults = new JPanel();
        this.jCheckBoxBarChartProcessing = new JCheckBox();
        this.jCheckBoxPieChartCommunication = new JCheckBox();
        this.jCheckBoxTimeChartUser = new JCheckBox();
        this.jCheckBoxTimeChartMachine = new JCheckBox();
        this.jCheckBoxTimeChartTask = new JCheckBox();

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final JPanel jPanelSimulation = new JPanel();
        jPanelSimulation.setBorder(BorderFactory.createTitledBorder(
                "Simulation"));

        this.jRadioButtonDefault.setSelected(true);
        this.jRadioButtonDefault.setText("Default");
        this.jRadioButtonDefault.addActionListener(this::jRadioButtonDefaultActionPerformed);

        this.jRadioButtonOptimistic.setText("Optimistic parallel (trial)");
        this.jRadioButtonOptimistic.addActionListener(this::jRadioButtonOptimisticActionPerformed);

        this.jRadioButtonGraphical.setText("Graphical");
        this.jRadioButtonGraphical.addActionListener(this::jRadioButtonGraphicalActionPerformed);

        this.jLabelThread.setHorizontalAlignment(SwingConstants.CENTER);
        this.jLabelThread.setText("Number of threads");

        this.jLabelNumSim.setHorizontalAlignment(SwingConstants.CENTER);
        this.jLabelNumSim.setText("Number of simulations");

        this.jTextFieldThread.setBackground(new Color(238, 238, 238));
        this.jTextFieldThread.setBorder(null);
        this.jTextFieldThread.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0"))));
        this.jTextFieldThread.setHorizontalAlignment(SwingConstants.CENTER);
        this.jTextFieldThread.setText("1");
        this.jTextFieldThread.addActionListener(this::jTextFieldThreadActionPerformed);
        this.jTextFieldThread.addFocusListener(new SomeFocusAdapter());

        this.jTextFieldNumSim.setBackground(new Color(238, 238, 238));
        this.jTextFieldNumSim.setBorder(null);
        this.jTextFieldNumSim.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0"))));
        this.jTextFieldNumSim.setHorizontalAlignment(SwingConstants.CENTER);
        this.jTextFieldNumSim.setText("1");
        this.jTextFieldNumSim.addActionListener(this::jTextFieldNumSimActionPerformed);
        this.jTextFieldNumSim.addFocusListener(new SomeOtherFocusAdapter());

        final GroupLayout jPanelSimulationLayout =
                new GroupLayout(jPanelSimulation);
        jPanelSimulation.setLayout(jPanelSimulationLayout);
        jPanelSimulationLayout.setHorizontalGroup(
                jPanelSimulationLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelSimulationLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanelSimulationLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanelSimulationLayout.createSequentialGroup()
                                                .addComponent(this.jRadioButtonDefault)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(this.jRadioButtonOptimistic)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(this.jRadioButtonGraphical))
                                        .addGroup(jPanelSimulationLayout.createSequentialGroup()
                                                .addGroup(jPanelSimulationLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(this.jLabelThread, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(this.jLabelNumSim, GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanelSimulationLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(this.jTextFieldThread, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(this.jTextFieldNumSim, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE))))
                                .addContainerGap(12, Short.MAX_VALUE))
        );
        jPanelSimulationLayout.setVerticalGroup(
                jPanelSimulationLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelSimulationLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanelSimulationLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jRadioButtonDefault)
                                        .addComponent(this.jRadioButtonOptimistic)
                                        .addComponent(this.jRadioButtonGraphical))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanelSimulationLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(this.jLabelThread)
                                        .addComponent(this.jTextFieldThread,
                                                GroupLayout.PREFERRED_SIZE,
                                                15, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanelSimulationLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jLabelNumSim)
                                        .addComponent(this.jTextFieldNumSim,
                                                GroupLayout.PREFERRED_SIZE,
                                                15, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        jPanelResults.setBorder(BorderFactory.createTitledBorder("Results"));

        this.jCheckBoxBarChartProcessing.setSelected(true);
        this.jCheckBoxBarChartProcessing.setText("Chart of Processing");
        this.jCheckBoxBarChartProcessing.addActionListener(this::jCheckBoxBarChartProcessingActionPerformed);

        this.jCheckBoxPieChartCommunication.setSelected(true);
        this.jCheckBoxPieChartCommunication.setText("Chart of Communication");
        this.jCheckBoxPieChartCommunication.addActionListener(this::jCheckBoxPieChartCommunicationActionPerformed);

        this.jCheckBoxTimeChartUser.setSelected(true);
        this.jCheckBoxTimeChartUser.setText("User computing through time");
        this.jCheckBoxTimeChartUser.addActionListener(this::jCheckBoxTimeChartUserActionPerformed);

        this.jCheckBoxTimeChartMachine.setText("Machine use through time");
        this.jCheckBoxTimeChartMachine.addActionListener(this::jCheckBoxTimeChartMachineActionPerformed);

        this.jCheckBoxTimeChartTask.setText("Tasks through time");
        this.jCheckBoxTimeChartTask.addActionListener(this::jCheckBoxTimeChartTaskActionPerformed);

        final GroupLayout jPanelResultsLayout = new GroupLayout(jPanelResults);
        jPanelResults.setLayout(jPanelResultsLayout);
        jPanelResultsLayout.setHorizontalGroup(
                jPanelResultsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelResultsLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanelResultsLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(this.jCheckBoxTimeChartUser, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(this.jCheckBoxPieChartCommunication, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(this.jCheckBoxBarChartProcessing, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(this.jCheckBoxTimeChartMachine, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(this.jCheckBoxTimeChartTask, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap(158, Short.MAX_VALUE))
        );
        jPanelResultsLayout.setVerticalGroup(
                jPanelResultsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelResultsLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(this.jCheckBoxBarChartProcessing)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(this.jCheckBoxPieChartCommunication)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(this.jCheckBoxTimeChartUser)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(this.jCheckBoxTimeChartMachine)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(this.jCheckBoxTimeChartTask)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE))
        );

        final GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(jPanelSimulation,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(jPanelResults,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanelSimulation,
                                        GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanelResults,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE)
                                .addContainerGap())
        );

        this.pack();
    }

    private void jRadioButtonDefaultActionPerformed(final ActionEvent evt) {
        this.jRadioButtonDefault.setSelected(true);
        this.jRadioButtonOptimistic.setSelected(false);
        this.jRadioButtonGraphical.setSelected(false);
        this.textFieldEnable(true);
        this.config.setSimulationMode(ConfiguracaoISPD.DEFAULT);
    }

    private void jRadioButtonOptimisticActionPerformed(final ActionEvent evt) {
        this.jRadioButtonDefault.setSelected(false);
        this.jRadioButtonOptimistic.setSelected(true);
        this.jRadioButtonGraphical.setSelected(false);
        this.textFieldEnable(true);
        this.config.setSimulationMode(ConfiguracaoISPD.OPTIMISTIC);
    }

    private void jRadioButtonGraphicalActionPerformed(final ActionEvent evt) {
        this.jRadioButtonDefault.setSelected(false);
        this.jRadioButtonOptimistic.setSelected(false);
        this.jRadioButtonGraphical.setSelected(true);
        this.textFieldEnable(false);
        this.config.setSimulationMode(ConfiguracaoISPD.GRAPHICAL);
    }

    private void jTextFieldThreadFocusLost(final FocusEvent evt) {
        try {
            final int valor = Integer.parseInt(this.jTextFieldThread.getText());
            if (valor > 0) {
                this.config.setNumberOfThreads(valor);
            } else {
                this.jTextFieldThread.setValue(this.config.getNumberOfThreads());
            }
        } catch (final NumberFormatException ex) {
            this.jTextFieldThread.setValue(this.config.getNumberOfThreads());
        }
    }

    private void jTextFieldThreadActionPerformed(final ActionEvent evt) {
        this.jTextFieldThread.transferFocus();
    }

    private void jTextFieldNumSimFocusLost(final FocusEvent evt) {
        try {
            final int valor = Integer.parseInt(this.jTextFieldNumSim.getText());
            if (valor > 0) {
                this.config.setNumberOfSimulations(valor);
            } else {
                this.jTextFieldNumSim.setValue(this.config.getNumberOfSimulations());
            }
        } catch (final NumberFormatException ex) {
            this.jTextFieldNumSim.setValue(this.config.getNumberOfSimulations());
        }
    }

    private void jTextFieldNumSimActionPerformed(final ActionEvent evt) {
        this.jTextFieldNumSim.transferFocus();
    }

    private void jCheckBoxBarChartProcessingActionPerformed(final ActionEvent evt) {
        this.config.setCreateProcessingChart(this.jCheckBoxBarChartProcessing.isSelected());
    }

    private void jCheckBoxPieChartCommunicationActionPerformed(final ActionEvent evt) {
        this.config.setCreateCommunicationChart(this.jCheckBoxPieChartCommunication.isSelected());
    }

    private void jCheckBoxTimeChartUserActionPerformed(final ActionEvent evt) {
        this.config.setCreateUserThroughTimeChart(this.jCheckBoxTimeChartUser.isSelected());
    }

    private void jCheckBoxTimeChartMachineActionPerformed(final ActionEvent evt) {
        this.config.setCreateMachineThroughTimeChart(this.jCheckBoxTimeChartMachine.isSelected());
    }

    private void jCheckBoxTimeChartTaskActionPerformed(final ActionEvent evt) {
        this.config.setCreateTaskThroughTimeChart(this.jCheckBoxTimeChartTask.isSelected());
    }

    private void textFieldEnable(final boolean enabled) {
        this.jTextFieldThread.setEnabled(enabled);
        this.jLabelThread.setEnabled(enabled);
        this.jTextFieldNumSim.setEnabled(enabled);
        this.jLabelNumSim.setEnabled(enabled);
    }

    private class SomeFocusAdapter extends FocusAdapter {
        public void focusLost(final FocusEvent evt) {
            SettingsDialog.this.jTextFieldThreadFocusLost(evt);
        }
    }

    private class SomeOtherFocusAdapter extends FocusAdapter {
        public void focusLost(final FocusEvent evt) {
            SettingsDialog.this.jTextFieldNumSimFocusLost(evt);
        }
    }
}