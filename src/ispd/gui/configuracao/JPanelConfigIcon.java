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
 * JPanelConfigIcon.java
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
 *
 */
package ispd.gui.configuracao;

import ispd.escalonador.ManipularArquivos;
import ispd.alocacaoVM.ManipularArquivosAlloc;
import ispd.arquivo.Alocadores;
import ispd.arquivo.EscalonadoresCloud;
import ispd.escalonador.ManipularArquivos;
import ispd.escalonadorCloud.ManipularArquivosCloud;
import ispd.gui.iconico.grade.Cluster;
import ispd.gui.iconico.grade.Internet;
import ispd.gui.iconico.grade.ItemGrade;
import ispd.gui.iconico.grade.Link;
import ispd.gui.iconico.grade.Machine;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import ispd.gui.EscolherClasse;
import java.awt.event.MouseEvent;

/**
 *
 * @author denison
 */
public class JPanelConfigIcon extends javax.swing.JPanel {

    /**
     * Creates new form JPanelConfigIcon
     */
    private VariedRowTable Tmachine;
    private VariedRowTable TmachineIaaS;
    private VariedRowTable Tcluster;
    private VariedRowTable TclusterIaaS;
    private VariedRowTable Tlink;
    private ResourceBundle palavras;
    private ManipularArquivos escalonadores;
    private ManipularArquivosCloud escalonadoresCloud;
    private ManipularArquivosAlloc alocadores;

