// exchange for a general operating log
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class GeneralExchange extends AbstractExchange {

  private String name = "";
  private String qth = "";
  private String report = "";

  private String state = ""; 

  private static String [] states = {
    "CT", "MA", "ME", "NH", "RI", "VT", "NY", "NJ", "DE", "PA", "MD",
    "DC", "AL", "GA", "KY", "NC", "FL", "SC", "TN", "VA", "AR", "LA",
    "MS", "NM", "TX", "OK", "CA", "HI", "AZ", "WA", "ID", "MT", "NV",
    "OR", "UT", "WA", "WY", "AK", "MI", "OH", "WV", "IL", "IN", "WI",
    "CO", "IA", "KS", "MN", "MO", "NE", "ND", "SD", "NB", "NS", "QC",
    "ON", "MB", "SK", "AB", "BC", "NWT", "NF", "LB", "YT", "PE", "NU"
  };

  private static HashMap statesMap = new HashMap(70)  ;

  boolean sticky = false;

  static {
    for ( int i = 0; i < states.length; i++ ) statesMap.put( states[i], null);      
  }

  public String getGUIExchange() {
    return name + " " + report + " " + qth;
  }

  public String getCabrilloExchange() {
    // cab format only allows 3 sp for qth.
    String tqth;
    if ( qth.length() > 3 ) tqth = qth.substring(0,3);
    else tqth = qth;
    String call = callsign.getCallsign();
    return U.trunc(call, 11) + U.findPad( call, 11 ) + " "
           + report + U.findPad(report, 3) + " "
           + U.trunc(name, 10) + U.findPad( name, 10) + " "
           + U.trunc(tqth, 3) + U.findPad( tqth, 3) ;
  }


  // the heart of the class; figure out what we've been given
  // INTERPRETS:  
  // cc : qth; cc..c : name; =cc : name (2 char); .cc : qth; =cc..c : name
  //  The rules have gotten a little too bizarre, and I'm not sure the
  // escapes are adequate.  
  //  TWO CHARS IN THE STATE LIST: interpreted as a name (if name null),
  //     then as QTH, so giving name first is handled correctly
  //     THIS MEANS THAT in the default case, you have to give the name first
  //     To get around this, use the .QTH escape.
  //  = forces interpretation of 2 chars as a name
  //  . forces interpretation of anything as a QTH.
  //  Sticky means that QTH can no longer be set automatically
  public void addToExchange( String s) {
    int alphas = 0;
    int nums = 0; 
    StringTokenizer input = new StringTokenizer(s);
    while ( input.hasMoreTokens() ) {
      String tok = input.nextToken().toUpperCase();
      char [] cs = tok.toCharArray();
      int len = tok.length();
      for (int i = 0 ; i < cs.length; i++ ) {
        if ( Character.isDigit(cs[i]) ) nums++;
        if ( Character.isLetter(cs[i]) ) alphas++;
      }
      if ( ( nums == 2 || nums == 3 ) && alphas == 0 ) report = tok;
      if ( statesMap.containsKey(tok) && ! name.equals("") ) {
        qth = tok;
        sticky = true;
      }
      else if (alphas == len ) name = tok;
      // escapes: force two-character name
      else if ( len == 3 && tok.charAt(0) == '=') 
        name = tok.substring(1);
      // force two or more character qth
      else if ( len >= 3 && tok.charAt(0) == '.') {
        qth = tok.substring(1);
        sticky = true;
      }
      // AT LEAST 2 alphas in a US callsign
      else if ( nums >= 1 && alphas >=2 ) {
        callsign = new Callsign(tok);
        if ( ! sticky ) {
          String px = callsign.getCanonicalPrefix();
          if ( px != null && ! isDomestic()) qth = px;
        }
      }
      nums = 0; alphas = 0;
    }
    // assign a report, if there isn't a mode-appropriate report already   
    if ( (! (report.length() == 2)) && mode.equals("PH")) report = "59";
    else if ( (! (report.length() == 3)) && mode.equals("CW")) report = "599";  
  }

  // We need some way to create the "sent" half of the two-way 
  // exchange that concentrates all contest-specific knowledge
  // in this object.  
  public void addToExchange(Properties p, LogEntry le) {
    this.addToExchange(
        p.getProperty("callsign") + " " 
      + p.getProperty("firstname") + " "
      + p.getProperty("state") + " ");
  }

  public void parseLoggedExchange( String s) {
    // handles the case where state is missing, but not name missing
    StringTokenizer st = new StringTokenizer(s);
    if (st.hasMoreTokens()) callsign = new Callsign(st.nextToken());
    if (st.hasMoreTokens()) report = st.nextToken();
    if (st.hasMoreTokens()) name = st.nextToken();
    if (st.hasMoreTokens()) qth = st.nextToken(); 
    sticky = true; // all ex read from log are 'sticky' 
  }

  public void modifySentExchange(String s) { // allows general guest operation
    this.addToExchange(s);
    p.setProperty("firstname", name); 
  }

  public boolean isComplete() { 
    if ( callsign==Callsign.NOCALL || name.equals("") 
      || qth.equals("") || report.equals("") ) return false; 
    return true; 
  }

  public String getMultiplierField() { 
    return qth;
  }

  private boolean isDomestic() {
    // System.out.println("isDomestic: " + country);
    String country = callsign.getCountry();
    if ( ! country.equals("") ) 
      return country.equalsIgnoreCase("united states") 
       || country.equalsIgnoreCase("hawaii")
       || country.equalsIgnoreCase("alaska")
       || country.equalsIgnoreCase("canada") ;
    return false;
  }

}