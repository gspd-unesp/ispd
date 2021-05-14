/* ==========================================================
 * iSPD : iconic Simulator of Parallel and Distributed System
 * ==========================================================
 *
 * (C) Copyright 2010-2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Project Info:  http://gspd.dcce.ibilce.unesp.br/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------
 * JResultados.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * Created on 20/09/2011
 * 09-Set-2014 : Version 2.0;
 *
 */
package ispd.gui;

import ispd.arquivo.SalvarResultadosHTML;
import ispd.arquivo.xml.ConfiguracaoISPD;
import ispd.arquivo.xml.TraceXML;
import ispd.gui.auxiliar.FiltroDeArquivos;
import ispd.gui.auxiliar.Graficos;
import ispd.gui.auxiliar.HtmlPane;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.gui.auxiliar.ParesOrdenadosUso;
import ispd.arquivo.SalvarResultadosHTML;
import ispd.arquivo.interpretador.cargas.Interpretador;
import ispd.gui.auxiliar.FiltroDeArquivos;
import ispd.gui.auxiliar.HtmlPane;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.metricas.Metricas;
import ispd.motor.metricas.MetricasComunicacao;
import ispd.motor.metricas.MetricasGlobais;
import ispd.motor.metricas.MetricasProcessamento;
import ispd.motor.metricas.MetricasUsuarios;
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
import javax.swing.JOptionPane;
import org.jfree.chart.ChartPanel;
/**
 *
 * @author denison
 */
public class JResultados extends javax.swing.JDialog {

    /**
     * Cria no JResultado sem carregar parte gráfica para utilizar no modo
     * terminal
     */
    public JResultados(Metricas metricas, RedeDeFilas rdf, List tarefas) {
        html.setMetricasGlobais(metricas.getMetricasGlobais());
        tabelaRecurso = setTabelaRecurso(metricas);
        getResultadosTarefas(metricas);
        html.setMetricasTarefas(metricas);
        gerarGraficosProcessamento(metricas.getMetricasProcessamento());
        gerarGraficosComunicacao(metricas.getMetricasComunicacao());
        if (rdf.getMaquinas().size() < 21) {
            graficoProcessamentoTempo = new ChartPanel(criarGraficoProcessamentoTempo(rdf));
        } else {
            for (CS_Processamento maq : rdf.getMaquinas()) {
                poderComputacionalTotal += (maq.getPoderComputacional() - (maq.getOcupacao() * maq.getPoderComputacional()));
            }
        }
        if (tarefas.size() < 50) {
            graficoProcessamentoTempoTarefa = new ChartPanel(criarGraficoProcessamentoTempoTarefa(tarefas));
        }
        JFreeChart temp[] = gerarGraficoProcessamentoTempoUser(tarefas, rdf);
        graficoProcessamentoTempoUser1 = new ChartPanel(temp[0]);
        graficoProcessamentoTempoUser2 = new ChartPanel(temp[1]);
        
        charts = new Graficos();
        charts.criarProcessamento(metricas.getMetricasProcessamento());
        charts.criarComunicacao(metricas.getMetricasComunicacao());
        charts.criarProcessamentoTempoMaquina(rdf);
        charts.criarProcessamentoTempoTarefa(tarefas);
        charts.criarProcessamentoTempoUser(tarefas, rdf);
        charts.rede = rdf;
    }

    /**
     * Cria no JResultado sem carregar parte gráfica para utilizar no modo
     * terminal usando apenas a classe "Metricas"
     */
    public JResultados(Metricas metricas) {
        html.setMetricasGlobais(metricas.getMetricasGlobais());
        tabelaRecurso = setTabelaRecurso(metricas);
        getResultadosTarefas(metricas);
        html.setMetricasTarefas(metricas);
        gerarGraficosProcessamento(metricas.getMetricasProcessamento());
        gerarGraficosComunicacao(metricas.getMetricasComunicacao());
        
        charts = new Graficos();
        charts.criarProcessamento(metricas.getMetricasProcessamento());
        charts.criarComunicacao(metricas.getMetricasComunicacao());
    }
    
