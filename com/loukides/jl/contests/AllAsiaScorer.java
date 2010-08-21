//Scorer for All-Asia contest.  
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;

public class AllAsiaScorer extends GenericScorer {

  protected boolean isWorkable(LogEntry le) {
    return le.getRcvd().getContinent().equalsIgnoreCase("AS");
  }

  protected int findPoints(LogEntry le) { 
    if (! isWorkable(le) ) return 0;
    String band = le.getBand();
    if ( band.equals("b160") ) return 3;
    else if ( band.equals("b80") ) return 2;
    else if ( band.equals("b10") ) return 2;
    else return 1;
  }

  public String getMultName(LogEntry le) {
    return le.getRcvd().getMultiplierField();
  }

  public java.awt.Component getSummaryDisplay() { return sd; }

}