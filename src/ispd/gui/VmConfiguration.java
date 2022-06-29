package ispd.gui;

import ispd.gui.iconico.grade.VirtualMachine;
import ispd.gui.utils.ButtonBuilder;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

class VmConfiguration extends JDialog {
    private static final String[] OPERATING_SYSTEMS = {
            "Linux", "Macintosh", "Windows" };
    private final JComboBox<String> osComboBox =
            VmConfiguration.configuredComboBox(
                    new DefaultComboBoxModel<>(VmConfiguration.OPERATING_SYSTEMS),
                    "Select the operational system hosted in the virtual " +
                    "machine",
                    this::jSOComboBoxActionPerformed);
    private final JSpinner disk = VmConfiguration.spinnerWithTooltip(
            "Insert the amount of disk that VM " +
            "allocates in resource's hard disk");
    private final JSpinner memory = VmConfiguration.spinnerWithTooltip(
            "Insert the amount of  memory that VM " +
            "allocates in the  resource's primary storage");
    private final JSpinner processors = VmConfiguration.spinnerWithTooltip(
            "Insert the number of virtual cores that VM " +
            "allocates in the resource's physical processor");
    private final Vector<String> tableColumns =
            new Vector<>(List.of(new String[] {
                    "VM Label",
                    "User",
                    "VMM",
                    "Proc alloc",
                    "Mem alloc",
                    "Disk alloc",
                    "OS",
            }));
    private JScrollPane tableScrollPane;
    private Vector<Vector<Object>> tableRows;
    private Vector<String> users;
    private JTable vmTable;
    private JComboBox<String> usersComboBox;
    private JComboBox<String> vmmsComboBox;
    private int tableIndex;
    private HashSet<VirtualMachine> virtualMachines;

    VmConfiguration(final Frame parent, final boolean modal,
                    final Object[] users, final Object[] vmms,
                    final HashSet<VirtualMachine> vmList) {
        super(parent, modal);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.initComponents(users, vmms, vmList);
        this.makeLayoutAndPack();
    }

    private void initComponents(
            final Object[] users, final Object[] vmms,
            final HashSet<VirtualMachine> vmList) {

        this.users = VmConfiguration.stringVectorFromObjectArray(users);

        this.fillTableAndVms(vmList);

        this.usersComboBox = VmConfiguration.configuredComboBox(
                new DefaultComboBoxModel<>(this.users),
                "Select the virtual machine owner",
                this::jUserComboBoxActionPerformed);

        this.vmmsComboBox = VmConfiguration.configuredComboBox(
                new DefaultComboBoxModel<>(VmConfiguration.stringVectorFromObjectArray(vmms)),
                "Select the VMM that coorditates the virtual machine",
                this::jVMMComboBoxActionPerformed);

        this.vmTable = new JTable(
                new DefaultTableModel(this.tableRows, this.tableColumns));
        this.tableScrollPane = new JScrollPane(this.vmTable);
    }

