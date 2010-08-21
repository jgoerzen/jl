// this class makes messages of different types, plus other bits and pieces
// used in the management of messages.
// port is an Object to keep its nature "hidden" from us (and prevent
// bad behavior if the javax.comm API is unavailable); it's really a serial port
// Doesn't deal with the possibility that ph and cw use different ports
package com.loukides.jl.keyer;

import javax.sound.sampled.*;

public class MessageFactory {
  private static Object port = null;
  private static final String base = "com.loukides.jl.keyer.";
  private static KeyerSettable ctsl = null;

  public static void setPort(Object prt) { port = prt; }

  public static Message getMessage(String message, String mode, int loopdelay) {
    Message msg;
    if ( message.equals("")) return new NullMessage();
    String classname =  base + mode + "Message";
    // System.out.println("MF: " + classname);
    try {
      msg = (Message)(Class.forName(classname).newInstance()); 
      msg.setParams(message, port, loopdelay);
    } catch(Exception e) {
      // System.out.println(e.getMessage());
      // System.out.println(e);
      msg = new NullMessage();
    }
    return msg;
  }

  public static void trytomakeCTSListener(Keyer k) {
    try {
      ctsl = (KeyerSettable)(Class.forName(base + "CTSListener").newInstance());
      ctsl.setKeyer(k, port);
    } catch (Throwable t) {
      // System.out.println("makeCTS: " + t);
      System.out.println("No CTS control for audio");
    }
  }

  public static LineListener makePTTController() {
    try {
      // System.out.println("Making PTT controller for port: " + port);
      if (port == null) return null;
      Object obj = Class.forName(base + "PTTController").newInstance();
      ((PortSettable)obj).setPort(port);
      return (LineListener)obj;
    } catch (Throwable t) {
      // System.out.println("makePTT: " + t);
      System.out.println("Unable to control PTT: use vox mode");
      return null;
    }
  }

}