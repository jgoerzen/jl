// exchange for the NAQP.  tricky because exchange depends
// on the location of the station, and includes the name, which is easily
// mistaken for a two-letter qth.  
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public class NAQPExchange extends AbstractExchange {

  protected String name = "";
  protected String qth = "";

  protected String state = ""; 

  protected static String [] states = {
    "CT", "MA", "ME", "NH", "RI", "VT", "NY", "NJ", "DE", "PA", "MD",
    "DC", "AL", "GA", "KY", "NC", "FL", "SC", "TN", "VA", "AR", "LA",
    "MS", "NM", "TX", "OK", "CA", "HI", "AZ", "WA", "ID", "MT", "NV",
    "OR", "UT", "WA", "WY", "AK", "MI", "OH", "WV", "IL", "IN", "WI",
    "CO", "IA", "KS", "MN", "MO", "NE", "ND", "SD", "NB", "NS", "QC",
    "ON", "MB", "SK", "AB", "BC", "NWT", "NF", "LB", "YT", "PE", "NU"
  };

  protected static String [][] nonDomestic = 
    {
      { "4U1/u",  "United Nations HQ NY" },
      { "HK0/a",  "San Andres/Providencia" },
//      { "HK0/m",  "Malpelo I." },  // don't know whether Malpelo is north america!
      { "6Y",     "Jamaica" },
      { "8P",     "Barbados" },
      { "C6",     "Bahamas" },
      { "CM",     "Cuba" },
      { "CY9",    "St. Paul I." },
      { "CY0",    "Sable I." },
      { "FG",     "Guadeloupe" },
      { "FM",     "Martinique" },
      { "FP",     "St. Pierre & Miquelon" },
      { "FS",     "French St. Martin" },
      { "HH",     "Haiti" },
      { "HI",     "Dominican Republic" },
      { "HP",     "Panama" },
      { "HR",     "Honduras" },
      { "J3",     "Grenada" },
      { "J6",     "St. Lucia" },
      { "J7",     "Dominica" },
      { "J8",     "St. Vincent" },
      { "KG4",    "Guantanamo Bay" },
      { "KP1",    "Navassa I." },
      { "KP2",    "US Virgin Is." },
      { "KP4",    "Puerto Rico" },
      { "KP5",    "Desecheo I." },
      { "OX",     "Greenland" }, 
      { "PJ8",    "Sint Maarten" },
      { "TG",     "Guatemala" }, 
      { "TI",     "Costa Rica" }, 
      { "TI9",    "Cocos I." },
      { "V2",     "Antigua & Barbuda" }, 
      { "V3",     "Belize" },
      { "V4",     "St. Kitts & Nevis" },
      { "VP2E",   "Anguilla" },
      { "VP2M",   "Montserrat" },
      { "VP2V",   "British Virgin Is." },
      { "VP5",    "Turks & Caicos" },
      { "VP9",    "Bermuda" },
      { "XE",     "Mexico" },
      { "XF4",    "Revilla Gigedo" },
      { "YN",     "Nicaragua" }, 
      { "YS",     "El Salvador" },
      { "ZF",     "Cayman Is." },
    } ;

  protected static HashMap statesMap = new HashMap(70)  ;
  protected static HashMap countryMap = new HashMap(50);

  boolean sticky = false;

  static {
    for ( int i = 0; i < states.length; i++ ) statesMap.put( states[i], null);      
    for ( int i = 0; i < nonDomestic.length; i++)
      countryMap.put( nonDomestic[i][1], nonDomestic[i][0] );
  }

  public String getGUIExchange() {
    return name + " " + qth;
  }

  public String getCabrilloExchange() {
    // cab format only allows 3 sp for qth.
    String tqth;
    if ( qth.length() > 3 ) tqth = qth.substring(0,3);
    else tqth = qth;
    String call = callsign.getCallsign();
    return U.trunc(call, 15) + U.findPad( call, 15 ) + " "
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
          if ( px != null && isNorthAmerica() && ! isDomestic()) qth = px;
          // not-NA can't have qth filled in; may have gotten a name 
          // by mistake.  Bounce down if qth is filled and name is empty
          if ( ! isNorthAmerica() && ! qth.equals("")){
            if ( name.equals("") ) name = qth; 
            qth = "";
          }
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
      + p.getProperty("firstname") + " "
      + p.getProperty("state") + " ");
  }

  public void parseLoggedExchange( String s) {
    // handles the case where state is missing, but not name missing
    StringTokenizer st = new StringTokenizer(s);
    if (st.hasMoreTokens()) callsign = new Callsign(st.nextToken());
    if (st.hasMoreTokens()) name = st.nextToken();
    if (st.hasMoreTokens()) qth = st.nextToken(); 
    // country = callsign.getCountry();  // you know, this doesn't really matter.
    // System.out.println("ple: " + country);
    sticky = true; // all ex read from log are 'sticky' 
  }

  public void modifySentExchange(String s) {
    // only a multi can change name mid-contest
    // We'll allow for the possibility that the category isn't exactly right, though
    // strictly speaking, only m-2 is allowed
    if (p.getProperty("category").startsWith("MULTI")) {
      this.addToExchange(s);
      p.setProperty("firstname", name); 
    }
  }

  public boolean isComplete() { 
    boolean northamerica = isNorthAmerica();
    if ( callsign==Callsign.NOCALL || name.equals("") ) return false; 
    if ( northamerica && qth.equals("") ) return false; 
    return true; 
  }

  public String getMultiplierField() { 
    if ( qth.equals("DC") ) return "MD";
    if ( qth.equals("NF") ) return "LB";
    return qth;
  }

  public boolean isDomestic() {
    // System.out.println("isDomestic: " + country);
    String country = callsign.getCountry();
    return country.equalsIgnoreCase("united states") 
       || country.equalsIgnoreCase("hawaii")
       || country.equalsIgnoreCase("alaska")
       || country.equalsIgnoreCase("canada") ;
  }

  public boolean isNorthAmerica() {
    // ARRL NA, plus Hawaii (per rules)
    if ( callsign.getContinent().equalsIgnoreCase("NA") ) return true;
    else if ( callsign.getCountry().equalsIgnoreCase("hawaii") ) return true;
    return false;
  }

}