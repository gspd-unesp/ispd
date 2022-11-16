package ispd.gui;

import ispd.arquivo.xml.TraceXML;
import ispd.gui.auxiliar.MultipleExtensionFileFilter;
import ispd.motor.workload.WorkloadGenerator;
import ispd.motor.workload.WorkloadGeneratorType;
import ispd.motor.workload.impl.CollectionWorkloadGenerator;
import ispd.motor.workload.impl.GlobalWorkloadGenerator;
import ispd.motor.workload.impl.PerNodeWorkloadGenerator;
import ispd.motor.workload.impl.TraceFileWorkloadGenerator;
import ispd.utils.SequentialIntSupplier;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileView;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ispd.gui.utils.ButtonBuilder.aButton;
import static ispd.gui.utils.ButtonBuilder.basicButton;

public class LoadConfigurationDialog extends JDialog {
    private static final Supplier<SpinnerModel> UNSIGNED_MODEL =
            () -> new SpinnerNumberModel(0, 0, null, 1);
    private static final Supplier<SpinnerModel> PROBABILITY_MODEL =
            () -> new SpinnerNumberModel(0.0d, 0.0d, 1.0d, 0.1d);
    private static final Supplier<SpinnerModel> POSITIVE_REAL_MODEL =
            () -> new SpinnerNumberModel(0.0d, 0.0d, null, 0.1d);
    private static final ActionListener DO_NOTHING = evt -> {
    };
    private static final Dimension PREFERRED_BUTTON_SIZE =
            new Dimension(80, 30);
    private static final Dimension PREFERRED_PANEL_SIZE =
            new Dimension(500, 300);
    private static final Font TAHOMA_FONT =
            new Font("Tahoma", Font.PLAIN, 11);
    private final MultipleExtensionFileFilter traceFileFilter;
    private final MultipleExtensionFileFilter workloadFileFilter;
    private final Vector<String> users;
    private final Vector<String> schedulers;
    private final Vector<Vector> tableRow = new Vector<>(0);
    private final Vector<String> tableColumn = new Vector<>(7);
    private final ResourceBundle translator;
    private File file = null;
    private JComboBox comboBoxSchedulers;
    private JComboBox comboBoxUsers;
    private JFileChooser jFileExternalTrace;
    private JFileChooser jOpenTrace;
    private JPanel jPanelConvertTrace;
    private JPanel jPanelForNode;
    private JPanel jPanelRandom;
    private JPanel jPanelPickTrace;
    private JPanel jPanelTrace;
    private JRadioButton jRadioButtonConvTrace;
    private JRadioButton jRadioButtonForNode;
    private JRadioButton jRadioButtonRandom;
    private JRadioButton jRadioButtonTraces;
    private JRadioButton jRadioButtonWmsx;
    private JScrollPane jScrollPaneSelecionado;
    private JScrollPane jScrollPaneTabela;
    private JSpinner jSpinnerAverageComputacao;
    private JSpinner jSpinnerAverageComunicacao;
    private JSpinner jSpinnerMaxCompNo;
    private JSpinner jSpinnerMaxComputacao;
    private JSpinner jSpinnerMaxComuNo;
    private JSpinner jSpinnerMaxComunicacao;
    private JSpinner jSpinnerMinCompNo;
    private JSpinner jSpinnerMinComputacao;
    private JSpinner jSpinnerMinComuNo;
    private JSpinner jSpinnerMinComunicacao;
    private JSpinner jSpinnerNumTarefas;
    private JSpinner jSpinnerNumTarefasNo;
    private JSpinner jSpinnerProbabilityComputacao;
    private JSpinner jSpinnerProbabilityComunicacao;
    private JSpinner jSpinnerTimeOfArrival;
    private JTable jTable1;
    private JTextField jTextFieldCaminhoTrace;
    private JTextField jTextFieldCaminhoWMS;
    private JTextArea jTextNotifTrace;
    private JTextArea jTextNotification;
    private WorkloadGenerator loadGenerator;
    private int indexTable = 0;
    private int traceTaskNumber = 0;
    private String traceType = "";

    LoadConfigurationDialog(final Frame parent, final boolean modal,
                            final Object[] users, final Object[] schedulers,
                            final WorkloadGenerator loadGenerator,
                            final ResourceBundle translator) {
        super(parent, modal);

        this.translator = translator;

        this.users = new Vector<>(0);
        for (final var user : users) {
            this.users.add((String) user);
        }

        this.schedulers = new Vector<>(0);
        for (final var scheduler : schedulers) {
            this.schedulers.add((String) scheduler);
        }

        this.loadGenerator = loadGenerator;

        this.tableColumn.add(this.translate("Application"));
        this.tableColumn.add(this.translate("User"));
        this.tableColumn.add(this.translate("Scheduler"));
        this.tableColumn.add(this.translate("Tasks"));
        this.tableColumn.add(this.translate("Maximum computing"));
        this.tableColumn.add(this.translate("Minimum computing"));
        this.tableColumn.add(this.translate("Maximum communication"));
        this.tableColumn.add(this.translate("Minimum communication"));

        this.workloadFileFilter = new MultipleExtensionFileFilter(
                "Workload Model of Sumulation",
                "wmsx",
                true
        );
        this.traceFileFilter = new MultipleExtensionFileFilter(
                "External Trace Files",
                new String[] { "swf", "gwf" },
                true
        );

        this.initComponents();
        this.setValores(loadGenerator);
    }

