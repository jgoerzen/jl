// exchange for LZ DX contest.  
// Exchange is RST + ITU zone or 2-letter district
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class LZDXExchange extends AbstractExchange {

  private String report = "";      // signal report
  private String zone = "";    // ITU zone
  private String district = ""; // Bulgarian district
  private boolean stickyzone = false;

  public String getGUIExchange() {
    if (isBulgaria()) return report + " " + district;
    return report + "  " + zone;
  }

  public String getCabrilloExchange() {
    String thiscall = callsign.getCallsign();
    String item = "";
    if (isBulgaria()) item = district;
    else item = zone;
    return thiscall + U.findPad( thiscall, 13 ) + " "
           + report + U.findPad( report, 3) + " "  // 3 to account for CW
           + item   + U.findPad(item, 6) ; 
  }


  // the heart of the class; figure out what we've been given
  // INTERPRETS:  
  // dd : zone; rdd or rddd = RS(T)
  // ll = district
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
      if ( nums == 2 && tok.length() == 2) { // Zone (two chars)
        zone = tok; stickyzone = true; 
      }
      if ( nums == 1 && tok.length() == 1) { // Zone (one char)
        zone = "0" + tok; stickyzone = true; 
      }
      if ( alphas == 2 && tok.length() == 2) { // LZ District
        district = tok;
      }
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
      // COULD EASILY BE GENERALIZED TO FOR RESIDENTS
      p.getProperty("callsign") + " " + defrpt 
                                + " " + p.getProperty("iaruZone") 
    );
  }

  public void parseLoggedExchange( String s) {
    StringTokenizer st = new StringTokenizer(s);
    if (st.hasMoreTokens()) callsign = new Callsign(st.nextToken());
    if (st.hasMoreTokens()) report = st.nextToken();
    if (st.hasMoreTokens()) {
      if (isBulgaria()) 
        district = st.nextToken();
      else {
        zone = st.nextToken();
        if (zone.length() == 1) zone = "0" + zone;
      }
    }
  }

  public boolean isComplete() { 
    // System.out.println("isComplete: |" + callsign + "|" + zone + "|" + report);
    if (callsign == Callsign.NOCALL) return false;
    if (report.equals("")) return false;
    if (isBulgaria() && district.equals("")) return false;
    if (! isBulgaria() && zone.equals("")) return false;
    return true; 
  }

  public String getMultiplierField() { 
    if (isBulgaria()) return district;
    else return zone ; 
  }

  public boolean isBulgaria() {
    return callsign.getCountry().equalsIgnoreCase("Bulgaria");
  }

}
