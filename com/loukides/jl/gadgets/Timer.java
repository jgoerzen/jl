/* elapsed/remaining timer and qso rate meter
 * THIS VERSION IS BASED ON COUNTING MINUTES and should count time as the ARRL does.
 * NOT SURE that synchronization is really right, but I think it's close
 * (the timer can be set either by the elapsed time kicker, or by 
 * logging a QSO.  These may not always interact correctly)
 *  *** potential synch problem with reloading the log--think it's tamed***
 * When thinking about this, it helps to remember a couple of rules:
 *    :  A QSO always terminates an OFF period if one is in progress
 *    :  A timer kick NEVER terminates an OFF period, and doesn't
 *    :  A timer kick doesn't really add time in and of itself
 *    :  A recalc never changes the state of the off period
 */
package com.loukides.jl.gadgets;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;

public class Timer extends JPanel {
  private final static long SECOND = 1000;
  private final static long MINUTE = 60 * SECOND;
  private final static long HOUR = 60 * MINUTE;
  private final static long DAY = 24 * HOUR;
  private final static int MINUTESPERHOUR = 60;
  private static Dimension size;

  private final static int RATEBASE = 10;

  private Properties p;
  private Vector log;

  private int offTime;
  private int maxTime;
  private boolean trackTime;
  private boolean inOffTime;

  private JLabel elapsed = new JLabel();
  private JLabel remaining = new JLabel();
  private JButton ratemeter = new JButton();
  private Color normalcolor = elapsed.getForeground();
  private Color labelcolor = elapsed.getForeground();

  private int totalMinutes = 0;  // total accumulated time
  private long lastLoggedMinute;   // time from which new increments are added

  private boolean ratemeteron = false;
  private DecimalFormat rateformat = new DecimalFormat("##0"); 
  private int qsobase = 0;

