package ispd.gui;

import ispd.gui.auxiliar.MultipleExtensionFileFilter;
import ispd.gui.auxiliar.TextEditorStyle;
import ispd.gui.utils.ButtonBuilder;
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
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
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
    private JScrollPane scrollPane;
    private JTextPane textPane;
    private boolean wasCurrentFileModified;
    private String openFileName;

    ManageAllocationPolicies() {
        this.initComponents();
        this.addWindowListener(new SomeWindowAdapter());

        this.configureTextEditor(this.textPane, this.scrollPane);
        this.fecharEdicao();
        this.configureTextPaneDoc();
        this.updatePolicyList();
    }

    private static boolean isDoubleClick(final MouseEvent evt) {
        return evt.getClickCount() == 2;
    }

    private static String getPolicyNameFromFile(final File file) {
        final var fullName = file.getName();
        return fullName.substring(0, fullName.length() - ".java".length());
    }

    private void configureTextEditor(
            final JTextPane textPane, final JScrollPane scrollPane) {
        final var editor = new TextEditorStyle();
        editor.configurarTextComponent(textPane);
        scrollPane.setRowHeaderView(editor.getLinhas());
        scrollPane.setColumnHeaderView(editor.getCursor());
    }

    private void configureTextPaneDoc() {
        final var doc = this.textPane.getDocument();
        doc.addUndoableEditListener(this::onUndoEvent);
        doc.addDocumentListener(new SomeDocumentListener());
    }

    private void onUndoEvent(final UndoableEditEvent evt) {
        if (!"style change".equals(evt.getEdit().getPresentationName())) {
            this.undo.addEdit(evt.getEdit());
        }
    }

    private void initComponents() {
        this.setNoFileTitle();
        this.setAlwaysOnTop(true);
        this.setFocusable(false);
        this.setIconImage(Toolkit.getDefaultToolkit()
                .getImage(this.getResource("imagens/Logo_iSPD_25.png"))
        );

        this.textPane = new JTextPane();
        this.scrollPane = new JScrollPane(this.textPane);

        this.fileChooser = new JFileChooser();
        this.fileChooser.setAcceptAllFileFilterUsed(false);
        this.fileChooser.setFileFilter(new MultipleExtensionFileFilter(this.translate(
                "Java Source Files (. java)"), ".java", true));

        this.policyList = new JList<>();
        this.policyList.setBorder(BorderFactory.createTitledBorder(null,
                this.translate("Scheduler"),
                TitledBorder.CENTER,
                TitledBorder.DEFAULT_POSITION
        ));
        this.policyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.policyList.addMouseListener(new SomeMouseAdapter());

        this.initMenuBar();
        this.makeLayout();
        this.pack();
    }

    private void initMenuBar() {
        this.setJMenuBar(this.makeMenuBar(
                this.makeMenu("File",
                        this.makeMenuItem("New",
                                "/ispd/gui/imagens/insert-object_1.png",
                                this::onNew, KeyEvent.VK_N,
                                "Creates a new scheduler"
                        ),
                        this.makeMenuItem("Open",
                                "/ispd/gui/imagens/document-open.png",
                                this::onOpen, KeyEvent.VK_O,
                                "Opens an existing scheduler"
                        ),
                        this.makeMenuItem("Save",
                                "/ispd/gui/imagens/document-save_1.png",
                                this::onSave, KeyEvent.VK_S,
                                "Save the open file"
                        ),
                        this.makeMenuItem("Import",
                                "/ispd/gui/imagens/document-import.png",
                                this::onImport, KeyEvent.VK_I
                        )
                ),
                this.makeMenu("Edit",
                        this.makeMenuItem("Undo",
                                "/ispd/gui/imagens/edit-undo.png",
                                this::onUndo, KeyEvent.VK_Z
                        ),
                        this.makeMenuItem("Redo",
                                "/ispd/gui/imagens/edit-redo.png",
                                this::onRedo, KeyEvent.VK_Y
                        ),
                        new JPopupMenu.Separator(),
                        this.makeMenuItem("Cut",
                                "/ispd/gui/imagens/edit-cut.png",
                                e -> this.textPane.cut(), KeyEvent.VK_X
                        ),
                        this.makeMenuItem("Copy",
                                "/ispd/gui/imagens/edit-copy.png",
                                e -> this.textPane.copy(), KeyEvent.VK_C
                        ),
                        this.makeMenuItem("Paste",
                                "/ispd/gui/imagens/edit-paste.png",
                                e -> this.textPane.paste(), KeyEvent.VK_P
                        ),
                        new JPopupMenu.Separator(),
                        this.makeMenuItem("Delete",
                                "/ispd/gui/imagens/edit-delete.png",
                                this::onDelete
                        )
                )
        ));
    }

    private void makeLayout() {
        final var toolBar = this.makeToolBar();
        final var policyListPanel = this.makePolicyListPanel();
        final var textEditorPanel = this.makeTextEditorPanel();
        final var caretPosLabel = new JLabel();

        final var layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(policyListPanel
                                                        ,
                                                        GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(textEditorPanel
                                                        ,
                                                        GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addContainerGap(904,
                                                        Short.MAX_VALUE)
                                                .addComponent(caretPosLabel))
                                        .addComponent(toolBar,
                                                GroupLayout.DEFAULT_SIZE, 904
                                                , Short.MAX_VALUE))
                                .addContainerGap())
        );

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(toolBar,
                                        GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(caretPosLabel,
                                                GroupLayout.Alignment.TRAILING)
                                        .addComponent(policyListPanel,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(textEditorPanel,
                                                GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );
    }

    private JPanel makeTextEditorPanel() {
        final var textEditorPanel = new JPanel();
        final var layout = new GroupLayout(textEditorPanel);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(this.scrollPane,
                                        GroupLayout.DEFAULT_SIZE,
                                        734, Short.MAX_VALUE)
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(this.scrollPane)
                                .addContainerGap())
        );
        textEditorPanel.setLayout(layout);
        return textEditorPanel;
    }

    private JPanel makePolicyListPanel() {
        final var policyListScrollPane = new JScrollPane(this.policyList);

        final var policyListPanel = new JPanel();
        final var policyListPanelLayout = new GroupLayout(policyListPanel);
        policyListPanelLayout.setHorizontalGroup(
                policyListPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(policyListPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(policyListScrollPane,
                                        GroupLayout.DEFAULT_SIZE,
                                        120, Short.MAX_VALUE)
                                .addContainerGap())
        );
        policyListPanelLayout.setVerticalGroup(
                policyListPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING,
                                policyListPanelLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(policyListScrollPane,
                                                GroupLayout.DEFAULT_SIZE,
                                                506, Short.MAX_VALUE)
                                        .addContainerGap())
        );
        policyListPanel.setLayout(policyListPanelLayout);
        return policyListPanel;
    }

    private JMenuBar makeMenuBar(final JMenu... menus) {
        final JMenuBar menuBar = new JMenuBar();

        for (final var menu : menus) {
            menuBar.add(menu);
        }

        return menuBar;
    }

    private JMenu makeMenu(final String name, final Component... items) {
        final var menu = new JMenu();
        menu.setText(this.translate(name));
//        menu.addActionListener(this::onSave);

        for (final var item : items) {
            menu.add(item);
        }

        return menu;
    }

    private JMenuItem makeMenuItem(
            final String itemName, final String imagePath,
            final ActionListener action, final int acceleratorKey,
            final String toolTip) {
        final var item = this.makeMenuItem(
                itemName, imagePath,
                action, acceleratorKey
        );
        item.setToolTipText(this.translate(toolTip));
        return item;
    }

    private JMenuItem makeMenuItem(
            final String itemName, final String imagePath,
            final ActionListener action, final int acceleratorKey) {
        final var item = this.makeMenuItem(itemName, imagePath, action);
        item.setAccelerator(KeyStroke.getKeyStroke(
                acceleratorKey,
                InputEvent.CTRL_DOWN_MASK
        ));
        return item;
    }

    private JMenuItem makeMenuItem(
            final String itemName, final String imagePath,
            final ActionListener action) {
        final var item = new JMenuItem();
        item.setIcon(new ImageIcon(this.getResource(imagePath)));
        item.setText(this.translate(itemName));
        item.addActionListener(action);
        return item;
    }

    private JToolBar makeToolBar() {
        final var tb = new JToolBar();
        tb.setRollover(true);

        tb.add(this.makeButton(
                "/ispd/gui/imagens/insert-object.png",
                "Creates a new scheduler", this::onNew));

        tb.add(this.makeButton(
                "/ispd/gui/imagens/document-save.png",
                "Save the open file", this::onSave
        ));

        tb.add(this.makeButton(
                "/ispd/gui/imagens/system-run.png",
                "Compile", this::onCompile
        ));

        return tb;
    }

    private JButton makeButton(
            final String iconPath, final String helpText,
            final ActionListener action) {
        final var translated = this.translate(helpText);
        return ButtonBuilder.aButton(new ImageIcon(this.getResource(
                        iconPath
                )), action)
                .withToolTip(translated)
//                .withAccessibleDescription(translated)
                .withCenterBottomTextPosition()
                .nonFocusable()
                .build();
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

    private void onUndo(final ActionEvent evt) {
        try {
            this.undo.undo();
        } catch (final CannotUndoException ignored) {
        }
    }

    private void onRedo(final ActionEvent evt) {
        try {
            this.undo.redo();
        } catch (final CannotRedoException ignored) {
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

    private void onSave(final ActionEvent evt) {
        if (this.openFileName != null && this.wasCurrentFileModified) {
            this.saveModifications();
        }
    }

    private void onDelete(final ActionEvent evt) {

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

    private void onCompile(final ActionEvent evt) {

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

    private void onOpen(final ActionEvent evt) {
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

    private void onImport(final ActionEvent evt) {
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
        this.policyList.setListData(this.policyManager.listar().toArray(String[]::new));
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
                this.openFileName, this.textPane.getText());
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
        this.setNoFileTitle();
        this.openFileName = null;

        try {
            final var doc = (TextEditorStyle) this.textPane.getDocument();
            doc.remove(0, doc.getLength());
        } catch (final BadLocationException ignored) {
        }

        this.textPane.setEnabled(false);
        this.wasCurrentFileModified = false;
    }

    private void setNoFileTitle() {
        this.setTitle(this.translate("Manage Schedulers"));
    }

    private void abrirEdicao(final String nome, final String conteudo) {
        this.openFileName = nome;
        try {
            final var doc =
                    (TextEditorStyle) this.textPane.getDocument();
            if (doc.getLength() > 0) {
                doc.remove(0, doc.getLength());
            }
            doc.insertString(0, conteudo, null);
        } catch (final BadLocationException ex) {
        }
        this.textPane.setEnabled(true);
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
            this.setAsPendingChanges();
        }

        @Override
        public void removeUpdate(final DocumentEvent e) {
            this.setAsPendingChanges();
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {
        }

        private void setAsPendingChanges() {
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
