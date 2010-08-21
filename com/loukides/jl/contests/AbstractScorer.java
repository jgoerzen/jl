package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;
import com.loukides.jl.scorereporter.ScoreReporter;

import java.util.*;
import java.io.*;
import javax.swing.JOptionPane;

public abstract class AbstractScorer {
  // Scorers contain knowledge of scoring rules (qso points, multipliers, duping)
  // and are contest-specific.  In particular, Scorer knows about things
  // like the double multiplier for CQWW.  
  // I think ideally, the Logger would contain the 
  // Scorer and would delegate to it.  The Scorer instantiates the 
  // dupesheet and other checkers.

  // The fancy genericized initialization stuff looks increasingly like dead wood.
  // Perhaps it can be salvaged, since it's certainly the right idea.

  public Properties p;
  protected int pts = 0;
  protected int mults = 0;
  protected Rescorable [] tools;
  protected boolean updateVisual;
  private ScoreReporter sr = null;

  // setProperties is *very* important; it ends up being a general initialization
  // routine for all the checkers.  It (a) sets props on any checkers the
  // scorer owns, and (b) loads files as specified by the properties, (c)
  // initializes any constants that can be taken from the properties, and (d) starts the
  // global score reporter.  IMPORTANT AS IT MAY BE, IT DOESN"T LOOK LIKE IT"S CALLED
  public void setProperties( Properties p){
    // System.out.println("Properties set");
    this.p = p;
    for (int i = 0; i < tools.length; i++) ((Rescorable)tools[i]).setProperties(p);

    String enabled = p.getProperty("globalscorer.enable", "false");
    String url = p.getProperty("globalscorer.reportURL", "");
    int period = Integer.parseInt(p.getProperty("globalscorer.periodSeconds", "0"));
    // System.out.println("AbstractScorer constructor: " + enabled + " " + url + " " + period);
    if (enabled.equals("false") || url.equals("") || period == 0) return;
    sr = new ScoreReporter(url, period, this);
    sr.start();
  }
  
  public void setUpdateVisual( boolean b) { // to enable/disable mult display
    updateVisual = b;
    for (int i = 0; i < tools.length; i++) ((Rescorable)tools[i]).setUpdateVisual(b);
    // System.out.println("AS:updateVisual: " + updateVisual);
  }

  public void clear(){ // reset scorer and any checkers it holds
    pts = 0;
    mults = 0;
    for (int i = 0; i < tools.length; i++) ((Rescorable)tools[i]).clear();
  }

  public abstract QSOInfo check( LogEntry le);  // is it in dupe/mult sheets?

  public abstract QSOInfo score( LogEntry le);  // add it to dupe/mult sheets

  public void report(){ // write a multiplier report; can be overridden if needed
    FileOutputStream fos = null;
    String filename = "logs/" + (String)p.get("logfilebasename") + "-mult.txt";
    try {
      fos = new FileOutputStream( filename ); 
    } catch (IOException e) { System.out.println( e ); System.exit(1); }
    PrintWriter report = new PrintWriter( fos );
    for (int i = 0; i < tools.length; i++) ((Rescorable)tools[i]).writeReport(report);
    report.close();
  }

  // write cabrillo-format summary.  Should be shareable by all scorers.
  // (If they don't like it, they can override)
  public void summarize() {
    FileWriter fos = null;
    String filename = "logs/" + (String)p.get("logfilebasename") 
                    + "-summary.txt";
    try {
      fos = new FileWriter( filename, false );  
    } catch (IOException e) { System.out.println( e ); System.exit(1); }
    summarize(fos);    
  }

  // Writes the Cabrillo header, with call-outs to handle bits of information that 
  // can vary.  THere should be more uniformity in the way these callouts work.  BUT:
  // mandatory non-variable fields just printed out here
  // mandatory fields with differing information print the keyword and call a function
  //    to get the value
  // optional fields just call a function (which often does nothing)
  public void summarize(Writer s) {
    PrintWriter pw = new PrintWriter( s );
    pw.println("START-OF-LOG: 2.0");
    pw.println("ARRL-SECTION: " + getQTH());
    pw.println("CONTEST: " + (String)p.get("cabContestName"));
    pw.println("CALLSIGN: " + (String)p.get("callsign"));
    doCategory(pw); 
    pw.println("CLAIMED-SCORE: " + findScore());
    pw.println("OPERATORS: " + (String)p.get("operators"));
    pw.println("CLUB: " + (String)p.get("club"));
    pw.println("CREATED-BY: JL 1.0");
    pw.println("NAME: " + (String)p.get("name"));
    pw.println("ADDRESS: " + (String)p.get("address1"));
    pw.println("ADDRESS: " + (String)p.get("address2"));
    pw.println("ADDRESS: " + (String)p.get("address3"));
    doIotaIsland(pw);
    doOfftime(pw);
    doSoapbox(pw);
    doCopyLogfile(pw);
    pw.println("END-OF-LOG:");
    pw.close();
  }

  private void doSoapbox(PrintWriter pw) {
    String soap = JOptionPane.showInputDialog("Short soapbox comment");
    if (soap == null) soap = "";
    pw.println("SOAPBOX: " + soap);
  }

  private void doCopyLogfile(PrintWriter pw) {
    String line = null;
    File logfile = new File ("logs/" + p.getProperty("logfilebasename") + "-log.txt");
    try {
      BufferedReader fr = new BufferedReader( new FileReader(logfile));
      while ( null != ( line = fr.readLine() )) {
        if (line.startsWith("QSO:") || line.startsWith("QTC:"))
          pw.println(line);
      }
      fr.close(); 
    } catch (IOException e) {System.out.println(e); }
  }

  // add a header for IOTA island (so far, only for IOTA)
  protected void doIotaIsland(PrintWriter w) {}; 

  protected void doCategory(PrintWriter w) {
    w.println("CATEGORY: " + (String)p.get("category") + " "
                            + getBandCategory() + " "
                            + getPowerCategory() + " "
                            + getModeCategory() );
  }

  // add a header listing off periods (required for WAE)
  protected void doOfftime(PrintWriter w) {}; 

  protected String getQTH() {
    return p.getProperty("arrlSection");
  }

  protected String getBandCategory() {
    return p.getProperty("bandCategory", "ALL");
  }

  protected String getPowerCategory() {
    return p.getProperty("powerCategory", "HIGH");
  }

  protected String getModeCategory() {
    if ( p.getProperty("useModeCategory", "false").equals("true") )
      return p.getProperty("modeCategory", "");
    else return "";
  };

  protected int findScore() {
    return pts * mults;
  }

  public static class PropertyValidator  {
    // Returns properties keys that are NOT valid given the current contest
    public Vector validateOperation(Properties p) {
      Vector v = new Vector();
      // System.out.println("Validator called (AbstractScorer)");
      if (p.getProperty("callsign", "").equals("")) v.add("callsign");
      if (p.getProperty("name", "").equals("")) v.add("name");
      if (p.getProperty("address1", "").equals("")) v.add("address1");
      if (p.getProperty("address2", "").equals("")) v.add("address2");
      if (p.getProperty("address3", "").equals("")) v.add("address3");
      if (p.getProperty("zipcode", "").equals("")) v.add("zipcode");
      if (p.getProperty("operationName", "").equals("")) v.add("operationName");
      int nops = new StringTokenizer(p.getProperty("operators", "")).countTokens();
      if ( ! (nops > 0) ) v.add("operators");
      return v;
    }
  }

  public java.awt.Component getSummaryDisplay() { return null; }
  
}