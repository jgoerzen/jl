/* An operation editor; builds the operation properties file 
   Builds an operation properties file, and passes it back to the main program.
   (Note that it does NOT simply add properties to the current properties file.
   The main program re-reads the file created here and merges them.)
   Lots of weird quirks, including processing all the input for every pass,
   creating a file for every pass (possibly with a different name), etc.
   DOESN'T DO:  overlay category (yet)
                IOTA name (should be computable from the number)
                continent (not used by code)
                input validation */
package com.loukides.jl.gadgets;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;
import com.loukides.jl.contests.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;

public class OperationBuilder extends JPanel {
  private Properties p;
  private Properties tempprops = new Properties();
  private Object[] arrlsections, states, categories, bands, 
                   powers, modes, clubs, transceivers;
  private GeneralActionListener gal = new GeneralActionListener();
  private static final Dimension labelsize = new Dimension(150, 25);
  private static final Dimension comboboxsize = new Dimension(150, 25);
  private boolean fileloaded = false;

  private OperationProperty sectionProperty = 
    new OperationProperty("ARRL Section", "arrlSection", new JComboBox() );
  private OperationProperty stateProperty = 
    new OperationProperty("State", "state", new JComboBox() );
  private OperationProperty categoryProperty = 
    new OperationProperty("Operator Category", "category", new JComboBox() );
  private OperationProperty bandProperty = 
    new OperationProperty("Band Category", "bandCategory", new JComboBox() );
  private OperationProperty powerProperty = 
    new OperationProperty("Power Category", "powerCategory", new JComboBox() );
  private OperationProperty modeProperty = 
    new OperationProperty("Mode Category", "modeCategory", new JComboBox() );
  private OperationProperty clubProperty = 
    new OperationProperty("Club", "club", new JComboBox() );
  private OperationProperty transceiverProperty = 
    new OperationProperty("Transceiver", "transceiver", new JComboBox() );

  private OperationProperty [] optemplate  = {
    new OperationProperty("Operation Name", "operationName", new  JTextField( ) ),
    new OperationProperty("Full Name", "name", new  JTextField( ) ),
    new OperationProperty("Callsign", "callsign", new JTextField() ),
    new OperationProperty("Street Address", "address1", new  JTextField( ) ),
    new OperationProperty("Operators", "operators", new JTextField() ),
    new OperationProperty("City/State", "address2", new  JTextField( ) ),
    sectionProperty,
    new OperationProperty("Postal Country", "address3", new  JTextField( ) ),
    stateProperty,
    new OperationProperty("Postal (ZIP) Code", "zipcode", new  JTextField( ) ),
    categoryProperty,
    new OperationProperty("Transmitter ID", "transmitterNumber", new JTextField() ),
    bandProperty,
    new OperationProperty("Field Day Category", "fieldDayCategory", new JTextField() ),
    powerProperty,
    new OperationProperty("CQ WW Zone", "cqZone", new JTextField() ),
    modeProperty,
    new OperationProperty("IARU Zone", "iaruZone", new  JTextField( ) ),
    new OperationProperty("ARRL SS Check", "check", new JTextField() ),
    new OperationProperty("Grid Square", "gridSquare6", new  JTextField( ) ),
    new OperationProperty("County Name", "county", new  JTextField( ) ),
    new OperationProperty("County Abbreviation", "countyAbbrev", new  JTextField( ) ),
    new OperationProperty("IOTA Island Name", "iotaName", new JTextField() ),
    new OperationProperty("IOTA Reference", "iota", new  JTextField( ) ),
    new OperationProperty("First Name", "firstname", new  JTextField( ) ),
    clubProperty,
    transceiverProperty,
    new OperationProperty("Age", "age", new JTextField() )
  }; 
  public static final String SECTIONSFILE = "data/arrl-sections-mod.txt";
  public static final String STATESFILE = "data/states-n-provs.txt";
  public static final String CATEGORIESFILE = "data/cabrillo/categories.txt";
  public static final String BANDSFILE = "data/cabrillo/bands.txt";
  public static final String POWERFILE = "data/cabrillo/powers.txt";
  public static final String MODEFILE = "data/cabrillo/modes.txt";
  public static final String CLUBFILE = "data/cabrillo/club-list.txt";
  public static final String TRANSCEIVERFILE = "configuration/transceivers.txt";

