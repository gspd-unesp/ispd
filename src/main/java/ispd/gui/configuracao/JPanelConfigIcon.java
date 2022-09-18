package ispd.gui.configuracao;

import ispd.alocacaoVM.ManipularArquivosAlloc;
import ispd.escalonador.ManipularArquivos;
import ispd.escalonadorCloud.ManipularArquivosCloud;
import ispd.gui.PickModelTypeDialog;
import ispd.gui.iconico.grade.Cluster;
import ispd.gui.iconico.grade.Internet;
import ispd.gui.iconico.grade.GridItem;
import ispd.gui.iconico.grade.Link;
import ispd.gui.iconico.grade.Machine;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableModel;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Supplier;

public class JPanelConfigIcon extends JPanel {

    private static final int ROW_HEIGHT = 20;
    private static final Font TAHOMA_FONT_BOLD = new Font("Tahoma", 1, 12);
    private final JLabel jLabelIconName = new JLabel(
            "Configuration for the icon # 0");
    private final JLabel jLabelTitle = JPanelConfigIcon.makeTitleLabel();
    private final JScrollPane jScrollPane = new JScrollPane();
    private ResourceBundle words = ResourceBundle.getBundle(
            "ispd.idioma.Idioma", new Locale("en", "US"));
    private final VariedRowTable machineTable = this.createTableWith(
            MachineVariedRowTable::new,
            MachineTable::new);
    private final VariedRowTable iassMachineTable = this.createTableWith(
            IaasMachineVariedRowTable::new,
            MachineTableIaaS::new);
    private final VariedRowTable clusterTable = this.createTableWith(
            ClusterVariedRowTable::new,
            ClusterTable::new);
    private final VariedRowTable iassClusterTable = this.createTableWith(
            IaasClusterVariedRowTable::new,
            ClusterTableIaaS::new);
    private final VariedRowTable linkTable = this.createTableWith(
            LinkVariedRowTable::new,
            LinkTable::new);
    private ManipularArquivos schedulers = null;
    private ManipularArquivosCloud cloudSchedulers = null;
    private ManipularArquivosAlloc allocators = null;

    public JPanelConfigIcon() {
        this.setLayout();
    }

