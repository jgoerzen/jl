package com.loukides.jl.contests;
import com.loukides.jl.jl.*;

import java.util.*;

public class SSScorer extends GenericScorer {

  protected int findPoints(LogEntry le) {
    return 2;
  }

  protected char encodeMult(LogEntry le) {
    if (le.isMultiplier()) return 'm';
    return '_';
  }

  // isWorkable would be meaningful but something of a pain

  protected String getBandCategory() { return "ALL"; }

  // return the vector of keys that don't have valid values for this contest
  public static class PropertyValidator extends AbstractScorer.PropertyValidator {
    public Vector validateOperation(Properties p) {
      Vector v = super.validateOperation(p);
      if (p.getProperty("arrlSection", "").equals("")) v.add("arrlSection");
      if (p.getProperty("state", "").equals("")) v.add("state");
      String pwr = p.getProperty("powerCategory", "");
      String cat = p.getProperty("category", "");
      if (pwr.equals("")) v.add("powerCategory");
      if (cat.equals("")            || cat.equals("MULTI-TWO")       || 
          cat.equals("MULTI-MULTI") || cat.equals("MULTI-UNLIMITED") ||
          cat.equals("ROVER") ) v.add("category");
      int nops = new StringTokenizer(p.getProperty("operators", "")).countTokens();
      if ( (nops == 1 && cat.equals("MULTI-ONE"))  ||
           (nops >= 2 && ! cat.equals("MULTI-ONE")) ) {
        v.add("category");
        v.add("operators");
      }
      String check = p.getProperty("check", "");
      if (check.equals("") || check.length() > 2) v.add("check");
      // System.out.println("Validator called (SSSCorer) " + v);
      return v;
    }
  }

  public java.awt.Component getSummaryDisplay() { return sd; }

}