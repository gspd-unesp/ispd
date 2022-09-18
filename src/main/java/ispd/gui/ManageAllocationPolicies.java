package ispd.gui;

import ispd.alocacaoVM.ManipularArquivosAlloc;
import ispd.arquivo.Alocadores;
import ispd.gui.auxiliar.TextEditorStyle;
import ispd.gui.auxiliar.MultipleExtensionFileFilter;
import ispd.utils.ValidaValores;

import javax.swing.BorderFactory;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;

class ManageAllocationPolicies extends JFrame {
    private final UndoableEdit undo = new UndoManager();
    private final ManipularArquivosAlloc allocators;
    private final ResourceBundle words =
            ResourceBundle.getBundle("ispd.idioma.Idioma", Locale.getDefault());
    private JFileChooser fileChooser;
    private JList allocatorList;
    private JScrollPane jScrollPane2;
    private JTextPane jTextPane1;
    private boolean wasCurrentFileModified;
    private String openAllocator;

    ManageAllocationPolicies() {
        //Inicia o editor
        this.initComponents();
        //Define a linguagem do editor
        final TextEditorStyle javaStyle = new TextEditorStyle();
        javaStyle.configurarTextComponent(this.jTextPane1);
        this.jScrollPane2.setRowHeaderView(javaStyle.getLinhas());
        this.jScrollPane2.setColumnHeaderView(javaStyle.getCursor());
        this.fecharEdicao();
        // Eventos de desfazer e refazer
        final Document doc = this.jTextPane1.getDocument();
        doc.addUndoableEditListener(this::onUndoEvent);
        // Evento verifica alterações
        doc.addDocumentListener(new SomeDocumentListener());
        this.allocators = new Alocadores();
        this.atualizarAlocadores(this.allocators.listar());
        this.addWindowListener(new SomeWindowAdapter());
    }

    private void onUndoEvent(final UndoableEditEvent evt) {
        if (!"style change".equals(evt.getEdit().getPresentationName())) {
            this.undo.addEdit(evt.getEdit());
        }
    }

