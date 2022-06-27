/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MostraSaida.java
 *
 * Created on 24/04/2010, 16:49:12
 */

package ispd.Interface;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList; 
import java.util.Vector;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.*;
import java.awt.image.*;  
import java.net.*;  
import javax.imageio.*;  
import java.io.PrintStream;
 
 
/**
 *
 * @author Aldo Ianelo Guerra
 */
public class CaixaTexto extends JDialog{

    /** Creates new form MostraSaida */
    public CaixaTexto(String titulo, String texto) {
        initComponents(titulo,texto);
    }

    /** Creates new form MostraSaida */
    public CaixaTexto(String titulo, JTable tabela) {
        initComponents(titulo,tabela);
    }

    private void initComponents(String titulo, String texto) {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButtonOK = new javax.swing.JButton();

        setTitle(titulo);
        setResizable(false);
        setLocationRelativeTo(null);
		setModal(true);

        jScrollPane1.setBorder(null);

        jTextArea1.setBackground(new java.awt.Color(240, 240, 240));
        jTextArea1.setColumns(20);
        jTextArea1.setEditable(true);
        jTextArea1.setFont(jTextArea1.getFont().deriveFont(jTextArea1.getFont().getStyle() | java.awt.Font.BOLD, jTextArea1.getFont().getSize()+1));
        jTextArea1.setRows(5);
        jTextArea1.setText(texto);
        jTextArea1.setBorder(null);
        jScrollPane1.setViewportView(jTextArea1);

        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonOK, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonOK)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void initComponents(String titulo, JTable tabela) {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = tabela;
        jButtonOK = new javax.swing.JButton();

        setTitle(titulo);
        setResizable(false);
        setLocationRelativeTo(null);
	setModal(true);

        jScrollPane1.setBorder(null);

        jScrollPane1.setViewportView(jTable1);

        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonOK, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonOK)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
		this.setVisible(false);
	}
	
	public String getTexto(){
		return jTextArea1.getText();
	}

	public String getDadosTabela(){
		String retorno = "";
		// Obtem o modelo da JTable  
		DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();  
		// Faz um looping em cima das linhas do modelo  
		for( int linha=0; linha < modelo.getRowCount(); linha++){  
			// Obtem o valor atual na coluna 
			String valor0 = (String) modelo.getValueAt(linha, 0);
			String valor1 = (String) modelo.getValueAt(linha, 1);
			String valor2 = (String) modelo.getValueAt(linha, 2);
			String valor3 = (String) modelo.getValueAt(linha, 3);
			String valor4 = (String) modelo.getValueAt(linha, 4);
			String valor5 = (String) modelo.getValueAt(linha, 5);
			
			valor1 = String.valueOf(Integer.parseInt(valor1));
			valor2 = String.valueOf(Double.parseDouble(valor2));
			valor3 = String.valueOf(Double.parseDouble(valor3));
			valor4 = String.valueOf(Double.parseDouble(valor4));
			valor5 = String.valueOf(Double.parseDouble(valor5));	
			retorno += valor0+" "+valor1+" "+valor2+" "+valor3+" "+valor4+" "+valor5+"\n";
		}
		return retorno;
	}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonOK;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private JTable jTable1;
    // End of variables declaration//GEN-END:variables

}
