// scorer for a generic state qso party.  1 point per q; 
// mults either arbitrary (if multiplierList property == none)
// or come from some sort of county list
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;

public class OKOMScorer extends GenericScorer {

  protected int findPoints(LogEntry le) {
    if (le.getSent().getContinent().equalsIgnoreCase("EU")) return 1;
    else return 3;
  }

  // yes, we want a summary display
  public java.awt.Component getSummaryDisplay() { return sd; }

}