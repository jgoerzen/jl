package com.loukides.jl.jl;
import com.loukides.jl.gadgets.*;
import com.loukides.jl.util.*;
import com.loukides.jl.contests.*;
import com.loukides.jl.keyer.*;
import com.loukides.jl.qtc.OriginatedQTCManager;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class MainWindow extends JFrame implements Canceller {

  final JLabel bandlabel = new JLabel("Band");
  final JLabel calllabel = new JLabel("Call");
  final JLabel rcvdlabel = new JLabel("Rcvd");
  final JLabel sentlabel = new JLabel("Sent");
  final JLabel entrylabel = new JLabel("Entry");
  final JLabel dupelabel = new JLabel("");  // might become generic error label
  final JLabel seriallabel = new JLabel("");
  final JLabel multlabel = new JLabel("<html></html>"); // pre-start html parser
  final JLabel countrymultlabel = new JLabel("");
  final JLabel errorlabel = new JLabel(""); 
  final JLabel nulllabel2 = new JLabel(""); // spare
  final JLabel nulllabel3 = new JLabel(""); // spare
  final JTextField bandfield = new JTextField(13);
  final JTextField callfield = new JTextField(13);
  final JTextField rcvdfield = new JTextField(13);
  final JTextField sentfield = new JTextField(13);
  final JTextField entryfield = new JTextField(13);
  final JButton logbutton = new JButton("Log");
  final JButton cancelbutton = new JButton("Cancel");
  final JButton pushbutton = new JButton("Push");
  final JButton popbutton = new JButton("Pop");
  final JButton qtcbutton = new JButton("QTC");

  final Box col1 = new Box(BoxLayout.Y_AXIS);
  final Box col2 = new Box(BoxLayout.Y_AXIS);
  final Box col3 = new Box(BoxLayout.Y_AXIS);
  final Box col4 = new Box(BoxLayout.Y_AXIS);

  final Properties props;
  final Vector bands = new Vector();
  final Vector modes = new Vector();
  final ExchangeFactory exf;
  final Logger logger;
//  final Logger lg; // same as logger; BAH.
  final EditWindow ew;
  private OriginatedQTCManager qtcm = null;
  private LogEntry le;
  private DoubleButtonArray shelfdisplay;
  private ShelfModel shelf;
  private Transceiver transceiver = null;
  private Keyer vk = null;

  private String currentband = "";
  private String currentmode = "";
  boolean stickyzone = false;
  Color normalcolor; 
  QSOInfo qinfo = new QSOInfo();
  private final Dimension gap = new Dimension(10, 5);

  // public final static String RS = "59  ";

  public MainWindow(Properties prps, ExchangeFactory ef) {
    super("JL: " + prps.getProperty("contestName"));

    this.props = prps;
    this.exf = ef;

    logger = new Logger(prps, exf);
//    lg = logger; // YECCH!  saves some renaming, that's all
    transceiver = new TransceiverFactory(prps).getInstance();
    qtcm = logger.getQTCManager();
    if (qtcm != null) qtcm.setCanceller(this);
    MessageCommon.setMainEntryField(entryfield);

    ew = new EditWindow(logger, this);
    ew.addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e) { System.exit(0); }
    });

    String bandlist = props.getProperty("bands");
    String modelist = props.getProperty("mode");
    currentmode = (new StringTokenizer(modelist)).nextToken();
    currentband = (new StringTokenizer(bandlist)).nextToken();
    // on startup, first band / mode in list is the default
    for ( StringTokenizer st = new StringTokenizer(bandlist); st.hasMoreTokens(); 
             bands.add(st.nextToken()));
    for ( StringTokenizer st = new StringTokenizer(modelist); st.hasMoreTokens(); 
             modes.add(st.nextToken()));
    bandfield.setText( currentband + " " + currentmode );

    le = new LogEntry( exf, currentmode);
    setBandAndMode(le);
    le.getSent().setSerial( logger.getNextSerial());

    Container p = getContentPane();
    BoxLayout lm = new BoxLayout(p, BoxLayout.X_AXIS);
    p.setLayout(lm);

    // start building the GUI.  The way this is done is a REAL embarassment, and 
    // should be cleaned up.
    // First, add the timer.  (Which may include a status panel...)
    p.add(new Box.Filler(gap, gap, gap));
    p.add(logger.getTimer());
    // Add the QSO/Mult summary, if one is available
    Component sumdisplay = logger.getScorer().getSummaryDisplay();
    if (sumdisplay != null) {
      p.add(new Box.Filler(gap, gap, gap));
      p.add(sumdisplay);
    }
    p.add(new Box.Filler(gap, gap, gap));
    // Add a QSO shelf
    shelf = new ShelfModel(this, transceiver, props);
    shelfdisplay = new DoubleButtonArray(shelf);
    shelfdisplay.setTitle("QSO Stack");
    p.add(shelfdisplay);
    p.add(new Box.Filler(gap, gap, gap));

    // Now the main display (should really be a separate class)
    String pbase = "layout.MainWindow.input.";
    final int labelwidth = Integer.parseInt(props.getProperty(pbase + "labelwidth", "40"));
    final int fieldheight = Integer.parseInt(props.getProperty(pbase + "fieldheight", "30"));
    final int reportwidth = Integer.parseInt(props.getProperty(pbase + "reportwidth", "150"));
    final int fieldwidth = Integer.parseInt(props.getProperty(pbase + "fieldwidth", "130"));
    final int buttonwidth = Integer.parseInt(props.getProperty(pbase + "buttonwidth", "130"));
    final Dimension labelsize = new Dimension( labelwidth, fieldheight);
    final Dimension fieldsize = new Dimension ( fieldwidth, fieldheight);
    final Dimension reportsize = new Dimension (reportwidth, fieldheight);
    final Dimension buttonsize = new Dimension( buttonwidth, fieldheight);

    U.setSizes(bandlabel, labelsize);
    U.setSizes(calllabel, labelsize);
    U.setSizes(rcvdlabel, labelsize);
    U.setSizes(sentlabel, labelsize);
    U.setSizes(entrylabel,labelsize);

    U.setSizes(bandfield, fieldsize);
    U.setSizes(callfield, fieldsize);
    U.setSizes(rcvdfield, fieldsize);
    U.setSizes(sentfield, fieldsize);
    U.setSizes(entryfield,fieldsize);

    U.setSizes(qtcbutton, buttonsize);
    U.setSizes(pushbutton, buttonsize);
    U.setSizes(cancelbutton, buttonsize);
    U.setSizes(popbutton, buttonsize);
    U.setSizes(errorlabel, buttonsize);

    U.setSizes(seriallabel, reportsize);
    U.setSizes(multlabel, reportsize);
    U.setSizes(countrymultlabel, reportsize);
    U.setSizes(logbutton, reportsize);
    U.setSizes(dupelabel, reportsize);

    col1.add(bandlabel);
    col1.add(calllabel);
    col1.add(rcvdlabel);
    col1.add(sentlabel);
    col1.add(entrylabel);
    col1.add(Box.createVerticalGlue());
    p.add(col1);

    col2.add(bandfield);
    col2.add(callfield);
    col2.add(rcvdfield);
    col2.add(sentfield); sentfield.setText( le.getSent().getGUIExchange() );
    col2.add(entryfield);
    col2.add(Box.createVerticalGlue());
    p.add(col2);

    col3.add(seriallabel);
    col3.add(multlabel);
    col3.add(countrymultlabel);
    col3.add(logbutton);
    col3.add(dupelabel); dupelabel.setForeground(Color.red);
    col3.add(Box.createVerticalGlue());
    p.add(col3);

    col4.add(qtcbutton);
    col4.add(pushbutton);
    col4.add(popbutton);
    col4.add(cancelbutton);
    col4.add(errorlabel);
    col4.add(Box.createVerticalGlue());
    p.add(col4);

    // make entry field grab focus whenever the mouse is in the main window
    this.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent me) { 
        entryfield.requestFocus(); 
        // System.out.println("Focus requested for entryfield");
      }
    });
    normalcolor = logbutton.getForeground();
    qinfo = logger.score(null);
    setLabels(qinfo);

    entryfield.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent ae){
        String entry = entryfield.getText().toLowerCase();

        if ( isBand(entry) ) {
          bandfield.setText(entry + " " + currentmode + " ");
          entryfield.setText("");
          currentband = entry;
          // seems to me there's some reason to set this here, but not sure
          // what it would be; the band and mode of the log entry are set in doLog()
          setBandAndMode(le);
          return;
        }

        if ( isMode(entry) ) {
          entry = entry.toUpperCase();
          bandfield.setText(currentband + " " + entry + " ");
          entryfield.setText("");
          currentmode = entry;
          setBandAndMode(le);
          return;
        }

        if ( isCommit(entry) ) {
          /* here's where we create a LogEntry and log it */
          doLog(); 
          return;
        }

        if ( isSearch(entry) ) {
          String searchstring = entry.substring(1);
          if ( searchstring.equals("") ) ew.next();
          else ew.search(searchstring);
          entryfield.setText("");
          return;
        }

        if ( isTransceiverOrKeyerCommand(entry) ) {
          if (entry.length() == 1) {       //  > by itself; terminates split freq operation
            transceiver.setSplit(0.0f);
            entryfield.setText("");
            return;
          }
          String s1 = entry.substring(1);
          char firstchar = s1.charAt(0);
          if (Character.isLetter(firstchar) || firstchar == '$') { // >text or >$something
            vk.playArbitraryMessage(s1);                           // means keyer command
            entryfield.setText(s1);
            return;
          }
          String freqstr = entry.substring(2);  // various set frequency operations
          float newfreq = 0.0f;
          // System.out.println("set freq: " + s1 + " " + freqstr);
          try { 
            // >=digits means just set the frequency (both tx and rx freqs) 
            if ( s1.startsWith("=") )
              transceiver.setFrequency(Float.parseFloat(freqstr));
            // >+digits or >-digits means set split operation; digits gives the offset
            else if (s1.startsWith("+") || s1.startsWith("-")) 
              transceiver.setSplit(Float.parseFloat(s1) + transceiver.getFrequency());
            // >digits means absolute split (digit after initial >); digits gives TX freq
            else transceiver.setSplit(Float.parseFloat(s1));
          } catch (NumberFormatException e) {System.out.println(e);} // just ignore the command
          entryfield.setText("");
          return;
        } 
        if ( ! (qtcm == null) && wantsQTC(entry) ) {  // EVENTUALLY S/B Button
          qtcm.showQTC(currentband, currentmode);
          return;
        }

        // we got some other part, or parts, of the exchange

        updateAfterEntry( entryfield, le ); 
      } // end actionPerformed
    });
 
    logbutton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        doLog();
      }
    });

    callfield.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        updateAfterEntry( callfield, le );
      }
    });

    rcvdfield.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        updateAfterEntry( rcvdfield, le );
      }
    });

    bandfield.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        String entry = "";
        StringTokenizer bandentries = 
          new StringTokenizer(bandfield.getText().toLowerCase());
        while ( bandentries.hasMoreTokens() ) { 
          entry = bandentries.nextToken();
          // System.out.println(entry);
          if ( true == isBand(entry) ) {
            // I don't see why we need to le.setBand() here...
            currentband = entry;
          } else if ( true == isMode(entry) ) { 
            currentmode = entry.toUpperCase();
            cancel();  // cancel to make sure the current le's RST is set right
          }
        }       
        bandfield.setText(currentband + " " + currentmode + " ");
        entryfield.setText("");
      }
    });

    sentfield.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        modifySentExchange(sentfield, le);
      }
    });

    qtcbutton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
          if (qtcm != null) qtcm.showQTC(currentband, currentmode);
      }
    });
    if (qtcm == null) qtcbutton.setEnabled(false);
    
    popbutton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        pop();
      }
    });

    pushbutton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        push();
      }
    });

    cancelbutton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        cancel();
      }
    });

    // finally, we're done with the main display so add the keyer (if there is one)
    // p.add(new Box.Filler(gap,gap,gap));
    vk = new Keyer(this, transceiver, props);
    if ( vk != null )  {
      p.add(vk.getUI());
      if (qtcm != null) qtcm.setKeyer(vk);
      // now wire function keys to the keyer
      JRootPane enclosure = this.getRootPane();  // can be changed if necessary
      int numclips = Integer.parseInt(prps.getProperty("keyer.numclips", "0"));
      ActionMap am = enclosure.getActionMap();
      InputMap im = enclosure.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      for (int i = 0; i < Math.min(numclips, 10); i++) {
        am.put("playkeyermessage" + i, PlayMessageAction.getInstance(i));
        im.put(KeyStroke.getKeyStroke("F" + (i+1)), "playkeyermessage" + i);
      }
      am.put("stopmessage", StopMessageAction.getInstance());
      im.put(KeyStroke.getKeyStroke("ESCAPE"), "stopmessage");
    }
    pbase = "layout.MainWindow.";
    setLocation(Integer.parseInt(props.getProperty(pbase + "location.x", "0")),
                Integer.parseInt(props.getProperty(pbase + "location.y", "0")));

    pack(); 
    show(); 
  }

  // I started off doing a good job of avoiding repeated code.
  // But I've gotten very sloppy; need to refactor updateAfterEntry, setLabels,
  // cancel, doLog
  // THIS IS AN EXCEEDINGLY WEIRD INTERFACE! 1st arg should be string, not text field
  private void updateAfterEntry( JTextField tf, LogEntry le ) {
    Exchange rcvd = le.getRcvd();
    rcvd.addToExchange( tf.getText() );
    // we need to set the log entry's band and mode now
    // so that we can dupe/check mult correctly
    setBandAndMode(le);
    entryfield.setText("");
    qinfo = logger.check(le);
    setLabels(qinfo);
    if ( rcvd.isComplete()) logbutton.setForeground(Color.red);
    callfield.setText( rcvd.getCallsign() );
    entryfield.requestFocus(); // make sure we still have focus! 
  }

  private void modifySentExchange(JTextField tf, LogEntry le) {
    Exchange sent = le.getSent();
    sent.modifySentExchange(tf.getText());
    sentfield.setText(sent.getGUIExchange() );
  }

  private void setLabels(QSOInfo qinfo) {
    seriallabel.setText("  Next: " + le.getSent().getSerial() 
                      + qinfo.countlabeltext);
    dupelabel.setText(qinfo.dupelabeltext);
    multlabel.setText(qinfo.multlabeltext);
    countrymultlabel.setText(qinfo.otherlabeltext);
  }

  private void doLog() {
    entryfield.setText(""); 
    le.setDate( null );  // null means assign current date.  (Overload better?)
    // and we need to make sure that we *really* have the band and mode correctly
    setBandAndMode(le);
    qinfo = logger.score(le);  // add to dupe/mult sheets
    logger.addLogEntry(le);
    le = new LogEntry(exf, currentmode ); // log entry for the NEXT qso
    // post-processing--after adding to the log.
    logbutton.setForeground(normalcolor);
    le.getSent().setSerial( logger.getNextSerial() );
    sentfield.setText( le.getSent().getGUIExchange() );
    callfield.setText("");
    rcvdfield.setText("");
    setLabels(qinfo); 
    entryfield.requestFocus();   
  }

  public void cancel() { // public so QTC manager can call it...
    le = new LogEntry(exf, currentmode);
    qinfo = logger.score(null); // get a special qinfo package
    qinfo.dupelabeltext = "";
    logbutton.setForeground(normalcolor);
    le.getSent().setSerial( logger.getNextSerial() );
    sentfield.setText( le.getSent().getGUIExchange() );
    setLabels(qinfo);
    entryfield.setText("");
    callfield.setText("");
    rcvdfield.setText("");
    entryfield.requestFocus();
  }

  private boolean isBand(String s) {
    return bands.contains(s);
  }

  private boolean isMode(String s) {
    String t = s.toUpperCase();
    return modes.contains(t);
  }

  private boolean isCommit(String s) {
    return s.equals("");
  }

  private boolean isSearch(String s) {
    return s.startsWith("/");
  }

  private boolean isTransceiverOrKeyerCommand(String s) {
    return s.startsWith(">");
  }

  private boolean wantsQTC(String s) {
    return s.equalsIgnoreCase("q");
  }

  private void push() {
    // Current thinking on the mode and frequency:
    // If rig control is in effect, the memory will save both mode and frequency.
    // Furthermore, we'll read it once more upon logging.
    // If rig control is not in effect, currentmode and currentband are GOOD.
    String heard = 
      callfield.getText() + " " + rcvdfield.getText() + " " + entryfield.getText();
    int memnumber = transceiver.getAvailableMemory();
    transceiver.storeMemory(memnumber);
    ShelvableQSO q = new ShelvableQSO(currentband, currentmode, heard, memnumber);
    cancel();
    shelf.push(q);
  }

  private void pop() {
    // This still makes too many rig control calls, but it's improved.
    // You get setVFO, getFreq, and getMode (implictly in updateAfterEntry)
    // I'm not sure these can be avoided.
    ShelvableQSO popped = shelf.pop();
    if (popped == null || popped.isNull() ) return;
    int mem = popped.getMemory();
    if ( mem > 0 ) {
      transceiver.setVFOFromMemory(mem);  // rig op 1 + 2
      transceiver.releaseMemory(mem);
    }
    bandfield.setText( popped.getBand() + " " + popped.getMode() + " ");
    currentband = popped.getBand();
    currentmode = popped.getMode();
    cancel();
    entryfield.setText(popped.getHeard());  // process as if we made an entry by hand
    updateAfterEntry(entryfield, le);       // simulate CR in the entry field  :: op 3 + 4
  }

  public ShelvableQSO swapWithMain(ShelvableQSO q) {
    // System.out.println("Swap called");
    // See current thinking for push()
    // see notes for pop()  :: 6 rig ops
    String heard = 
      callfield.getText() + " " + rcvdfield.getText() + " " + entryfield.getText();
    int memnumber = transceiver.getAvailableMemory();
    transceiver.storeMemory(memnumber);  // rig op 1 + 2
    ShelvableQSO current = new ShelvableQSO(currentband, currentmode, heard, memnumber);
    cancel();
    if (q == null || q.isNull() ) return current;
    int mem = q.getMemory();
    if ( mem > 0 ) {
      transceiver.setVFOFromMemory(mem);  // rig op 3 + 4
      transceiver.releaseMemory(mem);
    }
    bandfield.setText( q.getBand() + " " + q.getMode() + " ");
    currentband = q.getBand();
    currentmode = q.getMode();
    entryfield.setText(q.getHeard());  // process as if we made an entry by hand
    updateAfterEntry(entryfield, le);  // simulate CR in the entry field :: ops 5 + 6
    return current;
  }

  private void setBandAndMode(LogEntry le) {
    String t1, t2;
    float freq = transceiver.getFrequency();
    String mode = transceiver.getMode();
    if ( freq == (float) 0.0 ) le.setBand(currentband);
    else {
      le.setFrequency(freq);
      currentband = le.getBand(); // use LogEntry's freq-to-band conversion
    }
    if ( mode.equals("")) le.setMode(currentmode);
    else {
      le.setMode(mode);
      currentmode = mode;
    }
    bandfield.setText(currentband + " " + currentmode + " ");
    // since mode changes might change the signal report, update the display
    // The once-somewhat-clean design is wandering more and more.  This stuff
    // doesn't belong here, but what the heck?
    sentfield.setText(le.getSent().getGUIExchange());
    rcvdfield.setText(le.getRcvd().getGUIExchange());
  }

  public String getUIMode() {
    return currentmode;
  }
}