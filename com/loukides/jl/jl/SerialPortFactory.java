// Centralize creation of serial ports.  The motivation for this is to support
// other devices (like a rig blaster) that we need to talk to, but that aren't 
// transceivers.  It also allows the possibility of "overloading" a port--i.e.,
// having two or modules talk to the same port.  I want this *specifically* to 
// talk to a RigBlaster and a transceiver on the same line.  And it would come in
// handy for supporting other devices (i.e., rotors, antenna switches), though 
// these currently aren't in the cards.
// This class should also support distribution of serial events.  (The 
// javax.comm API only allows one listener, which may not be enough.)
package com.loukides.jl.jl;

import gnu.io.*;
import java.util.*;

public class SerialPortFactory {
  private static HashMap ports = new HashMap(8);

  // make sure only one serial port can exist for any port
  // return an Object so this method can be called by classes without knowledge
  // of javax.comm.  (Does this work?)
  public static Object getSerialPort(String portname, int baudrate, int timeout) 
  {
    try {
      SerialPort port;
      port = (SerialPort)ports.get(portname);
      if ( port == null ) {
        System.out.println("Opening serial port: " + portname);
        CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portname);
        port = (SerialPort)portID.open("JL", timeout);
        port.setSerialPortParams(baudrate, port.getDataBits(), 
                                 port.getStopBits(), port.getParity());
        port.setRTS(false);     // RTS seems to come up high, which keys the transmitter (SSB).
        port.setDTR(false);     // DTR seems to come up high, which keys the transmitter (CW).
        ports.put(portname, port);
      }
      return port;
    } catch ( Throwable e ) {
      return null;
    }
  }

  public static Object getSerialPort(String portname) {
    return getSerialPort(portname, 9600, 10);
  }

}