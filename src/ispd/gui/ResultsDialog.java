package ispd.gui;

import ispd.arquivo.SalvarResultadosHTML;
import ispd.arquivo.xml.ConfiguracaoISPD;
import ispd.arquivo.xml.TraceXML;
import ispd.gui.auxiliar.MultipleExtensionFileFilter;
import ispd.gui.auxiliar.SimulationResultChartMaker;
import ispd.gui.auxiliar.HtmlPane;
import ispd.gui.auxiliar.ParesOrdenadosUso;
import ispd.gui.auxiliar.UserOperationTime;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.metricas.Metricas;
import ispd.motor.metricas.MetricasComunicacao;
import ispd.motor.metricas.MetricasGlobais;
import ispd.motor.metricas.MetricasProcessamento;
import ispd.motor.metricas.MetricasUsuarios;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYStepAreaRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.AbstractButton;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResultsDialog extends JDialog {
    private static final Dimension CHART_PREFERRED_SIZE = new Dimension(600,
            300);
    private static final Font COURIER_NEW_FONT_BOLD = new Font("Courier New",
            Font.BOLD, 14);
    private static final Font COURIER_NEW_FONT = new Font("Courier New",
            Font.PLAIN, 14);
    private final Object[][] resourceTable;
    private final SalvarResultadosHTML html = new SalvarResultadosHTML();
    private JButton jButtonProcessamentoTarefa = null;
    private JScrollPane jScrollPaneComunicacao = null;
    private JScrollPane jScrollPaneProcessamento = null;
    private JScrollPane jScrollPaneProcessamentoTempo = null;
    private JTabbedPane jTabbedPaneGrid = null;
    private JButton jButtonCommunicationBarra = null;
    private JButton jButtonCommunicationPizza = null;
    private JButton jButtonExperimental = null;
    private JButton jButtonProcessBarra = null;
    private JButton jButtonProcessPizza = null;
    private JButton jButtonProcessamentoMaquina = null;
    private JButton jButtonUserModelo1 = null;
    private JButton jButtonUserModelo2 = null;
    private JPanel jPanel1 = null;
    private JScrollPane jScrollPaneCharts = null;
    private JScrollPane jScrollPaneGraficoTarefa = null;
    private JScrollPane jScrollPaneUsuario = null;
    private JTextArea jTextAreaGlobal = null;
    private JTextArea jTextAreaTarefa = null;
    private JTextArea jTextAreaUsuario = null;
    private List<Tarefa> tarefas = null;
    private ChartPanel graficoBarraProcessamento = null;
    private ChartPanel graficoBarraComunicacao = null;
    private ChartPanel graficoPizzaProcessamento = null;
    private ChartPanel graficoPizzaComunicacao = null;
    private ChartPanel graficoProcessamentoTempo = null;
    private ChartPanel graficoProcessamentoTempoTarefa = null;
    private ChartPanel graficoProcessamentoTempoUser1 = null;
    private ChartPanel graficoProcessamentoTempoUser2 = null;
    private double poderComputacionalTotal = 0;
    private JTextField jTextFieldLerID = null;
    private JTextField jTextFieldLerID1 = null;
    private SimulationResultChartMaker charts = null;

    /**
     * Create JResultado without graphical interface for use in CLI.
     * Initialized entirely from class Metricas
     */
    public ResultsDialog(final Metricas metricas) {
        this.html.setMetricasGlobais(metricas.getMetricasGlobais());
        this.resourceTable = ResultsDialog.setTabelaRecurso(metricas);
        ResultsDialog.getResultadosTarefas(metricas);
        this.html.setMetricasTarefas(metricas);
        this.gerarGraficosProcessamento(metricas.getMetricasProcessamento());
        this.gerarGraficosComunicacao(metricas.getMetricasComunicacao());

        this.charts = new SimulationResultChartMaker();
        this.charts.criarProcessamento(metricas.getMetricasProcessamento());
        this.charts.criarComunicacao(metricas.getMetricasComunicacao());
    }

    private static Object[][] setTabelaRecurso(final Metricas metricas) {
        final List<Object[]> tabela = new ArrayList<>();
        //linha [Nome] [Proprietario] [Processamento] [comunicacao]
        String nome;
        String prop;
        double proc;
        double comu;
        if (metricas.getMetricasProcessamento() != null) {
            for (final Map.Entry<String, MetricasProcessamento> entry :
                    metricas.getMetricasProcessamento().entrySet()) {
                final MetricasProcessamento maq = entry.getValue();
                if (maq.getnumeroMaquina() == 0) {
                    nome = maq.getId();
                } else {
                    nome = maq.getId() + " node " + maq.getnumeroMaquina();
                }
                prop = maq.getProprietario();
                proc = maq.getSegundosDeProcessamento();
                comu = 0.0;
                tabela.add(Arrays.asList(nome, prop, proc, comu).toArray());
            }
        }
        if (metricas.getMetricasComunicacao() != null) {
            for (final Map.Entry<String, MetricasComunicacao> entry :
                    metricas.getMetricasComunicacao().entrySet()) {
                final MetricasComunicacao link = entry.getValue();
                nome = link.getId();
                prop = "---";
                proc = 0.0;
                comu = link.getSegundosDeTransmissao();
                tabela.add(Arrays.asList(nome, prop, proc, comu).toArray());
            }
        }
        final Object[][] temp = new Object[tabela.size()][4];
        for (int i = 0; i < tabela.size(); i++) {
            temp[i] = tabela.get(i);
        }
        return temp;
    }

    private static String getResultadosTarefas(final Metricas metrica) {
        String texto = "\n\n\t\tTASKS\n ";
        final double tempoMedioSistemaComunicacao =
                metrica.getTempoMedioFilaComunicacao() + metrica.getTempoMedioComunicacao();
        final double tempoMedioSistemaProcessamento =
                metrica.getTempoMedioFilaProcessamento() + metrica.getTempoMedioProcessamento();
        texto += "\n Communication \n";
        texto += String.format("    Queue average time: %g seconds.\n",
                metrica.getTempoMedioFilaComunicacao());
        texto += String.format("    Communication average time: %g seconds" +
                               ".\n", metrica.getTempoMedioComunicacao());
        texto += String.format("    System average time: %g seconds.\n",
                tempoMedioSistemaComunicacao);
        texto += "\n Processing \n";
        texto += String.format("    Queue average time: %g seconds.\n",
                metrica.getTempoMedioFilaProcessamento());
        texto += String.format("    Processing average time: %g seconds.\n",
                metrica.getTempoMedioProcessamento());
        texto += String.format("    System average time: %g seconds.\n",
                tempoMedioSistemaProcessamento);
        if (metrica.getNumTarefasCanceladas() > 0) {
            texto += "\n Tasks Canceled \n";
            texto += String.format("    Number: %d \n",
                    metrica.getNumTarefasCanceladas());
            texto += String.format("    Wasted Processing: %g Mflops",
                    metrica.getMflopsDesperdicio());
        }
        return texto;
    }

    private void gerarGraficosProcessamento(final Map<String,
            MetricasProcessamento> mProcess) {
        final DefaultCategoryDataset dadosGraficoProcessamento =
                new DefaultCategoryDataset();
        final DefaultPieDataset dadosGraficoPizzaProcessamento =
                new DefaultPieDataset();

        if (mProcess != null) {
            for (final Map.Entry<String, MetricasProcessamento> entry :
                    mProcess.entrySet()) {
                final MetricasProcessamento mt = entry.getValue();
                if (mt.getnumeroMaquina() == 0) {
                    dadosGraficoProcessamento.addValue(mt.getMFlopsProcessados(), "vermelho", mt.getId());
                    dadosGraficoPizzaProcessamento.insertValue(0, mt.getId(),
                            mt.getMFlopsProcessados());
                } else {
                    dadosGraficoProcessamento.addValue(mt.getMFlopsProcessados(), "vermelho", mt.getId() + " node " + mt.getnumeroMaquina());
                    dadosGraficoPizzaProcessamento.insertValue(0,
                            mt.getId() + " node " + mt.getnumeroMaquina(),
                            mt.getMFlopsProcessados());
                }
            }
        }

        JFreeChart jfc = ChartFactory.createBarChart(
                "Total processed on each resource", //Titulo
                "Resource", // Eixo X
                "Mflops", //Eixo Y
                dadosGraficoProcessamento, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                false, false, false); // exibir: legendas, tooltips, url
        //Inclina nome da barra em 45 graus
        if (mProcess.size() > 10) {
            jfc.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }
        this.graficoBarraProcessamento = new ChartPanel(jfc);
        this.graficoBarraProcessamento.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);

        jfc = ChartFactory.createPieChart(
                "Total processed on each resource", //Titulo
                dadosGraficoPizzaProcessamento, // Dados para o grafico
                true, false, false);
        this.graficoPizzaProcessamento = new ChartPanel(jfc);
        this.graficoPizzaProcessamento.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);

    }

    private void gerarGraficosComunicacao(final Map<String,
            MetricasComunicacao> mComunicacao) {
        final DefaultCategoryDataset dadosGraficoComunicacao =
                new DefaultCategoryDataset();
        final DefaultPieDataset dadosGraficoPizzaComunicacao =
                new DefaultPieDataset();

        if (mComunicacao != null) {
            for (final Map.Entry<String, MetricasComunicacao> entry :
                    mComunicacao.entrySet()) {
                final MetricasComunicacao link = entry.getValue();
                dadosGraficoComunicacao.addValue(link.getMbitsTransmitidos(),
                        "vermelho", link.getId());
                dadosGraficoPizzaComunicacao.insertValue(0, link.getId(),
                        link.getMbitsTransmitidos());
            }
        }

        JFreeChart jfc = ChartFactory.createBarChart(
                "Total communication in each resource", //Titulo
                "Resource", // Eixo X
                "Mbits", //Eixo Y
                dadosGraficoComunicacao, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                false, false, false); // exibir: legendas, tooltips, url
        //Inclina nome da barra em 45 graus
        if (mComunicacao != null && mComunicacao.size() > 10) {
            jfc.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }
        this.graficoBarraComunicacao = new ChartPanel(jfc);
        this.graficoBarraComunicacao.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);

        jfc = ChartFactory.createPieChart(
                "Total communication in each resource", //Titulo
                dadosGraficoPizzaComunicacao, // Dados para o grafico
                true, false, false);
        this.graficoPizzaComunicacao = new ChartPanel(jfc);
        this.graficoPizzaComunicacao.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);
    }

    ResultsDialog(final Frame parent, final Metricas metricas,
                  final RedeDeFilas rdf, final List<Tarefa> tarefas) {
        super(parent, true);
        this.tarefas = tarefas;
        this.gerarGraficosProcessamento(metricas.getMetricasProcessamento());
        this.gerarGraficosComunicacao(metricas.getMetricasComunicacao());
        this.resourceTable = ResultsDialog.setTabelaRecurso(metricas);
        this.charts = new SimulationResultChartMaker();
        this.charts.criarProcessamento(metricas.getMetricasProcessamento());
        this.charts.criarComunicacao(metricas.getMetricasComunicacao());
        this.initComponents();
        this.jTextAreaGlobal.setText(ResultsDialog.getResultadosGlobais(metricas.getMetricasGlobais()));
        this.html.setMetricasGlobais(metricas.getMetricasGlobais());
        this.jTextAreaTarefa.setText(ResultsDialog.getResultadosTarefas(metricas));
        this.html.setMetricasTarefas(metricas);
        final CS_Mestre mestre = (CS_Mestre) rdf.getMestres().get(0);
        this.setResultadosUsuario(mestre.getEscalonador().getMetricaUsuarios(),
                metricas);

        if (rdf.getMaquinas().size() < 21) {
            this.graficoProcessamentoTempo =
                    new ChartPanel(this.criarGraficoProcessamentoTempo(rdf));
            this.graficoProcessamentoTempo.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);
        } else {
            this.jButtonProcessamentoMaquina.setVisible(false);
            for (final CS_Processamento maq : rdf.getMaquinas()) {
                this.poderComputacionalTotal += (maq.getPoderComputacional() - (maq.getOcupacao() * maq.getPoderComputacional()));
            }
        }
        if (tarefas.size() < 50) {
            this.graficoProcessamentoTempoTarefa =
                    new ChartPanel(this.criarGraficoProcessamentoTempoTarefa(tarefas));
            this.graficoProcessamentoTempoTarefa.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);
        } else {
            this.jButtonProcessamentoTarefa.setVisible(false);
        }
        final JFreeChart[] temp =
                this.gerarGraficoProcessamentoTempoUser(tarefas, rdf);
        this.graficoProcessamentoTempoUser1 = new ChartPanel(temp[0]);
        this.graficoProcessamentoTempoUser1.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);
        this.graficoProcessamentoTempoUser2 = new ChartPanel(temp[1]);
        this.graficoProcessamentoTempoUser2.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);

        this.jScrollPaneProcessamento.setViewportView(this.graficoBarraProcessamento);
        this.jScrollPaneComunicacao.setViewportView(this.graficoBarraComunicacao);
        this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempo);

    }

    private void initComponents() {

        this.jTabbedPaneGrid = new JTabbedPane();
        final JPanel jPanelGlobal = new JPanel();
        final JToolBar jToolBar1 = new JToolBar();
        final AbstractButton jButtonSalvar = new JButton();
        final AbstractButton jButtonSalvarTraces = new JButton();
        final JScrollPane jScrollPaneGobal =
                new JScrollPane();
        this.jTextAreaGlobal = new JTextArea();
        final JScrollPane jScrollPaneTarefa =
                new JScrollPane();
        this.jTextAreaTarefa = new JTextArea();
        this.jScrollPaneUsuario = new JScrollPane();
        this.jTextAreaUsuario = new JTextArea();
        final JScrollPane jScrollPaneRecurso =
                new JScrollPane();
        final Object[] colunas = { "Label", "Owner", "Processing performed",
                "Communication performed" };
        final javax.swing.JTable jTableRecurso = new javax.swing.JTable();
        final JPanel jPanelProcessamento = new JPanel();
        final JToolBar jToolBarProcessamento =
                new JToolBar();
        final AbstractButton jButtonPBarra = new JButton();
        final AbstractButton jButtonPPizza = new JButton();
        this.jScrollPaneProcessamento = new JScrollPane();
        final JPanel jPanelComunicacao = new JPanel();
        final JToolBar jToolBarComunicacao =
                new JToolBar();
        final AbstractButton jButtonCBarra = new JButton();
        final AbstractButton jButtonCPizza = new JButton();
        this.jScrollPaneComunicacao = new JScrollPane();
        final JPanel jPanelProcessamentoTempo =
                new JPanel();
        final JToolBar jToolBarProcessamentoTempo =
                new JToolBar();
        final AbstractButton jButtonProcessamentoUser =
                new JButton();
        this.jButtonProcessamentoMaquina = new JButton();
        this.jButtonProcessamentoTarefa = new JButton();
        this.jScrollPaneProcessamentoTempo = new JScrollPane();
        final Component jTabbedPane2 = new JTabbedPane();

        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setTitle("Simulation Results");
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(this.getResource(
                "imagens/Logo_iSPD_25.png")));
        this.jButtonProcessBarra = new JButton();
        this.jButtonCommunicationBarra = new JButton();
        this.jButtonExperimental = new JButton();
        this.jScrollPaneCharts = new JScrollPane();
        final JToolBar jToolBar3 = new JToolBar();
        this.jButtonProcessPizza = new JButton();
        this.jButtonCommunicationPizza = new JButton();
        final JToolBar jToolBar4 = new JToolBar();
        this.jButtonUserModelo1 = new JButton();
        this.jButtonUserModelo2 = new JButton();
        this.jButtonProcessamentoMaquina = new JButton();
        this.jButtonProcessamentoTarefa = new JButton();
        final JToolBar jToolBar2 = new JToolBar();
        final AbstractButton jButtonPreemption = new JButton();
        final AbstractButton jButtonUsage1 = new JButton();
        final AbstractButton jButtonUsage2 = new JButton();
        this.jPanel1 = new JPanel();
        final JToolBar jToolBarTask = new JToolBar();
        final JLabel jLabel1 = new JLabel();
        this.jTextFieldLerID = new JTextField();
        final AbstractButton jButtonGerarGraficoTarefa =
                new JButton();
        this.jScrollPaneGraficoTarefa = new JScrollPane();
        final JToolBar jToolBar5 = new JToolBar();
        final JLabel jLabel2 = new JLabel();
        this.jTextFieldLerID1 = new JTextField();
        final AbstractButton jButtonGerarGraficoMaquina =
                new JButton();

        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setTitle("Simulation Results");

        jToolBar1.setRollover(true);

        jButtonSalvar.setIcon(new ImageIcon(this.getResource("/ispd/gui" +
                                                             "/imagens" +
                                                             "/document" +
                                                             "-save_1.png")));
        jButtonSalvar.setToolTipText("Save results as HTML");
        jButtonSalvar.setFocusable(false);
        jButtonSalvar.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonSalvar.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonSalvar.addActionListener(this::jButtonSalvarActionPerformed);
        jToolBar1.add(jButtonSalvar);

        jButtonSalvarTraces.setText("Save traces");
        jButtonSalvarTraces.setToolTipText("Save a trace file of simulaton");
        jButtonSalvarTraces.setFocusable(false);
        jButtonSalvarTraces.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonSalvarTraces.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonSalvarTraces.addActionListener(this::jButtonSalvarTracesActionPerformed);
        jToolBar1.add(jButtonSalvarTraces);

        this.jTextAreaGlobal.setEditable(false);
        this.jTextAreaGlobal.setColumns(20);
        this.jTextAreaGlobal.setFont(ResultsDialog.COURIER_NEW_FONT_BOLD);

        this.jTextAreaGlobal.setRows(5);
        jScrollPaneGobal.setViewportView(this.jTextAreaGlobal);

        final GroupLayout jPanelGlobalLayout =
                new GroupLayout(jPanelGlobal);
        jPanelGlobal.setLayout(jPanelGlobalLayout);
        jPanelGlobalLayout.setHorizontalGroup(
                jPanelGlobalLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(jToolBar1,
                                GroupLayout.Alignment.TRAILING,
                                GroupLayout.DEFAULT_SIZE, 651,
                                Short.MAX_VALUE)
                        .addGroup(jPanelGlobalLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(GroupLayout.Alignment.TRAILING,
                                        jPanelGlobalLayout.createSequentialGroup()
                                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jScrollPaneGobal,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        627,
                                                        GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addComponent(jToolBar1,
                                GroupLayout.Alignment.TRAILING,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                        .addGroup(jPanelGlobalLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPaneGobal,
                                        GroupLayout.DEFAULT_SIZE,
                                        637, Short.MAX_VALUE)
                                .addContainerGap())
        );
        jPanelGlobalLayout.setVerticalGroup(
                jPanelGlobalLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelGlobalLayout.createSequentialGroup()
                                .addComponent(jToolBar1,
                                        GroupLayout.PREFERRED_SIZE, 25,
                                        GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 316, Short.MAX_VALUE))
                        .addGroup(jPanelGlobalLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(GroupLayout.Alignment.TRAILING,
                                        jPanelGlobalLayout.createSequentialGroup()
                                                .addContainerGap(31,
                                                        Short.MAX_VALUE)
                                                .addComponent(jScrollPaneGobal,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        298,
                                                        GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jTabbedPaneGrid.addTab("Global", jPanelGlobal);

        this.jTextAreaTarefa.setEditable(false);
        this.jTextAreaTarefa.setColumns(20);
        this.jTextAreaTarefa.setFont(ResultsDialog.COURIER_NEW_FONT);

        this.jTextAreaTarefa.setRows(5);
        jScrollPaneTarefa.setViewportView(this.jTextAreaTarefa);

        this.jTabbedPaneGrid.addTab("Tasks", jScrollPaneTarefa);

        this.jTextAreaUsuario.setColumns(20);
        this.jTextAreaUsuario.setEditable(false);
        this.jTextAreaUsuario.setFont(ResultsDialog.COURIER_NEW_FONT);

        this.jTextAreaUsuario.setRows(5);
        this.jScrollPaneUsuario.setViewportView(this.jTextAreaUsuario);

        this.jTabbedPaneGrid.addTab("User", this.jScrollPaneUsuario);

        jTableRecurso.setModel(new javax.swing.table.DefaultTableModel(this.resourceTable, colunas));
        jScrollPaneRecurso.setViewportView(jTableRecurso);

        this.jTabbedPaneGrid.addTab("Resources", jScrollPaneRecurso);

        jToolBarProcessamento.setRollover(true);

        jButtonPBarra.setText("Bar Chart");
        jButtonPBarra.setFocusable(false);
        jButtonPBarra.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonPBarra.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonPBarra.addActionListener(this::jButtonPBarraActionPerformed);
        jToolBarProcessamento.add(jButtonPBarra);

        jButtonPPizza.setText("Pie chart");
        jButtonPPizza.setFocusable(false);
        jButtonPPizza.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonPPizza.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonPPizza.addActionListener(this::jButtonPPizzaActionPerformed);
        jToolBarProcessamento.add(jButtonPPizza);

        final GroupLayout jPanelProcessamentoLayout =
                new GroupLayout(jPanelProcessamento);
//        jPanelProcessamento.setLayout(jPanelProcessamentoLayout);
//        jPanelProcessamentoLayout.setHorizontalGroup(
//                jPanelProcessamentoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addComponent(jToolBarProcessamento,
//                                GroupLayout.DEFAULT_SIZE, 651,
//                                Short.MAX_VALUE)
//                        .addComponent(this.jScrollPaneProcessamento,
//                                GroupLayout.DEFAULT_SIZE, 651,
//                                Short.MAX_VALUE)
//        );
//        jPanelProcessamentoLayout.setVerticalGroup(
//                jPanelProcessamentoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(jPanelProcessamentoLayout.createSequentialGroup()
//                                .addComponent(jToolBarProcessamento,
//                                        GroupLayout.PREFERRED_SIZE, 25,
//                                        GroupLayout.PREFERRED_SIZE)
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                .addComponent(this.jScrollPaneProcessamento,
//                                        GroupLayout.DEFAULT_SIZE,
//                                        310, Short.MAX_VALUE))
//        );

        this.jTabbedPaneGrid.addTab("Chart of the processing",
                jPanelProcessamento);

        jToolBarComunicacao.setRollover(true);

        jButtonCBarra.setText("Bar Chart");
        jButtonCBarra.setFocusable(false);
        jButtonCBarra.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonCBarra.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonCBarra.addActionListener(this::jButtonCBarraActionPerformed);
        jToolBarComunicacao.add(jButtonCBarra);

        jButtonCPizza.setText("Pie chart");
        jButtonCPizza.setFocusable(false);
        jButtonCPizza.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonCPizza.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonCPizza.addActionListener(this::jButtonCPizzaActionPerformed);
        jToolBarComunicacao.add(jButtonCPizza);

        final GroupLayout jPanelComunicacaoLayout =
                new GroupLayout(jPanelComunicacao);
        jPanelComunicacao.setLayout(jPanelComunicacaoLayout);
        jPanelComunicacaoLayout.setHorizontalGroup(
                jPanelComunicacaoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(jToolBarComunicacao,
                                GroupLayout.DEFAULT_SIZE, 651,
                                Short.MAX_VALUE)
                        .addComponent(this.jScrollPaneComunicacao,
                                GroupLayout.DEFAULT_SIZE, 651,
                                Short.MAX_VALUE)
        );
        jPanelComunicacaoLayout.setVerticalGroup(
                jPanelComunicacaoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelComunicacaoLayout.createSequentialGroup()
                                .addComponent(jToolBarComunicacao,
                                        GroupLayout.PREFERRED_SIZE, 25,
                                        GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(this.jScrollPaneComunicacao,
                                        GroupLayout.DEFAULT_SIZE,
                                        310, Short.MAX_VALUE))
        );

        this.jTabbedPaneGrid.addTab("Chart of the communication",
                jPanelComunicacao);

        jToolBarProcessamentoTempo.setRollover(true);

        jButtonProcessamentoUser.setText("Per user");
        jButtonProcessamentoUser.setFocusable(false);
        jButtonProcessamentoUser.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonProcessamentoUser.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonProcessamentoUser.addActionListener(this::jButtonProcessamentoUserActionPerformed);
        jToolBarProcessamentoTempo.add(jButtonProcessamentoUser);

        this.jButtonProcessamentoMaquina.setText("Per machine");
        jTabbedPaneGrid.addTab("Resources", jScrollPaneRecurso);

        jToolBarProcessamento.setFloatable(false);
        jToolBarProcessamento.setRollover(true);

        this.jButtonProcessBarra.setText("Process 1");
        this.jButtonProcessBarra.setFocusable(false);
        this.jButtonProcessBarra.setHorizontalTextPosition(SwingConstants.CENTER);
        this.jButtonProcessBarra.setVerticalTextPosition(SwingConstants.BOTTOM);
        this.jButtonProcessBarra.addActionListener(this::jButtonProcessBarraActionPerformed);
        jToolBarProcessamento.add(this.jButtonProcessBarra);

        this.jButtonCommunicationBarra.setText("Network 1");
        this.jButtonCommunicationBarra.setFocusable(false);
        this.jButtonCommunicationBarra.setHorizontalTextPosition(SwingConstants.CENTER);
        this.jButtonCommunicationBarra.setVerticalTextPosition(SwingConstants.BOTTOM);
        this.jButtonCommunicationBarra.addActionListener(this::jButtonCommunicationBarraActionPerformed);
        jToolBarProcessamento.add(this.jButtonCommunicationBarra);

        this.jButtonExperimental.setText(".");
        this.jButtonExperimental.setFocusable(false);
        this.jButtonExperimental.setHorizontalTextPosition(SwingConstants.CENTER);
        this.jButtonExperimental.setVerticalTextPosition(SwingConstants.BOTTOM);
        this.jButtonExperimental.addActionListener(this::jButtonExperimentalActionPerformed);
        jToolBarProcessamento.add(this.jButtonExperimental);

        jToolBar3.setFloatable(false);
        jToolBar3.setRollover(true);

        this.jButtonProcessPizza.setText("Process 2");
        this.jButtonProcessPizza.setFocusable(false);
        this.jButtonProcessPizza.setHorizontalTextPosition(SwingConstants.CENTER);
        this.jButtonProcessPizza.setVerticalTextPosition(SwingConstants.BOTTOM);
        this.jButtonProcessPizza.addActionListener(this::jButtonProcessPizzaActionPerformed);
        jToolBar3.add(this.jButtonProcessPizza);

        this.jButtonCommunicationPizza.setText("Network 2");
        this.jButtonCommunicationPizza.setFocusable(false);
        this.jButtonCommunicationPizza.setHorizontalTextPosition(SwingConstants.CENTER);
        this.jButtonCommunicationPizza.setVerticalTextPosition(SwingConstants.BOTTOM);
        this.jButtonCommunicationPizza.addActionListener(this::jButtonCommunicationPizzaActionPerformed);
        jToolBar3.add(this.jButtonCommunicationPizza);

        jToolBar4.setFloatable(false);
        jToolBar4.setRollover(true);

        this.jButtonUserModelo1.setText("User 1");
        this.jButtonUserModelo1.setFocusable(false);
        this.jButtonUserModelo1.setHorizontalTextPosition(SwingConstants.CENTER);
        this.jButtonUserModelo1.setVerticalTextPosition(SwingConstants.BOTTOM);
        this.jButtonUserModelo1.addActionListener(this::jButtonUserModelo1ActionPerformed);
        jToolBar4.add(this.jButtonUserModelo1);

        this.jButtonUserModelo2.setText("User 2");
        this.jButtonUserModelo2.setFocusable(false);
        this.jButtonUserModelo2.setHorizontalTextPosition(SwingConstants.CENTER);
        this.jButtonUserModelo2.setVerticalTextPosition(SwingConstants.BOTTOM);
        this.jButtonUserModelo2.addActionListener(this::jButtonUserModelo2ActionPerformed);
        jToolBar4.add(this.jButtonUserModelo2);

        this.jButtonProcessamentoMaquina.setText("Machine");
        this.jButtonProcessamentoMaquina.setFocusable(false);
        this.jButtonProcessamentoMaquina.setHorizontalTextPosition(SwingConstants.CENTER);
        this.jButtonProcessamentoMaquina.setVerticalTextPosition(SwingConstants.BOTTOM);
        this.jButtonProcessamentoMaquina.addActionListener(this::jButtonProcessamentoMaquinaActionPerformed);
        jToolBarProcessamentoTempo.add(this.jButtonProcessamentoMaquina);

        this.jButtonProcessamentoTarefa.setText("Per task");
        jToolBar4.add(this.jButtonProcessamentoMaquina);

        this.jButtonProcessamentoTarefa.setText("Task");
        this.jButtonProcessamentoTarefa.setFocusable(false);
        this.jButtonProcessamentoTarefa.setHorizontalTextPosition(SwingConstants.CENTER);
        this.jButtonProcessamentoTarefa.setVerticalTextPosition(SwingConstants.BOTTOM);
        this.jButtonProcessamentoTarefa.addActionListener(this::jButtonProcessamentoTarefaActionPerformed);
        jToolBarProcessamentoTempo.add(this.jButtonProcessamentoTarefa);

        final GroupLayout jPanelProcessamentoTempoLayout =
                new GroupLayout(jPanelProcessamentoTempo);
        jPanelProcessamentoTempo.setLayout(jPanelProcessamentoTempoLayout);
        jPanelProcessamentoTempoLayout.setHorizontalGroup(
                jPanelProcessamentoTempoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(jToolBarProcessamentoTempo,
                                GroupLayout.DEFAULT_SIZE, 651,
                                Short.MAX_VALUE)
                        .addComponent(this.jScrollPaneProcessamentoTempo,
                                GroupLayout.DEFAULT_SIZE, 651,
                                Short.MAX_VALUE)
        );
        jPanelProcessamentoTempoLayout.setVerticalGroup(
                jPanelProcessamentoTempoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelProcessamentoTempoLayout.createSequentialGroup()
                                .addComponent(jToolBarProcessamentoTempo,
                                        GroupLayout.PREFERRED_SIZE, 25,
                                        GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(this.jScrollPaneProcessamentoTempo,
                                        GroupLayout.DEFAULT_SIZE,
                                        310, Short.MAX_VALUE))
        );

        this.jTabbedPaneGrid.addTab("Use of computing power through time",
                jPanelProcessamentoTempo);
        jToolBar4.add(this.jButtonProcessamentoTarefa);

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);

        jButtonPreemption.setText("Preemption");
        jButtonPreemption.setFocusable(false);
        jButtonPreemption.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonPreemption.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonPreemption.addActionListener(this::jButtonPreemptionActionPerformed);
        jToolBar2.add(jButtonPreemption);

        jButtonUsage1.setText("Usage 1");
        jButtonUsage1.setFocusable(false);
        jButtonUsage1.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonUsage1.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonUsage1.addActionListener(this::jButtonUsage1ActionPerformed);
        jToolBar2.add(jButtonUsage1);

        jButtonUsage2.setText("Usage 2");
        jButtonUsage2.setFocusable(false);
        jButtonUsage2.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonUsage2.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonUsage2.addActionListener(this::jButtonUsage2ActionPerformed);
        jToolBar2.add(jButtonUsage2);

        jPanelProcessamento.setLayout(jPanelProcessamentoLayout);
        jPanelProcessamentoLayout.setHorizontalGroup(
                jPanelProcessamentoLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(this.jScrollPaneCharts)
                        .addGroup(jPanelProcessamentoLayout.createSequentialGroup()
                                .addComponent(jToolBarProcessamento,
                                        GroupLayout.DEFAULT_SIZE,
                                        205, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jToolBar3,
                                        GroupLayout.DEFAULT_SIZE,
                                        190, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jToolBar4,
                                        GroupLayout.DEFAULT_SIZE,
                                        254, Short.MAX_VALUE))
                        .addComponent(jToolBar2,
                                GroupLayout.Alignment.LEADING,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
        );
        jPanelProcessamentoLayout.setVerticalGroup(
                jPanelProcessamentoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelProcessamentoLayout.createSequentialGroup()
                                .addGroup(jPanelProcessamentoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(jToolBarProcessamento,
                                                GroupLayout.PREFERRED_SIZE,
                                                25, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jToolBar3,
                                                GroupLayout.PREFERRED_SIZE,
                                                25, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jToolBar4,
                                                GroupLayout.PREFERRED_SIZE,
                                                25, GroupLayout.PREFERRED_SIZE))
                                .addGap(2, 2, 2)
                                .addComponent(jToolBar2,
                                        GroupLayout.PREFERRED_SIZE, 25,
                                        GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(this.jScrollPaneCharts,
                                        GroupLayout.DEFAULT_SIZE,
                                        335, Short.MAX_VALUE))
        );

        jTabbedPaneGrid.addTab("Charts", jPanelProcessamento);

        jToolBarTask.setRollover(true);

        jLabel1.setText("Task Graph:");
        jToolBarTask.add(jLabel1);

        this.jTextFieldLerID.setColumns(20);
        this.jTextFieldLerID.setHorizontalAlignment(SwingConstants.CENTER);
        this.jTextFieldLerID.setText("Type the task ID (Integer number)");
        this.jTextFieldLerID.addActionListener(this::jTextFieldLerIDActionPerformed);
        this.jTextFieldLerID.addFocusListener(new SomeFocusAdapter());
        jToolBarTask.add(this.jTextFieldLerID);

        jButtonGerarGraficoTarefa.setText("Generate Graph");
        jButtonGerarGraficoTarefa.addActionListener(this::jButtonGerarGraficoTarefaActionPerformed);
        jToolBarTask.add(jButtonGerarGraficoTarefa);

        jToolBar5.setRollover(true);

        jLabel2.setText("Machine Graph:");
        jToolBar5.add(jLabel2);

        this.jTextFieldLerID1.setColumns(20);
        this.jTextFieldLerID1.setHorizontalAlignment(SwingConstants.CENTER);
        this.jTextFieldLerID1.setText("Type the machine name");
        this.jTextFieldLerID1.addActionListener(this::jTextFieldLerID1ActionPerformed);
        this.jTextFieldLerID1.addFocusListener(new SomeOtherFocusAdapter());
        jToolBar5.add(this.jTextFieldLerID1);

        jButtonGerarGraficoMaquina.setText("Generate Graph");
        jButtonGerarGraficoMaquina.setFocusable(false);
        jButtonGerarGraficoMaquina.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonGerarGraficoMaquina.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonGerarGraficoMaquina.addActionListener(this::jButtonGerarGraficoMaquinaActionPerformed);
        jToolBar5.add(jButtonGerarGraficoMaquina);

        final GroupLayout jPanel1Layout =
                new GroupLayout(this.jPanel1);
        this.jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(jToolBarTask,
                                GroupLayout.DEFAULT_SIZE, 661,
                                Short.MAX_VALUE)
                        .addComponent(this.jScrollPaneGraficoTarefa)
                        .addComponent(jToolBar5,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jToolBarTask,
                                        GroupLayout.PREFERRED_SIZE, 25,
                                        GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jToolBar5,
                                        GroupLayout.PREFERRED_SIZE, 25,
                                        GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(this.jScrollPaneGraficoTarefa,
                                        GroupLayout.DEFAULT_SIZE,
                                        331, Short.MAX_VALUE))
        );

        jTabbedPaneGrid.addTab("Individual Graphs",
                this.jPanel1);

        final GroupLayout layout =
                new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(this.jTabbedPaneGrid)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(jTabbedPane2,
                                                GroupLayout.PREFERRED_SIZE,
                                                100, GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(this.jTabbedPaneGrid,
                                GroupLayout.DEFAULT_SIZE, 386,
                                Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(jTabbedPane2,
                                                GroupLayout.PREFERRED_SIZE,
                                                100, GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE)))
        );

        this.pack();
    }

    private static String getResultadosGlobais(final MetricasGlobais globais) {
        String texto = "\t\tSimulation Results\n\n";
        texto += String.format("\tTotal Simulated Time = %g \n",
                globais.getTempoSimulacao());
        texto += String.format("\tSatisfaction = %g %%\n",
                globais.getSatisfacaoMedia());
        texto += String.format("\tIdleness of processing resources = %g %%\n"
                , globais.getOciosidadeComputacao());
        texto += String.format("\tIdleness of communication resources = %g " +
                               "%%\n", globais.getOciosidadeComunicacao());
        texto += String.format("\tEfficiency = %g %%\n",
                globais.getEficiencia());
        if (globais.getEficiencia() > 70.0) {
            texto += "\tEfficiency GOOD\n ";
        } else if (globais.getEficiencia() > 40.0) {
            texto += "\tEfficiency MEDIA\n ";
        } else {
            texto += "\tEfficiency BAD\n ";
        }
        return texto;
    }

    private void setResultadosUsuario(final MetricasUsuarios metricasUsuarios,
                                      final Metricas metricas) {
        if (metricasUsuarios != null && metricasUsuarios.getUsuarios().size() > 1) {
            String texto = "";
            for (int i = 0; i < metricasUsuarios.getUsuarios().size(); i++) {
                final String userName = metricasUsuarios.getUsuarios().get(i);
                texto += "\n\n\t\tUser " + userName + "\n";
                final HashSet set =
                        metricasUsuarios.getTarefasConcluidas(userName);
                texto += "\nNumber of task: " + set.size() + "\n";
                //Applications:
                //Name: Number of task: Mflops:
                double tempoMedioFilaComunicacao = 0;
                double tempoMedioComunicacao = 0;
                final double tempoMedioSistemaComunicacao;
                double tempoMedioFilaProcessamento = 0;
                double tempoMedioProcessamento = 0;
                final double tempoMedioSistemaProcessamento;
                int numTarefas = 0;
                for (final Tarefa no :
                        metricasUsuarios.getTarefasConcluidas(userName)) {
                    tempoMedioFilaComunicacao += no.getMetricas().getTempoEsperaComu();
                    tempoMedioComunicacao += no.getMetricas().getTempoComunicacao();
                    tempoMedioFilaProcessamento =
                            no.getMetricas().getTempoEsperaProc();
                    tempoMedioProcessamento =
                            no.getMetricas().getTempoProcessamento();
                    numTarefas++;
                }
                tempoMedioFilaComunicacao =
                        tempoMedioFilaComunicacao / numTarefas;
                tempoMedioComunicacao = tempoMedioComunicacao / numTarefas;
                tempoMedioFilaProcessamento =
                        tempoMedioFilaProcessamento / numTarefas;
                tempoMedioProcessamento = tempoMedioProcessamento / numTarefas;
                tempoMedioSistemaComunicacao =
                        tempoMedioFilaComunicacao + tempoMedioComunicacao;
                tempoMedioSistemaProcessamento =
                        tempoMedioFilaProcessamento + tempoMedioProcessamento;
                texto += "\n Communication \n";
                texto += String.format("    Queue average time: %g seconds" +
                                       ".\n", tempoMedioFilaComunicacao);
                texto += String.format("    Communication average time: %g " +
                                       "seconds.\n", tempoMedioComunicacao);
                texto += String.format("    System average time: %g seconds" +
                                       ".\n", tempoMedioSistemaComunicacao);
                texto += "\n Processing \n";
                texto += String.format("    Queue average time: %g seconds" +
                                       ".\n", tempoMedioFilaProcessamento);
                texto += String.format("    Processing average time: %g " +
                                       "seconds.\n", tempoMedioProcessamento);
                texto += String.format("    System average time: %g seconds" +
                                       ".\n", tempoMedioSistemaProcessamento);
            }
            texto += """

                    Satisfação dos usuários em porcentagem
                    """;
            for (final Map.Entry<String, Double> entry :
                    metricas.getMetricasSatisfacao().entrySet()) {

                final String user = entry.getKey();
                final Double satisfacao = entry.getValue();
                texto += user + " : " + satisfacao + " %\n";

            }
            this.jTextAreaUsuario.setText(texto);
        } else {
            this.jTabbedPaneGrid.remove(this.jScrollPaneUsuario);
        }
    }

    //Cria o gráfico que demonstra o uso de cada recurso do sistema através
    // do tempo.
    //Ele recebe como parâmetro a lista com as maquinas que processaram
    // durante a simulação.
    private JFreeChart criarGraficoProcessamentoTempo(final RedeDeFilas rdf) {
        final XYSeriesCollection dadosGrafico = new XYSeriesCollection();
        //Se tiver alguma máquina na lista.
        if (rdf.getMaquinas() != null) {
            //Laço foreach que percorre as máquinas.
            for (final CS_Processamento maq : rdf.getMaquinas()) {
                //Lista que recebe os pares de intervalo de tempo em que a
                // máquina executou.
                final List<ParesOrdenadosUso> lista =
                        maq.getListaProcessamento();
                this.poderComputacionalTotal += (maq.getPoderComputacional() - (maq.getOcupacao() * maq.getPoderComputacional()));
                //Se a máquina tiver intervalos.
                if (!lista.isEmpty()) {
                    //Cria o objeto do tipo XYSeries.
                    final XYSeries tmp_series;
                    //Se o atributo numeroMaquina for 0, ou seja, não for um
                    // nó de um cluster.
                    if (maq.getnumeroMaquina() == 0) //Estancia com o nome puro.
                    {
                        tmp_series = new XYSeries(maq.getId());
                    } //Se for 1 ou mais, ou seja, é um nó de cluster.
                    else //Estancia tmp_series com o nome concatenado com a
                    // palavra node e seu numero.
                    {
                        tmp_series =
                                new XYSeries(maq.getId() + " node " + maq.getnumeroMaquina());
                    }
                    int i;
                    //Laço que vai adicionando os pontos para a criação do
                    // gráfico.
                    for (i = 0; i < lista.size(); i++) {
                        //Calcula o uso, que é 100% - taxa de ocupação inicial.
                        Double uso = 100 - (maq.getOcupacao() * 100);
                        //Adiciona ponto inicial.
                        tmp_series.add(lista.get(i).getInicio(), uso);
                        //Adiciona ponto final.
                        tmp_series.add(lista.get(i).getFim(), uso);
                        if (i + 1 != lista.size()) {
                            uso = 0.0000;
                            tmp_series.add(lista.get(i).getFim(), uso);
                            tmp_series.add(lista.get(i + 1).getInicio(), uso);
                        }
                    }
                    //Add no gráfico.
                    dadosGrafico.addSeries(tmp_series);
                }
            }
        }

        return ChartFactory.createXYAreaChart(
                "Use of computing power through time "
                + "\nMachines", //Titulo
                "Time (seconds)", // Eixo X
                "Rate of use of computing power for each node (%)", //Eixo Y
                dadosGrafico, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false);
    }

    private JFreeChart criarGraficoProcessamentoTempoTarefa(final Collection<
            ? extends Tarefa> tarefas) {

        final XYSeriesCollection dadosGrafico = new XYSeriesCollection();
        if (!tarefas.isEmpty()) {
            for (final Tarefa task : tarefas) {
                final XYSeries tmp_series;
                tmp_series = new XYSeries("task " + task.getIdentificador());
                final CS_Processamento temp =
                        (CS_Processamento) task.getLocalProcessamento();
                if (temp != null) {
                    final Double uso =
                            (temp.getPoderComputacional() / this.poderComputacionalTotal) * 100;
                    for (int j = 0; j < task.getTempoInicial().size(); j++) {
                        tmp_series.add(task.getTempoInicial().get(j),
                                (Double) 0.0);
                        tmp_series.add(task.getTempoInicial().get(j), uso);
                        tmp_series.add(task.getTempoFinal().get(j), uso);
                        tmp_series.add(task.getTempoFinal().get(j),
                                (Double) 0.0);
                    }
                    dadosGrafico.addSeries(tmp_series);
                }
            }

        }

        return ChartFactory.createXYAreaChart(
                "Use of total computing power through time "
                + "\nTasks", //Titulo
                "Time (seconds)", // Eixo X
                "Rate of total use of computing power (%)", //Eixo Y
                dadosGrafico, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false);
    }

    private JFreeChart[] gerarGraficoProcessamentoTempoUser(final Collection<
            ? extends Tarefa> tarefas, final RedeDeFilas rdf) {
        final List<UserOperationTime> lista = new ArrayList<>();
        final int numberUsers = rdf.getUsuarios().size();
        final Map<String, Integer> users = new HashMap<>();
        final XYSeries[] tmp_series = new XYSeries[numberUsers];
        final XYSeries[] tmp_series1 = new XYSeries[numberUsers];
        final Double[] utilizacaoUser = new Double[numberUsers];
        final Double[] utilizacaoUser1 = new Double[numberUsers];
        final XYSeriesCollection dadosGrafico = new XYSeriesCollection();
        final XYSeriesCollection dadosGrafico1 = new XYSeriesCollection();
        for (int i = 0; i < numberUsers; i++) {
            users.put(rdf.getUsuarios().get(i), i);
            utilizacaoUser[i] = 0.0;
            tmp_series[i] = new XYSeries(rdf.getUsuarios().get(i));
            utilizacaoUser1[i] = 0.0;
            tmp_series1[i] = new XYSeries(rdf.getUsuarios().get(i));
        }
        if (!tarefas.isEmpty()) {
            //Insere cada tarefa como dois pontos na lista
            for (final Tarefa task : tarefas) {
                final CS_Processamento local =
                        (CS_Processamento) task.getLocalProcessamento();
                if (local != null) {

                    for (int i = 0; i < task.getTempoInicial().size(); i++) {
                        final Double uso =
                                (task.getHistoricoProcessamento().get(i).getPoderComputacional() / this.poderComputacionalTotal) * 100;
                        final UserOperationTime provisorio1 =
                                new UserOperationTime(task.getTempoInicial().get(i), true, uso, users.get(task.getProprietario()));
                        lista.add(provisorio1);
                        final UserOperationTime provisorio2 =
                                new UserOperationTime(task.getTempoFinal().get(i), false, uso, users.get(task.getProprietario()));
                        lista.add(provisorio2);
                    }
                }
            }
            //Ordena lista
            Collections.sort(lista);
        }
        for (final UserOperationTime UserOperationTime : lista) {
            final int usuario = UserOperationTime.getUserId();
            //Altera os valores do usuario atual e todos acima dele
            for (int j = usuario; j < numberUsers; j++) {
                //Salva valores anteriores
                tmp_series[j].add(UserOperationTime.getTime(),
                        utilizacaoUser[j]);
                if (UserOperationTime.isStartTime()) {
                    utilizacaoUser[j] += UserOperationTime.getNodeUse();
                } else {
                    utilizacaoUser[j] -= UserOperationTime.getNodeUse();
                }
                //Novo valor
                tmp_series[j].add(UserOperationTime.getTime(),
                        utilizacaoUser[j]);
            }
            //Grafico1
            tmp_series1[usuario].add(UserOperationTime.getTime(),
                    utilizacaoUser1[usuario]);
            if (UserOperationTime.isStartTime()) {
                utilizacaoUser1[usuario] += UserOperationTime.getNodeUse();
            } else {
                utilizacaoUser1[usuario] -= UserOperationTime.getNodeUse();
            }
            tmp_series1[usuario].add(UserOperationTime.getTime(),
                    utilizacaoUser1[usuario]);
        }
        for (int i = 0; i < numberUsers; i++) {
            dadosGrafico.addSeries(tmp_series[i]);
            dadosGrafico1.addSeries(tmp_series1[i]);
        }
        final JFreeChart[] saida = new JFreeChart[2];
        saida[0] = ChartFactory.createXYAreaChart(
                "Use of total computing power through time"
                + "\nUsers", //Titulo
                "Time (seconds)", // Eixo X
                "Rate of total use of computing power (%)", //Eixo Y
                dadosGrafico1, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false); // exibir: legendas, tooltips, url

        saida[1] = ChartFactory.createXYLineChart(
                "Use of total computing power through time"
                + "\nUsers", //Titulo
                "Time (seconds)", // Eixo X
                "Rate of total use of computing power (%)", //Eixo Y
                dadosGrafico, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false); // exibir: legendas, tooltips, url
        final XYPlot xyplot = (XYPlot) saida[1].getPlot();
        xyplot.setDomainPannable(true);
        final AbstractXYItemRenderer xysteparearenderer =
                new XYStepAreaRenderer(2);
        xysteparearenderer.setDataBoundsIncludesVisibleSeriesOnly(false);
        xysteparearenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        xysteparearenderer.setDefaultEntityRadius(6);
        xyplot.setRenderer(xysteparearenderer);

        return saida;
    }

    private URL getResource(final String name) {
        return this.getClass().getResource(name);
    }

    private void jButtonSalvarActionPerformed(final java.awt.event.ActionEvent evt) {
        final JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        final int returnVal = jFileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File file = jFileChooser.getSelectedFile();
            this.salvarHTML(file);
            try {
                HtmlPane.openDefaultBrowser(new URL("file://" + file.getAbsolutePath() + "/result.html"));
            } catch (final MalformedURLException ex) {
                Logger.getLogger(ResultsDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void jButtonSalvarTracesActionPerformed(final java.awt.event.ActionEvent evt) {
        final FileFilter filtro = new MultipleExtensionFileFilter("Workload Model of " +
                                                                  "Simulation", ".wmsx",
                true);
        final JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileFilter(filtro);
        final int returnVal = jFileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser.getSelectedFile();
            if (!file.getName().endsWith(".wmsx")) {
                file = new File(file + ".wmsx");
            }
            final TraceXML interpret = new TraceXML(file.getAbsolutePath());
            interpret.geraTraceSim(this.tarefas);
        }
    }

    private void jButtonPBarraActionPerformed(final java.awt.event.ActionEvent evt) {

        this.jScrollPaneCharts.setViewportView(this.graficoBarraProcessamento);
    }

    private void jButtonPPizzaActionPerformed(final java.awt.event.ActionEvent evt) {

        this.jScrollPaneCharts.setViewportView(this.graficoPizzaProcessamento);
    }

    private void jButtonCBarraActionPerformed(final java.awt.event.ActionEvent evt) {

        this.jScrollPaneComunicacao.setViewportView(this.graficoBarraComunicacao);
    }

    private void jButtonCPizzaActionPerformed(final java.awt.event.ActionEvent evt) {

        this.jScrollPaneComunicacao.setViewportView(this.graficoPizzaComunicacao);
    }

    private void jButtonProcessamentoUserActionPerformed(final java.awt.event.ActionEvent evt) {
        if (this.jScrollPaneProcessamentoTempo.getViewport().getView() != this.graficoProcessamentoTempoUser1) {
            this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempoUser1);
        } else {
            this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempoUser2);
        }
    }

    private void jButtonProcessBarraActionPerformed(final java.awt.event.ActionEvent evt) {

        this.jScrollPaneCharts.setViewportView(this.charts.getProcessingBarChart());
    }

    private void jButtonCommunicationBarraActionPerformed(final java.awt.event.ActionEvent evt) {

        this.jScrollPaneCharts.setViewportView(this.charts.getCommunicationBarChart());
    }

    private void jButtonExperimentalActionPerformed(final java.awt.event.ActionEvent evt) {
        this.jScrollPaneCharts.setViewportView(new ChartPanel(null));
    }

    private void jButtonProcessPizzaActionPerformed(final java.awt.event.ActionEvent evt) {

        this.jScrollPaneCharts.setViewportView(this.charts.getProcessingPieChart());
    }

    private void jButtonCommunicationPizzaActionPerformed(final java.awt.event.ActionEvent evt) {

        this.jScrollPaneCharts.setViewportView(this.charts.getCommunicationPieChart());
    }

    private void jButtonUserModelo1ActionPerformed(final java.awt.event.ActionEvent evt) {
        this.jScrollPaneCharts.setViewportView(this.charts.getUserThroughTimeChart1());
    }

    private void jButtonUserModelo2ActionPerformed(final java.awt.event.ActionEvent evt) {
        this.jScrollPaneCharts.setViewportView(this.charts.getUserThroughTimeChart2());
    }

    private void jButtonProcessamentoMaquinaActionPerformed(final java.awt.event.ActionEvent evt) {
        this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempo);
        this.jScrollPaneCharts.setViewportView(this.charts.getMachineThroughTimeChart());
    }

    private void jButtonProcessamentoTarefaActionPerformed(final java.awt.event.ActionEvent evt) {
        this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempoTarefa);
        this.jScrollPaneCharts.setViewportView(this.charts.getTaskThroughTimeChart());
    }

    private void jButtonPreemptionActionPerformed(final java.awt.event.ActionEvent evt) {
        this.jScrollPaneCharts.setViewportView(this.charts.PreemptionPerUser);
    }

    private void jButtonUsage1ActionPerformed(final java.awt.event.ActionEvent evt) {
        this.jScrollPaneCharts.setViewportView(this.charts.criarGraficoAproveitamento(this.tarefas));
    }

    private void jButtonUsage2ActionPerformed(final java.awt.event.ActionEvent evt) {
        this.jScrollPaneCharts.setViewportView(this.charts.criarGraficoNumTarefasAproveitamento(this.tarefas));
    }

    private void jTextFieldLerIDActionPerformed(final java.awt.event.ActionEvent evt) {
        this.jButtonGerarGraficoTarefaActionPerformed(null);
    }

    private void jButtonGerarGraficoTarefaActionPerformed(final java.awt.event.ActionEvent evt) {
        try {
            final String ID = this.jTextFieldLerID.getText();
            final int task = Integer.parseInt(ID);
            if (task >= 0 && task < this.tarefas.size()) {
                this.jScrollPaneGraficoTarefa.setViewportView(this.charts.criarGraficoPorTarefa(this.tarefas, task));
            } else {
                JOptionPane.showMessageDialog(this.jPanel1, "Task not found. " +
                                                            "Your " +
                                                            "tasks go from 0 " +
                                                            "to " + (this.tarefas.size() - 1), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (final NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this.jPanel1, "The task id is an " +
                                                        "integer", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void jTextFieldLerID1ActionPerformed(final java.awt.event.ActionEvent evt) {
        this.jButtonGerarGraficoMaquinaActionPerformed(null);
    }

    private void jButtonGerarGraficoMaquinaActionPerformed(final java.awt.event.ActionEvent evt) {
        final String id = this.jTextFieldLerID1.getText();
        final ChartPanel grafico =
                this.charts.gerarGraficoPorMaquina(this.tarefas, id);
        if (grafico != null) {
            this.jScrollPaneGraficoTarefa.setViewportView(grafico);
        } else {
            JOptionPane.showMessageDialog(this.jPanel1, "Machine not Found",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Save results to html files, organized in given directory.
     *
     * @param dir Destination <b>Directory</b>
     */
    public void salvarHTML(final File dir) {
        //Gerar resultados:
        final BufferedImage[] chartsImagem = new BufferedImage[8];
        if (this.charts.getProcessingBarChart() != null) {
            chartsImagem[0] =
                    this.charts.getProcessingBarChart().getChart().createBufferedImage(1200, 600);
        }
        if (this.charts.getProcessingPieChart() != null) {
            chartsImagem[1] =
                    this.charts.getProcessingPieChart().getChart().createBufferedImage(1200, 600);
        }
        if (this.charts.getCommunicationBarChart() != null) {
            chartsImagem[2] =
                    this.charts.getCommunicationBarChart().getChart().createBufferedImage(1200, 600);
        }
        if (this.charts.getCommunicationPieChart() != null) {
            chartsImagem[3] =
                    this.charts.getCommunicationPieChart().getChart().createBufferedImage(1200, 600);
        }
        if (this.charts.getMachineThroughTimeChart() != null) {
            chartsImagem[4] =
                    this.charts.getMachineThroughTimeChart().getChart().createBufferedImage(1200, 600);
        }
        if (this.charts.getTaskThroughTimeChart() != null) {
            chartsImagem[5] =
                    this.charts.getTaskThroughTimeChart().getChart().createBufferedImage(1200, 600);
        }
        if (this.charts.getUserThroughTimeChart1() != null) {
            chartsImagem[6] =
                    this.charts.getUserThroughTimeChart1().getChart().createBufferedImage(1200, 600);
        }
        if (this.charts.getUserThroughTimeChart2() != null) {
            chartsImagem[7] =
                    this.charts.getUserThroughTimeChart2().getChart().createBufferedImage(1200, 600);
        }
        this.html.setCharts(chartsImagem);
        try {
            this.html.gerarHTML(dir);
        } catch (final IOException ex) {
            Logger.getLogger(ResultsDialog.class.getName()).log(Level.SEVERE,
                    null, ex);
        }
    }

    public ResultsDialog(final java.awt.Window parent, final Metricas metricas,
                         final RedeDeFilas rdf, final List<Tarefa> tarefas,
                         final ConfiguracaoISPD config) {
        super(parent, ModalityType.APPLICATION_MODAL);
        this.tarefas = tarefas;
        this.charts = new SimulationResultChartMaker();
        if (config.getCreateProcessingChart()) {
            this.charts.criarProcessamento(metricas.getMetricasProcessamento());
        }
        if (config.getCreateCommunicationChart()) {
            this.charts.criarComunicacao(metricas.getMetricasComunicacao());
        }
        this.resourceTable = ResultsDialog.setTabelaRecurso(metricas);
        this.initComponents();
        this.setButtons(config);
        this.jTextAreaGlobal.setText(ResultsDialog.getResultadosGlobais(metricas.getMetricasGlobais()));
        this.html.setMetricasGlobais(metricas.getMetricasGlobais());
        this.jTextAreaTarefa.setText(ResultsDialog.getResultadosTarefas(metricas));
        this.html.setMetricasTarefas(metricas);
        final CS_Mestre mestre = (CS_Mestre) rdf.getMestres().get(0);
        this.setResultadosUsuario(mestre.getEscalonador().getMetricaUsuarios(),
                metricas);

        if (rdf.getMaquinas().size() < 21) {
            this.graficoProcessamentoTempo =
                    new ChartPanel(this.criarGraficoProcessamentoTempo(rdf));
            this.graficoProcessamentoTempo.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);
        } else {
            this.jButtonProcessamentoMaquina.setVisible(false);
            for (final CS_Processamento maq : rdf.getMaquinas()) {
                this.poderComputacionalTotal += (maq.getPoderComputacional() - (maq.getOcupacao() * maq.getPoderComputacional()));
            }
        }
        if (tarefas.size() < 50) {
            this.graficoProcessamentoTempoTarefa =
                    new ChartPanel(this.criarGraficoProcessamentoTempoTarefa(tarefas));
            this.graficoProcessamentoTempoTarefa.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);
        } else {
            this.jButtonProcessamentoTarefa.setVisible(false);
        }
        final JFreeChart[] temp =
                this.gerarGraficoProcessamentoTempoUser(tarefas, rdf);
        this.graficoProcessamentoTempoUser1 = new ChartPanel(temp[0]);
        this.graficoProcessamentoTempoUser1.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);
        this.graficoProcessamentoTempoUser2 = new ChartPanel(temp[1]);
        this.graficoProcessamentoTempoUser2.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);

        if (config.getCreateMachineThroughTimeChart()) {
            this.charts.criarProcessamentoTempoMaquina(rdf);
        } else {
            this.charts.calculaPoderTotal(rdf);
        }
        if (config.getCreateTaskThroughTimeChart()) {
            this.charts.criarProcessamentoTempoTarefa(tarefas);
        }
        if (config.getCreateUserThroughTimeChart()) {
            this.charts.criarProcessamentoTempoUser(tarefas, rdf);
        }

        this.charts.criarGraficoPreempcao(rdf, tarefas);


        this.jScrollPaneProcessamento.setViewportView(this.graficoBarraProcessamento);
        this.jScrollPaneComunicacao.setViewportView(this.graficoBarraComunicacao);
        this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempo);

        this.jScrollPaneCharts.setViewportView(this.charts.getProcessingBarChart());

        this.jButtonExperimental.setVisible(false);
        this.charts.rede = rdf;
    }

    private void setButtons(final ConfiguracaoISPD config) {
        this.jButtonProcessBarra.setEnabled(config.getCreateProcessingChart());
        this.jButtonProcessPizza.setEnabled(config.getCreateProcessingChart());
        this.jButtonCommunicationBarra.setEnabled(config.getCreateCommunicationChart());
        this.jButtonCommunicationPizza.setEnabled(config.getCreateCommunicationChart());
        this.jButtonProcessamentoMaquina.setEnabled(config.getCreateMachineThroughTimeChart());
        this.jButtonProcessamentoTarefa.setEnabled(config.getCreateTaskThroughTimeChart());
        this.jButtonUserModelo1.setEnabled(config.getCreateUserThroughTimeChart());
        this.jButtonUserModelo2.setEnabled(config.getCreateUserThroughTimeChart());
    }

    private void jTextFieldLerIDFocusGained(final FocusEvent evt) {

        this.jTextFieldLerID.selectAll();
    }

    private void jTextFieldLerID1FocusGained(final FocusEvent evt) {

        this.jTextFieldLerID1.selectAll();
    }

    private class SomeFocusAdapter extends FocusAdapter {
        public void focusGained(final FocusEvent evt) {
            ResultsDialog.this.jTextFieldLerIDFocusGained(evt);
        }
    }

    private class SomeOtherFocusAdapter extends FocusAdapter {
        public void focusGained(final FocusEvent evt) {
            ResultsDialog.this.jTextFieldLerID1FocusGained(evt);
        }
    }
}