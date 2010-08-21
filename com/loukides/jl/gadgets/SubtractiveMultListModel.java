package com.loukides.jl.gadgets;
import com.loukides.jl.jl.*;
import com.loukides.jl.checkers.*;

import javax.swing.*;
import javax.swing.table.*;
import java.util.*;


public class SubtractiveMultListModel extends AbstractTableModel 
  implements MultListModel {
  HashMap mults;
  Vector visual;
  Properties p;
  ArbitraryChecker owner;
  boolean updateVisual;
  boolean permodemults = true;
  boolean perbandmults = true;

  public SubtractiveMultListModel(HashMap mults, Properties p, ArbitraryChecker c) {
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
    Object [] l = mults.values().toArray();
    // there has got to be a better way to do this... 
    Arrays.sort(l);

    visual = new Vector();
    String nmode = "";
    String nband = "";
    String modelist = "";
    String bandlist = "";
    if (permodemults) modelist = p.getProperty("mode") ;
    else modelist = "x"; // to allow the loops to run
    if (perbandmults) bandlist = p.getProperty("bands") ; 
    else bandlist = "x"; // to allow the loops to run
    // System.out.println("setup visual!: " + modelist + " " + bandlist);
    StringTokenizer stm = new StringTokenizer(modelist);
    while ( stm.hasMoreTokens() ) {
      nmode = stm.nextToken();
      StringTokenizer stb = new StringTokenizer(bandlist);
      while ( stb.hasMoreTokens() ) {
        nband = stb.nextToken();
        for ( int i = 0; i < l.length; i++) {
          // System.out.println("!!" + nmode + "!!!" + nband);
          visual.addElement(makeVisKey(nmode, nband, (String)l[i]));
        }
      }
    }
  }


  private String makeVisKey(String mode, String band, String name) {
    if ( ! perbandmults ) band = "";
    if ( ! permodemults ) mode = "";
    return mode + " " + band + " " + name; 
  }

  public void setUpdateVisual(boolean b) {
    // System.out.println("updateVisual: " + b);
    updateVisual = b;
    if (updateVisual) fireTableDataChanged();
  }

  public void updateDisplay(LogEntry le, String mv) {
    // String name = le.getName();
    String name = makeVisKey(le.getMode(), le.getBand(), mv);
    int ix = visual.indexOf(name);
    // System.out.println("update (MLM): " + name + " " + ix);
    if ( ix != -1) {
      visual.remove( ix );
      if (updateVisual) fireTableRowsDeleted(ix, ix);
    }
  }

  // table model methods (for visual display)
  public String getColumnName( int i ) { return "Needed:" ;}
  public int getRowCount() { return visual.size() ;}
  public int getColumnCount() { return 1 ;}

  public Object getValueAt( int row, int col) { 
    return (String) visual.elementAt(row);
  }
}