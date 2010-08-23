// Base class for scorers with a single multiplier checker. 
// (scorers with 2 mult checkers, so far, have to be treated separately)
// REQUIRES the property multiplier.1 (used to dynamically instantiate the mult checker)
// A number of methods customize the scorer's behavior:
//   isWorkable      tells you if a Q is legit in this contest. 
//   getMultName     says what to put in the "multiplier" field of the main display
//   encodeMult      gives the character for the multchar field of a log entry.
//                   (which is over-used...)
//   getNewMultTotal returns the total number of multipliers in the contest.
//                   Can be set to 1 when there's no multiplier...
//   findPoints      tells you the number of QSO points for the current Q (which might vary)
//                   This implementation defaults to 1/1/1 (PH/CW/RY), but can either
//                   be set in the contest properties (pointsPerPHQSO/pointsPerCWQSO)
//                   or overridden entirely for a more complex algorithm.
//   findScore       computes the total score; normally pts*mults, but can be overridden
//                   for contests with bonuses [MOVED INTO ABSTRACTSCORER]
//   getMultTotalText  returns the text for the message that reports the total mults.
//                   Can be customized to report bonuses (like QTC).
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;
import com.loukides.jl.checkers.*;
import com.loukides.jl.gadgets.*;

import java.util.*;
import java.io.*;

public class GenericScorer extends AbstractScorer {
  protected boolean updateVisual;
  protected Checker dupe = new DupeChecker();
  protected Checker mult; 
  protected SummaryDisplay sd = new SummaryDisplay();
  protected int ppcwq = 1;
  protected int ppryq = 1;
  protected int ppphq = 1;
  protected boolean perbandmult = false;
 
  public void setProperties(Properties p) { 
    this.p = p; 
    // Normally, the checker is given in the properties; we instantiate and run.
    String multtype = "com.loukides.jl.checkers." + 
                       p.getProperty("multiplier.1", "NullChecker");
    try { 
      mult = (Checker)(Class.forName(multtype).newInstance());
    } catch (Exception e) {
      System.out.println(e);
      System.out.println("No checker; using null");
      mult = new NullChecker();
    }
    dupe.setProperties(p);
    mult.setProperties(p);
    sd.setProperties(p);
    ppcwq = Integer.parseInt(p.getProperty("pointsPerCWQSO", "1"));
    ppryq = Integer.parseInt(p.getProperty("pointsPerRYQSO", "1"));
    ppphq = Integer.parseInt(p.getProperty("pointsPerPHQSO", "1"));
    if ( p.getProperty("perBandMultiplier", "false").equals("true")) perbandmult = true;
  }

  public void setUpdateVisual(boolean b) { 
    updateVisual = b; 
    mult.setUpdateVisual(b);
    dupe.setUpdateVisual(b);
    sd.setUpdateVisual(b);
  }

  public QSOInfo check( LogEntry le ) {
    if (updateVisual && perbandmult) sd.showNeededBands(mult.getNeeded(le)); 
    QSOInfo q = new QSOInfo();
    boolean newqso = dupe.isNew(le);
    if ( ! newqso ) {
      q.dupelabeltext = "  dupe";
      q.countlabeltext = "  QSOP: " + Integer.toString(pts);
      q.qsopoints = 0;
      q.isdupe = true;
      if ( mult.isValidMult(le) ) // WHY IS THIS HERE? We know it's a dupe; 
        // not sure that getMF is what we want if there's a real county file
        q.multlabeltext = "  " + getMultName(le);
      q.otherlabeltext = "  Score: " + findScore();
      return q;
    }
    if ( ! isWorkable(le))             q.multlabeltext = " Out-of-bounds Station";
    else if ( ! mult.isValidMult(le))  q.multlabeltext = "  Invalid: " + getMultName(le);
    else if ( mult.isNew(le) )         q.multlabeltext = "  New mult: " + getMultName(le);
    else                               q.multlabeltext = "  " + getMultName(le);

    q.dupelabeltext = "";
    q.countlabeltext = "  QSOP: " + Integer.toString(pts);
    q.qsopoints = 0;
    q.isdupe = false;
    q.otherlabeltext = "  Score: " + findScore();
      
    return q;

  }

