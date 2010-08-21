// Scorer for NAQP.  Multiplier checking is 
// rather shaky...  Some strange gyrations about getting the country info
// in the right place
package com.loukides.jl.contests;
import com.loukides.jl.jl.LogEntry;
import java.util.*;

public class NAQPScorer extends GenericDoubleMultScorer {

  protected boolean isDomestic(LogEntry le) {
    // System.out.println("NAQPScorer::isDom: " + country);
    return ((NAQPExchange)le.getRcvd()).isDomestic();
  }

  protected boolean isNorthAmerica(LogEntry le) {
    return ((NAQPExchange)le.getRcvd()).isNorthAmerica();
  }

  protected boolean useMultChecker1(LogEntry le) {
    return ! isDomestic(le) && isNorthAmerica(le);
  }

  protected boolean useMultChecker2(LogEntry le) {
    return isDomestic(le);
  }

  protected String getMultName(LogEntry le) {
    if (isDomestic(le)) return le.getName();
    if (isNorthAmerica(le)) return le.getRcvd().getCountry();
    else return "DX";
  }

  protected char encodeMult(LogEntry le) {
    boolean c = le.isCountryMultiplier();
    boolean s = le.isMultiplier();
    boolean q = le.isCountryQuery();
    // System.out.println("encode: c: " + c + " s: " + s + " q: " + q); 
    // note that the multiplier bits don't tell the whole story...
    if (isDomestic(le)) {
      if ( s ) return 's';
      else return '_';
    }
    else if (isNorthAmerica(le) ) {
      if ( c && !q ) return 'c';
      else if ( c && q ) return '?';
      else return '_';
    }
    return '_';  // non-NA
  }

  protected String getPowerCategory() { 
    return "LOW";  // only category allowed for this contest
  }

  protected String getBandCategory() {
    return "ALL";  // only category allowed for this contest
  }

  public static class PropertyValidator extends AbstractScorer.PropertyValidator {
    // Returns properties keys that are NOT valid given the current contest
    public Vector validateOperation(Properties p) {
      Vector v = super.validateOperation(p);
      if (p.getProperty("state", "").equals("")) v.add("state");
      if (p.getProperty("firstname", "").equals("")) v.add("firstname");
      String cat = p.getProperty("category", "");
      // only single and multi-two categories
      if ( ! ( cat.equals("SINGLE-OP") || cat.equals("MULTI-TWO") ) ) v.add("category");
      // System.out.println(cat);
      // System.out.println("Validator called (NAQPScorer) " + v);
      int nops = (new StringTokenizer(p.getProperty("operators", ""))).countTokens();
      if ( nops >= 2 && !  cat.equals("MULTI-TWO") ){
        v.add("category");
        v.add("operators");
      }

      return v;
    }
  }

  public java.awt.Component getSummaryDisplay() { return sd; }


}
