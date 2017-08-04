package cz.zcu.kiv.eeg.gtn.gui;

import javax.swing.table.AbstractTableModel;

import cz.zcu.kiv.eeg.gtn.utils.Const;

@SuppressWarnings("serial")
class StimuliTableModel extends AbstractTableModel {

    private Object[][] data;
    private int count;
    
    private final static String[] TABLE_COLUMN_NAMES = {"ID", "Name", "Score", "Trials"};

    StimuliTableModel(int count) {
        this.count = count;
        data = new Object[this.count][4];
        for (int i = 0; i < data.length; i++) {
            data[i][0] = i + 1;
        }
    }

    @Override
    public String getColumnName(int column) {
        return TABLE_COLUMN_NAMES[column];
    }

    @Override
    public int getColumnCount() {
        return TABLE_COLUMN_NAMES.length;
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        return data[row][column];
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        data[row][column] = value;
        fireTableCellUpdated(row, column);
    }

    public void setCount(int count){
        this.count = count;
    }
}