  // some notes about the (il)logic.
  // PRE-first:  if a null is passed in, just generate score status info.
  // (I'm getting punchy.  This is an awful way to do this.  But it keeps
  // the interface simple).  Now the real stuff...
  // FIRST deals with the possibility of a dupe (and doesn't touch mults)
  // the mult checker knows to mark dupes as 'no mult'.
  // SECOND deals with the possibility of an incomplete (and marks
  // as 'no mult'; the mult checker *HAS* to mark mults before it knows
  // whether or not the exchange is complete)
  // FINALLY adds to the mult checker, ONLY IF the exchange is complete
  // and not a dupe.  This avoids mucking up the mult checker with bad
  // contacts.  But it does *not* give the mult checker a chance to
  // correct status if the contact is incomplete.
  // BOTTOM LINE-- there's probably a lot of extra checking, but I think
  // that, at least, the outcome is always right.
  public QSOInfo score( LogEntry le ) {
    QSOInfo q = new QSOInfo();
    if (le == null) {
      q.dupelabeltext = "Welcome back!";
      q.countlabeltext = "  QSOP: " + Integer.toString(pts);
      q.multlabeltext =  getMultTotalText();
      q.otherlabeltext = "  Score: " + findScore();
      return q;
    }
    dupe.addEntry(le);
    boolean newqso = ! le.getDupe();
    if ( ! newqso ) {
      le.setQsoPoints(0);
      q.dupelabeltext = "";
      q.countlabeltext = "  QSOP: " + Integer.toString(pts);
      q.qsopoints = 0;
      q.isdupe = true;
      q.multlabeltext = getMultTotalText();
      q.otherlabeltext = "  Score: " + findScore();
      return q;
    }
    else if ( ! isWorkable(le) || ! le.isComplete() ) {
      le.setQsoPoints(0);
      le.setMultChar( ' ' ); // repugnant to be touching this here...
      q.dupelabeltext="  Bad QSO (logged)";
      q.countlabeltext = "  QSOP: " + Integer.toString(pts);
      q.qsopoints = 0;
      q.isdupe = false;
      q.multlabeltext = getMultTotalText();
      q.otherlabeltext = "  Score: " + findScore();
      return q;
    }
    mult.addEntry(le); // addEntry() only adds if valid
    if (updateVisual && perbandmult) sd.showNeededBands(mult.getNeeded(le)); 
    // for the moment, we'll grant points if invalid; probably just a typo
    // (does ARRL recognize mistyped section abbreviations?)
    int points = findPoints(le);
    pts += points;
    le.setQsoPoints(points);

    // MUCH BETTER way to encode mults;
    char multchar = encodeMult(le);
    le.setMultChar(multchar);

    //if ( multchar == 'm' ) mults++;
    // Can't just count, because some contests have odd behavior (like WAE)
    mults = getNewMultTotal(le); 
    // ASSUMPTION:  multchar is ALWAYS _ or ' ' for non-mults.    
    // it is (I think) true for everything that subclasses GenericScorer
    sd.contact(le.getBand(), le.getMode(), ! (multchar == '_' || multchar == ' '));

    q.dupelabeltext = "";
    q.countlabeltext = "  QSOP: " + Integer.toString(pts);
    q.qsopoints = points;
    q.isdupe = false;
    q.multlabeltext = getMultTotalText();
    q.otherlabeltext = "  Score: " + findScore();

    return q;
  }

  // THE NEXT FOUR METHODS CUSTOMIZE THE BEHAVIOR OF THE score() and check() METHODS
  /* find points per QSO, based on mode; the generic implementation 
     reads two properties. default values are 1 for CW, RYTTY, and Phone */
  protected int findPoints(LogEntry le) { 
    if (le.getMode().equals("CW")) return ppcwq; 
    if (le.getMode().equals("RY")) return ppryq; 
    if (le.getMode().equals("PH")) return ppphq;
    else return ppcwq;
 }

  protected int getNewMultTotal(LogEntry le) {
    // best way to get the total is to get the count from the mult checker.
    // But doesn't work for some tests, like WAE
    // (LogEntry needed as arg because in WAE, and maybe others, mults depend on band...)
    return mult.getTotal();
  }

  protected char encodeMult(LogEntry le) {
    boolean s = le.isMultiplier();
    if ( s ) return 'm';
      else return '_';
  }

  protected String getMultName(LogEntry le) {
    return le.getName();
  }

  protected String getMultTotalText() {
    return "  Total multipliers: " + mults;
  }

  public void clear() {
    pts = 0;
    mults = 0;
    dupe.clear();
    mult.clear();
    sd.clear();
  }

  public void report() {
    FileOutputStream fos = null;
    String filename = "logs/" + (String)p.get("logfilebasename") + "-mult.txt";
    try {
      fos = new FileOutputStream( filename ); 
    } catch (IOException e) { System.out.println( e ); System.exit(1); }
    PrintWriter report = new PrintWriter( fos );
    mult.writeReport(report);
    report.close();
  }

  // start of a new feature: "qualify" the station worked
  protected boolean isWorkable(LogEntry le) { return true; } 

  // NO, don't put this here, put it in the subclass (so they can control use of the display)
  // public java.awt.Component getSummaryDisplay() { return sd; }

}