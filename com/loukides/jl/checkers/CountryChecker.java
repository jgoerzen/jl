// A checker for countries defined in a country list
//
// the checker sets the country and the multiplier flags, but NOT
// the multchar itself.
package com.loukides.jl.checkers;
import com.loukides.jl.jl.*;
import com.loukides.jl.gadgets.*;
import com.loukides.jl.util.*;

import java.util.*;
import java.io.*;

// Extends arbitrary checker; we inherit the report writer and  
// other useful things, like visual mult display.
// Prefix decoding now takes place in the Callsign class, but there's 
// still some more-complex-than-necessary fiddling around with multiplier flags.
public class CountryChecker extends ArbitraryChecker {

  private static HashMap countrytable;
  private static int totalEntities = 0;
  private String filename;
  private String mode;
  private boolean updateVisual = false;
  private MultListModel mlm = null;
  private VisualMult vm;

  public void setProperties(Properties p) {
    super.setProperties(p);
    mode = (String)p.get("mode"); 
    mlm = new AdditiveMultListModel(null, p, this);
    vm = new VisualMult(mlm);
  }

  public int getTotal() { return totalEntities; }

  public boolean isValidMult(LogEntry le) {
    return ( ! le.getRcvd().getCountry().equals("") );
  }

  public boolean isNew( LogEntry le ) {
    String country, zone;
    country = le.getRcvd().getCountry();
    zone = le.getRcvd().getZone();
    // System.out.println("isNew: " + country);
    // unknown country NEVER new, but marked questionable  
    if ( country.equals("") ) {  
      le.setCountryMultiplier(false);
      le.setCountryQuery(true);
      return false; 
    }
    String key = makeMultKey(le) ;
    // System.out.println("isNew: "+le.getCountry()+"|" 
    //                   + le.getBand() + "|"  + key + "|"+ le.getName()+"|");
    // System.out.println( "keys: " + worked.keySet() );
    if ( ! worked.containsKey(key) ) {      // new country on this band
      le.setCountryMultiplier(true); 
      le.setCountryQuery(false);
      return true;
    } else {                                   // otherwise OK, but not new
      le.setCountryMultiplier(false); 
      le.setCountryQuery(false);
      return false;
    }
  }

  protected String makeMultKey(LogEntry le, String b, String m) {
    String band = "";
    String mode = "";
    if ( perbandmults ) band = b;
    if ( permodemults ) mode = m;
    return band.toLowerCase() + "  " +
           mode.toLowerCase() + "  " +
           le.getCountry().toLowerCase();
  }

  protected String makeMultKey(LogEntry le) {
    return makeMultKey(le, le.getBand(), le.getMode());
  }

  public void addEntry( LogEntry le ) {
    // we omit lots of multiplier field setting--that's done by isNew().
    // Last time, I thought I had a reason for duplicating that here.
    // BUt since we start by calling isNew(), I don't see it.
    boolean newmult = this.isNew(le); 
    if ( newmult  ) {
      String key = makeMultKey(le) ; 
//      System.out.println(" putting in mult table: key: " + key) ;  
      worked.put( key, le );
      updateDisplay(le, le.getCountry());
      totalEntities++; 
    }
  }

  public void clear() {
    worked.clear();
    totalEntities = 0; 
    mlm.setupVisual(); 
  }

  public void setUpdateVisual(boolean b) { 
    updateVisual = b;
    mlm.setUpdateVisual(b);
  }

  protected void updateDisplay(LogEntry le, String c) {
    mlm.updateDisplay(le, c);
  }

  public Vector getNeeded(LogEntry le) { 
    // System.out.println("CC::getNeeded");
//    if ( ! updateVisual ) return null;
    Vector v = new Vector();
    StringTokenizer st = new StringTokenizer(bands);
    String currentband = "";
    String hashkey;
    while ( st.hasMoreTokens()) {
      currentband = st.nextToken();
      hashkey = makeMultKey( le, currentband, le.getMode() );
      if ( ! worked.containsKey(hashkey) ) v.add(currentband);
      // System.out.println("|" +currentband +"|"+ hashkey +"|"+ worked.containsKey(hashkey));
    }
    //System.out.println(v);
    return v;
  }


}