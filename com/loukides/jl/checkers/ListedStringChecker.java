// a general checker that checks against a reasonably short list of fixed
// strings (e.g., arrl sections, states, counties).
//LOTS of extra computation in this class...
// Routines in this class GENERALLY set as many fields in the log entry
// as they have knowledge about.
package com.loukides.jl.checkers;
import com.loukides.jl.jl.*;
import com.loukides.jl.gadgets.*;
import com.loukides.jl.util.*;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;

public class ListedStringChecker extends ArbitraryChecker {

  protected HashMap mults = new HashMap(100);   // abbreviation, full name; never deleted
  // Vector visual; 
  boolean updateVisual = true;
  protected VisualMult vm;
  protected MultListModel mlm = null;

  public void setProperties(Properties p) {
    // System.out.println("LSC: setprops");
    super.setProperties(p);
    try {
      BufferedReader fis = new BufferedReader( 
        new FileReader("data/" + p.getProperty("multiplierList")));
      // load list of entities
      readlist( fis );
      fis.close();
      // System.out.println("read mult list");
    } catch (IOException e) { System.out.println( e );}
    if ( p.getProperty("perBandMultiplier").equals("true") )
      mlm = new AdditiveMultListModel( mults, p, this);
    else 
      mlm = new SubtractiveMultListModel( mults, p, this);
    vm = new VisualMult(mlm);
  }

  private void readlist( BufferedReader br) throws IOException {
    String nextline;
    String abbrev;
    String section;
    boolean abbrevfirst = p.getProperty("abbrevFirst", "false").equals("true");
    int b1 = Integer.parseInt(p.getProperty("multiplierListBreak", "0"));
    while ( null != (nextline = br.readLine()) ) {
      // sigh.  all the case wrangling and trimming multiple times is not
      // clean, but touching it tends to break things.
      nextline = nextline.toLowerCase();
      if ( ! nextline.equals("") ) {
        if (abbrevfirst) {
          StringTokenizer st = new StringTokenizer(nextline);
          abbrev = st.nextToken().trim();
          section = nextline.substring(abbrev.length()).trim();
        } else {
          // System.out.println("Line: " + "|" + nextline + "|");
          section = nextline.substring(0, b1).trim();
          abbrev = nextline.substring(b1+1).trim();
        }
        mults.put( abbrev, section );
        // System.out.println("|" + abbrev + "| |" + section + "|");
      }
    }
  } 

  public boolean isValidMult(LogEntry le) {
    // NB:  we DON'T compute a key; we are only finding out if the 
    // multiplier is potentially valid, and that doesn't depend on
    // band and mode
    String s = le.getRcvd().getMultiplierField().toLowerCase();
    String name = (String)mults.get(s);
    if ( name == null ) {
      // System.out.println("invalid mult: " + s + " " + name);
      le.setName( "" );
      return false;
    }
    // System.out.println("valid mult " + name);
    le.setName( name );
    return true;
  }

  public void clear() {
    worked.clear();           // reset internal tables
    mlm.setupVisual();
  } 

  // Could just inherit, but this report is somewhat nicer than usual
  // though not really useful...
  public void writeReport(PrintWriter o) {
    o.println();
    Object [] mlist = worked.keySet().toArray();
    Arrays.sort(mlist);
    for (int i =0; i < mlist.length; i++)  {
      String k = (String) mlist[i];
      LogEntry le = (LogEntry)worked.get(k);
      o.println( k.toUpperCase() + " " + le.getName() + " " 
                 + le.getRcvd().getCallsign() + " " 
                 + le.getDate() + " " + le.getSent().getSerial());
//      System.out.println(mlist[i]);
    }
  }

  public void setUpdateVisual(boolean b) {
    mlm.setUpdateVisual(b);
  }

  protected void updateDisplay(LogEntry le, String name) {
    mlm.updateDisplay(le, name);
  }

}
