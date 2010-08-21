package com.loukides.jl.gadgets;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;
import com.loukides.jl.qtc.QTCEntry;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.text.*;
import javax.swing.table.*;

public class EditableLogModel extends AbstractTableModel {

  private String [] headings = {
    "Serial", "Time", "Mode", "Band", "Sent", "Call", "Rcvd", "Dup", "Mul", "Pt"
  }   ;
  Vector veclog;

  public void setLog(Vector v) {
    veclog = v;
  } 

  // table model stuff
  public int getRowCount() { 
    if (veclog == null) return 0;
    else return veclog.size() + 2 ; 
  }

  public int getColumnCount() { return headings.length ; } 

  // calls are displayed in the table in the TYPED font; normalized on saving.
  // This may be right--don't know.
  public Object getValueAt(int row, int col) {
    if (veclog == null) return "";
    if ( row >= veclog.size() ) return null;
    Loggable l = (Loggable) veclog.elementAt(row);
//    System.out.println(row + " " + l.toCabrilloString());
    if (col == 1) return l.timeToGMT(l.getDate());
    if (col == 2) return l.getMode();
    if (col == 3) return l.getBand();
    if ( l instanceof LogEntry ) {
      LogEntry le = (LogEntry) l;
      if (col == 0) return new Integer(le.getSent().getSerial());
      if (col == 4) return le.getSent().getGUIExchange();
      if (col == 5) return le.getRcvd().getCallsign();
      if (col == 6) return le.getRcvd().getGUIExchange();
      if (col == 7) { if (! le.isComplete()) return "I";
                      else if ( le.getDupe() ) return "D"; 
                      else if ( le.isQTC() ) return "G"; 
                      else return ""; }
      if (col == 8) return new Character(le.getMultChar());  // multiplier 
      if (col == 9) return new Integer(le.getQsoPoints());  // qso pts
    } else if ( l instanceof QTCEntry) {
      QTCEntry qe = (QTCEntry) l;
      if (col == 0) return qe.getGroupNumber() + "/" + qe.getGroupTotal();
      if (col == 4) return qe.getRecipient();
      if (col == 5) return qe.getAboutCallsign();
      if (col == 6) return qe.getQSOTime() + " :: " + qe.getQSOSerial();
      if (col == 7) { if (qe.isCompleted()) return "Q";
                      else return " ";}
      if (col == 8) return " ";
      if (col == 9) { if (qe.isCompleted()) return "1";
                      else return "0";}
    }
    return "BOGUS" ; // shouldn't get here
  }

  public String getColumnName(int col){
    return headings[col]; 
  }

  public boolean isCellEditable(int row, int col) {
    if ( row >= veclog.size() ) return false;
    Loggable l = (Loggable) veclog.get( row ); 
    if (l instanceof QTCEntry) return false;
    if ( col >= 3  && col < 7) return true;
    else return false ;
  }

  public void setValueAt(Object value, int row, int col) {
    if ( row >= veclog.size() ) return; // can't edit fake rows
    Loggable l = (Loggable) veclog.get( row ); 
    if (l instanceof QTCEntry) return; // can't edit QTC
    LogEntry le = (LogEntry) l;
    if (col == 3)  le.setBand( (String) value );
    if (col == 4)  le.getSent().addToExchange( (String) value );
    if (col == 5)  le.getRcvd().addToExchange( (String) value );
    if (col == 6)  le.getRcvd().addToExchange( (String) value );
    veclog.set(row, le);  // not needed?
    fireTableRowsUpdated(row, row);
  }

}