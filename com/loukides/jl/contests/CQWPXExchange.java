// exchange for WPX.  Extends GenericSerialExchange, since this
// is basically a report/serial contest, with a lot of extra logic for
// working with prefixes.  There are some hooks in GSS to support subclassing.
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class CQWPXExchange extends GenericSerialExchange {
  private static final int PFX = 0; 
  private static final int NUMS = 1;
  private static final int SFX = 2;

  public String getMultiplierField() { 
    return prefix;  
  }

  protected String findPrefix(String call) {
    return U.findPrefix(call);
  }

}