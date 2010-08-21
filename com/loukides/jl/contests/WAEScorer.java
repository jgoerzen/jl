package com.loukides.jl.contests;

import java.util.*; 
import java.text.*;
import java.io.PrintWriter;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

public class WAEScorer extends GenericScorer {

  private int qtcpoints = 0;
  private Vector log = null;
  private static final long HOUR = 1000 * 60 * 60;
  private static TimeInterval longest = null, next = null, least = null;

  protected int getNewMultTotal(LogEntry le) {
    // Weird WAE mult rules
    String b = le.getBand();
    if ( ! le.isCountryMultiplier()) return mults; 
    else if (b.equals("b20") || b.equals("b15") || b.equals("b10") ) return mults +=2;
    else if (b.equals("b40")) return mults += 3;
    else if (b.equals("b80")) return mults += 4;
    else return mults;
  }

  protected char encodeMult(LogEntry le) {
    boolean s = le.isCountryMultiplier();
    if ( s ) return 'm';
    else return '_';
  }

  // start of a new feature: "qualify" the station worked
  protected boolean isWorkable(LogEntry le) { 
    if (le.getRcvd().getContinent().equalsIgnoreCase("EU")) return true;
    return false;
  } 

  protected String getMultName(LogEntry le) {
    return le.getRcvd().getCountry();
  }

  protected int findScore() { 
    // System.out.println("findpts: " + pts + " " + qtcpoints + " " + mults);
    return (pts + qtcpoints) * mults; 
  }

  protected String getMultTotalText() {
    return "  Mults: " + mults + " QTC: " + qtcpoints;
  }

  public void setQTCPoints(int qtcp) { 
    qtcpoints = qtcp; 
    // System.out.println("Setqtcp: " + qtcpoints); 
  }

  public void setLog(Vector log) { this.log = log; }

  // This routine adds WAE offtimes to the header, per DARC requirement.
  // I think it is fairly fragile.  It assumes the log is linear, and can probably
  // freak out if it finds something untoward in the log.  How it works in the presence
  // of QTC interspersed with log entries is still unclear.  The first OFF minute IS
  // the minute of the last QSO of the period--unlike ARRL. I think this interpretation
  // of the rules is probably incorrect.
  protected void doOfftime(PrintWriter w) {
    int i;
    Date startTime = null, 
         endTime = null,
         lastTime = null, 
         firstQSOTime = null, 
         thisTime = null;

    longest = null; next = null; least = null;
    for ( i = 0; i < log.size(); i++) {
      Loggable lg = (Loggable)log.elementAt(i);
      if (startTime == null && lg instanceof LogEntry ) {
        firstQSOTime = lg.getDate();     // first QSO (QTC doesn't count) in log
        String t = U.timef.format(firstQSOTime).substring(0,10); // round down to get 
        try {                                                  // start of contest period
          startTime = U.dayf.parse(t);
          // System.out.println(startTime);
        } catch (ParseException e) { System.out.println(e); }
        endTime = new Date(startTime.getTime() + 48*HOUR-1);  // compute end of contest
        sortTimes(                                            // from beginning to 1st Q
          new TimeInterval(startTime, firstQSOTime, true, false)); 
        lastTime = firstQSOTime;
      }
      if (startTime == null) continue; // haven't found first QSO yet
      thisTime = ((Loggable)log.elementAt(i)).getDate();
      sortTimes(new TimeInterval(lastTime, thisTime));
      lastTime = thisTime;
    } 
    sortTimes(new TimeInterval(lastTime, endTime, false, true));
    printOfftime(w, longest);
    printOfftime(w, next);
    printOfftime(w, least);
  }

  // This prints ARRL-style time periods; if a Q is in one minute, the offtime begins
  // with the next minute.
  private void printOfftime(PrintWriter w, TimeInterval ti) {
    w.print("OFFTIME: ");
    if (! ti.isPeriodStart()) w.print(U.timef.format(
      new Date(ti.getStart().getTime() + U.MINUTE))); // push time into the next minute
    else w.print(U.timef.format(ti.getStart()));
    w.print(" ");
    if (! ti.isPeriodEnd()) w.print(U.timef.format(
      new Date(ti.getEnd().getTime() - U.MINUTE))); // pull time into the previous minute
    else w.print(U.timef.format(ti.getEnd()));
    w.println();
  }

  private void sortTimes(TimeInterval diff) {
    // System.out.println("sorttime: " + diff);
    if ( longest == null ) 
      longest = diff; 
    else if ( diff.getDifference() >= longest.getDifference() ) {
      least = next;
      next = longest;
      longest = diff;
    }
    else if ( next == null ) next = diff;  // really shouldn't happen...
    else if ( diff.getDifference() >= next.getDifference() ) {
      least = next; 
      next = diff;
    }
    else if ( least == null ) least = diff; // really shouldn't happen ...
    else if ( diff.getDifference() >= least.getDifference() ) 
      least = diff;
  }

  protected void doCategory(PrintWriter w) {
    super.doCategory(w);
    if (p.getProperty("category", "").equals("SINGLE-OP-ASSISTED"))
      w.println("CATEGORY-ASSISTED: ASSISTED");
    else if (p.getProperty("category", "").equals("SINGLE-OP"))
      w.println("CATEGORY-ASSISTED: UNASSISTED");
  }

  public java.awt.Component getSummaryDisplay() { return sd; }

}