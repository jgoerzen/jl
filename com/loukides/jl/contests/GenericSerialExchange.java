// exchange for a generic contest with an exchange like rst + serial
// and could be the basis for other contests
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class GenericSerialExchange extends AbstractExchange {

  protected String report = "";
  protected String prefix = ""; // to facilitate subclassing for wpx

  public String getGUIExchange() {
    return report + " " + U.zpad.format(serial.doubleValue());
  }

  public String getCabrilloExchange() {
    String serialstring = U.zpad.format(serial.doubleValue());
    return callsign.getCallsign() + U.findPad( callsign.getCallsign(), 13 ) + " "
           + report + U.findPad( report, 3) + " "
           + serialstring + U.findPad(serialstring, 6) ;
  }


  // the heart of the class; figure out what we've been given
  // INTERPRETS:  rnn: rs(t); nnn: serial (not sure this is adequate)
  public void addToExchange( String s) {
    int alphas = 0;
    int nums = 0; 
    StringTokenizer input = new StringTokenizer(s.toUpperCase());
    // System.out.println( "S: " + s + " tokens: " + input.countTokens() );
    while ( input.hasMoreTokens() ) {
      // System.out.println( "More: " + input.hasMoreTokens() );
      String tok = input.nextToken();
      // System.out.println( "Token: " + tok + " More: " + input.hasMoreTokens() );
      char [] cs = tok.toCharArray();
      for (int i = 0 ; i < cs.length; i++ ) {
        if ( Character.isDigit(cs[i]) ) nums++;
        if ( Character.isLetter(cs[i]) ) alphas++;
      }
      // System.out.println("Token: " + tok + " " + nums + " " + alphas);
      if ( nums == tok.length() -1 && cs[0] == 'R') 
        report = tok.substring(1);
      else if ( nums == tok.length() )
        serial = Integer.valueOf(tok);
      else if ( nums >= 1 && alphas >=2 ) {
        callsign = new Callsign(tok);
        prefix = findPrefix(tok);
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
    if ( mode.equals("CW") ) report = "599";
    else if (mode.equals("PH")) report = "59";
    this.addToExchange(
        (String) p.get("callsign") + " " 
      + "R" + report +" "
      + le.getSerialAsPaddedString() );
  }

  public String getMultiplierField() { 
    return "";  // no multipliers in generic serial contest
  }

  public void parseLoggedExchange( String s) {
    StringTokenizer st = new StringTokenizer(s);
    callsign = new Callsign(st.nextToken());
    prefix = findPrefix(callsign.getCallsign());
    if (st.hasMoreTokens()) report = st.nextToken();
    if (st.hasMoreTokens()) serial = Integer.valueOf(st.nextToken()); 
  }

  public boolean isComplete() {
    return ! (callsign == Callsign.NOCALL)
        && ! report.equals("") 
        && ! serial.equals(U.ZERO) ;
  }

  protected String findPrefix(String s) { return ""; }
}