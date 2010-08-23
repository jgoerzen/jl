// Generator for CW messages on a serial port.
// Special chars:
// =: BT
// *: SK
// ^: AR
// Makes the following text substitutions:
// $n: serial number
// $x: complete outgoing exchange
// $f: (from) my callsign
// $t: (to) other station's callsign
// $?: whatever's in the main entry field (presumably a partial callsign) followed by "?"
// NB:  The property "keyer.cw.useCutZerosInQTCs" is actually consumed by QTCLine
package com.loukides.jl.keyer;
import com.loukides.jl.jl.LogEntry;

import gnu.io.*;  // This message type can know about javax.comm, because it's meaningless
                      // without a serial port.
import java.util.*;
import javax.swing.JTextField;

public class CWMessage extends MessageCommon {

  private String text = null;
  private static SerialPort port = null;
  private int loopdelay = 0;

  private static final HashMap morse = new HashMap();
  private static final char DOT = '.';
  private static final char DASH = '_';
  private static final Character SPACE = new Character(' ');

  private static double dotTime = 0.05; // seconds??
  private static double dashDotRatio = 3.0;
  private static double markSpaceRatio = 1.0;
  private static double charSpaceRatio = 3.0;
  private static double wordSpaceRatio = 9.0;
  private static boolean cutSerials = false;
  private static boolean truncSerials = false;
  private static String oneCharSerialPad = "0", twoCharSerialPad = "00";
  private static boolean showCW = false;

  private static long dotMillis = (long) (dotTime * 1000);
  private static long dashMillis = (long) (dotTime * dashDotRatio * 1000);
  private static long markSpaceMillis = (long) (dotTime * markSpaceRatio * 1000);
  private static long charSpaceMillis = (long) (dotTime * charSpaceRatio * 1000);
  private static long wordSpaceMillis = (long) (dotTime * wordSpaceRatio * 1000);

  static {  // perhaps not the best way to represent morse, but it's easy to deal with
    morse.put(new Character('a'), "._");
    morse.put(new Character('b'), "_...");
    morse.put(new Character('c'), "_._.");
    morse.put(new Character('d'), "_..");
    morse.put(new Character('e'), ".");
    morse.put(new Character('f'), ".._.");
    morse.put(new Character('g'), "__.");
    morse.put(new Character('h'), "....");
    morse.put(new Character('i'), "..");
    morse.put(new Character('j'), ".___");
    morse.put(new Character('k'), "_._");
    morse.put(new Character('l'), "._..");
    morse.put(new Character('m'), "__");
    morse.put(new Character('n'), "_.");
    morse.put(new Character('o'), "___");
    morse.put(new Character('p'), ".__.");
    morse.put(new Character('q'), "__._");
    morse.put(new Character('r'), "._.");
    morse.put(new Character('s'), "...");
    morse.put(new Character('t'), "_");
    morse.put(new Character('u'), ".._");
    morse.put(new Character('v'), "..._");
    morse.put(new Character('w'), ".__");
    morse.put(new Character('x'), "_.._");
    morse.put(new Character('y'), "_.__");
    morse.put(new Character('z'), "__..");

    morse.put(new Character('0'), "_____");
    morse.put(new Character('1'), ".____");
    morse.put(new Character('2'), "..___");
    morse.put(new Character('3'), "...__");
    morse.put(new Character('4'), "...._");
    morse.put(new Character('5'), ".....");
    morse.put(new Character('6'), "_....");
    morse.put(new Character('7'), "__...");
    morse.put(new Character('8'), "___..");
    morse.put(new Character('9'), "____.");

    morse.put(new Character('/'), "_.._.");
    morse.put(new Character('?'), "..__..");
    morse.put(new Character(','), "__..__");
    morse.put(new Character('.'), "._._._");
    morse.put(new Character('-'), "._...");  // wait; interpretation as - arguably wrong
    morse.put(new Character('='), "_..._");  // BT (=?  As per ARRL Handbook)
    morse.put(new Character(':'), "___...");
    morse.put(new Character(';'), "_._._.");
    morse.put(new Character('('), "_.__.");
    morse.put(new Character(')'), "_.__._");
    morse.put(new Character('*'), "..._._");  // SK (unnatural)
    morse.put(new Character('^'), "._._.");   // AR (unnatural)
  }

  public CWMessage() {
    String base = "keyer.cw.";
    dashDotRatio = Double.parseDouble(props.getProperty(base + "dashDotRatio", "3.0"));
    markSpaceRatio = Double.parseDouble(props.getProperty(base + "markSpaceRatio", "1.0"));
    charSpaceRatio = Double.parseDouble(props.getProperty(base + "charSpaceRatio", "3.0"));
    wordSpaceRatio = Double.parseDouble(props.getProperty(base + "wordSpaceRatio", "9.0"));
    showCW = props.getProperty(base + "showCWOnConsole", "false").equals("true");
    cutSerials = Boolean.valueOf(props.getProperty(base + "useCutZerosInSerials", "false"))
                        .booleanValue();
    truncSerials = Boolean.valueOf(props.getProperty(base + "truncateZerosInSerials", "false"))
                        .booleanValue();
    if (cutSerials) { 
      oneCharSerialPad = "t"; twoCharSerialPad = "tt";  // must be lc
    } else if (truncSerials) {
      oneCharSerialPad = ""; twoCharSerialPad = "";
    } else {
      oneCharSerialPad = "0"; twoCharSerialPad = "00";
    }    
  }

