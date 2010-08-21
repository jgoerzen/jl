// Connect to an Icom transceiver via a CT-17 serial interface and control it
// Icom *appears* to be fairly general.  This class has only been tested against the 
// IC-746 and IC-765.
//
// The 756PRO series (and the 7800) change the behavior of the VFOs (rather than A and B, they
// "split" mode).  The subclass IcomSplit is for those radios.  Behavior of the 756 (not Pro)
// is unknown
//
// The Icom rigs have a 'CI-V transceive' mode (the default) in which they try 
// to control all other rigs connected to the same interface.  They do it by sending
// GOBS of unsolicited commands on the serial interface.  The only way to get this code
// to work is to turn that mode OFF.  Potentially, you could create a thread that would
// listen to the transceiver babbling, decode all the commands, save the freq, mode,
// and whatever's interesting in state variables; this would give you asynchronous 
// notification of changes to the rig, and is potentially a nice idea.  But that's
// not what I've done.
//
// because CI-V is just a one-wire bus, we always get two messages back from the 
// interface:  the original command itself, and the transceiver's response to the 
// command.  Note that the locations in the buffer
// are hardwired, which is prone to error.  
//
// The IC746 and 765 respond rather sluggishly to commands.  The ProIII is a lot
// peppier.
// 
// The IC746 (and other late Icom radios) have some complex memory
// setting commands  lumped under 0x1a that allow you to set/read the memory with one
// command.  However, they don't really do what we want; you have to supply 
// all the freqs you care about (on set), and on read, they give you back data, 
// without setting the VFO.  So they can't be used to optimize rig operations.
//
// Debug is an int; 0 is off, bigger numbers enable more printfs for debugging.
//
package com.loukides.jl.transceivers;
import com.loukides.jl.util.*;
import com.loukides.jl.jl.*;

import java.util.*;
import javax.comm.*;
import java.io.*;

public class Icom extends AbstractTransceiver {
  private String portName = "";
  private int timeout = 30;
  private static final int READSIZE = 20;

  // icom command stuff
  protected static final byte readfreqCommand = (byte)0x03;
  protected static final byte readmodeCommand = (byte)0x04;
  protected static final byte setfreqCommand = (byte) 0x05;
  protected static final byte vfoCommand = (byte) 0x07;
  protected static final byte syncvfoABSubcommand = (byte) 0xa0;
  protected static final byte setvfoASubcommand = (byte) 0x00;
  protected static final byte setvfoBSubcommand = (byte) 0x01;
  protected static final byte swapvfoABSubcommand = (byte) 0xB0;
  protected static final byte splitCommand = (byte) 0x0f;
  protected static final byte setsplitSubcommand = (byte) 0x01;
  protected static final byte clearsplitSubcommand = (byte) 0x00;
  protected static final byte selectmemCommand = (byte) 0x08;
  protected static final byte loadmemCommand = (byte) 0x09;
  protected static final byte readmemCommand = (byte) 0x0A;
  //Commands for Icom756Pro series (and 7800)
  protected static final byte setMainvfoSubcommand = (byte) 0xD0;
  protected static final byte setSubvfoSubcommand = (byte) 0xD1;

  protected static final byte preamble = (byte) 0xfe;
  protected static final byte terminal = (byte) 0xfd;
  protected static final byte ack = (byte) 0xfb;
  protected static final byte error = (byte) 0xfa;
  protected static final byte [] resultbuf = new byte[READSIZE];

