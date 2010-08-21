// Scorer for the Japan International DX Contest
package com.loukides.jl.contests;

import com.loukides.jl.jl.LogEntry;

public class JIDXScorer extends GenericScorer {

  protected int findPoints(LogEntry le) {
    String b = le.getBand();
    if (b.equals("b80") || b.equals("b10")) return 2;
    if (b.equals("b40") || b.equals("b20") || b.equals("b15")) return 1;
    return 0;
  }

  public java.awt.Component getSummaryDisplay() { return sd; }

}
