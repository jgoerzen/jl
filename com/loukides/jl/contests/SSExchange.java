package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class SSExchange extends AbstractExchange {

  private String check = "";      // check number; year first licensed
  private String precedence = "";      // one char, indicating entry class
  private String section = "";    // ARRL section 

  public SSExchange() {}; // no-arg constructor does nothing; fields filled later

  public String getGUIExchange() {
    String sec;
    if (section.length() > 3) sec = section.substring(0,4);
    else sec = section + U.findPad(section, 3); 
    return U.zpad.format(serial.doubleValue()) + " " 
         + precedence + " " 
         + check + " " + sec;
  }

  public String getCabrilloExchange() {
    String sec;
    if (section.length() > 3) sec = section.substring(0,4);
    else sec = section + U.findPad(section, 3); 
    String call = callsign.getCallsign();
    return U.trunc(call, 10) + U.findPad( call, 10 ) + " "
           + U.zpad.format(serial.doubleValue()) + " "
           + precedence + " "
           + check + " "  // won't bother about a possible 1-digit check...
           + sec; 
  }


  // the heart of the class; figure out what we've been given
  // INTERPRETS:  
  // dd: check; ddd=serial; c=prec; ccc=section;
  // ddc: check+prec; dddc: serial+prec
  public void addToExchange( String s) {
    int alphas = 0;
    int nums = 0; 
    StringTokenizer input = new StringTokenizer(s);
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
      if ( nums == 2 && tok.length() == 2 ) check = tok.toUpperCase();
      if ( alphas == 1 && tok.length() == 1 && isPrecedence(cs[0])) 
        precedence = tok.toUpperCase();
      if ( nums >= 3 && alphas == 0 && tok.length() == nums ) 
        serial = Integer.valueOf(tok);
      if ( nums == 0 && alphas >=2 ) section = tok.toUpperCase(); 
      // AT LEAST 2 alphas in a US callsign
      if ( nums >= 1 && alphas >=2 ) callsign = new Callsign(tok.toUpperCase());
      // if ( nums == 2 && tok.length() == 3 && isPrecedence(cs[2])) {
      //    check = tok.substring(0,2);
      //    precedence = tok.substring(2,3);
      // }
      if ( nums >=1 && tok.length() == nums+1 && isPrecedence(cs[cs.length-1])) {
         serial = Integer.valueOf(tok.substring(0,cs.length-1));
         precedence = tok.substring(cs.length-1,cs.length);
      }
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
        (String) p.get("callsign") + " " 
      + (String) p.get("arrlSection") + " "
      + (String) p.get("check") + " "
      + makePrecedence(p) + " "
      + le.getSerialAsPaddedString()
    );
  }

  private String makePrecedence(Properties p) {
    // all station properties must supply power.
    // strikes me as a lot of lookups, esp. since this is
    // may not change during a contest.
    String cat = (String)p.get("category");
    String pwr = (String)p.get("powerCategory");
    String bnd = (String)p.get("bandCategory");
    if ( cat.equals("SINGLE-OP") && pwr.equals("LOW")) return "A";
    if ( cat.equals("SINGLE-OP") && pwr.equals("HIGH")) return "B";
    if ( cat.equals("SINGLE-OP") && pwr.equals("QRP")) return "Q";
    if ( cat.equals("SINGLE-OP-ASSISTED")) return "U";
    if ( cat.startsWith("MULTI")) return "M";
    if ( cat.equals("SCHOOL-CLUB")) return "S";
    if ( cat.equals("CHECKLOG") && pwr.equals("LOW")) return "A";
    if ( cat.equals("CHECKLOG") && pwr.equals("HIGH")) return "B";
    if ( cat.equals("CHECKLOG") && pwr.equals("QRP")) return "Q";
    return "";
  }

  public boolean isComplete() { 
    return ! ( serial.equals(U.ZERO) || callsign==Callsign.NOCALL 
            || precedence.equals("") || check.equals("") || section.equals("") ); 
  }

  public String getMultiplierField() { return section ; }
  public void   setMultiplierField(String s) { section = s; }

  private boolean isPrecedence( char c ) {
    return ( c == 'a' || c == 'b' || c == 'q' || c == 'u' || c == 'm' || c == 's'
          || c == 'A' || c == 'B' || c == 'Q' || c == 'U' || c == 'M' || c == 'S'
           );
  } 

}