  private AbstractScorer.PropertyValidator validator;
  private Vector mandatoryprops = null;
  private Color invalid = Color.red;
  private Color valid = null;  

  public OperationBuilder(Properties p, String filename) {
    this.p = p;
    if ( !filename.equals("")) {
      try {
        FileInputStream fis = new FileInputStream("operations/" + filename);
        tempprops.load(fis); 
        fileloaded = true;
        fis.close();
      } catch (IOException e) { U.die("Can't load properties file"); }
    }
    // get the "right" foreground color for valid fields
    valid = new JLabel().getForeground();

    arrlsections = readAbbrevsFromFile(SECTIONSFILE); 
    states = readAbbrevsFromFile(STATESFILE);
    categories = readAbbrevsFromFile(CATEGORIESFILE);
    bands = readAbbrevsFromFile(BANDSFILE);
    powers = readAbbrevsFromFile(POWERFILE);
    modes = readAbbrevsFromFile(MODEFILE);
    clubs = readAbbrevsFromFile(CLUBFILE, false, false);
    transceivers = readAbbrevsFromFile(TRANSCEIVERFILE, false, false);
    sectionProperty.values = arrlsections;
    stateProperty.values = states;
    categoryProperty.values = categories;
    bandProperty.values = bands;
    powerProperty.values = powers;
    modeProperty.values = modes;
    clubProperty.values = clubs;
    transceiverProperty.values = transceivers;
    ((JComboBox)clubProperty.inputgadget).setEditable(true);
    setLayout(new GridLayout(1+optemplate.length/2, 4, 20, 5));
    String validatorname = "com.loukides.jl.contests." +
      p.getProperty("classBasename") + "Scorer$PropertyValidator";
    // a hand-implementation of overriding: if we can't get the property validator
    //  we want,instantiate one from the superclass by hand.
    try {
      validator = (com.loukides.jl.contests.AbstractScorer.PropertyValidator)
        Class.forName(validatorname).newInstance();
    } catch (Exception e1) { 
      try { 
        validator = (com.loukides.jl.contests.AbstractScorer.PropertyValidator) 
          Class.forName(
            "com.loukides.jl.contests.AbstractScorer$PropertyValidator").newInstance();
       } catch (Exception e2) {
         System.out.println(e2);
         U.die("Could not find validator " + validatorname); 
       }
    }
    mandatoryprops = validator.validateOperation(tempprops);
    for ( int i = 0; i < optemplate.length; i++) {
      setupPropertyInput(optemplate[i], mandatoryprops);
    }
  }

  private void setupPropertyInput(OperationProperty rowtemplate, 
                                  Vector mandatoryprops) {
    add(rowtemplate.label);
    if (mandatoryprops.contains(rowtemplate.propskey))
      rowtemplate.label.setForeground(invalid);
    else rowtemplate.label.setForeground(valid);
    if ( rowtemplate.inputgadget instanceof JComboBox ) {
      JComboBox jcb = (JComboBox) rowtemplate.inputgadget;
      ComboBoxModel cbm = new DefaultComboBoxModel((Object[])rowtemplate.values);
      rowtemplate.model = cbm;
      jcb.setModel(cbm);
      U.setSizes(jcb, comboboxsize);
      if (fileloaded) jcb.setSelectedItem(
        tempprops.getProperty( (String)rowtemplate.propskey, "" ));
      jcb.addActionListener(gal); 
      add(jcb);
    } else if ( rowtemplate.inputgadget instanceof JTextField ) {
      JTextField jtf = (JTextField) rowtemplate.inputgadget;
      jtf.setText((String)(rowtemplate.values));
      U.setSizes(jtf, labelsize);
      if (fileloaded) jtf.setText(tempprops.getProperty(
        (String)rowtemplate.propskey, ""));
      jtf.addActionListener(gal);
      jtf.addFocusListener(gal);
      add(jtf);
    } else { System.out.println(" something bad happened in op builder"); }
  }

