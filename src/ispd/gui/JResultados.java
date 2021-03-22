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
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.metricas.Metricas;
import ispd.motor.metricas.MetricasComunicacao;
import ispd.motor.metricas.MetricasGlobais;
import ispd.motor.metricas.MetricasProcessamento;
import ispd.motor.metricas.MetricasUsuarios;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
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
        charts = new Graficos();
        charts.criarProcessamento(metricas.getMetricasProcessamento());
        charts.criarComunicacao(metricas.getMetricasComunicacao());
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneGobal, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPanelGraficosIndividuais.addTab("Global", jPanelGlobal);

        jTextAreaTarefa.setEditable(false);
        jTextAreaTarefa.setColumns(20);
        jTextAreaTarefa.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
        jTextAreaTarefa.setRows(5);
        jScrollPaneTarefa.setViewportView(jTextAreaTarefa);

        jTabbedPanelGraficosIndividuais.addTab("Tasks", jScrollPaneTarefa);

        jTextAreaUsuario.setColumns(20);
        jTextAreaUsuario.setEditable(false);
        jTextAreaUsuario.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
        jTextAreaUsuario.setRows(5);
        jScrollPaneUsuario.setViewportView(jTextAreaUsuario);

        jTabbedPanelGraficosIndividuais.addTab("User", jScrollPaneUsuario);

        jTableRecurso.setModel(new javax.swing.table.DefaultTableModel(tabelaRecurso,colunas));
        jScrollPaneRecurso.setViewportView(jTableRecurso);

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

        javax.swing.GroupLayout jPanelProcessamentoLayout = new javax.swing.GroupLayout(jPanelProcessamento);
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
            .addComponent(jTabbedPanelGraficosIndividuais)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPanelGraficosIndividuais, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonProcessamentoTarefaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonProcessamentoTarefaActionPerformed
        this.jScrollPaneCharts.setViewportView(charts.getTaskThroughTimeChart());
    }//GEN-LAST:event_jButtonProcessamentoTarefaActionPerformed

    private void jButtonProcessamentoMaquinaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonProcessamentoMaquinaActionPerformed
        this.jScrollPaneCharts.setViewportView(charts.getMachineThroughTimeChart());
    }//GEN-LAST:event_jButtonProcessamentoMaquinaActionPerformed

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
    private javax.swing.JButton jButtonProcessamentoTarefa;
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
    private List<Tarefa> tarefas;
    private Object[][] tabelaRecurso;
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

    /**
     * Salva resultados obtidos em um arquivo html
     *
     * @param file diretório destino
     */
    public void salvarHTML(File file) {
        //Gerar resultados:
        html.setTabela(tabelaRecurso);
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
            jTextAreaUsuario.setText(texto);
        } else {
            jTabbedPanelGraficosIndividuais.remove(jScrollPaneUsuario);
        }
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
