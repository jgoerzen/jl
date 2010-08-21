// This interface allows us to concentrate some of the maintainance for 
// objects that require resetting (during a rescore) in AbstractScorer.
// The concrete scorer builds an array of rescorables at startup, and then
// the abstract scorer loops through to set properites/clear/etc.
// NOTE:  Many scorers still use the "old way", but they should be rewritten.
package com.loukides.jl.jl;

import java.util.*;
import java.io.*;

public interface Rescorable {
  public void setProperties(Properties p);
  public void clear();            // reset internal tables
  public void setUpdateVisual(boolean b);
  public void writeReport(PrintWriter o);
}