  // prebuilt commands
  protected static byte [] READFREQCOMMAND =  { preamble, preamble, 
                      (byte) 0, (byte) 0, // transceiver, controller
                       readfreqCommand, terminal };
  protected static byte [] READMODECOMMAND =  { preamble, preamble, 
                      (byte) 0, (byte) 0, // transceiver, controller
                       readmodeCommand, terminal };
  protected static byte [] SYNCVFOCOMMAND =  { preamble, preamble, 
                      (byte) 0, (byte) 0, // transceiver, controller
                       vfoCommand, syncvfoABSubcommand, terminal };
  protected static byte [] SWAPVFOCOMMAND =  { preamble, preamble, 
                      (byte) 0, (byte) 0, // transceiver, controller
                       vfoCommand, swapvfoABSubcommand, terminal };
  protected static byte [] SETSPLITCOMMAND =  { preamble, preamble, 
                      (byte) 0, (byte) 0, // transceiver, controller
                       splitCommand, setsplitSubcommand, terminal };
  protected static byte [] CLEARSPLITCOMMAND =  { preamble, preamble, 
                      (byte) 0, (byte) 0, // transceiver, controller
                       splitCommand, clearsplitSubcommand, terminal };
  protected static byte [] SETFREQCOMMAND = { preamble, preamble, 
                      (byte)0, (byte)0, setfreqCommand,  // Header + command
                      (byte)0, (byte)0, (byte)0, (byte)0, (byte)0,           // data area
                      terminal };                                            // terminal code
  protected static byte [] SELECTMEMCOMMAND =  { preamble, preamble, 
                      (byte) 0, (byte) 0, // transceiver, controller
                       selectmemCommand, (byte) 0, terminal }; // command, mem#, terminal
  protected static byte [] LOADMEMCOMMAND =  { preamble, preamble, 
                      (byte) 0, (byte) 0, // transceiver, controller
                       loadmemCommand, terminal };
  protected static byte [] READMEMCOMMAND =  { preamble, preamble, 
                      (byte) 0, (byte) 0, // transceiver, controller
                       readmemCommand, terminal };
  protected static byte [] SETVFOACOMMAND =  { preamble, preamble, 
                      (byte) 0, (byte) 0, // transceiver, controller
                       vfoCommand, setvfoASubcommand, terminal };
  protected static byte [] SETVFOBCOMMAND =  { preamble, preamble, 
                      (byte) 0, (byte) 0, // transceiver, controller
                       vfoCommand, setvfoBSubcommand, terminal };
  // Prebuilt commands for 756Pro series, and 7800
  protected static byte [] SETMAINVFOCOMMAND =  { preamble, preamble, 
                      (byte) 0, (byte) 0, // transceiver, controller
                       vfoCommand, setMainvfoSubcommand, terminal };
  protected static byte [] SETSUBVFOCOMMAND =  { preamble, preamble, 
                      (byte) 0, (byte) 0, // transceiver, controller
                       vfoCommand, setSubvfoSubcommand, terminal };

  // lengths of expected results
  protected static final int READFREQLENGTH = 17;
  protected static final int READMODELENGTH = 14;
  protected static final int SETFREQLENGTH = 17;
  protected static final int SYNCVFOLENGTH = 13;  
  protected static final int SETVFOLENGTH = 13;  // for setVFO A/B, setVfo main/sub
  protected static final int SWAPVFOLENGTH = 13;
  protected static final int SETSPLITLENGTH = 13;
  protected static final int CLEARSPLITLENGTH = 13;
  protected static final int SELECTMEMLENGTH = 13;
  protected static final int LOADMEMLENGTH = 12;
  protected static final int READMEMLENGTH = 12;

  private int baudrate = 1200;
  private static byte transceiver;
  private static byte controller;
  private CommPortIdentifier portID;
  private SerialPort port; 
  private DataInputStream is;
  private DataOutputStream os;
  protected int debug; 

  public Icom() {}

