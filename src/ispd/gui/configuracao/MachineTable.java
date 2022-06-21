package ispd.gui.configuracao;

import ispd.arquivo.Escalonadores;
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

public class MachineTable extends AbstractTableModel {

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
    private static final int MASTR = 7;
    private static final int SCHED = 8;
    private static final int SLAVE = 9;
    private static final int ENERGY = 10;
    private static final int NUMLINHAS = 11;
    private static final int NUMCOLUNAS = 2;
    // Array com os nomes das linhas
    private ResourceBundle palavras;
    private Machine maquina;
    private final JButton escravos;
    private final JComboBox escalonador;
    private final JComboBox usuarios;
    private final JList selecionadorEscravos;

    public MachineTable(final ResourceBundle palavras) {
        this.palavras = palavras;
        this.selecionadorEscravos = new JList();
        final CheckListRenderer clr = new CheckListRenderer(this.selecionadorEscravos);
        this.escravos = new JButton();
        this.escravos.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                //Cria lista com nós escalonaveis
                if (!MachineTable.this.selecionadorEscravos.isVisible()) {
                    final DefaultListModel listModel = new DefaultListModel();
                    final List<ItemGrade> listaConectados =
                            MachineTable.this.maquina.getNosEscalonaveis();
                    for (final ItemGrade item : listaConectados) {
                        listModel.addElement(item);
                    }
                    MachineTable.this.selecionadorEscravos.setModel(listModel);
                    for (final ItemGrade escravo : MachineTable.this.maquina.getEscravos()) {
                        final int index = listaConectados.indexOf(escravo);
                        MachineTable.this.selecionadorEscravos.addSelectionInterval(index, index);
                    }
                    MachineTable.this.selecionadorEscravos.setVisible(true);
                }
                if (MachineTable.this.selecionadorEscravos.getModel().getSize() > 0) {
                    final int opcao = JOptionPane.showConfirmDialog(
                            MachineTable.this.escravos,
                            MachineTable.this.selecionadorEscravos,
                            "Select the slaves",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE);
                    if (opcao == JOptionPane.OK_OPTION) {
                        final List<ItemGrade> escravosList =
                                new ArrayList<ItemGrade>(MachineTable.this.selecionadorEscravos.getSelectedValuesList());
                        MachineTable.this.maquina.setEscravos(escravosList);
                        MachineTable.this.escravos.setText(MachineTable.this.maquina.getEscravos().toString());
                    }
                }
            }
        });
        this.escalonador = new JComboBox(Escalonadores.ESCALONADORES);
        this.usuarios = new JComboBox();
    }

    public void setMaquina(final Machine maquina, final HashSet users) {
        this.maquina = maquina;
        this.escalonador.setSelectedItem(this.maquina.getAlgoritmo());
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
        return MachineTable.NUMLINHAS;
    }

    @Override
    public int getColumnCount() {
        return MachineTable.NUMCOLUNAS;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        switch (columnIndex) {
            case MachineTable.TYPE:
                switch (rowIndex) {
                    case MachineTable.LABEL:
                        return this.palavras.getString("Label");
                    case MachineTable.OWNER:
                        return this.palavras.getString("Owner");
                    case MachineTable.PROCS:
                        return this.palavras.getString("Computing power") + " " +
                                "(Mflop/s)";
                    case MachineTable.LOADF:
                        return this.palavras.getString("Load Factor");
                    case MachineTable.MERAM:
                        return "Primary Storage";
                    case MachineTable.HDISK:
                        return "Secondary Storage";
                    case MachineTable.CORES:
                        return "Cores";
                    case MachineTable.MASTR:
                        return this.palavras.getString("Master");
                    case MachineTable.SCHED:
                        return this.palavras.getString("Scheduling algorithm");
                    case MachineTable.SLAVE:
                        return "Slave Nodes";
                    case MachineTable.ENERGY:
                        return "Energy consumption";
                }
            case MachineTable.VALUE:
                if (this.maquina != null) {
                    switch (rowIndex) {
                        case MachineTable.LABEL:
                            return this.maquina.getId().getNome();
                        case MachineTable.OWNER:
                            return this.usuarios;
                        case MachineTable.PROCS:
                            return this.maquina.getPoderComputacional();
                        case MachineTable.LOADF:
                            return this.maquina.getTaxaOcupacao();
                        case MachineTable.MERAM:
                            return this.maquina.getMemoriaRAM();
                        case MachineTable.HDISK:
                            return this.maquina.getDiscoRigido();
                        case MachineTable.CORES:
                            return this.maquina.getNucleosProcessador();
                        case MachineTable.MASTR:
                            return this.maquina.isMestre();
                        case MachineTable.SCHED:
                            return this.escalonador;
                        case MachineTable.SLAVE:
                            return this.escravos;
                        case MachineTable.ENERGY:
                            return this.maquina.getConsumoEnergia();
                    }
                } else {
                    switch (rowIndex) {
                        case MachineTable.OWNER:
                            return this.usuarios;
                        case MachineTable.SCHED:
                            return this.escalonador;
                        case MachineTable.SLAVE:
                            return this.escravos;
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
            case MachineTable.TYPE:
                return this.palavras.getString("Properties");
            case MachineTable.VALUE:
                return this.palavras.getString("Values");
        }
        return null;
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex != MachineTable.TYPE;
    }

    @Override
    public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
        // Pega o sócio referente a linha especificada.
        if (columnIndex == MachineTable.VALUE && this.maquina != null) {
            switch (rowIndex) {
                case MachineTable.LABEL:
                    this.maquina.getId().setNome(aValue.toString());
                    break;
                case MachineTable.OWNER:
                    this.maquina.setProprietario(this.usuarios.getSelectedItem().toString());
                    break;
                case MachineTable.PROCS:
                    this.maquina.setPoderComputacional(Double.valueOf(aValue.toString()));
                    break;
                case MachineTable.LOADF:
                    this.maquina.setTaxaOcupacao(Double.valueOf(aValue.toString()));
                    break;
                case MachineTable.MERAM:
                    this.maquina.setMemoriaRAM(Double.valueOf(aValue.toString()));
                    break;
                case MachineTable.HDISK:
                    this.maquina.setDiscoRigido(Double.valueOf(aValue.toString()));
                    break;
                case MachineTable.CORES:
                    this.maquina.setNucleosProcessador(Integer.valueOf(aValue.toString()));
                    break;
                case MachineTable.ENERGY:
                    this.maquina.setConsumoEnergia(Double.valueOf(aValue.toString()));
                    break;
                case MachineTable.MASTR:
                    this.maquina.setMestre(Boolean.valueOf(aValue.toString()));
                    break;
                case MachineTable.SCHED:
                    this.maquina.setAlgoritmo(this.escalonador.getSelectedItem().toString());
                    break;
            }
            this.fireTableCellUpdated(rowIndex, VALUE); // Notifica a
            // atualização da célula
        }
    }

    public JComboBox getEscalonadores() {
        return this.escalonador;
    }

    public void setPalavras(final ResourceBundle palavras) {
        this.palavras = palavras;
        this.fireTableStructureChanged();
    }
}