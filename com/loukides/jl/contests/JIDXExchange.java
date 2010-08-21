// exchange for a contest with an RST + (number) exchange
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class JIDXExchange extends GenericNumeralExchange {

  public void addToExchange(Properties p, LogEntry le) {
    this.addToExchange( p.getProperty("callsign") + " " 
                      + report + " " + p.getProperty("cqZone") );
  }

  // don't need to override getMultiplierField();
}