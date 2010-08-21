package com.loukides.jl.transceivers;
import com.loukides.jl.util.*;
import com.loukides.jl.jl.*;

import java.util.*;
import javax.comm.*; // Change to gnu.io if using rxtx for communications
import java.io.*;

import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;

/**
A class to implement Elecraft K2 rig control for jl
*/
public class ElecraftK2 extends AbstractTransceiver {

  private InputStream is;
  private OutputStream os;
  private DecimalFormat frequencyFormat = new DecimalFormat("00000000000");
  private DecimalFormat memoryFormat = new DecimalFormat("00");
  private FieldPosition pos = new FieldPosition(NumberFormat.INTEGER_FIELD);
  private String portName = "";
  private int timeout = 30;
  private int baudrate = 4800;


  public ElecraftK2() {}

  public void setProperties(Properties p) throws Exception {
     super.setProperties(p);
     portName = p.getProperty("transceiver.port","/dev/ttyS0");
     baudrate = Integer.parseInt(p.getProperty("transceiver.baudrate","4800"));
     timeout = Integer.parseInt(p.getProperty("transceiver.timeout", "30"));
     SerialPort serialPort =
        (SerialPort)SerialPortFactory.getSerialPort(portName,4800,30);
     is = serialPort.getInputStream();
     os = serialPort.getOutputStream();
     try {
        os.write("k22;".getBytes()); //K2 Extended mode
     } catch (Exception e) {e.printStackTrace(); }
  }

/**
Store the current state of the K2 in one of its memories.
@param mem The K2 memory in which to store the current state. Only values
0 to 9 are legal.
*/
   public void storeMemory(int mem) {
      StringBuffer memoryCommand = new StringBuffer("sw19;sw");
      memoryFormat.format(mem+7,memoryCommand,pos);
      memoryCommand.append(";");
      try {
         os.write(memoryCommand.toString().getBytes());
         os.flush();
      } catch (Exception ee) {
         ee.printStackTrace();
      }
   }

/**
Recalls the current state of the K2 from one of its memories.
Note this will give different behavior than the jl docs, since, for
example, it will remember if the rig is in split, etc.
@param mem The K2 memory from which to recall the state. Only values
0 to 9 are legal.
*/
   public void setVFOFromMemory(int mem) {
      StringBuffer memoryCommand = new StringBuffer("sw17;sw");
      memoryFormat.format(mem+7,memoryCommand,pos);
      memoryCommand.append(";");
      try {
         os.write(memoryCommand.toString().getBytes());
         os.flush();
      } catch (Exception ee) {
         ee.printStackTrace();
      }
   }

/**
Set transmit frequency for split operation.
@param f the transmit frequency in KHz. If zero cancel split.
*/
   public void setSplit(float f) {
      StringBuffer frequencyCommand;
      int irx = getRxVFO();
      if (f == 0.0) {
         setRxVFO(irx); //cancel split
         return;
      }
      frequencyCommand = new StringBuffer("ft");
      if (irx == 0) {
         frequencyCommand.append("1;fb");
      } else {
         frequencyCommand.append("0;fa");
      }
      frequencyFormat.format((long) (f*1000.0f),frequencyCommand,pos);
      frequencyCommand.append(";");
      try {
         os.write(frequencyCommand.toString().getBytes());
         os.flush();
      } catch (Exception ee) {
         ee.printStackTrace();
      }
   }

/**
Cancel split and set the current receive VFO to f.
@param f The frequency in KHz.
*/
   public void setFrequency(float f) {
      StringBuffer frequencyCommand;
      int irx = getRxVFO();
      setRxVFO(irx); //cancels split
      if (irx == 0) {
         frequencyCommand = new StringBuffer("fa");
      } else {
         frequencyCommand = new StringBuffer("fb");
      }
      frequencyFormat.format((long) (f*1000.0f),frequencyCommand,pos);
      frequencyCommand.append(";");
      try {
         os.write(frequencyCommand.toString().getBytes());
         os.flush();
      } catch (Exception ee) {
         ee.printStackTrace();
      }
   }

/**
Return 0 if the current receive VFO is A, 1 if B
*/
   public int getRxVFO() {
      byte[] b = new byte[4];
      try {
         while (is.available() > 0) is.read();
         os.write("fr;".getBytes());
         os.flush();
         for (int i=0;i<4;i++) {
            b[i] = (byte) is.read();
         }
      } catch (Exception ee) {
         ee.printStackTrace();
      }
      return (int) (b[2]-0x30); //0 for VFO-A, 1 for VFO-B
   }

/**
Set the current receive VFO, cancelling any split operation.
@param vfo 0 for VFO A, 1 for VFO B
*/
   public void setRxVFO(int vfo) { //cancels split
      byte[] b = new byte[4];
      b = "frX;".getBytes();
      b[2] = (byte) ((vfo == 0)?0x30:0x31);
      try {
         while (is.available() > 0) is.read();
         os.write(b);
         os.flush();
      } catch (Exception ee) {
         ee.printStackTrace();
      }
   }

/**
Get the frequency in KHz of the current receive VFO.
*/
   public float getFrequency() {
      byte[] b = new byte[14];
      int irx = getRxVFO(); //get current vfo (also cancels split)
      try {
         while (is.available() > 0) is.read();
         if (irx == 0) {
            os.write("fa;".getBytes());
         } else {
            os.write("fb;".getBytes());
         }
         os.flush();
         for (int i=0;i<14;i++) {
            b[i] = (byte) is.read();
         }
      } catch (Exception ee) {
         ee.printStackTrace();
      }
      return new Float(new String(b,2,11)).floatValue()*0.001f;
  } 

/**
Get the Mode. The possible returned values are CW, PH, or RY.
*/
  public String getMode() {
      byte[] b = new byte[4];
      try {
         while (is.available() > 0) is.read();
         os.write("md;".getBytes());
         os.flush();
         for (int i=0;i<4;i++) {
            b[i] = (byte) is.read();
         }
      } catch (Exception ee) {
         ee.printStackTrace();
      }
      int i = new Integer(new String(b,2,1)).intValue();
      switch (i) {
         case 1: //lsb
         case 2: //usb
            return "PH";
         case 3: //cw
         case 7: //cw reverse
            return "CW";
         case 6: //rtty
         case 9: //rtty reverse
            return "RY";
         default:
            return "";
      }
   } 

}
