package ispd.gui;

import ispd.gui.utils.ButtonBuilder;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

/**
 * Window to add and remove users from modeled simulation.
 */
public class UserConfigurationDialog extends JDialog {
    private static final Dimension BUTTON_PREFERRED_SIZE =
            new Dimension(45, 45);
    private final ResourceBundle translator;
    private final Vector<Vector> users;
    private final Vector<String> profiles;
    private final Set<? super String> userSet;
    private JScrollPane jScrollPane1;
    private JTable table;

    UserConfigurationDialog(final Frame parent, final boolean modal,
                            final Set<? super String> users,
                            final ResourceBundle translator,
                            final Map<String, Double> profiles) {
        super(parent, modal);

        this.users = new Vector<>(0);

        this.profiles = new Vector<>(0);
        this.profiles.add("Users");
        this.profiles.add(
                "Limite de Consumo (Porcentagem do consumo da porção)");

        for (final var user : users) {
            final Vector userAndDouble = new Vector<String>(0);
            userAndDouble.add(user);
            userAndDouble.add(profiles.get(user));
            this.users.add(userAndDouble);
        }

        this.userSet = users;
        this.translator = translator;
        this.initComponents();
    }

    private void initComponents() {

        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setTitle(this.translate("Manage Users"));
        this.setResizable(false);

        final JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(ButtonBuilder
                .aButton(this.translate("Add"), this::onAddClick)
                .nonFocusable()
                .withIcon(new ImageIcon(this.getResource(
                        "/ispd/gui/imagens/insert-object.png")))
                .withSize(UserConfigurationDialog.BUTTON_PREFERRED_SIZE)
                .withCenterBottomTextPosition()
                .build());

        toolbar.add(ButtonBuilder
                .aButton(this.translate("Remove"), this::onRemoveClick)
                .nonFocusable()
                .withIcon(new ImageIcon(this.getResource(
                        "/ispd/gui/imagens/window-close.png")))
                .withSize(UserConfigurationDialog.BUTTON_PREFERRED_SIZE)
                .withCenterBottomTextPosition()
                .build());

        final var jButtonOk = ButtonBuilder.basicButton(
                this.translate("OK"), (e) -> this.setVisible(false));

        this.table = new JTable();
        this.table.setModel(new SomeDefaultTableModel(
                this.users, this.profiles));

        this.jScrollPane1 = new JScrollPane();
        this.jScrollPane1.setViewportView(this.table);

        final var layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(this.jScrollPane1,
                                                GroupLayout.PREFERRED_SIZE, 0
                                                , Short.MAX_VALUE)
                                        .addComponent(toolbar,
                                                GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 145, Short.MAX_VALUE)
                                                .addComponent(jButtonOk,
                                                        GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
                                                .addGap(145, 145, 145)))
                                .addContainerGap())
        );

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(toolbar,
                                        GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(this.jScrollPane1,
                                        GroupLayout.DEFAULT_SIZE,
                                        213, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonOk)
                                .addContainerGap())
        );

        this.pack();
    }

    private String translate(final String word) {
        return this.translator.getString(word);
    }

    private void onAddClick(final ActionEvent evt) {
        final String add = JOptionPane.showInputDialog(
                this,
                this.translate("Enter the name"),
                this.translate("Add"),
                JOptionPane.QUESTION_MESSAGE
        );

        // TODO: Parse result and error.
        final Double result =
                Double.parseDouble(JOptionPane.showInputDialog(this,
                "Enter user power comsumption limit"));

        if (!this.userSet.contains(add) && !add.isEmpty()) {
            this.userSet.add(add);
            final Vector user = new Vector<String>();
            user.add(add);
            user.add(result);
            this.users.add(user);
            this.jScrollPane1.setViewportView(this.table);
        }
    }

    private URL getResource(final String name) {
        return this.getClass().getResource(name);
    }

    private void onRemoveClick(final ActionEvent evt) {
        if (this.table.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this,
                    this.translate("A user should be selected"));
            return;
        }

        final var profile =
                this.table.getValueAt(this.table.getSelectedRow(), 0).toString();
        final int choice = JOptionPane.showConfirmDialog(
                this,
                "%s%s".formatted(this.translate(
                        "Are you sure want delete this user:"), profile),
                null,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        this.userSet.remove(profile);
        this.users.remove(this.table.getSelectedRow());
        this.jScrollPane1.setViewportView(this.table);
    }

    public HashSet<String> getUsuarios() {
        final HashSet<String> ret = new HashSet<>(0);
        for (final List userList : this.users) {
            ret.add(userList.get(0).toString());
        }
        return ret;
    }

    HashMap<String, Double> getLimite() {
        // TODO: Check and throw
        final HashMap<String, Double> ret = new HashMap<>(0);
        for (final List userList : this.users) {
            ret.put(userList.get(0).toString(),
                    Double.parseDouble(userList.get(1).toString()));
        }
        return ret;
    }

    private static class SomeDefaultTableModel extends DefaultTableModel {
        SomeDefaultTableModel(
                final Vector<Vector> users,
                final Vector<String> profiles) {
            super(users, profiles);
        }

        @Override
        public boolean isCellEditable(
                final int rowIndex,
                final int columnIndex) {
            return true;
        }
    }
}