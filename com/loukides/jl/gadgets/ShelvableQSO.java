package com.loukides.jl.gadgets;

import java.util.*;

public class ShelvableQSO {
  private String band; 
  private String mode;
  private String heard;
  private int mem = -1;

  public final static ShelvableQSO NULLQSO = new ShelvableQSO(null, null, null, -1);

  public ShelvableQSO() {};

  public ShelvableQSO ( String band, String mode, String heard, int mem) {
    this.band = band;
    this.mode = mode;
    this.heard = heard;
    this.mem = mem;
  }

  public String getMode() { return mode; }
  public String getBand() { return band; }
  public String getHeard() { return heard; }
  public int getMemory() { return mem; }
  public boolean isNull() {
    if ( this == null ) return true;  // not sure this is legal
    if ( this == NULLQSO ) return true;
    if ( heard == null ) return true; 
    if ( heard.trim().equals("59")) return true;
    if ( heard.trim().equals("599")) return true;
    if ( heard.trim().equals("")) return true;
    return false;
  }
}