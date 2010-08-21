// scorer for california state qso party; 
// mults from county list; uses multiple listed string checker, to support county lines
// (that should be some sort of configuration option)
package com.loukides.jl.contests;

import java.util.*;

public class CaliforniaScorer extends GenericScorer {

  public String getQTH() {
    return p.getProperty("state");
  }

}