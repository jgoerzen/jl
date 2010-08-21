// exchange for a generic contest with an asymmetric exchange:
// send rst + serial; receive rst + some other token
// could be the basis for other contests, like PACC, WAG, RUDX
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class GenericMixedExchange extends AbstractExchange {

  protected String report = "";
  protected String districtOrSomething = ""; // whatever the other guy sends

  // The serial number takes precedence if both SN and district are set.
  // BUT serial number can be zero'd explicitly (by entering 0)
  public String getGUIExchange() {
    String exchange;
    if ( ! serial.equals(U.ZERO)) exchange = U.zpad.format(serial.doubleValue());
    else exchange = districtOrSomething;
    return report + " " + exchange;
  }

  // see getGUIExchange() comment
  public String getCabrilloExchange() {
    String exchange;
    String cl = callsign.getCallsign();
    if ( ! serial.equals(U.ZERO)) exchange = U.zpad.format(serial.doubleValue());
    else exchange = districtOrSomething;
    return cl + U.findPad( cl, 13 ) + " "
           + report + U.findPad( report, 3) + " "
           + exchange + U.findPad(exchange, 6) ;
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
      else if ( nums == tok.length() ) {
        serial = Integer.valueOf(tok);
        districtOrSomething = "";  // can't have both serial and district set
      }
      else if ( cs[0] == '.' )    // an escape, for things like WAG
        districtOrSomething = tok.substring(1); 
      else if ( nums >= 1 && alphas >=2 ) // Could be a problem in some contests...
        callsign = new Callsign(tok);
      else { 
        districtOrSomething = tok;
        serial = U.ZERO;   // can't have both serial and district set 
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
    return districtOrSomething;  
  }

  public void parseLoggedExchange( String s) {
    StringTokenizer st = new StringTokenizer(s);
    callsign = new Callsign(st.nextToken());
    if (st.hasMoreTokens()) report = st.nextToken();
    if (st.hasMoreTokens()) addToExchange(st.nextToken()); // let addToExchange figure
                                                           // out what's here
  }

  public boolean isComplete() {
    return ! (callsign == Callsign.NOCALL)
        && ! report.equals("") 
        && ! ( serial.equals(U.ZERO) && districtOrSomething.equals(""));
  }

}