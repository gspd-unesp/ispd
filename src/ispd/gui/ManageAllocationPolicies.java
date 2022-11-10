package ispd.gui;

import ispd.gui.auxiliar.MultipleExtensionFileFilter;
import ispd.gui.auxiliar.TextEditorStyle;
import ispd.policy.PolicyManager;
import ispd.policy.managers.Alocadores;
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
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

class ManageAllocationPolicies extends JFrame {
    private final UndoableEdit undo = new UndoManager();
    private final PolicyManager policyManager = new Alocadores();
    private final ResourceBundle words =
            ResourceBundle.getBundle("ispd.idioma.Idioma", Locale.getDefault());
    private JFileChooser fileChooser;
    private JList<String> policyList;
    private JScrollPane jScrollPane2;
    private JTextPane jTextPane1;
    private boolean wasCurrentFileModified;
    private String openFileName;

    ManageAllocationPolicies() {
        this.initComponents();

        this.configureTextEditor();
        this.fecharEdicao();
        this.configureDoc();
        this.updatePolicyList();
        this.addWindowListener(new SomeWindowAdapter());
    }

    private static boolean isDoubleClick(final MouseEvent evt) {
        return evt.getClickCount() == 2;
    }

    private static String getPolicyNameFromFile(final File file) {
        final var fullName = file.getName();
        return fullName.substring(0, fullName.length() - ".java".length());
    }

    private void configureTextEditor() {
        final var javaStyle = new TextEditorStyle();
        javaStyle.configurarTextComponent(this.jTextPane1);
        this.jScrollPane2.setRowHeaderView(javaStyle.getLinhas());
        this.jScrollPane2.setColumnHeaderView(javaStyle.getCursor());
    }

