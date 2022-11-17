package ispd.gui;

import ispd.arquivo.interpretador.gerador.InterpretadorGerador;
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
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
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
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

class ManageAllocationPolicies extends JFrame {
    private final NonThrowingUndoManager undoManager =
            new NonThrowingUndoManager();
    private final PolicyManager policyManager = new Alocadores();
    private final ResourceBundle words =
            ResourceBundle.getBundle("ispd.idioma.Idioma", Locale.getDefault());
    private final JFileChooser fileChooser = this.configuredFileChooser();
    private final JList<String> policyList = this.makePolicyList();
    private final JTextPane textPane =
            ManageAllocationPolicies.disabledTextPane();
    private final TextEditorStyle textEditor = new TextEditorStyle();
    private Optional<String> currentlyOpenFileName = Optional.empty();
    private boolean hasPendingChanges = false;

    /* package-private */
    ManageAllocationPolicies() {
        this.addWindowListener(new CancelableCloseWindowAdapter());
        this.setAlwaysOnTop(true);
        this.setFocusable(false);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(
                this.getResource("imagens/Logo_iSPD_25.png"))
        );

        this.configureMenuBar();
        this.configureTextEditor();

        this.makeLayout();
        this.pack();

        this.updateTitle();
        this.updatePolicyList();
    }

    private static JTextPane disabledTextPane() {
        final var tp = new JTextPane();
        tp.setEnabled(false);
        return tp;
    }

    private static String policyNameFromFile(final File file) {
        final var fullName = file.getName();
        return fullName.substring(0, fullName.length() - ".java".length());
    }

    private static JMenuBar makeMenuBarWith(final JMenu... menus) {
        final JMenuBar menuBar = new JMenuBar();

        for (final var menu : menus) {
            menuBar.add(menu);
        }

        return menuBar;
    }

    private void configureTextEditor() {
        this.textEditor.configurarTextComponent(this.textPane);

        final var doc = this.textPane.getDocument();
        doc.addUndoableEditListener(this::onUndoableEvent);
        doc.addDocumentListener(new PendingChangesDocListener());
    }

    private JFileChooser configuredFileChooser() {
        final var fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(new MultipleExtensionFileFilter(
                this.translate("Java Source Files (. java)"), ".java", true
        ));
        return fileChooser;
    }

    private String translate(final String cut) {
        return this.words.getString(cut);
    }

    private void configureMenuBar() {
        this.setJMenuBar(ManageAllocationPolicies.makeMenuBarWith(
                this.makeMenu("File",
                        this.makeMenuItem("New",
                                "/ispd/gui/imagens/insert-object_1.png",
                                this::onNew, KeyEvent.VK_N,
                                "Creates a new scheduler"
                        ),
                        this.makeMenuItem("Open",
                                "/ispd/gui/imagens/document-open.png",
                                evt1 -> this.runCancelableAction(this::openFile), KeyEvent.VK_O,
                                "Opens an existing scheduler"
                        ),
                        this.makeMenuItem("Save",
                                "/ispd/gui/imagens/document-save_1.png",
                                this::onSave, KeyEvent.VK_S,
                                "Save the open file"
                        ),
                        this.makeMenuItem("Import",
                                "/ispd/gui/imagens/document-import.png",
                                evt -> this.runCancelableAction(this::importFile), KeyEvent.VK_I
                        )
                ),
                this.makeMenu("Edit",
                        this.makeMenuItem("Undo",
                                "/ispd/gui/imagens/edit-undo.png",
                                this.undoManager::undo, KeyEvent.VK_Z
                        ),
                        this.makeMenuItem("Redo",
                                "/ispd/gui/imagens/edit-redo.png",
                                this.undoManager::redo, KeyEvent.VK_Y
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

    private JScrollPane makeEditorScrollPane() {
        final var scrollPane = new JScrollPane(this.textPane);
        scrollPane.setRowHeaderView(this.textEditor.getLinhas());
        scrollPane.setColumnHeaderView(this.textEditor.getCursor());
        return scrollPane;
    }

    private void onUndoableEvent(final UndoableEditEvent evt) {
        final var edit = evt.getEdit();
        if (!"style change".equals(edit.getPresentationName())) {
            this.undoManager.addEdit(edit);
        }
    }

    private JList<String> makePolicyList() {
        final var list = new JList<String>();
        list.setBorder(BorderFactory.createTitledBorder(null,
                this.translate("Scheduler"),
                TitledBorder.CENTER,
                TitledBorder.DEFAULT_POSITION
        ));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new PolicyListMouseAdapter());
        return list;
    }

    private void makeLayout() {
        final var toolBar = this.makeToolBar();
        final var policyListPanel = this.makePolicyListPane();
        final var textEditorPanel = this.makeEditorPanel();
        final var caretPosLabel = new JLabel();

        final var layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);

        layout.setHorizontalGroup(layout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout
                        .createSequentialGroup()
                        .addGroup(layout
                                .createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(layout
                                        .createSequentialGroup()
                                        .addComponent(policyListPanel,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(textEditorPanel,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE))
                                .addGroup(GroupLayout.Alignment.TRAILING, layout
                                        .createSequentialGroup()
                                        .addContainerGap(904, Short.MAX_VALUE)
                                        .addComponent(caretPosLabel))
                                .addComponent(toolBar, GroupLayout.DEFAULT_SIZE,
                                        904, Short.MAX_VALUE))
                        .addContainerGap()));

        layout.setVerticalGroup(layout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout
                        .createSequentialGroup()
                        .addComponent(toolBar,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addGroup(layout
                                .createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(caretPosLabel,
                                        GroupLayout.Alignment.TRAILING)
                                .addComponent(policyListPanel,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE)
                                .addComponent(textEditorPanel,
                                        GroupLayout.Alignment.TRAILING,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE))
                        .addContainerGap()));
    }

    private JPanel makeEditorPanel() {
        final var editorPane = this.makeEditorScrollPane();
        final var editorPanel = new JPanel();
        final var layout = new GroupLayout(editorPanel);

        layout.setHorizontalGroup(layout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(editorPane,
                                GroupLayout.DEFAULT_SIZE, 734, Short.MAX_VALUE)
                        .addContainerGap()));

        layout.setVerticalGroup(layout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(editorPane)
                        .addContainerGap())
        );

        editorPanel.setLayout(layout);
        return editorPanel;
    }

    private JPanel makePolicyListPane() {
        final var policyListScrollPane = new JScrollPane(this.policyList);

        final var policyListPanel = new JPanel();
        final var layout = new GroupLayout(policyListPanel);

        layout.setHorizontalGroup(layout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(policyListScrollPane,
                                GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                        .addContainerGap()));

        layout.setVerticalGroup(layout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(policyListScrollPane,
                                GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
                        .addContainerGap())
        );

        policyListPanel.setLayout(layout);
        return policyListPanel;
    }

    private JMenu makeMenu(final String name, final Component... items) {
        final var menu = new JMenu();
        menu.setText(this.translate(name));

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
        return ButtonBuilder.aButton(
                        new ImageIcon(this.getResource(iconPath)),
                        action
                )
                .withToolTip(this.translate(helpText))
                .withCenterBottomTextPosition()
                .nonFocusable()
                .build();
    }

    private URL getResource(final String name) {
        return this.getClass().getResource(name);
    }

    private void onNew(final ActionEvent e) {
        this.runCancelableAction(this::makeNewPolicy);
    }

    private void makeNewPolicy() {
        final var options = Map.<String, Runnable>of(
                "Edit java class", this::newPolicyFromCode,
                "Generator schedulers", this::newPolicyFromGenerator
        );

        final var result = (String) JOptionPane.showInputDialog(
                this,
                "Creating the scheduler with:",
                null,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options.keySet().toArray(String[]::new),
                null
        );

        Optional.ofNullable(result)
                .map(options::get)
                .ifPresent(Runnable::run);
    }

    private void newPolicyFromCode() {
        final var name = JOptionPane.showInputDialog(
                this, "Enter the name of the scheduler"
        );

        Optional.ofNullable(name)
                .filter(ValidaValores::isValidClassName)
                .ifPresent(this::openNewPolicyInEditor);
    }

    private void openNewPolicyInEditor(final String name) {
        this.fillEditorWithCode(
                name,
                this.policyManager.getPolicyTemplate(name)
        );
        this.setAsPendingChanges();
    }

    private void newPolicyFromGenerator() {
        this.getGeneratedPolicy()
                .ifPresent(this::saveAndCompileGeneratedPolicy);
    }

    private void saveAndCompileGeneratedPolicy(final InterpretadorGerador policy) {
        final var fileName = policy.getNome();
        this.policyManager.escrever(fileName, policy.getCodigo());
        this.tryCompileTarget(fileName);
    }

    private Optional<InterpretadorGerador> getGeneratedPolicy() {
        final var dialog = new CreateSchedulerDialog(
                this,
                true,
                this.policyManager.directory().getAbsolutePath(),
                this.words
        );

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return Optional.ofNullable(dialog.getParse());
    }

    private void tryCompileTarget(final String fileName) {
        final var errors = this.policyManager.compilar(fileName);

        if (errors != null) {
            JOptionPane.showMessageDialog(
                    this,
                    errors,
                    "Erros encontrados",
                    JOptionPane.ERROR_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    """
                            Alocador%s
                            Compilador com sucesso""".formatted(this.currentlyOpenFileName.get())
            );
        }

        this.updatePolicyList();
    }

    private void openSelectedPolicy() {
        final var name = (String) this.policyList.getSelectedValue();
        final var code = this.policyManager.ler(name);
        this.fillEditorWithCode(name, code);
    }

    private void fillEditorWithCode(final String fileName, final String code) {
        this.currentlyOpenFileName = Optional.of(fileName);
        this.setAsNoPendingChanges();

        this.clearTextPaneContents();

        try {
            this.textPane.getDocument()
                    .insertString(0, code, null);
        } catch (final BadLocationException ignored) {
        }

        this.textPane.setEnabled(true);
    }

    private void clearTextPaneContents() {
        final var doc = (TextEditorStyle) this.textPane.getDocument();

        try {
            doc.remove(0, doc.getLength());
        } catch (final BadLocationException ignored) {
        }
    }

    private void setAsNoPendingChanges() {
        this.hasPendingChanges = false;
        this.updateTitle();
    }

    private void updateTitle() {
        this.currentlyOpenFileName.ifPresentOrElse(
                this::updateTitleWithFileName,
                this::updateTitleWithNoFile
        );
    }

    private void updateTitleWithNoFile() {
        this.setTitle(this.getWindowTitle());
    }

    private void updateTitleWithFileName(final String fileName) {
        final var afterFileName = this.hasPendingChanges
                ? " [%s] ".formatted(this.translate("modified"))
                : " ";

        this.setTitle("%s.java%s- %s".formatted(
                fileName, afterFileName,
                this.getWindowTitle()
        ));
    }

    private String getWindowTitle() {
        return this.translate("Manage Schedulers");
    }

    private void onSave(final ActionEvent evt) {
        if (this.currentlyOpenFileName.isPresent() && this.hasPendingChanges) {
            this.savePendingChanges();
        }
    }

    private void onDelete(final ActionEvent evt) {
        if (this.policyList.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "A scheduler should be selected"
            );
            return;
        }

        final var selected = this.policyList.getSelectedValue();

        final int choice = JOptionPane.showConfirmDialog(
                this,
                """
                        Are you sure want delete this scheduler:
                        %s""".formatted(selected),
                null,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        if (!this.policyManager.remover(selected)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to remove %s".formatted(selected)
            );
            return;
        }

        if (selected.equals(this.currentlyOpenFileName.get())) {
            this.closeEditing();
        }

        this.updatePolicyList();
    }

    private void onCompile(final ActionEvent evt) {
        if (this.currentlyOpenFileName.isEmpty()) {
            return;
        }

        if (this.hasPendingChanges) {
            this.savePendingChanges();
        }

        this.tryCompileTarget(this.currentlyOpenFileName.get());
    }

    private void runCancelableAction(final Runnable action) {
        final var choice = this.hasPendingChanges
                ? this.askAboutPendingChanges()
                : JOptionPane.YES_OPTION;

        switch (choice) {
            case JOptionPane.CANCEL_OPTION,
                    JOptionPane.CLOSED_OPTION:
                return;
            default:
                action.run();
        }
    }

    private void openFile() {
        this.configureFileChooser(false, this.policyManager.directory());
        this.getFileChoice()
                .ifPresent(this::openChosenFile);
    }

    private void openChosenFile(final File file) {
        final var name =
                ManageAllocationPolicies.policyNameFromFile(file);
        final var code = this.policyManager.ler(name);
        this.fillEditorWithCode(name, code);
    }

    private void importFile() {
        this.configureFileChooser(true, null);
        this.getFileChoice()
                .ifPresent(this::importFile);
    }

    private void importFile(final File f) {
        if (!this.policyManager.importJavaPolicy(f)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Falha na importação",
                    "Error!",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        this.updatePolicyList();
        this.openChosenFile(f);
    }

    private Optional<File> getFileChoice() {
        final int fileChoice = this.fileChooser.showOpenDialog(this);

        if (fileChoice != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }

        final var file = this.fileChooser.getSelectedFile();
        return Optional.ofNullable(file);
    }

    private void configureFileChooser(
            final boolean showTopBorder, final File startDirectory) {
        ((BorderLayout) this.fileChooser.getLayout())
                .getLayoutComponent(BorderLayout.NORTH)
                .setVisible(showTopBorder);

        this.fileChooser.getComponent(0).setVisible(showTopBorder);
        this.fileChooser.setCurrentDirectory(startDirectory);
    }

    void updatePolicyList() {
        this.policyList.setListData(this.policyManager.listar().toArray(String[]::new));
    }

    private int askAboutPendingChanges() {
        final int choice = JOptionPane.showConfirmDialog(
                this,
                "%s %s.java".formatted(
                        this.translate("Do you want to save changes to"),
                        this.currentlyOpenFileName.get()
                )
        );

        if (choice == JOptionPane.YES_OPTION) {
            this.savePendingChanges();
        }

        return choice;
    }

    private void savePendingChanges() {
        this.policyManager.escrever(
                this.currentlyOpenFileName.get(),
                this.textPane.getText()
        );
        this.setAsNoPendingChanges();
    }

    private void closeEditing() {
        this.currentlyOpenFileName = Optional.empty();
        this.hasPendingChanges = false;
        this.updateTitle();

        this.clearTextPaneContents();

        this.textPane.setEnabled(false);
    }

    private void setAsPendingChanges() {
        this.hasPendingChanges = true;
        this.updateTitle();
    }

    /* package-private */
    PolicyManager getAlocadores() {
        return this.policyManager;
    }

    private static class NonThrowingUndoManager extends UndoManager {
        private void undo(final ActionEvent evt) {
            try {
                this.undo();
            } catch (final CannotUndoException ignored) {
            }
        }

        private void redo(final ActionEvent evt) {
            try {
                this.redo();
            } catch (final CannotRedoException ignored) {
            }
        }
    }

    private class PendingChangesDocListener implements DocumentListener {
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
            if (ManageAllocationPolicies.this.hasPendingChanges) {
                return;
            }

            ManageAllocationPolicies.this.setAsPendingChanges();
        }
    }

    private class CancelableCloseWindowAdapter extends WindowAdapter {
        @Override
        public void windowClosing(final WindowEvent e) {
            ManageAllocationPolicies.this.runCancelableAction(this::closeWindow);
        }

        private void closeWindow() {
            ManageAllocationPolicies.this.setVisible(false);
        }
    }

    private class PolicyListMouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(final MouseEvent evt) {
            if (!PolicyListMouseAdapter.isDoubleClick(evt)) {
                return;
            }

            ManageAllocationPolicies.this.runCancelableAction(
                    ManageAllocationPolicies.this::openSelectedPolicy);
        }

        private static boolean isDoubleClick(final MouseEvent evt) {
            return evt.getClickCount() == 2;
        }
    }
}
