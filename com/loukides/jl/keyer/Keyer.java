package com.loukides.jl.keyer;
import com.loukides.jl.jl.*;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;

public class Keyer {

  private Properties props = null;
  private KeyerUI ui = null;
  private int numclips = 0;
  private static final String base = "keyer.";
  private HashMap messages = new HashMap();
  // private String audiodir = null;
  private Transceiver tx = null;
  private MainWindow mw = null;
  private int cwrate = 0;

  public Keyer(MainWindow mw, Transceiver tx, Properties p) {
    props = p;
    this.tx = tx;
    this.mw = mw;
    ActionCommon.setPropertiesAndKeyer(this, p);
    ui = new KeyerUI(this, p);
    String portname = props.getProperty(base + "port");
    int baudrate = Integer.parseInt(props.getProperty(base + "baudrate", "1200"));
    int timeout = Integer.parseInt(props.getProperty(base + "timeout", "30"));
    Object port = SerialPortFactory.getSerialPort( portname, baudrate, timeout );
    MessageFactory.setPort(port);
    numclips = Integer.parseInt(p.getProperty(base + "numclips", "0"));
    for (int i = 0; i < numclips; i++) {
      int loopDelay = Integer.parseInt(p.getProperty(base + "loopdelay." + i, "0"));
      // System.out.println("Keyer: " + i + " " + buttonName);
      ui.addMessage(! (loopDelay == 0), i);
      String audioFilename = p.getProperty(base + "ph.clip." + i, "");
      Message m = MessageFactory.getMessage(audioFilename, 
                                                  "PH", loopDelay);
      messages.put(makeKey("PH", i), m);
      String cwtext = p.getProperty(base + "cw.clip." + i, "");
      m = MessageFactory.getMessage(cwtext, "CW", loopDelay);
      messages.put(makeKey("CW", i), m);
    }
    MessageFactory.trytomakeCTSListener(this);
  }

  public JPanel getUI() {
    return ui;
  }

  public void startMessage(int msg) {
    Message m = (Message)messages.get(makeKey(getMode(), msg));
    m.playMessage();
    // System.out.println("Message " + msg + " started");
  }

  public void stopAllMessages() {
    // System.out.println("All messages stopped");
    Iterator allmsgs = messages.values().iterator();
    while (allmsgs.hasNext()) ((Message)allmsgs.next()).stopPlaying();
  }

  private String makeKey(String mode, int num) { return mode + " " + num; }

  public void setCWRate(int rate) {
    cwrate = rate;
    CWMessage.setSpeed(rate);
  }

  public void playArbitraryMessage(String text) {
    // System.out.println("K: " + text);
    Message m = MessageFactory.getMessage(text, getMode(), 0);
    messages.put("ARBITRARY", m);  // so we can stop the message
    m.playMessage();
  }

  private String getMode() {
    String mode = tx.getMode();
    if (mode.equals("")) mode = mw.getUIMode();
    return mode;
  }

}