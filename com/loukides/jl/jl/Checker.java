// interface for multiplier checkers and dupesheets
package com.loukides.jl.jl;

import java.util.*;
import java.io.*;

public interface Checker extends Rescorable {

  public boolean isNew(LogEntry le);
  public void addEntry(LogEntry le);  
  public boolean isValidMult(LogEntry le);
  // getTotal() appears to be used only by CQWW, and can probably be avoided there.
  public int getTotal();
  public Vector getNeeded(LogEntry le);
}