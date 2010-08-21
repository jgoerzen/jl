// A holder for connection monitors, and whatever UI the server cares to present.
// Can be thrown off by "bulk updates" of single log entries.  This presumably
// won't happen in the final version.
package com.loukides.jl.server;
import com.loukides.jl.gadgets.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class ServerDisplay {
  private JFrame f = new JFrame();
  private Container p;
  private ArrayList monitors = new ArrayList();
  private JPanel myself = new JPanel();
  private JLabel numberLabel = new JLabel();
  private JLabel qLabel = new JLabel();
  private JLabel mLabel = new JLabel();
  private JLabel qptsLabel = new JLabel();
//  private JLabel scoreLabel = new JLabel();

  public ServerDisplay() {
    p = f.getContentPane();
    p.setLayout(new FlowLayout());
    myself.setLayout(new BoxLayout(myself, BoxLayout.Y_AXIS));
    update();
    myself.add(numberLabel); 
    myself.add(qLabel);   
    myself.add(mLabel);   
    myself.add(qptsLabel);   
//    myself.add(scoreLabel);   
    p.add(myself);
    f.pack();
  }

  public void addConnectionMonitor(ConnectionMonitor cm) {
    p.add(cm.getDisplay());
    monitors.add(cm);
    update();
    f.pack();
  }

  public void removeConnectionMonitor(ConnectionMonitor cm) {
   // we keep the old monitors around to save their data;
   // but we toss their individual screen displays
   // (I can see this causing problems when monitors return...)
//    monitors.remove(monitors.indexOf(cm));
    p.remove(cm.getDisplay()); 
    update();
    f.pack();
  }

  public void update() {
    int totalq = 0;
    int totalm = 0;
    int totalqpts = 0;
    numberLabel.setText(monitors.size() + " Loggers");
    for ( int i = 0; i < monitors.size(); i++) {
      totalq += ((ConnectionMonitor)monitors.get(i)).getQCount();
      totalm += ((ConnectionMonitor)monitors.get(i)).getMCount();
      totalqpts += ((ConnectionMonitor)monitors.get(i)).getQPts();
    }
    qLabel.setText("Total QSOs: " + totalq);
    mLabel.setText("Total Mults: " + totalm);
    qptsLabel.setText("Total QSO Pts: " + totalqpts);
//    scoreLabel.setText("Total Score: " + totalm * totalqpts);
  }

  public JFrame getDisplay() { return f; }
}