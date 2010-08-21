// Scorer for cq160, similar to naqp.  Multiplier checking is 
// rather shaky...  Some strange gyrations about getting the country info
// in the right place
package com.loukides.jl.contests;
import com.loukides.jl.jl.LogEntry;
import java.util.*;

public class CQ160Scorer extends GenericDoubleMultScorer {

  private boolean isDomestic(String country) {
    // System.out.println("CQ160Scorer::isDom: " + country);
    return country.equalsIgnoreCase("united states") 
       || country.equalsIgnoreCase("canada") ;
  }

  protected boolean useMultChecker1(LogEntry le) {
    return ! isDomestic(le.getRcvd().getCountry());
  }

  protected char encodeMult(LogEntry le) {
    boolean c = le.isCountryMultiplier();
    boolean s = le.isMultiplier();
    boolean q = le.isCountryQuery();
    // System.out.println("encode: c: " + c + " s: " + s + " q: " + q); 
    // note that the multiplier bits don't tell the whole story...
    if (isDomestic(le.getCountry())) {
      if ( s ) return 's';
      else return '_';
    }
    else {
      if ( c && !q ) return 'c';
      else if ( c && q ) return '?';
      else return '_';
    }
  }

  protected String getMultName(LogEntry le) {
    if (isDomestic(le.getRcvd().getCountry())) return le.getName(); 
    else return le.getRcvd().getCountry();
  }

  protected int findPoints(LogEntry le) {
    if ( ! (le.getRcvd().isComplete() ) ) return 0;
    if ( le.getRcvd().getCountry().equalsIgnoreCase("united states")) return 2;
    if ( le.getRcvd().getContinent().equalsIgnoreCase("NA") ) return 5;
    return 10;
  }

  protected String getBandCategory() { return "160M"; }

  // return the vector of keys that don't have valid values for this contest
  public static class PropertyValidator extends AbstractScorer.PropertyValidator {
    public Vector validateOperation(Properties p) {
      Vector v = super.validateOperation(p);
      if (p.getProperty("state", "").equals("")) v.add("state");
      // System.out.println("Validator called (CQ160Scorer) " + v);
      return v;
    }
  }

}