    private void makeLayoutAndPack() {

        final var ok = ButtonBuilder.aButton("OK!",
                        this::jButtonOKVmActionPerformed)
                .withToolTip("Apply configurations")
                .build();
        final var addUser = ButtonBuilder.aButton("Add User"
                        , this::jButtonAddUserActionPerformed)
                .withToolTip("Add a new user")
                .build();
        final var removeVm = ButtonBuilder.aButton("Remove " +
                                                   "VM",
                        this::jButtonRemoveVMActionPerformed)
                .withToolTip("Remove the virtual machine selected in the " +
                             "table below")
                .build();
        final var addVm = ButtonBuilder.aButton("Add VM",
                        this::jButtonAddVMActionPerformed)
                .withToolTip("Add the configured virtual machine")
                .build();

        final var vmm = new JLabel("VMM:");
        final var user = new JLabel("User:");
        final var vConfig = new JLabel("Virtual machines configuration:");
        final var vCores = new JLabel("Number of virtual cores:");
        final var vMemory = new JLabel("Memory Allocated (MB):");
        final var vDisk = new JLabel("Disk Allocated (GB):");
        final var vOS = new JLabel("Operational System:");

        final JPanel vmConfigPanel = new JPanel();
        final GroupLayout groupLayout = new GroupLayout(vmConfigPanel);
        vmConfigPanel.setLayout(groupLayout);
        groupLayout.setHorizontalGroup(
                groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(groupLayout.createSequentialGroup()
                                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(vConfig)
                                                        .addGroup(groupLayout.createSequentialGroup()
                                                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                                        .addGroup(groupLayout.createSequentialGroup()
                                                                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(this.usersComboBox, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
                                                                                        .addComponent(user))
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(vmm)
                                                                                        .addComponent(this.vmmsComboBox, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)))
                                                                        .addComponent(addUser, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                .addGap(36,
                                                                        36, 36)
                                                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addComponent(vDisk)
                                                                        .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                                                .addComponent(vCores, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                                .addComponent(this.processors)
                                                                                .addComponent(this.disk)))))
                                                .addGap(18, 18, 18)
                                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(this.memory, GroupLayout.PREFERRED_SIZE, 113, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(this.osComboBox, GroupLayout.PREFERRED_SIZE, 113, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(vMemory)
                                                        .addGroup(groupLayout.createSequentialGroup()
                                                                .addGap(10,
                                                                        10, 10)
                                                                .addComponent(vOS))))
                                        .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(this.tableScrollPane, GroupLayout.Alignment.LEADING)
                                                .addGroup(groupLayout.createSequentialGroup()
                                                        .addComponent(addVm,
                                                                GroupLayout.PREFERRED_SIZE, 240, GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(removeVm, GroupLayout.PREFERRED_SIZE, 240, GroupLayout.PREFERRED_SIZE)))
                                        .addComponent(ok,
                                                GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 82, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE))
        );

        groupLayout.setVerticalGroup(
                groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(vConfig)
                                .addGap(18, 18, 18)
                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(vCores)
                                        .addComponent(vMemory)
                                        .addComponent(user)
                                        .addComponent(vmm))
                                .addGap(7, 7, 7)
                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.processors,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(this.memory,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(this.usersComboBox,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(this.vmmsComboBox,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                                .addGap(7, 7, 7)
                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(vDisk)
                                        .addComponent(vOS))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(this.disk,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(this.osComboBox,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(addUser))
                                .addGap(18, 18, 18)
                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(addVm)
                                        .addComponent(removeVm))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(this.tableScrollPane,
                                        GroupLayout.PREFERRED_SIZE, 119,
                                        GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ok)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE))
        );

        final GroupLayout layout =
                new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(vmConfigPanel,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(vmConfigPanel,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
        );

        this.pack();
    }

    private static Vector<String> stringVectorFromObjectArray(final Object[] arr) {
        return Arrays.stream(arr)
                .map(o -> (String) o)
                .collect(Collectors.toCollection(Vector::new));
    }

    private void fillTableAndVms(final HashSet<VirtualMachine> vmList) {
        this.tableRows = new Vector<>(0);
        this.tableIndex = 0;

        if (vmList == null) {
            this.virtualMachines = new HashSet<>(0);
            return; // TODO: Was this refactor correct?
        }

        this.virtualMachines = vmList;

        for (final var aux : this.virtualMachines) {
            this.tableIndex++;
            this.tableRows.add(VmConfiguration.vmToVector(aux));
        }
    }

    private static JComboBox<String> configuredComboBox(
            final ComboBoxModel<String> model,
            final String toolTip,
            final ActionListener action) {
        final var box = new JComboBox<String>();
        box.setModel(model);
        box.setToolTipText(toolTip);
        box.addActionListener(action);
        return box;
    }

    private void jUserComboBoxActionPerformed(final ActionEvent evt) {
    }

    private void jVMMComboBoxActionPerformed(final ActionEvent evt) {
    }

    private void jButtonOKVmActionPerformed(final ActionEvent evt) {
        this.virtualMachines = this.tableRows.stream()
                .map(VmConfiguration::vmFromTableVector)
                .collect(Collectors.toCollection(HashSet::new));
        this.setVisible(false);
    }

    private void jButtonAddUserActionPerformed(final ActionEvent evt) {
        final var newUser = JOptionPane.showInputDialog(
                this,
                "Enter the name",
                "Add user",
                JOptionPane.QUESTION_MESSAGE
        );

        if (this.users.contains(newUser) || newUser.isEmpty()) {
            return;
        }

        this.users.add(newUser);
    }

    private void jButtonRemoveVMActionPerformed(final ActionEvent evt) {
        final int line = this.vmTable.getSelectedRow();
        if (line >= 0 && line < this.tableRows.size()) {
            this.tableRows.remove(line);
        }
        this.tableScrollPane.setViewportView(this.vmTable);
    }

    private void jButtonAddVMActionPerformed(final ActionEvent evt) {
        this.tableRows.add(this.newTableRow());
        this.tableScrollPane.setViewportView(this.vmTable);
    }

    private static Vector<Object> vmToVector(final VirtualMachine aux) {
        final Vector<Object> line = new Vector<>(7);
        line.add(aux.getName());
        line.add(aux.getProprietario());
        line.add(aux.getVmm());
        line.add(aux.getPoderComputacional());
        line.add(aux.getAllocatedMemory());
        line.add(aux.getAllocatedDisk());
        line.add(aux.getOs());
        return line;
    }

    private static VirtualMachine vmFromTableVector(final List<Object> line) {
        return new VirtualMachine(
                line.get(0).toString(),
                line.get(1).toString(),
                line.get(2).toString(),
                Integer.parseInt(line.get(3).toString()),
                Double.parseDouble(line.get(4).toString()),
                Double.parseDouble(line.get(5).toString()),
                line.get(6).toString()
        );
    }

    private Vector<Object> newTableRow() {
        final var row =
                new Vector<>(List.of(new Object[] {
                        "VM%d".formatted(this.tableIndex),
                        this.usersComboBox.getSelectedItem(),
                        this.vmmsComboBox.getSelectedItem(),
                        this.processors.getValue(),
                        this.memory.getValue(),
                        this.disk.getValue(),
                        this.osComboBox.getSelectedItem()
                }));
        this.tableIndex++;
        return row;
    }

    private static JSpinner spinnerWithTooltip(final String text) {
        final var spinner = new JSpinner();
        spinner.setToolTipText(text);
        return spinner;
    }

    private void jSOComboBoxActionPerformed(final ActionEvent evt) {
    }

    HashSet<String> atualizaUsuarios() {
        return new HashSet<>(this.users);
    }

    HashSet<VirtualMachine> getMaqVirtuais() {
        return this.virtualMachines;
    }
}