    /**
     * Creates new form JResultados
     */
    public JResultados(java.awt.Frame parent, Metricas metricas, RedeDeFilas rdf, List<Tarefa> tarefas) {
        super(parent, true);
        this.tarefas = tarefas;
        gerarGraficosProcessamento(metricas.getMetricasProcessamento());
        gerarGraficosComunicacao(metricas.getMetricasComunicacao());
        tabelaRecurso = setTabelaRecurso(metricas);
        initComponents();
        this.jTextAreaGlobal.setText(getResultadosGlobais(metricas.getMetricasGlobais()));
        html.setMetricasGlobais(metricas.getMetricasGlobais());
        this.jTextAreaTarefa.setText(getResultadosTarefas(metricas));
        html.setMetricasTarefas(metricas);
        CS_Mestre mestre = (CS_Mestre) rdf.getMestres().get(0);
        setResultadosUsuario(mestre.getEscalonador().getMetricaUsuarios(), metricas);

        if (rdf.getMaquinas().size() < 21) {
            graficoProcessamentoTempo = new ChartPanel(criarGraficoProcessamentoTempo(rdf));
            graficoProcessamentoTempo.setPreferredSize(new Dimension(600, 300));
        } else {
            this.jButtonProcessamentoMaquina.setVisible(false);
            for (CS_Processamento maq : rdf.getMaquinas()) {
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
        this.jScrollPaneProcessamento.setViewportView(this.graficoBarraProcessamento);
        this.jScrollPaneComunicacao.setViewportView(this.graficoBarraComunicacao);
        this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempo);

        //this.jScrollPaneProcessamento.setViewportView(this.graficoEstadoTarefa);
        //this.jScrollPaneComunicacao.setViewportView(this.graficoEstadoTarefa2);
    }
    
    /**
     * Creates new form JResultados
     */
    public JResultados(java.awt.Window parent, Metricas metricas, RedeDeFilas rdf, List<Tarefa> tarefas, ConfiguracaoISPD config) {
        super(parent, ModalityType.APPLICATION_MODAL);
        this.tarefas = tarefas;
        charts = new Graficos();
        if (config.getCreateProcessingChart()) {
            charts.criarProcessamento(metricas.getMetricasProcessamento());
        }
        if (config.getCreateCommunicationChart()) {
            charts.criarComunicacao(metricas.getMetricasComunicacao());
        }
        tabelaRecurso = setTabelaRecurso(metricas);
        initComponents();
        setButtons(config);
        this.jTextAreaGlobal.setText(getResultadosGlobais(metricas.getMetricasGlobais()));
        html.setMetricasGlobais(metricas.getMetricasGlobais());
        this.jTextAreaTarefa.setText(getResultadosTarefas(metricas));
        html.setMetricasTarefas(metricas);
        CS_Mestre mestre = (CS_Mestre) rdf.getMestres().get(0);
        setResultadosUsuario(mestre.getEscalonador().getMetricaUsuarios(), metricas);

        if (rdf.getMaquinas().size() < 21) {
            graficoProcessamentoTempo = new ChartPanel(criarGraficoProcessamentoTempo(rdf));
            graficoProcessamentoTempo.setPreferredSize(new Dimension(600, 300));
        } else {
            this.jButtonProcessamentoMaquina.setVisible(false);
            for (CS_Processamento maq : rdf.getMaquinas()) {
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

        if (config.getCreateMachineThroughTimeChart()) {
            charts.criarProcessamentoTempoMaquina(rdf);
        } else {
            charts.calculaPoderTotal(rdf);
        }
        if (config.getCreateTaskThroughTimeChart()) {
            charts.criarProcessamentoTempoTarefa(tarefas);
        }
        if (config.getCreateUserThroughTimeChart()) {
            charts.criarProcessamentoTempoUser(tarefas, rdf);
        }
        
        charts.criarGraficoPreempcao(rdf, tarefas);
        //graficoEstadoTarefa = new ChartPanel(criarGraficoEstadoTarefa(tarefas));
        //graficoEstadoTarefa.setPreferredSize(new Dimension(600, 300));
        //graficoEstadoTarefa2 = new ChartPanel(criarGraficoEstadoTarefa2(tarefas, rdf));
        //graficoEstadoTarefa2.setPreferredSize(new Dimension(600, 300));

        this.jScrollPaneProcessamento.setViewportView(this.graficoBarraProcessamento);
        this.jScrollPaneComunicacao.setViewportView(this.graficoBarraComunicacao);
        this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempo);

        //this.jScrollPaneProcessamento.setViewportView(this.graficoEstadoTarefa);
        //this.jScrollPaneComunicacao.setViewportView(this.graficoEstadoTarefa2);
        this.jScrollPaneCharts.setViewportView(charts.getProcessingBarChart());

        //this.jScrollPaneProcessamento.setViewportView(this.graficoEstadoTarefa);
        //this.jScrollPaneComunicacao.setViewportView(this.graficoEstadoTarefa2);
        
        jButtonExperimental.setVisible(false);
        charts.rede = rdf;
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
        jTabbedPanelGraficosIndividuais = new javax.swing.JTabbedPane();
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
        jTabbedPane2 = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Simulation Results");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("imagens/Logo_iSPD_25.png")));
        jButtonProcessBarra = new javax.swing.JButton();
        jButtonCommunicationBarra = new javax.swing.JButton();
        jButtonExperimental = new javax.swing.JButton();
        jScrollPaneCharts = new javax.swing.JScrollPane();
        jToolBar3 = new javax.swing.JToolBar();
        jButtonProcessPizza = new javax.swing.JButton();
        jButtonCommunicationPizza = new javax.swing.JButton();
        jToolBar4 = new javax.swing.JToolBar();
        jButtonUserModelo1 = new javax.swing.JButton();
        jButtonUserModelo2 = new javax.swing.JButton();
        jButtonProcessamentoMaquina = new javax.swing.JButton();
        jButtonProcessamentoTarefa = new javax.swing.JButton();
        jToolBar2 = new javax.swing.JToolBar();
        jButtonPreemption = new javax.swing.JButton();
        jButtonUsage1 = new javax.swing.JButton();
        jButtonUsage2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jToolBarTask = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldLerID = new javax.swing.JTextField();
        jButtonGerarGraficoTarefa = new javax.swing.JButton();
        jScrollPaneGraficoTarefa = new javax.swing.JScrollPane();
        jToolBar5 = new javax.swing.JToolBar();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldLerID1 = new javax.swing.JTextField();
        jButtonGerarGraficoMaquina = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Simulation Results");

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
            .addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanelGlobalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneGobal, javax.swing.GroupLayout.DEFAULT_SIZE, 637, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelGlobalLayout.setVerticalGroup(
            jPanelGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelGlobalLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 316, Short.MAX_VALUE))
            .addGroup(jPanelGlobalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelGlobalLayout.createSequentialGroup()
                    .addContainerGap(31, Short.MAX_VALUE)
                    .addComponent(jScrollPaneGobal, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jTabbedPanelGraficosIndividuais.addTab("Global", jPanelGlobal);

        jTextAreaTarefa.setEditable(false);
        jTextAreaTarefa.setColumns(20);
        jTextAreaTarefa.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
        jTextAreaTarefa.setRows(5);
        jScrollPaneTarefa.setViewportView(jTextAreaTarefa);

        jTabbedPaneGrid.addTab("Tasks", jScrollPaneTarefa);
        jTabbedPanelGraficosIndividuais.addTab("Tasks", jScrollPaneTarefa);

        jTextAreaUsuario.setColumns(20);
        jTextAreaUsuario.setEditable(false);
        jTextAreaUsuario.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
        jTextAreaUsuario.setRows(5);
        jScrollPaneUsuario.setViewportView(jTextAreaUsuario);

        jTabbedPaneGrid.addTab("User", jScrollPaneUsuario);
        jTabbedPanelGraficosIndividuais.addTab("User", jScrollPaneUsuario);

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
                .addComponent(jScrollPaneProcessamento, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE))
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
                .addComponent(jScrollPaneComunicacao, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE))
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
        jTabbedPanelGraficosIndividuais.addTab("Resources", jScrollPaneRecurso);

        jToolBarProcessamento.setFloatable(false);
        jToolBarProcessamento.setRollover(true);

        jButtonProcessBarra.setText("Process 1");
        jButtonProcessBarra.setFocusable(false);
        jButtonProcessBarra.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonProcessBarra.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonProcessBarra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonProcessBarraActionPerformed(evt);
            }
        });
        jToolBarProcessamento.add(jButtonProcessBarra);

        jButtonCommunicationBarra.setText("Network 1");
        jButtonCommunicationBarra.setFocusable(false);
        jButtonCommunicationBarra.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonCommunicationBarra.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonCommunicationBarra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCommunicationBarraActionPerformed(evt);
            }
        });
        jToolBarProcessamento.add(jButtonCommunicationBarra);

        jButtonExperimental.setText(".");
        jButtonExperimental.setFocusable(false);
        jButtonExperimental.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonExperimental.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonExperimental.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExperimentalActionPerformed(evt);
            }
        });
        jToolBarProcessamento.add(jButtonExperimental);

        jToolBar3.setFloatable(false);
        jToolBar3.setRollover(true);

        jButtonProcessPizza.setText("Process 2");
        jButtonProcessPizza.setFocusable(false);
        jButtonProcessPizza.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonProcessPizza.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonProcessPizza.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonProcessPizzaActionPerformed(evt);
            }
        });
        jToolBar3.add(jButtonProcessPizza);

        jButtonCommunicationPizza.setText("Network 2");
        jButtonCommunicationPizza.setFocusable(false);
        jButtonCommunicationPizza.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonCommunicationPizza.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonCommunicationPizza.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCommunicationPizzaActionPerformed(evt);
            }
        });
        jToolBar3.add(jButtonCommunicationPizza);

        jToolBar4.setFloatable(false);
        jToolBar4.setRollover(true);

        jButtonUserModelo1.setText("User 1");
        jButtonUserModelo1.setFocusable(false);
        jButtonUserModelo1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonUserModelo1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonUserModelo1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUserModelo1ActionPerformed(evt);
            }
        });
        jToolBar4.add(jButtonUserModelo1);

        jButtonUserModelo2.setText("User 2");
        jButtonUserModelo2.setFocusable(false);
        jButtonUserModelo2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonUserModelo2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonUserModelo2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUserModelo2ActionPerformed(evt);
            }
        });
        jToolBar4.add(jButtonUserModelo2);

        jButtonProcessamentoMaquina.setText("Machine");
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
        jToolBar4.add(jButtonProcessamentoMaquina);

        jButtonProcessamentoTarefa.setText("Task");
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
                .addComponent(jScrollPaneProcessamentoTempo, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE))
        );

        jTabbedPaneGrid.addTab("Use of computing power through time", jPanelProcessamentoTempo);
        jToolBar4.add(jButtonProcessamentoTarefa);

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);

        jButtonPreemption.setText("Preemption");
        jButtonPreemption.setFocusable(false);
        jButtonPreemption.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonPreemption.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonPreemption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPreemptionActionPerformed(evt);
            }
        });
        jToolBar2.add(jButtonPreemption);

        jButtonUsage1.setText("Usage 1");
        jButtonUsage1.setFocusable(false);
        jButtonUsage1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonUsage1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonUsage1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUsage1ActionPerformed(evt);
            }
        });
        jToolBar2.add(jButtonUsage1);

        jButtonUsage2.setText("Usage 2");
        jButtonUsage2.setFocusable(false);
        jButtonUsage2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonUsage2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonUsage2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUsage2ActionPerformed(evt);
            }
        });
        jToolBar2.add(jButtonUsage2);

        jPanelProcessamento.setLayout(jPanelProcessamentoLayout);
        jPanelProcessamentoLayout.setHorizontalGroup(
            jPanelProcessamentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jScrollPaneCharts)
            .addGroup(jPanelProcessamentoLayout.createSequentialGroup()
                .addComponent(jToolBarProcessamento, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar3, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar4, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE))
            .addComponent(jToolBar2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanelProcessamentoLayout.setVerticalGroup(
            jPanelProcessamentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelProcessamentoLayout.createSequentialGroup()
                .addGroup(jPanelProcessamentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToolBarProcessamento, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToolBar4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneCharts, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE))
        );

        jTabbedPanelGraficosIndividuais.addTab("Charts", jPanelProcessamento);

        jToolBarTask.setRollover(true);

        jLabel1.setText("Task Graph:");
        jToolBarTask.add(jLabel1);

        jTextFieldLerID.setColumns(20);
        jTextFieldLerID.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldLerID.setText("Type the task ID (Integer number)");
        jTextFieldLerID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldLerIDActionPerformed(evt);
            }
        });
        jTextFieldLerID.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldLerIDFocusGained(evt);
            }
        });
        jToolBarTask.add(jTextFieldLerID);

        jButtonGerarGraficoTarefa.setText("Generate Graph");
        jButtonGerarGraficoTarefa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGerarGraficoTarefaActionPerformed(evt);
            }
        });
        jToolBarTask.add(jButtonGerarGraficoTarefa);

        jToolBar5.setRollover(true);

        jLabel2.setText("Machine Graph:");
        jToolBar5.add(jLabel2);

        jTextFieldLerID1.setColumns(20);
        jTextFieldLerID1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldLerID1.setText("Type the machine name");
        jTextFieldLerID1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldLerID1ActionPerformed(evt);
            }
        });
        jTextFieldLerID1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldLerID1FocusGained(evt);
            }
        });
        jToolBar5.add(jTextFieldLerID1);

        jButtonGerarGraficoMaquina.setText("Generate Graph");
        jButtonGerarGraficoMaquina.setFocusable(false);
        jButtonGerarGraficoMaquina.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonGerarGraficoMaquina.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonGerarGraficoMaquina.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGerarGraficoMaquinaActionPerformed(evt);
            }
        });
        jToolBar5.add(jButtonGerarGraficoMaquina);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBarTask, javax.swing.GroupLayout.DEFAULT_SIZE, 661, Short.MAX_VALUE)
            .addComponent(jScrollPaneGraficoTarefa)
            .addComponent(jToolBar5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jToolBarTask, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar5, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneGraficoTarefa, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE))
        );

        jTabbedPanelGraficosIndividuais.addTab("Individual Graphs", jPanel1);

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
            .addComponent(jTabbedPaneGrid, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addComponent(jTabbedPanelGraficosIndividuais)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPanelGraficosIndividuais, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
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
        this.jScrollPaneCharts.setViewportView(charts.getMachineThroughTimeChart());
    }//GEN-LAST:event_jButtonProcessamentoMaquinaActionPerformed

    private void jButtonProcessamentoTarefaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonProcessamentoTarefaActionPerformed
        this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempoTarefa);
        this.jScrollPaneCharts.setViewportView(charts.getTaskThroughTimeChart());
    }//GEN-LAST:event_jButtonProcessamentoTarefaActionPerformed

    private void jButtonProcessamentoUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonProcessamentoUserActionPerformed
        if (this.jScrollPaneProcessamentoTempo.getViewport().getView() != this.graficoProcessamentoTempoUser1) {
            this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempoUser1);
        } else {
            this.jScrollPaneProcessamentoTempo.setViewportView(this.graficoProcessamentoTempoUser2);
        }
    }//GEN-LAST:event_jButtonProcessamentoUserActionPerformed

    private void jButtonUserModelo1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUserModelo1ActionPerformed
        this.jScrollPaneCharts.setViewportView(charts.getUserThroughTimeChart1());
    }//GEN-LAST:event_jButtonUserModelo1ActionPerformed

    private void jButtonCommunicationPizzaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCommunicationPizzaActionPerformed
        // TODO add your handling code here:
        this.jScrollPaneCharts.setViewportView(charts.getCommunicationPieChart());
    }//GEN-LAST:event_jButtonCommunicationPizzaActionPerformed

    private void jButtonCommunicationBarraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCommunicationBarraActionPerformed
        // TODO add your handling code here:
        this.jScrollPaneCharts.setViewportView(charts.getCommunicationBarChart());
    }//GEN-LAST:event_jButtonCommunicationBarraActionPerformed

    private void jButtonProcessPizzaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonProcessPizzaActionPerformed
        // TODO add your handling code here:
        this.jScrollPaneCharts.setViewportView(charts.getProcessingPieChart());
    }//GEN-LAST:event_jButtonProcessPizzaActionPerformed

    private void jButtonProcessBarraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonProcessBarraActionPerformed
        // TODO add your handling code here:
        this.jScrollPaneCharts.setViewportView(charts.getProcessingBarChart());
    }//GEN-LAST:event_jButtonProcessBarraActionPerformed

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
            TraceXML interpret = new TraceXML(file.getAbsolutePath());
            interpret.geraTraceSim(tarefas);
        }
    }//GEN-LAST:event_jButtonSalvarTracesActionPerformed

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
                Logger.getLogger(JResultados.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButtonSalvarActionPerformed

    // Variables declaration - do not modify                     
    private javax.swing.JButton jButtonCBarra;
    private javax.swing.JButton jButtonCPizza;
    private javax.swing.JButton jButtonPBarra;
    private javax.swing.JButton jButtonPPizza;
    private javax.swing.JButton jButtonProcessamentoTarefa;
    private javax.swing.JButton jButtonProcessamentoUser;
    private javax.swing.JPanel jPanelComunicacao;
    private javax.swing.JPanel jPanelProcessamentoTempo;
    private javax.swing.JScrollPane jScrollPaneComunicacao;
    private javax.swing.JScrollPane jScrollPaneProcessamento;
    private javax.swing.JScrollPane jScrollPaneProcessamentoTempo;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPaneGrid;

    private void jButtonGerarGraficoTarefaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGerarGraficoTarefaActionPerformed
        try {
            String ID = jTextFieldLerID.getText();
            int task = Integer.parseInt(ID);
            if (task >= 0 && task < tarefas.size()) {
                this.jScrollPaneGraficoTarefa.setViewportView(charts.criarGraficoPorTarefa(tarefas, task));
            } else {
                JOptionPane.showMessageDialog(jPanel1, "Task not found. Your tasks go from 0 to " + (tarefas.size() - 1), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(jPanel1, "The task id is an integer", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButtonGerarGraficoTarefaActionPerformed

    private void jTextFieldLerIDFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldLerIDFocusGained
        jTextFieldLerID.selectAll();
    }//GEN-LAST:event_jTextFieldLerIDFocusGained

    private void jTextFieldLerIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldLerIDActionPerformed
        jButtonGerarGraficoTarefaActionPerformed(null);
    }//GEN-LAST:event_jTextFieldLerIDActionPerformed

    private void jButtonUserModelo2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUserModelo2ActionPerformed
        this.jScrollPaneCharts.setViewportView(charts.getUserThroughTimeChart2());
    }//GEN-LAST:event_jButtonUserModelo2ActionPerformed

    private void jButtonExperimentalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExperimentalActionPerformed
        this.jScrollPaneCharts.setViewportView(new ChartPanel(null));
    }//GEN-LAST:event_jButtonExperimentalActionPerformed

    private void jTextFieldLerID1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldLerID1ActionPerformed
        jButtonGerarGraficoMaquinaActionPerformed(null);
    }//GEN-LAST:event_jTextFieldLerID1ActionPerformed

    private void jTextFieldLerID1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldLerID1FocusGained
        jTextFieldLerID1.selectAll();
    }//GEN-LAST:event_jTextFieldLerID1FocusGained

    private void jButtonGerarGraficoMaquinaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGerarGraficoMaquinaActionPerformed
        String id = jTextFieldLerID1.getText();
        ChartPanel grafico = charts.gerarGraficoPorMaquina(tarefas, id);
        if(grafico != null){
            this.jScrollPaneGraficoTarefa.setViewportView(grafico);
        }
        else{
            JOptionPane.showMessageDialog(jPanel1, "Machine not Found","Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButtonGerarGraficoMaquinaActionPerformed

    private void jButtonPreemptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPreemptionActionPerformed
        this.jScrollPaneCharts.setViewportView(charts.PreemptionPerUser);
    }//GEN-LAST:event_jButtonPreemptionActionPerformed

    private void jButtonUsage1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUsage1ActionPerformed
        this.jScrollPaneCharts.setViewportView(charts.criarGraficoAproveitamento(tarefas));
    }//GEN-LAST:event_jButtonUsage1ActionPerformed

    private void jButtonUsage2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUsage2ActionPerformed
        this.jScrollPaneCharts.setViewportView(charts.criarGraficoNumTarefasAproveitamento(tarefas));
    }//GEN-LAST:event_jButtonUsage2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCommunicationBarra;
    private javax.swing.JButton jButtonCommunicationPizza;
    private javax.swing.JButton jButtonExperimental;
    private javax.swing.JButton jButtonGerarGraficoMaquina;
    private javax.swing.JButton jButtonGerarGraficoTarefa;
    private javax.swing.JButton jButtonPreemption;
    private javax.swing.JButton jButtonProcessBarra;
    private javax.swing.JButton jButtonProcessPizza;
    private javax.swing.JButton jButtonProcessamentoMaquina;
    private javax.swing.JButton jButtonSalvar;
    private javax.swing.JButton jButtonSalvarTraces;
    private javax.swing.JButton jButtonUsage1;
    private javax.swing.JButton jButtonUsage2;
    private javax.swing.JButton jButtonUserModelo1;
    private javax.swing.JButton jButtonUserModelo2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelGlobal;
    private javax.swing.JPanel jPanelProcessamento;
    private javax.swing.JScrollPane jScrollPaneCharts;
    private javax.swing.JScrollPane jScrollPaneGobal;
    private javax.swing.JScrollPane jScrollPaneGraficoTarefa;
    private javax.swing.JScrollPane jScrollPaneRecurso;
    private javax.swing.JScrollPane jScrollPaneTarefa;
    private javax.swing.JScrollPane jScrollPaneUsuario;
    private javax.swing.JTabbedPane jTabbedPanelGraficosIndividuais;
    private javax.swing.JTable jTableRecurso;
    private javax.swing.JTextArea jTextAreaGlobal;
    private javax.swing.JTextArea jTextAreaTarefa;
    private javax.swing.JTextArea jTextAreaUsuario;
    private javax.swing.JToolBar jToolBarComunicacao;
    private javax.swing.JToolBar jToolBarProcessamentoTempo;
    // End of variables declaration                   
    private List<Tarefa> tarefas;
    private Object[][] tabelaRecurso;
    private ChartPanel graficoBarraProcessamento;
    private ChartPanel graficoBarraComunicacao;
    private ChartPanel graficoPizzaProcessamento;
    private ChartPanel graficoPizzaComunicacao;
    private ChartPanel graficoProcessamentoTempo;
    private ChartPanel graficoProcessamentoTempoTarefa;
    private ChartPanel graficoProcessamentoTempoUser1;
    private ChartPanel graficoProcessamentoTempoUser2;
    private ChartPanel graficoEstadoTarefa;
    private ChartPanel graficoEstadoTarefa2;
    private double poderComputacionalTotal = 0;
    private javax.swing.JTextField jTextFieldLerID;
    private javax.swing.JTextField jTextFieldLerID1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    private javax.swing.JToolBar jToolBar4;
    private javax.swing.JToolBar jToolBar5;
    private javax.swing.JToolBar jToolBarProcessamento;
    private javax.swing.JToolBar jToolBarTask;
    // End of variables declaration//GEN-END:variables
    private Graficos charts;
    private SalvarResultadosHTML html = new SalvarResultadosHTML();

    private String getResultadosGlobais(MetricasGlobais globais) {
        String texto = "\t\tSimulation Results\n\n";
        texto += String.format("\tTotal Simulated Time = %g \n", globais.getTempoSimulacao());
        texto += String.format("\tSatisfaction = %g %%\n", globais.getSatisfacaoMedia());
        texto += String.format("\tIdleness of processing resources = %g %%\n", globais.getOciosidadeComputacao());
        texto += String.format("\tIdleness of communication resources = %g %%\n", globais.getOciosidadeComunicacao());
        texto += String.format("\tEfficiency = %g %%\n", globais.getEficiencia());
        if (globais.getEficiencia() > 70.0) {
            texto += "\tEfficiency GOOD\n ";
        } else if (globais.getEficiencia() > 40.0) {
            texto += "\tEfficiency MEDIA\n ";
        } else {
            texto += "\tEfficiency BAD\n ";
        }
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

    private JFreeChart[] gerarGraficoProcessamentoTempoUser(List<Tarefa> tarefas, RedeDeFilas rdf) {
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
//        html.setTabela(tabelaRecurso);
//        BufferedImage[] charts = new BufferedImage[8];
//        if (this.graficoBarraProcessamento != null) {
//            charts[0] = this.graficoBarraProcessamento.getChart().createBufferedImage(1200, 600);
//        }
//        if (this.graficoPizzaProcessamento != null) {
//            charts[1] = this.graficoPizzaProcessamento.getChart().createBufferedImage(1200, 600);
//        }
//        if (this.graficoBarraComunicacao != null) {
//            charts[2] = this.graficoBarraComunicacao.getChart().createBufferedImage(1200, 600);
//        }
//        if (this.graficoPizzaComunicacao != null) {
//            charts[3] = this.graficoPizzaComunicacao.getChart().createBufferedImage(1200, 600);
//        }
//        if (this.graficoProcessamentoTempo != null) {
//            charts[4] = this.graficoProcessamentoTempo.getChart().createBufferedImage(1200, 600);
//        }
//        if (this.graficoProcessamentoTempoTarefa != null) {
//            charts[5] = this.graficoProcessamentoTempoTarefa.getChart().createBufferedImage(1200, 600);
//        }
//        if (this.graficoProcessamentoTempoUser1 != null) {
//            charts[6] = this.graficoProcessamentoTempoUser1.getChart().createBufferedImage(1200, 600);
//        }
//        if (this.graficoProcessamentoTempoUser2 != null) {
//            charts[7] = this.graficoProcessamentoTempoUser2.getChart().createBufferedImage(1200, 600);
//        }
//        html.setCharts(charts);
        BufferedImage[] chartsImagem = new BufferedImage[8];
        if (charts.getProcessingBarChart() != null) {
            chartsImagem[0] = charts.getProcessingBarChart().getChart().createBufferedImage(1200, 600);
        }
        if (charts.getProcessingPieChart() != null) {
            chartsImagem[1] = charts.getProcessingPieChart().getChart().createBufferedImage(1200, 600);
        }
        if (charts.getCommunicationBarChart() != null) {
            chartsImagem[2] = charts.getCommunicationBarChart().getChart().createBufferedImage(1200, 600);
        }
        if (charts.getCommunicationPieChart() != null) {
            chartsImagem[3] = charts.getCommunicationPieChart().getChart().createBufferedImage(1200, 600);
        }
        if (charts.getMachineThroughTimeChart() != null) {
            chartsImagem[4] = charts.getMachineThroughTimeChart().getChart().createBufferedImage(1200, 600);
        }
        if (charts.getTaskThroughTimeChart() != null) {
            chartsImagem[5] = charts.getTaskThroughTimeChart().getChart().createBufferedImage(1200, 600);
        }
        if (charts.getUserThroughTimeChart1() != null) {
            chartsImagem[6] = charts.getUserThroughTimeChart1().getChart().createBufferedImage(1200, 600);
        }
        if (charts.getUserThroughTimeChart2() != null) {
            chartsImagem[7] = charts.getUserThroughTimeChart2().getChart().createBufferedImage(1200, 600);
        }
        html.setCharts(chartsImagem);
        try {
            html.gerarHTML(file);
        } catch (IOException ex) {
            Logger.getLogger(JResultados.class.getName()).log(Level.SEVERE, null, ex);
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

    private JFreeChart criarGraficoEstadoTarefa2(List<Tarefa> tarefas, RedeDeFilas rdf) {
        DefaultCategoryDataset dados = new DefaultCategoryDataset();
        for (CS_Processamento maq : rdf.getMaquinas()) {
            dados.addValue(0, "Canceled", maq.getId());
            dados.addValue(0, "Completed", maq.getId());
            dados.addValue(0, "Not executed", maq.getId());
            dados.addValue(0, "Failures", maq.getId());
        }
        for (CS_Processamento maq : rdf.getMestres()) {
            if (maq instanceof CS_Mestre) {
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
    private JFreeChart criarGraficoProcessamentoTempo(RedeDeFilas rdf) {
        XYSeriesCollection dadosGrafico = new XYSeriesCollection();
        //Se tiver alguma máquina na lista.
        if (rdf.getMaquinas() != null) {
            //Laço foreach que percorre as máquinas.
            for (CS_Processamento maq : rdf.getMaquinas()) {
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

    private void setButtons(ConfiguracaoISPD config) {
        jButtonProcessBarra.setEnabled(config.getCreateProcessingChart());
        jButtonProcessPizza.setEnabled(config.getCreateProcessingChart());
        jButtonCommunicationBarra.setEnabled(config.getCreateCommunicationChart());
        jButtonCommunicationPizza.setEnabled(config.getCreateCommunicationChart());
        jButtonProcessamentoMaquina.setEnabled(config.getCreateMachineThroughTimeChart());
        jButtonProcessamentoTarefa.setEnabled(config.getCreateTaskThroughTimeChart());
        jButtonUserModelo1.setEnabled(config.getCreateUserThroughTimeChart());
        jButtonUserModelo2.setEnabled(config.getCreateUserThroughTimeChart());
    }
}
