// scorer for the cq wpx contest.  Most of the functionality is in 
// GenericSerialScorer.  
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;

import java.util.*;

public class CQWPXScorer extends GenericScorer {

  private String myCountry = ""; 
  private String myZone = "";

  public void setProperties(Properties p) {
    super.setProperties(p);
    myCountry = p.getProperty("dxccCountry");
    myZone = p.getProperty("cqZone");
  }


  protected char encodeMult(LogEntry le) {
    boolean s = le.isMultiplier();
    if ( s ) return 'm';
      else return '_';
  }

  // should be location-independent
  protected int findPoints(LogEntry le) { 
    if ( le.getCountry().equalsIgnoreCase(myCountry)) return 1;  // same country
    boolean isNA = le.getRcvd().getContinent().toUpperCase().equals("NA");
    boolean sameContinent = le.getRcvd().getContinent().equals(le.getSent().getContinent());
    String band = le.getBand();
    boolean lowbands = band.equalsIgnoreCase("b160") 
                    || band.equalsIgnoreCase("b80")
                    || band.equalsIgnoreCase("b40");
    if ( lowbands )            // 160, 80, 40
      if (sameContinent) 
        if ( isNA ) return 4;  // same continent and NA
        else return 2;         // same continent and not NA
      else return 6;           // different continent
    else                       // 40, 20, 10
      if (sameContinent) 
        if ( isNA ) return 2;  // same continent and NA
        else return 1;         // same continent and not NA
      else return 3;           // different continent
  }

  protected String getMultName(LogEntry le) {
    return le.getRcvd().getMultiplierField();
  }

  // return the vector of keys that don't have valid values for this contest
  public static class PropertyValidator extends AbstractScorer.PropertyValidator {
    public Vector validateOperation(Properties p) {
      Vector v = super.validateOperation(p);
      if (p.getProperty("state", "").equals("")) v.add("state");
      if (p.getProperty("arrlSection", "").equals("")) v.add("arrlSection");
      String cat = p.getProperty("category", "");
      if (cat.equals("") || cat.equals("ROVER") || cat.equals("SCHOOL-CLUB") ||
          cat.equals("SINGLE-OP-PORTABLE") || cat.equals("MULTI-LIMITED") ||
          cat.equals("MULTI-UNLIMITED") ) v.add("category");
      if (p.getProperty("bandCategory", "").equals("")) v.add("bandCategory");
      if (p.getProperty("powerCategory", "").equals("")) v.add("powerCategory");
      return v;
    }
  }

  public java.awt.Component getSummaryDisplay() { return sd; }

}