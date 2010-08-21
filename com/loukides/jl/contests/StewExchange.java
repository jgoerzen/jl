// The exchange for the Stew Perry Topband contest is identical
// to the ARRL VHF contests: just a grid square.  NO WAY to enter an alternate RST.
package com.loukides.jl.contests;
import com.loukides.jl.contests.*;
import com.loukides.jl.util.U;

public class StewExchange extends VHFExchange {
  // The contest managers WANT a 599 in every log entry, even though it isn't in the exchange
  // doesn't keep report in the log.
  public final static String rpt = "599";

  public String getCabrilloExchange() {
    String call = callsign.getCallsign();
    return U.trunc(call, 13) + U.findPad( call, 13 ) + " " 
         + rpt + U.findPad(rpt, 3) + " "
         + grid + U.findPad(grid, 6) ; 
  }

}