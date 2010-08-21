// As far as I know, this exchange is correct for ARRL VHF SS and QSO 
// parties.
// I am not sure that the implementation of the cabrillo format is correct.
// there is no actual spec for this contest.  Open questions:
// should signal reports be logged if they are given? (behavior: no)
// should 6-char grids be logged if given (behavior: truncated to 4)
// what are the correct field boundaries?
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class VHFExchange extends AbstractExchange {

  private String report = "";      // signal report
  protected String grid = "";    // grid square section 

  public VHFExchange() {}; // no-arg constructor does nothing; fields filled later

  public String getGUIExchange() {
    return report + " " + grid;
  }

  // doesn't keep report in the log.
  public String getCabrilloExchange() {
    String call = callsign.getCallsign();
    return U.trunc(call, 13) + U.findPad( call, 13 ) + " " 
         + report + U.findPad(report, 3) + " "
         + grid + U.findPad(grid, 6) ; 
  }


  // the heart of the class; figure out what we've been given
  // INTERPRETS:  
  // dd: RS; ddd = RST
  public void addToExchange( String s) {
    int alphas = 0;
    int nums = 0; 
    StringTokenizer input = new StringTokenizer(s);
//    System.out.println( "S: " + s + " tokens: " + input.countTokens() );
    while ( input.hasMoreTokens() ) {
      boolean iscall = false;  // dead code!
//      System.out.println( "More: " + input.hasMoreTokens() );
      String tok = input.nextToken();
//      System.out.println( "Token: " + tok + " More: " + input.hasMoreTokens() );
      char [] cs = tok.toCharArray();
      for (int i = 0 ; i < cs.length; i++ ) {
        if ( Character.isDigit(cs[i]) ) nums++;
        if ( Character.isLetter(cs[i]) ) alphas++;
      }
//    System.out.println("parse: " + nums + " " +tok+" "+tok.length()+" "+mode);
      if ( nums == 2 && tok.length() == 2 && mode.equals("PH") ) report = tok;
      else if ( nums == 3 && tok.length() == 3 && mode.equals("CW") ) report = tok;
      // AT LEAST 2 alphas in any callsign I know
      else if ( tok.length() == 4 && ! iscall
           && Character.isLetter(cs[0]) && Character.isLetter(cs[1])
           && Character.isDigit(cs[2]) && Character.isDigit(cs[3])) 
             grid = tok.toUpperCase();
      else if ( tok.length() == 6 && ! iscall
           && Character.isLetter(cs[0]) && Character.isLetter(cs[1])
           && Character.isDigit(cs[2]) && Character.isDigit(cs[3]) 
           && Character.isLetter(cs[4]) && Character.isLetter(cs[5])) 
             grid = tok.toUpperCase();
      else if ( nums >= 1 && alphas >=2 ) 
        callsign = new Callsign(tok.toUpperCase());
      nums = 0; alphas = 0;
    }
    // assign a report, if there isn't a mode-appropriate report already   
    // if ( (! (report.length() == 2)) && mode.equals("PH")) report = "59";
    // else if ( (! (report.length() == 3)) && mode.equals("CW")) report = "599";
  }

  // We need some way to create the "sent" half of the two-way 
  // exchange that concentrates all contest-specific knowledge
  // in this object.
  public void addToExchange(Properties p, LogEntry le) {
    String report = "";
    mode = le.getMode() ; 
    // if ( mode.equals("PH") ) report = "59" ;
    // else if (mode.equals("CW") ) report = "599"; 
    this.addToExchange(
      p.getProperty("callsign") + " " + report + " " 
                    + p.getProperty("gridSquare") );
  }

  public void modifySentExchange(String s) {
    // only rovers allowed to alter their exchange
    if (p.getProperty("category", "SINGLE-OP").equals("ROVER")) {
      this.addToExchange(s);
      if (grid.length() == 6) grid = grid.substring(0,4);
      p.setProperty("gridSquare", grid); 
    }
  }


  public boolean isComplete() { 
    // System.out.println("isComplete: |" + callsign + "|" + zone + "|" + report);
    return ! ( callsign==Callsign.NOCALL || grid.equals("") ); 
  }

  public String getMultiplierField() { 
    if ( grid.length() > 4 ) return grid.substring(0,4);
    else return grid; 
  }

  public void   setMultiplierField(String s) { grid = s; }

  public String getGrid() { return grid; } // for VHF scorer only

}