// Scorer for Russian DX contest.  Relies on a special cty.dat file that includes 
// oblasts.  Although this file comes from the rdx organizers, I don't quite trust it. 
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;

import java.util.*;

public class RUDXScorer extends GenericScorer {

  protected char encodeMult(LogEntry le) {
    // oblasts are in the country table; new mult and Russian == new oblast
    boolean c = le.isCountryMultiplier();
    boolean q = le.isCountryQuery();
    // System.out.println("encode: c: " + c +  " q: " + q); 
    if (((RUDXExchange)le.getRcvd()).isRussian()) {
      if ( c ) return 'o';
      else return '_';
    }
    else {
      if ( c && !q ) return 'c';
      else if ( c && q ) return '?';
      else return '_';
    }
  }

  protected int findPoints(LogEntry le) {
    if ( ((RUDXExchange)le.getRcvd()).isRussian() ) return 10;
    if ( le.getRcvd().getCountry().equalsIgnoreCase("United States") ) return 2;
    if ( le.getRcvd().getContinent().equalsIgnoreCase("NA") && 
         ! le.getRcvd().getCountry().equalsIgnoreCase("United States") ) return 3;
    return 5;
  }

  public static class PropertyValidator extends AbstractScorer.PropertyValidator {
    // Returns properties keys that are NOT valid given the current contest
    public Vector validateOperation(Properties p) {
      Vector v = super.validateOperation(p);
      String cat = p.getProperty("category", "");
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

  // tweak the string we display so that the oblast's abbrev comes first
  protected String getMultName(LogEntry le) {
    String oblast = le.getRcvd().getCountry();
    int paren = oblast.indexOf((int)'(');
    if (paren == -1) return oblast;
    return oblast.substring(paren +1, oblast.length() -1).toUpperCase() + ": " 
         + oblast.substring(0, paren);
  }

}
