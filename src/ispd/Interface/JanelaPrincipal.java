/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.Interface;
import ispd.DescreveSistema.*;
import ispd.CarregaArqTexto.*;
import ispd.InterpretadorExterno.SimGrid.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;  
import java.net.*;  
import javax.imageio.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
//import javax.swing.SwingUtilities;
/*
 * JanelaPrincipal.java
 *
 * Created on 02/11/2009, 17:57:29
 */
/**
 *
 * @author Aldo Ianelo Guerra
 */
public class JanelaPrincipal extends javax.swing.JFrame implements KeyListener, Serializable{

    /** Creates new form JanelaPrincipal */
    public JanelaPrincipal() {
        initComponents();

	this.setObjetosEnabled(false);

	addWindowListener( new WindowAdapter() {
		public void windowClosing( WindowEvent e ){
			if(!arquivoSalvo){
				int opcao = JOptionPane.showConfirmDialog(null,"Do you want to save changes to \'"+nomeArquivo+"\'", "Exit", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				switch(opcao){
					case JOptionPane.YES_OPTION:{
									salvar();
									System.exit(0);
								}break;
					case JOptionPane.NO_OPTION:{
									System.exit(0);
								}break;
					case JOptionPane.CANCEL_OPTION:{
									
								}break;
					}
			}else{
				System.exit(0);
			}
		}
	} ); 


	jTextAreaBarraLateral.addKeyListener( this );
	notificaTextArea.addKeyListener( this );
	barraIcones.addKeyListener( this );
		painelPrincipal.addKeyListener( this );
		botaoMaquina.addKeyListener( this );
		botaoRede.addKeyListener( this );
		botaoCluster.addKeyListener( this );
		botaoInternet.addKeyListener( this );
		botaoSimular.addKeyListener( this );
		botaoTarefas.addKeyListener( this );
		barraLateral.addKeyListener( this );
		jScrollPaneBarraLateral.addKeyListener( this );
		jTextAreaBarraLateral.addKeyListener( this );
		barraNotifica.addKeyListener( this );
		notificaTextArea.addKeyListener( this );
		painelDesenho.addKeyListener( this );
	this.addKeyListener( this ); // permite que o frame processe os eventos de teclado
    }//Fim do construtor

    private void initComponents() {

		painelPrincipal = new javax.swing.JPanel();
		barraIcones = new javax.swing.JPanel();
		botaoMaquina = new javax.swing.JButton();
		botaoRede = new javax.swing.JButton();
		botaoCluster = new javax.swing.JButton();
		botaoInternet = new javax.swing.JButton();
		botaoSimular = new javax.swing.JButton();
		botaoTarefas = new javax.swing.JButton();
		barraLateral = new javax.swing.JPanel();
		jScrollPaneBarraLateral = new javax.swing.JScrollPane();
		jTextAreaBarraLateral = new javax.swing.JTextArea();
		barraNotifica = new javax.swing.JScrollPane();
		notificaTextArea = new javax.swing.JTextArea();
		painelDesenho = new javax.swing.JScrollPane();
		barraMenus = new javax.swing.JMenuBar();
		fileMenu = new javax.swing.JMenu();
		aboutMenu = new javax.swing.JMenu();
		exportMenu = new javax.swing.JMenu();
		importMenu = new javax.swing.JMenu();
		jSeparator1 = new javax.swing.JSeparator();
		jSeparator2 = new javax.swing.JSeparator();
		newMenuItem = new javax.swing.JMenuItem();
		openMenuItem = new javax.swing.JMenuItem();
		saveMenuItem = new javax.swing.JMenuItem();
		exportJpgMenuItem = new javax.swing.JMenuItem();
		exportTxtMenuItem = new javax.swing.JMenuItem();
		importSimgridMenuItem = new javax.swing.JMenuItem();
		matchNetworkMenuItem = new javax.swing.JMenuItem();
		aboutSimulatorMenuItem = new javax.swing.JMenuItem();
		helpMenuItem = new javax.swing.JMenuItem();
		exitMenuItem = new javax.swing.JMenuItem();
		closeMenuItem = new javax.swing.JMenuItem();
		editMenu = new javax.swing.JMenu();
		gridJCheckBoxMenuItem = new JCheckBoxMenuItem();
		ruleJCheckBoxMenuItem = new JCheckBoxMenuItem();
		conectadosJCheckBoxMenuItem = new JCheckBoxMenuItem();
		indiretosJCheckBoxMenuItem = new JCheckBoxMenuItem();
		escalonaveisJCheckBoxMenuItem = new JCheckBoxMenuItem();
		fc = new JFileChooser();
		arquivoSalvo = true;
		aDesenhoAberta = false;


        //setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(ValidaValores.nomePrograma);
        setMinimumSize(new java.awt.Dimension(800, 600));
		Image imagem = Toolkit.getDefaultToolkit().getImage( getClass().getResource("imagens/Logo_GSPD_128.png"));
		setIconImage(imagem);
		setLocationRelativeTo(null);
		
        painelPrincipal.setMinimumSize(new java.awt.Dimension(800, 600));

		painelDesenho.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		painelDesenho.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );

        barraIcones.setBorder(javax.swing.BorderFactory.createTitledBorder("Icon"));
        barraIcones.setMinimumSize(new java.awt.Dimension(800, 111));
        barraIcones.setPreferredSize(new java.awt.Dimension(800, 111));

        botaoMaquina.setIcon(new javax.swing.ImageIcon(getClass().getResource("imagens/botao_no.gif"))); // NOI18N
		botaoMaquina.setToolTipText("Selects machine icon for add to the model");
        botaoMaquina.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoMaquinaActionPerformed(evt);
            }
        });

        botaoRede.setIcon(new javax.swing.ImageIcon(getClass().getResource("imagens/botao_rede.gif"))); // NOI18N
		botaoRede.setToolTipText("Selects network icon for add to the model");
        botaoRede.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoRedeActionPerformed(evt);
            }
        });

        botaoCluster.setIcon(new javax.swing.ImageIcon(getClass().getResource("imagens/botao_cluster.gif"))); // NOI18N
		botaoCluster.setToolTipText("Selects cluster icon for add to the model");
        botaoCluster.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoClusterActionPerformed(evt);
            }
        });

        botaoInternet.setIcon(new javax.swing.ImageIcon(getClass().getResource("imagens/botao_internet.gif"))); // NOI18N
		botaoInternet.setToolTipText("Selects internet icon for add to the model");
        botaoInternet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoInternetActionPerformed(evt);
            }
        });

        botaoSimular.setText("Simulate");
		botaoSimular.setToolTipText("Starts the simulation");
		botaoSimular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoSimularActionPerformed(evt);
            }
        });

		botaoTarefas.setIcon(new javax.swing.ImageIcon(getClass().getResource("imagens/botao_tarefas.gif")));
		botaoTarefas.setToolTipText("Selects insertion model of tasks");
        botaoTarefas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoTarefasActionPerformed(evt);
            }
        });
		
        javax.swing.GroupLayout barraIconesLayout = new javax.swing.GroupLayout(barraIcones);
        barraIcones.setLayout(barraIconesLayout);
        barraIconesLayout.setHorizontalGroup(
            barraIconesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(barraIconesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(botaoMaquina)
                .addGap(10, 10, 10)
                .addComponent(botaoRede)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botaoCluster)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botaoInternet)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(botaoTarefas)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 142, Short.MAX_VALUE)
                .addComponent(botaoSimular)
                .addContainerGap())
        );
        barraIconesLayout.setVerticalGroup(
            barraIconesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(barraIconesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(barraIconesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(barraIconesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(botaoRede)
                        .addComponent(botaoCluster)
                        .addComponent(botaoSimular)
                        .addComponent(botaoInternet)
                        .addComponent(botaoTarefas))
                    .addComponent(botaoMaquina))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        barraLateral.setBorder(javax.swing.BorderFactory.createTitledBorder("Settings"));
		barraLateral.setMaximumSize(new java.awt.Dimension(251, 32767));
		
		jTextAreaBarraLateral.setBackground(new java.awt.Color(240, 240, 240));
        jTextAreaBarraLateral.setColumns(20);
        jTextAreaBarraLateral.setEditable(false);
        jTextAreaBarraLateral.setRows(5);
        jScrollPaneBarraLateral.setViewportView(jTextAreaBarraLateral);

        javax.swing.GroupLayout barraLateralLayout = new javax.swing.GroupLayout(barraLateral);
        barraLateral.setLayout(barraLateralLayout);
        barraLateralLayout.setHorizontalGroup(
            barraLateralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(barraLateralLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneBarraLateral, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        barraLateralLayout.setVerticalGroup(
            barraLateralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(barraLateralLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneBarraLateral, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
                .addContainerGap())
        );

        barraNotifica.setBorder(javax.swing.BorderFactory.createTitledBorder("Notifications"));
        barraNotifica.setMinimumSize(new java.awt.Dimension(800, 111));
        barraNotifica.setPreferredSize(new java.awt.Dimension(800, 111));

        notificaTextArea.setColumns(20);
        notificaTextArea.setEditable(false);
        notificaTextArea.setRows(5);
		notificaTextArea.setText("Welcome to the grid computing simulator");
        barraNotifica.setViewportView(notificaTextArea);
		

        javax.swing.GroupLayout painelPrincipalLayout = new javax.swing.GroupLayout(painelPrincipal);
        painelPrincipal.setLayout(painelPrincipalLayout);
        painelPrincipalLayout.setHorizontalGroup(
            painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelPrincipalLayout.createSequentialGroup()
                .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelPrincipalLayout.createSequentialGroup()
                        .addComponent(barraLateral, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(painelDesenho, javax.swing.GroupLayout.DEFAULT_SIZE, 533, Short.MAX_VALUE))
                    .addComponent(barraNotifica, javax.swing.GroupLayout.PREFERRED_SIZE, 790, Short.MAX_VALUE)
                    .addComponent(barraIcones, javax.swing.GroupLayout.PREFERRED_SIZE, 790, Short.MAX_VALUE))
                .addContainerGap())
        );
        painelPrincipalLayout.setVerticalGroup(
            painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(barraLateral, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(painelDesenho, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(barraIcones, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(barraNotifica, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27))
        );

        fileMenu.setText("File");

        newMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newMenuItem.setText("New");
		newMenuItem.setToolTipText("Starts a new model");
        newMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(newMenuItem);

		openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openMenuItem.setText("Open");
		openMenuItem.setToolTipText("Opens an existing model");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);
		
		saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setText("Save");
		saveMenuItem.setToolTipText("Save the open model");
		saveMenuItem.setEnabled(false);
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);


		importMenu.setText("Import");

        importSimgridMenuItem.setText("SimGrid model");
		importSimgridMenuItem.setToolTipText("Open model from the specification files of Simgrid");
        importSimgridMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importSimgridMenuItemActionPerformed(evt);
            }
        });
        importMenu.add(importSimgridMenuItem);
		
		fileMenu.add(importMenu);


		exportMenu.setText("Export");

        exportJpgMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_J, java.awt.event.InputEvent.CTRL_MASK));
        exportJpgMenuItem.setText("to JPG");
		exportJpgMenuItem.setToolTipText("Creates a jpg file with the model image");
		exportJpgMenuItem.setEnabled(false);
        exportJpgMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportJpgMenuItemActionPerformed(evt);
            }
        });
        exportMenu.add(exportJpgMenuItem);

		exportTxtMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        exportTxtMenuItem.setText("to TXT");
		exportTxtMenuItem.setToolTipText("Creates a file in plain text with the model data according to the grammar of the iconic model");
		exportTxtMenuItem.setEnabled(false);
        exportTxtMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportTxtMenuItemActionPerformed(evt);
            }
        });
        exportMenu.add(exportTxtMenuItem);
		
		fileMenu.add(exportMenu);
		
		fileMenu.add(jSeparator1);

        closeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.CTRL_MASK));
        closeMenuItem.setText("Close");
		closeMenuItem.setToolTipText("Closes the currently open model");
		closeMenuItem.setEnabled(false);
        closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(closeMenuItem);

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        exitMenuItem.setText("Exit");
		exitMenuItem.setToolTipText("Closes the program");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        barraMenus.add(fileMenu);

        editMenu.setText("Edit");

		conectadosJCheckBoxMenuItem.setText("Show Connected Nodes");
		conectadosJCheckBoxMenuItem.setToolTipText("Displays in the settings area, the list of nodes connected for the selected icon");
		conectadosJCheckBoxMenuItem.setSelected(false);
		conectadosJCheckBoxMenuItem.setEnabled(false);
        conectadosJCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conectadosJCheckBoxMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(conectadosJCheckBoxMenuItem);
		
		indiretosJCheckBoxMenuItem.setText("Show Indirectly Connected Nodes");
		indiretosJCheckBoxMenuItem.setToolTipText("Displays in the settings area, the list of nodes connected through the internet icon, to the icon selected");
		indiretosJCheckBoxMenuItem.setSelected(false);
		indiretosJCheckBoxMenuItem.setEnabled(false);
        indiretosJCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indiretosJCheckBoxMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(indiretosJCheckBoxMenuItem);
		
		escalonaveisJCheckBoxMenuItem.setText("Show Schedulable Nodes");
		escalonaveisJCheckBoxMenuItem.setToolTipText("Displays in the settings area, the list of nodes schedulable for the selected icon");
		escalonaveisJCheckBoxMenuItem.setSelected(true);
		escalonaveisJCheckBoxMenuItem.setEnabled(false);
        escalonaveisJCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                escalonaveisJCheckBoxMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(escalonaveisJCheckBoxMenuItem);
		
		gridJCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
		gridJCheckBoxMenuItem.setText("Drawing grid");
		gridJCheckBoxMenuItem.setToolTipText("Displays grid in the drawing area");
		gridJCheckBoxMenuItem.setSelected(false);
		gridJCheckBoxMenuItem.setEnabled(false);
        gridJCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gridJCheckBoxMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(gridJCheckBoxMenuItem);

		ruleJCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
		ruleJCheckBoxMenuItem.setText("Drawing rule");
		ruleJCheckBoxMenuItem.setToolTipText("Displays rule in the drawing area");
		ruleJCheckBoxMenuItem.setSelected(false);
		ruleJCheckBoxMenuItem.setEnabled(false);
        ruleJCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ruleJCheckBoxMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(ruleJCheckBoxMenuItem);
		
		//matchNetworkMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        matchNetworkMenuItem.setText("Match network settings");
		matchNetworkMenuItem.setToolTipText("Matches the settings of icons of networks according to a selected icon");
		matchNetworkMenuItem.setEnabled(false);
        matchNetworkMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                matchNetworkMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(matchNetworkMenuItem);

        barraMenus.add(editMenu);

	//barraMenus.add(Box.createHorizontalGlue());
		
		aboutMenu.setText("Help");
	
		helpMenuItem.setText("Help");
		helpMenuItem.setToolTipText("Help");
        helpMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpMenuItemActionPerformed(evt);
            }
        });
		aboutMenu.add(helpMenuItem);

		aboutMenu.add(jSeparator2);
	
		aboutSimulatorMenuItem.setText("About "+ValidaValores.nomePrograma);
		aboutSimulatorMenuItem.setToolTipText("About "+ValidaValores.nomePrograma);
        aboutSimulatorMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutSimulatorMenuItemActionPerformed(evt);
            }
        });
		aboutMenu.add(aboutSimulatorMenuItem);
		
		barraMenus.add(aboutMenu);
		
        setJMenuBar(barraMenus);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(painelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(painelPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }

    private void botaoTarefasActionPerformed(java.awt.event.ActionEvent evt) {
    	aDesenho.selecionaCargas();
	}
	
    private void botaoSimularActionPerformed(java.awt.event.ActionEvent evt) {
    
		//aDesenho.setIconeSelecionado(5);
		aDesenho.iniciarSimulacao();
		setTextAreaNotifica("Simulate button added.");
    }
	
    private void botaoMaquinaActionPerformed(java.awt.event.ActionEvent evt) {
       
		aDesenho.setIconeSelecionado(1);
		setTextAreaNotifica("Machine button selected.");
    }

    private void botaoRedeActionPerformed(java.awt.event.ActionEvent evt) {
   
		aDesenho.setIconeSelecionado(2);
		setTextAreaNotifica("Network button selected.");
    }

    private void botaoClusterActionPerformed(java.awt.event.ActionEvent evt) {
        
		aDesenho.setIconeSelecionado(3);
		setTextAreaNotifica("Cluster button selected.");
    }

    private void botaoInternetActionPerformed(java.awt.event.ActionEvent evt) {
		aDesenho.setIconeSelecionado(4);
		setTextAreaNotifica("Internet button selected.");
    }

    private void newMenuItemActionPerformed(java.awt.event.ActionEvent evt) {

		aDesenho = new AreaDesenho(1500,1500);
		aDesenho.setPaineis(notificaTextArea,jTextAreaBarraLateral,arquivoSalvo);
		this.setRegua();
		String mensagem;
		painelDesenho.setViewportView(aDesenho);
		mensagem = "New model opened";
		setTextAreaNotifica(mensagem);
		setArquivoAberto("New_Model.ims"," [modified] - ");
		arquivoSalvo = false;
		
		gridJCheckBoxMenuItem.setSelected(false);
		ruleJCheckBoxMenuItem.setSelected(false);
		conectadosJCheckBoxMenuItem.setSelected(false);
		indiretosJCheckBoxMenuItem.setSelected(false);
		escalonaveisJCheckBoxMenuItem.setSelected(true);
		painelDesenho.setColumnHeaderView(null);
		painelDesenho.setRowHeaderView(null);
		painelDesenho.setCorner(JScrollPane.UPPER_LEFT_CORNER,null);
		painelDesenho.setCorner(JScrollPane.LOWER_LEFT_CORNER,null);
		painelDesenho.setCorner(JScrollPane.UPPER_RIGHT_CORNER,null);
		
		this.setObjetosEnabled(true);
    }

	private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {

		fc = new JFileChooser();
		aDesenho = new AreaDesenho(1500,1500);
		String mensagem;
		fc.addChoosableFileFilter(new ArquivoFilter());
		fc.setAcceptAllFileFilterUsed(false);
		//Add custom icons for file types.
		fc.setFileView(new ArquivoFileView());
		int returnVal = fc.showOpenDialog(JanelaPrincipal.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                //This is where a real application would open the file.
                //Abrir arquivo.
		if(file.getName().endsWith(".ims")){
			try{
				FileInputStream arquivo = new FileInputStream(file);
				ObjectInputStream objectInput = new ObjectInputStream(arquivo);
				DescreveSistema descricao =(DescreveSistema) objectInput.readObject();
				objectInput.close();
				aDesenho.setDadosSalvos(descricao);
				aDesenho.setPaineis(notificaTextArea,jTextAreaBarraLateral,arquivoSalvo);
				this.setRegua();
				painelDesenho.setViewportView(aDesenho);
				mensagem = "model opened";
				setTextAreaNotifica(mensagem);
				setArquivoAberto(file.getName()," [modified] - ");
				arquivoSalvo = false;
				this.setObjetosEnabled(true);
			}
			catch(Exception e){
				JOptionPane.showMessageDialog(null,"Error opening file.\n"+e.getMessage(),"WARNING",JOptionPane.PLAIN_MESSAGE);
				//e.printStackTrace();
			}     
		}else{JOptionPane.showMessageDialog(null,"Invalid file","WARNING",JOptionPane.PLAIN_MESSAGE);}
	   } else {
                //Cancelado
          }		
		
    }
	
	private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		salvar();
	}
	
	public void salvar(){

		fc = new JFileChooser();
		String mensagem;		
		fc.addChoosableFileFilter(new ArquivoFilter());
		fc.setAcceptAllFileFilterUsed(false);
		//Add custom icons for file types.
		fc.setFileView(new ArquivoFileView());
		int returnVal = fc.showSaveDialog(JanelaPrincipal.this);

	if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                //This is where a real application would open the file.
		if(!file.getName().endsWith(".ims")){
			File temp = new File(file.toString()+".ims");
			file = temp;
		}
			try{
				FileOutputStream arquivo = new FileOutputStream(file);
				ObjectOutputStream objectOutput = new ObjectOutputStream(arquivo);
				objectOutput.writeObject(aDesenho.getDadosASalvar());
				objectOutput.close();
				mensagem = "model saved";
				setTextAreaNotifica(mensagem);
				setArquivoAberto(file.getName()," - ");
				arquivoSalvo = true;
			}
			catch(Exception e){e.printStackTrace();} 
	        } else {
                //Cancelado
		}
		
	}

	private void conectadosJCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
	if(!conectadosJCheckBoxMenuItem.isSelected()){
		conectadosJCheckBoxMenuItem.setSelected(false);
		aDesenho.setConectados(false);
		setTextAreaNotifica("Connected Nodes hidden.");
	}else{
		conectadosJCheckBoxMenuItem.setSelected(true);
		aDesenho.setConectados(true);
		setTextAreaNotifica("Connected Nodes unhidden.");
	}
    }//GEN-LAST:event_jMenuItem1ActionPerformed
	
	private void indiretosJCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
	if(!indiretosJCheckBoxMenuItem.isSelected()){
		indiretosJCheckBoxMenuItem.setSelected(false);
		aDesenho.setIndiretos(false);
		setTextAreaNotifica("Indirectly Connected Nodes are now not being shown");
	}else{
		indiretosJCheckBoxMenuItem.setSelected(true);
		aDesenho.setIndiretos(true);
		setTextAreaNotifica("Indirectly Connected Nodes are now being shown");
	}
    }//GEN-LAST:event_jMenuItem1ActionPerformed
	
	private void escalonaveisJCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
	if(!escalonaveisJCheckBoxMenuItem.isSelected()){
		escalonaveisJCheckBoxMenuItem.setSelected(false);
		aDesenho.setEscalonaveis(false);
		setTextAreaNotifica("Schedulable Nodes hidden.");
	}else{
		escalonaveisJCheckBoxMenuItem.setSelected(true);
		aDesenho.setEscalonaveis(true);
		setTextAreaNotifica("Schedulable Nodes unhidden.");
	}
    }//GEN-LAST:event_jMenuItem1ActionPerformed
	
    private void gridJCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
	if(!gridJCheckBoxMenuItem.isSelected()){
		gridJCheckBoxMenuItem.setSelected(false);
		aDesenho.setGrid(false);
		setTextAreaNotifica("Drawing grid disabled.");
	}else{
		gridJCheckBoxMenuItem.setSelected(true);
		aDesenho.setGrid(true);
		setTextAreaNotifica("Drawing grid enabled.");
	}
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void ruleJCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
	if(!ruleJCheckBoxMenuItem.isSelected()){
		ruleJCheckBoxMenuItem.setSelected(false);
		setTextAreaNotifica("Drawing rule disabled.");
		painelDesenho.setColumnHeaderView(null);
		painelDesenho.setRowHeaderView(null);
		//Set the corners.
		painelDesenho.setCorner(JScrollPane.UPPER_LEFT_CORNER,null);
		painelDesenho.setCorner(JScrollPane.LOWER_LEFT_CORNER,null);
		painelDesenho.setCorner(JScrollPane.UPPER_RIGHT_CORNER,null);
	}else{
		ruleJCheckBoxMenuItem.setSelected(true);
		setTextAreaNotifica("Drawing rule enabled.");
		painelDesenho.setColumnHeaderView(columnView);
		painelDesenho.setRowHeaderView(rowView);
	
		painelDesenho.setCorner(JScrollPane.UPPER_LEFT_CORNER,buttonCorner);
		painelDesenho.setCorner(JScrollPane.LOWER_LEFT_CORNER, new Corner());
		painelDesenho.setCorner(JScrollPane.UPPER_RIGHT_CORNER,	new Corner());
	}
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		if(!arquivoSalvo){
			int opcao = JOptionPane.showConfirmDialog(this,"Do you want to save changes to \'"+nomeArquivo+"\'", "Exit", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			switch(opcao){
				case JOptionPane.YES_OPTION:{
								salvar();
								System.exit(0);
							}break;
				case JOptionPane.NO_OPTION:{
								System.exit(0);
							}break;
				case JOptionPane.CANCEL_OPTION:{
								
							}break;
			}
		}else{
			System.exit(0);
		}
    }

    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		close();
    }

	public void close(){
		if(!arquivoSalvo){
			int opcao = JOptionPane.showConfirmDialog(this,"Do you want to save changes to \'"+nomeArquivo+"\'", "Close", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			switch(opcao){
				case JOptionPane.YES_OPTION:{
								salvar();
								painelDesenho.setViewportView(null);
								String mensagem = "model closed";
								setTextAreaNotifica(mensagem);
								setArquivoAberto("","");
								this.setObjetosEnabled(false);
								arquivoSalvo = true;
							}break;
				case JOptionPane.NO_OPTION:{
								painelDesenho.setViewportView(null);
								String mensagem = "model closed";
								setTextAreaNotifica(mensagem);
								setArquivoAberto("","");
								this.setObjetosEnabled(false);
								arquivoSalvo = true;
							}break;
				case JOptionPane.CANCEL_OPTION:{
								
							}break;
			}
		}else{
			painelDesenho.setViewportView(null);
			String mensagem = "model closed";
			setTextAreaNotifica(mensagem);
			setArquivoAberto("","");
			this.setObjetosEnabled(false);
			arquivoSalvo = true;
		}
	}

	private void exportJpgMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		fc = new JFileChooser();
		fc.addChoosableFileFilter(new ImgFilter());
		fc.setAcceptAllFileFilterUsed(false);
		//Add custom icons for file types.
		//fc.setFileView(new ArquivoFileView());
		int returnVal = fc.showSaveDialog(JanelaPrincipal.this);
	
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
	
			if(!file.getName().endsWith(".jpg")){
				File temp = new File(file.toString()+".jpg");
				file = temp;
			}
	
			BufferedImage img = aDesenho.createImage();
			try {  
				ImageIO.write(img, "jpg", file);
			}
			catch (IOException x) {  
				x.printStackTrace();
			}
		}else{
			//Cancelado
		}
	
	}
	
	private void exportTxtMenuItemActionPerformed(java.awt.event.ActionEvent evt) {

		fc = new JFileChooser();
		fc.addChoosableFileFilter(new TxtFilter());
		fc.setAcceptAllFileFilterUsed(false);
		//Add custom icons for file types.
		//fc.setFileView(new ArquivoFileView());
		int returnVal = fc.showSaveDialog(JanelaPrincipal.this);

	if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();

		if(!file.getName().endsWith(".txt")){
			File temp = new File(file.toString()+".txt");
			file = temp;
		}
		aDesenho.escreverArquivo(file);
		
	}else{
                //Cancelado
	}

    }
	
	private void importSimgridMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		
		//File arquivo = new File("modeloiconico");
		//if(arquivo.exists()){ arquivo.delete(); }

		fc = new JFileChooser();

		JOptionPane.showMessageDialog(null,"Select the application file.","WARNING",JOptionPane.PLAIN_MESSAGE);
		int returnVal = fc.showOpenDialog(JanelaPrincipal.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file1 = fc.getSelectedFile();
			JOptionPane.showMessageDialog(null,"Select the platform file.","WARNING",JOptionPane.PLAIN_MESSAGE);
			returnVal = fc.showOpenDialog(JanelaPrincipal.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file2 = fc.getSelectedFile();

				InterpretadorSimGrid interp = new InterpretadorSimGrid();
				interp.interpreta(file1,file2);

				try{
					File file = new File("modeloiconico");
					if(file.exists()){
						String mensagem;
						CarregaArqTexto arq = new CarregaArqTexto();
						boolean achouErro = arq.leArquivo(file);
						if(!achouErro){
							DescreveSistema descricao = arq.getDescricao();
							if(arq.getW()>1500){
								aDesenho = new AreaDesenho(arq.getW(),arq.getW());
							}else{
								aDesenho = new AreaDesenho(1500,1500);
							}
							aDesenho.setDadosSalvos(descricao);
							aDesenho.setPaineis(notificaTextArea,jTextAreaBarraLateral,arquivoSalvo);
							this.setRegua();
							painelDesenho.setViewportView(aDesenho);
							mensagem = "model2 opened";
							setTextAreaNotifica(mensagem);
							setArquivoAberto(file.getName()," [modified] - ");
							arquivoSalvo = false;
							this.setObjetosEnabled(true);
						}
					}else{
						JOptionPane.showMessageDialog(null,"File not found.\n","WARNING",JOptionPane.PLAIN_MESSAGE);
					}
					file.delete();
				}
				catch(Exception e){
					JOptionPane.showMessageDialog(null,"Error opening file.\n"+e.getMessage(),"WARNING",JOptionPane.PLAIN_MESSAGE);
				}
			}else{
				//Cancelado
			}
	
		}else{
			//Cancelado
		}

	}	

	

	private void metodoImportTxt(){

		fc = new JFileChooser();
		fc.addChoosableFileFilter(new TxtFilter());
		fc.setAcceptAllFileFilterUsed(false);
		//Add custom icons for file types.
		//fc.setFileView(new ArquivoFileView());
		int returnVal = fc.showOpenDialog(JanelaPrincipal.this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
		File file = fc.getSelectedFile();
	
			if(file.getName().endsWith(".txt")){
				String mensagem;
				CarregaArqTexto arq = new CarregaArqTexto();
				boolean achouErro = arq.leArquivo(file);
				if(!achouErro){
					DescreveSistema descricao = arq.getDescricao();
					if(arq.getW()>1500){
						aDesenho = new AreaDesenho(arq.getW(),arq.getW());
					}else{
						aDesenho = new AreaDesenho(1500,1500);
					}
					aDesenho.setDadosSalvos(descricao);
					aDesenho.setPaineis(notificaTextArea,jTextAreaBarraLateral,arquivoSalvo);
					this.setRegua();
					painelDesenho.setViewportView(aDesenho);
					mensagem = "model2 opened";
					setTextAreaNotifica(mensagem);
					setArquivoAberto(file.getName()," [modified] - ");
					arquivoSalvo = false;
					this.setObjetosEnabled(true);
				}
			}
		}else{
			//Cancelado
		}

	}

	private void matchNetworkMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		aDesenho.matchNetwork();
	}

	private void helpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
	
	TreeHelp help = new TreeHelp();
	help.setVisible(true);

    }

	private void aboutSimulatorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {

		Icon icone = new ImageIcon( getClass().getResource("imagens/simbolo_t.gif"));
		String sobre =  "\nThe \""+ValidaValores.nomePrograma+"\" was developed and supported by\"\n"+
				"\"Instituto de Biociências, Letras e Ciências Exatas\", UNESP - Univ Estadual\n"+
				"Paulista, campus de São José do Rio Preto, Departamento de Ciências de\n"+ 
				"Computação e Estatística (DCCE), Laboratório do Grupo de Sistemas\n"+
				"Paralelos e Distribuídos (GSPD).\n\n"+
				//"     \n"+
				"Developers:\n"+
				"                Prof. Dr. Aleardo Manacero Junior\n"+
				"                Profª. Drª. Renata Spolon Lobato\n"+
				"                Aldo Ianelo Guerra\n"+
				"                Marco Antonio Barros Alves Garcia\n"+
				"                Paulo Henrique Maestrello Assad Oliveira\n"+
				"                Tiago Polizelli Brait\n"+
				"                Vanessa Gomes de Oliveira\n"+
				"                Victor Aoqui\n";
		
		//JOptionPane.showMessageDialog(null,sobre);
		JOptionPane.showOptionDialog(this,sobre,"About \""+ValidaValores.nomePrograma+"\"",JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE,icone,null,null);
	
    }
	
	public void setArquivoAberto(String nome,String complemento){
		nomeArquivo = nome;
		setTitle(nomeArquivo+complemento+ValidaValores.nomePrograma);

	}

	public void setTextAreaNotifica(String novoTexto){
		String antigoTexto;
		antigoTexto = notificaTextArea.getText();
		novoTexto = antigoTexto + "\n" + novoTexto;
		notificaTextArea.setText(novoTexto);
	}
	
	public void setObjetosEnabled(boolean opcao){
		botaoCluster.setEnabled(opcao);
		botaoInternet.setEnabled(opcao);
		botaoMaquina.setEnabled(opcao);
		botaoRede.setEnabled(opcao);
		botaoSimular.setEnabled(opcao);
		botaoTarefas.setEnabled(opcao);
		saveMenuItem.setEnabled(opcao);
		closeMenuItem.setEnabled(opcao);
		exportJpgMenuItem.setEnabled(opcao);
		exportTxtMenuItem.setEnabled(opcao);
		matchNetworkMenuItem.setEnabled(opcao);
		gridJCheckBoxMenuItem.setEnabled(opcao);
		ruleJCheckBoxMenuItem.setEnabled(opcao);
		conectadosJCheckBoxMenuItem.setEnabled(opcao);
		indiretosJCheckBoxMenuItem.setEnabled(opcao);
		escalonaveisJCheckBoxMenuItem.setEnabled(opcao);
		if(opcao==true){aDesenho.addKeyListener( this );}
		aDesenhoAberta = opcao;
	}
	
	public void setRegua(){
		//Create the row and column headers.
		columnView = new Rule(Rule.HORIZONTAL, true);
		rowView = new Rule(Rule.VERTICAL, true);
	
		columnView.setPreferredWidth(aDesenho.getIconWidth());
		rowView.setPreferredHeight(aDesenho.getIconHeight());
	
		//Create the corners.
		buttonCorner = new JPanel(); //use FlowLayout
		isMetric = new JToggleButton("cm", true);
		isMetric.setFont(new Font("SansSerif", Font.PLAIN, 11));
	        isMetric.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
			isMetricActionPerformed(evt);
			}
		});

		buttonCorner.add(isMetric); 

	}

	private void isMetricActionPerformed(java.awt.event.ActionEvent evt) {

		if (isMetric.isSelected()) {
		//Turn it to metric.
		isMetric.setText("cm");
		rowView.setIsMetric(true);
		columnView.setIsMetric(true);
		aDesenho.setIsMetric(true);
		} else {
		//Turn it to inches.
		isMetric.setText("in");
		rowView.setIsMetric(false);
		columnView.setIsMetric(false);
		aDesenho.setIsMetric(false);
		}

	}

	public void keyTyped(KeyEvent evento) {
		//this.setTextAreaNotifica("KEY TYPED: "+evento.getKeyChar());
	}
 
	public void keyPressed(KeyEvent evento) {
		//this.setTextAreaNotifica("KEY PRESSED: "+ evento.getKeyText( evento.getKeyCode() ));
		if(aDesenhoAberta && evento.getKeyCode() == KeyEvent.VK_DELETE){
			aDesenho.deletarIcone();
		}
		if(!aDesenhoAberta && evento.getModifiers() == (InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK | InputEvent.ALT_MASK ) && evento.getKeyCode() == KeyEvent.VK_I){
			metodoImportTxt();
		}
		if(aDesenhoAberta && evento.getModifiers() == InputEvent.CTRL_MASK && evento.getKeyCode() == KeyEvent.VK_C){
			aDesenho.acaoCopiarIcone();
		}
		if(aDesenhoAberta && evento.getModifiers() == InputEvent.CTRL_MASK && evento.getKeyCode() == KeyEvent.VK_V){
			aDesenho.acaoColarIcone();
		}
	}

	public void keyReleased(KeyEvent evento) {
		//this.setTextAreaNotifica("KEY RELEASED: "+evento.getKeyText( evento.getKeyCode() ));
	}


    // Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel barraIcones;
	private javax.swing.JPanel barraLateral;
	private javax.swing.JMenuBar barraMenus;
	private javax.swing.JScrollPane barraNotifica;
	private javax.swing.JScrollPane pDesenho;
	private javax.swing.JButton botaoCluster;
	private javax.swing.JButton botaoInternet;
	private javax.swing.JButton botaoMaquina;
	private javax.swing.JButton botaoRede;
	private javax.swing.JButton botaoTarefas;
	private javax.swing.JButton botaoSimular;
	private javax.swing.JMenu editMenu;
	private javax.swing.JMenu fileMenu;
	private javax.swing.JMenu aboutMenu;
	private javax.swing.JMenu exportMenu;
	private javax.swing.JMenu importMenu;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JSeparator jSeparator2;
	private javax.swing.JMenuItem newMenuItem;
	private javax.swing.JMenuItem closeMenuItem;
	private javax.swing.JMenuItem openMenuItem;
	private javax.swing.JMenuItem saveMenuItem;
	private javax.swing.JMenuItem exitMenuItem;
	private javax.swing.JMenuItem matchNetworkMenuItem;
	private javax.swing.JMenuItem exportJpgMenuItem;
	private javax.swing.JMenuItem exportTxtMenuItem;
	private javax.swing.JMenuItem importSimgridMenuItem;
	private javax.swing.JMenuItem aboutSimulatorMenuItem;
	private javax.swing.JMenuItem helpMenuItem;
	private javax.swing.JCheckBoxMenuItem gridJCheckBoxMenuItem;
	private javax.swing.JCheckBoxMenuItem conectadosJCheckBoxMenuItem;
	private javax.swing.JCheckBoxMenuItem indiretosJCheckBoxMenuItem;
	private javax.swing.JCheckBoxMenuItem escalonaveisJCheckBoxMenuItem;
	private javax.swing.JCheckBoxMenuItem ruleJCheckBoxMenuItem;
	private javax.swing.JTextArea notificaTextArea;
	private javax.swing.JScrollPane painelDesenho;
	private javax.swing.JPanel painelPrincipal;
	private javax.swing.JScrollPane jScrollPaneBarraLateral;
	private javax.swing.JTextArea jTextAreaBarraLateral;
	private JFileChooser fc;
	private AreaDesenho aDesenho;
	private boolean aDesenhoAberta;
	private String nomeArquivo;
	private boolean arquivoSalvo;
	private Rule columnView;
	private Rule rowView;
	private JToggleButton isMetric;
	private JPanel buttonCorner;
	// End of variables declaration//GEN-END:variables
	

}
