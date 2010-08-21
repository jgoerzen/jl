// exchange for the RSGB IOTA (islands on the air) contest
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class IOTAExchange extends AbstractExchange {

  private String report = "";
  private String iota = "";

  final private static String iotapad = "000";

  public String getGUIExchange() {
    return report + " " + U.zpad.format(serial.doubleValue()) + " " + iota;
  }

  // HAH--doesn't even need asymmetric log printing capability I so 
  // carefully added....
  public String getCabrilloExchange() {
    String localiota;
    if ( iota.equals("") ) localiota = "------";
    else localiota = iota;
    String call = callsign.getCallsign();
    return U.trunc(call, 13) + U.findPad( call, 13 ) + " "
         + U.trunc(report, 3) + U.findPad( report, 3) + " "
         + U.zpad.format(serial.doubleValue()) + " " 
         + localiota + U.findPad(localiota, 6);
  }


  // the heart of the class; figure out what we've been given
  // INTERPRETS:  rnn/rnnn = report; nnnn = serial
  // nn-aaa; iota; nnaaa; iota
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
      if ( nums == tok.length() ) serial = Integer.valueOf(tok);
      else if ( nums == tok.length() -1 && cs[0] == 'R') 
        report = tok.substring(1);
      else if ( tok.startsWith("AF") ||    // first check continent
                tok.startsWith("AN") ||
                tok.startsWith("AS") ||
                tok.startsWith("EU") ||
                tok.startsWith("NA") ||
                tok.startsWith("OC") ||
                tok.startsWith("SA") ) {
         if ( nums == tok.length() -2 ||   // everything else numeric
              ( cs[2] == '-' && nums == tok.length() -3 ) ) // dash case
            iota = normalizeIota(tok, cs[2]);
      }
      // AT LEAST 2 alphas in a US callsign
      else if ( nums >= 1 && alphas >=2 ) callsign = new Callsign(tok);
      nums = 0; alphas = 0;
    }
    // assign a report, if there isn't a mode-appropriate report already   
    if ( (! (report.length() == 2)) && mode.equals("PH")) report = "59";
    else if ( (! (report.length() == 3)) && mode.equals("CW")) report = "599";
  }

  private String normalizeIota(String s, char c) {
    // c passed in just because it's handy and saves trouble
    String number;
    String continent = s.substring(0,2);
    if ( c == '-' ) number = s.substring(3);
    else number = s.substring(2);
    number = iotapad.substring(0, Math.max(0, 3-number.length())) + number;
    String fixediota = continent + "-" + number; 
    // System.out.println("iota: |"+s+"|"+c+"|"+continent+"|"+number);
    return fixediota;   
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
                      + report + " " + p.getProperty("iota", "") + " "
                      + le.getSerialAsPaddedString());
  }

  public void parseLoggedExchange( String s) {
    // handles the case where state is missing, but not name missing
    StringTokenizer st = new StringTokenizer(s);
    callsign = new Callsign(st.nextToken());
    if (st.hasMoreTokens()) report = st.nextToken();
    if (st.hasMoreTokens()) serial = Integer.valueOf(st.nextToken()); 
    if (st.hasMoreTokens()) iota = st.nextToken();
    if (iota.equals("------")) iota = "";
  }

  public boolean isComplete() { // note: IOTA not required for completion
    if (callsign == Callsign.NOCALL ||
        report.equals("")   ||
        serial.equals(U.ZERO)) return false; 
    return true; 
  }

  public String getMultiplierField() { 
    return iota;
  }

  public void   setMultiplierField(String s) { 
  // the country checker uses this method to set the zone, so do nothing.
  // The use of multiplier field is really obsolete...
  }

}