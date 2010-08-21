package com.loukides.jl.util;

// various utilities and constants...

import java.util.*;
import java.text.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;

public class U {

  private static final String padsrc = "                 " ;
  public static final Integer ZERO = new Integer(0);
  public static final DecimalFormat zpad = new DecimalFormat("0000");
  public static final DecimalFormat freqpad = new DecimalFormat("0000000.000");
  public static final DecimalFormat degreeFormat = new DecimalFormat("00.0");
  public static final DecimalFormat zpad3 = new DecimalFormat("000");
  public static final DecimalFormat zpad2 = new DecimalFormat("00");
  public static final DecimalFormat zpad3or4 = new DecimalFormat("#000");
  public static final long MINUTE = 1000 * 60;
  public static DateFormat timef = new SimpleDateFormat("yyyy-MM-dd HHmm");
  public static DateFormat dayf = new SimpleDateFormat("yyyy-MM-dd");
  
  public static final int SOCKET = 5454;
//  public static final int SHELFSIZE = 6; // should be a property

  public static final double PI = 3.14159265;
  public static final double deg2radians = PI/180.0;

  static {
    timef.setTimeZone(TimeZone.getTimeZone("GMT"));
    dayf.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  // returns a string of space chars sufficient to pad t to width
  // maybe should just return the padded string itself--then it count control
  // truncation properly
  public static String findPad( String t, int width ) {
    int l = width - t.length();
    if (l < 0) l = 0;
    return padsrc.substring( 0, l);
  }

  public static String trunc( String t, int width ) {
    if ( t.length() > width ) return t.substring( 0, width);
    return t;
  }

  public static void die(String s) {
    System.out.println(s);
    System.exit(1);
  }

  public static void setSizes(JComponent c, Dimension d) {
    c.setSize(d);
    c.setPreferredSize(d);
    c.setMinimumSize(d);
    c.setMaximumSize(d);
  }

  // nothing more than a holder for the latitude and longitude
  private static class Location {
    double latitude;
    double longitude;

    public String toString() { 
      String ns = "S", ew = "E";
      if (latitude >=0) ns = "N";
      if (longitude >= 0) ew = "W";
      return "Latitude: " + latitude + ns + " Longitude: " + longitude + ew ;
    }

    public boolean equals(Location l) {
      // exactly equality is OK, even w/ double, because we're only dealing with
      // grid square centers.
      return ((latitude == l.latitude) && (longitude == l.longitude));
    }
  }

  // find the latitude and longitude of the center of a (4-character) grid square
  // negative latitude is east; negative longitude is south
  // doesn't check whether the grid square is valid
  private static Location findLatLong(String g) {
    String grid = g.toUpperCase();
    final char A = 'A';
    double deglatperfinegrid = 1.0/24;
    double deglongperfinegrid = 2.0/24;
    int longvyfineoffset = 0; 
    int latvyfineoffset = 0; 
    int longcoarseoffset = (int)grid.charAt(0) - (int)A;
    int latcoarseoffset = (int)grid.charAt(1) - (int)A;
    int longfineoffset = Integer.parseInt(grid.substring(2,3));
    int latfineoffset = Integer.parseInt(grid.substring(3,4));
    Location result = new Location();
    result.latitude = -90 + 10*latcoarseoffset + latfineoffset;
    result.longitude = 180 - 20*longcoarseoffset - 2*longfineoffset;
    if (grid.length() == 4) { // just go to the center of the square
      result.latitude += 0.5;
      result.longitude -= 1.0;
    } else if (grid.length() == 6) { // deal with the subsquare
      longvyfineoffset = (int)grid.charAt(4) - (int)A;
      latvyfineoffset = (int)grid.charAt(5) - (int)A;
      result.latitude += latvyfineoffset*deglatperfinegrid + 1.25/60;
      result.longitude += -longvyfineoffset*deglongperfinegrid - 2.5/60;
    }
    //System.out.println("long:" + longcoarseoffset + " " + longfineoffset + " " + longvyfineoffset);
    //System.out.println("lat: " + latcoarseoffset  + " " + latfineoffset  + " " + latvyfineoffset);
    // System.out.println("lat/long for " + g + ": " + result.toString());
    return result;
  }

  // Based on great circle forumulae at http://williams.best.vwh.net/avform.htm
  // (Ed Williams, Aviation Formulary, V1.37
  // assumes west, north are positive
  public static double findDistanceRadians(String me, String him) {
    Location sentloc = findLatLong(me);
    Location rcvdloc = findLatLong(him);
    // System.out.println( sentgrid + " " + sentloc.toString());
    // System.out.println( rcvdgrid + " " + rcvdloc.toString());
    double radd;
    double lat1 = sentloc.latitude   * deg2radians;
    double lat2 = rcvdloc.latitude  * deg2radians;
    double lon1 = sentloc.longitude  * deg2radians;
    double lon2 = rcvdloc.longitude * deg2radians;
    // Williams gives two formulae; this is the more accurate for close distances.
    // In practice, the two differed only in the 8th or 9th place, for 
    // separations as small as 1 degree.
    radd=2*Math.asin(Math.sqrt(Math.pow(Math.sin((lat1-lat2)/2),2.0) +
                  Math.cos(lat1)*Math.cos(lat2)*Math.pow(Math.sin((lon1-lon2)/2),2.0)));
    return radd;
  }

  public static double findDistanceKM(String me, String him) {
    final double earthradiuskm = 6371.0;  // FAI standard
    return findDistanceRadians(me, him) * earthradiuskm;
  }

  // also from Williams (op. cit.); note that the great-circle course is NOT
  // what you expect for a due east-west path.
  public static double findHeadingDegrees(String me, String him) {
    double tc1;
    double d = findDistanceRadians(me, him);
    Location sentloc = findLatLong(me);     // YUCK extra work!
    Location rcvdloc = findLatLong(him);
    double lat1 = sentloc.latitude   * deg2radians;
    double lat2 = rcvdloc.latitude  * deg2radians;
    double lon1 = sentloc.longitude  * deg2radians;
    double lon2 = rcvdloc.longitude * deg2radians;
    double tmp = 
      (Math.sin(lat2)-Math.sin(lat1)*Math.cos(d))/(Math.sin(d)*Math.cos(lat1));
    if (tmp > 1) tmp = 1;        // hack to prevent nans (for taking acos of >1)
    else if (tmp < -1) tmp = -1; // ditto
    if ( Math.sin(lon2-lon1) < 0 )      
      tc1=Math.acos(tmp);   
    else      
      tc1=2*PI-Math.acos(tmp);
    double headingdeg = tc1 * ( 1/deg2radians );
    // System.out.println("Heading: " + headingdeg);
    return headingdeg;
  }

  public static String findPrefix(String call) {
    String first = "";
    String second = "";
    String prefix = "";
    int lfirst, lsecond;
    int slash = call.indexOf("/");
    if (slash > 0) {
      first = call.substring(0, slash); lfirst = first.length();
      second = call.substring(slash+1); lsecond = second.length();
      prefix = (lfirst > lsecond) ? second : first ; 
      if ( prefix.equals("P") || prefix.equals("MM") || prefix.equals("A")
        || prefix.equals("E") || prefix.equals("J")  || prefix.equals("P")
        || prefix.equals("AE") || prefix.equals("AG")
        || ( (prefix.length() == 1) && Character.isDigit(prefix.charAt(0)) ) ) {
        prefix = noSlash( (lfirst < lsecond) ? second: first );
      }
      if ( ! Character.isDigit(prefix.charAt(prefix.length()-1))) prefix += "0";
    } else prefix =  noSlash(call);
    // System.out.println("findPrefix: " + first + " " + second + " " + prefix);
    return prefix;
  }

  private static String noSlash(String call) { 
    int i;
    if (call.length() < 3) return "";
    for ( i = call.length() -1; i >=0; i-- ) { // iterate backwards through call
      if ( Character.isDigit(call.charAt(i)) ) return call.substring(0,i+1);
    }
    // if we get here, we didn't find a digit
    // (this case is mentioned in the rules, but note that this program won't
    // recognize a call with no digits.)
    return call.substring(0,2) + "0";
  }

  // UTILITIES FROM LogEntry; may as well be here.
  // one place with hard-coded band knowledge.  Overladed so freq can be string, int, 
  // or float.
  public static String toMeters( String freq ){
    int ifreq;
    // First, for no good reason at all, special-case the things that we expect to
    // read from log files
    if (freq.equals("1800"))  return "b" + 160;
    if (freq.equals("3500"))  return "b" + 80;  // coming from the tokenizer, so 
    if (freq.equals("7000"))  return "b" + 40;  // no leading space
    if (freq.equals("10100")) return "b" + 30; // needed for general-purpose logging
    if (freq.equals("14000")) return "b" + 20;
    if (freq.equals("18068")) return "b" + 17; // needed for general-purpose logging
    if (freq.equals("21000")) return "b" + 15;
    if (freq.equals("24890")) return "b" + 12; // needed for general-purpose logging
    if (freq.equals("28000")) return "b" + 10;
    if (freq.equals("50"))    return "b" + 6;
    if (freq.equals("144"))   return "b" + 2;
    if (freq.equals("222"))   return "b" + 220;
    if (freq.equals("432"))   return "b" + 432;
    if (freq.equals("902"))   return "b" + 902;
    if (freq.equals("1.2"))   return "b" + 1296;
    if (freq.equals("2.3"))   return "b" + 2300;
    // Someone tell me why I'm doing it this way...
    // The code below could *very clearly* replace all the code above.
    // I suppose I'm not, at this point, sure how rigs will return frequencies above
    // 50 MHz, and not yet quite willing to scrap code that I know works.
    try {
      ifreq = Integer.parseInt(freq);
    } catch (NumberFormatException e) { 
      return "BOGUS"; 
    }
    if ( ifreq >=   1800 && ifreq <=   2000) return "b" + 160;  
    if ( ifreq >=   3500 && ifreq <=   4000) return "b" + 80; 
    if ( ifreq >=   7000 && ifreq <=   7300) return "b" + 40; 
    if ( ifreq >=  10100 && ifreq <=  10150) return "b" + 30; 
    if ( ifreq >=  14000 && ifreq <=  14350) return "b" + 20; 
    if ( ifreq >=  18068 && ifreq <=  18168) return "b" + 17; 
    if ( ifreq >=  21000 && ifreq <=  21450) return "b" + 15; 
    if ( ifreq >=  24890 && ifreq <=  24990) return "b" + 12; 
    if ( ifreq >=  28000 && ifreq <=  29700) return "b" + 10;
    if ( ifreq >=  50000 && ifreq <=  54000) return "b" + 6; 
    if ( ifreq >= 144000 && ifreq <= 148000) return "b" + 2; 
    return "BOGUS";
  }

  public static String toMeters(int f) {
    return toMeters(Integer.toString(f));
  }

  public static String toMeters(float f) {
    return toMeters(Integer.toString(Math.round(f)));
  }

  public static String toFrequency( String band ){
    if (band.equals("b160")) return " 1800" ;
    if (band.equals("b80"))  return " 3500" ;
    if (band.equals("b40"))  return " 7000" ;
    if (band.equals("b30"))  return "10100" ;
    if (band.equals("b20"))  return "14000" ;
    if (band.equals("b17"))  return "18068" ;
    if (band.equals("b15"))  return "21000" ;
    if (band.equals("b12"))  return "24890" ;
    if (band.equals("b10"))  return "28000" ;
    if (band.equals("b6"))   return "   50" ;
    if (band.equals("b2"))   return "  144" ;
    if (band.equals("b220")) return "  222" ;
    if (band.equals("b432")) return "  432" ;
    if (band.equals("b902")) return "  902" ;
    if (band.equals("b1296")) return "  1.2" ;
    if (band.equals("b2300")) return "  2.3" ;
    // not sure how the 70c designation will work out...
    // actually, I wouldn't mind nuking the band designation, in favor of things
    // like 40m 20m 2m 70cm
    if (band.equals("b70c")) return "  432" ;
    return "BOGUS";
  }

  public static String cutLeadingZeros(String s) {
    if (s.startsWith("0000")) return "tttt" + s.substring(4);
    if (s.startsWith("000")) return "ttt" + s.substring(3);
    if (s.startsWith("00")) return "tt" + s.substring(2);
    if (s.startsWith("0")) return "t" + s.substring(1);
    return s;
  }

}