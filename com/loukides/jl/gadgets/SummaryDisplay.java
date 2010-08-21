// A display summarizing contacts and multipliers per band.
// DOES NOT DO MULTI-MODE yet.  (Is this a problem? Not for me...)
// Could be tweaked, and (unfortunately) needs to be added separately to every scorer.
// Adding it is a relatively simple operation, though.
//
// YOU KNOW--you could sleaze by with this.  I can't offhand think of a contest 
// in which perbandmults and permodemults are both true.  (Maybe VHF? QSOPs?)  And the 
// multi-mode contests are usually single-banders (arrl10).
package com.loukides.jl.gadgets;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.io.*;

public class SummaryDisplay extends JPanel implements Rescorable {
  private HashMap displays = new HashMap();  // a hash table of display elements
  private String bands; 
  private Properties props;
  private boolean updateVisual = true;

  public SummaryDisplay(){
    setSize(90,170);
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    JLabel title = new JLabel("QSO Summary");
    title.setForeground(Color.black);
    add(title);
  }

  // As in many of these classes, setProperties() is more of the construtor than the
  // constructor itself.
  public void setProperties(Properties props) {
    this.props = props;
    bands = props.getProperty("bands");
    // System.out.println("setProps:: bands: " + bands);
    StringTokenizer bt = new StringTokenizer(bands);
    while ( bt.hasMoreElements()) {
      String band = bt.nextToken();
      MultDisplayElement mde = new MultDisplayElement(band);
      displays.put( band, mde );
      add( mde.getLabel() );
      // System.out.println("put summary: " + band);
    }
    add( Box.createVerticalGlue());
  }

  public void clear() {
    StringTokenizer bt = new StringTokenizer(bands);
    while ( bt.hasMoreElements()) {
      String band = bt.nextToken();
      MultDisplayElement mde = (MultDisplayElement)displays.get(band);
      mde.clear();
    }
  }

  public void setUpdateVisual(boolean b) {
    updateVisual = b;
    // System.out.println("bands: " + bands);
    StringTokenizer bt = new StringTokenizer(bands);
    while ( bt.hasMoreElements()) {
      String band = bt.nextToken();
      MultDisplayElement mde = (MultDisplayElement)displays.get(band);
      mde.setUpdateVisual(b);
    }
  }

  public void contact(String band, String mode, boolean newmult) {
    // System.out.println("contact: " + band + " " + mode + " " + newmult);
    MultDisplayElement mde = (MultDisplayElement) displays.get(band);
    if (newmult) mde.incrementMults();
    mde.incrementQsos();
  }

  // for the use of contests that let you get 2 mults on one qso, like cqww
  public void extraMult(String band, String mode, boolean newmult) {
    // System.out.println("contact: " + band + " " + mode + " " + newmult);
    MultDisplayElement mde = (MultDisplayElement) displays.get(band);
    if (newmult) mde.incrementMults();
  }

  public void writeReport(PrintWriter p){
    StringTokenizer bt = new StringTokenizer(bands);
    // System.out.println("SD: " + p  +" " + bands + " " + bt + " " + bt.countTokens());
    while ( bt.hasMoreElements()) {
      String band = bt.nextToken();
      MultDisplayElement mde = (MultDisplayElement)displays.get(band);
      if (band.startsWith("b")) band = band.substring(1);
      // System.out.println("SD: " + band + " " + mde);
      p.println( band + U.findPad(band, 5) + 
                 mde.getQsos() + " Q " + 
                 mde.getMults() + " M");
    }
  }

  public void showNeededBands(Vector v) {
    if ( ! updateVisual ) return;
    // System.out.println(v + "|" + bands);
    StringTokenizer bt = new StringTokenizer(bands);
    while ( bt.hasMoreElements()) {
      String band = bt.nextToken();
      MultDisplayElement mde = (MultDisplayElement) displays.get(band);
      mde.showNeededBand(v.contains(band));
    }
  }

  public void showNeededBandsAlternate(Vector v) {
    if ( ! updateVisual ) return;
    // System.out.println(v + "|" + bands);
    StringTokenizer bt = new StringTokenizer(bands);
    while ( bt.hasMoreElements()) {
      String band = bt.nextToken();
      MultDisplayElement mde = (MultDisplayElement) displays.get(band);
      // System.out.println("sNBA:: band: " + band + " " + v.contains(band));
      mde.showNeededBandAlternate(v.contains(band));
    }
  }

}