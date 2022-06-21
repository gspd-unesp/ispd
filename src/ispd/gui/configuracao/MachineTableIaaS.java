package ispd.gui.configuracao;

import ispd.arquivo.Alocadores;
import ispd.arquivo.EscalonadoresCloud;
import ispd.gui.iconico.grade.ItemGrade;
import ispd.gui.iconico.grade.Machine;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author denison
 */
public class MachineTableIaaS extends AbstractTableModel {

    // Constantes representando o índice das colunas
    private static final int TYPE = 0;
    private static final int VALUE = 1;
    private static final int LABEL = 0;
    private static final int OWNER = 1;
    private static final int PROCS = 2;
    private static final int LOADF = 3;
    private static final int CORES = 4;
    private static final int MERAM = 5;
    private static final int HDISK = 6;
    private static final int CPP = 7; //cost processing
    private static final int CPM = 8; //cost memory
    private static final int CPDK = 9; //cost disk
    private static final int VMM = 10;
    private static final int SCHED = 11;
    private static final int VMMP = 12;
    private static final int SLAVE = 13;
    private static final int NUMLINHAS = 14;
    private static final int NUMCOLUNAS = 2;
    // Array com os nomes das linhas
    private ResourceBundle palavras;
    private Machine maquina;
    private final JButton escravos;
    private final JComboBox escalonador;
    private final JComboBox usuarios;
    private final JComboBox VMMPolicy;
    private final JList selecionadorEscravos;

