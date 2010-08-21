// A null checker.  All mults are valid, but none are new.
// (Could conceivably be a superclass of something)
package com.loukides.jl.checkers;
import com.loukides.jl.jl.*;
import com.loukides.jl.gadgets.*;
import com.loukides.jl.util.*;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;

public class NullChecker extends ArbitraryChecker {

  protected Properties p = new Properties();

  public void setProperties(Properties p) {}

  public void clear() {}

  // return total number of entries in the checker
  public int getTotal() { return 0;}

  public boolean isNew( LogEntry le ) {
    return false;
  }

  // like other methods, sets as many of the fields of the log entry
  // as we know about
  public void addEntry(LogEntry le) {
    le.setMultiplier(false);
  } 

  // for an arbitrary checker, all mults are valid
  public boolean isValidMult(LogEntry le) { return true; }

  protected void updateDisplay(LogEntry le, String s) {};

  public void setUpdateVisual(boolean b){ }

  public void writeReport(PrintWriter pw) {}

}