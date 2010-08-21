// A QTC entry; used only by WAG.  May regret this, but I'm not
// structuring this as highly as a LogEntry 
// NOTE:  ALMOST ALL OF THE QTC-specific info could be gotten from the le, which we're
// dragging around anyway....
package com.loukides.jl.qtc;

import java.util.*;
import java.text.SimpleDateFormat;
import com.loukides.jl.util.U;
import com.loukides.jl.jl.LogEntry;
import com.loukides.jl.jl.AbstractLoggable;

public class QTCEntry extends AbstractLoggable {

  private int qtcGroup = 0;  // serial of the QTC group
  private int qtcsTotal = 0; // serial of this QTC within the group
  private String qtcGroupString = null;
  private String qtcsTotalString = null;
  private String sentTo = null;   // the station receiving this QTC
  private String sentBy = null;   // the station receiving this QTC
  private String qtcAbout = null; // The station in the QSO reported by this QTC
  private String qsoTime = null;   // time of the reported QSO
  private int qsoSerial = 0; // serial number of the reported QSO
  private boolean completed = false; 
  private LogEntry reported = null;

  public QTCEntry(int qtcGroup, String sentTo, LogEntry reported, String band, String mode) {
    super();
    this.reported = reported;
    setGroupNumber(qtcGroup);
    this.sentTo = sentTo;
    this.sentBy = reported.getSent().getCallsign();
    this.qtcAbout = reported.getRcvd().getCallsign();
    band = reported.getBand();
    qsoTime = minutesformat.format(reported.getDate());
    qsoSerial = reported.getRcvd().getSerial();
    setBand(band);
    setMode(mode);
  }

  public QTCEntry(String logline) {
    super();
    parseCabrilloString(logline);
  }

  public int getGroupNumber() { return qtcGroup; }    // needed?
  public void setGroupNumber(int n) { 
    qtcGroup= n; 
    qtcGroupString = U.zpad3.format(qtcGroup);
  }  // needed?
  public String getGroupNumberString() { return qtcGroupString; }

  public int getGroupTotal() { return qtcsTotal; }    // needed?
  public void setGroupTotal(int n) { 
    qtcsTotal= n; 
    qtcsTotalString = U.zpad2.format(qtcsTotal);
  }  // needed?
  public String getGroupTotalString() { return qtcsTotalString; }

  public void setCompleted(boolean b) { 
    completed = b; 
    if (reported == null) {
      // find the corresponding log entry...
    }
    else reported.setQTC(b); 
  }
  public boolean isCompleted() { return completed; }

  public String getQSOTime() {return qsoTime; }
  public String getAboutCallsign() { return qtcAbout; }
  public String getQSOSerial() { return U.zpad.format(qsoSerial); }
  public String getRecipient() { return sentTo; }

  public void setReported(LogEntry reported) { this.reported = reported; }

  public String toCabrilloString(){  
    // if ( date == null ) setDate((Date)null); // HACK FOR TESTING (without a date assigned)
    String khz = U.toFrequency(band);
    String serialstring = qtcGroupString + "/" + qtcsTotalString;
    String cabrillo =  
                   "QTC: " + khz     + " " 
                 + mode + " " 
                 + timeToGMT(date) + " "
                 + sentTo + U.findPad(sentTo, 13) + " "
                 + serialstring + U.findPad(serialstring, 10) + " "
                 + sentBy + U.findPad(sentBy, 13) + " "
                 + qsoTime + " "
                 + qtcAbout + U.findPad(qtcAbout, 13) + " "
                 + U.zpad.format(qsoSerial);
    return cabrillo;
  }

  public String getSentTo() { return sentTo; }

  public void parseCabrilloString(String logline){
    // b1 is the end of the "general" part of the Cab. entry, which is the same for 
    // QTC and non-QTC entries
    String generalstr = logline.substring(0, b1).trim();
    String qtcdata = logline.substring(b1).trim();
    parseCommonFields(generalstr); 
    StringTokenizer tok = new StringTokenizer(qtcdata);
    sentTo = tok.nextToken();
    StringTokenizer serialstuff = new StringTokenizer(tok.nextToken(), "/");
    setGroupNumber(Integer.parseInt(serialstuff.nextToken()));
    setGroupTotal(Integer.parseInt(serialstuff.nextToken()));
    if ( 1 > qtcsTotal || 10 < qtcsTotal ) { }; // SOMETHING WRONG
    sentBy = tok.nextToken();
    qsoTime = tok.nextToken();
    qtcAbout = tok.nextToken();
    qsoSerial = Integer.parseInt(tok.nextToken());
    setCompleted(isComplete()); // MUST be a good QTC, because we found it in a logfile
  }

  public boolean isComplete(){
    return qtcGroup != 0
        && qtcsTotal != 0
        && sentTo != null
        && sentBy != null
        && qtcAbout != null
        && qsoTime != null
        && qsoSerial != 0;
  }

}