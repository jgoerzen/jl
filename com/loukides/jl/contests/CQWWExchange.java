// exchange for CQ WW DX contest.  There's some weird stuff in this class, partly because
// it was the first exchange to be written, and I didn't know what I was doing, and also
// because the double multiplier makes things difficult.  (The trick is that 
// getMultiplierField() returns the zone, while CountryChecker knows specifically to 
// look at the getCountry() rather than relying on the multiplier name.  A bad hack, but 
// the bottom line is a class that works pretty much the same as the others.)
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class CQWWExchange extends AbstractExchange {

  private String report = "";      // signal report
  private String zone = "";    // CQWW zone section 
  private boolean stickyzone = false;

  public String getGUIExchange() {
    return report + "  " + zone;
  }

  public String getCabrilloExchange() {
    String thiscall = callsign.getCallsign();
    return thiscall + U.findPad( thiscall, 13 ) + " "
           + report + U.findPad( report, 3) + " "  // 3 to account for CW
           + zone   + U.findPad(zone, 6) ; 
  }


  // the heart of the class; figure out what we've been given
  // INTERPRETS:  
  // dd + val <= 40: zone; rdd or rddd = RS(T)
  // if sig. report not set, sets appropriate default
  public void addToExchange( String s) {
    int alphas = 0;
    int nums = 0; 
    StringTokenizer input = new StringTokenizer(s.toUpperCase());
    // System.out.println( "S: " + s + " tokens: " + input.countTokens() );
    while ( input.hasMoreTokens() ) {
      // System.out.println( "More: " + input.hasMoreTokens() );
      String tok = input.nextToken();
      // System.out.println( "Token: " + tok + " More: " + input.hasMoreTokens() 
      //      + " " + mode);
      char [] cs = tok.toCharArray();
      for (int i = 0 ; i < cs.length; i++ ) {
        if ( Character.isDigit(cs[i]) ) nums++;
        if ( Character.isLetter(cs[i]) ) alphas++;
      }
      // System.out.println("Token: " + tok + " " + nums + " " + alphas);
      if ( nums == 2 && tok.length() == 2 
           && Integer.parseInt(tok) <= 40) { zone = tok; stickyzone = true;}
      if ( nums == 1 && tok.length() == 1) { zone = "0" + tok; stickyzone = true;}
      // provide a way to "force" a signal report in phone
      // Note that we use "R" on both CW and PH, even though there's no ambiguity for CW
      if ( nums == 2 && tok.length() == 3  && cs[0] == 'R' 
           && mode.equals("PH"))           report = tok.substring(1);                              
      if ( nums == 3 && tok.length() == 4  && cs[0] == 'R'
           && mode.equals("CW"))           report = tok.substring(1);
      // AT LEAST 2 alphas in any callsign I know
      if ( nums >= 1 && alphas >=2 ) {
        callsign = new Callsign(tok);
        if ( ! stickyzone ) zone = callsign.getZone();
        if (zone.length() ==1) zone = "0" + zone;
        // System.out.println("exch: " + callsign.getCountry());
      }
      nums = 0; alphas = 0;
    }
    // generate a report if it hasn't been filled in yet.
    // single-mode contests, so don't have to deal with mode changes.
    if ( report.equals("") ) {
      if ( mode.equals("PH") ) report = "59" ;   
      else if (mode.equals("CW") ) report = "599" ;
    }  
  }

  // We need some way to create the "sent" half of the two-way 
  // exchange that concentrates all contest-specific knowledge
  // in this object.  For several reasons, can't be a constructor.
  public void addToExchange(Properties p, LogEntry le) {
    String defrpt = "";
    mode = le.getMode() ; 
    if ( mode.equals("PH") ) defrpt = "59" ;
    else if (mode.equals("CW") ) defrpt = "599"; 
    this.addToExchange(
      (String)p.get("callsign") + " " + defrpt + " " + (String)p.get("cqZone") 
    );
  }

  public void parseLoggedExchange( String s) {
    StringTokenizer st = new StringTokenizer(s);
    if (st.hasMoreTokens()) callsign = new Callsign(st.nextToken());
    if (st.hasMoreTokens()) report = st.nextToken();
    if (st.hasMoreTokens()) {
      zone = st.nextToken();
      if (zone.length() == 1) zone = "0" + zone;
    }
  }

  public boolean isComplete() { 
    // System.out.println("isComplete: |" + callsign + "|" + zone + "|" + report);
    return ! ( callsign==Callsign.NOCALL 
                || zone.equals("") || report.equals("") ); 
  }

  public String getMultiplierField() { return zone ; }

}