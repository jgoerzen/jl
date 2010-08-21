// Scorer for RSGB Iota Contest.  
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;

public class IOTAScorer extends GenericScorer {

  protected char encodeMult(LogEntry le) {
    if ( le.isMultiplier() ) return 'm';
    return '_';
  }


  protected int findPoints(LogEntry le) {
    String myIota = le.getSent().getMultiplierField();
    String workedIota = le.getRcvd().getMultiplierField();
    if ( workedIota.equals(myIota) ) return 3;
    if ( workedIota.equals("")) return 3;
    else return 15;
  }

  public java.awt.Component getSummaryDisplay() { return sd; }

}
