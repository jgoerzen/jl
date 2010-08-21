package com.loukides.jl.cluster;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class SpotTableModel extends AbstractTableModel {

    private List spotlist = null;
    private String [] columnNames = { "Call", "Freq", "QSX", "Time" };

    public SpotTableModel(List spotlist) {
        this.spotlist = spotlist; 
    }

    public int getColumnCount() { return columnNames.length; }

    public int getRowCount() { return SpotController.MAXSPOTS; }

    public Object getValueAt( int row, int col ) {
        if ( row >= spotlist.size()) return null;
        Spot thisspot = (Spot)spotlist.get(row);
        if (col == 0) return thisspot.getCall();
        if (col == 1) return new Float(thisspot.getFrequency());
        if (col == 2)
            if (thisspot.getQSX() != 0f) return new Float(thisspot.getQSX());
            else return null;
        if (col == 3) return thisspot.getTimeSpotted();
        return null;
    }

    public String getColumnName(int i) {
        System.out.println("Column name: " + columnNames[i]);
        return columnNames[i];
    }

}
