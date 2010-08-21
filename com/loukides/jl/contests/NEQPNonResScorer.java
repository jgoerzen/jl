// scorer for NEQP, non-residents
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;
import com.loukides.jl.checkers.*;
import com.loukides.jl.gadgets.*;

import java.util.*;
import java.io.*;

public class NEQPNonResScorer extends GenericScorer {

  protected int findPoints(LogEntry le) {
    if ( le.getMode().equals("PH") ) return 1;
    if ( le.getMode().equals("CW") ) return 2; 
    if ( le.getMode().equals("RY") ) return 2;
    return 0;
  }

}