    private String translate(final String text) {
        return this.translator.getString(text);
    }

    private void initComponents() {
        this.jPanelRandom = new JPanel();
        this.jSpinnerNumTarefas = new JSpinner();
        this.jSpinnerMinComputacao = new JSpinner();
        this.jSpinnerMinComunicacao = new JSpinner();
        this.jSpinnerTimeOfArrival = new JSpinner();
        this.jSpinnerAverageComputacao = new JSpinner();
        this.jSpinnerAverageComunicacao = new JSpinner();
        this.jSpinnerMaxComputacao = new JSpinner();
        this.jSpinnerMaxComunicacao = new JSpinner();
        this.jSpinnerProbabilityComputacao = new JSpinner();
        this.jSpinnerProbabilityComunicacao = new JSpinner();
        this.jPanelForNode = new JPanel();
        this.comboBoxUsers = new JComboBox();
        this.comboBoxSchedulers = new JComboBox();
        this.jSpinnerNumTarefasNo = new JSpinner();
        this.jSpinnerMaxCompNo = new JSpinner();
        this.jSpinnerMinCompNo = new JSpinner();
        this.jSpinnerMinComuNo = new JSpinner();
        this.jSpinnerMaxComuNo = new JSpinner();
        this.jScrollPaneTabela = new JScrollPane();
        this.jTable1 = new JTable();
        this.jPanelTrace = new JPanel();
        this.jRadioButtonWmsx = new JRadioButton();
        this.jRadioButtonConvTrace = new JRadioButton();
        this.jPanelConvertTrace = new JPanel();
        this.jTextFieldCaminhoTrace = new JTextField();
        final JScrollPane jScrollPane2 =
                new JScrollPane();
        this.jTextNotifTrace = new JTextArea();

        this.jOpenTrace = new JFileChooser();
        this.jPanelPickTrace = new JPanel();
        final JScrollPane jScrollPane1 =
                new JScrollPane();
        this.jTextNotification = new JTextArea();

        this.jTextFieldCaminhoWMS = new JTextField();
        this.jFileExternalTrace = new JFileChooser();
        final JPanel jPanelModo = new JPanel();
        this.jRadioButtonTraces = new JRadioButton();
        this.jRadioButtonForNode = new JRadioButton();
        this.jRadioButtonRandom = new JRadioButton();

        this.jScrollPaneSelecionado = new JScrollPane();
        final JPanel jPanel1 = new JPanel();

        this.jPanelRandom.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
        this.jPanelRandom.setMaximumSize(null);
        this.jPanelRandom.setPreferredSize(LoadConfigurationDialog.PREFERRED_PANEL_SIZE);

        final var taskCount = new JLabel(
                this.translate("Number of tasks"));


        this.jSpinnerNumTarefas.setModel(LoadConfigurationDialog.UNSIGNED_MODEL.get());

        this.jSpinnerMinComputacao.setModel(LoadConfigurationDialog.POSITIVE_REAL_MODEL.get());

        final var computationalSize = new JLabel(
                this.translate("Computational size"));

        final var communicationSize = new JLabel(
                this.translate("Communication size"));

        final var arrivalTime = new JLabel(
                this.translate("Time of arrival"));

        this.jSpinnerMinComunicacao.setModel(LoadConfigurationDialog.POSITIVE_REAL_MODEL.get());
        this.jSpinnerTimeOfArrival.setModel(LoadConfigurationDialog.UNSIGNED_MODEL.get());

        final var minimumLabel = new JLabel(this.translate("Minimum"));
        final var averageLabel = new JLabel(this.translate("Average"));

        this.jSpinnerAverageComputacao.setModel(LoadConfigurationDialog.POSITIVE_REAL_MODEL.get());

        this.jSpinnerAverageComunicacao.setModel(LoadConfigurationDialog.POSITIVE_REAL_MODEL.get());

        final var maximumLabel = new JLabel(this.translate("Maximum"));

        this.jSpinnerMaxComputacao.setModel(LoadConfigurationDialog.POSITIVE_REAL_MODEL.get());

        this.jSpinnerMaxComunicacao.setModel(LoadConfigurationDialog.POSITIVE_REAL_MODEL.get());

        final var probability = new JLabel(this.translate("Probability"));

        this.jSpinnerProbabilityComputacao.setModel(LoadConfigurationDialog.PROBABILITY_MODEL.get());

        this.jSpinnerProbabilityComunicacao.setModel(LoadConfigurationDialog.PROBABILITY_MODEL.get());

        final var mFlops = new JLabel(this.translate("MFLOPS"));

        final var mBits = new JLabel(this.translate("Mbits"));

        final var seconds = new JLabel(this.translate("Seconds"));

        final GroupLayout jPanelRandomLayout =
                new GroupLayout(this.jPanelRandom);
        this.jPanelRandom.setLayout(jPanelRandomLayout);
        jPanelRandomLayout.setHorizontalGroup(
                jPanelRandomLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelRandomLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanelRandomLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(taskCount,
                                                GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(computationalSize,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(communicationSize,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(arrivalTime,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanelRandomLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(this.jSpinnerNumTarefas,
                                                GroupLayout.PREFERRED_SIZE,
                                                61, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(this.jSpinnerTimeOfArrival)
                                        .addComponent(this.jSpinnerMinComunicacao)
                                        .addComponent(this.jSpinnerMinComputacao)
                                        .addComponent(minimumLabel,
                                                GroupLayout.PREFERRED_SIZE, 0
                                                , Short.MAX_VALUE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanelRandomLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanelRandomLayout.createSequentialGroup()
                                                .addGroup(jPanelRandomLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(this.jSpinnerAverageComunicacao)
                                                        .addComponent(this.jSpinnerAverageComputacao)
                                                        .addComponent(averageLabel
                                                                ,
                                                                GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanelRandomLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(this.jSpinnerMaxComunicacao)
                                                        .addComponent(this.jSpinnerMaxComputacao)
                                                        .addComponent(maximumLabel
                                                                ,
                                                                GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE))
                                                .addGap(10, 10, 10)
                                                .addGroup(jPanelRandomLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(this.jSpinnerProbabilityComunicacao)
                                                        .addComponent(this.jSpinnerProbabilityComputacao, GroupLayout.Alignment.TRAILING)
                                                        .addComponent(probability
                                                                ,
                                                                GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanelRandomLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(mFlops)
                                                        .addComponent(mBits)))
                                        .addComponent(seconds))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE))
        );

        jPanelRandomLayout.setVerticalGroup(
                jPanelRandomLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelRandomLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanelRandomLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addGroup(jPanelRandomLayout.createSequentialGroup()
                                                .addGroup(jPanelRandomLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(taskCount)
                                                        .addComponent(this.jSpinnerNumTarefas, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(minimumLabel)
                                                .addGap(15, 15, 15)
                                                .addGroup(jPanelRandomLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(computationalSize)
                                                        .addComponent(this.jSpinnerMinComputacao, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(jPanelRandomLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(communicationSize)
                                                        .addComponent(this.jSpinnerMinComunicacao, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(jPanelRandomLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addGroup(jPanelRandomLayout.createSequentialGroup()
                                                        .addComponent(maximumLabel)
                                                        .addGap(15, 15, 15)
                                                        .addComponent(this.jSpinnerMaxComputacao, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(this.jSpinnerMaxComunicacao, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addGroup(jPanelRandomLayout.createSequentialGroup()
                                                        .addComponent(averageLabel)
                                                        .addGap(15, 15, 15)
                                                        .addComponent(this.jSpinnerAverageComputacao, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(this.jSpinnerAverageComunicacao, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addGroup(jPanelRandomLayout.createSequentialGroup()
                                                        .addComponent(probability)
                                                        .addGap(15, 15, 15)
                                                        .addGroup(jPanelRandomLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                                .addComponent(this.jSpinnerProbabilityComputacao, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(mFlops))
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addGroup(jPanelRandomLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                                .addComponent(this.jSpinnerProbabilityComunicacao, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(mBits)))))
                                .addGap(18, 18, 18)
                                .addGroup(jPanelRandomLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanelRandomLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(arrivalTime)
                                                .addComponent(this.jSpinnerTimeOfArrival, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addComponent(seconds))
                                .addContainerGap(134, Short.MAX_VALUE))
        );

        this.jPanelForNode.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
        this.jPanelForNode.setMaximumSize(null);
        this.jPanelForNode.setPreferredSize(LoadConfigurationDialog.PREFERRED_PANEL_SIZE);

        final var userLabel = new JLabel(this.translate("User"));

        this.comboBoxUsers.setModel(new DefaultComboBoxModel(this.users));
        this.comboBoxUsers.addActionListener(LoadConfigurationDialog.DO_NOTHING);

        final var scheduler = new JLabel(this.translate("Scheduler"));

        this.comboBoxSchedulers.setModel(new DefaultComboBoxModel(this.schedulers));

        final var numberOfTasks = new JLabel(this.translate("Number of" +
                                                            " tasks"));


        this.jSpinnerNumTarefasNo.setModel(LoadConfigurationDialog.UNSIGNED_MODEL.get());

        final var computational = new JLabel(this.translate("Computational"));

        final var communication = new JLabel(this.translate("Communication"));

        this.jSpinnerMaxCompNo.setModel(new SpinnerNumberModel(0.0f,
                0.0f, null, 1.0f));

        this.jSpinnerMinCompNo.setModel(new SpinnerNumberModel(0.0f,
                0.0f, null, 1.0f));

        this.jSpinnerMinComuNo.setModel(new SpinnerNumberModel(0.0f,
                0.0f, null, 1.0f));

        this.jSpinnerMaxComuNo.setModel(new SpinnerNumberModel(0.0f,
                0.0f, null, 1.0f));

        final var tableAdd =
                basicButton(this.translate("Add"), this::onTableAddClick);

        this.jTable1.setModel(new DefaultTableModel(this.tableRow,
                this.tableColumn));
        this.jScrollPaneTabela.setViewportView(this.jTable1);

        final var maximum = new JLabel(this.translate("Maximum"));

        final var minimum = new JLabel(this.translate("Minimum"));

        final var remove =
                basicButton(this.translate("Remove"), this::onTableAddClick1);

        final var jPanelForNodeLayout = new GroupLayout(this.jPanelForNode);
        this.jPanelForNode.setLayout(jPanelForNodeLayout);

        jPanelForNodeLayout.setHorizontalGroup(
                jPanelForNodeLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelForNodeLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanelForNodeLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(this.jScrollPaneTabela,
                                                GroupLayout.Alignment.LEADING
                                                , 0, 0, Short.MAX_VALUE)
                                        .addGroup(GroupLayout.Alignment.LEADING, jPanelForNodeLayout.createSequentialGroup()
                                                .addGroup(jPanelForNodeLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanelForNodeLayout.createSequentialGroup()
                                                                .addGroup(jPanelForNodeLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addComponent(this.comboBoxUsers, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                        .addComponent(userLabel))
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(jPanelForNodeLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addComponent(this.comboBoxSchedulers, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                        .addComponent(scheduler)))
                                                        .addComponent(tableAdd, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGroup(jPanelForNodeLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanelForNodeLayout.createSequentialGroup()
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(numberOfTasks))
                                                        .addGroup(jPanelForNodeLayout.createSequentialGroup()
                                                                .addGap(4, 4, 4)
                                                                .addComponent(this.jSpinnerNumTarefasNo))
                                                        .addGroup(jPanelForNodeLayout.createSequentialGroup()
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(remove, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanelForNodeLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                        .addGroup(jPanelForNodeLayout.createSequentialGroup()
                                                                .addGroup(jPanelForNodeLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addComponent(maximum)
                                                                        .addComponent(minimum))
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(jPanelForNodeLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addComponent(this.jSpinnerMaxCompNo, GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                                                                        .addComponent(this.jSpinnerMinCompNo)))
                                                        .addComponent(computational, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanelForNodeLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(this.jSpinnerMaxComuNo)
                                                        .addComponent(communication, GroupLayout.Alignment.TRAILING)
                                                        .addComponent(this.jSpinnerMinComuNo))))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE))
        );

        jPanelForNodeLayout.setVerticalGroup(
                jPanelForNodeLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelForNodeLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanelForNodeLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(userLabel)
                                        .addComponent(scheduler)
                                        .addComponent(numberOfTasks)
                                        .addComponent(communication)
                                        .addComponent(computational))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanelForNodeLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.comboBoxUsers,
                                                GroupLayout.PREFERRED_SIZE,
                                                25, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(this.comboBoxSchedulers,
                                                GroupLayout.PREFERRED_SIZE,
                                                25, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(this.jSpinnerNumTarefasNo,
                                                GroupLayout.PREFERRED_SIZE,
                                                29, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(maximum)
                                        .addComponent(this.jSpinnerMaxCompNo,
                                                GroupLayout.PREFERRED_SIZE,
                                                25, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(this.jSpinnerMaxComuNo,
                                                GroupLayout.PREFERRED_SIZE,
                                                24, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanelForNodeLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanelForNodeLayout.createSequentialGroup()
                                                .addGroup(jPanelForNodeLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(minimum)
                                                        .addComponent(this.jSpinnerMinCompNo, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                                                        .addComponent(this.jSpinnerMinComuNo, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE))
                                                .addGap(18, 18, 18))
                                        .addGroup(jPanelForNodeLayout.createSequentialGroup()
                                                .addGroup(jPanelForNodeLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(tableAdd)
                                                        .addComponent(remove))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addComponent(this.jScrollPaneTabela,
                                        GroupLayout.DEFAULT_SIZE,
                                        177, Short.MAX_VALUE)
                                .addContainerGap())
        );

        this.jPanelTrace.setPreferredSize(LoadConfigurationDialog.PREFERRED_PANEL_SIZE);

        this.jRadioButtonWmsx.setSelected(true);
        this.jRadioButtonWmsx.setText(this.translate("Open an existing iSPD " +
                                                     "trace file"));
        this.jRadioButtonWmsx.addActionListener(this::jRadioButtonwmsxActionPerformed);

        this.jRadioButtonConvTrace.setText(this.translate("Convert an " +
                                                          "external" +
                                                          " trace file to " +
                                                          "iSPD" +
                                                          " trace format"));

        this.jRadioButtonConvTrace.addActionListener(this::jRadioButtonConvTraceActionPerformed);

        final var optionSelect = new JLabel(this.translate(
                "Select the desired option"));

        final var next = basicButton(
                "%s >>".formatted(this.translate("Next")), this::onNextClick);

        final GroupLayout jPanelTraceLayout = new GroupLayout(this.jPanelTrace);
        this.jPanelTrace.setLayout(jPanelTraceLayout);

        jPanelTraceLayout.setHorizontalGroup(
                jPanelTraceLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelTraceLayout.createSequentialGroup()
                                .addGroup(jPanelTraceLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanelTraceLayout.createSequentialGroup()
                                                .addGroup(jPanelTraceLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanelTraceLayout.createSequentialGroup()
                                                                .addGap(29,
                                                                        29, 29)
                                                                .addComponent(optionSelect))
                                                        .addGroup(jPanelTraceLayout.createSequentialGroup()
                                                                .addGap(61,
                                                                        61, 61)
                                                                .addGroup(jPanelTraceLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addComponent(this.jRadioButtonConvTrace)
                                                                        .addComponent(this.jRadioButtonWmsx))))
                                                .addGap(0, 48, Short.MAX_VALUE))
                                        .addGroup(GroupLayout.Alignment.TRAILING, jPanelTraceLayout.createSequentialGroup()
                                                .addContainerGap(422,
                                                        Short.MAX_VALUE)
                                                .addComponent(next)))
                                .addContainerGap())
        );

        jPanelTraceLayout.setVerticalGroup(
                jPanelTraceLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelTraceLayout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addComponent(optionSelect)
                                .addGap(62, 62, 62)
                                .addComponent(this.jRadioButtonWmsx)
                                .addGap(18, 18, 18)
                                .addComponent(this.jRadioButtonConvTrace)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 66, Short.MAX_VALUE)
                                .addComponent(next)
                                .addGap(44, 44, 44))
        );

        this.jPanelConvertTrace.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
        this.jPanelConvertTrace.setPreferredSize(LoadConfigurationDialog.PREFERRED_PANEL_SIZE);

        final var externalSelect = new JLabel(this.translate(
                "Select a external format trace file to convert:"));

        final var openExternal = basicButton(
                this.translate("Open"), this::onOpenExternalClicked);

        final var notifications = new JLabel(this.translate("Notifications"));

        final var convertExternal = basicButton(
                this.translate("Convert"), this::onConvertExternalClicked);

        this.jTextFieldCaminhoTrace.addActionListener(LoadConfigurationDialog.DO_NOTHING);

        this.jTextNotifTrace.setColumns(20);
        this.jTextNotifTrace.setRows(5);
        this.jTextNotifTrace.setPreferredSize(new Dimension(164, 74));
        jScrollPane2.setViewportView(this.jTextNotifTrace);

        final var previous = basicButton(
                "<< %s".formatted(this.translate("Previous")),
                this::onPreviousClick);

        final GroupLayout jPanelConvertTraceLayout =
                new GroupLayout(this.jPanelConvertTrace);
        this.jPanelConvertTrace.setLayout(jPanelConvertTraceLayout);
        jPanelConvertTraceLayout.setHorizontalGroup(
                jPanelConvertTraceLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelConvertTraceLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanelConvertTraceLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane2,
                                                GroupLayout.DEFAULT_SIZE, 474
                                                , Short.MAX_VALUE)
                                        .addComponent(externalSelect)
                                        .addComponent(notifications)
                                        .addGroup(jPanelConvertTraceLayout.createSequentialGroup()
                                                .addComponent(openExternal,
                                                        GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(this.jTextFieldCaminhoTrace, GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(convertExternal))
                                        .addComponent(previous,
                                                GroupLayout.Alignment.TRAILING))
                                .addContainerGap())
        );
        jPanelConvertTraceLayout.setVerticalGroup(
                jPanelConvertTraceLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelConvertTraceLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(externalSelect)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanelConvertTraceLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(openExternal)
                                        .addComponent(this.jTextFieldCaminhoTrace,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(convertExternal))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(notifications)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2,
                                        GroupLayout.PREFERRED_SIZE, 142,
                                        GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(previous)
                                .addGap(65, 65, 65))
        );

        this.jOpenTrace.setAcceptAllFileFilterUsed(false);
        this.jOpenTrace.setFileFilter(this.workloadFileFilter);
        this.jOpenTrace.setFileView(new SomeFileView());

        this.jPanelPickTrace.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        this.jPanelPickTrace.setPreferredSize(LoadConfigurationDialog.PREFERRED_PANEL_SIZE);

        final var jLabel20 = new JLabel(this.translate("Select an iSPD trace " +
                                                       "file to open:"));

        final var jLabel21 = new JLabel(this.translate("Notifications"));

        this.jTextNotification.setColumns(20);
        this.jTextNotification.setFont(LoadConfigurationDialog.TAHOMA_FONT);
        this.jTextNotification.setRows(5);
        this.jTextNotification.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        jScrollPane1.setViewportView(this.jTextNotification);

        final var previous2 = basicButton(
                "<< %s".formatted(this.translate("Previous")),
                this::onPrevious2Click);

        final var openWmsx = basicButton(
                this.translate("Open"), this::onOpenWmsxClick);

        final GroupLayout jPanelSelecionaTraceLayout =
                new GroupLayout(this.jPanelPickTrace);
        this.jPanelPickTrace.setLayout(jPanelSelecionaTraceLayout);

        jPanelSelecionaTraceLayout.setHorizontalGroup(
                jPanelSelecionaTraceLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelSelecionaTraceLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanelSelecionaTraceLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel20)
                                        .addComponent(jScrollPane1,
                                                GroupLayout.DEFAULT_SIZE, 476
                                                , Short.MAX_VALUE)
                                        .addComponent(previous2,
                                                GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel21)
                                        .addGroup(jPanelSelecionaTraceLayout.createSequentialGroup()
                                                .addComponent(openWmsx
                                                        ,
                                                        GroupLayout.PREFERRED_SIZE, 71, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(this.jTextFieldCaminhoWMS, GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        jPanelSelecionaTraceLayout.setVerticalGroup(
                jPanelSelecionaTraceLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelSelecionaTraceLayout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(jLabel20)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanelSelecionaTraceLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(openWmsx)
                                        .addComponent(this.jTextFieldCaminhoWMS,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel21)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1,
                                        GroupLayout.PREFERRED_SIZE, 147,
                                        GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(previous2)
                                .addContainerGap(31, Short.MAX_VALUE))
        );

        this.jFileExternalTrace.setAcceptAllFileFilterUsed(false);
        this.jFileExternalTrace.setFileFilter(this.traceFileFilter);

        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setTitle(this.translate("Random Workloads"));
        this.setMinimumSize(new Dimension(550, 450));
        this.setResizable(false);

        jPanelModo.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)), this.translate("Insertion mode for the workloads")));
        jPanelModo.setMaximumSize(new Dimension(500, 60));
        jPanelModo.setMinimumSize(new Dimension(500, 60));
        jPanelModo.setPreferredSize(new Dimension(500, 60));

        this.jRadioButtonTraces.setText(this.translate("Traces"));
        this.jRadioButtonTraces.setOpaque(false);
        this.jRadioButtonTraces.addActionListener(this::jRadioButtonTracesActionPerformed);

        this.jRadioButtonForNode.setText(this.translate("For each node"));
        this.jRadioButtonForNode.addActionListener(this::jRadioButtonForNodeActionPerformed);

        this.jRadioButtonRandom.setSelected(true);
        this.jRadioButtonRandom.setText(this.translate("Random"));
        this.jRadioButtonRandom.addActionListener(this::jRadioButtonRandomActionPerformed);

        final GroupLayout jPanelModoLayout = new GroupLayout(jPanelModo);
        jPanelModo.setLayout(jPanelModoLayout);

        jPanelModoLayout.setHorizontalGroup(
                jPanelModoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelModoLayout.createSequentialGroup()
                                .addGap(62, 62, 62)
                                .addComponent(this.jRadioButtonRandom)
                                .addGap(37, 37, 37)
                                .addComponent(this.jRadioButtonForNode,
                                        GroupLayout.PREFERRED_SIZE, 126,
                                        GroupLayout.PREFERRED_SIZE)
                                .addGap(31, 31, 31)
                                .addComponent(this.jRadioButtonTraces,
                                        GroupLayout.PREFERRED_SIZE, 72,
                                        GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE))
        );

        jPanelModoLayout.setVerticalGroup(
                jPanelModoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelModoLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanelModoLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.jRadioButtonRandom)
                                        .addComponent(this.jRadioButtonTraces,
                                                GroupLayout.PREFERRED_SIZE,
                                                25, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(this.jRadioButtonForNode))
                                .addGap(9, 9, 9))
        );

        this.jScrollPaneSelecionado.setPreferredSize(LoadConfigurationDialog.PREFERRED_PANEL_SIZE);

        final GroupLayout jPanel1Layout =
                new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 498, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 309, Short.MAX_VALUE)
        );

        this.jScrollPaneSelecionado.setViewportView(jPanel1);

        this.makeLayoutAndPack(jPanelModo);
    }

    /**
     * Load properties from load generator into window components.
     *
     * @param loadGenerator The load generator.
     */
    private void setValores(final WorkloadGenerator loadGenerator) {
        if (loadGenerator == null) {
            this.setTipo(WorkloadGeneratorType.RANDOM);
            return;
        }

        switch (loadGenerator.getType()) {
            case RANDOM -> {
                final var random = (GlobalWorkloadGenerator) loadGenerator;
                this.jSpinnerNumTarefas.setValue(random.getTaskCount());
                this.jSpinnerMinComputacao.setValue(random.getComputationMinimum());
                this.jSpinnerMaxComputacao.setValue(random.getComputationMaximum());
                this.jSpinnerAverageComputacao.setValue(random.getComputationAverage());
                this.jSpinnerProbabilityComputacao.setValue(random.getComputationProbability());
                this.jSpinnerMinComunicacao.setValue(random.getCommunicationMinimum());
                this.jSpinnerMaxComunicacao.setValue(random.getCommunicationMaximum());
                this.jSpinnerAverageComunicacao.setValue(random.getCommunicationAverage());
                this.jSpinnerProbabilityComunicacao.setValue(random.getCommunicationProbability());
                this.jSpinnerTimeOfArrival.setValue(random.getTaskCreationTime());
                this.setTipo(WorkloadGeneratorType.RANDOM);
            }
            case PER_NODE -> {
                final var nodes = (CollectionWorkloadGenerator) loadGenerator;
                for (final WorkloadGenerator item : nodes.getList()) {
                    final PerNodeWorkloadGenerator node =
                            (PerNodeWorkloadGenerator) item;
                    this.tableRow.add(node.toVector());
                }
                this.indexTable = this.tableRow.size();
                this.setTipo(WorkloadGeneratorType.PER_NODE);
            }
            case TRACE -> {
                final TraceFileWorkloadGenerator trace =
                        (TraceFileWorkloadGenerator) loadGenerator;
                this.file = trace.getTraceFile();
                this.traceType = trace.getTraceType();
                this.traceTaskNumber = trace.getTaskCount();
                this.jTextFieldCaminhoWMS.setText(this.file.getAbsolutePath());
                this.jTextNotification.setText("""
                        File %s
                        \t- File format: %s
                        \t- File has a workload of %d tasks"""
                        .formatted(
                                this.file.getName(),
                                this.traceType,
                                this.traceTaskNumber));
                this.setTipo(WorkloadGeneratorType.TRACE);
            }
        }
    }

    private void onTableAddClick(final ActionEvent evt) {

        final Vector linha = new Vector(8);
        linha.add("app" + this.indexTable);
        this.indexTable++;
        linha.add(this.comboBoxUsers.getSelectedItem());
        linha.add(this.comboBoxSchedulers.getSelectedItem());
        linha.add(this.jSpinnerNumTarefasNo.getValue());
        linha.add(this.jSpinnerMaxCompNo.getValue());
        linha.add(this.jSpinnerMinCompNo.getValue());
        linha.add(this.jSpinnerMaxComuNo.getValue());
        linha.add(this.jSpinnerMinComuNo.getValue());
        this.tableRow.add(linha);
        this.jScrollPaneTabela.setViewportView(this.jTable1);
    }

    private void onTableAddClick1(final ActionEvent evt) {
        final int linha = this.jTable1.getSelectedRow();
        if (linha >= 0 && linha < this.tableRow.size()) {
            this.tableRow.remove(linha);
        }
        this.jScrollPaneTabela.setViewportView(this.jTable1);
    }

    private void jRadioButtonwmsxActionPerformed(final ActionEvent evt) {

        if (this.jRadioButtonWmsx.isSelected()) {
            this.jRadioButtonWmsx.setSelected(true);
            this.jRadioButtonConvTrace.setSelected(false);
            this.file = null;
            this.jTextFieldCaminhoTrace.setText("");
            this.jTextNotifTrace.setText("");
            this.traceTaskNumber = 0;

        } else if (this.jRadioButtonConvTrace.isSelected()) {
            this.jRadioButtonConvTrace.setSelected(true);
            this.jRadioButtonWmsx.setSelected(false);
        }
    }

    private void jRadioButtonConvTraceActionPerformed(final ActionEvent evt) {

        if (this.jRadioButtonConvTrace.isSelected()) {
            this.jRadioButtonWmsx.setSelected(false);
            this.jRadioButtonConvTrace.setSelected(true);
            this.file = null;
            this.jTextFieldCaminhoWMS.setText("");
            this.jTextNotification.setText("");
            this.traceTaskNumber = 0;
        } else {
            this.jRadioButtonConvTrace.setSelected(false);
            this.jRadioButtonWmsx.setSelected(true);
        }
    }

    private void onNextClick(final ActionEvent evt) {
        final JPanel panel = this.jRadioButtonWmsx.isSelected() ?
                this.jPanelPickTrace : this.jPanelConvertTrace;
        this.jScrollPaneSelecionado.setViewportView(panel);
    }

    private void onOpenExternalClicked(final ActionEvent evt) {

        this.traceFileFilter.setDescricao(this.translate("External Trace " +
                                                         "File"));
        final String[] exts = { ".swf", ".gwf" };
        this.traceFileFilter.setExtensao(exts);
        this.jFileExternalTrace.setAcceptAllFileFilterUsed(false);
        final int returnVal = this.jFileExternalTrace.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.file = this.jFileExternalTrace.getSelectedFile();
            this.jTextFieldCaminhoTrace.setText(this.file.getAbsolutePath());
        }

    }

    private void onConvertExternalClicked(final ActionEvent evt) {


        try {
            final TraceXML interpret =
                    new TraceXML(this.jTextFieldCaminhoTrace.getText());
            try {//inicia a conversão do arquivo
                final double t1 = System.currentTimeMillis();
                interpret.convert();
                final double t2 = System.currentTimeMillis();
                System.out.println((t2 - t1) / 1000);
                this.file = new File(interpret.getSaida());
                this.traceTaskNumber = interpret.getNum_Tasks();
                this.traceType = interpret.getTipo();
            } catch (final Exception e) {
                this.jTextNotifTrace.setText(this.translate("Badly formatted " +
                                                            "file"));
            }
            this.jTextNotifTrace.setText(interpret.toString());
        } catch (final Exception e) {
            this.jTextNotifTrace.setText(this.translate("There is no file " +
                                                        "selected"));
        }
    }

    private void onPreviousClick(final ActionEvent evt) {
        this.jScrollPaneSelecionado.setViewportView(this.jPanelTrace);
    }

    private void onPrevious2Click(final ActionEvent evt) {
        this.jScrollPaneSelecionado.setViewportView(this.jPanelTrace);
    }

    private void onOpenWmsxClick(final ActionEvent evt) {
        this.workloadFileFilter.setDescricao(
                this.translate("Workload Model of Simulation"));

        this.workloadFileFilter.setExtensao(".wmsx");
        this.jOpenTrace.setAcceptAllFileFilterUsed(false);
        final int returnVal = this.jOpenTrace.showOpenDialog(this);

        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        this.file = this.jOpenTrace.getSelectedFile();
        this.jTextFieldCaminhoWMS.setText(this.file.getAbsolutePath());
        final TraceXML interpret =
                new TraceXML(this.file.getAbsolutePath());
        this.jTextNotification.setText(interpret.LerCargaWMS());
        this.traceTaskNumber = interpret.getNum_Tasks();
        this.traceType = interpret.getTipo();
    }

    private void jRadioButtonTracesActionPerformed(final ActionEvent evt) {
        this.setTipo(WorkloadGeneratorType.TRACE);
    }

    private void jRadioButtonForNodeActionPerformed(final ActionEvent evt) {
        this.setTipo(WorkloadGeneratorType.PER_NODE);
    }

    private void jRadioButtonRandomActionPerformed(final ActionEvent evt) {
        this.setTipo(WorkloadGeneratorType.RANDOM);
    }

    private void makeLayoutAndPack(final Component panel) {
        final var ok =
                aButton(this.translate("OK"), this::onOkClick)
                        .withPreferredSize(LoadConfigurationDialog.PREFERRED_BUTTON_SIZE)
                        .build();

        final var cancel =
                aButton(this.translate("Cancel"), this::onCancelClick)
                        .withSize(LoadConfigurationDialog.PREFERRED_BUTTON_SIZE)
                        .build();

        final var layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(150, 150, 150)
                                                .addComponent(cancel
                                                        ,
                                                        GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                                                .addGap(28, 28, 28)
                                                .addComponent(ok,
                                                        GroupLayout.PREFERRED_SIZE, 124, GroupLayout.PREFERRED_SIZE))
                                        .addComponent(this.jScrollPaneSelecionado,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(panel,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE))
                                .addContainerGap())
        );

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING,
                                layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(panel,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(this.jScrollPaneSelecionado,
                                                GroupLayout.PREFERRED_SIZE,
                                                311, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(cancel,
                                                        GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(ok,
                                                        GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addContainerGap())
        );

        this.pack();
    }

    /**
     * Shows window and selects the needed radio button based on the given type.
     *
     * @param type Type selected by user
     */
    private void setTipo(final WorkloadGeneratorType type) {
        switch (type) {
            case RANDOM -> {
                this.jRadioButtonForNode.setSelected(false);
                this.jRadioButtonTraces.setSelected(false);
                this.jRadioButtonRandom.setSelected(true);
                this.jScrollPaneSelecionado.setViewportView(this.jPanelRandom);
            }
            case PER_NODE -> {
                this.jRadioButtonForNode.setSelected(true);
                this.jRadioButtonTraces.setSelected(false);
                this.jRadioButtonRandom.setSelected(false);
                this.jScrollPaneSelecionado.setViewportView(this.jPanelForNode);
            }
            case TRACE -> {
                this.jRadioButtonForNode.setSelected(false);
                this.jRadioButtonTraces.setSelected(true);
                this.jRadioButtonRandom.setSelected(false);
                this.jScrollPaneSelecionado.setViewportView(this.jPanelTrace);
            }
        }
    }

    private void onOkClick(final ActionEvent evt) {
        if (this.jRadioButtonRandom.isSelected()) {
            try {
                final var taskCount = (int) this.jSpinnerNumTarefas.getValue();
                final var minComp =
                        (double) this.jSpinnerMinComputacao.getValue();
                final var maxComp =
                        (double) this.jSpinnerMaxComputacao.getValue();
                final var aveComp =
                        (double) this.jSpinnerAverageComputacao.getValue();
                final double probComp =
                        (double) this.jSpinnerProbabilityComputacao.getValue();
                final var minComun =
                        (double) this.jSpinnerMinComunicacao.getValue();
                final var maxComun =
                        (double) this.jSpinnerMaxComunicacao.getValue();
                final var aveComun =
                        (double) this.jSpinnerAverageComunicacao.getValue();
                final double probComun =
                        (double) this.jSpinnerProbabilityComunicacao.getValue();
                final var timeArriv =
                        (int) this.jSpinnerTimeOfArrival.getValue();
                this.loadGenerator = new GlobalWorkloadGenerator(
                        taskCount,
                        minComp, maxComp, aveComp, probComp,
                        minComun, maxComun, aveComun, probComun,
                        timeArriv
                );
            } catch (final Exception ex) {
                Logger.getLogger(LoadConfigurationDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (this.jRadioButtonForNode.isSelected()) {
            try {
                final List<WorkloadGenerator> configuracaoNo =
                        new ArrayList<>(this.tableRow.size());
                final var idSupplier = new SequentialIntSupplier();
                for (final List item : this.tableRow) {
                    configuracaoNo.add(PerNodeWorkloadGenerator.fromTableRow(item, idSupplier));
                }
                this.loadGenerator = new CollectionWorkloadGenerator(
                        WorkloadGeneratorType.PER_NODE,
                        configuracaoNo
                );
            } catch (final Exception ex) {
                Logger.getLogger(LoadConfigurationDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (this.jRadioButtonTraces.isSelected()) {
            //configura a carga apartir do arquivo aberto..
            this.loadGenerator = new TraceFileWorkloadGenerator(this.file,
                    this.traceTaskNumber,
                    this.traceType);
        }
        this.setVisible(false);
    }

    private void onCancelClick(final ActionEvent evt) {
        this.setVisible(false);
    }

    WorkloadGenerator getCargasConfiguracao() {
        return this.loadGenerator;
    }

    public HashSet<String> getUsuarios() {
        return new HashSet<>(this.users);
    }

    private static class SomeFileView extends FileView {
        static final char FILE_EXTENSION_SEPARATOR = '.';

        @Override
        public Icon getIcon(final File filePath) {
            if (!"wmsx".equals(SomeFileView.getFileExtension(filePath))) {
                return null;
            }

            final var url =
                    MainWindow.class.getResource("imagens/Logo_iSPD_25.png");

            if (url == null) {
                return null;
            }

            return new ImageIcon(url);
        }

        private static String getFileExtension(final File file) {
            final var s = file.getName();
            final int i = s.lastIndexOf(SomeFileView.FILE_EXTENSION_SEPARATOR);

            if (i <= 0 || i >= s.length() - 1) {
                return null;
            }

            return s.substring(i + 1).toLowerCase();
        }
    }
}