// exchange for the ARRL 160 meter contest.  (report + section)
// Note that it's CW only, and that the 
// QTH isn't required for DX contacts
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class ARRL160Exchange extends AbstractExchange {

  private String name = "";
  private String qth = "";
  private String report = "";

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
      }
      // AT LEAST 2 alphas in a US callsign
      else if ( nums >= 1 && alphas >=2 ) {
        callsign = new Callsign(tok);
        // country = callsign.getCountry();
      }
      nums = 0; alphas = 0;
    } 
    if ( report.equals("") ) report = "599";  // only one mode, so no mode change problem
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
      + p.getProperty("arrlSection") );
  }

  public boolean isComplete() { 
    if (isDomestic())
      if ( callsign==null || report.equals("") || qth.equals(""))
        return false; 
      else return true;
    else  // DX; not required to send QTH
      if (callsign==null || report.equals("")) return false;
      else return true;
  }

  public String getMultiplierField() { 
    String country = callsign.getCountry();
    // NB:  alaska and hawaii count as DX 
    if ( ! ( country.equalsIgnoreCase("united states") 
          || country.equalsIgnoreCase("canada") ) ) return country;
    else return qth;
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