// Scorer for ARRL 160 meter contest.  Multiplier checking is 
// rather shaky...  Some strange gyrations about getting the country info
// in the right place (setMultiplierField())
package com.loukides.jl.contests;

import com.loukides.jl.jl.LogEntry;
import java.util.*;

public class ARRL160Scorer extends GenericDoubleMultScorer {

  private boolean isDomestic(String country) {
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
    else if ( c && !q ) return 'c';
    else if ( c && q ) return '?';
    else return '_';
  }

  protected String getMultName(LogEntry le) {
    if (isDomestic(le.getRcvd().getCountry())) return le.getName(); 
    else return le.getRcvd().getCountry();
  }

  protected int findPoints(LogEntry le) {
    if (isDomestic(le.getCountry())) return 2;
    else return 5;
  }

  protected String getBandCategory() { return "160M"; }

  // return the vector of keys that don't have valid values for this contest
  public static class PropertyValidator extends AbstractScorer.PropertyValidator {
    public Vector validateOperation(Properties p) {
      Vector v = super.validateOperation(p);
      if (p.getProperty("state", "").equals("")) v.add("state");
      if (p.getProperty("arrlSection", "").equals("")) v.add("arrlSection");
      String pwr = p.getProperty("powerCategory", "");
      String cat = p.getProperty("category", "");
      String mode = p.getProperty("modeCategory", "");
      if (pwr.equals("")) v.add("powerCategory");
      if (cat.equals("") || cat.equals("MULTI-TWO") || 
          cat.equals("MULTI-MULTI") || cat.equals("MULTI-UNLIMITED") ||
          cat.equals("MULTI-LIMITED") || cat.equals("ROVER") ||
          cat.equals("SCHOOL-CLUB")  || cat.equals("SINGLE-OP-PORTABLE")) 
        v.add("category");
      int nops = new StringTokenizer(p.getProperty("operators", "")).countTokens();
      if ( (nops == 1 && cat.equals("MULTI-ONE"))  ||
           (nops >= 2 && ! cat.equals("MULTI-ONE")) ) {
        v.add("category");
        v.add("operators");
      }
      if (p.getProperty("modeCategory", "").equals("")) v.add("modeCategory");
      // System.out.println("Validator called (ARRLDXScorer) " + v);
      return v;
    }
  }


}