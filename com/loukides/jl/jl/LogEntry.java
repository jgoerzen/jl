// general properties of a log entry (everything but the exchange).  Probably
// has a fair number of useless and redundant fields.
package com.loukides.jl.jl;
import com.loukides.jl.util.*;
import com.loukides.jl.contests.*;
import com.loukides.jl.keyer.MessageCommon;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*; 
import java.util.*;

// INHERITS Serializable
public class LogEntry extends AbstractLoggable {

  private int serial = 8999;
  private Exchange rcvd; 
  private Exchange sent; 
  private boolean dupe;
  private boolean multiplier = false;
  private char multChar = ' ';
  private boolean qtc = false;

  private boolean countryMultiplier = false; 
  private boolean countryQuery = false; 

  private int     qsoPoints = 0;
  private String name = "";

  public LogEntry( ExchangeFactory e, String mode ){
    band = "";  
    rcvd = e.getInstance();
    sent = e.getInstance();
    date = null;
    // df.setTimeZone(TimeZone.getTimeZone("GMT"));
    setMode(mode);
    sent.addToExchange( props, this );
    MessageCommon.setLogEntry(this); // Tell the keyer we have a new log entry!
    // (side-effect programming; horrible, but sometimes needed)
  }

  public String getSerialAsPaddedString() {
    String s = Integer.toString(serial);
    return padding.substring(0, 4-s.length()) + s;
  }

  public Exchange getSent() { return sent ; }
  public Exchange getRcvd() { return rcvd ; }

  public char getMultChar() { return multChar; }

  public void setDupe(boolean d){ dupe = d; }
  public boolean getDupe() { return dupe ;}

  // arguably not necessary, and only confusing
  public String getCountry() { return rcvd.getCountry(); }
  public String getZone() { return rcvd.getZone(); }

  public void setCountryMultiplier(boolean m) { countryMultiplier = m ; }
  public boolean isCountryMultiplier() { return countryMultiplier; }

  public void setCountryQuery(boolean m) { countryQuery = m ; };
  public boolean isCountryQuery() { return countryQuery; }

  public void setMultiplier(boolean m) { multiplier = m ; };
  public boolean isMultiplier() { return multiplier; }

  public void setMultChar( char c ) { multChar = c; }

  public void setQsoPoints( int i ) { qsoPoints = i; };
  public int getQsoPoints() { return qsoPoints; }

  public void setName( String s ) { name = s ;}
  public String getName() { return name; }

  public void setQTC( boolean q ) { qtc = q; }
  public boolean isQTC() { return qtc; }

  public String toCabrilloString() {    
    String khz = U.toFrequency(band);
    String cabrillo =  
                   "QSO: " + khz     + " " 
                 + mode + " " 
                 + timeToGMT(date) + " "
                 + sent.getSentCabrilloExchange() + " "
                 + rcvd.getCabrilloExchange();
    if ( ! txno.equals("") ) cabrillo += " "+ txno;
    return cabrillo;
  }

  public void parseCabrilloString(String logline) {
      String generalstr = logline.substring(0, b1).trim();
      String sentstr = logline.substring(b1, b2).trim();
      b3 = Math.min(b3, logline.length());
      String rcvdstr = logline.substring(b2, b3).trim();
      // System.out.println("|" + generalstr + "|");
      // System.out.println("|" + sentstr + "|");
      // System.out.println("|" + rcvdstr + "|");
      parseCommonFields(generalstr);
      rcvd.parseLoggedExchange(rcvdstr);
      sent.parseLoggedExchange(sentstr);
  }

  public boolean isComplete() { return rcvd.isComplete(); }

  public void setMode(String m) {
    super.setMode(m);
    // System.out.println("setMode: " + m); 
    // need to call rcvd and sent, so they know mode (for RST)
    rcvd.setMode(mode);
    sent.setMode(mode);
  }

}