    private void initComponents() {

        final JPopupMenu jPopupMenuTexto = new JPopupMenu();
        final JMenuItem jMenuItemCut1 = new JMenuItem();
        final JMenuItem jMenuItemCopy1 = new JMenuItem();
        final JMenuItem jMenuItemPaste1 = new JMenuItem();
        this.fileChooser = new JFileChooser();
        final JToolBar jToolBar1 = new JToolBar();
        final JButton jButtonNovo = new JButton();
        final JButton jButtonSalvar = new JButton();
        final JButton jButtonCompilar = new JButton();
        final JPanel jPanelAlocadores = new JPanel();
        final JScrollPane jScrollPane3 = new JScrollPane();
        this.allocatorList = new JList();
        final JPanel jPanelEditorTexto = new JPanel();
        this.jScrollPane2 = new JScrollPane();
        this.jTextPane1 = new JTextPane();
        final JLabel jLabelCaretPos = new JLabel();
        final JMenuBar jMenuBar1 = new JMenuBar();
        final JMenu jMenuArquivo = new JMenu();
        final JMenuItem jMenuItemNovo = new JMenuItem();
        final JMenuItem jMenuItemAbrir = new JMenuItem();
        final JMenuItem jMenuItemSalvar = new JMenuItem();
        final JMenuItem jMenuItemImportar = new JMenuItem();
        final JMenu jMenuEditar = new JMenu();
        final JMenuItem jMenuItemDesfazer = new JMenuItem();
        final JMenuItem jMenuItemRefazer = new JMenuItem();
        final JPopupMenu.Separator jSeparator1 = new JPopupMenu.Separator();
        final JMenuItem jMenuItemCut = new JMenuItem();
        final JMenuItem jMenuItemCopy = new JMenuItem();
        final JMenuItem jMenuItemPaste = new JMenuItem();
        final JPopupMenu.Separator jSeparator2 = new JPopupMenu.Separator();
        final JMenuItem jMenuItemDelete = new JMenuItem();

        jMenuItemCut1.setText(this.translate("Cut"));
        jMenuItemCut1.addActionListener(this::jMenuItemCutActionPerformed);
        jPopupMenuTexto.add(jMenuItemCut1);

        jMenuItemCopy1.setText(this.translate("Copy"));
        jMenuItemCopy1.addActionListener(this::jMenuItemCopyActionPerformed);
        jPopupMenuTexto.add(jMenuItemCopy1);

        jMenuItemPaste1.setText(this.translate("Paste"));
        jMenuItemPaste1.addActionListener(this::jMenuItemPasteActionPerformed);
        jPopupMenuTexto.add(jMenuItemPaste1);

        this.fileChooser.setAcceptAllFileFilterUsed(false);
        this.fileChooser.setFileFilter(new MultipleExtensionFileFilter(this.translate(
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
        jButtonNovo.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonNovo.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonNovo.addActionListener(this::jMenuItemNovojButtonNovoActionPerformed);
        jToolBar1.add(jButtonNovo);
        jButtonNovo.getAccessibleContext().setAccessibleDescription(this.translate("Creates a new scheduler"));

        jButtonSalvar.setIcon(new ImageIcon(this.getResource(
                "/ispd" +
                "/gui/imagens/document-save.png")));
        jButtonSalvar.setToolTipText(this.translate("Save the " +
                                                    "open file"));
        jButtonSalvar.setFocusable(false);
        jButtonSalvar.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonSalvar.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonSalvar.addActionListener(this::jButtonSalvarActionPerformed);
        jToolBar1.add(jButtonSalvar);

        jButtonCompilar.setIcon(new ImageIcon(this.getResource(
                "/ispd" +
                "/gui/imagens/system-run.png")));
        jButtonCompilar.setToolTipText(this.translate("Compile"));
        jButtonCompilar.setFocusable(false);
        jButtonCompilar.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonCompilar.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonCompilar.addActionListener(this::jButtonCompilarActionPerformed);
        jToolBar1.add(jButtonCompilar);

        this.allocatorList.setBorder(BorderFactory.createTitledBorder(null,
                this.translate("Scheduler"),
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.DEFAULT_POSITION));
        this.allocatorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.allocatorList.addMouseListener(new SomeMouseAdapter());
        jScrollPane3.setViewportView(this.allocatorList);

        final GroupLayout jPanelAlocadoresLayout =
                new GroupLayout(jPanelAlocadores);
        jPanelAlocadores.setLayout(jPanelAlocadoresLayout);
        jPanelAlocadoresLayout.setHorizontalGroup(
                jPanelAlocadoresLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelAlocadoresLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane3,
                                        GroupLayout.DEFAULT_SIZE,
                                        120, Short.MAX_VALUE)
                                .addContainerGap())
        );
        jPanelAlocadoresLayout.setVerticalGroup(
                jPanelAlocadoresLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING,
                                jPanelAlocadoresLayout.createSequentialGroup()
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

        jMenuItemNovo.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        final String name = "/ispd/gui/imagens/insert-object_1.png";
        jMenuItemNovo.setIcon(new ImageIcon(this.getResource(name)));
        jMenuItemNovo.setText(this.translate("New"));
        jMenuItemNovo.setToolTipText(this.translate("Creates a " +
                                                    "new " +
                                                    "scheduler"));
        jMenuItemNovo.addActionListener(this::jMenuItemNovojButtonNovoActionPerformed);
        jMenuArquivo.add(jMenuItemNovo);

        jMenuItemAbrir.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemAbrir.setIcon(new ImageIcon(this.getResource(
                "/ispd" +
                "/gui/imagens/document-open.png")));
        jMenuItemAbrir.setText(this.translate("Open"));
        jMenuItemAbrir.setToolTipText(this.translate("Opens an " +
                                                     "existing " +
                                                     "scheduler")); //

        jMenuItemAbrir.addActionListener(this::jMenuItemAbrirActionPerformed);
        jMenuArquivo.add(jMenuItemAbrir);

        jMenuItemSalvar.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemSalvar.setIcon(new ImageIcon(this.getResource(
                "/ispd" +
                "/gui/imagens/document-save_1.png")));
        jMenuItemSalvar.setText(this.translate("Save"));
        jMenuItemSalvar.setToolTipText(this.translate("Save the" +
                                                      " open " +
                                                      "file"));
        jMenuItemSalvar.addActionListener(this::jButtonSalvarActionPerformed);
        jMenuArquivo.add(jMenuItemSalvar);

        jMenuItemImportar.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemImportar.setIcon(new ImageIcon(this.getResource(
                "/ispd/gui/imagens/document-import.png")));
        jMenuItemImportar.setText(this.translate("Import"));
        jMenuItemImportar.addActionListener(this::jMenuItemImportarActionPerformed);
        jMenuArquivo.add(jMenuItemImportar);

        jMenuBar1.add(jMenuArquivo);

        jMenuEditar.setText(this.translate("Edit"));

        jMenuItemDesfazer.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemDesfazer.setIcon(new ImageIcon(this.getResource(
                "/ispd/gui/imagens/edit-undo.png")));
        jMenuItemDesfazer.setText(this.translate("Undo"));
        jMenuItemDesfazer.addActionListener(this::jMenuItemDesfazerActionPerformed);
        jMenuEditar.add(jMenuItemDesfazer);

        jMenuItemRefazer.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemRefazer.setIcon(new ImageIcon(this.getResource(
                "/ispd" +
                "/gui/imagens/edit-redo.png")));
        jMenuItemRefazer.setText(this.translate("Redo"));
        jMenuItemRefazer.addActionListener(this::jMenuItemRefazerActionPerformed);
        jMenuEditar.add(jMenuItemRefazer);
        jMenuEditar.add(jSeparator1);

        jMenuItemCut.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemCut.setIcon(new ImageIcon(this.getResource(
                "/ispd/gui" +
                "/imagens/edit-cut.png")));
        final String cut = "Cut";
        jMenuItemCut.setText(this.translate(cut));
        jMenuItemCut.addActionListener(this::jMenuItemCutActionPerformed);
        jMenuEditar.add(jMenuItemCut);

        jMenuItemCopy.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemCopy.setIcon(new ImageIcon(this.getResource(
                "/ispd" +
                "/gui/imagens/edit-copy.png")));
        jMenuItemCopy.setText(this.translate("Copy"));
        jMenuItemCopy.addActionListener(this::jMenuItemCopyActionPerformed);
        jMenuEditar.add(jMenuItemCopy);

        jMenuItemPaste.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
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
                                                .addComponent(jPanelAlocadores, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jPanelEditorTexto, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addContainerGap(904,
                                                        Short.MAX_VALUE)
                                                .addComponent(jLabelCaretPos))
                                        .addComponent(jToolBar1,
                                                GroupLayout.DEFAULT_SIZE, 904, Short.MAX_VALUE))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jToolBar1,
                                        GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabelCaretPos,
                                                GroupLayout.Alignment.TRAILING)
                                        .addComponent(jPanelAlocadores,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jPanelEditorTexto,
                                                GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );

        this.pack();
    }

