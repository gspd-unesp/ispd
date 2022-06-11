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
 * JSimulacao.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 
 * 09-Set-2014 : Version 2.0;
 * 18-Set-2014 : Retirado analise de modelo iconico e simulável
 *
 */
package ispd.gui;

import ispd.arquivo.xml.ConfiguracaoISPD;
import ispd.motor.ProgressoSimulacao;
import ispd.motor.metricas.Metricas;
import java.awt.Color;
import ispd.arquivo.xml.IconicoXML;
import ispd.motor.Simulation;
import ispd.motor.SimulacaoSequencial;
import ispd.motor.SimulacaoSequencialCloud;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.RedeDeFilasCloud;
import ispd.motor.filas.Tarefa;

import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.w3c.dom.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Realiza faz chamada ao motor de simulação e apresenta os passos realizados e
 * porcentagem da simulação concluida
 *
 * @author denison
 */
public class JSimulacao extends javax.swing.JDialog implements Runnable {

    public JSimulacao(javax.swing.JFrame parent, Document modelo, ResourceBundle plavras, ConfiguracaoISPD configuracao) {
        super(parent, true);
        this.palavras = plavras;
        this.configuracao = configuracao;
        this.progrSim = new ProgressoSimulacao() {
            @Override
            public void incProgresso(int n) {
                porcentagem += n;
                int value = (int) porcentagem;
                jProgressBar.setValue(value);
            }

            @Override
            public void print(String text, Color cor) {
                javax.swing.text.Document doc = jTextPaneNotificacao.getDocument();
                try {
                    if (cor != null) {
                        StyleConstants.setForeground(configuraCor, cor);
                    } else {
                        StyleConstants.setForeground(configuraCor, Color.black);
                    }
                    if (palavras != null && text != null && palavras.containsKey(text)) {
                        doc.insertString(doc.getLength(), palavras.getString(text), configuraCor);
                    } else {
                        doc.insertString(doc.getLength(), text, configuraCor);
                    }
                } catch (BadLocationException ex) {
                    Logger.getLogger(JSimulacao.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        initComponents();
        this.modelo = modelo;
        this.modeloTexto = modeloTexto;
        this.tarefas = null;
        this.redeDeFilas = null;
        this.redeDeFilasCloud = null;
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (threadSim != null) {
                    //threadSim.stop();
                    threadSim = null;
                }
                dispose();
            }
        });
    }

    public JSimulacao(java.awt.Frame parent, boolean modal, Document modelo, String modeloTexto, ResourceBundle plavras, int tipoModelo) {
        super(parent, modal);
        this.palavras = plavras;
        this.tipoModelo = tipoModelo;
        this.progrSim = new ProgressoSimulacao() {
            @Override
            public void incProgresso(int n) {
                porcentagem += n;
                int value = (int) porcentagem;
                jProgressBar.setValue(value);
            }

            @Override
            public void print(String text, Color cor) {
                javax.swing.text.Document doc = jTextPaneNotificacao.getDocument();
                try {
                    if (cor != null) {
                        StyleConstants.setForeground(configuraCor, cor);
                    } else {
                        StyleConstants.setForeground(configuraCor, Color.black);
                    }
                    if (palavras.containsKey(text)) {
                        doc.insertString(doc.getLength(), palavras.getString(text), configuraCor);
                    } else {
                        doc.insertString(doc.getLength(), text, configuraCor);
                    }
                } catch (BadLocationException ex) {
                    Logger.getLogger(JSimulacao.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        initComponents();
        this.modelo = modelo;
        this.modeloTexto = modeloTexto;
        this.tarefas = null;
        this.redeDeFilas = null;
        this.redeDeFilasCloud = null;
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (threadSim != null) {
                    //threadSim.stop();
                    threadSim = null;
                }
                dispose();
            }
        });
    }

    public JSimulacao() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int getTipoModelo() {
        return tipoModelo;
    }

    public void setTipoModelo(int tipoModelo) {
        this.tipoModelo = tipoModelo;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jProgressBar = new javax.swing.JProgressBar();
        jButtonCancelar = new javax.swing.JButton();
        jScrollPane = new javax.swing.JScrollPane();
        jTextPaneNotificacao = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(palavras.getString("Running Simulation")); // NOI18N

        jProgressBar.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);

        jButtonCancelar.setText(palavras.getString("Cancel")); // NOI18N
        jButtonCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelarActionPerformed(evt);
            }
        });

        jTextPaneNotificacao.setEditable(false);
        jTextPaneNotificacao.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jScrollPane.setViewportView(jTextPaneNotificacao);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonCancelar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                    .addComponent(jScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                    .addComponent(jProgressBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>                        

    private void jButtonCancelarActionPerformed(java.awt.event.ActionEvent evt) {                                                
        // TODO add your handling code here:
        if (this.threadSim != null) {
            //this.threadSim.stop();
            this.threadSim = null;
        }
        this.dispose();
    }                                               
    // Variables declaration - do not modify                     
    private javax.swing.JButton jButtonCancelar;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JTextPane jTextPaneNotificacao;
    // End of variables declaration                   
    private SimpleAttributeSet configuraCor = new SimpleAttributeSet();
    private Thread threadSim;
    private RedeDeFilas redeDeFilas;
    private RedeDeFilasCloud redeDeFilasCloud;
    private List<Tarefa> tarefas;
    private String modeloTexto;
    private Document modelo;
    private ResourceBundle palavras;
    private double porcentagem = 0;
    private ProgressoSimulacao progrSim;
    private int tipoModelo; //define se o modelo simulado é de grid, cloud iaas ou cloud paas

    public void setRedeDeFilas(RedeDeFilas redeDeFilas) {
        this.redeDeFilas = redeDeFilas;
    }

    public void setTarefas(List<Tarefa> tarefas) {
        this.tarefas = tarefas;
    }

    public List<Tarefa> getTarefas() {
        return tarefas;
    }

    private ConfiguracaoISPD configuracao;

    public void iniciarSimulacao() {
        threadSim = new Thread(this);
        threadSim.start();
    }

    public void setMaxProgresso(int n) {
        jProgressBar.setMaximum(n);
    }

    public void setProgresso(int n) {
        this.porcentagem = n;
        jProgressBar.setValue(n);
    }

    public void incProgresso(double n) {
        this.porcentagem += n;
        int value = (int) porcentagem;
        jProgressBar.setValue(value);
    }

    @Override
    public void run() {
        progrSim.println("Simulation Initiated.");
        try {
            //0%
            //Verifica se foi construido modelo na area de desenho
            progrSim.validarInicioSimulacao(modelo);//[5%] --> 5%
            //Constrói e verifica modelos icônicos e simuláveis
            progrSim.AnalisarModelos(modeloTexto);//[20%] --> 25%
            //criar grade
            progrSim.print("Mounting network queue.");
            progrSim.print(" -> ");
            if (tipoModelo == EscolherClasse.GRID) {
                this.redeDeFilas = IconicoXML.newRedeDeFilas(modelo);
                incProgresso(10);//[10%] --> 35%
                progrSim.println("OK", Color.green);
                //criar tarefas
                progrSim.print("Creating tasks.");
                progrSim.print(" -> ");
                this.tarefas = IconicoXML.newGerarCarga(modelo).toTarefaList(redeDeFilas);
                incProgresso(10);//[10%] --> 45%
                progrSim.println("OK", Color.green);
                //Verifica recursos do modelo e define roteamento
                Simulation sim = new SimulacaoSequencial(progrSim, redeDeFilas, tarefas);//[10%] --> 55 %
                //Realiza asimulação
                progrSim.println("Simulating.");
                //recebe instante de tempo em milissegundos ao iniciar a simulação
                double t1 = System.currentTimeMillis();

                sim.simulate();//[30%] --> 85%

                //Recebe instnte de tempo em milissegundos ao fim da execução da simulação
                double t2 = System.currentTimeMillis();
                //Calcula tempo de simulação em segundos
                double tempototal = (t2 - t1) / 1000;
                //Obter Resultados
                Metricas metrica = sim.getMetrics();
                //[5%] --> 90%
                //Apresentar resultados
                progrSim.print("Showing results.");
                progrSim.print(" -> ");
                JResultados janelaResultados = new JResultados(null, metrica, redeDeFilas, tarefas);
                incProgresso(10);//[10%] --> 100%
                progrSim.println("OK", Color.green);
                progrSim.println("Simulation Execution Time = " + tempototal + "seconds");
                janelaResultados.setLocationRelativeTo(this);
                janelaResultados.setVisible(true);

            } else if (tipoModelo == EscolherClasse.IAAS) {
                this.redeDeFilasCloud = IconicoXML.newRedeDeFilasCloud(modelo);
                incProgresso(10);//[10%] --> 35%
                progrSim.println("OK", Color.green);
                //criar tarefas
                progrSim.print("Creating tasks.");
                progrSim.print(" -> ");
                this.tarefas = IconicoXML.newGerarCarga(modelo).toTarefaList(redeDeFilasCloud);
                incProgresso(10);//[10%] --> 45%
                progrSim.println("OK", Color.green);
                //Verifica recursos do modelo e define roteamento
                Simulation sim = new SimulacaoSequencialCloud(progrSim, redeDeFilasCloud, tarefas);//[10%] --> 55 %
                //Realiza asimulação
                progrSim.println("Simulating.");
                //recebe instante de tempo em milissegundos ao iniciar a simulação
                double t1 = System.currentTimeMillis();

                sim.simulate();//[30%] --> 85%

                //Recebe instnte de tempo em milissegundos ao fim da execução da simulação
                double t2 = System.currentTimeMillis();
                //Calcula tempo de simulação em segundos
                double tempototal = (t2 - t1) / 1000;
                //Obter Resultados
                Metricas metrica = sim.getCloudMetrics();
                //[5%] --> 90%
                //Apresentar resultados
                progrSim.print("Showing results.");
                progrSim.print(" -> ");
                JResultadosCloud janelaResultados = new JResultadosCloud(null, metrica, redeDeFilasCloud, tarefas);
                incProgresso(10);//[10%] --> 100%
                progrSim.println("OK", Color.green);
                progrSim.println("Simulation Execution Time = " + tempototal + "seconds");
                janelaResultados.setLocationRelativeTo(this);
                janelaResultados.setVisible(true);
            }
        } catch (IllegalArgumentException erro) {

            Logger.getLogger(JSimulacao.class.getName()).log(Level.SEVERE, null, erro);
            progrSim.println(erro.getMessage(), Color.red);
            progrSim.print("Simulation Aborted", Color.red);
            progrSim.println("!", Color.red);
        }
    }
}
