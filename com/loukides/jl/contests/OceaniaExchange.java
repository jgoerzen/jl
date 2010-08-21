// Scorer for the Oceania DX Contest
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

public class OceaniaExchange extends GenericSerialExchange {

  public String getMultiplierField() {
    return U.findPrefix(callsign.getCallsign());
  }

}