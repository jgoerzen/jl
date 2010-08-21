// multipler checker for multipliers that are a number in a fixed
// range (e.g., CQ zone, IARU zone, Japanese prefecture)

// revised so that AbstractChecker extends AbstractTableModel, and this can
// extend AbstractChecker.  A few more things could conceivably go in there...
package com.loukides.jl.checkers;
import com.loukides.jl.jl.*;
import com.loukides.jl.gadgets.*;
import com.loukides.jl.util.*;

import java.util.*;
import java.io.*;

public class EnumeratedChecker extends ArbitraryChecker {

  int maxZone = 0;
  int minZone = 1;

  public void setProperties(Properties p) {
    super.setProperties(p);
    maxZone = Integer.parseInt(p.getProperty("maxzone"));
    minZone = Integer.parseInt(p.getProperty("minzone", "1"));
  }

  public boolean isValidMult(LogEntry le) {
    // NB:  we DON'T compute a key; we are only finding out if the 
    // multiplier is potentially valid, and that doesn't depend on
    // band and mode
    int got;
    String s = le.getRcvd().getMultiplierField().trim();
//    System.out.println("isValidMult: |" + s + "|");
    if (s.equals("")) return false;
    try {
      got = Integer.parseInt(s);
    } catch ( NumberFormatException e) { 
      return false; // not a parseable number, so not a valid mult 
    }
    if ( got >= minZone && got <= maxZone ) {
      le.setName(s);
      return true;
    } else {
      le.setName("");
      return false; 
    }
  }

}