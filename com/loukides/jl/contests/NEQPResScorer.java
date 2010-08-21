// Scorer for NEQP.  
package com.loukides.jl.contests;
import com.loukides.jl.jl.LogEntry;
import java.util.*;

public class NEQPResScorer extends GenericDoubleMultScorer {

  private boolean isWorVE(String c) {
    if (c.equalsIgnoreCase("united states")) return true;
    if (c.equalsIgnoreCase("canada")) return true;
    if (c.equalsIgnoreCase("hawaii")) return true;
    if (c.equalsIgnoreCase("alaska")) return true;
    return false;
  }

  protected boolean useMultChecker1(LogEntry le) {
    return ! isWorVE(le.getCountry());
  }

  protected char encodeMult(LogEntry le) {
    boolean c = le.isCountryMultiplier();
    boolean s = le.isMultiplier();
    boolean q = le.isCountryQuery();
    // System.out.println("encode: c: " + c + " s: " + s + " q: " + q); 
    // note that the multiplier bits don't tell the whole story...
    if ( ! le.getRcvd().getMultiplierField().equals("DX")) {  //domestic
      if ( s ) return 's';
      else return '_';
    }
    else  {  // not-domestic
      if ( c && !q ) return 'c';
      else if ( c && q ) return '?';
      else return '_';
    }
 }

  protected int findPoints(LogEntry le) {
    if ( le.getMode().equals("PH") ) return 1;
    if ( le.getMode().equals("CW") ) return 2; 
    if ( le.getMode().equals("RY") ) return 2;
    return 0;
  }

  protected String getMultName(LogEntry le) {
    if (isWorVE(le.getCountry())) return le.getName();
    else return le.getCountry();
  }

}
