/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.gui.configuracao;

import ispd.arquivo.Escalonadores;
import ispd.gui.iconico.grade.Cluster;
import java.util.HashSet;
import java.util.ResourceBundle;
import javax.swing.JComboBox;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author denison
 */
public class ClusterTable extends AbstractTableModel {

    // Constantes representando o índice das colunas
    private static final int TYPE = 0;
    private static final int VALUE = 1;
    private static final int LABEL = 0;
    private static final int OWNER = 1;
    private static final int NODES = 2;
    private static final int PROCS = 3;
    private static final int CORES = 4;
    private static final int MERAM = 5;
    private static final int HDISK = 6;
    private static final int BANDW = 7;
    private static final int LATEN = 8;
    private static final int MASTR = 9;
    private static final int SCHED = 10;
    private static final int NUMLINHAS = 11;
    private static final int NUMCOLUNAS = 2;
    // Array com os nomes das linhas
    private Cluster cluster;
    private JComboBox escalonador;
    private JComboBox usuarios;
    private ResourceBundle palavras;

    public ClusterTable(ResourceBundle palavras) {
        this.palavras = palavras;
        escalonador = new JComboBox(Escalonadores.ESCALONADORES);
        usuarios = new JComboBox();
    }

    public void setCluster(Cluster cluster, HashSet users) {
        this.cluster = cluster;
        this.escalonador.setSelectedItem(this.cluster.getAlgoritmo());
        this.usuarios.removeAllItems();
        for (Object object : users) {
            this.usuarios.addItem(object);
        }
        this.usuarios.setSelectedItem(cluster.getProprietario());
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
        if (columnIndex == VALUE && cluster != null) {
            switch (rowIndex) {
                case LABEL:
                    cluster.getId().setNome(aValue.toString());
                    break;
                case OWNER:
                    cluster.setProprietario(usuarios.getSelectedItem().toString());
                    break;
                case NODES:
                    cluster.setNumeroEscravos(Integer.valueOf(aValue.toString()));
                    break;
                case PROCS:
                    cluster.setPoderComputacional(Double.valueOf(aValue.toString()));
                    break;
                case MERAM:
                    cluster.setMemoriaRAM(Double.valueOf(aValue.toString()));
                    break;
                case HDISK:
                    cluster.setDiscoRigido(Double.valueOf(aValue.toString()));
                    break;
                case CORES:
                    cluster.setNucleosProcessador(Integer.valueOf(aValue.toString()));
                    break;
                case BANDW:
                    cluster.setBanda(Double.valueOf(aValue.toString()));
                    break;
                case LATEN:
                    cluster.setLatencia(Double.valueOf(aValue.toString()));
                    break;
                case MASTR:
                    cluster.setMestre(Boolean.valueOf(aValue.toString()));
                    break;
                case SCHED:
                    cluster.setAlgoritmo(escalonador.getSelectedItem().toString());
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
                    case NODES:
                        return palavras.getString("Number of nodes");
                    case PROCS:
                        return palavras.getString("Computing power");
                    case MERAM:
                        return "Primary Storage";
                    case HDISK:
                        return "Secondary Storage";
                    case CORES:
                        return "Cores";
                    case BANDW:
                        return palavras.getString("Bandwidth");
                    case LATEN:
                        return palavras.getString("Latency");
                    case MASTR:
                        return palavras.getString("Master");
                    case SCHED:
                        return palavras.getString("Scheduling algorithm");
                }
            case VALUE:
                if (cluster != null) {
                    switch (rowIndex) {
                        case LABEL:
                            return cluster.getId().getNome();
                        case OWNER:
                            return usuarios;
                        case NODES:
                            return cluster.getNumeroEscravos();
                        case PROCS:
                            return cluster.getPoderComputacional();
                        case MERAM:
                            return cluster.getMemoriaRAM();
                        case HDISK:
                            return cluster.getDiscoRigido();
                        case CORES:
                            return cluster.getNucleosProcessador();
                        case BANDW:
                            return cluster.getBanda();
                        case LATEN:
                            return cluster.getLatencia();
                        case MASTR:
                            return cluster.isMestre();
                        case SCHED:
                            return escalonador;
                    }
                } else {
                    switch (rowIndex) {
                        case OWNER:
                            return usuarios;
                        case SCHED:
                            return escalonador;
                        default:
                            return "null";
                    }
                }
            default:
                // Não deve ocorrer, pois só existem 2 colunas
                throw new IndexOutOfBoundsException("columnIndex out of bounds");
        }
    }

    public void setPalavras(ResourceBundle palavras) {
        this.palavras = palavras;
        fireTableStructureChanged();
    }
}
