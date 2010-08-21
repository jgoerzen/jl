// exchange for CQ 160m contest:  exchange + QTH
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class CQ160Exchange extends AbstractExchange {

  private String name = "";
  private String qth = "";
  private String report = "";

  boolean sticky = false;

  public String getGUIExchange() {
    return report + " " + qth;
  }

  public String getCabrilloExchange() {
    // cab format only allows 3 sp for qth.
    String call = callsign.getCallsign();
    return U.trunc(call, 13) + U.findPad( call, 13 ) + " "
           + U.trunc(report, 3) + U.findPad( report, 3) + " "
           + U.trunc(qth, 6) + U.findPad( qth, 6) ;
  }


  // the heart of the class; figure out what we've been given
  // INTERPRETS:  
  // cc..c : qth; nn : report; =cncncn: qth
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
      if ( nums == len ) report = tok;
      else if ( alphas == len ) { 
        qth = tok;
        sticky = true;
      }
      else if ( cs[0] == '=' ) {
        qth = tok.substring(1);
        sticky = true;
      }
      // AT LEAST 2 alphas in a US callsign
      else if ( nums >= 1 && alphas >=2 ) {
        callsign = new Callsign(tok);
        // country = callsign.getCountry();
        if (! sticky && ! isDomestic()) qth = getCanonicalPrefix();
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
    mode = le.getMode() ; 
    if ( mode.equals("PH") ) report = "59" ;
    else if (mode.equals("CW") ) report = "599"; 
    this.addToExchange(
        p.getProperty("callsign") + " " 
      + report + " "
      + p.getProperty("state") );
  }

  public void parseLoggedExchange( String s) {
    // handles the case where state is missing, but not name missing
    // can't call addToExchange() because the qth of a DX station would confuse it
    StringTokenizer st = new StringTokenizer(s);
    if (st.hasMoreTokens()) {
      callsign = new Callsign(st.nextToken());
      // country = callsign.getCountry();
    }
    if (st.hasMoreTokens()) report = st.nextToken();
    if (st.hasMoreTokens()) qth = st.nextToken(); 
    sticky = true; // all ex read from log are 'sticky' 
  }

  public boolean isComplete() { 
    if ( callsign==null || report.equals("") || qth.equals("")) return false; 
    return true; 
  }

  public String getMultiplierField() { 
    // NB:  alaska and hawaii count as DX 
    String country = callsign.getCountry();
    if ( ! ( country.equalsIgnoreCase("united states") 
          || country.equalsIgnoreCase("canada") ) ) return country;
    else if ( qth.equals("NU") ) return "NT";
    return qth;
  }

  private boolean isDomestic() {
    String country = callsign.getCountry();
    // NB:  alaska and hawaii count as DX 
    // System.out.println("isDomestic: |" + country);
    if ( ! country.equals("") ) 
      return country.equalsIgnoreCase("united states") 
       || country.equalsIgnoreCase("canada") ;
    return false;
  }

}