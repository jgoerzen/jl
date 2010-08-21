// Scorer for the All Asia Contest
// Note that this contest wants electronic logs in a stupid XML format, NOT 
// cabrillo.  Essentially Cabrillo wrapped in <> tags.
// age property needs to be added to the configurator
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class AllAsiaExchange extends GenericNumeralExchange {

  // We need some way to create the "sent" half of the two-way 
  // exchange that concentrates all contest-specific knowledge
  // in this object.  USUALLY OVERRIDDEN BY SUBCLASS
  public void addToExchange(Properties p, LogEntry le) {
    this.addToExchange( p.getProperty("callsign") + " " 
                      + report + " " + p.getProperty("age") );
  }

  public String getMultiplierField() {
    return U.findPrefix(callsign.getCallsign());
  }

}