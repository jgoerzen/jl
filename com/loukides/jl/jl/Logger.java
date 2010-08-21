// allows all sorts of editing.  Editing is NOT reflected in dupesheet, 
// score, or the on-disk logfile
// UNTIL you re-build the log (on demand).  
// NET NOTES:  currently ONLY sends the log on redupe or on individual additions
// Sends it as individual entries
package com.loukides.jl.jl;
import com.loukides.jl.util.*;
import com.loukides.jl.gadgets.*;
import com.loukides.jl.contests.*;
import com.loukides.jl.net.*;
import com.loukides.jl.qtc.OriginatedQTCManager;
import com.loukides.jl.qtc.QTCEntry;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.text.*;
import javax.swing.table.*;

public class Logger  {

  private Vector veclog = new Vector(1000,100);

  private File logfile;
  private FileOutputStream fos;
  private PrintWriter log;
  private String logline;
  private String filename;
  private int bkno = 0;
  private Properties p;
  private ExchangeFactory f;
  private EditWindow editor;
  private AbstractScorer sc;
  private com.loukides.jl.gadgets.Timer timer;
  private EditableLogModel etm = new EditableLogModel();
  private NetworkLogger nl;
  private int searchrow;
  private int searchfield;
  private OriginatedQTCManager qtcm = null;

  public Logger(Properties p, ExchangeFactory f) { 
    this.p = p;
    this.f = f;

    etm.setLog(veclog);

    nl = new NetworkLogger(p);
    nl.setRemoteProperties(p);

    String classbase = "com.loukides.jl.contests." + p.getProperty("classBasename");
    try {
      sc = (AbstractScorer) Class.forName(classbase + "Scorer").newInstance();
      sc.setProperties(p);
    } catch (Exception e) { 
      e.printStackTrace();
      U.die("Died instantiating class " + classbase + "Scorer");
    }
    if ( sc instanceof WAEScorer ) {
      qtcm = new OriginatedQTCManager(veclog, (WAEScorer)sc, this);
      ((WAEScorer)sc).setLog(veclog); // so scorer can figure out times
    }

    String xmtr = p.getProperty("transmitterNumber", ""); 
    if (! xmtr.equals("") ) xmtr += "-";
    filename = "logs/" + p.getProperty("logfilebasename") 
                       + "-" + xmtr + "log.txt";
    logfile = new File(filename);

    sc.setUpdateVisual(false); 
    if ( logfile.exists() ) { //continuing a prev. run; accumulate old entries
      try {
        BufferedReader fis = new BufferedReader( new FileReader(filename) );
        try {
           // re-build in-memory log
           readlog( fis );
        } catch (IOException e) { 
             fis.close(); 
        } // done reading
      } catch (IOException e) { System.out.println( e ); }    
    } 
    sc.setUpdateVisual(true); 
    try {
      fos = new FileOutputStream( filename, true );  // append mode
    } catch (IOException e) { System.out.println( e ); }
    log = new PrintWriter( fos );
    timer = new com.loukides.jl.gadgets.Timer(veclog, p);
  }

  private void readlog( BufferedReader fis) throws IOException {
    HashMap logentriesforqtc = new HashMap();
    while ( null != (logline = fis.readLine()) ){
      if (logline.startsWith("QSO:")) {
        // Process a normal log entry
        LogEntry le = new LogEntry(f, ""); // will fill in the mode from the log
        le.parseCabrilloString(logline);
        // IF serial isn't set by now, it's a contest in which serial isn't
        // part of the exchange.  Set it anyway--I like to have it around.
        // Serials aren't preserved on restart if they're set this way.
        // But they are preserved if they're part of the exchange, which is
        // all we really care about.
        if ( le.getSent().getSerial() == 0 )
          le.getSent().setSerial( this.getNextSerial() );
        sc.check(le); // checking not really needed, but sets things up--?
        sc.score(le);
        veclog.add(le);
        // System.out.println(le.toCabrilloString());
        if (qtcm != null) logentriesforqtc.put(qtckey(le), le);
      } else if (logline.startsWith("QTC:")) {
        // Process a QTC log entry (WAE ONLY)
        QTCEntry qe = new QTCEntry(logline);
        qtcm.scoreQTCFromLog(qe, logentriesforqtc);
        veclog.add(qe);
        // System.out.println(qe.toCabrilloString());
      }
    }
    if (qtcm != null) qtcm.doFinalCleanup(logentriesforqtc);
    logentriesforqtc = null; // let gc cleanup
  }

