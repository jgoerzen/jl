package com.loukides.jl.contests;

import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;
import java.util.*;

public class WAGExchange extends GenericMixedExchange {

  public String getMultiplierField() {
    int i;
    for ( i = 0; i < districtOrSomething.length(); i++) {
      char c = districtOrSomething.charAt(i);
      if ( Character.isLetter(c) ) {
        String s = "" + c;
        // System.out.println("WAG:gmf:: " + c + " " + s);
        return s.toUpperCase();
      }
    }
  return ""; // no valid multiplier found
  }

  public void parseLoggedExchange( String s) {
    StringTokenizer st = new StringTokenizer(s);
    callsign = new Callsign(st.nextToken());
    if (st.hasMoreTokens()) report = st.nextToken();
    if (st.hasMoreTokens()) 
      if ( callsign.getCallsign().equalsIgnoreCase(p.getProperty("callsign")))
        serial = Integer.valueOf(st.nextToken());
      else 
        districtOrSomething = st.nextToken();
  }

}

