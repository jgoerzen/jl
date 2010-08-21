// A "map" that shows which grid squares have been worked.
// Also serves as a grid square checker.
// (Probably very bad from an encapsulation p.o.v.; these two just seemed
// to fit together fairly easily)
// Doesn't know about CW (all VHF contests I know don't have perModeMults),
// though it could be added
package com.loukides.jl.checkers;
import com.loukides.jl.util.*;
import com.loukides.jl.jl.*;
import com.loukides.jl.contests.VHFExchange;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

// Really should contain the JFrame as a field;
public class GridSquareMapandChecker extends JFrame implements Checker {
  private HashMap gridlists = new HashMap();
  private static String leftupper = "EN09";
  private static String rightlower = "FM90";
  private HashMap labels;
  private static final char A = 'A';
  private static final char Z = '0';
  private JPanel mappanel = new JPanel();
  private GridBagConstraints gbc = new GridBagConstraints();
  private JTextField bandfield = new JTextField();
  private JLabel bandlabel;
  private String currentband = "";
  private JTextField leftupperfield = new JTextField();
  private JTextField rightlowerfield = new JTextField();
  private JButton redraw = new JButton("Redraw grid");
  private Properties props;
  private boolean updateVisual = true;
  private String bands = null; 

  public GridSquareMapandChecker() {
    this(leftupper, rightlower);
  }