  private String qtckey(LogEntry le) {
    return le.getRcvd().getCallsign() + U.zpad.format(le.getRcvd().getSerial()) + 
           AbstractLoggable.minutesformat.format(le.getDate());
  }

  public void redoLog() {
    sc.setUpdateVisual(false); // don't update visual multiplier display  
    try {
      log.close(); fos.close();
      // System.out.println(logfile);
      String newfilename = filename + ".BAK" + bkno++;  
      File newfile = new File(newfilename);
      boolean success = logfile.renameTo(newfile);  
      // APPARENT WIN2K BUG:  sometimes doesn't write
      // System.out.println(" File rename: " + newfile + " " + success);
      logfile = new File(filename);
      fos = new FileOutputStream( filename, false);  // NOT append mode
      log = new PrintWriter( fos );
    } catch (IOException e) { System.out.println( e ); }
    // rebuild log file *AND* rechecked dupe sheet *AND* zone mult table
    sc.clear();  // clear the scorer, which holds dupe sheets, etc.
    nl.clear();  // clear the network logger 
    for ( int i = 0 ; i < veclog.size(); i++ ) {
      Loggable le = (Loggable) veclog.get(i);
      if (le instanceof LogEntry) // QTC are immutable, and don't need to be rescored
        sc.score((LogEntry)le);   // this should re-dupe, recheck mult, and rescore
      String logstring = le.toCabrilloString();
      log.println( logstring );  // save in the new file
      nl.addLogEntry( le );
    }
    log.flush();
    // make sure timer tracks changes to the log; update displays
    timer.resetTimer();
    etm.fireTableDataChanged();
    sc.setUpdateVisual(true);
  }

  public void addLogEntry(Loggable le) {
    veclog.add(le);
    String logstring = le.toCabrilloString();
    log.println( logstring );
    // System.out.println(le);
    log.flush();
    nl.addLogEntry( le ); 
    etm.fireTableRowsInserted(veclog.size() -1, veclog.size()); 
    editor.scrollToEnd(); 
    timer.contactMade(le.getDate());
  }

  // finds the next valid serial number to be issued. (Next serial number after
  // the last serial given out, not counting QTC).
  public int getNextSerial() {
    // fixed to account for QTC
    if ( veclog.size() == 0 ) return 1;
    for ( int i = veclog.size()-1 ; i >= 0; i--) { // go backwards, discarding QTC
      if (veclog.elementAt(i) instanceof LogEntry)
        return ((LogEntry)veclog.elementAt(i)).getSent().getSerial() + 1;
    }
    return -1; // shouldn't get here
  }

  public void setEditor(EditWindow e) { 	
    editor = e;
  }

  //pass throughs to the scorer
  // Is this really needed?  I like the idea that the mainwindow doesn't need
  // to know about the scorer, but this seems crazy.  
  public QSOInfo check(LogEntry le) { return sc.check(le); }
  public QSOInfo score(LogEntry le) { return sc.score(le); }
  public void report()              { sc.report(); }
  public void summarize()           { sc.summarize(); }

  // methods to make the text fields searchable
  // This should arguably (almost certainly?) be part of the model.
  // In any case, we query the model rather than looking directly at the 
  // log vector.  Could do the latter--any particular reason not to?
  // Querying the model saves us from having to convert things to strings,
  // but that surely isn't a big deal.
  public int search(String s, boolean fromBegin) {
    int i;
    s = s.toUpperCase();
    for (i = ( fromBegin ? 0 : searchrow ); i < veclog.size(); i++ ) {
      searchrow = i + 1;
      // search time (1), rcvd call (5), rcvd exchange (6) 
      if ( ((String)etm.getValueAt( i, 1 )).indexOf( s ) != -1 ) return i;
      if ( ((String)etm.getValueAt( i, 5 )).indexOf( s ) != -1 ) return i;
      if ( ((String)etm.getValueAt( i, 6 )).indexOf( s ) != -1 ) return i;
    }
    return 0;
  }

  public void deleteLogEntry(int row) {
    if ( row >= veclog.size() ) return;
    veclog.remove(row);
    etm.fireTableRowsDeleted(row, row);
    searchrow--; // (we've cut a row, so move the pointer back up one... I think
  }

  public TableModel getLogModel() { 
    return etm; 
  }

  public com.loukides.jl.gadgets.Timer getTimer() { return timer; }

  public AbstractScorer getScorer() { return sc; }

  public OriginatedQTCManager getQTCManager() { return qtcm; }

}
