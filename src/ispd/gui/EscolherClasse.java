package ispd.gui;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EscolherClasse extends JDialog {
    public static final int GRID = 0;
    public static final int IAAS = 1;
    public static final int PAAS = 2;
    private static final Font WINDOW_FONT = new Font("Tahoma", 0, 12);
    private final JRadioButton jRadioGrid;
    private final JRadioButton jRadioIaaS;
    private final JRadioButton jRadioPaaS;
    private int choice = 0;

    EscolherClasse(final Frame owner, final boolean modal) {
        super(owner, modal);
        this.initWindowProperties();
        this.jRadioGrid = EscolherClasse.configuredRadioButton(
                "Grid", this::gridButtonClicked);
        this.jRadioIaaS = EscolherClasse.configuredRadioButton(
                "Cloud - IaaS", this::iaasButtonClicked);
        this.jRadioPaaS = EscolherClasse.configuredRadioButton(
                "Cloud - PaaS", this::paasButtonClicked);
        this.jRadioGrid.setSelected(true);
        this.makeLayoutAndPack();
    }

    private void initWindowProperties() {
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setFont(EscolherClasse.WINDOW_FONT);
    }

    private void makeLayoutAndPack() {
        final var ok = EscolherClasse.configuredButton(
                "OK!", this::okButtonClicked);
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

    private static JButton configuredButton(
            final String text,
            final ActionListener action) {
        final var button = new JButton(text);
        button.addActionListener(action);
        return button;
    }

    private void okButtonClicked(final ActionEvent evt) {
        this.choice = this.getChoiceForSelectedButton();
        this.setVisible(false);
    }

    private void deselectAllButtons() {
        this.jRadioGrid.setSelected(false);
        this.jRadioIaaS.setSelected(false);
        this.jRadioPaaS.setSelected(false);
    }

    private int getChoiceForSelectedButton() {
        if (this.jRadioGrid.isSelected()) {
            return EscolherClasse.GRID;
        }

        if (this.jRadioIaaS.isSelected()) {
            return EscolherClasse.IAAS;
        }

        if (this.jRadioPaaS.isSelected()) {
            return EscolherClasse.PAAS;
        }

        return this.choice;
    }

    int getEscolha() {
        return this.choice;
    }
}