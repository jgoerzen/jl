// new style dupe checker with history lookup
// hmmm what happens when you reprocess the log?  clear() should (at least)
// make sure the history display is cleared.
package com.loukides.jl.checkers;
import com.loukides.jl.jl.*;
import com.loukides.jl.gadgets.*;
import com.loukides.jl.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

public class DupeChecker implements Checker {

  private HashMap h;
  int initentries;
  Properties p = new Properties();
  private EditableLogModel elm;
  private HistoryWindow hw;
  private boolean ignoredupe;
  private boolean perbanddupe;
  private boolean permodedupe;
  private boolean hasrovers;
  private boolean amrover;
  private char roverchar;
  private Vector contacts;
  private boolean updateVisual = true;

  public DupeChecker() {
    h = new HashMap(1000,100);
    elm = new EditableLogModel();
    elm.setLog(new Vector());
    hw = new HistoryWindow(elm);
  }

  public void clear() {
    h.clear();
  }

  public void setProperties(Properties p) {
    this.p = p;
    ignoredupe = p.getProperty("ignoreDupe", "false").equals("true");
    perbanddupe = p.getProperty("perBandDupe", "false").equals("true");
    permodedupe = p.getProperty("perModeDupe", "false").equals("true");
    hasrovers =   p.getProperty("hasRovers", "false").equals("true");
    roverchar = p.getProperty("roverDesignation", "R").charAt(0);
    amrover = p.getProperty("category", "SINGLE-OP").equals("ROVER");
    // System.out.println("props: "+ perbanddupe + permodedupe + hasrovers);
  }

  // methods from Checker; in a dupe checker, should never be called
  public boolean isValidMult(LogEntry le) { return false; }

  //  hmmm.  the rewrite screws up the getTotal() method.  (Don't think
  // it's used, though, so should trash it.
  public int getTotal() { return h.size() ; }

  public void writeReport(PrintWriter pw) {}
  public void setUpdateVisual(boolean v) { updateVisual = v ; }
  public boolean isNew( LogEntry l) { return ! isDupe(l) ; }

  // checks whether l is a duplicate entry, AND (unsightly) displays 
  // prev. contacts with l in the pre. contacts window.
  public boolean isDupe( LogEntry l ){
    Enumeration history;
    LogEntry currentqso;

    if ( l == null ) return false; // probably some other bug, but no dupe
    String call = l.getRcvd().getCallsign();
    if ( call.equals("") ) return false; 

    // get the vector of all contacts with this call
    contacts = (Vector)h.get(findBase(call));
    elm.setLog(contacts);
    if (updateVisual) elm.fireTableDataChanged();
    if (contacts == null ) return false; // nothing in table, so not a dupe
    // if there are rovers, and this call is a rover, you can't dupe until 
    // you have a complete exchange.
    if ( hasrovers && isRover(call) && ! l.isComplete() ) return false;

    history = contacts.elements();
    while (history.hasMoreElements()) {
      boolean equalband, equalmode, equalmult, equalsentmult;
      currentqso = (LogEntry)history.nextElement();
      equalband = currentqso.getBand()
                              .equalsIgnoreCase(l.getBand());
      equalmode = currentqso.getMode()
                              .equalsIgnoreCase(l.getMode());
      // roverfield is USUALLY the same as multiplier field
      equalmult = currentqso.getRcvd().getRoverField()
                              .equalsIgnoreCase(l.getRcvd().getRoverField());
      // amrover is really superfluous, since only a rover would ever change
      // its exchange info.  However, it's probably good to be defensive...
      if (amrover && hasrovers) 
        equalsentmult = currentqso.getSent().getRoverField()
                              .equalsIgnoreCase(l.getSent().getRoverField()); 
      else equalsentmult = true;
      // System.out.println("dc: " + amrover + " " + hasrovers + " " 
      //         + currentqso.getSent().getRoverField() + " "
      //         + l.getSent().getRoverField() + equalsentmult);
      //
      // this could reduce to one terribly complex logical expression
      // but basically, the band AND the mode match...
      // System.out.println("dupe: " + perbanddupe + " " + permodedupe + " "
      //                             + equalband + " " + equalmode);
      // Handle rovers first; then regulars
      if (hasrovers && isRover(call)) {
        if ( perbanddupe && permodedupe ) {
          if ( equalband && equalmode && equalmult && equalsentmult ) return true;
        }
        else if ( perbanddupe && !permodedupe) {
          if ( equalband && equalmult && equalsentmult ) return true;
        }
        else if ( !perbanddupe && permodedupe ) {
          if (equalmode && equalmult && equalsentmult ) return true;
        }
        else 
          // only one contact allowed; and if we get here, we know that we 
          // found a matching call in the dupe table
          if ( !permodedupe && !perbanddupe && equalmult && equalsentmult) return true;
      }
      else if ( perbanddupe && permodedupe ) {
        if ( equalband && equalmode && equalsentmult ) return true;
      }
      else if ( perbanddupe && !permodedupe) {
        if ( equalband && equalsentmult ) return true;
      }
      else if ( !perbanddupe && permodedupe ) {
        if (equalmode && equalsentmult ) return true;
      }
      else 
        // only one contact allowed; and if we get here, we know that we 
        // found a matching call in the dupe table
        if ( !permodedupe && !perbanddupe && equalsentmult ) return true;
    }
    return false; // if we get through all of this, we know it's not a dupe 
  }

  public void addEntry( LogEntry l) {
    if ( l != null && l.isComplete() ) {  // null or incomplete entries not duped
      boolean d = isDupe(l);
      l.setDupe(d);
      if ( d && ! ignoredupe) return ;
      String key = findBase(l.getRcvd().getCallsign());
      contacts = (Vector)h.get(key);
      if ( contacts == null ) contacts = new Vector();
      contacts.add(l);
      h.put( key , contacts ); 
    }
  }

  private boolean isRover(String call) {
    int slash = call.indexOf('/'); 
    if ( slash == -1 ) return false;
    if ( slash != call.length() -2) return false; // for rover / must be 2nd-last
    char rov = call.charAt( slash + 1 );
    // System.out.println( "isRover: " + slash + " " + call.length() + " " + rov);
    if ( rov == roverchar ) return true;
    return false;
  }

  public static String findBase( String call ) {
    // Makes the (poor?) assumption that the longer part is the actual 
    // call (as oppposed to /p, /hk0, etc.)
    // (the logic is that if we log both n1khb and n1khb/p, it's still a 
    // dupe.  It might be clever to "believe" the longer call and reset the log
    // accordingly.)
    String basecall;
    int slash = call.indexOf('/'); 
    if ( slash != -1 ) {
      String p1 = call.substring(0, slash);
      String p2 = call.substring(slash +1, call.length());
      if ( p1.length() > p2.length() ) basecall = p1 ; 
      else                             basecall = p2 ;
    } else                             basecall = call;
//    System.out.println( basecall );  //debug
    return basecall;
  }

  public Vector getNeeded(LogEntry le) { return null; }

}