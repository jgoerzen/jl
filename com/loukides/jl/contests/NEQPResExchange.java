// exchange for the New England QSO Party (NE residents)
// EXTENDED BY non-resident exchange
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.util.*;
import java.io.*;
import java.text.*;

public class NEQPResExchange extends AbstractExchange {

  protected String report = "";
  protected String county = "";
  protected String state = ""; 

  // all new england counties and states.  
  private static String [][] counties = 
    {
      { "FAI", "CT" },
      { "HAR", "CT" },
      { "LIT", "CT" },
      { "MID", "" },
      { "MIC", "MID CT" },
      { "NHV", "CT" },
      { "NLN", "CT" },
      { "TOL", "CT" },
      { "WIN", "CT" },
      { "AND", "ME" },
      { "ARO", "ME" },
      { "CUM", "ME" },
      { "FRA", "" },
      { "FRE", "FRA ME" },
      { "HAN", "ME" },
      { "KEN", "ME" },
      { "KNO", "ME" },
      { "LIN", "ME" },
      { "OXF", "ME" },
      { "PEN", "ME" },
      { "PIS", "ME" },
      { "SAG", "ME" },
      { "SOM", "ME" },
      { "WAL", "ME" },
      { "WAS", "" },
      { "WAM", "WAS ME" },
      { "YOR", "ME" },
      { "BAR", "MA" },
      { "BER", "MA" },
      { "BRI", "" },
      { "BRM", "BRI MA" },
      { "DUK", "MA" },
      { "ESS", "" },
      { "ESM", "MA" },
      { "FRA", "MA" },  // should be indeterminate...
      { "FRM", "MA" },
      { "HMD", "MA" },
      { "HMP", "MA" },
      { "MID", "" },
      { "MIM", "MID MA" },
      { "NAN", "MA" },
      { "NOR", "MA" },
      { "PLY", "MA" },
      { "SUF", "MA" },
      { "WOR", "MA" },
      { "BEL", "NH" },
      { "CAR", "NH" },
      { "CHE", "NH" },
      { "COO", "NH" },
      { "GRA", "" },
      { "GRN", "GRA NH" },
      { "HIL", "NH" },
      { "MER", "NH" },
      { "ROC", "NH" },
      { "STR", "NH" },
      { "SUL", "NH" },
      { "BRI", "" },
      { "BRR", "BRI RI" },
      { "KNT", "RI" },
      { "NEW", "RI" },
      { "PRO", "RI" },
      { "WAS", "" },
      { "WAR", "WAS RI" },
      { "ADD", "VT" },
      { "BEN", "VT" },
      { "CAL", "VT" },
      { "CHI", "VT" },
      { "ESS", "" },
      { "ESV", "ESS VT" },
      { "FRA", "" },
      { "FRV", "FRA VT" },
      { "GRA", "" },
      { "GRV", "GRA VT" },
      { "LAM", "VT" },
      { "ORA", "VT" },
      { "ORL", "VT" },
      { "RUT", "VT" },
      { "WAS", "" },
      { "WAV", "WAS VT" },
      { "WNH", "VT" },
      { "WND", "VT" }
    } ;

  protected static HashMap countyMap = new HashMap(87);

  boolean sticky = false;

  static {
    for ( int i = 0; i < counties.length; i++ ) 
      countyMap.put( counties[i][0], counties[i][1]);      
  }

  public String getGUIExchange() {
    return report + " " + county + " " + state;
  }

  public String getCabrilloExchange() {
    String call = callsign.getCallsign();
    return U.trunc(call, 13) + U.findPad( call, 13 ) + " "
         + U.trunc(report, 3) + U.findPad( report, 3) + " "
         + U.trunc(county, 3) + U.findPad(county, 3) + " "
         + U.trunc(state, 2) + U.findPad(state, 2);
  }


  // the heart of the class; figure out what we've been given
  // INTERPRETS:  
  public void addToExchange( String s) {
    int alphas = 0;
    int nums = 0; 
    String countyInfo;
    StringTokenizer input = new StringTokenizer(s.toUpperCase());
    while ( input.hasMoreTokens() ) {
      String tok = input.nextToken();
      char [] cs = tok.toCharArray();
      int len = tok.length();
      for (int i = 0 ; i < cs.length; i++ ) {
        if ( Character.isDigit(cs[i]) ) nums++;
        if ( Character.isLetter(cs[i]) ) alphas++;
      }
      if ( nums == len ) report = tok;
      else if (alphas == len && len == 2) {
        state = tok;
      }
      else if ( tok.equals("\'\'")) county = "";
      else if (alphas == len && len == 3) {
        countyInfo = (String)countyMap.get(tok);
        if (countyInfo == null) countyInfo = ""; // make sure string is valid
        int n = countyInfo.length();
        if ( n == 0 )    county = tok;
        else if ( n==2 ) { county = tok; state = countyInfo; }
        else if ( n==6 ) { county = countyInfo.substring(0,3); 
                           state =  countyInfo.substring(4);
        }        
      }
      else if ( alphas == len && len == 5) {
        county = tok.substring(0,3);
        state = tok.substring(3);
      }
      // AT LEAST 2 alphas in a US callsign
      else if ( nums >= 1 && alphas >=2 ) {
        callsign = new Callsign(tok);
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
        p.getProperty("callsign") + " " + p.getProperty("countyAbbrev"));
  }

  //  public void parseLoggedExchange( String s) 
  // because of interactions between the county and state fields, 
  // we want this passed through the exchange parser, which is the default
  // implementation

  public boolean isComplete() { 
    if ( callsign != Callsign.NOCALL ) {
      // note--if isNewEngland is true, we have a state
      if ( isNewEngland() && ! county.equals("") ) return true;
      else if ( ! state.equals("") ) return true;
    }
    return false;
  }

  public String getMultiplierField() { 
    return state;
  }

  public String getRoverField() { 
    return county + " " + state;
  }

  protected boolean isNewEngland() {
    if ( state.equals("CT") || state.equals("MA") || state.equals("NH") || 
         state.equals("VT") || state.equals("ME") || state.equals("RI") )
      return true;
    else return false;
  }

}