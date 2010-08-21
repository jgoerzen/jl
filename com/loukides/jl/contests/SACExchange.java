package com.loukides.jl.contests;

import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

public class SACExchange extends GenericSerialExchange {

  // In SAC, multipliers are based on a weird conception of a Scandinavian callsign
  // area.  Not sure I understand this...
  public String getMultiplierField() {
    if ( ! isScandinavian() ) return "";
    String country = callsign.getCountry();
    if (    country.equalsIgnoreCase("Svalbard") 
         || country.equalsIgnoreCase("Jan Mayen") 
         || country.equalsIgnoreCase("Aland Is.") 
         || country.equalsIgnoreCase("Market Reef") 
         || country.equalsIgnoreCase("Greenland") 
         || country.equalsIgnoreCase("Faroe Is.") ) return country;
    char area = callsign.getCallsign().charAt(2); // should be a numeral
    // relying on the coincidence that all Scandinavian prefixes are 3-chars, ending
    // with a call area.  Per rules, portables with no area count for area 0
    if ( area == '/' ) area = '0';  
    // System.out.println(area + " " + country);
    return country + "-" + area;
  }

  // note: strings have to match the names in cty.dat exactly.  (Bah...) 
  public boolean isScandinavian() {
    String country = callsign.getCountry();
    if (    country.equalsIgnoreCase("Norway") 
         || country.equalsIgnoreCase("Finland") 
         || country.equalsIgnoreCase("Denmark") 
         || country.equalsIgnoreCase("Sweden") 
         || country.equalsIgnoreCase("Svalbard") 
         || country.equalsIgnoreCase("Jan Mayen") 
         || country.equalsIgnoreCase("Aland Is.") 
         || country.equalsIgnoreCase("Market Reef") 
         || country.equalsIgnoreCase("Faroe Is.") 
         || country.equalsIgnoreCase("Iceland") 
         || country.equalsIgnoreCase("Greenland") ) return true;
    return false;
  }

  public boolean isComplete() {
    if (callsign == Callsign.NOCALL) return false;
    if (report.equals("")) return false;
    if (serial.equals(U.ZERO)) return false;
    return true;
  }

}