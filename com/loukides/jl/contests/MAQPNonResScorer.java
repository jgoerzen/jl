package com.loukides.jl.contests;

import com.loukides.jl.jl.LogEntry;

public class MAQPNonResScorer extends GenericScorer {

  protected int findPoints(LogEntry le) {
    if (le.getRcvd().getCallsign().endsWith("/M")) return 3;
    else return super.findPoints(le);
  }

}