  // As is often the case in this program, most of the initialization 
  // happens in setProperties
  // Could conceivably open the port in the superclass (except--don't take it for granted
  // that all transceivers will talk serial)
  public void setProperties(Properties p) throws Exception {
    super.setProperties(p);
    // properties and such
    portName = p.getProperty("transceiver.port");
    timeout = Integer.parseInt(p.getProperty("transceiver.timeout", "30"));
    String tprop = p.getProperty("transceiver.address");
    transceiver = Integer.decode(tprop).byteValue();
    String cprop = p.getProperty("transceiver.interface", "COM1");
    controller = Integer.decode(cprop).byteValue();
    baudrate = Integer.parseInt(p.getProperty("transceiver.baudrate", "1200"));
    debug = Integer.parseInt(p.getProperty("transceiver.debug", "0"));
    // System.out.println("Icom: "+portName+" "+timeout+" "+transceiver
    //                           +" "+controller+" "+baudrate);
    // can't be statically initialized because we are getting the values from props
    READFREQCOMMAND[2] = transceiver;      READFREQCOMMAND[3] = controller;
    READMODECOMMAND[2] = transceiver;      READMODECOMMAND[3] = controller; 
    SETFREQCOMMAND[2] =  transceiver;      SETFREQCOMMAND[3] =  controller;
    SYNCVFOCOMMAND[2] =  transceiver;      SYNCVFOCOMMAND[3] =  controller;
    SETVFOACOMMAND[2] =  transceiver;      SETVFOACOMMAND[3] =  controller;
    SETVFOBCOMMAND[2] =  transceiver;      SETVFOBCOMMAND[3] =  controller;
    SWAPVFOCOMMAND[2] =  transceiver;      SWAPVFOCOMMAND[3] =  controller;
    SETSPLITCOMMAND[2] = transceiver;      SETSPLITCOMMAND[3] = controller;
    CLEARSPLITCOMMAND[2] = transceiver;    CLEARSPLITCOMMAND[3] = controller;
    SELECTMEMCOMMAND[2] = transceiver;     SELECTMEMCOMMAND[3] = controller; 
    LOADMEMCOMMAND[2] = transceiver;       LOADMEMCOMMAND[3] = controller; 
    READMEMCOMMAND[2] = transceiver;       READMEMCOMMAND[3] = controller; 
    SETMAINVFOCOMMAND[2] = transceiver;    SETMAINVFOCOMMAND[3] = controller;
    SETSUBVFOCOMMAND[2] = transceiver;     SETSUBVFOCOMMAND[3] = controller;
    port = (SerialPort)SerialPortFactory.getSerialPort(portName, baudrate, timeout);
    if (port == null) throw new Exception("Can't get a serial port");
    // port.enableReceiveTimeout(2000);  // appears to screw things up, not sure why
    os = new DataOutputStream(port.getOutputStream());
    is = new DataInputStream(port.getInputStream());
    if ( getMode().equals("") ) throw new Exception("serial port appears disconnected");
  }

  public void storeMemory(int mem) {
    int bytes;
    String intstr = Integer.toString(mem);
    if (intstr.length() == 1) intstr = "0" + intstr;
    else if (intstr.length() == 0) intstr = "00";
    else if (intstr.length() > 2) intstr = intstr.substring(0,2);
    byte bcdmem = bcdEncodeDigits( intstr.substring(0,1), intstr.substring(1,2));
    SELECTMEMCOMMAND[5] = bcdmem;
    bytes = doCommand(SELECTMEMCOMMAND, SELECTMEMLENGTH);
    bytes = doCommand(LOADMEMCOMMAND, LOADMEMLENGTH);
  }

  public void setVFOFromMemory(int mem) {
    int bytes;
    String intstr = Integer.toString(mem);
    if (intstr.length() == 1) intstr = "0" + intstr;
    else if (intstr.length() == 0) intstr = "00";
    else if (intstr.length() > 2) intstr = intstr.substring(0,2);
    byte bcdmem = bcdEncodeDigits( intstr.substring(0,1), intstr.substring(1,2));
    SELECTMEMCOMMAND[5] = bcdmem;
    bytes = doCommand(SELECTMEMCOMMAND, SELECTMEMLENGTH);
    bytes = doCommand(READMEMCOMMAND, READMEMLENGTH);
  }  

  public void setSplit(float f) {
    int bytes;
    if ( f == 0.0f ) {
      bytes = doCommand(CLEARSPLITCOMMAND, CLEARSPLITLENGTH);
      return;
    }
    bytes = doCommand(SYNCVFOCOMMAND, SYNCVFOLENGTH); 
    System.arraycopy(floatToBytes(f), 0, SETFREQCOMMAND, 5, 5);  
    bytes = doCommand(SETVFOBCOMMAND, SETVFOLENGTH);             // this code works for IC-765
    bytes = doCommand(SETFREQCOMMAND, SETFREQLENGTH);            // which doesn't have SWAPVFO
    bytes = doCommand(SETVFOACOMMAND, SETVFOLENGTH);
    bytes = doCommand(SETSPLITCOMMAND, SETSPLITLENGTH);
  }

  public void setFrequency(float f) {
    System.arraycopy(floatToBytes(f), 0, SETFREQCOMMAND, 5, 5);
    int bytes = doCommand(SETFREQCOMMAND, SETFREQLENGTH);
    // no error checking???
  }

