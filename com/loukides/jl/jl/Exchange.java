package com.loukides.jl.jl;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.util.*;
import java.io.*;

public interface Exchange extends Serializable {

  // called to add (replace) one or more space-separated elements
  // to an exchange. The second routine lets you create a "sent" exchange
  // without requiring LogEntry to know anything about the exchange format.
  // (note that LogEntry carries the outgoing serial, at least for now.)
  // The final routine allows the main window to modify the exchange that's sent out.
  // This is presumably a null implementation for most contests (which typically
  // have a constant exchange), but allows for exceptions (most notably NAQP multi).
  public void addToExchange( String s ); 
  public void addToExchange( Properties p, LogEntry le );
  public void parseLoggedExchange( String s);
  public void modifySentExchange(String s);

  // different ways to display the exchange; the exchange knows 
  // how to print its part of the logfile
  public String getCabrilloExchange();
  public String getGUIExchange();

  // to support contests like ARRL DX where the exchange is asymmetric.
  // For symmetric contests, should just call getCabrilloExchange
  public String getSentCabrilloExchange(); 

  // methods to get different parts of the exchange

  // so next serial can be updated from the last exchange in the actual log,
  // in cases where it really counts...
  public int getSerial(); 
  public void setSerial(int serial);

  // do we have all the elements?
  public boolean isComplete();

  public String getCallsign();
  public String getContinent();
  public String getMultiplierField(); //the part of the exchange used to compute
                                      //multipliers
  public String getRoverField();      // the part used to compute equality of
                                      // rovers; normally equals getMF()

  // now it's unclear how we handle the signal report.  Maybe we *do*
  // need a separate text field for the report...
  public void setRST( String s );

  public void setMode( String s); // not sure this is the right way to handle...

  public CountryRecord getCountryRecord();
  public String getCountry(); 
  public String getZone();
  public String getCanonicalPrefix();

}