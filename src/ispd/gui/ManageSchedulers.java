package ispd.gui;

import ispd.arquivo.Escalonadores;
import ispd.escalonador.ManipularArquivos;
import ispd.gui.auxiliar.MultipleExtensionFileFilter;
import ispd.gui.auxiliar.TextEditorStyle;
import ispd.utils.ValidaValores;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ManageSchedulers extends JFrame {
    private final UndoableEdit undo = new UndoManager();
    private final ManipularArquivos escalonadores;
    private final ResourceBundle palavras;
    private JFileChooser jFileChooser1;
    private JList jListEscalonadores;
    private JScrollPane jScrollPane2;
    private JTextPane jTextPane1;
    private boolean modificado;//indica se arquivo atual foi modificado
    private String escalonadorAberto;

    ManageSchedulers() {
        final Locale locale = Locale.getDefault();
        this.palavras = ResourceBundle.getBundle("ispd.idioma.Idioma", locale);
        //Gerenciamento dos escalonadores
        this.escalonadores = new Escalonadores();
        //Inicia o editor
        this.initComponents();
        //Define a linguagem do editor
        final TextEditorStyle estiloJava = new TextEditorStyle();
        estiloJava.configurarTextComponent(this.jTextPane1);
        this.jScrollPane2.setRowHeaderView(estiloJava.getLinhas());
        this.jScrollPane2.setColumnHeaderView(estiloJava.getCursor());
        this.fecharEdicao();
        // Eventos de desfazer e refazer
        final javax.swing.text.Document doc = this.jTextPane1.getDocument();
        doc.addUndoableEditListener(this::onUndo);
        // Evento verifica alterações
        doc.addDocumentListener(new SomeDocumentListener());
        this.atualizarEscalonadores(this.escalonadores.listar());
        this.addWindowListener(new SomeWindowAdapter());
    }

    private void onUndo(final UndoableEditEvent evt) {
        if (!"style change".equals(evt.getEdit().getPresentationName())) {
            this.undo.addEdit(evt.getEdit());
        }
    }

    private void initComponents() {

        final JPopupMenu jPopupMenuTexto =
                new JPopupMenu();
        final JMenuItem jMenuItemCut1 = new JMenuItem();
        final JMenuItem jMenuItemCopy1 =
                new JMenuItem();
        final JMenuItem jMenuItemPaste1 =
                new JMenuItem();
        this.jFileChooser1 = new JFileChooser();
        final JToolBar jToolBar1 = new JToolBar();
        final JButton jButtonNovo = new JButton();
        final JButton jButtonSalvar = new JButton();
        final JButton jButtonCompilar = new JButton();
        final JPanel jPanelEscalonadores = new JPanel();
        final JScrollPane jScrollPane3 =
                new JScrollPane();
        this.jListEscalonadores = new JList();
        final JPanel jPanelEditorTexto = new JPanel();
        this.jScrollPane2 = new JScrollPane();
        this.jTextPane1 = new JTextPane();
        final JLabel jLabelCaretPos = new JLabel();
        final JMenuBar jMenuBar1 = new JMenuBar();
        final JMenu jMenuArquivo = new JMenu();
        final JMenuItem jMenuItemNovo = new JMenuItem();
        final JMenuItem jMenuItemAbrir =
                new JMenuItem();
        final JMenuItem jMenuItemSalvar =
                new JMenuItem();
        final JMenuItem jMenuItemImportar =
                new JMenuItem();
        final JMenu jMenuEditar = new JMenu();
        final JMenuItem jMenuItemDesfazer =
                new JMenuItem();
        final JMenuItem jMenuItemRefazer =
                new JMenuItem();
        final Component jSeparator1 =
                new JPopupMenu.Separator();
        final JMenuItem jMenuItemCut = new JMenuItem();
        final JMenuItem jMenuItemCopy = new JMenuItem();
        final JMenuItem jMenuItemPaste =
                new JMenuItem();
        final Component jSeparator2 =
                new JPopupMenu.Separator();
        final JMenuItem jMenuItemDelete =
                new JMenuItem();

        jMenuItemCut1.setText(this.translate("Cut"));
        jMenuItemCut1.addActionListener(this::jMenuItemCutActionPerformed);
        jPopupMenuTexto.add(jMenuItemCut1);

        jMenuItemCopy1.setText(this.translate("Copy"));
        jMenuItemCopy1.addActionListener(this::jMenuItemCopyActionPerformed);
        jPopupMenuTexto.add(jMenuItemCopy1);

        jMenuItemPaste1.setText(this.translate("Paste")); //

        jMenuItemPaste1.addActionListener(this::jMenuItemPasteActionPerformed);
        jPopupMenuTexto.add(jMenuItemPaste1);

        this.jFileChooser1.setAcceptAllFileFilterUsed(false);
        this.jFileChooser1.setFileFilter(new MultipleExtensionFileFilter(this.translate(
                "Java" +
                " Source Files (. java)"), ".java", true));

        this.setTitle(this.translate("Manage Schedulers"));
        this.setAlwaysOnTop(true);
        this.setFocusable(false);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(this.getResource(
                "imagens/Logo_iSPD_25.png")));

        jToolBar1.setRollover(true);

        jButtonNovo.setIcon(new ImageIcon(this.getResource("/ispd" +
                                                           "/gui" +
                                                           "/imagens/insert" +
                                                           "-object.png")));
        jButtonNovo.setToolTipText(this.translate("Creates a " +
                                                  "new " +
                                                  "scheduler"));
        jButtonNovo.setFocusable(false);
        jButtonNovo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonNovo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonNovo.addActionListener(this::jMenuItemNovojButtonNovoActionPerformed);
        jToolBar1.add(jButtonNovo);
        jButtonNovo.getAccessibleContext().setAccessibleDescription(this.translate("Creates a new scheduler"));

        jButtonSalvar.setIcon(new ImageIcon(this.getResource(
                "/ispd" +
                "/gui/imagens/document-save.png")));
        jButtonSalvar.setToolTipText(this.translate("Save the " +
                                                    "open file"));
        jButtonSalvar.setFocusable(false);
        jButtonSalvar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSalvar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonSalvar.addActionListener(this::jButtonSalvarActionPerformed);
        jToolBar1.add(jButtonSalvar);

        jButtonCompilar.setIcon(new ImageIcon(this.getResource(
                "/ispd" +
                "/gui/imagens/system-run.png")));
        jButtonCompilar.setToolTipText(this.translate("Compile"));
        jButtonCompilar.setFocusable(false);
        jButtonCompilar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonCompilar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonCompilar.addActionListener(this::jButtonCompilarActionPerformed);
        jToolBar1.add(jButtonCompilar);

        this.jListEscalonadores.setBorder(javax.swing.BorderFactory.createTitledBorder(null, this.translate("Scheduler"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        this.jListEscalonadores.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        this.jListEscalonadores.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(final java.awt.event.MouseEvent evt) {
                ManageSchedulers.this.jListEscalonadoresMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(this.jListEscalonadores);

        final GroupLayout jPanelEscalonadoresLayout =
                new GroupLayout(jPanelEscalonadores);
        jPanelEscalonadores.setLayout(jPanelEscalonadoresLayout);
        jPanelEscalonadoresLayout.setHorizontalGroup(
                jPanelEscalonadoresLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelEscalonadoresLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane3,
                                        GroupLayout.DEFAULT_SIZE,
                                        120, Short.MAX_VALUE)
                                .addContainerGap())
        );
        jPanelEscalonadoresLayout.setVerticalGroup(
                jPanelEscalonadoresLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING,
                                jPanelEscalonadoresLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(jScrollPane3,
                                                GroupLayout.DEFAULT_SIZE,
                                                506, Short.MAX_VALUE)
                                        .addContainerGap())
        );

        this.jScrollPane2.setViewportView(this.jTextPane1);

        final GroupLayout jPanelEditorTextoLayout =
                new GroupLayout(jPanelEditorTexto);
        jPanelEditorTexto.setLayout(jPanelEditorTextoLayout);
        jPanelEditorTextoLayout.setHorizontalGroup(
                jPanelEditorTextoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelEditorTextoLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(this.jScrollPane2,
                                        GroupLayout.DEFAULT_SIZE,
                                        734, Short.MAX_VALUE)
                                .addContainerGap())
        );
        jPanelEditorTextoLayout.setVerticalGroup(
                jPanelEditorTextoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelEditorTextoLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(this.jScrollPane2)
                                .addContainerGap())
        );

        jMenuArquivo.setText(this.translate("File"));
        jMenuArquivo.addActionListener(this::jButtonSalvarActionPerformed);

        jMenuItemNovo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                InputEvent.CTRL_DOWN_MASK));
        final String name = "/ispd/gui/imagens/insert-object_1.png";
        jMenuItemNovo.setIcon(new ImageIcon(this.getResource(name)));
        jMenuItemNovo.setText(this.translate("New"));
        jMenuItemNovo.setToolTipText(this.translate("Creates a " +
                                                    "new " +
                                                    "scheduler"));
        jMenuItemNovo.addActionListener(this::jMenuItemNovojButtonNovoActionPerformed);
        jMenuArquivo.add(jMenuItemNovo);

        jMenuItemAbrir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                InputEvent.CTRL_DOWN_MASK));
        jMenuItemAbrir.setIcon(new ImageIcon(this.getResource(
                "/ispd" +
                "/gui/imagens/document-open.png")));
        jMenuItemAbrir.setText(this.translate("Open"));
        jMenuItemAbrir.setToolTipText(this.translate("Opens an " +
                                                     "existing " +
                                                     "scheduler"));
        jMenuItemAbrir.addActionListener(this::jMenuItemAbrirActionPerformed);
        jMenuArquivo.add(jMenuItemAbrir);

        jMenuItemSalvar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                InputEvent.CTRL_DOWN_MASK));
        jMenuItemSalvar.setIcon(new ImageIcon(this.getResource(
                "/ispd" +
                "/gui/imagens/document-save_1.png")));
        jMenuItemSalvar.setText(this.translate("Save"));
        jMenuItemSalvar.setToolTipText(this.translate("Save the" +
                                                      " open " +
                                                      "file"));
        jMenuItemSalvar.addActionListener(this::jButtonSalvarActionPerformed);
        jMenuArquivo.add(jMenuItemSalvar);

        jMenuItemImportar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I
                , InputEvent.CTRL_DOWN_MASK));
        jMenuItemImportar.setIcon(new ImageIcon(this.getResource(
                "/ispd/gui/imagens/document-import.png")));
        jMenuItemImportar.setText(this.translate("Import")); //

        jMenuItemImportar.addActionListener(this::jMenuItemImportarActionPerformed);
        jMenuArquivo.add(jMenuItemImportar);

        jMenuBar1.add(jMenuArquivo);

        jMenuEditar.setText(this.translate("Edit"));

        jMenuItemDesfazer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z
                , InputEvent.CTRL_DOWN_MASK));
        jMenuItemDesfazer.setIcon(new ImageIcon(this.getResource(
                "/ispd/gui/imagens/edit-undo.png")));
        jMenuItemDesfazer.setText(this.translate("Undo")); //

        jMenuItemDesfazer.addActionListener(this::jMenuItemDesfazerActionPerformed);
        jMenuEditar.add(jMenuItemDesfazer);

        jMenuItemRefazer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                InputEvent.CTRL_DOWN_MASK));
        jMenuItemRefazer.setIcon(new ImageIcon(this.getResource(
                "/ispd" +
                "/gui/imagens/edit-redo.png")));
        final String word = "Redo";
        jMenuItemRefazer.setText(this.translate(word)); //

        jMenuItemRefazer.addActionListener(this::jMenuItemRefazerActionPerformed);
        jMenuEditar.add(jMenuItemRefazer);
        jMenuEditar.add(jSeparator1);

        jMenuItemCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                InputEvent.CTRL_DOWN_MASK));
        jMenuItemCut.setIcon(new ImageIcon(this.getResource(
                "/ispd/gui" +
                "/imagens/edit-cut.png")));
        jMenuItemCut.setText(this.translate("Cut"));
        jMenuItemCut.addActionListener(this::jMenuItemCutActionPerformed);
        jMenuEditar.add(jMenuItemCut);

        jMenuItemCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                InputEvent.CTRL_DOWN_MASK));
        jMenuItemCopy.setIcon(new ImageIcon(this.getResource(
                "/ispd" +
                "/gui/imagens/edit-copy.png")));
        jMenuItemCopy.setText(this.translate("Copy"));
        jMenuItemCopy.addActionListener(this::jMenuItemCopyActionPerformed);
        jMenuEditar.add(jMenuItemCopy);

        jMenuItemPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                InputEvent.CTRL_DOWN_MASK));
        jMenuItemPaste.setIcon(new ImageIcon(this.getResource(
                "/ispd" +
                "/gui/imagens/edit-paste.png")));
        jMenuItemPaste.setText(this.translate("Paste"));
        jMenuItemPaste.addActionListener(this::jMenuItemPasteActionPerformed);
        jMenuEditar.add(jMenuItemPaste);
        jMenuEditar.add(jSeparator2);

        jMenuItemDelete.setIcon(new ImageIcon(this.getResource(
                "/ispd" +
                "/gui/imagens/edit-delete.png")));
        jMenuItemDelete.setText(this.translate("Delete"));

        jMenuItemDelete.addActionListener(this::jMenuItemDeleteActionPerformed);
        jMenuEditar.add(jMenuItemDelete);

        jMenuBar1.add(jMenuEditar);

        this.setJMenuBar(jMenuBar1);

        final GroupLayout layout =
                new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jPanelEscalonadores, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jPanelEditorTexto, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addContainerGap(904,
                                                        Short.MAX_VALUE)
                                                .addComponent(jLabelCaretPos))
                                        .addComponent(jToolBar1,
                                                GroupLayout.DEFAULT_SIZE, 904
                                                , Short.MAX_VALUE))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jToolBar1,
                                        GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabelCaretPos,
                                                GroupLayout.Alignment.TRAILING)
                                        .addComponent(jPanelEscalonadores,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(jPanelEditorTexto,
                                                GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );

        this.pack();
    }

    private String translate(final String word) {
        return this.palavras.getString(word);
    }

    private URL getResource(final String name) {
        return this.getClass().getResource(name);
    }

    private void jMenuItemNovojButtonNovoActionPerformed(final ActionEvent evt) {

        int escolha = JOptionPane.YES_OPTION;
        if (this.modificado) {
            escolha = this.savarAlteracao();
        }
        if (escolha != JOptionPane.CANCEL_OPTION && escolha != JOptionPane.CLOSED_OPTION) {
            final String[] ops = { "Edit java class", "Generator schedulers" };
            final String result = (String) JOptionPane.showInputDialog(this,
                    "Creating the scheduler with:", null,
                    JOptionPane.INFORMATION_MESSAGE, null, ops, ops[0]);
            if (result != null) {
                if (result.equals(ops[0])) {
                    final String result1 = JOptionPane.showInputDialog(this,
                            "Enter the name of the scheduler");
                    boolean nomeOk = false;
                    if (result1 != null) {
                        nomeOk = ValidaValores.validaNomeClasse(result1);
                    }
                    if (nomeOk) {
                        //Carregar classe para esditar java
                        this.abrirEdicao(result1,
                                Escalonadores.getEscalonadorJava(result1));
                        this.modificar();
                    }
                } else if (result.equals(ops[1])) {
                    //Carregar classe para construir escalonador automaticamente
                    final CreateSchedulerDialog ge =
                            new CreateSchedulerDialog(this,
                            true,
                            this.escalonadores.getDiretorio().getAbsolutePath(), this.palavras);
                    ge.setLocationRelativeTo(this);
                    ge.setVisible(true);
                    if (ge.getParse() != null) {
                        this.escalonadores.escrever(ge.getParse().getNome(),
                                ge.getParse().getCodigo());
                        final String erros =
                                this.escalonadores.compilar(ge.getParse().getNome());
                        if (erros != null) {
                            JOptionPane.showMessageDialog(this, erros, "Erros" +
                                                                       " encontrados", JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "Escalonador" + this.escalonadorAberto +
                                    "\nCompilador com sucesso");
                        }
                        this.atualizarEscalonadores(this.escalonadores.listar());
                    }
                }
            }
        }
    }

    private void jMenuItemCutActionPerformed(final ActionEvent evt) {

        this.jTextPane1.cut();
    }

    private void jMenuItemCopyActionPerformed(final ActionEvent evt) {

        this.jTextPane1.copy();
    }

    private void jMenuItemPasteActionPerformed(final ActionEvent evt) {

        this.jTextPane1.paste();
    }

    private void jMenuItemDesfazerActionPerformed(final ActionEvent evt) {

        try {
            this.undo.undo();
        } catch (final Exception e) {
        }
    }

    private void jMenuItemRefazerActionPerformed(final ActionEvent evt) {

        try {
            this.undo.redo();
        } catch (final Exception e) {
        }
    }

    private void jListEscalonadoresMouseClicked(final java.awt.event.MouseEvent evt) {

        if (evt.getClickCount() == 2) {
            int escolha = JOptionPane.YES_OPTION;
            if (this.modificado) {
                escolha = this.savarAlteracao();
            }
            if (escolha != JOptionPane.CANCEL_OPTION && escolha != JOptionPane.CLOSED_OPTION) {
                //Escalonador a ser aberto
                final String result =
                        (String) this.jListEscalonadores.getSelectedValue();
                //Conteudo do arquivo
                final String conteud = this.escalonadores.ler(result);
                //Adicionar ao editor
                this.abrirEdicao(result, conteud);
            }
        }
    }

    private void jButtonSalvarActionPerformed(final ActionEvent evt) {

        if (this.escalonadorAberto != null && this.modificado) {
            this.escalonadores.escrever(this.escalonadorAberto,
                    this.jTextPane1.getText());
            this.salvarModificacao();
        }
    }

    private void jMenuItemDeleteActionPerformed(final ActionEvent evt) {

        if (this.jListEscalonadores.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(this, "A scheduler should be " +
                                                "selected");
            return;
        }

        final String aux =
                this.jListEscalonadores.getSelectedValue().toString();
        final int escolha = JOptionPane.showConfirmDialog(this, "Are you " +
                                                                "sure " +
                                                                "want " +
                                                                "delete " +
                                                                "this " +
                                                                "scheduler: " +
                                                                "\n" + aux,
                null, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (escolha == JOptionPane.YES_OPTION) {
            if (!this.escalonadores.remover(aux)) {
                JOptionPane.showMessageDialog(this,
                        "Failed to remove " + aux);
            } else if (this.escalonadorAberto != null) {
                if (this.escalonadorAberto.equals(aux)) {
                    this.fecharEdicao();
                }
                this.atualizarEscalonadores(this.escalonadores.listar());
            } else {
                this.atualizarEscalonadores(this.escalonadores.listar());
            }
        }
    }

    private void jButtonCompilarActionPerformed(final ActionEvent evt) {

        if (this.escalonadorAberto != null) {
            if (this.modificado) {
                this.escalonadores.escrever(this.escalonadorAberto,
                        this.jTextPane1.getText());
                this.salvarModificacao();
            }
            final String erros =
                    this.escalonadores.compilar(this.escalonadorAberto);
            if (erros != null) {
                JOptionPane.showMessageDialog(this, erros, "Erros encontrados"
                        , JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Escalonador" + this.escalonadorAberto +
                        "\nCompilador com sucesso");
            }
            this.atualizarEscalonadores(this.escalonadores.listar());
        }
    }

    private void jMenuItemAbrirActionPerformed(final ActionEvent evt) {

        int escolha = JOptionPane.YES_OPTION;
        if (this.modificado) {
            escolha = this.savarAlteracao();
        }
        if (escolha != JOptionPane.CANCEL_OPTION && escolha != JOptionPane.CLOSED_OPTION) {
            final BorderLayout chooserLayout =
                    (BorderLayout) this.jFileChooser1.getLayout();
            chooserLayout.getLayoutComponent(BorderLayout.NORTH).setVisible(false);
            //aqui está o X da questão ;D
            this.jFileChooser1.getComponent(0).setVisible(false);
            this.jFileChooser1.setCurrentDirectory(this.escalonadores.getDiretorio());
            escolha = this.jFileChooser1.showOpenDialog(this);
            if (escolha == JFileChooser.APPROVE_OPTION) {
                final File arquivo = this.jFileChooser1.getSelectedFile();
                if (arquivo != null) {
                    final String nome = arquivo.getName().substring(0,
                            arquivo.getName().length() - 5);
                    final String conteud = this.escalonadores.ler(nome);
                    //Adicionar ao editor
                    this.abrirEdicao(nome, conteud);
                }
            }
        }
    }

    private void jMenuItemImportarActionPerformed(final ActionEvent evt) {

        int escolha = JOptionPane.YES_OPTION;
        if (this.modificado) {
            escolha = this.savarAlteracao();
        }
        if (escolha != JOptionPane.CANCEL_OPTION && escolha != JOptionPane.CLOSED_OPTION) {
            final BorderLayout chooserLayout =
                    (BorderLayout) this.jFileChooser1.getLayout();
            chooserLayout.getLayoutComponent(BorderLayout.NORTH).setVisible(true);
            //aqui está o X da questão ;D
            this.jFileChooser1.getComponent(0).setVisible(true);
            this.jFileChooser1.setCurrentDirectory(null);
            this.jFileChooser1.showOpenDialog(this);
            if (escolha == JFileChooser.APPROVE_OPTION) {
                final File arquivo = this.jFileChooser1.getSelectedFile();
                if (arquivo != null) {
                    if (this.escalonadores.importarEscalonadorJava(arquivo)) {
                        this.atualizarEscalonadores(this.escalonadores.listar());
                        final String nome = arquivo.getName().substring(0,
                                arquivo.getName().length() - 5);
                        final String conteud = this.escalonadores.ler(nome);
                        //Adicionar ao editor
                        this.abrirEdicao(nome, conteud);
                    } else {
                        JOptionPane.showMessageDialog(this, "Falha na " +
                                                            "importação",
                                "Error!", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    void atualizarEscalonadores() {
        this.atualizarEscalonadores(this.escalonadores.listar());
    }

    private void atualizarEscalonadores(final List<String> escal) {
        this.jListEscalonadores.setListData(escal.toArray());
    }

    private int savarAlteracao() {
        final int escolha = JOptionPane.showConfirmDialog(this,
                this.translate("Do you want to save changes to") +
                " " + this.escalonadorAberto + ".java");
        if (escolha == JOptionPane.YES_OPTION) {
            this.escalonadores.escrever(this.escalonadorAberto,
                    this.jTextPane1.getText());
            this.salvarModificacao();
        }
        return escolha;
    }

    private void fecharEdicao() {
        this.setTitle(this.translate("Manage Schedulers"));
        this.escalonadorAberto = null;
        try {
            final AbstractDocument doc =
                    (TextEditorStyle) this.jTextPane1.getDocument();
            doc.remove(0, doc.getLength());
        } catch (final BadLocationException ex) {
        }
        this.jTextPane1.setEnabled(false);
        this.modificado = false;
    }

    private void abrirEdicao(final String nome, final String conteudo) {
        this.escalonadorAberto = nome;
        try {
            final AbstractDocument doc =
                    (TextEditorStyle) this.jTextPane1.getDocument();
            if (doc.getLength() > 0) {
                doc.remove(0, doc.getLength());
            }
            doc.insertString(0, conteudo, null);
        } catch (final BadLocationException ex) {
        }
        this.jTextPane1.setEnabled(true);
        this.salvarModificacao();
    }

    private void modificar() {
        this.setTitle(this.escalonadorAberto + ".java [" + this.translate(
                "modified") + "] - " + this.translate("Manage Schedulers"));
        this.modificado = true;
    }

    private void salvarModificacao() {
        this.setTitle(this.escalonadorAberto + ".java - " + this.translate(
                "Manage" +
                " " +
                "Schedulers"));
        this.modificado = false;
    }

    public ManipularArquivos getEscalonadores() {
        return this.escalonadores;
    }

    private class SomeWindowAdapter extends WindowAdapter {
        @Override
        public void windowClosing(final WindowEvent e) {
            if (ManageSchedulers.this.modificado) {
                final int escolha =
                        ManageSchedulers.this.savarAlteracao();
                if (escolha != JOptionPane.CANCEL_OPTION && escolha != JOptionPane.CLOSED_OPTION) {
                    ManageSchedulers.this.setVisible(false);
                }
            } else {
                ManageSchedulers.this.setVisible(false);
            }
        }
    }

    private class SomeDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(final DocumentEvent e) {
            if (!ManageSchedulers.this.modificado) {
                ManageSchedulers.this.modificar();
            }
        }

        @Override
        public void removeUpdate(final DocumentEvent e) {
            if (!ManageSchedulers.this.modificado) {
                ManageSchedulers.this.modificar();
            }
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {
        }
    }
}