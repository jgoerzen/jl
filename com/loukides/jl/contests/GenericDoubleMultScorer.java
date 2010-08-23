// A scorer that can use two multiplier checkers.
// Checkers specified by multiplier.1 and multiplier.2
// Other properties as specific to the checkers.
// If you have two checkers of the same type, you're in trouble...
// Supports *all* the subclass customization methods of GenericScorer, plus a required
// (and in this class, abstract) method, useMultChecker1() (which will normally tell you if 
// a LogEntry should be checked against a CountryChecker or a ListedStringChecker).
// ORDER IS THEREFORE IMPORTANT!  By convention, I put a CountryChecker before a 
// ListedStringChecker.
// ABSTRACT because there's no meaningful way to define useMultChecker1() without
// a concrete contest.
// This adds a couple of methods for customization:
//    useMultChecker1:   whether to use the first mult checker
//    useMultChecker2:   whether to use the second
//    showNeeded:        colorize the multiplier/qso display
//    incrementMultAndQSOCounter: 
//                       increment values in the multiplier/qso display
package com.loukides.jl.contests;

import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;
import com.loukides.jl.checkers.*;
import com.loukides.jl.gadgets.*;

import java.util.*;
import java.io.*;

public abstract class GenericDoubleMultScorer extends GenericScorer {
  protected Checker mult1, mult2;

  public void setProperties(Properties p) { 
    this.p = p; 
    // Normally, the checker is given in the properties; we instantiate and run.
    String mult1type = "com.loukides.jl.checkers." + 
                       p.getProperty("multiplier.1", "NullChecker");
    String mult2type = "com.loukides.jl.checkers." + 
                       p.getProperty("multiplier.2", "NullChecker");
    try { 
      mult1 = (Checker)(Class.forName(mult1type).newInstance());
      mult2 = (Checker)(Class.forName(mult2type).newInstance());
    } catch (Exception e) {
      System.out.println(e);
      System.out.println("No checker; using null");
      mult1 = new NullChecker();
      mult2 = new NullChecker();
    }
    dupe.setProperties(p);
    mult1.setProperties(p);
    mult2.setProperties(p);
    sd.setProperties(p);
    ppcwq = Integer.parseInt(p.getProperty("pointsPerCWQSO", "1"));
    ppryq = Integer.parseInt(p.getProperty("pointsPerRYQSO", "1"));
    ppphq = Integer.parseInt(p.getProperty("pointsPerPHQSO", "1"));
    if ( p.getProperty("perBandMultiplier", "false").equals("true")) perbandmult = true;
  }

  protected abstract boolean useMultChecker1(LogEntry le);

  protected boolean useMultChecker2(LogEntry le) { return ! useMultChecker1(le); }

  public QSOInfo check( LogEntry le ) {
    showNeeded(le);
    QSOInfo q = new QSOInfo();
    boolean newqso = dupe.isNew(le);
    if ( ! newqso ) {
      q.dupelabeltext = "  dupe";
      q.countlabeltext = "  QSOP: " + Integer.toString(pts);
      q.qsopoints = 0;
      q.isdupe = true;
      q.multlabeltext = le.getName();
      q.otherlabeltext = "  Score: " + pts*mults;
      return q;
    }
    if ( ! isWorkable(le) )                  q.multlabeltext = "  Out-of-bounds Station";
    else {  // Nonsense required to use an HTML in the multlabel.  (Needed for CQWW)
      if ( useMultChecker1(le) ) {
        if ( mult1.isNew(le) )              q.multlabeltext = 
          "<html>  New: " + getMultName(le) + "</html>";
        else if ( ! mult1.isValidMult(le) )      q.multlabeltext = "  Invalid mult";
        else                                     q.multlabeltext = 
          "<html>  " + getMultName(le) + "</html>";
      }
      if (  useMultChecker2(le) ) {
        if ( mult2.isNew(le) )                 q.multlabeltext = 
          "<html>  New: " + getMultName(le) + "</html>";
        else if ( ! mult2.isValidMult(le) )    q.multlabeltext = "  Invalid mult";
        else                                   q.multlabeltext = 
          "<html>  " + getMultName(le) + "</html>";
      }
    }
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
    if ( useMultChecker1(le) ) 
       mult1.addEntry(le); // addEntry() only adds if valid
    if ( useMultChecker2(le) ) 
       mult2.addEntry(le);    // addEntry() only adds if valid
    // for the moment, we'll grant points if invalid; probably just a typo
    // (does ARRL recognize mistyped section abbreviations?)
    showNeeded(le);
    int points = findPoints(le);
    pts += points;
    le.setQsoPoints(points);

    char multchar = encodeMult(le);
    le.setMultChar(multchar);

    mults = getNewMultTotal(le);
    incrementMultAndQSOCounter(le);

    q.countlabeltext = "  QSOP: " + Integer.toString(pts);
    q.qsopoints = le.getQsoPoints();
    q.isdupe = false;
    q.multlabeltext = getMultTotalText();
    q.otherlabeltext = "  Score: " + findScore();

    return q;
  } 

  public void setUpdateVisual(boolean b) { 
    updateVisual = b; 
    mult1.setUpdateVisual(b);
    mult2.setUpdateVisual(b);
    dupe.setUpdateVisual(b);
    sd.setUpdateVisual(b);
  }

  public void clear() {
    pts = 0;
    mults = 0;
    dupe.clear();
    mult1.clear();
    mult2.clear();
    sd.clear();
  }

  public void report() {
    FileOutputStream fos = null;
    String filename = "logs/" + (String)p.get("logfilebasename") + "-mult.txt";
    try {
      fos = new FileOutputStream( filename ); 
    } catch (IOException e) { System.out.println( e ); System.exit(1); }
    PrintWriter report = new PrintWriter( fos );
    mult1.writeReport(report);
    mult2.writeReport(report);
    report.close();
  }

  protected int getNewMultTotal(LogEntry le) {
    return mult1.getTotal() + mult2.getTotal();
  }

  protected void showNeeded(LogEntry le) {
    if (updateVisual && perbandmult) {
      if (useMultChecker1(le)) sd.showNeededBands(mult1.getNeeded(le));
      else if (useMultChecker2(le)) sd.showNeededBands(mult2.getNeeded(le));
    }
  }

  protected void incrementMultAndQSOCounter(LogEntry le) {
    sd.contact(le.getBand(), le.getMode(), 
               ! (le.getMultChar() == '_' || le.getMultChar() == ' '));
  }

}
