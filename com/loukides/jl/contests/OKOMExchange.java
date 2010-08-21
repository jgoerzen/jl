// exchange for the OK OM DX contest
// send rst + serial; receive rst + district
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

public class OKOMExchange extends GenericMixedExchange {
  
  public boolean isComplete() {
    return ! (callsign == Callsign.NOCALL)
        && ! report.equals("") 
        && ! districtOrSomething.equals("");
  }

}