  public void playMessage() {
    if ( player != null && player.isAlive() ) player.interrupt();  // stop anything playing
    player = new DTR_CWSignaller();
    player.start();   // start new msg
  }

  public void stopPlaying() {
    if (player == null) return;
    if (player.isAlive()) player.interrupt();
  }

  public void setParams(String message, Object p, int loopdelay) throws Exception {
    port = (SerialPort) p;
    this.loopdelay = loopdelay;
    this.text = message.toLowerCase();
    // System.out.println("CW port: " + port);
  }

  public static void setSpeed(int wpm) {
    // Speed (wpm) is 2.4/dottime (per formula in handbook, located by n2mg and other ycccers;
    // dottime in seconds).  This formula includes both
    // the dot and the following space, so the actual dot time is half this value.
    // Note that this is *independent* of other factors, like the various ratios.
    dotTime = 1.2 / (double)wpm;
    dotMillis = (long) (dotTime * 1000);
    dashMillis = (long) (dotTime * dashDotRatio * 1000);
    markSpaceMillis = (long) (dotTime * markSpaceRatio * 1000);
    charSpaceMillis = (long) (dotTime * charSpaceRatio * 1000);
    wordSpaceMillis = (long) (dotTime * wordSpaceRatio * 1000);
  }

  // the class that actually sends the CW.  NOTE:: there could be other kinds of 
  // signallers (e.g., using a parallel port, using other signals)
  private class DTR_CWSignaller extends Thread {
    public void run() {
      this.setPriority(Thread.MAX_PRIORITY -1); 
      try {
        if (loopdelay == 0) playCW(makeSubstitutions(text));
        else while (true) {
          playCW(makeSubstitutions(text));
          sleep((long)loopdelay*1000);
        }
      } catch (InterruptedException e) {
        // System.out.println("CW message terminated");
        if (port != null) port.setDTR(false); // make sure we end up with the key up!
      }
    }

    private void playCW(String finaltext) throws InterruptedException {
      // 'all but the last' logic preserves the ratios between the 
      // different spaces by avoiding playing two spaces in a row.
      for ( int i = 0; i < finaltext.length(); i++ ) {
        Character c = new Character(finaltext.charAt(i)); // WHY is this Character, not char?
        if ( c.equals(SPACE) ) playWordSpace(); 
        else {
          String dotsndashes = (String)morse.get(c);   // c must be an Object to be a hashkey
          for ( int j = 0; j < dotsndashes.length(); j++ ) {
            char ch = dotsndashes.charAt(j);
            if ( ch == DOT ) playDot();
            if ( ch == DASH ) playDash();
            if ( !(j == dotsndashes.length() -1)) playMarkSpace(); // all but the last mark
          }
          if ( (i+1 < finaltext.length() ) && finaltext.charAt(i+1) != ' ' ) 
            playCharSpace();  // lookahead for word space
        }
      }
    }

    // port != null only useful for testing w/o a real port.  Normally, shouldn't
    // even get here.
    private void playDot() throws InterruptedException {
      if (showCW) System.out.print(".");
      if (port != null) port.setDTR(true);
      sleep(dotMillis);
      if (port != null) port.setDTR(false);
    };

    private void playDash() throws InterruptedException {
      if (showCW) System.out.print("-"); 
      if (port != null) port.setDTR(true);
      sleep(dashMillis);
      if (port != null) port.setDTR(false);
    };

    private void playMarkSpace() throws InterruptedException {
      // System.out.print("x");
      sleep(markSpaceMillis);
    }

    private void playCharSpace() throws InterruptedException {
      if (showCW) System.out.print("|");
      sleep(charSpaceMillis);
    };

    private void playWordSpace() throws InterruptedException {
      if (showCW) System.out.print(" ");
      sleep(wordSpaceMillis);
    };

    // parses and substitutes escapes.  Valid escapes listed at top of page
    // can cut leading zeros in serial numbers.  Doesn't cut anything else.
    // (should it?  I think not.  N for 9 in a serial number is just rude.)
    // There's no way to get a signal report and know with confidence that it's a 
    // signal report and nothing else, so cut chars aren't used elsewhere.
    private String makeSubstitutions(String text) {  // UGLY recursion
      int dollar = text.indexOf('$');
      if (dollar == -1) return text;
      String part1 = text.substring(0, dollar);
      String part2 = text.substring(dollar, dollar+2);
      String part3 = text.substring(dollar+2);
      // System.out.println("Sub: " + part1 + ":" + part2 + ":" + part3 + ":");
      if (part2.charAt(1) == 'n') {  
        // cheap, stupid padding with leading zeros.  Or Ts, if cut character substitution
        // is enabled
        int serial = le.getSent().getSerial();  
        if (serial < 10) part2 = twoCharSerialPad + serial;
        else if (serial < 100) part2 = oneCharSerialPad + serial;
        else part2 = "" + serial; // don't pad 3 or 4 digits
      } 
      else if (part2.charAt(1) == 'x') part2 = le.getSent().getGUIExchange().toLowerCase();
      else if (part2.charAt(1) == 'f') part2 = le.getSent().getCallsign().toLowerCase();
      else if (part2.charAt(1) == 't') part2 = getToCall();
      else if (part2.charAt(1) == '?') part2 = getToCall() + "?";
      String result = part1 + part2 + part3;
      // System.out.println("Sub: " + result);
      String tmp = makeSubstitutions(result);
      return tmp; 
    }

    private String getToCall() {
      String cl = le.getRcvd().getCallsign().toLowerCase();
      if ( cl == null || cl.equals("") ) cl = jtf.getText();
      return cl;
    }

  }  // end inner class

}
