/*
 * AguardaSimulacao.java
 *
 * Created on 12 de Julho de 2010, 15:56
 */

package ispd.Interface;
import javax.swing.*;
import java.awt.*;
import java.lang.*;
import ispd.DescreveSistema.*;
import ispd.Simulacao.*;
import ispd.RedesDeFilas.*;
import ispd.InterpretadorInterno.ModeloIconico.*;
import ispd.InterpretadorInterno.ModeloSimulavel.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.HashSet;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import java.io.*;
import java.awt.image.*;  
import java.net.*;  
import javax.imageio.*;  

/**
 *
 * @author  aldoig
 */
public class AguardaSimulacao extends javax.swing.JDialog implements Runnable {
    

    public AguardaSimulacao(Object objetos[]) {
		cargasConfiguradas	= (Boolean) objetos[0];
		cargasTipoConfiguracao 	= (Integer) objetos[1];
		cargasConfiguracao = (String) objetos[2];
 		icones            	= (HashSet<Icone>) objetos[3];
		porcentagem = 0.0;
		cancelado = false;
		
		addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent e ){
				cancelado = true;
				setVisible(false);
				JOptionPane.showMessageDialog(null,"Simulation Aborted","WARNING",JOptionPane.WARNING_MESSAGE);
			}
		} );
		
		Thread janela = new Thread(this);
		janela.setName("janela");
		janela.start();
		Thread simula = new Thread(this);  
		simula.setName("simula");
		simula.start();	
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jProgressBar1 = new javax.swing.JProgressBar();
        jProgressBar2 = new javax.swing.JProgressBar();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
		jButtonCancelar = new javax.swing.JButton();
		
		
        //setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Running Simulation\t\t");
        setResizable(false);
        setLocationRelativeTo(null);
		setModal(true);
		
		jButtonCancelar.setText("Cancelar");
        jButtonCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelarActionPerformed(evt);
            }
        });


        jProgressBar2.setStringPainted(true);
		jProgressBar2.setMaximum(100);
		jProgressBar1.setIndeterminate(true);

        jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));

        jTextArea1.setBackground(new java.awt.Color(238, 238, 238));
        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setRows(7);
        jTextArea1.setText("Simulation Initiated.\n");
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                    .addComponent(jProgressBar2, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
					.addComponent(jButtonCancelar, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
	public void run(){
		//System.out.println(Thread.currentThread().getName());  
		if (Thread.currentThread().getName().equals("janela")){
			this.initComponents();
			this.setVisible(true);
		}else{
			this.iniciarSimulacao();
		}
	}

	public void iniciarSimulacao(){
		try{
			Thread.currentThread().sleep(1000);
			boolean todosConfigurados = true;
			setPorcentagem(0);
			addMensagem("Verifying configuration of the icons. -> ");
			if(cancelado) return;
			if(!(icones.isEmpty())){
				int tamanho = icones.size();
				Double razao = 10.0/(double)tamanho;
				for(Icone I:icones){
					setPorcentagem(razao);
					if(I.getConfigurado()==false){
						todosConfigurados = false;
						if(cancelado) return;
						this.setVisible(false);
						JOptionPane.showMessageDialog(null,"One or more parameters have not been configured.","WARNING",JOptionPane.WARNING_MESSAGE);
						break;
					}
				}
				if(todosConfigurados){
					addMensagem("OK\nVerifying configuration of the tasks. -> ");
					if(cargasConfiguradas){//verifica se as cargas de trabalho est o confgiuradas
						addMensagem("OK\nWriting iconic model. -> ");
						setPorcentagem(5.0);
						//Chamar o m todo para escrever no arquivo
						if(cancelado) return;
						escreverArquivo(new File("modeloiconico"));
						if(operacaoArquivoOk){
							addMensagem("OK\nInterpreting iconic model. -> ");
							setPorcentagem(5.0);
							if(cancelado) return;
							try {
								File arquivo = new File("modeloiconico");
								InterpretadorIconico parser = new InterpretadorIconico();
								boolean retorno = parser.leArquivo(arquivo);
								if(!retorno){
									addMensagem("OK\nWriting simulation model. -> ");
									setPorcentagem(5.0);
									if(cancelado) return;
									parser.escreveArquivo();
									//arquivo.delete();
									addMensagem("OK\nInterpreting simulation model. -> ");
									setPorcentagem(5.0);
									if(cancelado) return;
									try {
										File arquivo2 = new File("modelosimulavel");
										InterpretadorSimulavel parser2 = new InterpretadorSimulavel();
										retorno = parser2.leArquivo(arquivo2);
										if(!retorno){
											addMensagem("OK\n");
											Thread.currentThread().sleep(1000);
											addMensagem("Mounting network queue. -> ");
											setPorcentagem(5.0);
											if(cancelado) return;
											try{
												//arquivo2.delete();
												String tarefas = parser2.getTarefas();
												Simulacao motor = new Simulacao(parser2.getRedeFilas(),tarefas,parser2.getNomeId());
												addMensagem("OK\nCreating tasks. -> ");
												setPorcentagem(5.0);
												if(cancelado) return;
												motor.criacaoInicialTarefas( tarefas,this );
												addMensagem("OK\nSimulating. -> ");
												//setPorcentagem(20.0);
												if(cancelado) return;											
												double tempo = motor.iniciaSimulacao(this);
												int tempoSimulacao = (int) tempo;
												int sec = (int) tempoSimulacao%60;
												int min = (int) ((tempoSimulacao-sec)/60)%60;
												int hora = (int) ((((tempoSimulacao-sec)/60)-min)/60);		
												//addMensagem("OK\n");
												//setPorcentagem(10.0);
												if(cancelado) return;
												this.setVisible(false);
												//JOptionPane.showMessageDialog(null,"Simulation ran succesfully\nTime of simualtion: "+tempo+"s"+".\n","Completed",JOptionPane.INFORMATION_MESSAGE);
												//JOptionPane.showMessageDialog(null,"Simulation ran succesfully.\nThe results are in Saida_Simulador.","WARNING",JOptionPane.WARNING_MESSAGE);											
											}catch(Exception e){
												if(cancelado) return;
												this.setVisible(false);
												JOptionPane.showMessageDialog(null,"An error ocurred while during the simulation!","WARNING",JOptionPane.WARNING_MESSAGE);
												e.printStackTrace();
											}
										}else{
											if(cancelado) return;
											this.setVisible(false);
											arquivo2.delete();
										}
									}
									catch(Exception e){
										if(cancelado) return;
										this.setVisible(false);
										JOptionPane.showMessageDialog(null,"Error opening file 'modelosimulavel'.","WARNING",JOptionPane.WARNING_MESSAGE);
										e.printStackTrace();
									}
								}else{
									if(cancelado) return;
									this.setVisible(false);
									arquivo.delete();
								}
							}
							catch(Exception e){
								if(cancelado) return;
								this.setVisible(false);
								JOptionPane.showMessageDialog(null,"Error opening file 'modeloiconico'.","WARNING",JOptionPane.WARNING_MESSAGE);
								e.printStackTrace();
							}
						}else{
							if(cancelado) return;
							this.setVisible(false);
							JOptionPane.showMessageDialog(null,"Error writing file!","WARNING",JOptionPane.WARNING_MESSAGE);
						}
					}else{
						if(cancelado) return;
						this.setVisible(false);
						JOptionPane.showMessageDialog(null,"One or more  workloads have not been configured.","WARNING",JOptionPane.WARNING_MESSAGE);
					}
				}
			}
			if(icones.isEmpty()){
				if(cancelado) return;
				this.setVisible(false);
				JOptionPane.showMessageDialog(null,"The model has no icons.","WARNING",JOptionPane.WARNING_MESSAGE);
			}
		}catch(InterruptedException ex){
			this.setVisible(false);
		}
	}
	
	public void escreverArquivo(File file){
		try{
			FileWriter writer = new FileWriter(file);
			PrintWriter saida = new PrintWriter(writer,true);
			
			for(Icone I:icones) if(I.getTipoIcone()==1){
				saida.printf("MAQ %s %f %f ",I.getNome(),I.getPoderComputacional(),I.getTaxaOcupacao());
				if(I.getMestre()){
					saida.print("MESTRE "+I.getAlgoritmo()+" LMAQ");
					List<Integer> lista = I.getEscravos();
					for(int temp:lista){
						for(Icone Ico:icones){
							if(Ico.getIdGlobal()==temp && Ico.getTipoIcone()!=2){
								saida.print(" "+Ico.getNome());
							}	
						}
					}
				}else{
					saida.print("ESCRAVO");
				}
				saida.println("");	
			}

			for(Icone I:icones) if(I.getTipoIcone()==3){
				saida.printf("CLUSTER %s %d %f %f %f %s\n",I.getNome(),I.getNumeroEscravos(),I.getPoderComputacional(),I.getBanda(),I.getLatencia(),I.getAlgoritmo());
			}

			for(Icone I:icones) if(I.getTipoIcone()==4){
				saida.printf("INET %s %f %f %f\n",I.getNome(),I.getBanda(),I.getLatencia(),I.getTaxaOcupacao());
			}

			for(Icone I:icones) if(I.getTipoIcone()==2){
				saida.printf("REDE %s %f %f %f CONECTA",I.getNome(),I.getBanda(),I.getLatencia(),I.getTaxaOcupacao());
				for(Icone Ico:icones){
					if(Ico.getIdGlobal()==I.getNoOrigem() && Ico.getTipoIcone()!=2){
						saida.print(" "+Ico.getNome());
					}	
				}
				for(Icone Ico:icones){
					if(Ico.getIdGlobal()==I.getNoDestino() && Ico.getTipoIcone()!=2){
						saida.print(" "+Ico.getNome());
					}	
				}
				saida.println("");
			}

			saida.print("CARGA");
			switch(cargasTipoConfiguracao){
				case 0: saida.println(" RANDOM\n"+cargasConfiguracao); break;
				case 1: saida.println(" MAQUINA\n"+cargasConfiguracao); break;
				case 2: saida.println(" TRACE\n"+cargasConfiguracao); break;
			}
			
			saida.close();   
			writer.close();
			operacaoArquivoOk = true;
			//JOptionPane.showMessageDialog(null,"File successfully saved\n"+file.getAbsolutePath(),"Completed",JOptionPane.INFORMATION_MESSAGE);
		}	
		// em caso de erro apresenta mensagem abaixo  
		catch(Exception e){
			operacaoArquivoOk = false;
			JOptionPane.showMessageDialog(null,e.getMessage(),"Warning",JOptionPane.WARNING_MESSAGE);  
		}  
	}
	
	public void addMensagem(String novoTexto){
		String antigoTexto;
		antigoTexto = jTextArea1.getText();
		novoTexto = antigoTexto + novoTexto;
		jTextArea1.setText(novoTexto);
	}

	public void setPorcentagem(double valor){
		porcentagem += valor;
		int value = (int) porcentagem;
		if (value > jProgressBar2.getMaximum()){
			value = jProgressBar2.getMaximum();
		}
		jProgressBar2.setValue(value);
	}

    private void jButtonCancelarActionPerformed(java.awt.event.ActionEvent evt) {                     
		cancelado = true;
		setVisible(false);
		JOptionPane.showMessageDialog(null,"Simulation Aborted","WARNING",JOptionPane.WARNING_MESSAGE);
    }
	
	public boolean getCancelado(){
		return cancelado;
	}
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JProgressBar jProgressBar2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
	private HashSet<Icone> icones;
	private Boolean cargasConfiguradas;
	private Integer cargasTipoConfiguracao;
	private String cargasConfiguracao;
	private boolean operacaoArquivoOk;
	private double porcentagem;
	private boolean cancelado;
	private javax.swing.JButton jButtonCancelar;
    // End of variables declaration//GEN-END:variables
    
}
