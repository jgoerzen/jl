// exchange for a generic QSO party ( rst + state/county )
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class GenericExchange extends AbstractExchange {

  private String report = "";
  private String qth= ""; // 

  public String getGUIExchange() {
    return report + " " + qth;
  }

  public String getCabrilloExchange() {
    String c = callsign.getCallsign();
    return c + U.findPad( c, 13 ) + " "
           + report + U.findPad( report, 3) + " "
           + qth + U.findPad(qth, 6) ;
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
      if ( mode.equals("CW") && 
           nums == tok.length() 
           && nums == 3) report = tok;
      if ( mode.equals("PH") && 
           nums == tok.length() 
           && nums == 2) report = tok;
      else if ( alphas == tok.length() ) qth = tok; 
      // AT LEAST 2 alphas in a US callsign
      else if ( nums >= 1 && alphas >=2 ) callsign = new Callsign(tok);
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

  public String getMultiplierField() { 
    return qth;
  }

  public void setMultiplierField(String s) {}

  public boolean isComplete() {
    return ! (callsign == Callsign.NOCALL) 
        && ! qth.equals("") && ! report.equals("");
  }
}