    public MachineTableIaaS(final ResourceBundle palavras) {
        this.palavras = palavras;
        this.selecionadorEscravos = new JList();
        final CheckListRenderer clr = new CheckListRenderer(this.selecionadorEscravos);
        this.escravos = new JButton();
        this.escravos.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                //Cria lista com nós escalonaveis
                if (!MachineTableIaaS.this.selecionadorEscravos.isVisible()) {
                    final DefaultListModel listModel = new DefaultListModel();
                    final List<ItemGrade> listaConectados =
                            MachineTableIaaS.this.maquina.getNosEscalonaveis();
                    for (final ItemGrade item : listaConectados) {
                        listModel.addElement(item);
                    }
                    MachineTableIaaS.this.selecionadorEscravos.setModel(listModel);
                    for (final ItemGrade escravo : MachineTableIaaS.this.maquina.getEscravos()) {
                        final int index = listaConectados.indexOf(escravo);
                        MachineTableIaaS.this.selecionadorEscravos.addSelectionInterval(index, index);
                    }
                    MachineTableIaaS.this.selecionadorEscravos.setVisible(true);
                }
                if (MachineTableIaaS.this.selecionadorEscravos.getModel().getSize() > 0) {
                    final int opcao = JOptionPane.showConfirmDialog(
                            MachineTableIaaS.this.escravos,
                            MachineTableIaaS.this.selecionadorEscravos,
                            "Select the slaves",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE);
                    if (opcao == JOptionPane.OK_OPTION) {
                        final List<ItemGrade> escravosList =
                                new ArrayList<ItemGrade>(MachineTableIaaS.this.selecionadorEscravos.getSelectedValuesList());
                        MachineTableIaaS.this.maquina.setEscravos(escravosList);
                        MachineTableIaaS.this.escravos.setText(MachineTableIaaS.this.maquina.getEscravos().toString());
                    }
                }
            }
        });
        this.escravos.setToolTipText("Select the nodes that will be coordinated by" +
                " this VMM");
        this.escalonador = new JComboBox(EscalonadoresCloud.ESCALONADORES);
        this.escalonador.setToolTipText("Select the task scheduling policy");
        this.usuarios = new JComboBox();
        this.usuarios.setToolTipText("Select the resource owner");
        this.VMMPolicy = new JComboBox(Alocadores.ALOCACAO);
        this.VMMPolicy.setToolTipText("Select the virtual machine allocation " +
                "policy");
    }

    public void setMaquina(final Machine maquina, final HashSet users) {
        this.maquina = maquina;
        this.escalonador.setSelectedItem(this.maquina.getAlgoritmo());
        this.VMMPolicy.setSelectedItem(this.maquina.getVMMallocpolicy());
        this.usuarios.removeAllItems();
        for (final Object object : users) {
            this.usuarios.addItem(object);
        }
        this.usuarios.setSelectedItem(maquina.getProprietario());
        this.selecionadorEscravos.setVisible(false);
        this.escravos.setText(maquina.getEscravos().toString());
    }

    @Override
    public int getRowCount() {
        return MachineTableIaaS.NUMLINHAS;
    }

    @Override
    public int getColumnCount() {
        return MachineTableIaaS.NUMCOLUNAS;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        switch (columnIndex) {
            case MachineTableIaaS.TYPE:
                switch (rowIndex) {
                    case MachineTableIaaS.LABEL:
                        return this.palavras.getString("Label");
                    case MachineTableIaaS.OWNER:
                        return this.palavras.getString("Owner");
                    case MachineTableIaaS.PROCS:
                        return this.palavras.getString("Computing power") + " " +
                                "(Mflop/s)";
                    case MachineTableIaaS.LOADF:
                        return this.palavras.getString("Load Factor");
                    case MachineTableIaaS.MERAM:
                        return "Primary Storage";
                    case MachineTableIaaS.HDISK:
                        return "Secondary Storage";
                    case MachineTableIaaS.CORES:
                        return "Cores";
                    case MachineTableIaaS.VMM:
                        return "VMM";
                    case MachineTableIaaS.SCHED:
                        return this.palavras.getString("Scheduling algorithm");
                    case MachineTableIaaS.SLAVE:
                        return "Slave Nodes";
                    case MachineTableIaaS.CPP:
                        return "Cost per Processing";
                    case MachineTableIaaS.CPM:
                        return "Cost per Memory";
                    case MachineTableIaaS.CPDK:
                        return "Cost per Disk";
                    case MachineTableIaaS.VMMP:
                        return ("VMM allocated policy");
                }
            case MachineTableIaaS.VALUE:
                if (this.maquina != null) {
                    switch (rowIndex) {
                        case MachineTableIaaS.LABEL:
                            return this.maquina.getId().getNome();
                        case MachineTableIaaS.OWNER:
                            return this.usuarios;
                        case MachineTableIaaS.PROCS:
                            return this.maquina.getPoderComputacional();
                        case MachineTableIaaS.LOADF:
                            return this.maquina.getTaxaOcupacao();
                        case MachineTableIaaS.MERAM:
                            return this.maquina.getMemoriaRAM();
                        case MachineTableIaaS.HDISK:
                            return this.maquina.getDiscoRigido();
                        case MachineTableIaaS.CORES:
                            return this.maquina.getNucleosProcessador();
                        case MachineTableIaaS.VMM:
                            return this.maquina.isMestre();
                        case MachineTableIaaS.SCHED:
                            return this.escalonador;
                        case MachineTableIaaS.SLAVE:
                            return this.escravos;
                        case MachineTableIaaS.CPP:
                            return this.maquina.getCostperprocessing();
                        case MachineTableIaaS.CPM:
                            return this.maquina.getCostpermemory();
                        case MachineTableIaaS.CPDK:
                            return this.maquina.getCostperdisk();
                        case MachineTableIaaS.VMMP:
                            return this.VMMPolicy;
                    }
                } else {
                    switch (rowIndex) {
                        case MachineTableIaaS.OWNER:
                            return this.usuarios;
                        case MachineTableIaaS.SCHED:
                            return this.escalonador;
                        case MachineTableIaaS.SLAVE:
                            return this.escravos;
                        case MachineTableIaaS.VMMP:
                            return this.VMMPolicy;
                        default:
                            return "null";
                    }
                }
            default:
                // Não deve ocorrer, pois só existem 2 colunas
                throw new IndexOutOfBoundsException("ColumnIndex out of " +
                        "bounds");
        }
    }

    @Override
    public String getColumnName(final int columnIndex) {
        switch (columnIndex) {
            case MachineTableIaaS.TYPE:
                return this.palavras.getString("Properties");
            case MachineTableIaaS.VALUE:
                return this.palavras.getString("Values");
        }
        return null;
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex != MachineTableIaaS.TYPE;
    }

    @Override
    public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
        // Pega o sócio referente a linha especificada.
        if (columnIndex == MachineTableIaaS.VALUE && this.maquina != null) {
            switch (rowIndex) {
                case MachineTableIaaS.LABEL:
                    this.maquina.getId().setNome(aValue.toString());
                    break;
                case MachineTableIaaS.OWNER:
                    this.maquina.setProprietario(this.usuarios.getSelectedItem().toString());
                    break;
                case MachineTableIaaS.PROCS:
                    this.maquina.setPoderComputacional(Double.valueOf(aValue.toString()));
                    break;
                case MachineTableIaaS.LOADF:
                    this.maquina.setTaxaOcupacao(Double.valueOf(aValue.toString()));
                    break;
                case MachineTableIaaS.MERAM:
                    this.maquina.setMemoriaRAM(Double.valueOf(aValue.toString()));
                    break;
                case MachineTableIaaS.HDISK:
                    this.maquina.setDiscoRigido(Double.valueOf(aValue.toString()));
                    break;
                case MachineTableIaaS.CORES:
                    this.maquina.setNucleosProcessador(Integer.valueOf(aValue.toString()));
                    break;
                case MachineTableIaaS.VMM:
                    this.maquina.setMestre(Boolean.valueOf(aValue.toString()));
                    break;
                case MachineTableIaaS.SCHED:
                    this.maquina.setAlgoritmo(this.escalonador.getSelectedItem().toString());
                    break;
                case MachineTableIaaS.CPP:
                    this.maquina.setCostperprocessing(Double.valueOf(aValue.toString()));
                    break;
                case MachineTableIaaS.CPM:
                    this.maquina.setCostpermemory(Double.valueOf(aValue.toString()));
                    break;
                case MachineTableIaaS.CPDK:
                    this.maquina.setCostperdisk(Double.valueOf(aValue.toString()));
                    break;
                case MachineTableIaaS.VMMP:
                    this.maquina.setVMMallocpolicy(this.VMMPolicy.getSelectedItem().toString());
                    break;
            }
            this.fireTableCellUpdated(rowIndex, VALUE); // Notifica a
            // atualização da célula
        }
    }

    public JComboBox getEscalonadores() {
        return this.escalonador;
    }

    public JComboBox getAlocadores() {
        return this.VMMPolicy;
    }

    public void setPalavras(final ResourceBundle palavras) {
        this.palavras = palavras;
        this.fireTableStructureChanged();
    }
}