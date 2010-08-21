// Exchange for CQP (serial + county/state).  
// This really shouldn't extend generic serial exchange--in fact, it probably 
// doesn't need to.  I don't think it's getting anything useful from GSE.
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class CaliforniaExchange extends GenericSerialExchange {

  private String county = "";

  public String getGUIExchange() {
    String cty;
    if (callsign.getCallsign().equals(mycall)) cty = p.getProperty("state");
    else cty = county;
    return U.zpad.format(serial.doubleValue()) + " " + cty;
  }

  public String getCabrilloExchange() {
    String cty;
    if (callsign.getCallsign().equals(mycall)) cty = p.getProperty("state");
    else cty = county;
    String serialstring = U.zpad.format(serial.doubleValue());
    return callsign.getCallsign() + U.findPad( callsign.getCallsign(), 12 ) + " "
           + serialstring + U.findPad(serialstring, 4) + " "
           + cty + U.findPad( cty, 13);
  }


  // the heart of the class; figure out what we've been given
  // INTERPRETS:  nnn: serial (not sure this is adequate)
  //              aaa: county  
  //              aaa/aaa/... n counties (for county line stations)
  //              +aaa: add county to list of counties
  //              -aaa: subtract county from list
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
      if ( nums == tok.length() )
        serial = Integer.valueOf(tok);
      else if ( nums >= 1 && alphas >=2 ) {
        callsign = new Callsign(tok);
      }
      // add a county to the list of counties.. NOTE: order of tests is important
      else if ( cs[0] == '+' && nums == 0 && alphas == tok.length() -1 ) {
        if (county.equals("")) county = tok.substring(1); // skip first letter (+)
        else county = county + "/" + tok.substring(1);
      }
      // DELETE a county from the list of counties.. 
      else if ( cs[0] == '-' && nums == 0 && alphas == tok.length() -1 ) {
        int start = county.indexOf(tok.substring(1));
        if ( start != -1 ) {
          county = county.substring(0, start) + county.substring(start + tok.length() -1);
          // fix up to make sure we don't leave stray slashes around
          int dslash = county.indexOf("//");
          if (county.endsWith("/")) 
            county = county.substring(0, county.length() -1);
          else if (dslash != -1) 
            county = county.substring(0, dslash) + county.substring(dslash +1);
          else if (county.startsWith("/"))
            county = county.substring(1);
        }
      }
      // includes the case where there are multiple slash-separated counties
      else if ( nums == 0 && alphas <= tok.length() ) county = tok;      // add
      nums = 0; alphas = 0;
    }    
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
    this.addToExchange(
        p.getProperty("callsign") + " " 
      + le.getSerialAsPaddedString() + " " 
      + p.getProperty("state"));
  }

  public String getMultiplierField() { 
    return county;  
  }

  public void parseLoggedExchange( String s) {
    StringTokenizer st = new StringTokenizer(s);
    callsign = new Callsign(st.nextToken());
    if (st.hasMoreTokens()) serial = Integer.valueOf(st.nextToken()); 
    if (st.hasMoreTokens()) county = st.nextToken();
  }

  public boolean isComplete() {
    return ! (callsign == Callsign.NOCALL)
        && ! county.equals("") 
        && ! serial.equals(U.ZERO) ;
  }

}