  public float getFrequency() {
    int bytes = doCommand(READFREQCOMMAND, READFREQLENGTH);
    if (bytes == 0) return 0.0f;
    // convert the frequency (encoded as BCD) into a float
    int freqx1000 =     byteToInt(resultbuf[15]) * 100000000 +
                        byteToInt(resultbuf[14]) * 1000000+
                        byteToInt(resultbuf[13]) * 10000+
                        byteToInt(resultbuf[12]) * 100+
                        byteToInt(resultbuf[11]) * 1;
    float freq = ((float)freqx1000)/1000;
    // System.out.println("Read frequency: " + freq);
    return freq;    
  } 

  public String getMode() {
    int bytes = doCommand(READMODECOMMAND, READMODELENGTH);
    if (bytes == 0) return "";
    byte mode = resultbuf[11];
    // System.out.println("Mode: " + mode);
    if (mode == 0 || mode == 1 || mode == 2) return "PH"; // lsb, usb, am
    if (mode == 3) return "CW";
    if (mode == 4) return "RY"; // rtty (how to handle digital?)
    if (mode == 5) return "PH"; // fm
    return "";
  } 

  protected int doCommand(byte [] command, int length) {
    int bytes;
    try {
      os.write(command, 0, command.length);
      bytes = is.read(resultbuf, 0, length);
      if (debug  >  0)
	    System.out.println("Sent command " + byteToHex(command[5]) + "length: " + command.length);
      if (debug > 10) {
        for ( int i = 0; i < command.length; i++ ) 
          System.out.println("byte " + i + ": " + byteToHex(command[i]));      
          System.out.println("Read " + bytes + "bytes from transceiver");
        for ( int i = 0; i < bytes; i++ ) 
          System.out.println("byte " + i + ": " + byteToHex(resultbuf[i]));
	  }
    } catch (Exception e) {
      System.out.println("error during transceiver read");
      // e.printStackTrace();
      return 0;
    }
    if (resultbuf[bytes -1] != terminal || resultbuf[bytes -2] == error || bytes != length){
      System.out.println("error for command: " + 
		byteToHex(command[5]) + " rcvd " + bytes + " bytes, expected " + length);
      return 0;
    }
    return bytes;
  }

  protected static byte[] floatToBytes(float f) {
    String s = U.freqpad.format((double)f);
    // System.out.println(s);
    return floatToBytes(s);
  }

  // Convert a floating point (decimal point) number into a BCD string
  private static byte[] floatToBytes(String f) {
    byte [] bytes = new byte[5];
    bytes[0] = bcdEncodeDigits(f.substring(9,10), f.substring(10));
    bytes[1] = bcdEncodeDigits(f.substring(6,7),  f.substring(8,9));
    bytes[2] = bcdEncodeDigits(f.substring(4,5), f.substring(5,6));
    bytes[3] = bcdEncodeDigits(f.substring(2,3), f.substring(3,4));
    bytes[4] = bcdEncodeDigits(f.substring(0,1), f.substring(1,2));
    return bytes;
  }

  // Convert two (single) decimal digits into a BCD byte
  private static byte bcdEncodeDigits(String c1, String c2) {
    int b1 = Integer.parseInt(c1) << 4;
    int b2 = Integer.parseInt(c2);
    int result =  (b1 | b2);
    return (byte)result;
  }

  // Convert a BCD byte into the corresponding int value.  (MISNAMED)
  private int byteToInt(byte b) {
    int nibble1 = b & (byte) 0x0f;
    int nibble2 = (b >> 4) & (byte) 0x0f;
    return nibble2*10 + nibble1;
  }

  // Convert a byte to the corresponding two hex characters
  private static String byteToHex(byte b) {
    int nibble1 = b & (byte) 0x0f;
    int nibble2 = (b >> 4) & (byte) 0x0f;
    String digit1 = hexNibbleToString(nibble1);
    String digit2 = hexNibbleToString(nibble2);
    return digit2 + digit1;
  }

  private static String hexNibbleToString(int b) {
    if ( b == 10 ) return "A";
    if ( b == 11 ) return "B";
    if ( b == 12 ) return "C";
    if ( b == 13 ) return "D";
    if ( b == 14 ) return "E";
    if ( b == 15 ) return "F";
    if ( b < 10 && b >= 0 ) return Integer.toString(b);
    else return "-1"; 
  }
}
