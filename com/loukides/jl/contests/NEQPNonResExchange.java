// exchange for the New England QSO Party (NE residents)
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.util.*;
import java.io.*;
import java.text.*;

public class NEQPNonResExchange extends NEQPResExchange {


  // We need some way to create the "sent" half of the two-way 
  // exchange that concentrates all contest-specific knowledge
  // in this object.  
  public void addToExchange(Properties p, LogEntry le) {
    this.addToExchange(
        p.getProperty("callsign") + " " + p.getProperty("state"));
  }

  //  public void parseLoggedExchange( String s) 
  // because of interactions between the county and state fields, 
  // we want this passed through the exchange parser, which is the default
  // implementation

  public boolean isComplete() { 
    if ( callsign != Callsign.NOCALL ) {
      // note--if isNewEngland is true, we have a state
      if ( isNewEngland() && ! county.equals("") ) return true;
    }
    return false;
  }

  public String getMultiplierField() {
    return county + " " + state;
  }

}