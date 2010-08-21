// reads cty.dat format file (for CT 9), converts to a hashtable for country lookups
// download cty.dat from k1ea web site
package com.loukides.jl.util;
import com.loukides.jl.jl.*;

import java.io.*;
import java.util.*;

public class CtyToHash {
  static HashMap countryTable = new HashMap();
  static String mode;
  static String inputname = "cty.dat";

  public static void main( String [] args ) {
    if (args[0].toLowerCase().startsWith("cqww")) mode = "CQWW";
    else mode = "ITU";
    String outputname = args[0];
    getCountryTable(mode, inputname);
    try {
      File sf = new File("data/" + outputname + ".ser");
      ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream(sf));
      oos.writeObject(countryTable);
    } catch ( IOException e ) { System.out.println("excepted " + e); }  // abnormal eof
  }

  public static HashMap getCountryTable(String mode, String filename) {
    BufferedReader pr = null;
    String l;
    HeaderInfo header = null;
    if ( ! countryTable.isEmpty() ) return countryTable; // if not empty, must be full
    try {
      File f = new File("data/" + filename );
      pr = new BufferedReader( new FileReader(f));
      while ( (l = pr.readLine()) != null ) {
        // System.out.println(l);
        if ( l.charAt(0) == ' ' ) processPrefixes(l, header, mode);
        else {
          header = processHeader(l, mode);
          // System.out.println(header);
        }
      }
    } catch ( IOException e ) { System.out.println(e); System.exit(1); }
    return countryTable;
  }

  private static void processPrefixes( String line, HeaderInfo header, String mode) {
    String prefix;
    String zone;
    String cqwwzoneoverride;
    String ituzoneoverride;

    // System.out.println("processPrefixes: " + line + "|\nprocessPrefixes: " + header); 
    if ( header.canonicalPrefix.startsWith("*") && ( ! mode.equals("CQWW") ) ) 
      return;  // * indicates an entry only used in CQWW and WAE
    if ( header.canonicalPrefix.startsWith("*") ) 
      header.canonicalPrefix = header.canonicalPrefix.substring(1); // trim leading *
    StringTokenizer st = new StringTokenizer(line.toLowerCase().trim(), ",;");
    while (st.hasMoreTokens()) {
      cqwwzoneoverride = "";
      ituzoneoverride = "";
      StringTokenizer prefixTokenizer = new StringTokenizer(st.nextToken(), "()[]", true);
      prefix = prefixTokenizer.nextToken();
      while (prefixTokenizer.hasMoreTokens()) {
        String nexttoken = prefixTokenizer.nextToken();
        if ( nexttoken.equals("(")) {
          cqwwzoneoverride = prefixTokenizer.nextToken(); // grab the cq zone
          prefixTokenizer.nextToken();                    // consume the close paren
        } else if ( nexttoken.equals("[")) {
          ituzoneoverride = prefixTokenizer.nextToken();
          prefixTokenizer.nextToken();                    // consume the close bracket
        }
      }
      if ( mode.equals("CQWW")) {
        if ( cqwwzoneoverride.equals("")) zone = header.cqwwZone;
        else zone = cqwwzoneoverride;
      }
      else if (mode.equals("ITU")) {
        if (ituzoneoverride.equals("")) zone = header.ituZone;
        else zone = ituzoneoverride;
      }
      else zone = ""; // error
      CountryRecord rec =
        new CountryRecord( prefix, header.canonicalPrefix, zone, 
                           header.countryName, header.continent );
      if ( !(countryTable.containsKey(prefix) && mode.equals("CQWW")))
        countryTable.put( prefix, rec );
      // System.out.println(rec);
    }
  }

  private static HeaderInfo processHeader(String line, String mode) {
    // second arg not used, but for symmetry.
    HeaderInfo h = new HeaderInfo();
    StringTokenizer st = new StringTokenizer(line.toLowerCase(), ":");
    h.countryName = st.nextToken().trim();
    h.cqwwZone = st.nextToken().trim();
    h.ituZone = st.nextToken().trim();
    h.continent = st.nextToken().trim().toUpperCase();
    h.latitude = st.nextToken().trim();
    h.longitude = st.nextToken().trim();
    h.gmtOffset = st.nextToken().trim();
    h.canonicalPrefix = st.nextToken().trim().toUpperCase();

    return h;
  }

  static class HeaderInfo {
    public String countryName = "";
    public String cqwwZone = "";
    public String ituZone = "";
    public String continent = "";
    public String latitude = "";
    public String longitude = "";
    public String gmtOffset = "";
    public String canonicalPrefix = ""; 

    public String toString() {
      return "|" + countryName + 
             "|" + cqwwZone + 
             "|" + ituZone + 
             "|" + continent + 
             "|" + latitude + 
             "|" + longitude + 
             "|" + gmtOffset + 
             "|" + canonicalPrefix + "|";
    }
  }

}
