// exchange for the IARU HF Championship contest. 
// For the time being, I am assuming that we do *not* want to screw around
// with speculatively filling in the zone from the country
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class IARUExchange extends AbstractExchange {

  private String report = "";
  private String zone =   "";
  private String society = "";
  private boolean stickyzone = false;

  public String getGUIExchange() {
    return report + " " + zone;
  }

  // HAH--doesn't even need asymmetric log printing capability I so 
  // carefully added....
  public String getCabrilloExchange() {
    String call = callsign.getCallsign();
    return U.trunc(call, 13) + U.findPad( call, 13 ) + " "
         + U.trunc(report, 3) + U.findPad( report, 3) + " "
         + U.trunc(zone, 6)   + U.findPad(zone, 6) ;
  }


  // the heart of the class; figure out what we've been given
  // INTERPRETS:  rnn/rnnn = report;
  // r1, r2, r3, ac: special case
  // aaaa = society
  // nn = zone
  public void addToExchange( String s) {
    int alphas = 0;
    int nums = 0; 
    StringTokenizer input = new StringTokenizer(s);
    while ( input.hasMoreTokens() ) {
      String tok = input.nextToken().toUpperCase();
      char [] cs = tok.toCharArray();
      for (int i = 0 ; i < cs.length; i++ ) {
        if ( Character.isDigit(cs[i]) ) nums++;
        if ( Character.isLetter(cs[i]) ) alphas++;
      }
      // System.out.println("Token: " + tok + " " + nums + " " + alphas);
      if ( nums == tok.length() ) { 
        zone = (( nums == 1) ? "0" : "") + tok; 
        stickyzone = true; 
      }
      else if ( alphas == tok.length() ) { 
        zone = tok; 
        stickyzone = true; 
      }
      else if ( tok.equals("R1")   || tok.equals("R2")
               || tok.equals("R3") || tok.equals("AC") ) { 
        zone = tok; 
        stickyzone = true; 
      }
      else if ( nums == tok.length() -1 && cs[0] == 'R') 
        report = tok.substring(1);
      // AT LEAST 2 alphas in a US callsign
      else if ( nums >= 1 && alphas >=2 ) {
        callsign = new Callsign(tok);
        if ( ! stickyzone ) zone = callsign.getZone();
      }
      nums = 0; alphas = 0;
    }
    // assign a report, if there isn't a mode-appropriate report already   
    if ( (! (report.length() == 2)) && mode.equals("PH")) report = "59";
    else if ( (! (report.length() == 3)) && mode.equals("CW")) report = "599";
  }

  // We need some way to create the "sent" half of the two-way 
  // exchange that concentrates all contest-specific knowledge
  // in this object.  For several reasons, can't be a constructor.
  // try: an overloaded version of addToExchange
  // for the sent exchange; get stuff out of the properties and 
  // use our own tools to populate the object.  This guarantees that we can 
  // print in in cab form, etc., and isolates all the contest-specific stuff
  // in this class
  public void addToExchange(Properties p, LogEntry le) {
    if ( mode.equals("CW") ) report = "r599";
    else if (mode.equals("PH")) report = "r59";
    this.addToExchange( p.getProperty("callsign") + " " 
                      + report + " " + p.getProperty("iaruZone") );
  }

  public void parseLoggedExchange( String s) {
    // handles the case where state is missing, but not name missing
    StringTokenizer st = new StringTokenizer(s);
    callsign = new Callsign(st.nextToken());
    if (st.hasMoreTokens()) report = st.nextToken();
    if (st.hasMoreTokens()) zone = st.nextToken(); 
  }

  public boolean isComplete() { 
    if ( callsign==Callsign.NOCALL
      ||report.equals("")||zone.equals("") ) return false; 
    return true; 
  }

  public String getMultiplierField() { 
    return zone; 
  }

  public void   setMultiplierField(String s) { 
  // the country checker uses this method to set the zone, so do nothing.
  // The use of multiplier field is really obsolete...
  }

}