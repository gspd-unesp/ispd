/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.gui.configuracao;

import ispd.arquivo.Alocadores;
import ispd.arquivo.Escalonadores;
import ispd.arquivo.EscalonadoresCloud;
import ispd.gui.iconico.grade.Cluster;
import java.awt.Component;
import java.util.HashSet;
import java.util.ResourceBundle;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author denison
 */
public class ClusterTableIaaS extends AbstractTableModel{

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
    private static final int CPP = 7; //cost processing
    private static final int CPM = 8; //cost memory
    private static final int CPDK = 9; //cost disk
    private static final int BANDW = 10;
    private static final int LATEN = 11;
    private static final int VMM = 12;
    private static final int SCHED = 13;
    private static final int VMMP = 14;
    private static final int NUMLINHAS = 15;
    private static final int NUMCOLUNAS = 2;
    // Array com os nomes das linhas
    private Cluster cluster;
    private JComboBox escalonador;
    private JComboBox usuarios;
    private JComboBox VMMPolicy;
    private ResourceBundle palavras;

    public ClusterTableIaaS(ResourceBundle palavras) {
        this.palavras = palavras;
        escalonador = new JComboBox(EscalonadoresCloud.ESCALONADORES);
        escalonador.setToolTipText("Select the task scheduling policy");
        usuarios = new JComboBox();
        usuarios.setToolTipText("Select the resource owner");
        VMMPolicy = new JComboBox(Alocadores.ALOCACAO);
        VMMPolicy.setToolTipText("Select the virtual machine allocation policy");
    }

    public void setCluster(Cluster cluster, HashSet users) {
        this.cluster = cluster;
        this.escalonador.setSelectedItem(this.cluster.getAlgoritmo());
        this.VMMPolicy.setSelectedItem(this.cluster.getVMMallocpolicy());
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

    public JComboBox getAlocadores() {
        return VMMPolicy;
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
                case CORES:
                    cluster.setNucleosProcessador(Integer.valueOf(aValue.toString()));
                    break;
                case MERAM:
                    cluster.setMemoriaRAM(Double.valueOf(aValue.toString()));
                    break;
                case HDISK:
                    cluster.setDiscoRigido(Double.valueOf(aValue.toString()));
                    break;
                case CPP:
                    cluster.setCostperprocessing(Double.valueOf(aValue.toString()));
                    break;
                case CPM:
                    cluster.setCostpermemory(Double.valueOf(aValue.toString()));
                    break;
                case CPDK:
                    cluster.setCostperdisk(Double.valueOf(aValue.toString()));

                    break;
                case BANDW:
                    cluster.setBanda(Double.valueOf(aValue.toString()));
                    break;
                case LATEN:
                    cluster.setLatencia(Double.valueOf(aValue.toString()));
                    break;
                case VMM:
                    cluster.setMestre(Boolean.valueOf(aValue.toString()));
                    break;
                case SCHED:
                    cluster.setAlgoritmo(escalonador.getSelectedItem().toString());
                    break;
                case VMMP:
                    cluster.setVMMallocpolicy(VMMPolicy.getSelectedItem().toString());
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
                    case CORES:
                        return "Cores";
                    case MERAM:
                        return "Primary Storage";
                    case HDISK:
                        return "Secondary Storage";
                    case BANDW:
                        return palavras.getString("Bandwidth");
                    case LATEN:
                        return palavras.getString("Latency");
                    case VMM:
                        return "VMM";
                    case SCHED:
                        return palavras.getString("Scheduling algorithm");
                    case CPP:
                        return "Cost per Processing";
                    case CPM:
                        return "Cost per Memory";
                    case CPDK:
                        return "Cost per Disk";
                    case VMMP:
                        return ("VMM allocated policy");
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
                        case VMM:
                            return cluster.isMestre();
                        case SCHED:
                            return escalonador;
                        case CPP:
                            return cluster.getCostperprocessing();
                        case CPM:
                            return cluster.getCostpermemory();
                        case CPDK:
                            return cluster.getCostperdisk();
                        case VMMP:
                            return VMMPolicy;
                    }
                } else {
                    switch (rowIndex) {
                        case OWNER:
                            return usuarios;
                        case SCHED:
                            return escalonador;
                        case VMMP:
                            return VMMPolicy;
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
