//  A table model for a list of multipliers to which multipliers are added
// each time one is worked.
package com.loukides.jl.gadgets;
import com.loukides.jl.jl.*;
import com.loukides.jl.checkers.*;

import javax.swing.*;
import javax.swing.table.*;
import java.util.*;


public class AdditiveMultListModel extends AbstractTableModel 
  implements MultListModel {
  HashMap mults;
  Vector visual = new Vector();
  Properties p;
  ArbitraryChecker owner;
  boolean updateVisual;
  boolean permodemults = true;
  boolean perbandmults = true;

  public AdditiveMultListModel(HashMap mults, Properties p, ArbitraryChecker c) {
    this.mults = mults;
    this.p = p;
    if ( p.getProperty("perBandMultiplier").equals("false") ) 
      perbandmults = false; 
    if ( p.getProperty("perModeMultiplier").equals("false") ) 
      permodemults = false; 
    owner = c;
    setupVisual();
  }

  public void setupVisual() { 
    visual.clear();
  }

  public void setUpdateVisual(boolean b) {
    // System.out.println("updateVisual: " + b);
    updateVisual = b;
    if (updateVisual) fireTableDataChanged();
  }

  public void updateDisplay(LogEntry le, String mv) {
    // String name = le.getName();
    // System.out.println("updateDisplay: " + mv);
    MultListElement el = new MultListElement(le, mv);
    String name = el.getSortKey();
//    String name = makeVisKey(le.getMode(), le.getBand(), mv);
    String current = "";
    int first = 0;
    int last = visual.size();
    int ix = 0;
    if ( visual.size() == 0) {
      visual.insertElementAt(el, 0);
      if (updateVisual) fireTableRowsInserted(ix, ix);
      return;
    }

    while ( first < last -1 ) {
      ix = first + (last - first) / 2; // implicitly rounds down
      current = ((MultListElement)visual.elementAt(ix)).getSortKey();
      if ( name.compareTo(current) == 0 ) return; // strings equal
      else if ( name.compareTo(current) < 0 ) last = ix; //name before current
      else first = ix; // name falls after current
      // System.out.println("AMLM::update: " + first + " " + last + " " + ix);
    }
    if (name.compareTo(
         ((MultListElement)visual.elementAt(first)).getSortKey()) < 0) 
      visual.insertElementAt(el, first);
    else visual.insertElementAt(el, last);
    if (updateVisual) fireTableRowsInserted(ix, ix);
  }

  // table model methods (for visual display)
  public String getColumnName( int i ) { return "Worked:" ;}
  public int getRowCount() { return visual.size() ;}
  public int getColumnCount() { return 1 ;}

  public Object getValueAt( int row, int col) { 
    return ((MultListElement)visual.elementAt(row)).getDisplay();
  }

  public class MultListElement {
    private String sortAs;
    private String display;
    private String band;
    private String mode;
    public MultListElement(LogEntry le, String mv) {
      if ( ! perbandmults ) band = "";
      else band = le.getBand();
      if ( ! permodemults ) mode = "";
      else mode = le.getMode();
      display = band + " "+ mode + " " + mv; 
      sortAs = mv + " "+ band + " " + mode; 
    }
    public String getSortKey() { return sortAs; }
    public String getDisplay() { return display; }
  }
}