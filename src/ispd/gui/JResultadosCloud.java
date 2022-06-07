/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JResultados.java
 *
 * Created on 20/09/2011, 11:01:42
 */
package ispd.gui;

import ispd.gui.auxiliar.ParesOrdenadosUso;
import ispd.arquivo.SalvarResultadosHTML;
import ispd.arquivo.interpretador.cargas.Interpretador;
import ispd.gui.auxiliar.FiltroDeArquivos;
import ispd.gui.auxiliar.HtmlPane;
import ispd.motor.filas.RedeDeFilasCloud;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.metricas.Metricas;
import ispd.motor.metricas.MetricasComunicacao;
import ispd.motor.metricas.MetricasGlobais;
import ispd.motor.metricas.MetricasProcessamento;
import ispd.motor.metricas.MetricasUsuarios;
import ispd.motor.metricas.MetricasAlocacao;
import ispd.motor.metricas.MetricasCusto;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYStepAreaRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author denison_usuario
 */
public class JResultadosCloud extends javax.swing.JDialog {
   
    /**
     * Cria no JResultado sem carregar parte gráfica para utilizar no modo
     * terminal
     */
    public JResultadosCloud(Metricas metricas, RedeDeFilasCloud rdf, List tarefas) {
        html.setMetricasGlobais(metricas.getMetricasGlobais());
        tabelaRecurso = setTabelaRecurso(metricas);
        getResultadosTarefas(metricas);
        html.setMetricasTarefas(metricas);
        gerarGraficosProcessamento(metricas.getMetricasProcessamento());
        gerarGraficosComunicacao(metricas.getMetricasComunicacao());
        gerarGraficosAlocacao(metricas.getMetricasAlocacao());
        gerarGraficosCusto(metricas.getMetricasCusto());
        if (rdf.getVMs().size() < 21) {
            graficoProcessamentoTempo = new ChartPanel(criarGraficoProcessamentoTempo(rdf));
        } else {
            for (CS_Processamento maq : rdf.getVMs()) {
                poderComputacionalTotal += (maq.getPoderComputacional() - (maq.getOcupacao() * maq.getPoderComputacional()));
            }
        }
        if (tarefas.size() < 50) {
            graficoProcessamentoTempoTarefa = new ChartPanel(criarGraficoProcessamentoTempoTarefa(tarefas));
        }
        JFreeChart temp[] = gerarGraficoProcessamentoTempoUser(tarefas, rdf);
        graficoProcessamentoTempoUser1 = new ChartPanel(temp[0]);
        graficoProcessamentoTempoUser2 = new ChartPanel(temp[1]);
    }

    /**
     * Cria no JResultado sem carregar parte gráfica para utilizar no modo
     * terminal usando apenas a classe "Metricas"
     */
    public JResultadosCloud(Metricas metricas) {
        html.setMetricasGlobais(metricas.getMetricasGlobais());
        tabelaRecurso = setTabelaRecurso(metricas);
        getResultadosTarefas(metricas);
        html.setMetricasTarefas(metricas);
        gerarGraficosProcessamento(metricas.getMetricasProcessamento());
        gerarGraficosComunicacao(metricas.getMetricasComunicacao());
        gerarGraficosAlocacao(metricas.getMetricasAlocacao());
        gerarGraficosCusto(metricas.getMetricasCusto());
    }

    /**
     * Creates new form JResultados
     */
    public JResultadosCloud(java.awt.Frame parent, Metricas metricas, RedeDeFilasCloud rdf, List<Tarefa> tarefas) {
        super(parent, true);
        this.tarefas = tarefas;
        gerarGraficosProcessamento(metricas.getMetricasProcessamento());
        gerarGraficosComunicacao(metricas.getMetricasComunicacao());
        gerarGraficosAlocacao(metricas.getMetricasAlocacao());
        gerarGraficosCusto(metricas.getMetricasCusto());
        tabelaRecurso = setTabelaRecurso(metricas);
        initComponents();
        this.jTextAreaGlobal.setText(getResultadosGlobais(metricas.getMetricasGlobais()));
        html.setMetricasGlobais(metricas.getMetricasGlobais());
        this.jTextAreaTarefa.setText(getResultadosTarefas(metricas));
        html.setMetricasTarefas(metricas);
        CS_VMM mestre = (CS_VMM) rdf.getMestres().get(0);
        setResultadosUsuario(mestre.getEscalonador().getMetricaUsuarios(), metricas);

        if (rdf.getVMs().size() < 21) {
            graficoProcessamentoTempo = new ChartPanel(criarGraficoProcessamentoTempo(rdf));
            graficoProcessamentoTempo.setPreferredSize(new Dimension(600, 300));
        } else {
            this.jButtonProcessamentoMaquina.setVisible(false);
            for (CS_Processamento maq : rdf.getVMs()) {
                poderComputacionalTotal += (maq.getPoderComputacional() - (maq.getOcupacao() * maq.getPoderComputacional()));
            }
        }
        if (tarefas.size() < 50) {
            graficoProcessamentoTempoTarefa = new ChartPanel(criarGraficoProcessamentoTempoTarefa(tarefas));
            graficoProcessamentoTempoTarefa.setPreferredSize(new Dimension(600, 300));
        } else {
            this.jButtonProcessamentoTarefa.setVisible(false);
        }
        JFreeChart temp[] = gerarGraficoProcessamentoTempoUser(tarefas, rdf);
        graficoProcessamentoTempoUser1 = new ChartPanel(temp[0]);
        graficoProcessamentoTempoUser1.setPreferredSize(new Dimension(600, 300));
        graficoProcessamentoTempoUser2 = new ChartPanel(temp[1]);
        graficoProcessamentoTempoUser2.setPreferredSize(new Dimension(600, 300));

        //graficoEstadoTarefa = new ChartPanel(criarGraficoEstadoTarefa(tarefas));
        //graficoEstadoTarefa.setPreferredSize(new Dimension(600, 300));
        //graficoEstadoTarefa2 = new ChartPanel(criarGraficoEstadoTarefa2(tarefas, rdf));
        //graficoEstadoTarefa2.setPreferredSize(new Dimension(600, 300));
        this.jScrollPaneAlocacao.setViewportView(this.graficoBarraAlocacao);
        this.jScrollPaneCustos.setViewportView(this.graficoBarraCustoTotal);
        this.jScrollPaneProcessamento.setViewportView(this.graficoBarraProcessamento);
        this.jScrollPaneComunicacao.setViewportView(this.graficoBarraComunicacao);
        this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempo);

