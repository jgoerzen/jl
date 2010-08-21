/* Contests supported:  CQWWDX, ARRL SS, ARRL VHF SS (etc.), ARRLTen, 
                        ARRL DX, NAQP, IOTA, NA Sprint, Generic QSO, 
                        IARU HF, CQ 160, ARRL 160, Generic w/ serial #s, 
                        WPX, NEQP (res and non), CQP (nonres), Texas (nonres),
                        Indiana (nonres), Stew Perry, RDX, OK-OL, SAC, WAG, 
                        AllAsia, OceaniaDX, LZDX, WAE, MAQP (nonres)
*/
package com.loukides.jl.jl;
import com.loukides.jl.util.*;
import com.loukides.jl.gadgets.*;
import com.loukides.jl.contests.*;
import com.loukides.jl.qtc.*;
import com.loukides.jl.keyer.MessageCommon;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class JL {
  private static Properties p = new Properties();
  private static File cprops = null;
  private static File opprops = null;
  private static File layoutprops = null;
  private static File netprops = null;
  private static File xcvrprops = null;
  private static File audioprops = null;
  private static File globalscorerprops = null;
  private static final int NOFILEFOUND = 1;
  private static final int FILEFOUND = 0;
  private static String cfile, ofile;
  private static InitialChoices ic = new InitialChoices();

  public static void main ( String [] args ) {
    // figure out what files to load
    // if no arguments, use the choosers
    if (args.length == 0 ) {
      cfile = ic.getContestPropertiesFile();
      ofile = ic.getOperationPropertiesFile();
    } else if (args.length == 2 ) { // user specified a contest and an operation
      cfile =  args[0] + ".props";
      ofile =  args[1] + ".props";
    } else { 
      U.die("java JL contestname call --OR-- java JL");
    }
    // now load the properties files; fail if we can't find either one
    cprops = new File( "contests/" + cfile);
    if ( loadProps(cprops) == NOFILEFOUND ) 
      U.die("no contest specified: " + cprops);
    // set early; allows us to identify the country.  Set again later after
    // we have the full set of properties.  (Shouldn't make a difference, but 
    // just in case...  No, really does NOT make a difference)
    Callsign.setProperties(p);  
    // now assuming we *always* want to edit (or at least check)
    // this has the unfortunate side-effect of rewriting the file (with no changes,
    // we hope).  Not necessarily a bad thing...
    ofile = ic.makeOperationPropertiesFile(p, ofile); 
    opprops = new File("operations/" + ofile);
    if ( loadProps(opprops) == NOFILEFOUND ) 
      U.die("no operation specified: " + opprops);
    layoutprops = new File("configuration/layout.props");
    if ( loadProps(layoutprops) == NOFILEFOUND )
      U.die("cannot find layout properties");
    netprops = new File("configuration/network.props");
    String transceiver = p.getProperty("transceiver", "None");
    xcvrprops = new File("configuration/" + transceiver + ".props");
    audioprops = new File("configuration/keyer.props");
    globalscorerprops = new File("configuration/globalscorer.props");
    if ( loadProps(netprops) == NOFILEFOUND )
      U.die("cannot find network properties");
    if ( loadProps(xcvrprops ) == NOFILEFOUND ) // if no transceiver props
      System.out.println("Couldn't find transceiver props; no rig control"); 
    if ( loadProps(audioprops ) == NOFILEFOUND ) // if no transceiver props
      System.out.println("Couldn't find audio props; no voice keyer"); 
    loadProps(globalscorerprops); // don't freak out if it doesn't exist
    // p.list();
    // AbstractExchange.p is static, so we set it once for all.  Hmm--could
    // be a good idea elsewhere.
    AbstractExchange.setProperties(p);
    Callsign.setProperties(p);
    LogEntry.setProperties(p);
    VisualMult.setProperties(p);
    DoubleButtonArray.setProperties(p);
    HistoryWindow.setProperties(p);
    EditWindow.setProperties(p);
    MessageCommon.setProperties(p);
    QTCLine.setProperties(p);
    QTCDisplay.setProperties(p);

    ExchangeFactory f = new ExchangeFactory(p);
    MainWindow mw = new MainWindow(p, f);
    mw.addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e) { System.exit(0); }
    });
  }

  private static int loadProps(File propsfile) {
    try {
      FileInputStream fis = new FileInputStream(propsfile);
      p.load(fis); 
      fis.close();
    } catch (IOException e) { return NOFILEFOUND; }
    return FILEFOUND;
  }

}