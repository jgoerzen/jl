package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

public abstract class AbstractExchange implements Exchange {

  protected Callsign callsign = Callsign.NOCALL;
  protected Integer serial = new Integer(0); // serial number
  protected String mode;

  protected static String mycall;
  protected static Properties p;
  public AbstractExchange() {}; 

  public static void setProperties(Properties props) { 
    p = props ;
    mycall = (String) p.get("callsign");
  }

  public void setRST( String s ) {}; 
  public int getSerial() { return serial.intValue() ; }
  public void setSerial( int s ) { serial = new Integer(s); }
  public String getCallsign() { return callsign.getCallsign(); }
  public CountryRecord getCountryRecord() { return callsign.getCountryRecord();}
  public String getCountry() { return callsign.getCountry(); }
  public String getZone() { return callsign.getZone(); }
  public String getCanonicalPrefix() { return callsign.getCanonicalPrefix();}
  public String getContinent() { return callsign.getContinent(); }

  public abstract String getGUIExchange();

  public abstract String getCabrilloExchange();

  // in most contests, exchange is symmetric
  public String getSentCabrilloExchange() { return getCabrilloExchange(); }

  public void setMode(String s) { 
    // System.out.println("AE:setMode: " + s);
    mode = s; 
    // give the exchange a chance to change its default report.
    addToExchange("");
  }

  // the heart of the class; figure out what we've been given
  public abstract void addToExchange( String s) ;
  // the implementation for most contests
  public void modifySentExchange(String s) {} 

  // We need some way to create the "sent" half of the two-way 
  // exchange that concentrates all contest-specific knowledge
  // in this object.  For several reasons, can't be a constructor.
  // try: an overloaded version of addToExchange
  // for the sent exchange; get stuff out of the properties and 
  // use our own tools to populate the object.  This guarantees that we can 
  // print in in cab form, etc., and isolates all the contest-specific stuff
  // in this class
  public abstract void addToExchange(Properties p, LogEntry le);

  public void parseLoggedExchange(String s) { addToExchange(s); }

  public abstract boolean isComplete();

  // can return different fields, on a per-contest basis.
  // returns the actual part of the exchange that is used to compute
  // the multiplier
  public abstract String getMultiplierField();
  public String getRoverField() { return getMultiplierField(); }

}