package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class FieldDayExchange extends AbstractExchange {

  private String section = "";    // ARRL section 
  private String type = "";       // operation type designation (a, b, etc.)
  private String xmtrs = "";      // # of transmitters

  public String getGUIExchange() {
    return xmtrs + type + " " + section;
  }

  public String getCabrilloExchange() {
    String cat = xmtrs + type;
    String thiscall = callsign.getCallsign();
    return U.trunc(thiscall, 13) + U.findPad( thiscall, 13 ) + " "
         + U.trunc(cat, 3) + U.findPad( cat, 3) + " "
         + U.trunc(section, 6) + U.findPad(section, 6) ;
  }


  // the heart of the class; figure out what we've been given
  // INTERPRETS:  
  // ccc=section; ddc=category; dd=# xmtrs; c = operation type
  public void addToExchange( String s) {
    int alphas = 0;
    int nums = 0; 
    StringTokenizer input = new StringTokenizer(s.toUpperCase());
    // System.out.println( "S: " + s + " tokens: " + input.countTokens() );
    while ( input.hasMoreTokens() ) {
      String tok = input.nextToken();
      char [] cs = tok.toCharArray();
      for (int i = 0 ; i < cs.length; i++ ) {
        if ( Character.isDigit(cs[i]) ) nums++;
        if ( Character.isLetter(cs[i]) ) alphas++;
      }
      // System.out.println("Token: " + tok + " " + nums + " " + alphas);
      if ( alphas == tok.length() && alphas >= 2) section = tok;
      else if ( alphas == 1 && (nums == 1 || nums == 2) ) {
        if ( cs[cs.length -1 ] <= 'F' ) 
          type = tok.substring(cs.length -1);
        xmtrs = tok.substring(0, cs.length -1);
      }
      else if ( alphas == 1 && cs.length == 1 && cs[0] <= 'F' ) type = tok;
      else if ( alphas == 0 && nums == cs.length ) xmtrs = tok;
      // AT LEAST 2 alphas in a US callsign
      else if ( nums >= 1 && alphas >=2 ) callsign = new Callsign(tok);
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
      + p.getProperty("fieldDayCategory") + " "
      + p.get("arrlSection") 
    );
  }

  public boolean isComplete() { 
    return ! ( callsign==Callsign.NOCALL || type.equals("")
            || xmtrs.equals("") || section.equals("") ); 
  }

  public String getMultiplierField() { return section ; }

}