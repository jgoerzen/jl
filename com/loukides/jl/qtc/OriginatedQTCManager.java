// Tracks QTCs sent from this station.  If I ever do the EU side, will need a 
// ReceivedQTCManager
// ISSUES:
// When reading a pre-existing log and creating QTCs, the log entries that have been given
// as QTC must be marked.  
//
// When finding another QTC group, because the user has changed the callsign, do we need
// to reset firstavailable?  Yes, because the whole previous QTC series hasn't been given.
// Is there anyway not to start from the beginning?  
//
// Need to keep track of how many QTCs have been given to any particular station.
// This should take place in QTCDone.
package com.loukides.jl.qtc;

import java.util.*;
import com.loukides.jl.contests.WAEScorer; 
import com.loukides.jl.jl.*;
import com.loukides.jl.util.U;
import com.loukides.jl.keyer.Keyer;

public class OriginatedQTCManager {
  private Vector log = null;
  private WAEScorer sc = null;
  private QTCDisplay qtcd = null;
  private int series = 1; // QTC series number
  private int firstavailable = 0;
  private int lastfirstavailable = 0;
  private int totalqtcs = 0;
  private HashMap qtcssentto = new HashMap(1000);
  private Logger logger = null;
  private String currentband = "";
  private String currentmode = "";
  private ArrayList leftoverqtc = new ArrayList();
  private Canceller cancel = null;
  private Keyer keyer = null;

  public OriginatedQTCManager(Vector log, WAEScorer sc, Logger logger) {
    // System.out.println("QTC manager created");
    this.log = log;
    this.sc = (WAEScorer)sc;
    this.qtcd = new QTCDisplay(this); 
    this.logger = logger;
  }

  public void setCanceller(Canceller cancel) { this.cancel = cancel; }
  public void setKeyer(Keyer keyer) { this.keyer = keyer; }

  // return an array of QTC information that DOES NOT include contacts with the given 
  // callsign.  This QTC array may be displayed and to a recipient
  public ArrayList getQTCArray(String callsign) {
    int i = firstavailable; 
    lastfirstavailable = firstavailable;
    boolean oktoresetfirstavailable = true;
    int qtcswanted = qtcsWantedBy(callsign);
    ArrayList qtclist = new ArrayList(10); // 10 is the max. QTC that can be given at once
    while (qtclist.size() < qtcswanted && i < log.size()) {
      Loggable l = (Loggable) log.elementAt(i);
      if ( l instanceof LogEntry ) {
        LogEntry le = (LogEntry) l;
        if ( (! hasBeenGivenAsQTC(le)) &&   // can't give the same QSO twice as QTC
             (le.getQsoPoints() != 0)) {      // eliminates dupes, incompletes, and OOBs
          if ( differentCall(le, callsign)) {
            QTCEntry q = new QTCEntry(series, callsign, le, currentband, currentmode);
            qtclist.add(q);
          } else oktoresetfirstavailable = false; // le should be given in next set
        } 
      }
      if (oktoresetfirstavailable) firstavailable++;
      i++;
    }
    for ( i = 0; i < qtclist.size(); i++ ) 
      ((QTCEntry)qtclist.get(i)).setGroupTotal(qtclist.size());
    return qtclist;
  }

  // return an array of QTC information that does not include qsos with the last station
  // in the log.  (Assuming that you work someone who says "Do you have any QTC?")
  public ArrayList getQTCArray() {
    return getQTCArray(findLastCallsign());
  }

  // NOTE:  This must mark all log entries that have successfully been given as QTC.
  // ANd must record the total number of QTCs given to the recipient.
  public void qtcDone(ArrayList qtcarray) {
    int totalgiven;
    if (qtcarray.size() > 0) {  // SHOULD be able to assume this, but let's be safe
      series++;  // increment the series number
      totalqtcs += qtcarray.size();
      sc.setQTCPoints(totalqtcs);
      // mark the qtc entries as sent, and log them
      Iterator it = qtcarray.iterator();
      while ( it.hasNext() ) {
        QTCEntry qe = (QTCEntry)it.next();
        qe.setDate(null); // AS ELSEWHERE, set time on completion, not initiation
        qe.setCompleted(true); 
        logger.addLogEntry(qe);
      }
      String recipient = ((QTCEntry)qtcarray.get(0)).getSentTo();
      Integer givensofar = (Integer)qtcssentto.get(recipient);
      if (givensofar == null) 
        totalgiven = qtcarray.size();
      else 
        totalgiven = givensofar.intValue() + qtcarray.size();
      qtcssentto.put(recipient, new Integer(totalgiven));
    }
    cancel.cancel(); // good way to reset state and update display
  }

  // Failed QTC attempt; don't log anything, don't score anything, don't mark anything
  // I'm STILL not convinced that this won't occasionally miss entries. 
  public void qtcDone() {
    // BUT back up 
    firstavailable = lastfirstavailable;
    cancel.cancel(); // good way to reset state and update display
  }

  public void showQTC(String currentband, String currentmode) {
    // System.out.println("Showqtc: " + findLastCallsign());
    this.currentband = currentband;
    this.currentmode = currentmode;
    qtcd.show();
  }

