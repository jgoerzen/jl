// initially cloning the "old" keyer UI, but there are a lot of things we could do better:
// * a "record" button
// * one stateful start/stop button
// we are abandoning the "loop" button

package com.loukides.jl.keyer;
import com.loukides.jl.util.U;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;


public class KeyerUI extends JPanel {
  private Keyer keyer = null;
  private JSlider cwRateControl = new JSlider();
  Dimension clipbuttonsize, stopbuttonsize, slidersize;
  private int nclips = 0;  // not used
  private JPanel clippanel = null;
  private boolean leftside = true;

  public KeyerUI(Keyer keyer, Properties props) {
    this.keyer = keyer;
    int bheight = Integer.parseInt(props.getProperty("layout.Keyer.height", "0"));
    int clwidth = Integer.parseInt(props.getProperty("layout.Keyer.clipwidth", "0"));
    int stopwidth = Integer.parseInt(props.getProperty("layout.Keyer.stopwidth", "0"));
    int ratewidth = Integer.parseInt(props.getProperty("layout.Keyer.sliderwidth", "0"));
    int rateheight = Integer.parseInt(props.getProperty("layout.Keyer.sliderheight", "0"));
    int cwmaxspeed = Integer.parseInt(props.getProperty("keyer.cw.maxspeed", "60"));
    int cwminspeed = Integer.parseInt(props.getProperty("keyer.cw.minspeed", "8"));
    int initialspeed = Integer.parseInt(props.getProperty("keyer.cw.initspeed", 
                                                        "" + (cwmaxspeed + cwminspeed)/2));
    nclips = Integer.parseInt(props.getProperty("keyer.numclips", "0"));  // not used
    clipbuttonsize = new Dimension(clwidth, bheight);
    stopbuttonsize = new Dimension(stopwidth,bheight);
    slidersize = new Dimension(ratewidth, rateheight);

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    U.setSizes(cwRateControl, slidersize);
    add(cwRateControl);
    cwRateControl.setMinimum(cwminspeed);
    cwRateControl.setMaximum(cwmaxspeed);
    cwRateControl.addChangeListener(new CWRateChangeListener());
    cwRateControl.setMajorTickSpacing(10);
    cwRateControl.setMinorTickSpacing(5);
    cwRateControl.setPaintLabels(false);
    cwRateControl.setPaintTicks(true);
    cwRateControl.setValue(initialspeed);
    keyer.setCWRate(initialspeed);
    JButton stopper = new JButton(StopMessageAction.getInstance());
    U.setSizes(stopper, clipbuttonsize);
    JButton recorder = new JButton("REC"); 
    recorder.setForeground(Color.red);
    recorder.setEnabled(false); // unimplemented
    U.setSizes(recorder, clipbuttonsize);
    JPanel holder = new JPanel();
    holder.setLayout(new BoxLayout(holder, BoxLayout.X_AXIS));
    holder.add(stopper);
    holder.add(recorder);
    add(holder);
  }

  public void addMessage(boolean repeats, int msgnumber) {
    JButton starter = new JButton(PlayMessageAction.getInstance(msgnumber));
//    JPanel holder = new JPanel();
    U.setSizes(starter, clipbuttonsize);
    if ( leftside ) {
      clippanel = new JPanel();
      clippanel.setLayout(new BoxLayout(clippanel, BoxLayout.X_AXIS));
      add(clippanel);
    } 
    clippanel.add(starter);
    leftside = !leftside;
  }

  private class CWRateChangeListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      keyer.setCWRate(cwRateControl.getValue()); 
    }
  }
}