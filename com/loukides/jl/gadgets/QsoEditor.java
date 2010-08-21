package com.loukides.jl.gadgets;
import com.loukides.jl.util.*;
import com.loukides.jl.jl.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class QsoEditor implements Rescorable {
  private JFrame f; 
  private JButton done, cancel; 
  private JTextArea text; 
  private Container p;
  private File qsofile = null;
  private FileReader qsofilereader = null;
  private FileWriter qsofilewriter = null;
  private String filename;
  private char [] readchars = new char[1024];
  private String call, callbase;
  private int areawidth = 0, areaheight = 0, 
              framewidth = 0, frameheight = 0,
              framex = 0, framey = 0;
  private boolean updateVisual;
  private String directory;
  private boolean editorOpen = false;
  private boolean newfile = false;

  public void setProperties(Properties props) {
    areawidth = Integer.parseInt(
      props.getProperty("layout.QsoEditor.TextArea.width", "20"));
    areaheight = Integer.parseInt(
      props.getProperty("layout.QsoEditor.TextArea.height", "10"));
    framewidth = Integer.parseInt(
      props.getProperty("layout.QsoEditor.Frame.width", "100"));
    frameheight = Integer.parseInt(
      props.getProperty("layout.QsoEditor.Frame.height", "100"));
    framex = Integer.parseInt(
      props.getProperty("layout.QsoEditor.Frame.x", "0"));
    framey = Integer.parseInt(
      props.getProperty("layout.QsoEditor.Frame.y", "0"));
    directory = props.getProperty("qsoFilesDir", "qsofiles");
  }

  public void makeQsoEditor(LogEntry le) {
    call = le.getRcvd().getCallsign();
    if (updateVisual &&  ! editorOpen  && call != null) {
      f = new JFrame();
      f.addWindowListener( new WindowAdapter() {
        public void windowClosing(WindowEvent e) { editorOpen = false; }
      });
      // make entry field grab focus whenever the mouse is in the main window
      f.addMouseListener(new MouseAdapter() {
        public void mouseEntered(MouseEvent me) { 
          text.requestFocus(); 
        }
      });
      text = new JTextArea(areaheight, areawidth);
      done = new JButton("Done");
      done.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          saveAndClose();
        }
      }); 
      cancel = new JButton("Cancel");
      cancel.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          dontsaveAndClose();
        }
      }); 
      f.setTitle(call);
      U.setSizes(text, new Dimension(framewidth, frameheight));
      JPanel buttonpane = new JPanel();
      buttonpane.add(done);
      buttonpane.add(cancel);
      f.setLocation(framex, framey);
      p = f.getContentPane();
      p.setLayout(new BorderLayout());
      p.add(text, BorderLayout.NORTH);
      p.add(buttonpane, BorderLayout.SOUTH);
//      done.addActionListener(this);
      int slashloc = call.indexOf('/');
      if ( slashloc == -1 ) callbase = call; // no slash in call
      else {
        String s1 = call.substring(0, slashloc);
        String s2 = call.substring(slashloc +1);
        if ( s1.length() >= s2.length() ) callbase = s1;
        else callbase = s2;
      }
      filename = directory + "/" + callbase + ".txt";
      // System.out.println("QSOEditor: " + filename);
      try {
        String readtext;
        qsofile = new File(filename);
        qsofile.createNewFile();
        qsofilereader = new FileReader(qsofile);
        newfile = false;
        int numreadchars = qsofilereader.read(readchars, 0, readchars.length);
        if ( numreadchars == -1 ) {
          readtext = "Call: " + call + "\n"
                   + "QTH: " + "\n"
                   + "Name: " +  "\n"
                   + "QSL MGR: " + "\n"
                   + "QSL Sent: " + "\n"
                   + "QSL Rcvd: " + "\n"
                   + "Notes: " + "\n";
          newfile = true;
        }
        else readtext = new String(readchars, 0, numreadchars);
        text.setText(readtext);
        qsofilereader.close();
      } catch ( Exception e ) { 
        e.printStackTrace(); 
        System.out.println("Couldn't open QSO file"); 
      }
      f.pack();
      f.show();
      editorOpen = true;
    }
  }
  public void clear() {}
  public void setUpdateVisual(boolean b) { updateVisual = b; }
  public void writeReport(PrintWriter o) {}

  private void saveAndClose() {
    if (updateVisual) {
      String stuff = text.getText();
      if ( call != null) {
        try {
          qsofilewriter = new FileWriter(qsofile);
          qsofilewriter.write(stuff);
          qsofilewriter.close();
        } catch ( Exception e ) { U.die("Couldn't write QSO file"); }
        editorOpen = false;
        f.dispose();
      }
    }
  }

  // if we're cancelled for some reason, don't save: instead cleanup.
  // if we've created a new file for this QSO, we're aborting the QSO, and 
  // don't want the record of the new file around.
  public void dontsaveAndClose() {
    if (newfile) {
      try {
        qsofile.delete();
      } catch (Exception e) { System.out.println(e); }
    }
    editorOpen = false;
    // Get rid of the QSO editor frame.  The next line is probably horrible
    // programming, but here's the rationale.  We're called EITHER in response
    // to a click on our cancel button or the main window's cancel button.
    // But the main "cancel" is somewhat overloaded, and is called once to 
    // start things up cleanly.  But in this ONE case, we're asked to cancel
    // before there's anything to cancel.  (Presumably, though, someone could
    // also click the "cancel" button for no particular reason).  At any rate,
    // we can't dispose the frame if there's nothing to dispose.
    if ( f != null ) f.dispose();  
  }

}