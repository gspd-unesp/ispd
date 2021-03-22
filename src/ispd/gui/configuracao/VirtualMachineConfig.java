/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.gui.configuracao;

import javax.swing.table.AbstractTableModel;

/**
 *
 * @author arthurjorge
 */
public class VirtualMachineConfig extends AbstractTableModel {

    private String Label;
    private Integer idhosp;
    private String iduser;
    private Integer Ncores;
    private Double memallocated;
    private Double diskallocated;
    private String SO;

    @Override
    public int getRowCount() {
        return 7;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                switch (rowIndex) {
                    case 0:
                        return "Label";
                    case 1:
                        return "ID Physical Machine";
                    case 2:
                        return "ID user";
                    case 3:
                        return "Number of cores";
                    case 4:
                        return "Memory allocated";
                    case 5:
                        return "Disk Size";
                    case 6:
                        return "Operational System";
                }
            case 1:

                switch (rowIndex) {
                    case 0:
                        return Label;
                    case 1:
                        return idhosp;
                    case 2:
                        return iduser;
                    case 3:
                        return Ncores;
                    case 4:
                        return memallocated;
                    case 5:
                        return diskallocated;
                    case 6:
                        return SO;

                }




            default:
                // Não deve ocorrer, pois só existem 2 colunas
                throw new IndexOutOfBoundsException("columnIndex out of bounds");
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        // Pega o sócio referente a linha especificada.
        if (columnIndex == 1) {
            switch (rowIndex) {
                case 0:
                    Label = (String) aValue;
                    break;
                case 1:
                    idhosp = (Integer) aValue;
                    break;
                case 2:
                    iduser = (String) aValue;
                    break;
                case 3:
                    Ncores = (Integer) aValue;
                    break;
                case 4:
                    memallocated = (Double) aValue;
                    break;
                case 5:
                    diskallocated = (Double) aValue;
                    break;
                case 6:
                    SO = (String) aValue;
                    break;
            }
            fireTableCellUpdated(rowIndex, columnIndex); // Notifica a atualização da célula
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return false;
        }
        return true;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Properties";
            case 1:
                return "Values";
        }
        return null;
    }
}
