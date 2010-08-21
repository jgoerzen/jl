package com.loukides.jl.keyer;

import javax.comm.*;

public class CTSListener implements KeyerSettable, SerialPortEventListener {
  private Keyer keyer = null;
  private SerialPort port = null;

  public void setKeyer(Keyer k, Object p) { 
    keyer = k; 
    port = (SerialPort)p;
    try {
      if ( port != null ) {
        port.addEventListener(this); 
        port.notifyOnCTS(true); // we want CTS events 
        // System.out.println("Registered listener for port: " + port);
      }
    } catch (Exception e) { System.out.println("Too many listeners??"); }
  }

  public void serialEvent(SerialPortEvent spe) {
    // System.out.println("Got serial event on: " + port);
    int type = spe.getEventType();
    if ( type != SerialPortEvent.CTS ) return;    // uninteresting event
    if ( port.isCTS() ) {
      keyer.stopAllMessages();
      // System.out.println("Got CTS");
    }
  }
}