  // this action listener process the whole array when *any* component is touched
  // Note that tabbing through generates focus events not action events, so we
  // have to listen for both.  This could certainly be done more efficiently, but
  // appears to be fast enough.
  private class GeneralActionListener extends FocusAdapter implements ActionListener {

    public void focusLost(FocusEvent e) { actionPerformed(); }

    public void actionPerformed(ActionEvent e) { actionPerformed(); }

    public void actionPerformed() {
      String value = "";
      for ( int i = 0; i < optemplate.length; i++) {
        if ( optemplate[i].inputgadget instanceof JComboBox )
          value = (String)(((JComboBox)optemplate[i].inputgadget).getSelectedItem());
        else if ( optemplate[i].inputgadget instanceof JTextField )
          value = ((JTextField)optemplate[i].inputgadget).getText();
        else { System.out.println(" something bad happend reading components"); }
        tempprops.setProperty( (String)(optemplate[i].propskey), value);
      }
      // some post-processing to fill in fields we can compute ourselves
      // make sure callsign and operators are capitalized; find out the country
      String call = tempprops.getProperty("callsign", "");
      call = call.toUpperCase();
      tempprops.setProperty("callsign", call);
      if (! call.equals("")) {
        Callsign cs = new Callsign(call);
        tempprops.setProperty( "dxccCountry", cs.getCountry() );
      }
      tempprops.setProperty("operators", 
                            tempprops.getProperty("operators", "").toUpperCase());
      // fill in both kinds of grid squares
      String square = tempprops.getProperty("gridSquare6", "");
      if ( square.length() == 4 || square.length() == 6) {
        tempprops.setProperty("gridSquare6", square);
        tempprops.setProperty("gridSquare", square.substring(0,4));
      }
      // tempprops.list(System.out);
      mandatoryprops = validator.validateOperation(tempprops);
      for ( int i = 0; i < optemplate.length; i++) {
        if (mandatoryprops.contains(optemplate[i].propskey))
          optemplate[i].label.setForeground(invalid);
        else optemplate[i].label.setForeground(valid);
      }
    }
  }

  public String writeAndExit() {
    gal.actionPerformed();
    String filename = tempprops.getProperty("operationName", "");
    if ( filename.equals("")) filename = tempprops.getProperty("callsign", "") + 
                                       "-" + p.getProperty("cabContestName");
    filename  = filename + ".props";
    File ofile = new File("operations/" + filename);
    try {
      OutputStream os = new FileOutputStream(ofile); 
      tempprops.store(os, "Automatically generated by JL");
      os.close();
    } catch (IOException e2) {
      System.out.println(e2);
      U.die("Couldn't save properties file");
    }
    return filename;
  }

  private Object[] readAbbrevsFromFile(String filename) {
    return readAbbrevsFromFile(filename, true);
  }

  private Object[] readAbbrevsFromFile(String filename, boolean truncate) {
    return readAbbrevsFromFile(filename, truncate, true);
  }

  private Object[] readAbbrevsFromFile(String filename, boolean truncate, boolean uppercase)
  {
    // Reads the values for a combo box from a file.
    // TWO SPECIAL VALUES:  -SORT: don't sort the  entries
    //                      -DX:   don't automatically add DX to the list
    Vector abbrevs = new Vector();
    String nextline;
    boolean adddx = true;
    boolean sort = true;
    try {
      BufferedReader fis = new BufferedReader(new FileReader(filename));
      // load list of entities
      while ( null != (nextline = fis.readLine()) ) {
        if (uppercase) nextline = nextline.toUpperCase();
        if (nextline.length() != 0 ) {
          String s = "";
          if (truncate) // Finds the last token on the line
          // (begs the question of abbrev-first lists, but what the hell)
            for (StringTokenizer st = new StringTokenizer(nextline); 
                 st.hasMoreTokens(); s = st.nextToken()) {}
          else s = nextline;
          if ( s.equals("-DX") ) adddx = false;
          else if ( s.equals("-SORT") ) sort = false;
          else abbrevs.add(s);
        }
      }
      fis.close();
      // System.out.println("read mult list");
    } catch (IOException e) { System.out.println( e );}
    if ( adddx ) abbrevs.add("DX");
    Object [] a = abbrevs.toArray();
    if (sort) Arrays.sort(a);
    return a;
  }

}