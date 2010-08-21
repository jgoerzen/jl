// a new callsign object, in which ALL callsigns know their country
package com.loukides.jl.jl;
import com.loukides.jl.util.*;

import java.util.*;
import java.io.*;

public class Callsign implements Serializable {

  private String callsign;
  private CountryRecord myCountry;
  private static Properties props;  
  private static HashMap countrytable;
  private static String myself = "";

  public static final Callsign NOCALL = new Callsign("", CountryRecord.NOCOUNTRY);

  public Callsign(String callsign) {
    this.callsign = callsign;
    myCountry = findCountry(callsign);
  }

  // probably only for creating NOCALL, but there could also be a MYSELF
  // item that would optimize object creation
  public Callsign(String callsign, CountryRecord cr) {
    this.callsign = callsign;
    myCountry = cr;
  }

  // this leads to a lot of nasty "callsign.getCallsign()" stuff
  // which is probably worth avoiding... callsigns are sometimes strings,
  // sometimes objects
  public String getCallsign() { return callsign ;}

  public CountryRecord getCountryRecord() { return myCountry; }

  public String getCountry() { return myCountry.getName(); } 
  public String getZone() { return myCountry.getZone(); } 
  public String getCanonicalPrefix() { return myCountry.getCanonicalPx(); } 
  public String getContinent() { return myCountry.getContinent(); }

  public static void setProperties(Properties p) { 
    props = p; 
    // somewhat sillier machinations than really needed to account for old-style contest
    // definitions (pre-cty.dat)
    String filename = p.getProperty("countryData", "cty.dat");
    String zoneType = p.getProperty("zoneType", "");
    if (zoneType.equals("")) 
      if (p.getProperty("countryfile", "dxcccountries.ser").toLowerCase().startsWith("cqww"))
        zoneType = "CQWW";
      else 
        zoneType = "ITU";
      // System.out.println("zone type: " + zoneType);
    countrytable = CtyToHash.getCountryTable(zoneType, filename);
    myself = p.getProperty("callsign", "");
  }

  private static CountryRecord findCountry( String call ) {
    // first test for an exact match (with the = prefix), then take the
    // longest (first) match; start w/ whole call, take chars. off right end.
	// ONE THING I REALLY DON'T LIKE: knowledge about cty.dat is now in two classes.  
	// Move this method into CtyToHash?
    String lc = call.toLowerCase();
	CountryRecord cr = (CountryRecord) countrytable.get("=" + lc);
	if (cr != null) return cr;
    for ( int i = lc.length(); i > 0; i--) {
      String partial = lc.substring(0, i);
      // System.out.println( call + " |" + partial + "|" );      
      cr = (CountryRecord) countrytable.get(partial);
      if (cr != null) {
        // nasty fix for guantanamo, since KG4 is also a valid K call.
        // (Does cty.dat handle this through exceptions?  Not as of 6/08)
        if ( ! cr.getName().equalsIgnoreCase("Guantanamo Bay"))  return cr;
        else if ( call.length() == 5) return cr;
        else return findCountry("K"); 
      }
    }
    return CountryRecord.NOCOUNTRY;
  }

  public String toString() { return callsign; }

}