        //this.jScrollPaneProcessamento.setViewportView(this.graficoEstadoTarefa);
        //this.jScrollPaneComunicacao.setViewportView(this.graficoEstadoTarefa2);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPaneGrid = new javax.swing.JTabbedPane();
        jPanelGlobal = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jButtonSalvar = new javax.swing.JButton();
        jButtonSalvarTraces = new javax.swing.JButton();
        jScrollPaneGobal = new javax.swing.JScrollPane();
        jTextAreaGlobal = new javax.swing.JTextArea();
        jScrollPaneTarefa = new javax.swing.JScrollPane();
        jTextAreaTarefa = new javax.swing.JTextArea();
        jScrollPaneUsuario = new javax.swing.JScrollPane();
        jTextAreaUsuario = new javax.swing.JTextArea();
        jScrollPaneRecurso = new javax.swing.JScrollPane();
        Object[] colunas = {"Label", "Owner", "Processing performed", "Communication performed"};
        jTableRecurso = new javax.swing.JTable();
        jPanelProcessamento = new javax.swing.JPanel();
        jToolBarProcessamento = new javax.swing.JToolBar();
        jButtonPBarra = new javax.swing.JButton();
        jButtonPPizza = new javax.swing.JButton();
        jScrollPaneProcessamento = new javax.swing.JScrollPane();
        jPanelComunicacao = new javax.swing.JPanel();
        jToolBarComunicacao = new javax.swing.JToolBar();
        jButtonCBarra = new javax.swing.JButton();
        jButtonCPizza = new javax.swing.JButton();
        jScrollPaneComunicacao = new javax.swing.JScrollPane();
        jPanelProcessamentoTempo = new javax.swing.JPanel();
        jToolBarProcessamentoTempo = new javax.swing.JToolBar();
        jButtonProcessamentoUser = new javax.swing.JButton();
        jButtonProcessamentoMaquina = new javax.swing.JButton();
        jButtonProcessamentoTarefa = new javax.swing.JButton();
        jScrollPaneProcessamentoTempo = new javax.swing.JScrollPane();
        jPanelAlocacao = new javax.swing.JPanel();
        jToolBarAlocacao = new javax.swing.JToolBar();
        jButtonABarra = new javax.swing.JButton();
        jButtonAPizza = new javax.swing.JButton();
        jScrollPaneAlocacao = new javax.swing.JScrollPane();
        jPanelCusto = new javax.swing.JPanel();
        jToolBarCusto = new javax.swing.JToolBar();
        jButtonTotal = new javax.swing.JButton();
        jButtonDisc = new javax.swing.JButton();
        jButtonMem = new javax.swing.JButton();
        jButtonProc = new javax.swing.JButton();
        jScrollPaneCustos = new javax.swing.JScrollPane();
        jTabbedPane2 = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Simulation Results");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("imagens/Logo_iSPD_25.png")));

        jToolBar1.setRollover(true);

