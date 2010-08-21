// exchange for the NA Sprint.  tricky because exchange depends
// on the location of the station, and includes the name, which is easily
// mistaken for a two-letter qth.  Very similar to naqp, but just different
// enough to warrant a separate class.
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class NASprintExchange extends NAQPExchange {

  static {
    for ( int i = 0; i < states.length; i++ ) statesMap.put( states[i], null); 
    statesMap.remove("HI");     
    for ( int i = 0; i < nonDomestic.length; i++)
      countryMap.put( nonDomestic[i][1], nonDomestic[i][0] );
  }

  public String getGUIExchange() {
    return U.zpad.format(serial.doubleValue()) + " " + name + " " + qth;
  }

  public String getCabrilloExchange() {
    // cab format only allows 3 sp for qth.
    String tqth;
    if ( qth.length() > 3 ) tqth = qth.substring(0,3);
    else tqth = qth;
    String call = callsign.getCallsign();
    return U.trunc(call, 10) + U.findPad( call, 10 ) + " "
           + U.zpad.format(serial.doubleValue()) + " "
           + U.trunc(name, 10) + U.findPad( name, 10) + " "
           + U.trunc(tqth, 3) + U.findPad( tqth, 3) ;
  }


  // the heart of the class; figure out what we've been given
  // INTERPRETS:  
  // cc : qth; cc..c : name; =cc : name (2 char); .cc : qth; =cc..c : qth
  //  The rules have gotten a little too bizarre, and I'm not sure the
  // escapes are adequate.  
  //  TWO CHARS IN THE STATE LIST: interpreted as a name (if name null),
  //     then as QTH, so giving name first is handled correctly
  //  QTH field also bounced to name if name is empty and 
  //       the callsign is not-NA (so can't have a QTH)
  //  I can imagine this having really odd side-effects.
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
      if ( nums == len ) serial = Integer.valueOf(tok);
      if ( (statesMap.containsKey(tok) || countryMap.containsValue(tok)) 
            && ! name.equals("") ) {
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
          // country = callsign.getCountry();
          // zone = callsign.getZone();
          if ( px != null && ! isDomestic()) qth = px;
        }
      }
      nums = 0; alphas = 0;
    }    
  }

  // We need some way to create the "sent" half of the two-way 
  // exchange that concentrates all contest-specific knowledge
  // in this object.  
  public void addToExchange(Properties p, LogEntry le) {
    this.addToExchange(
        p.getProperty("callsign") + " " 
      + le.getSerialAsPaddedString() + " "
      + p.getProperty("firstname") + " "
      + p.getProperty("state") + " ");
  }

  public void parseLoggedExchange( String s) {
    // handles the case where state is missing, but not name missing
    StringTokenizer st = new StringTokenizer(s);
    if (st.hasMoreTokens()) callsign = new Callsign(st.nextToken());
    if (st.hasMoreTokens()) serial = Integer.valueOf(st.nextToken());
    if (st.hasMoreTokens()) name = st.nextToken();
    if (st.hasMoreTokens()) qth = st.nextToken(); 
    sticky = true; // all ex read from log are 'sticky' 
  }

  public boolean isComplete() { 
    boolean northamerica = isNorthAmerica();
    if ( callsign == Callsign.NOCALL ) return false;
    if ( name.equals("") ) return false; 
    if ( serial.equals(U.ZERO)) return false; 
    if ( qth.equals("") ) return false; //QTH required in NASprint--??
    return true; 
  }

  public String getMultiplierField() { 
    if ( qth.equals("DC") ) return "MD";
    if ( qth.equals("NF") ) return "LB";
    if ( qth.equals("PE") ) return "LB";
    if ( qth.equals("NS") ) return "LB";
    if ( qth.equals("NB") ) return "LB";
    if ( qth.equals("NWT") ) return "YT";
    if ( qth.equals("NU") ) return "YT";
    return qth;
  }

  public boolean isDomestic() {
    String country = callsign.getCountry();
    // System.out.println("isDomestic: |" + country);
    return country.equalsIgnoreCase("united states") 
       || country.equalsIgnoreCase("alaska")
       || country.equalsIgnoreCase("canada") ;
  }

  public boolean isNorthAmerica() {
    return callsign.getContinent().equalsIgnoreCase("NA");
  }

}