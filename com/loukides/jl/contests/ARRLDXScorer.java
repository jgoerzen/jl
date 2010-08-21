// Scorer for ARRL DX Contest.  US/Canada side
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.util.*;

public class ARRLDXScorer extends GenericScorer {

    protected boolean isWorkable(LogEntry le) {
      if (le.getRcvd().getCountry().equalsIgnoreCase("united states") ||
		  le.getRcvd().getCountry().equalsIgnoreCase("canada")) return false;
      return true;
    }

  protected char encodeMult(LogEntry le) {
    boolean c = le.isCountryMultiplier();
    boolean q = le.isCountryQuery();
    if ( c == false  && q == false ) return '_';
    if ( c == true   && q == false ) return 'c';
    if ( c == false  && q == true  ) return '?';
    if ( c == true   && q == true  ) return '?';
    return '_';
  }

  protected String getMultName(LogEntry le) {
    return le.getRcvd().getCountry();
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
      // System.out.println("Validator called (ARRLDXScorer) " + v);
      return v;
    }
  }

  public java.awt.Component getSummaryDisplay() { return sd; }

}
