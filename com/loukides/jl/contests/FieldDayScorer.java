package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import java.util.*;

public class FieldDayScorer extends GenericScorer {

  protected int findPoints(LogEntry le) { 
    if ( le.getMode().equals("CW") ) return 2; 
    if ( le.getMode().equals("PH") ) return 1; 
    if ( le.getMode().equals("DG") ) return 2; 
    return 0;
  }

  protected char encodeMult(LogEntry le) { return ' '; }

  protected int getNewMultTotal(LogEntry le) { return 1; }

  // return the vector of keys that don't have valid values for this contest
  public static class PropertyValidator extends AbstractScorer.PropertyValidator {
    public Vector validateOperation(Properties p) {
      Vector v = super.validateOperation(p);
      if (p.getProperty("state", "").equals("")) v.add("state");
      if (p.getProperty("arrlSection", "").equals("")) v.add("arrlSection");
      String fdc = p.getProperty("fieldDayCategory", "").toLowerCase();
      try {
        int ntx = Integer.parseInt(fdc.substring(0,fdc.length()-1));
      } catch (Exception e) {
        v.add("fieldDayCategory"); // field day category must be misset somehow
        return v;
      }
      char cls = fdc.charAt(fdc.length()-1);
      if ( cls > 'f' ) v.add("fieldDayCategory"); // highest class is f
      // System.out.println("Validator called (FieldDayScorer) " + class);
      return v;
    }
  }

  public java.awt.Component getSummaryDisplay() { return sd; }
}