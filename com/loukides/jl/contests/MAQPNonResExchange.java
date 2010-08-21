package com.loukides.jl.contests;

import com.loukides.jl.jl.LogEntry;
import com.loukides.jl.jl.Callsign;
import com.loukides.jl.util.U;
import java.util.*;

public class MAQPNonResExchange extends GenericExchange {

  private String county = "";

  public String getGUIExchange() {
    return U.zpad.format(serial.doubleValue()) + " " + county;
  }

  // see getGUIExchange() comment
  public String getCabrilloExchange() {
    String cl = callsign.getCallsign();
    return cl + U.findPad( cl, 13 ) + " "
           + U.zpad.format(serial.doubleValue()) + " "
           + county + U.findPad(county, 5) ;
  }


  // the heart of the class; figure out what we've been given
  // INTERPRETS:  nnn: serial aaaaa or aa: county
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
      if ( nums == tok.length() ) {
        serial = Integer.valueOf(tok);
      }
      else if ( nums >= 1 && alphas >=2 ) // Could be a problem in some contests...
        callsign = new Callsign(tok);
      else if ( alphas == tok.length()) { 
        county = tok;
      } else {}
      nums = 0; alphas = 0;
    } 
  }

  // to create "sent" exchange
  public void addToExchange(Properties p, LogEntry le) {
    addToExchange(
      p.getProperty("callsign") + " " 
      + le.getSerialAsPaddedString()  + " "
      + p.getProperty("state"));
  }

  public void parseLoggedExchange( String s) {
    addToExchange(s); // let addToExchange figure out what's here
  }

  public boolean isComplete() {
    if (callsign == Callsign.NOCALL) return false;
    if ( serial.equals(U.ZERO)) return false;
    if ( county.equals("") ) return false;
    return true;
  }

  public String getMultiplierField() { return county; }

}