// A checker that does no validity checking.
// most of the checking logic is in here; ideally, you only have to 
// provide isValidMult() to have a working checker.  (But in most case, you need to 
// implement isNew() and addEntry()).  It may be possible (and
// desirable) to decompose makeMultKey().   Could eventually incorporate
// a suitably arbitrary visual mult display.

// Currently subclassed by EnumeratedChecker and ListedStringChecker.
// These are pretty typical--LSC adds visual, EC does the minimum.
// Also subclassed by CountryChecker, but that overrides everything
// except the mult reporter.  
// Used as-is as a checker for grid squares.  This could cause problems--
// unless overridden, might not handle 6 vs 4 grids correctly.  (6 currently
// rewritten to 4 in the exchange)

// NOTE:  Watch out for access control on fields.  Fields must be 
// protected or public or friendly to be visible to a subclass.

// MOTE:  Also, the checker sets the multiplier property of le, but not
// the multChar.  (This is one reason that it's important for CountryChecker
// to override--countries have their own countryMultiplier field, which
// has to be maintained separately.) 
package com.loukides.jl.checkers;
import com.loukides.jl.jl.*;
import com.loukides.jl.gadgets.*;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;

public class ArbitraryChecker implements Checker {

  protected Properties p = new Properties();
  protected boolean updateVisual = false; 
  protected HashMap worked = new HashMap(1000);
  protected boolean perbandmults;
  protected boolean permodemults;
  protected boolean displayMultAbbrev;
  protected String bands = "";
  protected String mode = "";

  public void setProperties(Properties p) {
    this.p = p;
    if ( p.getProperty("perBandMultiplier", "true").equals("false") ) 
      perbandmults = false; 
    else perbandmults = true;
    if ( p.getProperty("perModeMultiplier", "true").equals("false") ) 
      permodemults = false; 
    else permodemults = true;
    if ( p.getProperty("displayMultAbbrev", "false").equals("false") ) 
      displayMultAbbrev = false; 
    else displayMultAbbrev = true;
    bands = p.getProperty("bands", "");
  }

  public void clear() {
    worked.clear();
  }

  // return total number of entries in the checker
  public int getTotal() { return worked.size();}

  protected String makeMultKey(LogEntry le, String band, String mode) {
    String b = "";
    String m = "";
    if ( perbandmults ) b = band;
    if ( permodemults ) m = mode;
    // System.out.println("perband: " +perbandmults+" permode: "+permodemults);
    // System.out.println("makekey: " + m + " " + le.getMode());
    // band first to facilitate dupe sheet
    return (b + " " + m + " " 
              + le.getRcvd().getMultiplierField()).toLowerCase(); 
  }

  protected String makeMultKey(LogEntry le) {
    return makeMultKey(le, le.getBand(), le.getMode() );
  }

  public boolean isNew( LogEntry le ) {
    boolean rv = false;
    String k = makeMultKey(le);
    if ( (! worked.containsKey(k)) && isValidMult(le)) { // not worked yet...
      le.setMultiplier(true);
      rv = true;
    } else {
      le.setMultiplier(false);
      rv = false; 
    }    
    // System.out.println("ArbChecker::isNew: " + k + " " + rv);
    return rv;
  }

  public boolean isDupe(LogEntry le) { return ! isNew(le); }

  // like other methods, sets as many of the fields of the log entry
  // as we know about
  public void addEntry(LogEntry le) {
    String k = makeMultKey(le);
    if ( ! isValidMult(le) ) {  // invalid; no mult points
      // System.out.println("AddEntry-invalid");
      le.setMultiplier(false);
      return;
    }
    if ( ! le.isComplete() ) {  // whatever it might have said, it doesn't
      // System.out.println("AddEntry-incomplete");
      le.setMultiplier(false);  // count if it's not complete
      return;
    }
    if ( isNew(le) ) { // not worked yet and valid; add to table
      // System.out.println("AddEntry-valid " + k);
      le.setMultiplier(true);
      worked.put( k, le );
      if (displayMultAbbrev) 
        updateDisplay(le, le.getRcvd().getMultiplierField());
      else 
        updateDisplay(le, le.getName());
    } else {   // just plain not new
      // System.out.println("AddEntry-not considered new" + k);
      le.setMultiplier(false);
    }
  } 

  // for an arbitrary checker, all mults are valid
  public boolean isValidMult(LogEntry le) { 
    if ( le.getRcvd().getMultiplierField().equals("") 
         || le.getRcvd().getMultiplierField() == null ) return false;
    return true; 
  }

  protected void updateDisplay(LogEntry le, String s) {};

  public void setUpdateVisual(boolean b){ updateVisual = b ;}

  public void writeReport(PrintWriter pw) {
    int total = 0;
    int bandtotal = 0;
    int linelength = 0;
    String current = "";
    
    // keys from the country mult table
    Object [] keys = worked.keySet().toArray(); 
    Arrays.sort(keys);

    for ( int i =0; i < keys.length; i++) {
      String band = "";
      String mname = "";
      String t = (String) keys[i];
      // in the true general case, we don't know how many tokens to find...
      StringTokenizer toks = new StringTokenizer(t.trim());

      if ( perbandmults && ! permodemults ) 
        { band = toks.nextToken(); mname = restOfTokenizer(toks); }  
      else if ( perbandmults &&   permodemults ) 
        { band = toks.nextToken() + " " + toks.nextToken(); 
          mname = restOfTokenizer(toks); }  
      else if ( ! perbandmults && ! permodemults ) 
        { band = " "; mname = restOfTokenizer(toks); }  
      else if ( ! perbandmults && permodemults ) 
        { band = toks.nextToken(); mname = restOfTokenizer(toks); }  
      // System.out.println("Reporter: |" + band + "| |" + mname + "| |" + t + "|");

      if ( ! band.equals(current)) { 
        pw.println("");
        pw.println("Total on band " +  current + ": " + bandtotal);       
        bandtotal = 0;
        pw.println("");
        pw.println("Entities worked on " + band + " meters:");
      }
      current = band;
      total++; bandtotal++;
      linelength += mname.length() +3;
      pw.print(mname + "   ");
      if (linelength > 70)  {
        pw.println("");
        linelength = 0;
      }
    }
    pw.println("");
    pw.println("Total on band " +  current + ": " + bandtotal);       
  }

  public Vector getNeeded( LogEntry le) {
    // System.out.println("AC::getNeeded");
    Vector v = new Vector();
    StringTokenizer st = new StringTokenizer(bands);
    String currentband = "";
    String hashkey;
    while ( st.hasMoreTokens()) {
      currentband = st.nextToken();
      hashkey = makeMultKey( le, currentband, le.getMode() );
      if ( ! worked.containsKey(hashkey) ) v.add(currentband);
      // System.out.println("|" + currentband + "|" + hashkey + "|" + worked.containsKey(hashkey));
    }
    // System.out.println(v);
    return v;
  }

  private String restOfTokenizer(StringTokenizer st) {
    String s = "";
    while (st.hasMoreTokens())  s += " " + st.nextToken() ;
    return s.trim();
  }
}