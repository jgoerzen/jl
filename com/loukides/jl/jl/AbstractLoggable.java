// Common features of the "loggable" classes.  Extended only by LogEntry and QTCEntry.
package com.loukides.jl.jl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


import com.loukides.jl.util.U;

public abstract class AbstractLoggable implements Loggable {

  public static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HHmm");
  public static final DateFormat minutesformat = new SimpleDateFormat("HHmm");
  final static String padding = "0000";
  protected static Properties props;
  protected static String txno = "";
  protected String band;
  protected float frequency = 0.0f;
  protected String mode; 
  protected Date date;
  protected static int b1, b2, b3;

  static {
    df.setTimeZone(TimeZone.getTimeZone("GMT"));
    minutesformat.setTimeZone(TimeZone.getTimeZone("GMT"));
  }  

  public static void setProperties(Properties p) { 
    props = p; 
    b1 =  Integer.parseInt(p.getProperty( "endGeneral" ));
    b2 =  Integer.parseInt(p.getProperty( "endSent" ));
    b3 =  Integer.parseInt(p.getProperty( "endRcvd" ));
    txno = props.getProperty("transmitterNumber", "");
  }

  public void setBand(String b){
    // System.out.println("setBand: " + b); 
    if ( b == null || b.equals("") )
       band = "BOGUS";
    else band = b;
  }
  public String getBand() { return band; }

  public void setFrequency(int f) {
    frequency = (float)f;
    setBand(U.toMeters(f));
  }

  public void setFrequency(float f) {
    frequency = f;
    setBand(U.toMeters(f));
  }

  public float  getFrequency() { return frequency; }

  public void setMode(String m){
    if ( m == null || m.equals("") )
       mode = "BG";
    else mode = m.toUpperCase();
  }
  public String getMode() { return mode; }

  public String timeToGMT (Date date) {
    String contactgmt = df.format(date);
    return contactgmt;  
  }

  public void setDate(Date d){
    if ( d == null ) {
      GregorianCalendar g = new GregorianCalendar();
      date = g.getTime();
    } else date = d;
  }
  public Date getDate() { return date; }

  public abstract String toCabrilloString();
  public abstract void parseCabrilloString(String s);
  public abstract boolean isComplete();

  protected void parseCommonFields( String s )  {
      StringTokenizer st = new StringTokenizer(s);
      st.nextToken(); // discard first (QSO:)
      this.setBand( U.toMeters( st.nextToken().toLowerCase() ) );
      this.setMode( st.nextToken() ); 
      String ds = st.nextToken() + " " + st.nextToken(); // time
      try {
        date = df.parse(ds);
      } catch ( java.text.ParseException e ) { System.out.println( e); }
      this.setDate(date); 
  }

}