        jButtonSalvar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ispd/gui/imagens/document-save_1.png"))); // NOI18N
        jButtonSalvar.setToolTipText("Save results as HTML");
        jButtonSalvar.setFocusable(false);
        jButtonSalvar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSalvar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSalvarActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonSalvar);

        jButtonSalvarTraces.setText("Save traces");
        jButtonSalvarTraces.setToolTipText("Save a trace file of simulaton");
        jButtonSalvarTraces.setFocusable(false);
        jButtonSalvarTraces.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSalvarTraces.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonSalvarTraces.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSalvarTracesActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonSalvarTraces);

        jTextAreaGlobal.setEditable(false);
        jTextAreaGlobal.setColumns(20);
        jTextAreaGlobal.setFont(new java.awt.Font("Courier New", 1, 14)); // NOI18N
        jTextAreaGlobal.setRows(5);
        jScrollPaneGobal.setViewportView(jTextAreaGlobal);

        javax.swing.GroupLayout jPanelGlobalLayout = new javax.swing.GroupLayout(jPanelGlobal);
        jPanelGlobal.setLayout(jPanelGlobalLayout);
        jPanelGlobalLayout.setHorizontalGroup(
            jPanelGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 651, Short.MAX_VALUE)
            .addGroup(jPanelGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelGlobalLayout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPaneGobal, javax.swing.GroupLayout.PREFERRED_SIZE, 627, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanelGlobalLayout.setVerticalGroup(
            jPanelGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelGlobalLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 316, Short.MAX_VALUE))
            .addGroup(jPanelGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelGlobalLayout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPaneGobal, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jTabbedPaneGrid.addTab("Global", jPanelGlobal);

        jTextAreaTarefa.setColumns(20);
        jTextAreaTarefa.setEditable(false);
        jTextAreaTarefa.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
        jTextAreaTarefa.setRows(5);
        jScrollPaneTarefa.setViewportView(jTextAreaTarefa);

        jTabbedPaneGrid.addTab("Tasks", jScrollPaneTarefa);

        jTextAreaUsuario.setColumns(20);
        jTextAreaUsuario.setEditable(false);
        jTextAreaUsuario.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
        jTextAreaUsuario.setRows(5);
        jScrollPaneUsuario.setViewportView(jTextAreaUsuario);

        jTabbedPaneGrid.addTab("User", jScrollPaneUsuario);

        jTableRecurso.setModel(new javax.swing.table.DefaultTableModel(tabelaRecurso,colunas));
        jScrollPaneRecurso.setViewportView(jTableRecurso);

        jTabbedPaneGrid.addTab("Resources", jScrollPaneRecurso);

        jToolBarProcessamento.setRollover(true);

        jButtonPBarra.setText("Bar Chart");
        jButtonPBarra.setFocusable(false);
        jButtonPBarra.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonPBarra.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonPBarra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPBarraActionPerformed(evt);
            }
        });
        jToolBarProcessamento.add(jButtonPBarra);

        jButtonPPizza.setText("Pie chart");
        jButtonPPizza.setFocusable(false);
        jButtonPPizza.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonPPizza.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonPPizza.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPPizzaActionPerformed(evt);
            }
        });
        jToolBarProcessamento.add(jButtonPPizza);

        javax.swing.GroupLayout jPanelProcessamentoLayout = new javax.swing.GroupLayout(jPanelProcessamento);
        jPanelProcessamento.setLayout(jPanelProcessamentoLayout);
        jPanelProcessamentoLayout.setHorizontalGroup(
            jPanelProcessamentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBarProcessamento, javax.swing.GroupLayout.DEFAULT_SIZE, 651, Short.MAX_VALUE)
            .addComponent(jScrollPaneProcessamento, javax.swing.GroupLayout.DEFAULT_SIZE, 651, Short.MAX_VALUE)
        );
        jPanelProcessamentoLayout.setVerticalGroup(
            jPanelProcessamentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelProcessamentoLayout.createSequentialGroup()
                .addComponent(jToolBarProcessamento, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneProcessamento, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE))
        );

        jTabbedPaneGrid.addTab("Chart of the processing", jPanelProcessamento);

        jToolBarComunicacao.setRollover(true);

        jButtonCBarra.setText("Bar Chart");
        jButtonCBarra.setFocusable(false);
        jButtonCBarra.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonCBarra.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonCBarra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCBarraActionPerformed(evt);
            }
        });
        jToolBarComunicacao.add(jButtonCBarra);

        jButtonCPizza.setText("Pie chart");
        jButtonCPizza.setFocusable(false);
        jButtonCPizza.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonCPizza.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonCPizza.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCPizzaActionPerformed(evt);
            }
        });
        jToolBarComunicacao.add(jButtonCPizza);

        javax.swing.GroupLayout jPanelComunicacaoLayout = new javax.swing.GroupLayout(jPanelComunicacao);
        jPanelComunicacao.setLayout(jPanelComunicacaoLayout);
        jPanelComunicacaoLayout.setHorizontalGroup(
            jPanelComunicacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBarComunicacao, javax.swing.GroupLayout.DEFAULT_SIZE, 651, Short.MAX_VALUE)
            .addComponent(jScrollPaneComunicacao, javax.swing.GroupLayout.DEFAULT_SIZE, 651, Short.MAX_VALUE)
        );
        jPanelComunicacaoLayout.setVerticalGroup(
            jPanelComunicacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelComunicacaoLayout.createSequentialGroup()
                .addComponent(jToolBarComunicacao, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneComunicacao, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE))
        );

        jTabbedPaneGrid.addTab("Chart of the communication", jPanelComunicacao);

        jToolBarProcessamentoTempo.setRollover(true);

        jButtonProcessamentoUser.setText("Per user");
        jButtonProcessamentoUser.setFocusable(false);
        jButtonProcessamentoUser.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonProcessamentoUser.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonProcessamentoUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonProcessamentoUserActionPerformed(evt);
            }
        });
        jToolBarProcessamentoTempo.add(jButtonProcessamentoUser);

        jButtonProcessamentoMaquina.setText("Per machine");
        jButtonProcessamentoMaquina.setFocusable(false);
        jButtonProcessamentoMaquina.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonProcessamentoMaquina.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonProcessamentoMaquina.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonProcessamentoMaquinaActionPerformed(evt);
            }
        });
        jToolBarProcessamentoTempo.add(jButtonProcessamentoMaquina);

        jButtonProcessamentoTarefa.setText("Per task");
        jButtonProcessamentoTarefa.setFocusable(false);
        jButtonProcessamentoTarefa.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonProcessamentoTarefa.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonProcessamentoTarefa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonProcessamentoTarefaActionPerformed(evt);
            }
        });
        jToolBarProcessamentoTempo.add(jButtonProcessamentoTarefa);

        javax.swing.GroupLayout jPanelProcessamentoTempoLayout = new javax.swing.GroupLayout(jPanelProcessamentoTempo);
        jPanelProcessamentoTempo.setLayout(jPanelProcessamentoTempoLayout);
        jPanelProcessamentoTempoLayout.setHorizontalGroup(
            jPanelProcessamentoTempoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBarProcessamentoTempo, javax.swing.GroupLayout.DEFAULT_SIZE, 651, Short.MAX_VALUE)
            .addComponent(jScrollPaneProcessamentoTempo, javax.swing.GroupLayout.DEFAULT_SIZE, 651, Short.MAX_VALUE)
        );
        jPanelProcessamentoTempoLayout.setVerticalGroup(
            jPanelProcessamentoTempoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelProcessamentoTempoLayout.createSequentialGroup()
                .addComponent(jToolBarProcessamentoTempo, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneProcessamentoTempo, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE))
        );

        jTabbedPaneGrid.addTab("Use of computing power through time", jPanelProcessamentoTempo);

        jToolBarAlocacao.setRollover(true);

        jButtonABarra.setText("Bar Chart");
        jButtonABarra.setFocusable(false);
        jButtonABarra.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonABarra.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonABarra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonABarraActionPerformed(evt);
            }
        });
        jToolBarAlocacao.add(jButtonABarra);

        jButtonAPizza.setText("Pie Chat");
        jButtonAPizza.setFocusable(false);
        jButtonAPizza.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonAPizza.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonAPizza.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAPizzaActionPerformed(evt);
            }
        });
        jToolBarAlocacao.add(jButtonAPizza);

        javax.swing.GroupLayout jPanelAlocacaoLayout = new javax.swing.GroupLayout(jPanelAlocacao);
        jPanelAlocacao.setLayout(jPanelAlocacaoLayout);
        jPanelAlocacaoLayout.setHorizontalGroup(
            jPanelAlocacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBarAlocacao, javax.swing.GroupLayout.DEFAULT_SIZE, 651, Short.MAX_VALUE)
            .addComponent(jScrollPaneAlocacao)
        );
        jPanelAlocacaoLayout.setVerticalGroup(
            jPanelAlocacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAlocacaoLayout.createSequentialGroup()
                .addComponent(jToolBarAlocacao, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneAlocacao, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE))
        );

        jTabbedPaneGrid.addTab("Chart of virtual machine allocation", jPanelAlocacao);

        jToolBarCusto.setRollover(true);

        jButtonTotal.setText("Total");
        jButtonTotal.setFocusable(false);
        jButtonTotal.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonTotal.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonTotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTotalActionPerformed(evt);
            }
        });
        jToolBarCusto.add(jButtonTotal);

        jButtonDisc.setText("Per disk");
        jButtonDisc.setFocusable(false);
        jButtonDisc.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonDisc.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonDisc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDiscActionPerformed(evt);
            }
        });
        jToolBarCusto.add(jButtonDisc);

        jButtonMem.setText("Per Memory");
        jButtonMem.setFocusable(false);
        jButtonMem.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonMem.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonMem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMemActionPerformed(evt);
            }
        });
        jToolBarCusto.add(jButtonMem);

        jButtonProc.setText("Per processing");
        jButtonProc.setFocusable(false);
        jButtonProc.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonProc.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonProc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonProcActionPerformed(evt);
            }
        });
        jToolBarCusto.add(jButtonProc);

        javax.swing.GroupLayout jPanelCustoLayout = new javax.swing.GroupLayout(jPanelCusto);
        jPanelCusto.setLayout(jPanelCustoLayout);
        jPanelCustoLayout.setHorizontalGroup(
            jPanelCustoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBarCusto, javax.swing.GroupLayout.DEFAULT_SIZE, 651, Short.MAX_VALUE)
            .addComponent(jScrollPaneCustos)
        );
        jPanelCustoLayout.setVerticalGroup(
            jPanelCustoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCustoLayout.createSequentialGroup()
                .addComponent(jToolBarCusto, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneCustos, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE))
        );

        jTabbedPaneGrid.addTab("Chart of the cost of utilization", jPanelCusto);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPaneGrid)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPaneGrid, javax.swing.GroupLayout.PREFERRED_SIZE, 386, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCPizzaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCPizzaActionPerformed
        // TODO add your handling code here:
        this.jScrollPaneComunicacao.setViewportView(this.graficoPizzaComunicacao);
    }//GEN-LAST:event_jButtonCPizzaActionPerformed

    private void jButtonCBarraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCBarraActionPerformed
        // TODO add your handling code here:
        this.jScrollPaneComunicacao.setViewportView(this.graficoBarraComunicacao);
    }//GEN-LAST:event_jButtonCBarraActionPerformed

    private void jButtonPPizzaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPPizzaActionPerformed
        // TODO add your handling code here:
        this.jScrollPaneProcessamento.setViewportView(this.graficoPizzaProcessamento);
    }//GEN-LAST:event_jButtonPPizzaActionPerformed

    private void jButtonPBarraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPBarraActionPerformed
        // TODO add your handling code here:
        this.jScrollPaneProcessamento.setViewportView(this.graficoBarraProcessamento);
    }//GEN-LAST:event_jButtonPBarraActionPerformed

    private void jButtonProcessamentoMaquinaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonProcessamentoMaquinaActionPerformed
        this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempo);
    }//GEN-LAST:event_jButtonProcessamentoMaquinaActionPerformed

    private void jButtonProcessamentoTarefaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonProcessamentoTarefaActionPerformed
        this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempoTarefa);
    }//GEN-LAST:event_jButtonProcessamentoTarefaActionPerformed

    private void jButtonProcessamentoUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonProcessamentoUserActionPerformed
        if (this.jScrollPaneProcessamentoTempo.getViewport().getView() != this.graficoProcessamentoTempoUser1) {
            this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempoUser1);
        } else {
            this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempoUser2);
        }
    }//GEN-LAST:event_jButtonProcessamentoUserActionPerformed

    private void jButtonSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSalvarActionPerformed
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = jFileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser.getSelectedFile();
            salvarHTML(file);
            try {
                HtmlPane.openDefaultBrowser(new URL("file://" + file.getAbsolutePath() + "/result.html"));
            } catch (MalformedURLException ex) {
                Logger.getLogger(JResultadosCloud.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButtonSalvarActionPerformed

    private void jButtonSalvarTracesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSalvarTracesActionPerformed
        FiltroDeArquivos filtro = new FiltroDeArquivos("Workload Model of Simulation", ".wmsx", true);
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileFilter(filtro);
        int returnVal = jFileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser.getSelectedFile();
            if (!file.getName().endsWith(".wmsx")) {
                File temp = new File(file.toString() + ".wmsx");
                file = temp;
            }
            Interpretador interpret = new Interpretador(file.getAbsolutePath());
            interpret.geraTraceSim(tarefas);
        }
    }//GEN-LAST:event_jButtonSalvarTracesActionPerformed

    private void jButtonABarraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonABarraActionPerformed
        // TODO add your
        this.jScrollPaneAlocacao.setViewportView(this.graficoBarraAlocacao);
    }//GEN-LAST:event_jButtonABarraActionPerformed

    private void jButtonAPizzaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAPizzaActionPerformed
        // TODO add your handling code here:is
        this.jScrollPaneAlocacao.setViewportView(this.graficoPizzaAlocacao);
    }//GEN-LAST:event_jButtonAPizzaActionPerformed

    private void jButtonTotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTotalActionPerformed
        // TODO add your handling code here:
        this.jScrollPaneCustos.setViewportView(this.graficoBarraCustoTotal);
    }//GEN-LAST:event_jButtonTotalActionPerformed

    private void jButtonMemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMemActionPerformed
        // TODO add your handling code here:
        this.jScrollPaneCustos.setViewportView(this.graficoBarraCustoMem);
    }//GEN-LAST:event_jButtonMemActionPerformed

    private void jButtonDiscActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDiscActionPerformed
        // TODO add your handling code here:
        this.jScrollPaneCustos.setViewportView(this.graficoBarraCustoDisco);
    }//GEN-LAST:event_jButtonDiscActionPerformed

    private void jButtonProcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonProcActionPerformed
        // TODO add your handling code here:
        this.jScrollPaneCustos.setViewportView(this.graficoBarraCustoProc);
    }//GEN-LAST:event_jButtonProcActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonABarra;
    private javax.swing.JButton jButtonAPizza;
    private javax.swing.JButton jButtonCBarra;
    private javax.swing.JButton jButtonCPizza;
    private javax.swing.JButton jButtonDisc;
    private javax.swing.JButton jButtonMem;
    private javax.swing.JButton jButtonPBarra;
    private javax.swing.JButton jButtonPPizza;
    private javax.swing.JButton jButtonProc;
    private javax.swing.JButton jButtonProcessamentoMaquina;
    private javax.swing.JButton jButtonProcessamentoTarefa;
    private javax.swing.JButton jButtonProcessamentoUser;
    private javax.swing.JButton jButtonSalvar;
    private javax.swing.JButton jButtonSalvarTraces;
    private javax.swing.JButton jButtonTotal;
    private javax.swing.JPanel jPanelAlocacao;
    private javax.swing.JPanel jPanelComunicacao;
    private javax.swing.JPanel jPanelCusto;
    private javax.swing.JPanel jPanelGlobal;
    private javax.swing.JPanel jPanelProcessamento;
    private javax.swing.JPanel jPanelProcessamentoTempo;
    private javax.swing.JScrollPane jScrollPaneAlocacao;
    private javax.swing.JScrollPane jScrollPaneComunicacao;
    private javax.swing.JScrollPane jScrollPaneCustos;
    private javax.swing.JScrollPane jScrollPaneGobal;
    private javax.swing.JScrollPane jScrollPaneProcessamento;
    private javax.swing.JScrollPane jScrollPaneProcessamentoTempo;
    private javax.swing.JScrollPane jScrollPaneRecurso;
    private javax.swing.JScrollPane jScrollPaneTarefa;
    private javax.swing.JScrollPane jScrollPaneUsuario;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPaneGrid;
    private javax.swing.JTable jTableRecurso;
    private javax.swing.JTextArea jTextAreaGlobal;
    private javax.swing.JTextArea jTextAreaTarefa;
    private javax.swing.JTextArea jTextAreaUsuario;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBarAlocacao;
    private javax.swing.JToolBar jToolBarComunicacao;
    private javax.swing.JToolBar jToolBarCusto;
    private javax.swing.JToolBar jToolBarProcessamento;
    private javax.swing.JToolBar jToolBarProcessamentoTempo;
    // End of variables declaration//GEN-END:variables
    private List<Tarefa> tarefas;
    private Object[][] tabelaRecurso;
    private ChartPanel graficoBarraProcessamento;
    private ChartPanel graficoBarraComunicacao;
    private ChartPanel graficoBarraAlocacao;
    private ChartPanel graficoPizzaAlocacao;
    private ChartPanel graficoPizzaProcessamento;
    private ChartPanel graficoPizzaComunicacao;
    private ChartPanel graficoProcessamentoTempo;
    private ChartPanel graficoProcessamentoTempoTarefa;
    private ChartPanel graficoProcessamentoTempoUser1;
    private ChartPanel graficoProcessamentoTempoUser2;
    private ChartPanel graficoEstadoTarefa;
    private ChartPanel graficoEstadoTarefa2;
    private ChartPanel graficoBarraCustoTotal;
    private ChartPanel graficoBarraCustoDisco;
    private ChartPanel graficoBarraCustoMem;
    private ChartPanel graficoBarraCustoProc;

    private double poderComputacionalTotal = 0;
    private SalvarResultadosHTML html = new SalvarResultadosHTML();

    private String getResultadosGlobais(MetricasGlobais globais) {
        String texto = "\t\tSimulation Results:\n\n";
        texto += String.format("\tTotal Simulated Time = %g \n", globais.getTempoSimulacao());
        texto += String.format("\tSatisfaction = %g %%\n", globais.getSatisfacaoMedia());
        texto += String.format("\tIdleness of processing resources = %g %%\n", globais.getOciosidadeComputacao());
        texto += String.format("\tIdleness of communication resources = %g %%\n", globais.getOciosidadeComunicacao());
        texto += String.format("\tEfficiency = %g %%\n", globais.getEficiencia());
        if (globais.getEficiencia() > 70.0) {
            texto += "\tEfficiency GOOD\n\n ";
        } else if (globais.getEficiencia() > 40.0) {
            texto += "\tEfficiency MEDIA\n\n ";
        } else {
            texto += "\tEfficiency BAD\n\n ";
        }
        texto += "\t\tCost Results:\n\n";
        texto += String.format("\tCost Total Processing = %g $\n", globais.getCustoTotalProc());
        texto += String.format("\tCost Total Memory = %g $\n", globais.getCustoTotalMem());
        texto += String.format("\tCost Total Disk = %g $\n\n", globais.getCustoTotalDisco());
        texto += "\t\tVM Alocation Results:\n\n";
        texto +=String.format("\tTotal of VMs alocated = %d \n", globais.getNumVMsAlocadas());
        texto +=String.format("\tTotal of VMs rejected = %d \n", globais.getNumVMsRejeitadas());
        
        return texto;
    }

    private String getResultadosTarefas(Metricas metrica) {
        String texto = "\n\n\t\tTASKS\n ";
        double tempoMedioSistemaComunicacao = metrica.getTempoMedioFilaComunicacao() + metrica.getTempoMedioComunicacao();
        double tempoMedioSistemaProcessamento = metrica.getTempoMedioFilaProcessamento() + metrica.getTempoMedioProcessamento();
        texto += "\n Communication \n";
        texto += String.format("    Queue average time: %g seconds.\n", metrica.getTempoMedioFilaComunicacao());
        texto += String.format("    Communication average time: %g seconds.\n", metrica.getTempoMedioComunicacao());
        texto += String.format("    System average time: %g seconds.\n", tempoMedioSistemaComunicacao);
        texto += "\n Processing \n";
        texto += String.format("    Queue average time: %g seconds.\n", metrica.getTempoMedioFilaProcessamento());
        texto += String.format("    Processing average time: %g seconds.\n", metrica.getTempoMedioProcessamento());
        texto += String.format("    System average time: %g seconds.\n", tempoMedioSistemaProcessamento);
        if (metrica.getNumTarefasCanceladas() > 0) {
            texto += "\n Tasks Canceled \n";
            texto += String.format("    Number: %d \n", metrica.getNumTarefasCanceladas());
            texto += String.format("    Wasted Processing: %g Mflops", metrica.getMflopsDesperdicio());
        }
        return texto;
    }

    private JFreeChart[] gerarGraficoProcessamentoTempoUser(List<Tarefa> tarefas, RedeDeFilasCloud rdf) {
        ArrayList<tempo_uso_usuario> lista = new ArrayList<tempo_uso_usuario>();
        int numberUsers = rdf.getUsuarios().size();
        Map<String, Integer> users = new HashMap<String, Integer>();
        XYSeries tmp_series[] = new XYSeries[numberUsers];
        XYSeries tmp_series1[] = new XYSeries[numberUsers];
        Double utilizacaoUser[] = new Double[numberUsers];
        Double utilizacaoUser1[] = new Double[numberUsers];
        XYSeriesCollection dadosGrafico = new XYSeriesCollection();
        XYSeriesCollection dadosGrafico1 = new XYSeriesCollection();
        for (int i = 0; i < numberUsers; i++) {
            users.put(rdf.getUsuarios().get(i), i);
            utilizacaoUser[i] = 0.0;
            tmp_series[i] = new XYSeries(rdf.getUsuarios().get(i));
            utilizacaoUser1[i] = 0.0;
            tmp_series1[i] = new XYSeries(rdf.getUsuarios().get(i));
        }
        if (!tarefas.isEmpty()) {
            //Insere cada tarefa como dois pontos na lista
            for (Tarefa task : tarefas) {
                CS_Processamento local = (CS_Processamento) task.getLocalProcessamento();
                if (local != null) {

                    for (int i = 0; i < task.getTempoInicial().size(); i++) {
                        Double uso = (task.getHistoricoProcessamento().get(i).getPoderComputacional() / poderComputacionalTotal) * 100;
                        tempo_uso_usuario provisorio1 = new tempo_uso_usuario(task.getTempoInicial().get(i), true, uso, users.get(task.getProprietario()));
                        lista.add(provisorio1);
                        tempo_uso_usuario provisorio2 = new tempo_uso_usuario(task.getTempoFinal().get(i), false, uso, users.get(task.getProprietario()));
                        lista.add(provisorio2);
                    }
                }
            }
            //Ordena lista
            Collections.sort(lista);
        }
        for (int i = 0; i < lista.size(); i++) {
            tempo_uso_usuario temp = (tempo_uso_usuario) lista.get(i);
            int usuario = temp.get_user();
            //Altera os valores do usuario atual e todos acima dele
            for (int j = usuario; j < numberUsers; j++) {
                //Salva valores anteriores
                tmp_series[j].add(temp.get_tempo(), utilizacaoUser[j]);
                if (temp.get_tipo()) {
                    utilizacaoUser[j] += temp.get_uso_no();
                } else {
                    utilizacaoUser[j] -= temp.get_uso_no();
                }
                //Novo valor
                tmp_series[j].add(temp.get_tempo(), utilizacaoUser[j]);
            }
            //Grafico1
            tmp_series1[usuario].add(temp.get_tempo(), utilizacaoUser1[usuario]);
            if (temp.get_tipo()) {
                utilizacaoUser1[usuario] += temp.get_uso_no();
            } else {
                utilizacaoUser1[usuario] -= temp.get_uso_no();
            }
            tmp_series1[usuario].add(temp.get_tempo(), utilizacaoUser1[usuario]);
        }
        for (int i = 0; i < numberUsers; i++) {
            dadosGrafico.addSeries(tmp_series[i]);
            dadosGrafico1.addSeries(tmp_series1[i]);
        }
        JFreeChart saida[] = new JFreeChart[2];
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
        XYPlot xyplot = (XYPlot) saida[1].getPlot();
        xyplot.setDomainPannable(true);
        XYStepAreaRenderer xysteparearenderer = new XYStepAreaRenderer(2);
        xysteparearenderer.setDataBoundsIncludesVisibleSeriesOnly(false);
        xysteparearenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        xysteparearenderer.setDefaultEntityRadius(6);
        xyplot.setRenderer(xysteparearenderer);

        return saida;
    }

    /**
     * Salva resultados obtidos em um arquivo html
     *
     * @param file diretório destino
     */
    public void salvarHTML(File file) {
        //Gerar resultados:
        html.setTabela(tabelaRecurso);
        BufferedImage[] charts = new BufferedImage[8];
        if (this.graficoBarraProcessamento != null) {
            charts[0] = this.graficoBarraProcessamento.getChart().createBufferedImage(1200, 600);
        }
        if (this.graficoPizzaProcessamento != null) {
            charts[1] = this.graficoPizzaProcessamento.getChart().createBufferedImage(1200, 600);
        }
        if (this.graficoBarraComunicacao != null) {
            charts[2] = this.graficoBarraComunicacao.getChart().createBufferedImage(1200, 600);
        }
        if (this.graficoPizzaComunicacao != null) {
            charts[3] = this.graficoPizzaComunicacao.getChart().createBufferedImage(1200, 600);
        }
        if (this.graficoProcessamentoTempo != null) {
            charts[4] = this.graficoProcessamentoTempo.getChart().createBufferedImage(1200, 600);
        }
        if (this.graficoProcessamentoTempoTarefa != null) {
            charts[5] = this.graficoProcessamentoTempoTarefa.getChart().createBufferedImage(1200, 600);
        }
        if (this.graficoProcessamentoTempoUser1 != null) {
            charts[6] = this.graficoProcessamentoTempoUser1.getChart().createBufferedImage(1200, 600);
        }
        if (this.graficoProcessamentoTempoUser2 != null) {
            charts[7] = this.graficoProcessamentoTempoUser2.getChart().createBufferedImage(1200, 600);
        }
        html.setCharts(charts);
        try {
            html.gerarHTML(file);
        } catch (IOException ex) {
            Logger.getLogger(JResultadosCloud.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private JFreeChart criarGraficoProcessamentoTempoTarefa(List<Tarefa> tarefas) {

        XYSeriesCollection dadosGrafico = new XYSeriesCollection();
        if (!tarefas.isEmpty()) {
            for (Tarefa task : tarefas) {
                XYSeries tmp_series;
                tmp_series = new XYSeries("task " + task.getIdentificador());
                CS_Processamento temp = (CS_Processamento) task.getLocalProcessamento();
                if (temp != null) {
                    Double uso = (temp.getPoderComputacional() / this.poderComputacionalTotal) * 100;
                    for (int j = 0; j < task.getTempoInicial().size(); j++) {
                        tmp_series.add(task.getTempoInicial().get(j), (Double) 0.0);
                        tmp_series.add(task.getTempoInicial().get(j), uso);
                        tmp_series.add(task.getTempoFinal().get(j), uso);
                        tmp_series.add(task.getTempoFinal().get(j), (Double) 0.0);
                    }
                    dadosGrafico.addSeries(tmp_series);
                }
            }

        }

        JFreeChart jfc = ChartFactory.createXYAreaChart(
                "Use of total computing power through time "
                + "\nTasks", //Titulo
                "Time (seconds)", // Eixo X
                "Rate of total use of computing power (%)", //Eixo Y
                dadosGrafico, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false); // exibir: legendas, tooltips, url
        return jfc;
    }

    private JFreeChart criarGraficoEstadoTarefa2(List<Tarefa> tarefas, RedeDeFilasCloud rdf) {
        DefaultCategoryDataset dados = new DefaultCategoryDataset();
        for (CS_Processamento maq : rdf.getVMs()) {
            dados.addValue(0, "Canceled", maq.getId());
            dados.addValue(0, "Completed", maq.getId());
            dados.addValue(0, "Not executed", maq.getId());
            dados.addValue(0, "Failures", maq.getId());
        }
        for (CS_Processamento maq : rdf.getMestres()) {
            if (maq instanceof CS_VMM) {
                dados.addValue(0, "Canceled", maq.getId());
                dados.addValue(0, "Completed", maq.getId());
                dados.addValue(0, "Not executed", maq.getId());
                dados.addValue(0, "Failures", maq.getId());
            }
        }
        for (Tarefa tarefa : tarefas) {
            Double val;
            switch (tarefa.getEstado()) {
                case Tarefa.PARADO:
                    val = (Double) dados.getValue("Not executed", tarefa.getOrigem().getId());
                    dados.setValue(val + 1, "Not executed", tarefa.getOrigem().getId());
                    break;
                case Tarefa.CONCLUIDO:
                    val = (Double) dados.getValue("Completed", tarefa.getLocalProcessamento().getId());
                    dados.setValue(val + 1, "Completed", tarefa.getLocalProcessamento().getId());
                    break;
                case Tarefa.CANCELADO:
                    val = (Double) dados.getValue("Canceled", tarefa.getLocalProcessamento().getId());
                    dados.setValue(val + 1, "Canceled", tarefa.getLocalProcessamento().getId());
                    break;
                case Tarefa.FALHA:
                    val = (Double) dados.getValue("Failures", tarefa.getLocalProcessamento().getId());
                    dados.setValue(val + 1, "Failures", tarefa.getLocalProcessamento().getId());
                    break;
            }
        }
        JFreeChart jfc = ChartFactory.createBarChart(
                "State of tasks per resource", //Titulo
                "Resource", // Eixo X
                "Numbers of tasks", //Eixo Y
                dados, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, false, false); // exibir: legendas, tooltips, url
        return jfc;
    }

    private JFreeChart criarGraficoEstadoTarefa(List<Tarefa> tarefas) {
        XYSeries canceladas = new XYSeries("Canceled");
        XYSeries concluidas = new XYSeries("Completed");
        XYSeries paradas = new XYSeries("Not executed");
        XYSeries falhas = new XYSeries("Failures");
        for (Tarefa tarefa : tarefas) {
            switch (tarefa.getEstado()) {
                case Tarefa.PARADO:
                    paradas.add(tarefa.getTimeCriacao(), 4);
                    break;
                case Tarefa.CONCLUIDO:
                    concluidas.add(
                            tarefa.getTempoFinal().get(tarefa.getTempoFinal().size() - 1), (Double) 1.0);
                    break;
                case Tarefa.CANCELADO:
                    if (!tarefa.getTempoFinal().isEmpty()) {
                        canceladas.add(tarefa.getTempoFinal().get(tarefa.getTempoFinal().size() - 1), (Double) 2.0);
                    }
                    break;
                case Tarefa.FALHA:
                    falhas.add(tarefa.getTempoFinal().get(tarefa.getTempoFinal().size() - 1), (Double) 3.0);
                    break;
            }
        }
        XYSeriesCollection data = new XYSeriesCollection();
        data.addSeries(falhas);
        data.addSeries(canceladas);
        data.addSeries(concluidas);
        data.addSeries(paradas);
        JFreeChart chart = ChartFactory.createScatterPlot(
                "State of tasks",
                "Time (seconds)",
                "Y", data,
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false);
        NumberAxis domainAxis = (NumberAxis) chart.getXYPlot().getDomainAxis();
        domainAxis.setAutoRangeIncludesZero(false);
        return chart;
    }

    //Cria o gráfico que demonstra o uso de cada recurso do sistema através do tempo.
    //Ele recebe como parâmetro a lista com as maquinas que processaram durante a simulação.
    private JFreeChart criarGraficoProcessamentoTempo(RedeDeFilasCloud rdf) {
        XYSeriesCollection dadosGrafico = new XYSeriesCollection();
        //Se tiver alguma máquina na lista.
        if (rdf.getVMs() != null) {
            //Laço foreach que percorre as máquinas.
            for (CS_Processamento maq : rdf.getVMs()) {
                //Lista que recebe os pares de intervalo de tempo em que a máquina executou.
                List<ParesOrdenadosUso> lista = maq.getListaProcessamento();
                poderComputacionalTotal += (maq.getPoderComputacional() - (maq.getOcupacao() * maq.getPoderComputacional()));
                //Se a máquina tiver intervalos.
                if (!lista.isEmpty()) {
                    //Cria o objeto do tipo XYSeries.
                    XYSeries tmp_series;
                    //Se o atributo numeroMaquina for 0, ou seja, não for um nó de um cluster.
                    if (maq.getnumeroMaquina() == 0) //Estancia com o nome puro.
                    {
                        tmp_series = new XYSeries(maq.getId());
                    } //Se for 1 ou mais, ou seja, é um nó de cluster.
                    else //Estancia tmp_series com o nome concatenado com a palavra node e seu numero.
                    {
                        tmp_series = new XYSeries(maq.getId() + " node " + maq.getnumeroMaquina());
                    }
                    int i;
                    //Laço que vai adicionando os pontos para a criação do gráfico.
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

        JFreeChart jfc = ChartFactory.createXYAreaChart(
                "Use of computing power through time "
                + "\nMachines", //Titulo
                "Time (seconds)", // Eixo X
                "Rate of use of computing power for each node (%)", //Eixo Y
                dadosGrafico, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                true, true, false); // exibir: legendas, tooltips, url
        return jfc;
    }

    private void setResultadosUsuario(MetricasUsuarios metricasUsuarios, Metricas metricas) {
        if (metricasUsuarios != null && metricasUsuarios.getUsuarios().size() > 1) {
            String texto = "";
            for (int i = 0; i < metricasUsuarios.getUsuarios().size(); i++) {
                String userName = metricasUsuarios.getUsuarios().get(i);
                texto += "\n\n\t\tUser " + userName + "\n";
                HashSet set = metricasUsuarios.getTarefasConcluidas(userName);
                texto += "\nNumber of task: " + set.size() + "\n";
                //Applications:
                //Name: Number of task: Mflops:
                double tempoMedioFilaComunicacao = 0;
                double tempoMedioComunicacao = 0;
                double tempoMedioSistemaComunicacao;
                double tempoMedioFilaProcessamento = 0;
                double tempoMedioProcessamento = 0;
                double tempoMedioSistemaProcessamento;
                int numTarefasCanceladas = 0;
                int numTarefas = 0;
                for (Tarefa no : metricasUsuarios.getTarefasConcluidas(userName)) {
                    tempoMedioFilaComunicacao += no.getMetricas().getTempoEsperaComu();
                    tempoMedioComunicacao += no.getMetricas().getTempoComunicacao();
                    tempoMedioFilaProcessamento = no.getMetricas().getTempoEsperaProc();
                    tempoMedioProcessamento = no.getMetricas().getTempoProcessamento();
                    numTarefas++;
                }
                tempoMedioFilaComunicacao = tempoMedioFilaComunicacao / numTarefas;
                tempoMedioComunicacao = tempoMedioComunicacao / numTarefas;
                tempoMedioFilaProcessamento = tempoMedioFilaProcessamento / numTarefas;
                tempoMedioProcessamento = tempoMedioProcessamento / numTarefas;
                tempoMedioSistemaComunicacao = tempoMedioFilaComunicacao + tempoMedioComunicacao;
                tempoMedioSistemaProcessamento = tempoMedioFilaProcessamento + tempoMedioProcessamento;
                texto += "\n Communication \n";
                texto += String.format("    Queue average time: %g seconds.\n", tempoMedioFilaComunicacao);
                texto += String.format("    Communication average time: %g seconds.\n", tempoMedioComunicacao);
                texto += String.format("    System average time: %g seconds.\n", tempoMedioSistemaComunicacao);
                texto += "\n Processing \n";
                texto += String.format("    Queue average time: %g seconds.\n", tempoMedioFilaProcessamento);
                texto += String.format("    Processing average time: %g seconds.\n", tempoMedioProcessamento);
                texto += String.format("    System average time: %g seconds.\n", tempoMedioSistemaProcessamento);
            }
            String name;
            texto += String.format("\nSatisfação dos usuários em porcentagem\n");
            for (Map.Entry<String, Double> entry : metricas.getMetricasSatisfacao().entrySet()) {

                String user = entry.getKey();
                Double satisfacao = entry.getValue();
                texto += user + " : " + satisfacao + " %\n";

            }
            jTextAreaUsuario.setText(texto);
        } else {
            jTabbedPaneGrid.remove(jScrollPaneUsuario);
        }
    }

    private void gerarGraficosComunicacao(Map<String, MetricasComunicacao> mComunicacao) {
        DefaultCategoryDataset dadosGraficoComunicacao = new DefaultCategoryDataset();
        DefaultPieDataset dadosGraficoPizzaComunicacao = new DefaultPieDataset();

        if (mComunicacao != null) {
            for (Map.Entry<String, MetricasComunicacao> entry : mComunicacao.entrySet()) {
                MetricasComunicacao link = entry.getValue();
                dadosGraficoComunicacao.addValue(link.getMbitsTransmitidos(), "vermelho", link.getId());
                dadosGraficoPizzaComunicacao.insertValue(0, link.getId(), link.getMbitsTransmitidos());
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
        graficoBarraComunicacao = new ChartPanel(jfc);
        graficoBarraComunicacao.setPreferredSize(new Dimension(600, 300));

        jfc = ChartFactory.createPieChart(
                "Total communication in each resource", //Titulo
                dadosGraficoPizzaComunicacao, // Dados para o grafico
                true, false, false);
        graficoPizzaComunicacao = new ChartPanel(jfc);
        graficoPizzaComunicacao.setPreferredSize(new Dimension(600, 300));
    }

    private void gerarGraficosProcessamento(Map<String, MetricasProcessamento> mProcess) {
        DefaultCategoryDataset dadosGraficoProcessamento = new DefaultCategoryDataset();
        DefaultPieDataset dadosGraficoPizzaProcessamento = new DefaultPieDataset();

        if (mProcess != null) {
            for (Map.Entry<String, MetricasProcessamento> entry : mProcess.entrySet()) {
                MetricasProcessamento mt = entry.getValue();
                if (mt.getnumeroMaquina() == 0) {
                    dadosGraficoProcessamento.addValue(mt.getMFlopsProcessados(), "vermelho", mt.getId());
                    dadosGraficoPizzaProcessamento.insertValue(0, mt.getId(), mt.getMFlopsProcessados());
                } else {
                    dadosGraficoProcessamento.addValue(mt.getMFlopsProcessados(), "vermelho", mt.getId() + " node " + mt.getnumeroMaquina());
                    dadosGraficoPizzaProcessamento.insertValue(0, mt.getId() + " node " + mt.getnumeroMaquina(), mt.getMFlopsProcessados());
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
        graficoBarraProcessamento = new ChartPanel(jfc);
        graficoBarraProcessamento.setPreferredSize(new Dimension(600, 300));

        jfc = ChartFactory.createPieChart(
                "Total processed on each resource", //Titulo
                dadosGraficoPizzaProcessamento, // Dados para o grafico
                true, false, false);
        graficoPizzaProcessamento = new ChartPanel(jfc);
        graficoPizzaProcessamento.setPreferredSize(new Dimension(600, 300));

    }

    private Object[][] setTabelaRecurso(Metricas metricas) {
        List<Object[]> tabela = new ArrayList<Object[]>();
        //linha [Nome] [Proprietario] [Processamento] [comunicacao]
        String nome;
        String prop;
        Double proc;
        Double comu;
        if (metricas.getMetricasProcessamento() != null) {
            for (Map.Entry<String, MetricasProcessamento> entry : metricas.getMetricasProcessamento().entrySet()) {
                MetricasProcessamento maq = entry.getValue();
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
            for (Map.Entry<String, MetricasComunicacao> entry : metricas.getMetricasComunicacao().entrySet()) {
                MetricasComunicacao link = entry.getValue();
                nome = link.getId();
                prop = "---";
                proc = 0.0;
                comu = link.getSegundosDeTransmissao();
                tabela.add(Arrays.asList(nome, prop, proc, comu).toArray());
            }
        }
        Object[][] temp = new Object[tabela.size()][4];
        for (int i = 0; i < tabela.size(); i++) {
            temp[i] = tabela.get(i);
        }
        return temp;
    }

    private void gerarGraficosAlocacao(Map<String, MetricasAlocacao> mAloc) {
        DefaultCategoryDataset dadosGraficoAloc = new DefaultCategoryDataset();
        DefaultPieDataset dadosGraficoPizzaAloc = new DefaultPieDataset();

        if (mAloc != null) {
            for (Map.Entry<String, MetricasAlocacao> entry : mAloc.entrySet()) {
                MetricasAlocacao mt = entry.getValue();
                dadosGraficoAloc.addValue(mt.getNumVMs(), "vermelho", mt.getId());
                dadosGraficoPizzaAloc.insertValue(0, mt.getId(), mt.getNumVMs());
            }
        }

        JFreeChart jfc = ChartFactory.createBarChart(
                "Total of virtual machines allocated in each resource", //Titulo
                "Resource", // Eixo X
                "Number of VMs", //Eixo Y
                dadosGraficoAloc, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                false, false, false); // exibir: legendas, tooltips, url
        //Inclina nome da barra em 45 graus
        if (mAloc.size() > 10) {
            jfc.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }
        graficoBarraAlocacao = new ChartPanel(jfc);
        graficoBarraAlocacao.setPreferredSize(new Dimension(600, 300));

        jfc = ChartFactory.createPieChart(
                "Total of virtual machines allocated in each resource", //Titulo
                dadosGraficoPizzaAloc, // Dados para o grafico
                true, false, false);
        graficoPizzaAlocacao = new ChartPanel(jfc);
        graficoPizzaAlocacao.setPreferredSize(new Dimension(600, 300));

    }

    private void gerarGraficosCusto(Map<String, MetricasCusto> mCusto) {
        DefaultCategoryDataset dadosGraficoCustoTotal = new DefaultCategoryDataset();
        DefaultCategoryDataset dadosGraficoCustoDisco = new DefaultCategoryDataset();
        DefaultCategoryDataset dadosGraficoCustoMem = new DefaultCategoryDataset();
        DefaultCategoryDataset dadosGraficoCustoProc = new DefaultCategoryDataset();

        if (mCusto != null) {
            for (Map.Entry<String, MetricasCusto> entry : mCusto.entrySet()) {
                MetricasCusto mt = entry.getValue();
                dadosGraficoCustoTotal.addValue(mt.getCustoDisco()+mt.getCustoMem()+mt.getCustoProc(), "vermelho", mt.getId());
                dadosGraficoCustoDisco.addValue(mt.getCustoDisco(), "vermelho", mt.getId());
                dadosGraficoCustoMem.addValue(mt.getCustoMem(), "vermelho", mt.getId());
                dadosGraficoCustoProc.addValue(mt.getCustoProc(), "vermelho", mt.getId());
            }
        }

        JFreeChart jfct = ChartFactory.createBarChart(
                "Total cost of utilization per virtual machine", //Titulo
                "Virtual machines", // Eixo X
                "Cost ($)", //Eixo Y
                dadosGraficoCustoTotal, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                false, false, false); // exibir: legendas, tooltips, url
        //Inclina nome da barra em 45 graus
        if (mCusto.size() > 10) {
            jfct.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }
        graficoBarraCustoTotal = new ChartPanel(jfct);
        graficoBarraAlocacao.setPreferredSize(new Dimension(600, 300));

        JFreeChart jfcd = ChartFactory.createBarChart(
                "Cost of disk utilization per virtual machine", //Titulo
                "Virtual machines", // Eixo X
                "Cost ($)", //Eixo Y
                dadosGraficoCustoDisco, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                false, false, false); // exibir: legendas, tooltips, url
        //Inclina nome da barra em 45 graus
        if (mCusto.size() > 10) {
            jfcd.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }
        graficoBarraCustoDisco = new ChartPanel(jfcd);
        graficoBarraCustoDisco.setPreferredSize(new Dimension(600, 300));
        
        JFreeChart jfcm = ChartFactory.createBarChart(
                "Cost of memory utilization per virtual machine", //Titulo
                "Virtual machines", // Eixo X
                "Cost ($)", //Eixo Y
                dadosGraficoCustoMem, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                false, false, false); // exibir: legendas, tooltips, url
        //Inclina nome da barra em 45 graus
        if (mCusto.size() > 10) {
            jfcm.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }
        graficoBarraCustoMem = new ChartPanel(jfcm);
        graficoBarraCustoMem.setPreferredSize(new Dimension(600, 300));
        
        JFreeChart jfcp = ChartFactory.createBarChart(
                "Cost of processing utilization per virtual machine", //Titulo
                "Virtual machines", // Eixo X
                "Cost ($)", //Eixo Y
                dadosGraficoCustoProc, // Dados para o grafico
                PlotOrientation.VERTICAL, //Orientacao do grafico
                false, false, false); // exibir: legendas, tooltips, url
        //Inclina nome da barra em 45 graus
        if (mCusto.size() > 10) {
            jfcp.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        }
        graficoBarraCustoProc = new ChartPanel(jfcp);
        graficoBarraCustoProc.setPreferredSize(new Dimension(600, 300));
        
    }

    protected class tempo_uso_usuario implements Comparable<tempo_uso_usuario> {

        private Double tempo;
        private Double uso_no;
        private Boolean tipo;
        private Integer user;

        private tempo_uso_usuario(double tempo, boolean tipo, Double uso, Integer user) {
            this.user = user;
            this.tempo = tempo;
            this.uso_no = uso;
            this.tipo = tipo;
        }

        public Double get_tempo() {
            return this.tempo;
        }

        public Integer get_user() {
            return this.user;
        }

        public Boolean get_tipo() {
            return this.tipo;
        }

        public Double get_uso_no() {
            return this.uso_no;
        }

        @Override
        public int compareTo(tempo_uso_usuario o) {
            return tempo.compareTo(o.tempo);
        }
    }
}