  /* This method and the following one play an important role in reconstructing
   * which QSOs have been given as a QTC.  The algorithm goes like this:
   * While reading back the log, whenever it processes a log entry, the logger adds 
   * it to a hash map, with a key consisting of the callsign, serial, and time.  
   * When it finds a qtc, the logger hands the qtc manager the QTC and the hashmap.
   * The manager (a) records the qtc, and (b) tries to look up the QSO in the hashmap.
   * If it finds it, it marks it as given and REMOVES it from the hashmap (to 
   * keep the hashmap from growing huge).  If it doesn't find it, it saves it in its
   * own array list of leftover QTCs (qtcs with no corresponding log entry).  Then the fun
   * continues in doFinalCleanupup()
   */
  public void scoreQTCFromLog(QTCEntry qe, HashMap les) {
    totalqtcs++;  // We assume that, if a QTC gets in the log, it is always valid.  Is this
    sc.setQTCPoints(totalqtcs);  // a good assumption?
    series = qe.getGroupNumber() + 1; // when we're done, we want this set to the NEXT series
    String to = qe.getRecipient();
    Integer numto = (Integer)qtcssentto.get(to);
    if (numto == null)  qtcssentto.put(to, new Integer(1)); 
    else                qtcssentto.put(to, new Integer(numto.intValue()+1));
    // Now find (and mark) the corresponding log entry
    String key = qe.getAboutCallsign() + qe.getQSOSerial() + qe.getQSOTime();
    LogEntry le = (LogEntry)les.remove(key);
    if ( le != null ) le.setQTC(true);  // do we need to attach the le to the qe?
    else leftoverqtc.add(qe);
  }

  /* This is where things get insane.  When the log is processed, the logger hands us
     a hashmap containing all the log entries that HAVEN'T been matched with QTC.
     (This is the same hashmap from the previous step).  We then:
     Build a new HashMap with a DIFFERENT key (not including the callsign), being 
     careful to create an array list whenever two log entries map to the same key;
     (Maybe we should just make everything an array list; simpler...) We then iterate
     over the list of leftover QTCs, and find matching (serial and time) entries
     in the map of leftover log entries.  We mark EVERYTHING we find.  There is a small 
     risk of double-marking, but if you don't, you will CERTAINLY resend a QTC you've 
     already sent.
   */
  public void doFinalCleanup(HashMap leftoverle) {
    // System.out.println("final: " + leftoverle.size());
    // Start by "reforming" the leftover log entries
    HashMap lemap = new HashMap();
    Iterator it = leftoverle.values().iterator();
    while ( it.hasNext() ) {
      LogEntry le = (LogEntry)it.next();
      String secondarykey = U.zpad.format(le.getRcvd().getSerial()) + 
                            AbstractLoggable.minutesformat.format(le.getDate());
      Object tmp = lemap.get(secondarykey);
      if ( tmp == null ) lemap.put (secondarykey, le);
      else if ( tmp instanceof LogEntry ) {
        ArrayList a = new ArrayList(); 
        a.add(tmp); a.add(le); 
        lemap.put(secondarykey, a);
      } 
      else if ( tmp instanceof ArrayList ) ((ArrayList)tmp).add(le); 
    }
    Iterator qit = leftoverqtc.iterator(); 
    while ( qit.hasNext() ) {
      QTCEntry qe = (QTCEntry) qit.next();
      String secondaryqtckey = qe.getQSOSerial() + qe.getQSOTime();
      Object o = lemap.get(secondaryqtckey);
      if ( o instanceof LogEntry) ((LogEntry)o).setQTC(true);
      else if ( o instanceof ArrayList) {
        Iterator tmp = ((ArrayList)o).iterator(); 
        while (tmp.hasNext()) ((LogEntry)tmp.next()).setQTC(true);
      }
    }
    leftoverqtc = null; // let GC clean up 
  }

  protected void sendKeyerMessage(String msg) {
    // System.out.println(msg);
    if (keyer != null) keyer.playArbitraryMessage(msg);
  }

  private boolean differentCall(LogEntry le, String qtcrecipient) {
    return ! le.getRcvd().getCallsign().equals(qtcrecipient);
  }

  private boolean hasBeenGivenAsQTC(LogEntry le) {
    return le.isQTC();
  }

  private int qtcsWantedBy(String callsign) {
    Integer qtcsGiven = (Integer)qtcssentto.get(callsign);
    if ( qtcsGiven == null ) return 10; 
    return 10 - qtcsGiven.intValue();
  }

  private String findLastCallsign() {
    int i;
    Loggable l;
    String lastcall= null;
    for ( i = log.size()-1; i > 0 ; i--) { // iterate backward over the log to find
      l = (Loggable)log.elementAt(i);      // the last log entry (that isn't a qtc entry)
      if (l instanceof LogEntry) {
        lastcall =  ((LogEntry)l).getRcvd().getCallsign(); 
        break;
      }
    }
    // System.out.println("Lastcall: " + lastcall);
    return lastcall;
  }

}