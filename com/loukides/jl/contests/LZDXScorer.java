package com.loukides.jl.contests;
import com.loukides.jl.jl.LogEntry;
import java.util.*;

public class LZDXScorer extends GenericDoubleMultScorer {

  protected boolean useMultChecker1(LogEntry le) {
    return ((LZDXExchange)le.getRcvd()).isBulgaria();
  }

  protected char encodeMult(LogEntry le) {
    return 
      le.isMultiplier() ? (((LZDXExchange)le.getRcvd()).isBulgaria() ? 'd' : 'z') : '_';
  }

  protected String getMultTotalText() {
    return "  Districts: " + mult1.getTotal() + "  Zones: " + mult2.getTotal();
  }


  // should be location-independent
  protected int findPoints(LogEntry le) {
    if (((LZDXExchange)le.getRcvd()).isBulgaria()) return 10;
    if (! le.getSent().getContinent().equals(le.getRcvd().getContinent())) return 3;
    return 1;
  }

  public java.awt.Component getSummaryDisplay() { return sd; }

}