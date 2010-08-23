package com.loukides.jl.keyer;

import javax.sound.sampled.*;
import gnu.io.*;

public class PTTController implements LineListener, PortSettable {

  private SerialPort port = null;

  public void setPort(Object o){
    if ( o != null ) port = (SerialPort)o;
    // System.out.println("PTT: Serial port set to: " + port);
  }

  public synchronized void update(LineEvent levt) {
    System.out.println("Line event received");
    if ( port == null ) return; // no serial line; nothing to be done
    if ( levt.getType() == LineEvent.Type.START ) {
      port.setRTS(true);
      // System.out.println("RTS raised");
    } else if ( levt.getType() == LineEvent.Type.STOP ) {
      // System.out.println("RTS lowered");
      port.setRTS(false);
    }
  }  

}