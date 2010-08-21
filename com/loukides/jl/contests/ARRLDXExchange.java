// exchange for the arrl DX contest. 
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class ARRLDXExchange extends AbstractExchange {

  private String report = "";
  private String power = "";
  private String state = "";

  public String getGUIExchange() {
    String sec;
    // System.out.println("GUI exchange: " + callsign.getCallsign() + " " + mycall);
    if ( callsign.getCallsign().equals(mycall) ) sec = state;
    else sec = power;
    return report + " " + sec;
  }

  // HAH--doesn't even need asymmetric log printing capability I so 
  // carefully added....
  public String getCabrilloExchange() {
    String sec;
    String thiscall = callsign.getCallsign();
    if ( thiscall.equals(mycall) ) sec = state;
    else sec = power;
    return U.trunc(thiscall, 13) + U.findPad( thiscall, 13 ) + " "
         + U.trunc(report, 3) + U.findPad( report, 3) + " "
         + U.trunc(sec, 6) + U.findPad(sec, 6) ;
  }


  // the heart of the class; figure out what we've been given
  // INTERPRETS:  rnn/rnnn = report; nnnn = power
  // shorthands:  k = 1000; f = 1500; c = 100; t = 200; s = 600
  // 59(9) is ALWAYS a report (to make the stack work); God help you if you 
  // work someone running 59 watts.  
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
      // I guess this means you can't run 59 or 599 watts
      if ( tok.equals("59") || tok.equals ("599"))  report = tok;
      // handle cw abbreviations
      else if ( tok.equals("KW") // k handled at end 
             || tok.equals("NNN")
             || tok.equals("TTT")
             || tok.equals("ATTT")
             || tok.equals("1TTT")) power = "1000";
      else if ( tok.equals("NTT") ) power = "900";
      else if ( tok.equals("5TT") 
             || tok.equals("ETT") ) power = "500";
      else if ( tok.equals("4TT") ) power = "400";
      else if ( tok.equals("3TT") ) power = "300";
      else if ( tok.equals("2TT") ) power = "200";
      else if ( tok.equals("1TT") 
             || tok.equals("ATT"))  power = "100";
      else if ( tok.equals("NN") )  power = "99";
      else if ( tok.equals("NT") )  power = "90";
      else if ( tok.equals("TT5") ) power = "5";
      else if ( nums == tok.length())              power = tok;
      else if ( nums == tok.length() -1 && cs[0] == 'R') 
        report = tok.substring(1);
      else if ( alphas == tok.length() && alphas > 1 ) 
        state = tok;  // only used for 'sent'
      // AT LEAST 2 alphas in a callsign
      // This might not work for some VERY RARE callsigns, like 5VZGE
      else if ( nums >= 1 && alphas >=2 ) 
        callsign = new Callsign(tok.toUpperCase());
      // power abbreviations; 300 is forced.
      else if ( tok.equals("K") ) power = "1000";
      else if ( tok.equals("S") ) power = "600";
      else if ( tok.equals("V") ) power = "500";
      else if ( tok.equals("F") ) power = "400";
      else if ( tok.equals("E") ) power = "300";
      else if ( tok.equals("T") ) power = "200";
      else if ( tok.equals("C") ) power = "100";
      nums = 0; alphas = 0;
    }
    if ( report.equals("") ) {    
      if ( mode.equals("CW") ) report = "599";   // ARRL DX contests single-mode,
      else if (mode.equals("PH")) report = "59"; // so no mode-change problem
    }
    // System.out.println("Parsed ex: " + callsign + " " + report + 
    //                                  " " + state + " " + power);
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
    // System.out.println("Creating sent exchange:");
    // p.list(System.out);
    if ( mode.equals("CW") ) report = "r599";
    else if (mode.equals("PH")) report = "r59";
    String ex = p.getProperty("callsign") + " " + report + " " + p.getProperty("state"); 
    // System.out.println(ex);
    this.addToExchange( ex);
  }

  public void parseLoggedExchange( String s) {
    StringTokenizer st = new StringTokenizer(s);
    callsign = new Callsign(st.nextToken());
    if (st.hasMoreTokens()) report = st.nextToken();
    if (st.hasMoreTokens()) power = st.nextToken(); 
  }

  public boolean isComplete() { 
    if ( (callsign==Callsign.NOCALL)
        ||report.equals("")||power.equals("")) return false; 
    return true; 
  }

  public String getMultiplierField() { 
    return callsign.getCountry();
  }

}