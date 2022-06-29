package ispd.Interface;
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

public class AreaDesenho extends JPanel implements MouseListener, MouseMotionListener, ActionListener, Serializable {

	//Objetos principais da classe
	private int w,h;
	private HashSet<Icone> icones;
	private HashSet<String> listaNos;
	private int numArestas;
	private int numVertices;
	private int numIcones;
	//Objetos usados para controlas os popupmenus
	private JPopupMenu popupMenu;
	private JPopupMenu popupMenu2;
	private JMenuItem botaoRemove;
	private JMenuItem botaoCopiar;
	private JMenuItem botaoColar;
	private JMenuItem botaoInverter;
	private JSeparator jSeparator1;
	private JSeparator jSeparator2;
	//Objetos advindo da classe JanelaPrincipal
	private JTextArea notificaTextArea;
	private JTextArea jTextAreaBarraLateral;
	private boolean arquivoSalvo;
	//Objetos usados para desenhar a regua e as grades
	private int units;
	private boolean metric;
	private int INCH;
	private boolean gridOn;
	//Objetos para Selecionar texto na Area Lateral
	private boolean imprimeNosConectados;
	private boolean imprimeNosIndiretos;
	private boolean imprimeNosEscalonaveis;
	//Objetos usados para add um icone
	private int tipoIcone;
	private boolean botaoSelecaoIconeClicado;
	private boolean primeiroClique;
	private int posPrimeiroCliqueX;
	private int posPrimeiroCliqueY;
	private int posSegundoCliqueX;
	private int posSegundoCliqueY;
	private int verticeInicio;
	private int verticeFim;
	//Objeots usados para minipular os icones
	private Icone iconeAuxiliar;
	private int posicaoMouseX;
	private int posicaoMouseY;
	private Icone iconeAuxiliarMatchRede;
	private Icone iconeNulo;
	private boolean iconeSelecionado;
	//Objetos para Manipular as cargas
	private Boolean cargasConfiguradas;
	private Integer cargasTipoConfiguracao;
	private String cargasConfiguracao;
	//Objetos para remover um icone
	private Icone iconeAuxiliaRemover;
	//Obejtos para copiar um icone
	private Icone iconeCopiado;
	private boolean acaoColar;
	//Objetos usado para Validar escrita no arquivo.
	private boolean operacaoArquivoOk;


	public AreaDesenho(int w,int h){
	
		addMouseListener(this);
		addMouseMotionListener(this);

		this.w = w;
		this.h = h;
		this.numArestas = 0;
		this.numVertices = 0;
		this.numIcones = 0;
		icones = new HashSet<Icone>();
		listaNos = new HashSet<String>();
		metric=true;
		gridOn=false;
		INCH = Toolkit.getDefaultToolkit().getScreenResolution();
		tipoIcone = 0;
		botaoSelecaoIconeClicado = false;
		primeiroClique = false;
		cargasConfiguradas = new Boolean(false);
		cargasTipoConfiguracao = new Integer(-1);
		cargasConfiguracao = new String("");
		imprimeNosConectados = false;
		imprimeNosIndiretos = false;
		imprimeNosEscalonaveis = true;
		acaoColar = false;
		operacaoArquivoOk = false;
		iconeNulo = new Icone(-100,-100,-1,0,0);
		iconeAuxiliarMatchRede = iconeNulo;

	}

	public void setPaineis(JTextArea notificaTextArea,JTextArea jTextAreaBarraLateral,boolean arquivoSalvo){
		this.notificaTextArea = notificaTextArea;
		this.jTextAreaBarraLateral = jTextAreaBarraLateral;
		this.arquivoSalvo = arquivoSalvo;
		this.initPopupMenu();
	}