  public Timer ( Vector log, Properties p ) {
    this.log = log;
    this.p = p;

    int width = Integer.parseInt(p.getProperty("layout.Timer.width", "100"));
    int height = Integer.parseInt(p.getProperty("layout.Timer.height", "25"));
    size = new Dimension(width, height);
    // offTime defaults to 30 (more for personal use in contests without a 
    // off periods; should always be specified in contests with an off period
    offTime = Integer.parseInt(p.getProperty("offTime", "30"));
    // zero indicates no time limit specified
    String max = p.getProperty("maxTime", "0h");
    max = max.substring(0, max.length() -1); // trim trailing 'h'
    maxTime = Integer.parseInt(max) * MINUTESPERHOUR;
    trackTime = p.getProperty("trackTime", "false").equals("true");

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    JLabel title = new JLabel("Timer");
    title.setForeground(Color.black);
    U.setSizes(title, size);
    U.setSizes(elapsed, size);
    U.setSizes(remaining, size);
    U.setSizes(ratemeter, size);
    add(title);
    add(elapsed);
    add(remaining);
    add(ratemeter); 
    add(Box.createVerticalGlue());

    // Dimension size = new Dimension(190, 17);
    // elapsed.setPreferredSize(size);
    elapsed.setHorizontalAlignment(SwingConstants.CENTER);
    // remaining.setPreferredSize(size);
    remaining.setHorizontalAlignment(SwingConstants.CENTER);

    resetTimer();
    // note the need to disambiguate this class and java.util.Timer
    Date nextMinute = new Date(roundUpToMinute(new Date().getTime()));
    java.util.TimerTask kickingtask = new java.util.TimerTask() { 
      public void run () {
        // System.out.println("Ding " );
        kickTimer( new Date().getTime() );
      }
    };
    java.util.Timer kicker = new java.util.Timer();
    kicker.schedule( kickingtask, nextMinute, MINUTE);

    ratemeter.setForeground(normalcolor);
    ratemeter.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        ratemeteron = !ratemeteron;
        // System.out.println("Rate meter: " + ratemeteron);
        resetTimer();
        setRateMeter(new Date().getTime());
      }
    });
    setRateMeter(new Date().getTime()); 
  }

  // called on startup or when rebuilding the log (recalc dupes)
  // sets the instance variable totalTime
  // synchronized to prevent timer ticks from screwing things 
  public synchronized void resetTimer() {
    Loggable le;
    long currentTime = 0;
    long timedelta;

    lastLoggedMinute = 0;
    totalMinutes = 0;
    for ( int i = 0; i < log.size(); i++) {
      currentTime = roundDownToMinute(((Loggable)log.elementAt(i)).getDate().getTime());
      if ( i == 0 ) {      // initial Q: no previous Q to compute an interval
        totalMinutes = 1;  // BUT we've been active during the current minute
      } else {
        timedelta = (currentTime - lastLoggedMinute)/MINUTE; // minutes since last q
        if ( timedelta < offTime ) {    // difference shorter than the off period
          totalMinutes += timedelta;    // so add the minutes
        } else {                        // but if the different longer than off period
          qsobase = i;                  // mark the entry (for use by ratemeter)
          totalMinutes++;               // first q of a new period-no base, but
        }                               // need to credit for a minute
      }  
      lastLoggedMinute = currentTime;   // set up to compute the next interval
      // System.out.println( "recalc: " + lastLoggedMinute + " "+ totalMinutes);
    }
    long now = roundDownToMinute(new Date().getTime()); // now set us up to start
    timedelta = (now - lastLoggedMinute)/MINUTE;        // inactive, if appropriate
    if (timedelta < offTime) inOffTime = false;
    else inOffTime = true;
    setHMS( totalMinutes );              // eventually--not needed because a call to
//    kickTimer( new Date().getTime() ); // kickTimer will be better for starting off
  }

  // writes the time(s) on the label.  Synchronized to avoid a race between
  // timer kicks and contacts. 
  private synchronized void setHMS(int minutes){
   int t = minutes;
   if ( t > maxTime && trackTime ) {
      t = maxTime;
      labelcolor = Color.red;
    } 
    elapsed.setForeground(labelcolor);
    int hours = t / MINUTESPERHOUR;
    int displayminutes = t - hours* MINUTESPERHOUR;
    String hack = (displayminutes < 10) ? "0":"";
    elapsed.setText("Total: " + hours + ":" + hack + displayminutes ); 
    // System.out.println("Total: " + hours + ":" + displayminutes);
    if (trackTime) {
       int remainingTime = maxTime - t;
       hours = remainingTime / MINUTESPERHOUR;
       displayminutes = remainingTime - hours* MINUTESPERHOUR;
       hack = (displayminutes < 10) ? "0":""; // quick way to get the right no. of zeros
       remaining.setText("Left: " + hours + ":" + hack + displayminutes ); 
    }
  }


  // called when a contact is made to register the time
  public void contactMade( Date d) {
    labelcolor = normalcolor;
    long now = d.getTime();
    long roundedNow = roundDownToMinute(now);
    long timedelta = (roundedNow - lastLoggedMinute)/MINUTE;
    // System.out.println("Timer kicked by Q    : " + d + " " 
    //                     + roundedNow + " " + timedelta);
    // note that the second clause below isn't clearly necessary.  Right now, 
    // without the timer working, there's no way to get put into an off time.
    // So we have to detect it when processing an entry.  But it might be able to
    // go when we're processing regular clock kicks.
    if ( inOffTime || timedelta > offTime ) { // a qso always exits an idle period
      totalMinutes++;  // count one minute (no effective "previous qso")
    } else {
      totalMinutes += timedelta;
    }
    lastLoggedMinute = roundedNow;
    inOffTime = false;
    setHMS(totalMinutes);
    setRateMeter(now);
  }

  // called from a thread to update the time during periods of inactivity
  // Only adds time to the timer; the additional time is held in addedMinutes.
  // If we enter an idle period, the added time is cleared, so we get back any
  // minutes since the last QSO.  The rate meter is also kicked from here.
  // New version should simply work in whole minutes...
  public void kickTimer( long now ) {
    int addedMinutes;
    long roundedNow = roundDownToMinute(now);
    int timedelta = (int)(( roundedNow - lastLoggedMinute)/MINUTE);
    // System.out.println("Timer kicked: " + now + " " + " " 
    //                                     + roundedNow + " " + timedelta);
    if (timedelta > offTime) {  // if we enter an off period, we get the time
      labelcolor = Color.green; // since the last QSO back 
      timedelta = 0; 
      inOffTime = true;
    } 
    setHMS(totalMinutes + timedelta);
    setRateMeter(now);
  }

  private void setRateMeter(long lasttime) {
    if (ratemeteron) {
      if ( inOffTime ) {
        // compute relative to the last contact in the log.
        // (the next time we get a log entry, qsobase will point to the 
        // newest entry)
        qsobase = log.size();
        ratemeter.setText("Off period");
        return;
      }
      int nqsos = RATEBASE;
      int first = log.size() - RATEBASE;
      if ( first < qsobase ) {
        first = qsobase;
        nqsos = log.size() - qsobase;
      }
      if ( first >= log.size() -1) { 
        // System.out.println("no rate: " + first + " " 
        //                  + log.size() + " " + qsobase);
        // startup, plus some deleted element cases
        // (prevents flameout if you delete Qs from the end of the log)
        ratemeter.setText("No rate");
        return;
      }
      long firsttime = ((Loggable)log.elementAt(first)).getDate().getTime();
      long interval = lasttime - firsttime;
      if (interval == 0) {           // can't compute a rate on a single Q
        ratemeter.setText("No rate");
        return;
      }
      double hours = ((double) interval)/HOUR;
      double rate = nqsos/hours;
      // System.out.println("ratemeter: " + qsobase + " " + first + " " + nqsos
      //  +" "+((LogEntry)log.elementAt(first)).getRcvd().getCallsign() 
      //  +" "+((LogEntry)log.elementAt(log.size()-1)).getRcvd().getCallsign() );
      // System.out.println("ratemeter: " + interval + " " + rate + " " + hours);
      ratemeter.setText("Rate: " + rateformat.format(rate) + "/hr");
    } else ratemeter.setText("Meter off");
  }

  private long roundDownToMinute(long t) {
    long rdn = MINUTE * ( t/MINUTE);
    // System.out.println("Round down: " + t + " " + rdn );
    return rdn;
  }

  private long roundUpToMinute(long t) { 
    return MINUTE + roundDownToMinute(t);
  }

}