    public JPanelConfigIcon() {
        palavras = ResourceBundle.getBundle("ispd.idioma.Idioma", new Locale("en", "US"));
        Tmachine = new VariedRowTable(){
            //Implementa as dicas para cada célula da tabela          
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);

                try {
                    if(colIndex==1){
                        if(rowIndex==0){
                            tip = "Insert the label name of the resource";
                        }else if(rowIndex==1){
                            tip = "Select the resource owner";
                        }else if(rowIndex==2){
                            tip = "Insert the amount of computing power of the resource in MFlops";
                        }else if(rowIndex==3){
                            tip = "Insert the percentage of background computing in decimal notation";
                        }else if(rowIndex==4){
                            tip = "Insert the number of precessing cores of the resource";
                        }else if(rowIndex==5){
                            tip = "Insert the amount of memory of the resource in MBytes";
                        }else if(rowIndex==6){
                            tip = "Insert the amount of hard disk of the resource in GBytes";
                        }else if(rowIndex==7){
                            tip = "Select if the resource is master node";
                        }else if(rowIndex==8){
                            tip = "Select the task scheduling policy of the master";
                        }else if(rowIndex==9){
                            tip = "Select the slave nodes that will be coordinated by this master";
                        }
                    }
                } catch (RuntimeException e1) {
                    //catch null pointer exception if mouse is over an empty line
                }

                return tip;
            }
        };
        Tmachine.setModel(new MachineTable(palavras));
        Tmachine.setRowHeight(20);
        TmachineIaaS = new VariedRowTable(){
            //Implementa as dicas para cada célula da tabela          
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);

                try {
                    if(colIndex==1){
                        if(rowIndex==0){
                            tip = "Insert the label name of the resource";
                        }else if(rowIndex==1){
                            tip = "Select the resource owner";
                        }else if(rowIndex==2){
                            tip = "Insert the amount of computing power of the resource in MFlops";
                        }else if(rowIndex==3){
                            tip = "Insert the percentage of background computing in decimal notation";
                        }else if(rowIndex==4){
                            tip = "Insert the number of precessing cores of the resource";
                        }else if(rowIndex==5){
                            tip = "Insert the amount of memory of the resource in MBytes";
                        }else if(rowIndex==6){
                            tip = "Insert the amount of hard disk of the resource in GBytes";
                        }else if(rowIndex==7){
                            tip = "Insert the cost of processing utilization ($/cores/h)";
                        }else if(rowIndex==8){
                            tip = "Insert the cost of memory utilization ($/MB/h)";
                        }else if(rowIndex==9){
                            tip = "Insert the cost of disk utilization ($/GB/h)";
                        }else if(rowIndex==10){
                            tip = "Select if the resource is a virtual machine monitor";
                        }else if(rowIndex==11){
                            tip = "Select the task scheduling policy of the VMM";
                        }else if(rowIndex==12){
                            tip = "Select the virtual machine allocation policy of the VMM";
                        }else if(rowIndex==13){
                            tip = "Select the nodes that will be coordinated by this VMM";
                        }
                    }
                } catch (RuntimeException e1) {
                    //catch null pointer exception if mouse is over an empty line
                }

                return tip;
            }
        };

        TmachineIaaS.setModel(new MachineTableIaaS(palavras));
        TmachineIaaS.setRowHeight(20);
        Tcluster = new VariedRowTable(){
            //Implementa as dicas para cada célula da tabela          
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);

                try {
                    if(colIndex==1){
                        if(rowIndex==0){
                            tip = "Insert the label name of the resource";
                        }else if(rowIndex==1){
                            tip = "Select the resource owner";
                        }else if(rowIndex==2){
                            tip = "Insert the number of nodes that composes the cluster";
                        }else if(rowIndex==3){
                             tip = "Insert the amount of computing power of the resource in MFlops";
                        }else if(rowIndex==4){
                            tip = "Insert the number of precessing cores of the resource";
                        }else if(rowIndex==5){
                            tip = "Insert the amount of memory of the resource in MBytes";
                        }else if(rowIndex==6){
                            tip = "Insert the amount of hard disk of the resource in GBytes";
                        }else if(rowIndex==7){
                            tip = "Insert the amount of bandwidth that connect the cluster nodes in Mbps";
                        }else if(rowIndex==8){
                            tip = "Insert the latency time of the links that connect the cluster nodes in seconds";
                        }else if(rowIndex==9){
                            tip = "Select if the resource is a master node";
                        }else if(rowIndex==10){
                            tip = "Select the task scheduling policy of the master node";
                        }else if(rowIndex==11){
                            tip = "Select the slave nodes that will be coordinated by this master";
                        }
                    }
                } catch (RuntimeException e1) {
                    //catch null pointer exception if mouse is over an empty line
                }

                return tip;
            }
        };
        Tcluster.setModel(new ClusterTable(palavras));
        Tcluster.setRowHeight(20);
        TclusterIaaS = new VariedRowTable(){
            //Implementa as dicas para cada célula da tabela          
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);

                try {
                    if(colIndex==1){
                        if(rowIndex==0){
                            tip = "Insert the label name of the resource";
                        }else if(rowIndex==1){
                            tip = "Select the resource owner";
                        }else if(rowIndex==2){
                            tip = "Insert the number of nodes that composes the cluster";
                        }else if(rowIndex==3){
                             tip = "Insert the amount of computing power of the resource in MFlops";
                        }else if(rowIndex==4){
                            tip = "Insert the number of precessing cores of the resource";
                        }else if(rowIndex==5){
                            tip = "Insert the amount of memory of the resource in MBytes";
                        }else if(rowIndex==6){
                            tip = "Insert the amount of hard disk of the resource in GBytes";
                        }else if(rowIndex==7){
                            tip = "Insert the cost of processing utilization ($/cores/h)";
                        }else if(rowIndex==8){
                            tip = "Insert the cost of memory utilization ($/MB/h)";
                        }else if(rowIndex==9){
                            tip = "Insert the cost of disk utilization ($/GB/h)";
                        }else if(rowIndex==10){
                            tip = "Insert the amount of bandwidth that connect the cluster nodes in Mbps";
                        }else if(rowIndex==11){
                            tip = "Insert the latency time of the links that connect the cluster nodes in seconds";
                        }else if(rowIndex==12){
                            tip = "Select if the resource is a virtual machine monitor";
                        }else if(rowIndex==13){
                            tip = "Select the task scheduling policy of the VMM";
                        }else if(rowIndex==14){
                            tip = "Select the virtual machine allocation policy of the VMM";
                        }else if(rowIndex==15){
                            tip = "Select the nodes that will be coordinated by this VMM";
                        }
                    }
                } catch (RuntimeException e1) {
                    //catch null pointer exception if mouse is over an empty line
                }

                return tip;
            }
        };
        TclusterIaaS.setModel(new ClusterTableIaaS(palavras));
        TclusterIaaS.setRowHeight(20);
        Tlink = new VariedRowTable(){
            //Implementa as dicas para cada célula da tabela          
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);

                try {
                    if(colIndex==1){
                        if(rowIndex==0){
                            tip = "Insert the label name of the resource";
                        }else if(rowIndex==1){
                             tip = "Insert the latency time of the resource in seconds";
                        }else if(rowIndex==2){
                            tip = "Insert the percentage of background communication in decimal notation";
                        }else if(rowIndex==3){
                            tip = "Insert the amount of bandwidth of the resource in seconds";
                        }
                    }
                } catch (RuntimeException e1) {
                    //catch null pointer exception if mouse is over an empty line
                }

                return tip;
            }
        };
        Tlink.setModel(new LinkTable(palavras));
        Tlink.setRowHeight(20);
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jLabelTitle = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jLabelIconName = new javax.swing.JLabel();

        jLabelTitle.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabelTitle.setText("Machine icon configuration");

        jLabelIconName.setText("Configuration for the icon # 0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabelTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelIconName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelIconName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelIconName;
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables

    public void setEscalonadores(ManipularArquivos escalonadores) {
        this.escalonadores = escalonadores;
        for (Object escal : escalonadores.listar()) {
            getTabelaMaquina().getEscalonadores().addItem(escal);
            //getTabelaMaquinaIaaS().getEscalonadores().addItem(escal);
            getTabelaCluster().getEscalonadores().addItem(escal);
        }
    }

    public void setEscalonadoresCloud(ManipularArquivosCloud escalonadoresCloud) {
        this.escalonadoresCloud = escalonadoresCloud;
        for (Object escal : escalonadoresCloud.listar()) {
            getTabelaMaquinaIaaS().getEscalonadores().addItem(escal);
            getTabelaClusterIaaS().getEscalonadores().addItem(escal);
        }
    }

    public void setAlocadores(ManipularArquivosAlloc alocadores) {
        this.alocadores = alocadores;
        for (Object alloc : alocadores.listar()) {
            getTabelaMaquinaIaaS().getAlocadores().addItem(alloc);
            getTabelaClusterIaaS().getAlocadores().addItem(alloc);
        }
    }

    public void setIcone(ItemGrade icone) {
        if (icone instanceof Link) {
            jLabelTitle.setText(palavras.getString("Network icon configuration"));
            System.out.println(palavras.getLocale() + " - " + palavras.getString("Network icon configuration"));
        } else if (icone instanceof Internet) {
            jLabelTitle.setText(palavras.getString("Internet icon configuration"));
        }
        jLabelIconName.setText(palavras.getString("Configuration for the icon") + "#: " + icone.getId().getIdGlobal());
        getTabelaLink().setLink(icone);
        jScrollPane1.setViewportView(Tlink);
    }

    public void setIcone(ItemGrade icone, HashSet<String> usuarios, int escolha) {
        if (escolha == EscolherClasse.GRID) {
            if (!escalonadores.listarRemovidos().isEmpty()) {
                for (Object escal : escalonadores.listarRemovidos()) {
                    getTabelaMaquina().getEscalonadores().removeItem(escal);

                }
                escalonadores.listarRemovidos().clear();
            }
            if (!escalonadores.listarAdicionados().isEmpty()) {
                for (Object escal : escalonadores.listarAdicionados()) {
                    getTabelaMaquina().getEscalonadores().addItem(escal);
                }
                escalonadores.listarAdicionados().clear();
            }
            jLabelIconName.setText(palavras.getString("Configuration for the icon") + "#: " + icone.getId().getIdGlobal());
            if (icone instanceof Machine) {
                jLabelTitle.setText(palavras.getString("Machine icon configuration"));
                getTabelaMaquina().setMaquina((Machine) icone, usuarios);
                jScrollPane1.setViewportView(Tmachine);
            }
            if (icone instanceof Cluster) {
                jLabelTitle.setText(palavras.getString("Cluster icon configuration"));
                getTabelaCluster().setCluster((Cluster) icone, usuarios);
                jScrollPane1.setViewportView(Tcluster);
            }
        } else if (escolha == EscolherClasse.IAAS) {
            if (!escalonadoresCloud.listarRemovidos().isEmpty()) {
                for (Object escal : escalonadoresCloud.listarRemovidos()) {
                    getTabelaMaquinaIaaS().getEscalonadores().removeItem(escal);
                    getTabelaClusterIaaS().getEscalonadores().removeItem(escal);
                }
                escalonadoresCloud.listarRemovidos().clear();
            }
            if (!escalonadoresCloud.listarAdicionados().isEmpty()) {
                for (Object escal : escalonadoresCloud.listarAdicionados()) {
                    getTabelaMaquinaIaaS().getEscalonadores().addItem(escal);
                    getTabelaClusterIaaS().getEscalonadores().addItem(escal);
                }
                escalonadoresCloud.listarAdicionados().clear();
            }

            if (!alocadores.listarRemovidos().isEmpty()) {
                for (Object alloc : alocadores.listarRemovidos()) {
                    getTabelaMaquinaIaaS().getAlocadores().removeItem(alloc);
                    getTabelaClusterIaaS().getAlocadores().removeItem(alloc);
                }
                alocadores.listarRemovidos().clear();
            }
            if (!alocadores.listarAdicionados().isEmpty()){
                for (Object alloc : alocadores.listarAdicionados()){
                    getTabelaMaquinaIaaS().getAlocadores().addItem(alloc);
                    getTabelaClusterIaaS().getAlocadores().addItem(alloc);
                }
                alocadores.listarAdicionados().clear();
            }
            
            jLabelIconName.setText(palavras.getString("Configuration for the icon") + "#: " + icone.getId().getIdGlobal());
            if (icone instanceof Machine) {
                jLabelTitle.setText(palavras.getString("Machine icon configuration"));
                getTabelaMaquinaIaaS().setMaquina((Machine) icone, usuarios);
                jScrollPane1.setViewportView(TmachineIaaS);
            }
            if (icone instanceof Cluster) {
                jLabelTitle.setText(palavras.getString("Cluster icon configuration"));
                getTabelaClusterIaaS().setCluster((Cluster) icone, usuarios);
                jScrollPane1.setViewportView(TclusterIaaS);
            }
        }

    }

    public String getTitle() {
        return jLabelTitle.getText();
    }

    public MachineTable getTabelaMaquina() {
        return (MachineTable) Tmachine.getModel();
    }

    public MachineTableIaaS getTabelaMaquinaIaaS() {
        return (MachineTableIaaS) TmachineIaaS.getModel();
    }

    public ClusterTable getTabelaCluster() {
        return (ClusterTable) Tcluster.getModel();
    }

    public ClusterTableIaaS getTabelaClusterIaaS() {
        return (ClusterTableIaaS) TclusterIaaS.getModel();
    }

    public LinkTable getTabelaLink() {
        return (LinkTable) Tlink.getModel();
    }

    public void setPalavras(ResourceBundle palavras) {
        this.palavras = palavras;
        ((MachineTable) Tmachine.getModel()).setPalavras(palavras);
        ((MachineTableIaaS) TmachineIaaS.getModel()).setPalavras(palavras);
        ((ClusterTable) Tcluster.getModel()).setPalavras(palavras);
        ((ClusterTableIaaS) TclusterIaaS.getModel()).setPalavras(palavras);
        ((LinkTable) Tlink.getModel()).setPalavras(palavras);
    }
}