	public void initPopupMenu(){
		popupMenu = new JPopupMenu();
		popupMenu2 = new JPopupMenu();
		jSeparator1 = new JSeparator();
		jSeparator2 = new JSeparator();

		botaoCopiar = new JMenuItem();
		botaoCopiar.setText("Copy");
		botaoCopiar.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
			botaoCopiarActionPerformed(evt);
		}
		});
		//popupMenu.add(botaoCopiar);

		botaoInverter = new JMenuItem();
		botaoInverter.setText("Turn Over");
		//botaoInverter.setEnabled(false);
		botaoInverter.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
			botaoInverterActionPerformed(evt);
		}
		});
		//popupMenu.add(botaoInverter);
		
		popupMenu.add(jSeparator1);

		botaoRemove = new JMenuItem();
		botaoRemove.setText("Remove");
		botaoRemove.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				botaoRemoveActionPerformed(evt);
			}
		});
		//popupMenu.add(botaoRemove);	

		botaoColar = new JMenuItem();
		botaoColar.setText("Paste");
		botaoColar.setEnabled(false);
		botaoColar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				botaoColarActionPerformed(evt);
			}
		});
		popupMenu2.add(botaoColar);
		
	}

	private void montarBotoesPopupMenu(int tipo){
		popupMenu.remove(botaoCopiar);
		popupMenu.remove(botaoRemove);
		popupMenu.remove(jSeparator1);
		popupMenu.remove(botaoInverter);
		if(tipo==2){
			popupMenu.add(botaoInverter);
			popupMenu.add(jSeparator1);
			popupMenu.add(botaoRemove);
		}else{
			popupMenu.add(botaoCopiar);
			popupMenu.add(jSeparator1);
			popupMenu.add(botaoRemove);
		}
	}

	public Icone adicionaAresta(int x, int y, int posPrimeiroCliqueX, int posPrimeiroCliqueY, int tipoIcone){
		Icone I = new Icone(x,y,posPrimeiroCliqueX,posPrimeiroCliqueY,tipoIcone,numArestas,numIcones);
		numArestas++;
		numIcones++;
		icones.add(I);
		I.setEstaAtivo(true);
		I.setNoOrigem(verticeInicio);
		I.setNoDestino(verticeFim);
		I.setNome("icon"+I.getIdGlobal());
		listaNos.add(I.getNome());
		return I;
	}

	public Icone adicionaVertice(int x, int y, int tipoIcone){
		Icone I = new Icone(x,y,tipoIcone,numVertices,numIcones);
		numVertices++;
		numIcones++;
		icones.add(I);
		I.setEstaAtivo(true);
		switch(I.getTipoIcone()){
			case 1: this.setTextAreaNotifica("Machine icon added."); break;
			case 3: this.setTextAreaNotifica("Cluster icon added."); break;
			case 4: this.setTextAreaNotifica("Internet icon added."); break;
		}
		I.setNome("icon"+I.getIdGlobal());
		listaNos.add(I.getNome());
		return I;
	}

	public int getIconWidth(){
		return w;
	}

	public int getIconHeight(){
		return h;
	}

	public Dimension getMaximumSize(){
		return getPreferredSize();
	}

	public Dimension getMinimumSize(){
		return getPreferredSize();
	}

	public Dimension getPreferredSize(){
		return new Dimension(w,h);
	}
	
	public void setConectados(boolean imprimeNosConectados){
		this.imprimeNosConectados = imprimeNosConectados;
	}
	
	public void setIndiretos(boolean imprimeNosIndiretos){
		this.imprimeNosIndiretos = imprimeNosIndiretos;
	}
	
	public void setEscalonaveis(boolean imprimeNosEscalonaveis){
		this.imprimeNosEscalonaveis = imprimeNosEscalonaveis;
	}

	public void setIsMetric(boolean metric){
		this.metric = metric;
		repaint();
	}

	public void setGrid(boolean gridOn){
		this.gridOn = gridOn;
		repaint();
	}
	
	protected void paintComponent(Graphics g){

		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setColor(new Color(255,255,255));
		g2d.fillRect(0,0,w,h);
		
		g2d.setColor(new Color(220,220,220));

		if(metric){
			units = (int)((double)INCH / (double)2.54);
		}else {
			units = (int) INCH / 2;
		}

		if(gridOn){
			for(int _w=0;_w<=w;_w+=units)
				g2d.drawLine(_w,0,_w,h);
			for(int _h=0;_h<=h;_h+=units)
				g2d.drawLine(0,_h,w,_h);
		}
		
	
		//Desenha a linha da conexão de rede antes dela se estabelcer.	
		if(botaoSelecaoIconeClicado && primeiroClique){
			g2d.setColor(new Color(0,0,0));
			g2d.drawLine(posPrimeiroCliqueX,posPrimeiroCliqueY,posicaoMouseX,posicaoMouseY);
		}
		
		// Desenhamos todos os icones
		for(Icone I:icones)
			if(I.getTipoIcone()==2)
				I.draw(g2d);
		for(Icone I:icones)
			if(I.getTipoIcone()!=2)
				I.draw(g2d);

	}

	public void mouseClicked(MouseEvent e){ 
		
		if(botaoSelecaoIconeClicado){
			Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
			for(Icone I:icones) I.setEstaAtivo(false);
			posicaoMouseX = e.getX();
			posicaoMouseY = e.getY();
			if(tipoIcone==2){
				if(!primeiroClique){
					boolean achouIcone = false;
					for(Icone I:icones){
						boolean clicado = I.getRectEnvolvente(e.getX(),e.getY());
						if (clicado){
						posPrimeiroCliqueX = I.getNumX();
						posPrimeiroCliqueY = I.getNumY();
						verticeInicio = I.getIdGlobal();
						achouIcone = true;
						break;
						}
					}
					if(achouIcone){
						primeiroClique = true;	
					}else{
						JOptionPane.showMessageDialog(null,"You must click an icon.","WARNING",JOptionPane.WARNING_MESSAGE);
						setCursor(normalCursor);
						botaoSelecaoIconeClicado = false;
					}
				}else{
					boolean achouIcone = false;
					primeiroClique = false;
					for(Icone I:icones){
						boolean clicado = I.getRectEnvolvente(e.getX(),e.getY());
						if (clicado && I.getTipoIcone()!=2){
						posSegundoCliqueX = I.getNumX();
						posSegundoCliqueY = I.getNumY();
						verticeFim = I.getIdGlobal();
						if(verticeInicio!=verticeFim) 
							achouIcone = true;
						break;
						}
					}
					if(achouIcone){
						Icone I = adicionaAresta(posSegundoCliqueX, posSegundoCliqueY, posPrimeiroCliqueX, posPrimeiroCliqueY, tipoIcone);
						this.setTextAreaNotifica("Network connection added.");
						this.arquivoSalvo = false;
						this.setLabelAtributos(I);
						setCursor(normalCursor);
						botaoSelecaoIconeClicado = false;
						//fors para adicionar numero do destino na origem e vice versa
						for(Icone Ico:icones) 
							if(Ico.getIdGlobal()==verticeInicio&&Ico.getTipoIcone()!=2){ 
								Ico.addIdConexaoSaida(verticeFim); 
								break;
							}
						for(Icone Ico:icones) 
							if(Ico.getIdGlobal()==verticeFim&&Ico.getTipoIcone()!=2){ 
								Ico.addIdConexaoEntrada(verticeInicio); 
								break;
							}
						atualizaNosIndiretos();
					}else{
						JOptionPane.showMessageDialog(null,"You must click an icon.","WARNING",JOptionPane.WARNING_MESSAGE);
						setCursor(normalCursor);
						botaoSelecaoIconeClicado = false;
					}
				}
			}else{
				Icone I = adicionaVertice(posicaoMouseX,posicaoMouseY,tipoIcone);
				this.arquivoSalvo = false;
				this.setLabelAtributos(I);
				setCursor(normalCursor);
				botaoSelecaoIconeClicado = false;
			}
		
		}else{
			this.jTextAreaBarraLateral.setText("No icon selected");
			iconeAuxiliarMatchRede = iconeNulo;
			for(Icone I:icones) I.setEstaAtivo(false);
			for(Icone I:icones){
				boolean clicado = I.getRectEnvolvente(e.getX(),e.getY());
				if (clicado){
					I.setEstaAtivo(true);
					this.setLabelAtributos(I);
					switch(e.getButton()){
						case MouseEvent.BUTTON1: if(e.getClickCount()==2){
										setAtributos(I);
									 }else{
										iconeAuxiliarMatchRede = I;
									 } break;
						case MouseEvent.BUTTON2: break;
						case MouseEvent.BUTTON3: iconeAuxiliaRemover=I;
									 this.montarBotoesPopupMenu(I.getTipoIcone());
									 popupMenu.show(e.getComponent(),e.getX(),e.getY());
									 break;
					}
					break;
				}else{
					switch(e.getButton()){
						case MouseEvent.BUTTON1: break;
						case MouseEvent.BUTTON2: break;
						case MouseEvent.BUTTON3: posicaoMouseX = e.getX(); 
									 posicaoMouseY = e.getY();
									 popupMenu2.show(e.getComponent(),e.getX(),e.getY());
									 break;
					}
				}	
			}
			
		}
		repaint();	
	}

	public void mouseEntered(MouseEvent e) {
		repaint();
	}

	public void mouseExited(MouseEvent e) {
		repaint();
	}

	public void mouseReleased(MouseEvent e) {
		repaint();
	}

	public void mouseMoved(MouseEvent e) {
		posicaoMouseX = e.getX();
		posicaoMouseY = e.getY();
		if(botaoSelecaoIconeClicado){
			Cursor hourglassCursor = new Cursor(Cursor.CROSSHAIR_CURSOR); 
			setCursor(hourglassCursor);
		}
		repaint();
	}

	public void mousePressed(MouseEvent e) {
		for(Icone I:icones) 
			I.setEstaAtivo(false);
		for(Icone I:icones){
			boolean clicado = I.getRectEnvolvente(e.getX(),e.getY());
			if (clicado && I.getTipoIcone()!=2){
					iconeAuxiliar = I;
					iconeSelecionado = true;
					break;	
			}else{
				iconeSelecionado = false;
			}
		}
		repaint();
	}

	public void mouseDragged(MouseEvent e){
		if(iconeSelecionado){
			if (iconeAuxiliar.getTipoIcone()!=2){
				iconeAuxiliar.setEstaAtivo(true);
				if(iconeAuxiliar.getIdGlobal()!=-1)
					this.setLabelAtributos(iconeAuxiliar);
				else 
					this.jTextAreaBarraLateral.setText("No icon selected.");
				posicaoMouseX = e.getX();
				posicaoMouseY = e.getY();
				for(Icone I:icones)
					if(I.getTipoIcone()==2 && I.getNumX()==iconeAuxiliar.getNumX() && I.getNumY()==iconeAuxiliar.getNumY() && (I.getNoOrigem()==iconeAuxiliar.getIdGlobal() || I.getNoDestino()==iconeAuxiliar.getIdGlobal() ))
						I.setPosition(posicaoMouseX,posicaoMouseY);
				for(Icone I:icones)
					if(I.getTipoIcone()==2 && I.getNumPreX()==iconeAuxiliar.getNumX() && I.getNumPreY()==iconeAuxiliar.getNumY() && (I.getNoOrigem()==iconeAuxiliar.getIdGlobal() || I.getNoDestino()==iconeAuxiliar.getIdGlobal() ) )
						I.setPrePosition(posicaoMouseX,posicaoMouseY);
				iconeAuxiliar.setPosition(posicaoMouseX,posicaoMouseY);
			}
		}
		repaint();
	}

	public void actionPerformed(ActionEvent e){
		for(Icone I:icones) I.move();
		atualizaNosIndiretos();
		repaint();
	}
	
	public void iniciarSimulacao(){

		Object objetos[] = new Object[4];
		objetos[0] = cargasConfiguradas;
		objetos[1] = cargasTipoConfiguracao;
		objetos[2] = cargasConfiguracao;
		objetos[3] = icones;
		AguardaSimulacao janela = new AguardaSimulacao(objetos);
		/*boolean todosConfigurados = true;

		if(!(icones.isEmpty())){
			for(Icone I:icones){
				if(I.getConfigurado()==false){
					todosConfigurados = false;
					JOptionPane.showMessageDialog(null,"One or more parameters have not been configured.","WARNING",JOptionPane.WARNING_MESSAGE);
					break;
				}
			}
			if(todosConfigurados){
				if(cargasConfiguradas){//verifica se as cargas de trabalho estão confgiuradas
					//Chamar o método para escrever no arquivo
					escreverArquivo(new File("modeloiconico"));
					if(operacaoArquivoOk){
						try {
							File arquivo = new File("modeloiconico");
							InterpretadorIconico parser = new InterpretadorIconico();
							boolean retorno = parser.leArquivo(arquivo);
							if(!retorno){
								parser.escreveArquivo();
								//arquivo.delete();
								try {
									File arquivo2 = new File("modelosimulavel");
									InterpretadorSimulavel parser2 = new InterpretadorSimulavel();
									retorno = parser2.leArquivo(arquivo2);
									if(!retorno){
										try{
											parser2.escreveArquivo();
											//arquivo2.delete();
											Simulacao motor = new Simulacao(parser2.getRedeFilas(),parser2.getTarefas());
											parser2.getNomeId();
											motor.iniciaSimulacao();
											//JOptionPane.showMessageDialog(null,"Simulation ran succesfully.\nThe results are in Saida_Simulador.","WARNING",JOptionPane.WARNING_MESSAGE);
										}catch(Exception e){
											JOptionPane.showMessageDialog(null,"An error ocurred while during the simulation!","WARNING",JOptionPane.WARNING_MESSAGE);
											e.printStackTrace();
										}
									}else{
										arquivo2.delete();
									}
								}
								catch(Exception e){
									JOptionPane.showMessageDialog(null,"Error opening file 'modelosimulavel'.","WARNING",JOptionPane.WARNING_MESSAGE);
									e.printStackTrace();
								}
							}else{
								arquivo.delete();
							}
						}
						catch(Exception e){
							JOptionPane.showMessageDialog(null,"Error opening file 'modeloiconico'.","WARNING",JOptionPane.WARNING_MESSAGE);
							e.printStackTrace();
						}
					}else{
						JOptionPane.showMessageDialog(null,"Error writing file!","WARNING",JOptionPane.WARNING_MESSAGE);
					}
				}else{
					JOptionPane.showMessageDialog(null,"One or more  workloads have not been configured.","WARNING",JOptionPane.WARNING_MESSAGE);
				}
			}
		}
		if(icones.isEmpty()){
			JOptionPane.showMessageDialog(null,"The model has no icons.","WARNING",JOptionPane.WARNING_MESSAGE);
		}*/
	}

	public void setIconeSelecionado(int tipoIcone){
		this.tipoIcone = tipoIcone;
		this.botaoSelecaoIconeClicado = true;
		if(tipoIcone==2) 
			this.primeiroClique = false;
	}
	
	public void removeIcone(Icone I){
		icones.remove(I);
	}

	public void atualizaNosIndiretos(){
		//Remover nodes Indiretos
		for(Icone I:icones){
			I.clearNosIndiretosEntrada();
			I.clearNosIndiretosSaida();
		}
		
		//Inserir Nodes Indiretos
		int numIcoInternet = 0;
		for(Icone I:icones){
			if(I.getTipoIcone()==4){
				numIcoInternet++;
			}
		}
		for(int i=0;i<numIcoInternet;i++){
			for(Icone I1:icones){
				if(I1.getTipoIcone()==4){
					HashSet<Integer> listaOrigem = I1.getObjetoConexaoEntrada();
					HashSet<Integer> listaDestino = I1.getObjetoConexaoSaida();
					for(int temp1:listaDestino){
						for(Icone I2:icones){
							if(I2.getIdGlobal()==temp1){
								HashSet<Integer> listaIndiretosEntrada = I2.getObjetoNosIndiretosEntrada();
								for(Integer temp2:listaOrigem){
									if(!listaIndiretosEntrada.contains(temp2) && I2.getIdGlobal()!=temp2){
										listaIndiretosEntrada.add(temp2);
									}
								}
								I2.setObjetoNosIndiretosEntrada(listaIndiretosEntrada);
							}
						}
					}
					listaOrigem = I1.getObjetoConexaoEntrada();
					listaDestino = I1.getObjetoConexaoSaida();
					for(int temp1:listaOrigem){
						for(Icone I2:icones){
							if(I2.getIdGlobal()==temp1){
								HashSet<Integer> listaIndiretosSaida= I2.getObjetoNosIndiretosSaida();
								for(Integer temp2:listaDestino){
									if(!listaIndiretosSaida.contains(temp2) && I2.getIdGlobal()!=temp2){
										listaIndiretosSaida.add(temp2);
									}
								}
								I2.setObjetoNosIndiretosSaida(listaIndiretosSaida);
							}
						}
					}
					listaOrigem = I1.getObjetoNosIndiretosEntrada();
					listaDestino = I1.getObjetoConexaoSaida();
					for(int temp1:listaOrigem){
						for(Icone I2:icones){
							if(I2.getIdGlobal()==temp1){
								HashSet<Integer> listaIndiretosSaida= I2.getObjetoNosIndiretosSaida();
								for(Integer temp2:listaDestino){
									if(!listaIndiretosSaida.contains(temp2) && I2.getIdGlobal()!=temp2){
										listaIndiretosSaida.add(temp2);
									}
								}
								I2.setObjetoNosIndiretosSaida(listaIndiretosSaida);
							}
						}
					}
					listaOrigem = I1.getObjetoConexaoEntrada();
					listaDestino = I1.getObjetoNosIndiretosSaida();
					for(int temp1:listaDestino){
						for(Icone I2:icones){
							if(I2.getIdGlobal()==temp1){
								HashSet<Integer> listaIndiretosEntrada = I2.getObjetoNosIndiretosEntrada();
								for(Integer temp2:listaOrigem){
									if(!listaIndiretosEntrada.contains(temp2) && I2.getIdGlobal()!=temp2){
										listaIndiretosEntrada.add(temp2);
									}
								}
								I2.setObjetoNosIndiretosEntrada(listaIndiretosEntrada);
							}
						}
					}

					/*HashSet<Integer> listaIndiretosEntrada = I1.getObjetoNosIndiretosEntrada();
					for(int temp1:listaIndiretosEntrada){
						for(Icone I2:icones){
							if(I2.getIdGlobal()==temp1){
								HashSet<Integer> listaDestino = I2.getObjetoNosIndiretos();
								HashSet<Integer> listaOrigem2 = I1.getObjetoNosIndiretos();
								for(int temp2:listaOrigem2){
									if(!listaDestino.contains(temp2) && temp2!=I2.getID()){
										listaDestino.add(temp2);
									}
								}
								I2.setObjetoNosIndiretos(listaDestino);
							}
						}	
					}*/
				}
			}
		}
		
		//Atualiza nos escalonaveis
		//Remover nos Escalonaveis
		for(Icone I:icones){
			I.clearNosEscalonaveis();
		}
		
		//adiciona nos escalonaveis
		for(Icone I:icones){
			if(I.getTipoIcone()!=2 && I.getTipoIcone()!=4){
				HashSet<Integer> listaOrigem1 = I.getObjetoConexaoSaida();
				HashSet<Integer> listaOrigem2 = I.getObjetoNosIndiretosSaida();
				HashSet<Integer> listaDestino = I.getObjetoNosEscalonaveis();
				//listaDestino.add(I.getIdGlobal());
				for(int temp1:listaOrigem1){
					for(Icone I2:icones){
						if(I2.getTipoIcone()!=2 && I2.getTipoIcone()!=4 && temp1==I2.getIdGlobal()){
							listaDestino.add(temp1);
						}
					}
				}
				for(int temp1:listaOrigem2){
					for(Icone I2:icones){
						if(I2.getTipoIcone()!=2 && I2.getTipoIcone()!=4 && temp1==I2.getIdGlobal()){
							listaDestino.add(temp1);
						}
					}
				}
				I.setObjetoNosEscalonaveis(listaDestino);
			}
		}
	}

	private void botaoRemoveActionPerformed(java.awt.event.ActionEvent evt) {
		acaoRemove();
	}

	private void acaoRemove(){
		int opcao = JOptionPane.showConfirmDialog(null,"Remove this icon?","Remove",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
		if(opcao == JOptionPane.YES_OPTION){
			if(iconeAuxiliaRemover.getTipoIcone()==2){
				int j=0;
				for(Icone I1:icones){ 
					if(I1.getIdGlobal()==iconeAuxiliaRemover.getNoOrigem() && I1.getTipoIcone()!=2){ 
						for(Icone I2:icones){ 
							if(I2.getIdGlobal()==iconeAuxiliaRemover.getNoDestino() && I2.getTipoIcone()!=2){ 
								I1.removeConexaoSaida(I2.getIdGlobal()); 
								I2.removeConexaoEntrada(I1.getIdGlobal()); 
								break;
							}
						}
						break;
					}
				}
				listaNos.remove(iconeAuxiliaRemover.getNome());
				removeIcone(iconeAuxiliaRemover);
				this.arquivoSalvo = false;
				atualizaNosIndiretos();
			}else{
				int cont=0;
				//Remover dados das conexoes q entram
				HashSet<Integer> listanos = iconeAuxiliaRemover.getObjetoConexaoEntrada();
				for(int i:listanos){
					for(Icone I:icones){ 
						if(i==I.getIdGlobal() && I.getTipoIcone()!=2){
							I.removeConexaoSaida(iconeAuxiliaRemover.getIdGlobal()); 
							break;
						}
					}
				}
				//Remover dados das conexoes q saem
				listanos = iconeAuxiliaRemover.getObjetoConexaoSaida();
				for(int i:listanos){
					for(Icone I:icones){ 
						if(i==I.getIdGlobal() && I.getTipoIcone()!=2){
							I.removeConexaoEntrada(iconeAuxiliaRemover.getIdGlobal()); 
							break;
						}
					}
				}
				for(Icone I:icones)
					if(I.getTipoIcone()==2 && ((I.getNumX()==iconeAuxiliaRemover.getNumX() && I.getNumY()==iconeAuxiliaRemover.getNumY()) || (I.getNumPreX() == iconeAuxiliaRemover.getNumX() && I.getNumPreY()==iconeAuxiliaRemover.getNumY())) )
						cont++;
				for(int j=0;j<cont;j++){
					for(Icone I:icones) 
						if(I.getTipoIcone()==2 && ((I.getNumX()==iconeAuxiliaRemover.getNumX() && I.getNumY()==iconeAuxiliaRemover.getNumY()) || (I.getNumPreX()==iconeAuxiliaRemover.getNumX() && I.getNumPreY()==iconeAuxiliaRemover.getNumY()))){
							listaNos.remove(I.getNome()); 
							removeIcone(I); 
							break;
						}
				}
				listaNos.remove(iconeAuxiliaRemover.getNome());
				removeIcone(iconeAuxiliaRemover);
				this.arquivoSalvo = false;
				atualizaNosIndiretos();
			}
			repaint();
		}
	}

	public void deletarIcone(){
		boolean iconeEncontrado = false;
		for(Icone I:icones){
			if(I.getEstaAtivo()==true){
				iconeEncontrado = true;
				iconeAuxiliaRemover = I;
				acaoRemove();
				break;
			}
		}
		if(!iconeEncontrado){
			JOptionPane.showMessageDialog(null,"No icon selected.","WARNING",JOptionPane.WARNING_MESSAGE);
		}
	}

	private void botaoCopiarActionPerformed(java.awt.event.ActionEvent evt) {
		if(iconeAuxiliaRemover.getTipoIcone()!=2){
			iconeCopiado = iconeAuxiliaRemover;
			acaoColar = true;
			botaoColar.setEnabled(true);
		}else{
			//copiar conexão de rede
		}
	}
	
	public void acaoCopiarIcone(){
		boolean iconeEncontrado = false;
		for(Icone I:icones){
			if(I.getEstaAtivo()==true){
				iconeEncontrado = true;
				iconeCopiado = I;
				acaoColar = true;
				botaoColar.setEnabled(true);
				break;
			}
		}
		if(!iconeEncontrado){
			JOptionPane.showMessageDialog(null,"No icon selected.","WARNING",JOptionPane.WARNING_MESSAGE);
		}
	}

	private void botaoColarActionPerformed(java.awt.event.ActionEvent evt) {
		acaoColarIcone();
	}

	public void acaoColarIcone(){
		for(Icone i:icones){
			i.setEstaAtivo(false);
		}
		if(acaoColar==true && iconeCopiado.getTipoIcone()!=2){
			Icone I = adicionaVertice(posicaoMouseX,posicaoMouseY,iconeCopiado.getTipoIcone());
			I.setNome("icon"+I.getIdGlobal());
			listaNos.add(I.getNome());
			I.setPoderComputacional(iconeCopiado.getPoderComputacional());
			I.setTaxaOcupacao(iconeCopiado.getTaxaOcupacao());
			I.setLatencia(iconeCopiado.getLatencia());
			I.setBanda(iconeCopiado.getBanda());
			I.setAlgoritmo(iconeCopiado.getAlgoritmo());
			I.setNumeroEscravos(iconeCopiado.getNumeroEscravos());
			this.arquivoSalvo = false;
		}else{
			//colar conexão de rede
		}
		repaint();
	}

	private void botaoInverterActionPerformed(java.awt.event.ActionEvent evt) {
		if(iconeAuxiliaRemover.getTipoIcone()==2){
			iconeAuxiliaRemover.setEstaAtivo(false);
			Icone I = adicionaAresta( iconeAuxiliaRemover.getNumPreX(), iconeAuxiliaRemover.getNumPreY(), iconeAuxiliaRemover.getNumX(), iconeAuxiliaRemover.getNumY(), iconeAuxiliaRemover.getTipoIcone());
			I.setNoOrigem(iconeAuxiliaRemover.getNoDestino());
			I.setNoDestino(iconeAuxiliaRemover.getNoOrigem());
			I.setPoderComputacional(iconeAuxiliaRemover.getPoderComputacional());
			I.setTaxaOcupacao(iconeAuxiliaRemover.getTaxaOcupacao());
			I.setLatencia(iconeAuxiliaRemover.getLatencia());
			I.setBanda(iconeAuxiliaRemover.getBanda());
			I.setAlgoritmo(iconeAuxiliaRemover.getAlgoritmo());
			I.setNumeroEscravos(iconeAuxiliaRemover.getNumeroEscravos());
			this.arquivoSalvo = false;
			//fors para adicionar numero do destino na origem e vice versa
			for(Icone Ico:icones) 
				if(Ico.getIdGlobal()==iconeAuxiliaRemover.getNoDestino() && Ico.getTipoIcone()!=2){ 
					Ico.addIdConexaoSaida(iconeAuxiliaRemover.getNoOrigem()); 
					break;
				}
			for(Icone Ico:icones) 
				if(Ico.getIdGlobal()==iconeAuxiliaRemover.getNoOrigem() && Ico.getTipoIcone()!=2){ 
					Ico.addIdConexaoEntrada(iconeAuxiliaRemover.getNoDestino()); 
					break;
				}
			this.setTextAreaNotifica("Network connection added.");
			this.arquivoSalvo = false;
			this.setLabelAtributos(I);
			atualizaNosIndiretos();
		}
	}

	private void setAtributos(Icone I){
		ConfiguraMaquina configMaquina;
		ConfiguraRede configuraRede;
		ConfiguraCluster configuraCluster;
		ConfiguraInternet configuraInternet;
		
		this.arquivoSalvo = false;
		atualizaNosIndiretos();

		switch(I.getTipoIcone()){
			case 1:{configMaquina = new ConfiguraMaquina(I,listaNos);
				configMaquina.setVisible(true);
				}break;
			case 2:{configuraRede = new ConfiguraRede(I,listaNos);
				configuraRede.setVisible(true);
				}break;
			case 3:{configuraCluster = new ConfiguraCluster(I,listaNos);
				configuraCluster.setVisible(true);	
				}break;
			case 4:{configuraInternet = new ConfiguraInternet(I,listaNos);
				configuraInternet.setVisible(true);
				}break;
		}
		repaint();
	}

	public void escreverArquivo(File file){
		this.arquivoSalvo = false;
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

	public void setTextAreaNotifica(String novoTexto){
		String antigoTexto;
		antigoTexto = notificaTextArea.getText();
		novoTexto = antigoTexto + "\n" + novoTexto;
		notificaTextArea.setText(novoTexto);
	}

	public void setLabelAtributos(Icone I){
		String Texto = "";
		HashSet<Integer> listaEntrada = I.getObjetoConexaoEntrada();
		HashSet<Integer> listaSaida = I.getObjetoConexaoSaida();
		switch(I.getTipoIcone()){
			case 1: {Texto = "Local ID: "+ String.valueOf(I.getIdLocal())+
				 "\nGlobal ID: "+ String.valueOf(I.getIdGlobal())+
				 "\nLabel: "+ I.getNome()+
				 "\nX-coordinate: "+ String.valueOf(I.getNumX())+
				 "\nY-coordinate: "+ String.valueOf(I.getNumY())+
				 "\nComputational power: "+ String.valueOf(I.getPoderComputacional())+
				 "\nLoad Factor: "+ String.valueOf(I.getTaxaOcupacao());
				 if(I.getMestre()){ Texto=Texto+
					"\nMASTER"+
					"\nScheduling algorithm: "+I.getAlgoritmo();
				 }else{Texto=Texto+
					"\nSLAVE";
				 }
			}break;
			case 2: {Texto = "Local ID: "+ String.valueOf(I.getIdLocal())+
				 "\nGlobal ID: "+ String.valueOf(I.getIdGlobal())+
				 "\nLabel: "+I.getNome()+
				 "\nX1-coordinate: "+String.valueOf(I.getNumX())+
				 "\nY1-coordinate: "+String.valueOf(I.getNumY())+
				 "\nX2-coordinate: "+String.valueOf(I.getNumPreX())+
				 "\nY2-coordinate: "+String.valueOf(I.getNumPreY())+
				 "\nBandwidth: "+String.valueOf(I.getBanda())+
				 "\nLatency: "+String.valueOf(I.getLatencia())+
				 "\nLoad Factor: "+String.valueOf(I.getTaxaOcupacao());
			}break;
			case 3: {Texto = "Local ID: "+ String.valueOf(I.getIdLocal())+
				 "\nGlobal ID: "+ String.valueOf(I.getIdGlobal())+
				 "\nLabel: "+I.getNome()+
				 "\nX-coordinate: "+String.valueOf(I.getNumX())+
				 "\nY-coordinate: "+String.valueOf(I.getNumY())+
				 "\nNumber of slaves: "+String.valueOf(I.getNumeroEscravos())+
				 "\nComputing power: "+String.valueOf(I.getPoderComputacional())+
				 "\nBandwidth: "+String.valueOf(I.getBanda())+
				 "\nLatency: "+String.valueOf(I.getLatencia())+
				 "\nScheduling algorithm: "+I.getAlgoritmo();
			}break;
			case 4: {Texto = "Local ID: "+ String.valueOf(I.getIdLocal())+
				 "\nGlobal ID: "+ String.valueOf(I.getIdGlobal())+
				 "\nLabel: "+I.getNome()+
				 "\nX-coordinate: "+String.valueOf(I.getNumX())+
				 "\nY-coordinate: "+String.valueOf(I.getNumY())+
				 "\nBandwidth: "+String.valueOf(I.getBanda())+
				 "\nLatency: "+String.valueOf(I.getLatencia())+
				 "\nLoad Factor: "+String.valueOf(I.getTaxaOcupacao());
			}break;
		}
		if(imprimeNosConectados && I.getTipoIcone()!=2){
			Texto = Texto + "\nOutput Connection:";
			for(int i:listaSaida)
				 Texto = Texto + "\n" + String.valueOf(i);
			Texto = Texto + "\nInput Connection:";
			for(int i:listaEntrada)
				 Texto = Texto + "\n" + String.valueOf(i);
		}
		if(imprimeNosConectados && I.getTipoIcone()==2){
			Texto = Texto + "\nSource Node: " + String.valueOf(I.getNoOrigem());
			Texto = Texto + "\nDestination Node: " + String.valueOf(I.getNoDestino());
		}
		if(imprimeNosIndiretos && I.getTipoIcone()!=2){
			listaEntrada = I.getObjetoNosIndiretosEntrada();
			listaSaida = I.getObjetoNosIndiretosSaida();
			Texto = Texto + "\nOutput Nodes Indirectly Connected:";
			for(int i:listaSaida)
				 Texto = Texto + "\n" + String.valueOf(i);
			Texto = Texto + "\nInput Nodes Indirectly Connected:";
			for(int i:listaEntrada)
				 Texto = Texto + "\n" + String.valueOf(i);
		}
		if(imprimeNosEscalonaveis && I.getTipoIcone()!=2){
			listaSaida = I.getObjetoNosEscalonaveis();
			Texto = Texto + "\nSchedulable Nodes:";
			for(int i:listaSaida)
				 Texto = Texto + "\n" + String.valueOf(i);
		}
		if(I.getTipoIcone()==1 && I.getMestre()){
			List<Integer> escravos= I.getEscravos();
			Texto = Texto + "\nSlave Nodes:";
			for(int i:escravos) Texto = Texto + "\n"+ String.valueOf(i);
		}
		this.jTextAreaBarraLateral.setText(Texto);
	}

	public DescreveSistema getDadosASalvar(){
		DescreveSistema descricao = new DescreveSistema();
		for(Icone I:icones){ 
			descricao.addIconeLista( I.getTipoIcone(), I.getIdLocal(),I.getIdGlobal(), I.getNome(), I.getNumX(), I.getNumY(), I.getNumPreX(), I.getNumPreY(), I.getPoderComputacional(), I.getTaxaOcupacao(), I.getLatencia(), I.getBanda(),I.getMestre(), I.getAlgoritmo(), I.getEscravos(), I.getObjetoConexaoEntrada(), I.getObjetoConexaoSaida(), I.getNoOrigem(), I.getNoDestino(), I.getNumeroEscravos() );
		}
		descricao.addListaNosLista(listaNos);
		descricao.addIDs(numIcones,numVertices,numArestas);
		descricao.setCargas(cargasConfiguradas,cargasTipoConfiguracao,cargasConfiguracao);
		return descricao;
	}
	
	public void setDadosSalvos(DescreveSistema descricao){
		List<DescreveIcone> lista = new ArrayList<DescreveIcone>();

		this.listaNos = descricao.getListaNosLista();
		this.numIcones = descricao.getNumIcones();
		this.numVertices = descricao.getNumVertices();
		this.numArestas = descricao.getNumArestas();
		this.cargasConfiguradas = descricao.getCargasConfiguradas();
		this.cargasTipoConfiguracao = descricao.getCargasTipoConfiguracao();
		this.cargasConfiguracao = descricao.getCargasConfiguracao();
		
		lista = descricao.getIconeLista();

		for(DescreveIcone Ico:lista){
			Icone I = new Icone(Ico.getX(),Ico.getY(),Ico.getPreX(),Ico.getPreY(),Ico.getTipoIcone(),Ico.getIdLocal(),Ico.getIdGlobal());
			icones.add(I);
			I.setNome(Ico.getNome());
			I.setPoderComputacional(Ico.getPoderComputacional());
			I.setTaxaOcupacao(Ico.getTaxaOcupacao());
			I.setLatencia(Ico.getLatencia());
			I.setBanda(Ico.getBanda());
			I.setMestre(Ico.getMestre());
			I.setAlgoritmo(Ico.getAlgoritmoEscalonamento());
			I.setEscravos(Ico.getEscravos());
			I.setConexaoEntrada(Ico.getConexaoEntrada());
			I.setConexaoSaida(Ico.getConexaoSaida());
			I.setNoDestino(Ico.getNoDestino());
			I.setNoOrigem(Ico.getNoOrigem());
			I.setNumeroEscravos(Ico.getNumeroEscravos());
		}
		
		atualizaNosIndiretos();
		repaint();
	}

	public BufferedImage createImage() {  
		int maiorx = 0;
		int maiory = 0;
	
		for(Icone I:icones){
			if(I.getNumX()>maiorx) maiorx=I.getNumX();
			if(I.getNumY()>maiory) maiory=I.getNumY();
		}
	
		BufferedImage image = new BufferedImage(maiorx+50, maiory+50, BufferedImage.TYPE_INT_RGB);  
	
		Graphics2D gc = (Graphics2D) image.getGraphics();
	
		gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		gc.setColor(new Color(255,255,255));
		gc.fillRect(0,0,maiorx+50,maiory+50);
		
		gc.setColor(new Color(220,220,220));
		if(metric){units = (int)((double)INCH / (double)2.54);} else {units = (int) INCH / 2;}
		if(gridOn){
		for(int _w=0;_w<=maiorx+50;_w+=units) gc.drawLine(_w,0,_w,maiory+50);
		for(int _h=0;_h<=maiory+50;_h+=units) gc.drawLine(0,_h,maiorx+50,_h);
		}
		
		// Desenhamos todos os icones
		for(Icone I:icones) if(I.getTipoIcone()==2) I.draw(gc);
		for(Icone I:icones) if(I.getTipoIcone()!=2) I.draw(gc);
	
		
		return image;
	}

/**###################################################### 
#	Metodo publico para efetuar a copia dos valores #
#de uma conexão de rede especifica informada pelo usuá- #
#rio para as demais conexões de rede.                   #
#######################################################*/
	public void matchNetwork(){
		if(iconeAuxiliarMatchRede.getTipoIcone() == 2){
			double banda=0.0, taxa=0.0, latencia=0.0;
			int intMatch = iconeAuxiliarMatchRede.getIdGlobal();
			banda=iconeAuxiliarMatchRede.getBanda();
			taxa=iconeAuxiliarMatchRede.getTaxaOcupacao();
			latencia=iconeAuxiliarMatchRede.getLatencia();
			for(Icone I:icones){
				if(I.getTipoIcone()==2 && I.getIdGlobal()!=intMatch){
					I.setNome("lan"+I.getIdGlobal());
					I.setBanda(banda);
					I.setTaxaOcupacao(taxa);
					I.setLatencia(latencia);
					listaNos.add(I.getNome());
				}
			}			
		}else{
			JOptionPane.showMessageDialog(null,"Please selected a network icon","Warning",JOptionPane.WARNING_MESSAGE);
		}
	}
	
	public void selecionaCargas(){
		String maquinas = new String("");
		for(Icone I:icones){
			if(I.getTipoIcone()==1 && I.getMestre()){maquinas += I.getNome()+"\n";}
			if(I.getTipoIcone()==3){maquinas += I.getNome()+"\n";}
		}
		SelecionaCargas carga = new SelecionaCargas(cargasConfiguradas,cargasTipoConfiguracao,cargasConfiguracao,maquinas);
		cargasConfiguradas = carga.getCargasConfiguradas();
		cargasTipoConfiguracao = carga.getCargasTipoConfiguracao();
		cargasConfiguracao = carga.getCargasConfiguracao();
		//JOptionPane.showMessageDialog(null,cargasConfiguradas+String.valueOf(cargasTipoConfiguracao)+cargasConfiguracao,"Completed",JOptionPane.INFORMATION_MESSAGE);
	}


}