  public GridSquareMapandChecker(String leftupper, String rightlower) { 
    setTitle("Grid square map");
    this.leftupper = leftupper;
    this.rightlower = rightlower;
    // System.out.println("Ranges: " + horizrange + " " + vertrange);
    Container panel = this.getContentPane();
    panel.setLayout(new BorderLayout());
    mappanel.setLayout(new GridBagLayout());
    gbc.insets = new Insets(1,1,1,1);
    Dimension textsize = new Dimension(90, 25);
    U.setSizes(bandfield, textsize);
    U.setSizes(leftupperfield, textsize);
    U.setSizes(rightlowerfield, textsize);
    U.setSizes(redraw, textsize);
    JScrollPane js = new JScrollPane(mappanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                               JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    Dimension scrollpaneSize = new Dimension(650, 370);
    U.setSizes(js.getViewport(), scrollpaneSize);
    panel.add(js, BorderLayout.CENTER);
    JPanel controls = new JPanel();
    controls.setLayout(new FlowLayout(FlowLayout.LEFT));
    U.setSizes(controls, new Dimension(110, 220));
    panel.add(controls, BorderLayout.EAST);
    controls.add(new JLabel("View Band:"));
    bandfield.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String newband = bandfield.getText().toUpperCase();
        newBand(newband);
      }
    });
    controls.add(bandfield);
    controls.add(new JLabel("NW corner:"));
    controls.add(leftupperfield);
    controls.add(new JLabel("SE corner:"));
    controls.add(rightlowerfield);
    redraw.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // System.out.println("redraw called");
        makeGrid(leftupperfield.getText().toUpperCase(),
                 rightlowerfield.getText().toUpperCase());
        newBand(currentband);
      }
    });
    controls.add(redraw);
    makeGrid(leftupper, rightlower);
    pack();
    show();
  }
 
  public void setProperties(Properties props) {
    this.props = props;
    bands = props.getProperty("bands");
  }

  // a helper for populating the grid.  Note that we re-draw on band changes and
  // grid dimension changes
  private void makeGrid(String leftupper, String rightlower) {
    mappanel.removeAll();
    labels = new HashMap();
    leftupperfield.setText(leftupper);
    rightlowerfield.setText(rightlower);
    int leftmost = 10*(leftupper.charAt(0)-A) + (leftupper.charAt(2)-Z);
    int rightmost = 10*(rightlower.charAt(0)-A) + (rightlower.charAt(2)-Z);
    int horizrange = rightmost - leftmost;
    int bottommost = 10*(rightlower.charAt(1)-A) + (rightlower.charAt(3)-Z);
    int topmost = 10*(leftupper.charAt(1)-A) + (leftupper.charAt(3)-Z);
    int vertrange = topmost - bottommost;
    char westchar = leftupper.charAt(0);
    char westnum = leftupper.charAt(2);
    char northchar = leftupper.charAt(1);
    char northnum = leftupper.charAt(3);
    char c2 = northchar;
    char c4 = northnum;
    for (int i = 0; i < vertrange+1; i++) { // off by one?  why?
      char c1 = westchar; 
      char c3 = westnum; 
      for (int j = 0; j < horizrange+1; j++) {
        String name = "" + c1 + c2 + c3 + c4 ; // "" forces string concat
        JLabel b = new JLabel(name);
        gbc.gridx = j; 
        gbc.gridy = i;
        b.setBackground(Color.white);
        mappanel.add(b, gbc);
        labels.put(name.toUpperCase(), b);
        if (c3 != '9') c3++;  // I guess I understand this "off by one" because
        else {                // it's really a "postincrement" situation.
          c3 = '0';
          if (c1 != 'R') c1++;
          else c1 = 'A';
        }
      }
      if (c4 != '0') c4--; // Outer loop--needs to DECREMENT!
      else {
        c4 = '9';
        if (c2 != 'A') c2--;
        else c2 = 'R';
      }
    }
    pack();
    show();
  }


  private void addGrid(String grid, String band) {
    if (band != currentband) { 
      currentband = band;
    }
    String g = grid.toUpperCase().substring(0,4);
    // System.out.println("addGrid: " + g + " " +  band);
    // Get the list of worked grids for the current band.
    HashMap bandlist = (HashMap)gridlists.get(band);
    if (bandlist == null) {  // if the list for the current band doesn't exist,
      bandlist = new HashMap();  // make a new list
      gridlists.put(band, bandlist);
    }
    bandlist.put(g, g); //  hmmm.  should REALLY use a log entry as the value.
    // the remainder adds the grid to the display
    if ( ! updateVisual ) return;  // and isn't needed if we're doing a mass update
    JLabel l = (JLabel)labels.get(g); 
    // System.out.println("Retrieved label: " + l);
    if ( l == null ) return; // nothing to do; presumably outside the range
    l.setBackground(Color.darkGray);
    l.setForeground(Color.green);
  }

  public void clear() {
    gridlists = new HashMap();  // just start from scratch...
  }

  private void clearDisplay() {
    // System.out.println("clearing");
    JLabel b = null;
    for (Iterator labelit = labels.values().iterator();
         labelit.hasNext(); b = (JLabel) labelit.next()) {
      // System.out.println(b);
      if ( b != null) b.setForeground(Color.black);
    }
  }

  private void newBand(String band) {
    if (!updateVisual) return;
    bandfield.setText(band);
    clearDisplay();
    HashMap bandlist = (HashMap)gridlists.get(band);
    // System.out.println(bandlist);
    if (bandlist == null) {
      bandlist = new HashMap();
      gridlists.put(band, bandlist);
    }
    String grid = null;
    for (Iterator it = bandlist.values().iterator();
         it.hasNext(); ) {
      grid = (String) it.next();
      // System.out.println(grid);
      if ( grid != null) addGrid(grid, band);
    }    
  }

  // test the grid square for validity.  Grid squares are of the form ccnn[dd]
  // cc are letters between A and R (inclusive); nn are digits (0-9); 
  // dd are letters between A and X (inclusive).
  // The test is case-insensitive (though traditionally c is uc, d is lc)
  // note that we validate the full 6-char grid, if one is available.
  // I have read that there are grid squares with even finer precisions, but I've
  // never seen them used.  If such exist, they're considered invalid.
  public boolean isValidMult(LogEntry le) {
    String g = ((VHFExchange)le.getRcvd()).getGrid().toUpperCase();
    // System.out.println("valid: " + g);
    int l = g.length();
    if ( l < 4 ) return false;
    if ( l == 4 || l == 6 ) {
      char c1 = g.charAt(0); char c2 = g.charAt(1);
      if ( c1 > 'R' || c1 < 'A' ) return false;
      if ( c2 > 'R' || c2 < 'A' ) return false;
      char c3 = g.charAt(2); char c4 = g.charAt(3);
      if ( ! Character.isDigit(c3) ) return false;
      if ( ! Character.isDigit(c4) ) return false;
    }
    if ( l == 6 ) {
      char c5 = g.charAt(4); char c6 = g.charAt(5);
      // System.out.println("valid: " + c5 + " " + c6);
      if ( c5 > 'X' || c5 < 'A' ) return false;
      if ( c6 > 'X' || c6 < 'A' ) return false;
    } return true;
  }

  public int getTotal() {  // probably not used anywhere HAH!!! Bad assumption
    Iterator it = gridlists.values().iterator();
    int count = 0;
    while ( it.hasNext() ) {
      count += ((HashMap)it.next()).size();
    }
    return count;
  }

  // these two need to make sure the LE is marked appropriately
  public void addEntry(LogEntry le) {
    String band = le.getBand().toUpperCase();
    // System.out.println("addEntry: " + band + " " + le.getRcvd().getMultiplierField());
    if (! band.equals(currentband)) newBand(band);
    addGrid(le.getRcvd().getMultiplierField(), band);
  }

  public boolean isNew(LogEntry le) {
    String band = le.getBand().toUpperCase();
    String grid = le.getRcvd().getMultiplierField();
    if (band != currentband) { 
      currentband = band;
      // bandlabel.setText("Grid Map for: " + currentband);
      newBand(band);
    }
    String g = grid.toUpperCase().substring(0,4);
    // System.out.println("addGrid: " + band + " " +  g);
    HashMap bandlist = (HashMap)gridlists.get(band);
    if (bandlist == null) {
      bandlist = new HashMap();
      gridlists.put(band, bandlist);
    }
    if ( bandlist.get(g) == null ) {
      le.setMultiplier(true);
      return true;
    } else return false;
  }

  // don't think this is needed; updating is fairly quick, and there's no real way
  // to separate updating from everything elses
  public void setUpdateVisual(boolean b) {
    updateVisual = b;
    if (updateVisual) newBand(currentband);
  } 

  public void writeReport(PrintWriter pw) {} // punt for now...

  public Vector getNeeded(LogEntry le) { 
    Vector v = new Vector();  // s/b ArrayList
    StringTokenizer st = new StringTokenizer (bands);
    String cb = null;
    while (st.hasMoreTokens()) {
      cb = st.nextToken();
      HashMap cbmap = (HashMap)gridlists.get(cb.toUpperCase()); // bands ucased in keys (why?)
      if (cbmap == null) v.add(cb);
      else if ( null == (String)cbmap.get(le.getRcvd().getMultiplierField())) v.add(cb);
    }
    // System.out.println(v);
    return v;
  }

}