    private String translate(final String cut) {
        return this.words.getString(cut);
    }

    private URL getResource(final String name) {
        return this.getClass().getResource(name);
    }

    private void jMenuItemNovojButtonNovoActionPerformed(final ActionEvent evt) {

        int escolha = JOptionPane.YES_OPTION;
        if (this.wasCurrentFileModified) {
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
                            "Enter" +
                            " the " +
                            "name " +
                            "of the scheduler");
                    boolean nomeOk = false;
                    if (result1 != null) {
                        nomeOk = ValidaValores.validaNomeClasse(result1);
                    }
                    if (nomeOk) {
                        //Carregar classe para esditar java
                        this.abrirEdicao(result1,
                                Alocadores.getAlocadorJava(result1));
                        this.modificar();
                    } else if (result.equals(ops[1])) {
                        //Carregar classe para construir alocador
                        // automaticamente
                        final CreateSchedulerDialog ge = new CreateSchedulerDialog(this
                                , true
                                ,
                                this.allocators.getDiretorio().getAbsolutePath()
                                , this.words);
                        ge.setLocationRelativeTo(this);
                        ge.setVisible(true);
                        if (ge.getParse() != null) {
                            this.allocators.escrever(ge.getParse().getNome(),
                                    ge.getParse().getCodigo());
                            final String erros =
                                    this.allocators.compilar(ge.getParse().getNome());
                            if (erros != null) {
                                JOptionPane.showMessageDialog(this, erros,
                                        "Erros encontrados",
                                        JOptionPane.ERROR_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(this, "Alocador"
                                                                    + this.openAllocator + "\nCompilador com sucesso");
                            }
                            this.atualizarAlocadores(this.allocators.listar());
                        }

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

    private void jListAlocadoresMouseClicked(final java.awt.event.MouseEvent evt) {


        if (evt.getClickCount() == 2) {
            int escolha = JOptionPane.YES_OPTION;
            if (this.wasCurrentFileModified) {
                escolha = this.savarAlteracao();
            }
            if (escolha != JOptionPane.CANCEL_OPTION && escolha != JOptionPane.CLOSED_OPTION) {
                //Alocador a ser aberto
                final String result =
                        (String) this.allocatorList.getSelectedValue();
                //Conteudo do arquivo
                final String conteud = this.allocators.ler(result);
                //Adicionar ao editor
                this.abrirEdicao(result, conteud);
            }
        }
    }

    private void jButtonSalvarActionPerformed(final ActionEvent evt) {

        if (this.openAllocator != null && this.wasCurrentFileModified) {
            this.allocators.escrever(this.openAllocator,
                    this.jTextPane1.getText());
            this.salvarModificacao();
        }
    }

    private void jMenuItemDeleteActionPerformed(final ActionEvent evt) {

        if (!this.allocatorList.isSelectionEmpty()) {
            final String aux =
                    this.allocatorList.getSelectedValue().toString();
            final int escolha = JOptionPane.showConfirmDialog(this, "Are you " +
                                                                    "sure " +
                                                                    "want " +
                                                                    "delete " +
                                                                    "this " +
                                                                    "scheduler" +
                                                                    ": \n" + aux,
                    null, JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (escolha == JOptionPane.YES_OPTION) {
                if (!this.allocators.remover(aux)) {
                    JOptionPane.showMessageDialog(this,
                            "Failed to remove " + aux);
                } else if (this.openAllocator != null) {
                    if (this.openAllocator.equals(aux)) {
                        this.fecharEdicao();
                    }
                    this.atualizarAlocadores(this.allocators.listar());
                } else {
                    this.atualizarAlocadores(this.allocators.listar());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "A scheduler should be " +
                                                "selected");
        }
    }

    private void jButtonCompilarActionPerformed(final ActionEvent evt) {

        if (this.openAllocator != null) {
            if (this.wasCurrentFileModified) {
                this.allocators.escrever(this.openAllocator,
                        this.jTextPane1.getText());
                this.salvarModificacao();
            }
            final String erros = this.allocators.compilar(this.openAllocator);
            if (erros != null) {
                JOptionPane.showMessageDialog(this, erros, "Erros encontrados"
                        , JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Alocador" + this.openAllocator + "\nCompilador com " +
                        "sucesso");
            }
            this.atualizarAlocadores(this.allocators.listar());
        }
    }

    private void jMenuItemAbrirActionPerformed(final ActionEvent evt) {

        int escolha = JOptionPane.YES_OPTION;
        if (this.wasCurrentFileModified) {
            escolha = this.savarAlteracao();
        }
        if (escolha != JOptionPane.CANCEL_OPTION && escolha != JOptionPane.CLOSED_OPTION) {
            final BorderLayout chooserLayout =
                    (BorderLayout) this.fileChooser.getLayout();
            chooserLayout.getLayoutComponent(BorderLayout.NORTH).setVisible(false);
            //aqui está o X da questão ;D
            this.fileChooser.getComponent(0).setVisible(false);
            this.fileChooser.setCurrentDirectory(this.allocators.getDiretorio());
            int choice2 = this.fileChooser.showOpenDialog(this);
            if (choice2 == JFileChooser.APPROVE_OPTION) {
                final File arquivo = this.fileChooser.getSelectedFile();
                if (arquivo != null) {
                    final String nome = arquivo.getName().substring(0,
                            arquivo.getName().length() - 5);
                    final String conteud = this.allocators.ler(nome);
                    //Adicionar ao editor
                    this.abrirEdicao(nome, conteud);
                }
            }
        }
    }

    private void jMenuItemImportarActionPerformed(final ActionEvent evt) {

        int escolha = JOptionPane.YES_OPTION;
        if (this.wasCurrentFileModified) {
            escolha = this.savarAlteracao();
        }
        if (escolha != JOptionPane.CANCEL_OPTION && escolha != JOptionPane.CLOSED_OPTION) {
            final BorderLayout chooserLayout =
                    (BorderLayout) this.fileChooser.getLayout();
            chooserLayout.getLayoutComponent(BorderLayout.NORTH).setVisible(true);
            //aqui está o X da questão ;D
            this.fileChooser.getComponent(0).setVisible(true);
            this.fileChooser.setCurrentDirectory(null);
            this.fileChooser.showOpenDialog(this);
            if (escolha == JFileChooser.APPROVE_OPTION) {
                final File arquivo = this.fileChooser.getSelectedFile();
                if (arquivo != null) {
                    if (this.allocators.importarAlocadoresJava(arquivo)) {
                        this.atualizarAlocadores(this.allocators.listar());
                        final String nome = arquivo.getName().substring(0,
                                arquivo.getName().length() - 5);
                        final String conteud = this.allocators.ler(nome);
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

    void atualizarAlocadores() {
        this.atualizarAlocadores(this.allocators.listar());
    }

    private void atualizarAlocadores(final Collection<String> escal) {
        this.allocatorList.setListData(escal.toArray());
    }

    private int savarAlteracao() {
        final int escolha = JOptionPane.showConfirmDialog(this,
                this.translate("Do you want to save changes to") + " " + this.openAllocator +
                ".java");
        if (escolha == JOptionPane.YES_OPTION) {
            this.allocators.escrever(this.openAllocator,
                    this.jTextPane1.getText());
            this.salvarModificacao();
        }
        return escolha;
    }

    private void fecharEdicao() {
        this.setTitle(this.translate("Manage Schedulers"));
        this.openAllocator = null;
        try {
            final TextEditorStyle doc =
                    (TextEditorStyle) this.jTextPane1.getDocument();
            doc.remove(0, doc.getLength());
        } catch (final BadLocationException ex) {
        }
        this.jTextPane1.setEnabled(false);
        this.wasCurrentFileModified = false;
    }

    private void abrirEdicao(final String nome, final String conteudo) {
        this.openAllocator = nome;
        try {
            final TextEditorStyle doc =
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
        this.setTitle(this.openAllocator + ".java [" + this.translate(
                "modified") + "] - " + this.translate("Manage " +
                                                      "Schedulers"));
        this.wasCurrentFileModified = true;
    }

    private void salvarModificacao() {
        this.setTitle(this.openAllocator + ".java - " + this.translate(
                "Manage " +
                "Schedulers"));
        this.wasCurrentFileModified = false;
    }

    public ManipularArquivosAlloc getAlocadores() {
        return this.allocators;
    }

    private class SomeDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(final DocumentEvent e) {
            if (!ManageAllocationPolicies.this.wasCurrentFileModified) {
                ManageAllocationPolicies.this.modificar();
            }
        }

        @Override
        public void removeUpdate(final DocumentEvent e) {
            if (!ManageAllocationPolicies.this.wasCurrentFileModified) {
                ManageAllocationPolicies.this.modificar();
            }
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {
        }
    }

    private class SomeWindowAdapter extends WindowAdapter {
        @Override
        public void windowClosing(final WindowEvent e) {
            if (ManageAllocationPolicies.this.wasCurrentFileModified) {
                final int escolha =
                        ManageAllocationPolicies.this.savarAlteracao();
                if (escolha != JOptionPane.CANCEL_OPTION && escolha != JOptionPane.CLOSED_OPTION) {
                    ManageAllocationPolicies.this.setVisible(false);
                }
            } else {
                ManageAllocationPolicies.this.setVisible(false);
            }
        }
    }

    private class SomeMouseAdapter extends java.awt.event.MouseAdapter {
        public void mouseClicked(final java.awt.event.MouseEvent evt) {
            ManageAllocationPolicies.this.jListAlocadoresMouseClicked(evt);
        }
    }
}
