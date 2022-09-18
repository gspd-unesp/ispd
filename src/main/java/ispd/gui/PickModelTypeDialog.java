package ispd.gui;

import ispd.gui.utils.ButtonBuilder;

import javax.swing.GroupLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PickModelTypeDialog extends JDialog {
    public static final int GRID = 0;
    public static final int IAAS = 1;
    public static final int PAAS = 2;
    private static final Font WINDOW_FONT =
            new Font("Tahoma", Font.PLAIN, 12);
    private final JRadioButton jRadioGrid;
    private final JRadioButton jRadioIaaS;
    private final JRadioButton jRadioPaaS;
    private int choice = 0;

    PickModelTypeDialog(final Frame owner, final boolean modal) {
        super(owner, modal);
        this.initWindowProperties();
        this.jRadioGrid = PickModelTypeDialog.configuredRadioButton(
                "Grid", this::gridButtonClicked);
        this.jRadioIaaS = PickModelTypeDialog.configuredRadioButton(
                "Cloud - IaaS", this::iaasButtonClicked);
        this.jRadioPaaS = PickModelTypeDialog.configuredRadioButton(
                "Cloud - PaaS", this::paasButtonClicked);
        this.jRadioGrid.setSelected(true);
        this.makeLayoutAndPack();
    }

    private void initWindowProperties() {
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setFont(PickModelTypeDialog.WINDOW_FONT);
    }

    private static JRadioButton configuredRadioButton(
            final String text,
            final ActionListener action) {
        final var button = new JRadioButton();
        button.setText(text);
        button.addActionListener(action);
        return button;
    }

    private void gridButtonClicked(final ActionEvent evt) {
        this.deselectAllButtons();
        this.jRadioGrid.setSelected(true);
    }

    private void iaasButtonClicked(final ActionEvent evt) {
        this.deselectAllButtons();
        this.jRadioIaaS.setSelected(true);
    }

    private void paasButtonClicked(final ActionEvent evt) {
        this.deselectAllButtons();
        this.jRadioPaaS.setSelected(true);
    }

    private void makeLayoutAndPack() {
        final var ok = ButtonBuilder.basicButton("OK!", this::onOkClick);
        final var title = new JLabel(
                "Choose the service that do you want to model");
        final var pickModelType = new JPanel();
        final var groupLayout = new GroupLayout(pickModelType);
        pickModelType.setLayout(groupLayout);

        groupLayout.setHorizontalGroup(
                groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING,
                                groupLayout.createSequentialGroup()
                                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(ok)
                                        .addGap(33, 33, 33))
                        .addGroup(groupLayout.createSequentialGroup()
                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(groupLayout.createSequentialGroup()
                                                .addGap(132, 132, 132)
                                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(this.jRadioGrid)
                                                        .addComponent(this.jRadioIaaS)
                                                        .addComponent(this.jRadioPaaS)))
                                        .addGroup(groupLayout.createSequentialGroup()
                                                .addGap(22, 22, 22)
                                                .addComponent(title)))
                                .addContainerGap(114, Short.MAX_VALUE))
        );

        groupLayout.setVerticalGroup(
                groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addContainerGap(21, Short.MAX_VALUE)
                                .addComponent(title)
                                .addGap(18, 18, 18)
                                .addComponent(this.jRadioGrid)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(this.jRadioIaaS)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(this.jRadioPaaS)
                                .addGap(18, 18, 18)
                                .addComponent(ok)
                                .addContainerGap())
        );

        final var layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(pickModelType,
                                        GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
        );

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(pickModelType,
                                        GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
        );

        this.pack();
    }

    private void deselectAllButtons() {
        this.jRadioGrid.setSelected(false);
        this.jRadioIaaS.setSelected(false);
        this.jRadioPaaS.setSelected(false);
    }

    private void onOkClick(final ActionEvent evt) {
        this.choice = this.getChoiceForSelectedButton();
        this.setVisible(false);
    }

    private int getChoiceForSelectedButton() {
        if (this.jRadioGrid.isSelected()) {
            return PickModelTypeDialog.GRID;
        }

        if (this.jRadioIaaS.isSelected()) {
            return PickModelTypeDialog.IAAS;
        }

        if (this.jRadioPaaS.isSelected()) {
            return PickModelTypeDialog.PAAS;
        }

        return this.choice;
    }

    int getEscolha() {
        return this.choice;
    }
}