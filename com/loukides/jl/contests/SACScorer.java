// scorer for a generic state qso party.  1 point per q; 
// mults either arbitrary (if multiplierList property == none)
// or come from some sort of county list
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;

public class SACScorer extends GenericScorer {

  protected String findMultName(LogEntry le) {
    return le.getRcvd().getMultiplierField();
  }

  protected int findPoints(LogEntry le) {
    if (((SACExchange)le.getRcvd()).isScandinavian() == false) return 0;
    String band = le.getBand();
    if (band.equalsIgnoreCase("b80") || band.equalsIgnoreCase("b40"))
      return 3;
    else return 1;
  }

  // yes, we want a summary display
  public java.awt.Component getSummaryDisplay() { return sd; }

}