// exchange for the arrl 10 meter contest.  exchange depends
// on the location of the station.  AK and HI count as states.
// precede non-59(9) RST by R to log.
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class ARRLTenExchange extends AbstractExchange {

  private String state = "";    // ARRL section
  private String report = "";
  private String country= ""; // ARRL country

  public String getGUIExchange() {
    String sec;
    if ( isDomestic() ) sec = state;
    else sec = U.zpad.format(serial.doubleValue());
    return report + " " + sec;
  }

  public String getCabrilloExchange() {
    String sec;
    if ( isDomestic() ) {
      if ( state.length() > 2) sec = state.substring(0,3);
      else sec = state;
    } else sec = U.zpad.format(serial.doubleValue());
    String call = callsign.getCallsign();
    return call + U.findPad( call, 11 ) + " "
           + report + U.findPad( report, 5) + " "
           + U.findPad(sec, 4) + sec + " " ;
  }


  // the heart of the class; figure out what we've been given
  // INTERPRETS:  
  public void addToExchange( String s) {
    int alphas = 0;
    int nums = 0; 
    StringTokenizer input = new StringTokenizer(s.toUpperCase());
//    System.out.println( "S: " + s + " tokens: " + input.countTokens() );
    while ( input.hasMoreTokens() ) {
//      System.out.println( "More: " + input.hasMoreTokens() );
      String tok = input.nextToken();
//      System.out.println( "Token: " + tok + " More: " + input.hasMoreTokens() );
      char [] cs = tok.toCharArray();
      for (int i = 0 ; i < cs.length; i++ ) {
        if ( Character.isDigit(cs[i]) ) nums++;
        if ( Character.isLetter(cs[i]) ) alphas++;
      }
//      System.out.println("Token: " + tok + " " + nums + " " + alphas);
      // discard SENT signal reports... [[why is this a good thing?]]
      if ( ( tok.equals("59") && mode.equals("PH") 
                              && callsign.getCallsign().equals(mycall))
        ||( tok.equals("599") && mode.equals("CW") 
                              && callsign.getCallsign().equals(mycall)));
      else if ( nums == tok.length() ) setSerial(Integer.parseInt(tok));
      else if ( alphas == tok.length() ) state = tok; 
      else if ( nums == 2 && tok.length() == 3  && cs[0] == 'R' 
           && mode.equals("PH"))           report = tok.substring(1);                              
      else if ( nums == 3 && tok.length() == 4  && cs[0] == 'R'
           && mode.equals("CW"))           report = tok.substring(1);
      // AT LEAST 2 alphas in a US callsign
      else if ( nums >= 1 && alphas >=2 ) {
        callsign = new Callsign(tok);
        country = callsign.getCountry();
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
      + (String) p.get("state") + " ");
  }

  public boolean isComplete() { 
    boolean domestic = isDomestic();
    if ( callsign==Callsign.NOCALL || report.equals("") ) return false; 
    if ( domestic && state.equals("") ) return false; 
    if ( ! domestic && serial.equals(U.ZERO) ) return false; 
    return true; 
  }

  public void parseLoggedExchange( String s) {
    StringTokenizer st = new StringTokenizer(s);
    if (st.hasMoreTokens()) callsign = new Callsign(st.nextToken());
    if (st.hasMoreTokens()) report = st.nextToken();
    if (st.hasMoreTokens()) {
      String temp = st.nextToken(); 
      boolean isSerial = Character.isDigit(temp.charAt(0));
      if ( isSerial ) setSerial(Integer.parseInt(temp));
      else state = temp;
    }
    country = callsign.getCountry();
  }

  public String getMultiplierField() { 
    if ( isDomestic() ) return state;
    return country;
  }

  private boolean isDomestic() {
    // System.out.println("isDomestic: |" + country);
    if ( ! country.equals("") ) 
      return country.equalsIgnoreCase("united states") 
       || country.equalsIgnoreCase("hawaii")
       || country.equalsIgnoreCase("alaska")
       || country.equalsIgnoreCase("canada") ;
    else if ( ! state.equals("") ) return true;
    return false;
  }

}