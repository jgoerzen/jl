// Scorer for IARU HF.  
package com.loukides.jl.contests;
import com.loukides.jl.jl.LogEntry;
import java.util.*;

public class IARUScorer extends GenericDoubleMultScorer {
  private String myiaruzone;

  public void setProperties(Properties p) { 
    super.setProperties(p);
    myiaruzone = p.getProperty("iaruZone");
  }

  protected boolean isIARU(String z) {
    // System.out.println("IARUScorer::isIARU: " + z);
    // IARU HQs have lengths > 2; zones have length = 2
    return z.equalsIgnoreCase("AC") 
       || z.equalsIgnoreCase("R1")
       || z.equalsIgnoreCase("R2")
       || z.equalsIgnoreCase("R3") 
       || ! (z.length() == 2) ;
  }

  protected boolean isNorthAmerica(String z) {
    // System.out.println("NAQPScorer::isNA: " + zone + " " + country);
    // sort-of cheating; IARU stations all are scored the same as NA
    boolean t = isIARU(z);
    if ( t ) return t; 
    if ( z.equalsIgnoreCase("na")) return true ;
    else return false; 
  }

  protected boolean useMultChecker1(LogEntry le) {
    return mult1.isValidMult(le);
  }


  protected char encodeMult(LogEntry le) {
    if (le.isMultiplier()) return 'm';
    return ' ';
  }


  protected int findPoints(LogEntry le) {
    String mult = le.getRcvd().getMultiplierField();
    if ( isIARU(mult) ) return 1;
    if (myiaruzone.equals(mult)) return 1;
    if ( isNorthAmerica(le.getRcvd().getContinent()) ) return 3; 
    else return 5;
  }

  public java.awt.Component getSummaryDisplay() { return sd; }

}
