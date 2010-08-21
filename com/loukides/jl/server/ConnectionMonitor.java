// A display to show the status of and information about a log client.
package com.loukides.jl.server;
import com.loukides.jl.jl.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.net.*;

public class ConnectionMonitor {
  private JPanel panel = new JPanel();
  private JLabel lastHeard = new JLabel();
  private JLabel number = new JLabel();
  private JLabel multLabel = new JLabel();
  private JLabel qsoptsLabel = new JLabel();
  private int nqs = 0;
  private int mults = 0;
  private int qpts = 0;

  public ConnectionMonitor(InetAddress a) {
    panel.setLayout( new BoxLayout(panel, BoxLayout.Y_AXIS ));
    panel.add(new JLabel("Connect: " + new Date()));
    panel.add(new JLabel("Address: " + a));
    panel.add(lastHeard);
    panel.add(number);
    panel.add(multLabel);
    panel.add(qsoptsLabel);
  }

  public Component getDisplay() { return panel; }

  public void update(LogEntry le) {
    lastHeard.setText("Updated: " + new Date());
    nqs++;
    qpts += le.getQsoPoints();
    if (le.isMultiplier()) mults++;        // not sure we're doing this right...
    if (le.isCountryMultiplier()) mults++; // 
    number.setText("Qs: " + nqs);
    multLabel.setText("Mults: " + mults);
    qsoptsLabel.setText("QSOP: " + qpts);   
  }

  public int getQCount() { return nqs; }

  public int getMCount() { return mults; }
  
  public int getQPts() { return qpts; }

}