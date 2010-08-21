// scorer for a generic asymmetric contest (RS + serial, RS + district)
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;
import com.loukides.jl.checkers.*;
import com.loukides.jl.gadgets.*;

import java.util.*;
import java.io.*;

public class WAGScorer extends GenericScorer {
  protected int findPoints() {
    return 3; 
  }

  // yes, we want a summary display
  public java.awt.Component getSummaryDisplay() { return sd; }

}