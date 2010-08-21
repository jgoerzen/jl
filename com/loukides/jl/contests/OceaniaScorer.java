//Scorer for Oceania DX contest.  
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;
import com.loukides.jl.checkers.*;
import com.loukides.jl.gadgets.*;

import java.util.*;
import java.io.*;

public class OceaniaScorer extends GenericScorer {

  protected boolean isWorkable(LogEntry le) {
    return le.getRcvd().getContinent().equalsIgnoreCase("OC");
  }

  protected int findPoints(LogEntry le) { 
    if (! isWorkable(le) ) return 0;
    String band = le.getBand();
    if ( band.equals("b160") ) return 20;
    else if ( band.equals("b80") ) return 10;
    else if ( band.equals("b40") ) return 5;
    else if ( band.equals("b20") ) return 1;
    else if ( band.equals("b15") ) return 2;
    else if ( band.equals("b10") ) return 3;
    else return 1;
  }


}