    private void configureDoc() {
        final var doc = this.jTextPane1.getDocument();
        doc.addUndoableEditListener(this::onUndoEvent);
        doc.addDocumentListener(new SomeDocumentListener());
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
        this.policyList = new JList();
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
                "Java Source Files (. java)"), ".java", true));

        this.setTitle(this.translate("Manage Schedulers"));
        this.setAlwaysOnTop(true);
        this.setFocusable(false);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(this.getResource(
                "imagens/Logo_iSPD_25.png")));

        jToolBar1.setRollover(true);

        jButtonNovo.setIcon(new ImageIcon(this.getResource(
                "/ispd/gui/imagens/insert-object.png")));

        jButtonNovo.setToolTipText(this.translate("Creates a new scheduler"));
        jButtonNovo.setFocusable(false);
        jButtonNovo.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonNovo.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonNovo.addActionListener(this::onNew);
        jToolBar1.add(jButtonNovo);
        jButtonNovo.getAccessibleContext().setAccessibleDescription(this.translate("Creates a new scheduler"));

        jButtonSalvar.setIcon(new ImageIcon(this.getResource(
                "/ispd/gui/imagens/document-save.png")));
        jButtonSalvar.setToolTipText(this.translate("Save the open file"));
        jButtonSalvar.setFocusable(false);
        jButtonSalvar.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonSalvar.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonSalvar.addActionListener(this::jButtonSalvarActionPerformed);
        jToolBar1.add(jButtonSalvar);

        jButtonCompilar.setIcon(new ImageIcon(this.getResource(
                "/ispd/gui/imagens/system-run.png")));
        jButtonCompilar.setToolTipText(this.translate("Compile"));
        jButtonCompilar.setFocusable(false);
        jButtonCompilar.setHorizontalTextPosition(SwingConstants.CENTER);
        jButtonCompilar.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButtonCompilar.addActionListener(this::jButtonCompilarActionPerformed);
        jToolBar1.add(jButtonCompilar);

        this.policyList.setBorder(BorderFactory.createTitledBorder(null,
                this.translate("Scheduler"),
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.DEFAULT_POSITION));
        this.policyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.policyList.addMouseListener(new SomeMouseAdapter());

        jScrollPane3.setViewportView(this.policyList);
        this.jScrollPane2.setViewportView(this.jTextPane1);


        final var jPanelAlocadoresLayout = new GroupLayout(jPanelAlocadores);
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


        final var jPanelEditorTextoLayout = new GroupLayout(jPanelEditorTexto);
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
        jMenuItemNovo.addActionListener(this::onNew);
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

        final var layout = new GroupLayout(this.getContentPane());
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
                                        .addComponent(jPanelAlocadores,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
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

    private void onNew(final ActionEvent e) {
        int escolha = JOptionPane.YES_OPTION;
        if (this.wasCurrentFileModified) {
            escolha = this.onCloseCurrentModel();
        }

        if (escolha == JOptionPane.CANCEL_OPTION || escolha == JOptionPane.CLOSED_OPTION) {
            return;
        }

        final var options = new String[] {
                "Edit java class",
                "Generator schedulers"
        };

        final var result = (String) JOptionPane.showInputDialog(
                this,
                "Creating the scheduler with:",
                null,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (result == null) {
            return;
        }

        if (!result.equals(options[0])) {
            return;
        }

        final var name = JOptionPane.showInputDialog(
                this, "Enter the name of the scheduler"
        );

        final boolean isValidClassName =
                name != null && ValidaValores.validaNomeClasse(name);

        if (isValidClassName) {
            this.abrirEdicao(name, this.policyManager.getPolicyTemplate(name));
            this.setAsPendingChanges();
            return;
        }

        if (!result.equals(options[1])) {
            return;
        }

        final var ge = this.makePolicyGeneratorDialog();

        if (ge.getParse() == null) {
            return;
        }

        this.policyManager.escrever(
                ge.getParse().getNome(),
                ge.getParse().getCodigo()
        );

        final var compilationErrors =
                this.policyManager.compilar(ge.getParse().getNome());

        if (compilationErrors == null) {
            JOptionPane.showMessageDialog(
                    this,
                    """
                            Alocador%s
                            Compilador com sucesso""".formatted(this.openFileName)
            );
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    compilationErrors,
                    "Erros encontrados",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        this.updatePolicyList();
    }

    private CreateSchedulerDialog makePolicyGeneratorDialog() {
        final var ge = new CreateSchedulerDialog(
                this,
                true,
                this.policyManager.directory().getAbsolutePath(),
                this.words
        );

        ge.setLocationRelativeTo(this);
        ge.setVisible(true);

        return ge;
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

    private void jListAlocadoresMouseClicked(final MouseEvent e) {
        if (!ManageAllocationPolicies.isDoubleClick(e)) {
            return;
        }

        final var choice = this.wasCurrentFileModified
                ? this.onCloseCurrentModel()
                : JOptionPane.YES_OPTION;

        if (choice == JOptionPane.CANCEL_OPTION ||
            choice == JOptionPane.CLOSED_OPTION) {
            return;
        }

        this.openSelectedPolicy();
    }

    private void openSelectedPolicy() {
        final var result = (String) this.policyList.getSelectedValue();
        final String fileContents = this.policyManager.ler(result);
        this.abrirEdicao(result, fileContents);
    }

    private void jButtonSalvarActionPerformed(final ActionEvent evt) {
        if (this.openFileName != null && this.wasCurrentFileModified) {
            this.saveModifications();
        }
    }

    private void jMenuItemDeleteActionPerformed(final ActionEvent evt) {

        if (!this.policyList.isSelectionEmpty()) {
            final String aux =
                    this.policyList.getSelectedValue();
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
                if (!this.policyManager.remover(aux)) {
                    JOptionPane.showMessageDialog(this,
                            "Failed to remove " + aux);
                } else if (this.openFileName != null) {
                    if (this.openFileName.equals(aux)) {
                        this.fecharEdicao();
                    }
                    this.updatePolicyList();
                } else {
                    this.updatePolicyList();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "A scheduler should be " +
                                                "selected");
        }
    }

    private void jButtonCompilarActionPerformed(final ActionEvent evt) {

        if (this.openFileName != null) {
            if (this.wasCurrentFileModified) {
                this.saveModifications();
            }
            final String erros =
                    this.policyManager.compilar(this.openFileName);
            if (erros != null) {
                JOptionPane.showMessageDialog(this, erros, "Erros encontrados"
                        , JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Alocador" + this.openFileName + "\nCompilador com " +
                        "sucesso");
            }
            this.updatePolicyList();
        }
    }

    private void jMenuItemAbrirActionPerformed(final ActionEvent evt) {
        int escolha = JOptionPane.YES_OPTION;
        if (this.wasCurrentFileModified) {
            escolha = this.onCloseCurrentModel();
        }

        if (escolha == JOptionPane.CANCEL_OPTION || escolha == JOptionPane.CLOSED_OPTION) {
            return;
        }

        final var chooserLayout = (BorderLayout) this.fileChooser.getLayout();
        chooserLayout.getLayoutComponent(BorderLayout.NORTH).setVisible(false);

        this.fileChooser.getComponent(0).setVisible(false);
        this.fileChooser.setCurrentDirectory(this.policyManager.directory());

        final int choice2 = this.fileChooser.showOpenDialog(this);
        if (choice2 != JFileChooser.APPROVE_OPTION) {
            return;
        }

        final var file = this.fileChooser.getSelectedFile();

        if (file == null) {
            return;
        }

        final var name = ManageAllocationPolicies.getPolicyNameFromFile(file);
        final var code = this.policyManager.ler(name);
        this.abrirEdicao(name, code);
    }

    private void jMenuItemImportarActionPerformed(final ActionEvent evt) {
        int escolha = JOptionPane.YES_OPTION;
        if (this.wasCurrentFileModified) {
            escolha = this.onCloseCurrentModel();
        }

        if (escolha == JOptionPane.CANCEL_OPTION || escolha == JOptionPane.CLOSED_OPTION) {
            return;
        }

        final var chooserLayout = (BorderLayout) this.fileChooser.getLayout();
        chooserLayout.getLayoutComponent(BorderLayout.NORTH).setVisible(true);

        this.fileChooser.getComponent(0).setVisible(true);
        this.fileChooser.setCurrentDirectory(null);
        this.fileChooser.showOpenDialog(this);

        if (escolha != JFileChooser.APPROVE_OPTION) {
            return;
        }

        final var file = this.fileChooser.getSelectedFile();

        if (file == null) {
            return;
        }

        if (!this.policyManager.importJavaPolicy(file)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Falha na importação",
                    "Error!",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        this.updatePolicyList();
        final var nome = ManageAllocationPolicies.getPolicyNameFromFile(file);
        final var code = this.policyManager.ler(nome);
        this.abrirEdicao(nome, code);
    }

    void updatePolicyList() {
        this.policyList.setListData(
                this.policyManager.listar()
                        .toArray(String[]::new)
        );
    }

    private int onCloseCurrentModel() {
        final int choice = this.showConfirmSaveDialog();

        if (choice == JOptionPane.YES_OPTION) {
            this.saveModifications();
        }

        return choice;
    }

    private void saveModifications() {
        this.policyManager.escrever(
                this.openFileName, this.jTextPane1.getText());
        this.setAsNoPendingChanges();
    }

    private int showConfirmSaveDialog() {
        return JOptionPane.showConfirmDialog(this,
                "%s %s.java".formatted(
                        this.translate("Do you want to save changes to"),
                        this.openFileName
                )
        );
    }

    private void fecharEdicao() {
        this.setTitle(this.translate("Manage Schedulers"));
        this.openFileName = null;

        try {
            final var doc = (TextEditorStyle) this.jTextPane1.getDocument();
            doc.remove(0, doc.getLength());
        } catch (final BadLocationException ignored) {
        }

        this.jTextPane1.setEnabled(false);
        this.wasCurrentFileModified = false;
    }

    private void abrirEdicao(final String nome, final String conteudo) {
        this.openFileName = nome;
        try {
            final var doc =
                    (TextEditorStyle) this.jTextPane1.getDocument();
            if (doc.getLength() > 0) {
                doc.remove(0, doc.getLength());
            }
            doc.insertString(0, conteudo, null);
        } catch (final BadLocationException ex) {
        }
        this.jTextPane1.setEnabled(true);
        this.setAsNoPendingChanges();
    }

    private void setAsPendingChanges() {
        this.updateTitle(" [%s] ".formatted(this.translate("modified")));
        this.wasCurrentFileModified = true;
    }

    private void updateTitle(final String afterFileName) {
        this.setTitle("%s.java%s- %s".formatted(
                this.openFileName,
                afterFileName,
                this.translate("Manage Schedulers")
        ));
    }

    private void setAsNoPendingChanges() {
        this.updateTitle(" ");
        this.wasCurrentFileModified = false;
    }

    public PolicyManager getAlocadores() {
        return this.policyManager;
    }

    private class SomeDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(final DocumentEvent e) {
            this.setAsModified();
        }

        @Override
        public void removeUpdate(final DocumentEvent e) {
            this.setAsModified();
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {
        }

        private void setAsModified() {
            if (ManageAllocationPolicies.this.wasCurrentFileModified) {
                return;
            }

            ManageAllocationPolicies.this.setAsPendingChanges();
        }
    }

    private class SomeWindowAdapter extends WindowAdapter {
        @Override
        public void windowClosing(final WindowEvent e) {
            if (!ManageAllocationPolicies.this.wasCurrentFileModified) {
                ManageAllocationPolicies.this.setVisible(false);
                return;
            }

            final int choice =
                    ManageAllocationPolicies.this.onCloseCurrentModel();
            if (choice != JOptionPane.CANCEL_OPTION && choice != JOptionPane.CLOSED_OPTION) {
                ManageAllocationPolicies.this.setVisible(false);
            }
        }
    }

    private class SomeMouseAdapter extends MouseAdapter {
        public void mouseClicked(final MouseEvent evt) {
            ManageAllocationPolicies.this.jListAlocadoresMouseClicked(evt);
        }
    }
}
