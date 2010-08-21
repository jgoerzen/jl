package com.loukides.jl.contests;

import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

public class RUDXExchange extends GenericMixedExchange {

  public String getMultiplierField() {
    return callsign.getCountry();
  }

  // we'll declare something to be "russian" if we've copied an oblast.
  // (We could automatically copy an oblast abbrev into districtOrSomething, but 
  // I'm not going to bother right now)
  public boolean isRussian() {
    if (districtOrSomething.length() < 2) return false;
    if (districtOrSomething == null) return false;
    if (Character.isLetter(districtOrSomething.charAt(0)) &&
        Character.isLetter(districtOrSomething.charAt(1)) ) return true;
    return false;
  }

  public boolean isComplete() {
    if (callsign == Callsign.NOCALL) return false;
    if (report.equals("")) return false;
    if (serial.equals(U.ZERO) && ! isRussian() ) return false;
    if ((districtOrSomething.length() != 2) && isRussian()) return false;
    return true;
  }

}