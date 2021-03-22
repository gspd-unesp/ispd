/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.gui.configuracao;

import ispd.arquivo.Escalonadores;
import ispd.gui.iconico.grade.ItemGrade;
import ispd.gui.iconico.grade.Machine;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author denison
 */
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
    private static final int NUMLINHAS = 10;
    private static final int NUMCOLUNAS = 2;
    // Array com os nomes das linhas
    private ResourceBundle palavras;
    private Machine maquina;
    private JButton escravos;
    private JComboBox escalonador;
    private JComboBox usuarios;
    private JList selecionadorEscravos;

    public MachineTable(ResourceBundle palavras) {
        this.palavras = palavras;
        selecionadorEscravos = new JList();
        CheckListRenderer clr = new CheckListRenderer(selecionadorEscravos);
        escravos = new JButton();
        escravos.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //Cria lista com nós escalonaveis
                if (!selecionadorEscravos.isVisible()) {
                    DefaultListModel listModel = new DefaultListModel();
                    List<ItemGrade> listaConectados = maquina.getNosEscalonaveis();
                    for (ItemGrade item : listaConectados) {
                        listModel.addElement(item);
                    }
                    selecionadorEscravos.setModel(listModel);
                    for (ItemGrade escravo : maquina.getEscravos()) {
                        int index = listaConectados.indexOf(escravo);
                        selecionadorEscravos.addSelectionInterval(index, index);
                    }
                    selecionadorEscravos.setVisible(true);
                }
                if (selecionadorEscravos.getModel().getSize() > 0) {
                    int opcao = JOptionPane.showConfirmDialog(
                            escravos,
                            selecionadorEscravos,
                            "Select the slaves",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE);
                    if (opcao == JOptionPane.OK_OPTION) {
                        List<ItemGrade> escravosList = new ArrayList<ItemGrade>(selecionadorEscravos.getSelectedValuesList());
                        maquina.setEscravos(escravosList);
                        escravos.setText(maquina.getEscravos().toString());
                    }
                }
            }
        });
        escalonador = new JComboBox(Escalonadores.ESCALONADORES);
        usuarios = new JComboBox();
    }

    public void setMaquina(Machine maquina, HashSet users) {
        this.maquina = maquina;
        this.escalonador.setSelectedItem(this.maquina.getAlgoritmo());
        this.usuarios.removeAllItems();
        for (Object object : users) {
            this.usuarios.addItem(object);
        }
        this.usuarios.setSelectedItem(maquina.getProprietario());
        this.selecionadorEscravos.setVisible(false);
        escravos.setText(maquina.getEscravos().toString());
    }

    @Override
    public int getRowCount() {
        return NUMLINHAS;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case TYPE:
                return palavras.getString("Properties");
            case VALUE:
                return palavras.getString("Values");
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == TYPE) {
            return false;
        }
        return true;
    }

    @Override
    public int getColumnCount() {
        return NUMCOLUNAS;
    }

    public JComboBox getEscalonadores() {
        return escalonador;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        // Pega o sócio referente a linha especificada.
        if (columnIndex == VALUE && maquina != null) {
            switch (rowIndex) {
                case LABEL:
                    maquina.getId().setNome(aValue.toString());
                    break;
                case OWNER:
                    maquina.setProprietario(usuarios.getSelectedItem().toString());
                    break;
                case PROCS:
                    maquina.setPoderComputacional(Double.valueOf(aValue.toString()));
                    break;
                case LOADF:
                    maquina.setTaxaOcupacao(Double.valueOf(aValue.toString()));
                    break;
                case MERAM:
                    maquina.setMemoriaRAM(Double.valueOf(aValue.toString()));
                    break;
                case HDISK:
                    maquina.setDiscoRigido(Double.valueOf(aValue.toString()));
                    break;
                case CORES:
                    maquina.setNucleosProcessador(Integer.valueOf(aValue.toString()));
                    break;
                case MASTR:
                    maquina.setMestre(Boolean.valueOf(aValue.toString()));
                    break;
                case SCHED:
                    maquina.setAlgoritmo(escalonador.getSelectedItem().toString());
                    break;
            }
            fireTableCellUpdated(rowIndex, columnIndex); // Notifica a atualização da célula
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case TYPE:
                switch (rowIndex) {
                    case LABEL:
                        return palavras.getString("Label");
                    case OWNER:
                        return palavras.getString("Owner");
                    case PROCS:
                        return palavras.getString("Computing power") + " (Mflop/s)";
                    case LOADF:
                        return palavras.getString("Load Factor");
                    case MERAM:
                        return "Primary Storage";
                    case HDISK:
                        return "Secondary Storage";
                    case CORES:
                        return "Cores";
                    case MASTR:
                        return palavras.getString("Master");
                    case SCHED:
                        return palavras.getString("Scheduling algorithm");
                    case SLAVE:
                        return "Slave Nodes";
                }
            case VALUE:
                if (maquina != null) {
                    switch (rowIndex) {
                        case LABEL:
                            return maquina.getId().getNome();
                        case OWNER:
                            return usuarios;
                        case PROCS:
                            return maquina.getPoderComputacional();
                        case LOADF:
                            return maquina.getTaxaOcupacao();
                        case MERAM:
                            return maquina.getMemoriaRAM();
                        case HDISK:
                            return maquina.getDiscoRigido();
                        case CORES:
                            return maquina.getNucleosProcessador();
                        case MASTR:
                            return maquina.isMestre();
                        case SCHED:
                            return escalonador;
                        case SLAVE:
                            return escravos;
                    }
                } else {
                    switch (rowIndex) {
                        case OWNER:
                            return usuarios;
                        case SCHED:
                            return escalonador;
                        case SLAVE:
                            return escravos;
                        default:
                            return "null";
                    }
                }
            default:
                // Não deve ocorrer, pois só existem 2 colunas
                throw new IndexOutOfBoundsException("ColumnIndex out of bounds");
        }
    }

    public void setPalavras(ResourceBundle palavras) {
        this.palavras = palavras;
        fireTableStructureChanged();
    }
}