    private void setLayout() {
        final var layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(this.jScrollPane,
                                GroupLayout.PREFERRED_SIZE, 0,
                                Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(this.jLabelTitle,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(this.jLabelIconName,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(this.jLabelTitle)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(this.jLabelIconName)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(this.jScrollPane,
                                        GroupLayout.DEFAULT_SIZE,
                                        158, Short.MAX_VALUE))
        );
    }

    private static JLabel makeTitleLabel() {
        final JLabel label = new JLabel(
                "Machine icon configuration");
        label.setFont(JPanelConfigIcon.TAHOMA_FONT_BOLD);
        return label;
    }

    private VariedRowTable createTableWith(
            final Supplier<? extends VariedRowTable> makeTable,
            final Function<? super ResourceBundle, ? extends TableModel> makeModel) {
        final var t = makeTable.get();
        t.setModel(makeModel.apply(this.words));
        t.setRowHeight(JPanelConfigIcon.ROW_HEIGHT);
        return t;
    }

    public void setEscalonadores(final ManipularArquivos schedulers) {
        this.schedulers = schedulers;
        schedulers.listar().forEach(sch -> {
            this.getTabelaMaquina().getEscalonadores().addItem(sch);
            this.getTabelaCluster().getEscalonadores().addItem(sch);
        });
    }

    private MachineTable getTabelaMaquina() {
        return (MachineTable) this.machineTable.getModel();
    }

    private ClusterTable getTabelaCluster() {
        return (ClusterTable) this.clusterTable.getModel();
    }

    public void setEscalonadoresCloud(final ManipularArquivosCloud cloudSchedulers) {
        this.cloudSchedulers = cloudSchedulers;
        cloudSchedulers.listar().forEach(sch -> {
            this.getTabelaMaquinaIaaS().getEscalonadores().addItem(sch);
            this.getTabelaClusterIaaS().getEscalonadores().addItem(sch);
        });
    }

    private MachineTableIaaS getTabelaMaquinaIaaS() {
        return (MachineTableIaaS) this.iassMachineTable.getModel();
    }

    private ClusterTableIaaS getTabelaClusterIaaS() {
        return (ClusterTableIaaS) this.iassClusterTable.getModel();
    }

    public void setAlocadores(final ManipularArquivosAlloc allocators) {
        this.allocators = allocators;
        allocators.listar().forEach(alloc -> {
            this.getTabelaMaquinaIaaS().getAlocadores().addItem(alloc);
            this.getTabelaClusterIaaS().getAlocadores().addItem(alloc);
        });
    }

    public void setIcone(final GridItem icon) {
        if (icon instanceof Link) {
            String text = this.translate(
                    "Network icon configuration");
            this.jLabelTitle.setText(text);
            System.out.printf("%s - %s%n", this.words.getLocale(), text);
        } else if (icon instanceof Internet) {
            this.jLabelTitle.setText(this.translate(
                    "Internet icon configuration"));
        }
        this.jLabelIconName.setText("%s#: %d".formatted(this.translate(
                "Configuration for the icon"), icon.getId().getGlobalId()));
        this.getTabelaLink().setLink(icon);
        this.jScrollPane.setViewportView(this.linkTable);
    }

    private String translate(final String text) {
        return this.words.getString(text);
    }

    private LinkTable getTabelaLink() {
        return (LinkTable) this.linkTable.getModel();
    }

    public void setIcone(
            final GridItem icon,
            final Iterable<String> users,
            final int choice) {
        if (choice == PickModelTypeDialog.GRID) {
            if (!this.schedulers.listarRemovidos().isEmpty()) {
                for (final Object escal : this.schedulers.listarRemovidos()) {
                    this.getTabelaMaquina().getEscalonadores().removeItem(escal);

                }
                this.schedulers.listarRemovidos().clear();
            }
            if (!this.schedulers.listarAdicionados().isEmpty()) {
                for (final Object escal : this.schedulers.listarAdicionados()) {
                    this.getTabelaMaquina().getEscalonadores().addItem(escal);
                }
                this.schedulers.listarAdicionados().clear();
            }
            this.jLabelIconName.setText("%s#: %d".formatted(this.translate(
                    "Configuration for the icon"), icon.getId().getGlobalId()));
            if (icon instanceof Machine) {
                this.jLabelTitle.setText(this.translate(
                        "Machine icon configuration"));
                this.getTabelaMaquina().setMaquina((Machine) icon, users);
                this.jScrollPane.setViewportView(this.machineTable);
            }
            if (icon instanceof Cluster) {
                this.jLabelTitle.setText(this.translate(
                        "Cluster icon configuration"));
                this.getTabelaCluster().setCluster((Cluster) icon, users);
                this.jScrollPane.setViewportView(this.clusterTable);
            }
        } else if (choice == PickModelTypeDialog.IAAS) {
            if (!this.cloudSchedulers.listarRemovidos().isEmpty()) {
                for (final Object escal :
                        this.cloudSchedulers.listarRemovidos()) {
                    this.getTabelaMaquinaIaaS().getEscalonadores().removeItem(escal);
                    this.getTabelaClusterIaaS().getEscalonadores().removeItem(escal);
                }
                this.cloudSchedulers.listarRemovidos().clear();
            }
            if (!this.cloudSchedulers.listarAdicionados().isEmpty()) {
                for (final Object escal :
                        this.cloudSchedulers.listarAdicionados()) {
                    this.getTabelaMaquinaIaaS().getEscalonadores().addItem(escal);
                    this.getTabelaClusterIaaS().getEscalonadores().addItem(escal);
                }
                this.cloudSchedulers.listarAdicionados().clear();
            }

            if (!this.allocators.listarRemovidos().isEmpty()) {
                for (final Object alloc : this.allocators.listarRemovidos()) {
                    this.getTabelaMaquinaIaaS().getAlocadores().removeItem(alloc);
                    this.getTabelaClusterIaaS().getAlocadores().removeItem(alloc);
                }
                this.allocators.listarRemovidos().clear();
            }
            if (!this.allocators.listarAdicionados().isEmpty()) {
                for (final Object alloc : this.allocators.listarAdicionados()) {
                    this.getTabelaMaquinaIaaS().getAlocadores().addItem(alloc);
                    this.getTabelaClusterIaaS().getAlocadores().addItem(alloc);
                }
                this.allocators.listarAdicionados().clear();
            }

            this.jLabelIconName.setText("%s#: %d".formatted(this.translate(
                    "Configuration for the icon"), icon.getId().getGlobalId()));
            if (icon instanceof Machine) {
                this.jLabelTitle.setText(this.translate("Machine icon configuration"));
                this.getTabelaMaquinaIaaS().setMaquina((Machine) icon, users);
                this.jScrollPane.setViewportView(this.iassMachineTable);
            }
            if (icon instanceof Cluster) {
                this.jLabelTitle.setText(this.translate("Cluster icon configuration"));
                this.getTabelaClusterIaaS().setCluster((Cluster) icon, users);
                this.jScrollPane.setViewportView(this.iassClusterTable);
            }
        }

    }

    public String getTitle() {
        return this.jLabelTitle.getText();
    }

    public void setPalavras(final ResourceBundle words) {
        this.words = words;
        ((MachineTable) this.machineTable.getModel()).setPalavras(words);
        ((MachineTableIaaS) this.iassMachineTable.getModel()).setPalavras(words);
        ((ClusterTable) this.clusterTable.getModel()).setPalavras(words);
        ((ClusterTableIaaS) this.iassClusterTable.getModel()).setPalavras(words);
        ((LinkTable) this.linkTable.getModel()).setPalavras(words);
    }

    private static class MachineVariedRowTable extends VariedRowTable {

        private static final String[] TOOL_TIPS = {
                "Insert the label name of the resource",
                "Select the resource owner",
                "Insert the amount of computing power of the resource in " +
                        "MFlops",
                "Insert the percentage of background computing in decimal " +
                        "notation",
                "Insert the number of precessing cores of the resource",
                "Insert the amount of memory of the resource in MBytes",
                "Insert the amount of hard disk of the resource in GBytes",
                "Select if the resource is master node",
                "Select the task scheduling policy of the master",
                "Select the slave nodes that will be coordinated by this " +
                        "master",
        };

        public String getToolTipText(final MouseEvent e) {
            final Point p = e.getPoint();
            final int rowIndex = this.rowAtPoint(p);
            final int colIndex = this.columnAtPoint(p);
            return this.getToolTip(rowIndex, colIndex);
        }

        private String getToolTip(final int rowIndex, final int colIndex) {
            try {
                if (colIndex != 1) {
                    return null;
                }
                return MachineVariedRowTable.getRowToolTip(rowIndex);
            } catch (final RuntimeException ignored) {
                return null;
            }
        }

        private static String getRowToolTip(final int rowIndex) {
            if (rowIndex >= MachineVariedRowTable.TOOL_TIPS.length)
                return null;
            return MachineVariedRowTable.TOOL_TIPS[rowIndex];
        }
    }

    private static class IaasMachineVariedRowTable extends VariedRowTable {

        public String getToolTipText(final MouseEvent e) {
            String tip = null;
            final Point p = e.getPoint();
            final int rowIndex = this.rowAtPoint(p);
            final int colIndex = this.columnAtPoint(p);

            try {
                if (colIndex == 1) {
                    if (rowIndex == 0) {
                        tip = "Insert the label name of the resource";
                    } else if (rowIndex == 1) {
                        tip = "Select the resource owner";
                    } else if (rowIndex == 2) {
                        tip = "Insert the amount of computing power of " +
                                "the resource in MFlops";
                    } else if (rowIndex == 3) {
                        tip = "Insert the percentage of background " +
                                "computing in decimal notation";
                    } else if (rowIndex == 4) {
                        tip = "Insert the number of precessing cores of " +
                                "the resource";
                    } else if (rowIndex == 5) {
                        tip = "Insert the amount of memory of the " +
                                "resource in MBytes";
                    } else if (rowIndex == 6) {
                        tip = "Insert the amount of hard disk of the " +
                                "resource in GBytes";
                    } else if (rowIndex == 7) {
                        tip = "Insert the cost of processing utilization " +
                                "($/cores/h)";
                    } else if (rowIndex == 8) {
                        tip = "Insert the cost of memory utilization " +
                                "($/MB/h)";
                    } else if (rowIndex == 9) {
                        tip = "Insert the cost of disk utilization " +
                                "($/GB/h)";
                    } else if (rowIndex == 10) {
                        tip = "Select if the resource is a virtual " +
                                "machine monitor";
                    } else if (rowIndex == 11) {
                        tip = "Select the task scheduling policy of the " +
                                "VMM";
                    } else if (rowIndex == 12) {
                        tip = "Select the virtual machine allocation " +
                                "policy of the VMM";
                    } else if (rowIndex == 13) {
                        tip = "Select the nodes that will be coordinated " +
                                "by this VMM";
                    }
                }
            } catch (final RuntimeException e1) {


            }

            return tip;
        }
    }

    private static class ClusterVariedRowTable extends VariedRowTable {

        public String getToolTipText(final MouseEvent e) {
            String tip = null;
            final Point p = e.getPoint();
            final int rowIndex = this.rowAtPoint(p);
            final int colIndex = this.columnAtPoint(p);

            try {
                if (colIndex == 1) {
                    if (rowIndex == 0) {
                        tip = "Insert the label name of the resource";
                    } else if (rowIndex == 1) {
                        tip = "Select the resource owner";
                    } else if (rowIndex == 2) {
                        tip = "Insert the number of nodes that composes " +
                                "the cluster";
                    } else if (rowIndex == 3) {
                        tip = "Insert the amount of computing power of " +
                                "the resource in MFlops";
                    } else if (rowIndex == 4) {
                        tip = "Insert the number of precessing cores of " +
                                "the resource";
                    } else if (rowIndex == 5) {
                        tip = "Insert the amount of memory of the " +
                                "resource in MBytes";
                    } else if (rowIndex == 6) {
                        tip = "Insert the amount of hard disk of the " +
                                "resource in GBytes";
                    } else if (rowIndex == 7) {
                        tip = "Insert the amount of bandwidth that " +
                                "connect the cluster nodes in Mbps";
                    } else if (rowIndex == 8) {
                        tip = "Insert the latency time of the links that " +
                                "connect the cluster nodes in seconds";
                    } else if (rowIndex == 9) {
                        tip = "Select if the resource is a master node";
                    } else if (rowIndex == 10) {
                        tip = "Select the task scheduling policy of the " +
                                "master node";
                    } else if (rowIndex == 11) {
                        tip = "Select the slave nodes that will be " +
                                "coordinated by this master";
                    }
                }
            } catch (final RuntimeException e1) {


            }

            return tip;
        }
    }

    private static class IaasClusterVariedRowTable extends VariedRowTable {

        public String getToolTipText(final MouseEvent e) {
            String tip = null;
            final Point p = e.getPoint();
            final int rowIndex = this.rowAtPoint(p);
            final int colIndex = this.columnAtPoint(p);

            try {
                if (colIndex == 1) {
                    if (rowIndex == 0) {
                        tip = "Insert the label name of the resource";
                    } else if (rowIndex == 1) {
                        tip = "Select the resource owner";
                    } else if (rowIndex == 2) {
                        tip = "Insert the number of nodes that composes " +
                                "the cluster";
                    } else if (rowIndex == 3) {
                        tip = "Insert the amount of computing power of " +
                                "the resource in MFlops";
                    } else if (rowIndex == 4) {
                        tip = "Insert the number of precessing cores of " +
                                "the resource";
                    } else if (rowIndex == 5) {
                        tip = "Insert the amount of memory of the " +
                                "resource in MBytes";
                    } else if (rowIndex == 6) {
                        tip = "Insert the amount of hard disk of the " +
                                "resource in GBytes";
                    } else if (rowIndex == 7) {
                        tip = "Insert the cost of processing utilization " +
                                "($/cores/h)";
                    } else if (rowIndex == 8) {
                        tip = "Insert the cost of memory utilization " +
                                "($/MB/h)";
                    } else if (rowIndex == 9) {
                        tip = "Insert the cost of disk utilization " +
                                "($/GB/h)";
                    } else if (rowIndex == 10) {
                        tip = "Insert the amount of bandwidth that " +
                                "connect the cluster nodes in Mbps";
                    } else if (rowIndex == 11) {
                        tip = "Insert the latency time of the links that " +
                                "connect the cluster nodes in seconds";
                    } else if (rowIndex == 12) {
                        tip = "Select if the resource is a virtual " +
                                "machine monitor";
                    } else if (rowIndex == 13) {
                        tip = "Select the task scheduling policy of the " +
                                "VMM";
                    } else if (rowIndex == 14) {
                        tip = "Select the virtual machine allocation " +
                                "policy of the VMM";
                    } else if (rowIndex == 15) {
                        tip = "Select the nodes that will be coordinated " +
                                "by this VMM";
                    }
                }
            } catch (final RuntimeException e1) {


            }

            return tip;
        }
    }

    private static class LinkVariedRowTable extends VariedRowTable {

        public String getToolTipText(final MouseEvent e) {
            String tip = null;
            final Point p = e.getPoint();
            final int rowIndex = this.rowAtPoint(p);
            final int colIndex = this.columnAtPoint(p);

            try {
                if (colIndex == 1) {
                    if (rowIndex == 0) {
                        tip = "Insert the label name of the resource";
                    } else if (rowIndex == 1) {
                        tip = "Insert the latency time of the resource in" +
                                " seconds";
                    } else if (rowIndex == 2) {
                        tip = "Insert the percentage of background " +
                                "communication in decimal notation";
                    } else if (rowIndex == 3) {
                        tip = "Insert the amount of bandwidth of the " +
                                "resource in seconds";
                    }
                }
            } catch (final RuntimeException e1) {


            }

            return tip;
        }
    }
}