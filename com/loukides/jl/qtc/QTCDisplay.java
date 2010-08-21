package com.loukides.jl.qtc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import com.loukides.jl.util.U;

public class QTCDisplay {

  private OriginatedQTCManager manager = null;
  private JFrame jf = null;
  private Container p = null;
  private JTextField recipient = new JTextField();
  private JButton done = new JButton("Done");
  private JButton abandon = new JButton("Abandon");
  private JPanel qtcp = null; 
  private JLabel headerlabel = new JLabel();
  private JLabel qtclabel = new JLabel("QTCs for: ");
  private JButton sendheader = new JButton("Send");
  //  private JButton cb = new JButton("Rcvd");   // button not useful
  private ArrayList qtcs = null;
  private String qtcnumber = "";
  private String qtcgroupsize = "";
  private String mode = ""; 
  private static Properties props = null;
  private static boolean cutZeros = false;

  public QTCDisplay(OriginatedQTCManager m) {
    int displayx = Integer.parseInt(props.getProperty("layout.qtcdisplay.main.x", "0"));
    int displayy = Integer.parseInt(props.getProperty("layout.qtcdisplay.main.y", "0"));
    int recipientwidth = 
      Integer.parseInt(props.getProperty("layout.qtcdisplay.recipient.width", "75"));
    int acceptwidth = 
      Integer.parseInt(props.getProperty("layout.qtcdisplay.accept.width", "75"));
    int allheight = 
      Integer.parseInt(props.getProperty("layout.qtcdisplay.allitems.height", "25"));
    this.manager = m;
    jf = new JFrame();
    p = jf.getContentPane();
    jf.setTitle("QTC Sender");
    jf.setLocation(displayx, displayy); 
    p.setLayout(new BorderLayout());
    JPanel controls = new JPanel();
    U.setSizes(recipient, new Dimension(recipientwidth, allheight));
    U.setSizes(abandon, new Dimension(acceptwidth, allheight));
    U.setSizes(done, new Dimension(acceptwidth, allheight));
    controls.add(done);
    controls.add(abandon);
    p.add(controls, BorderLayout.SOUTH);
    done.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) { done(); }
    });
    abandon.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) { abandon(); }
    });
    recipient.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) { changeRecipient(); }
    });
    //    cb.addActionListener( new ActionListener() {
    //  public void actionPerformed(ActionEvent e) { 
    //    cb.setEnabled(false); 
    //    cb.setText("Sent");
    //  }
    //  });
    JPanel headerPanel = new JPanel();
    ((FlowLayout)headerPanel.getLayout()).setAlignment(FlowLayout.LEFT);
    ((FlowLayout)headerPanel.getLayout()).setHgap(10);
    headerPanel.add(qtclabel);
    headerPanel.add(recipient);
    headerPanel.add(headerlabel);
    headerPanel.add(sendheader);
    //    headerPanel.add(cb);
    sendheader.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sendMyHeader();
        headerlabel.setEnabled(false);
      }
    });
    p.add(headerPanel, BorderLayout.NORTH);

    // closing the window is the same as cancelling the QTC
    jf.addWindowListener( new WindowAdapter() { 
      public void windowClosing(WindowEvent e) { manager.qtcDone(); } });
  }

  public static void setProperties(Properties pr) { 
    props = pr; 
    cutZeros = props.getProperty("keyer.cw.useCutZerosInQTCs", "false").equals("true");
  }

  public void show() {
    // System.out.println("Showing QTC");
    qtcs = manager.getQTCArray();
    showInternal();
  }

  private void done() {
    jf.hide();
    manager.qtcDone(qtcs);
  }

  private void abandon() {
    jf.hide();
    manager.qtcDone();
  }

  private void changeRecipient() {
    manager.qtcDone();
    String toCall = recipient.getText().toUpperCase();
    qtcs = manager.getQTCArray(toCall);
    showInternal();
  }

  private void showInternal() {
    headerlabel.setEnabled(true);  // disabled by clicking 'send'
    if (qtcs.size() > 0) {
        //      cb.setEnabled(true);
        //cb.setText("Rcvd");
      QTCEntry template = ((QTCEntry)qtcs.get(0));
      qtcnumber = template.getGroupNumberString();
      qtcgroupsize = U.zpad2.format(qtcs.size());
      mode = template.getMode();
      headerlabel.setText(" Series: " + qtcnumber + " / " + qtcgroupsize);
      recipient.setText(template.getRecipient());
    } else {
      qtcnumber = ""; 
      qtcgroupsize = "";
      headerlabel.setText("No QTCs!");
    } 
    if ( qtcp != null ) p.remove(qtcp);   
    qtcp = new JPanel();
    qtcp.setLayout(new BoxLayout(qtcp, BoxLayout.Y_AXIS));
    Iterator it = qtcs.iterator();
    while ( it.hasNext() ) {
      qtcp.add(new QTCLine((QTCEntry)(it.next()), manager));
    }
    p.add(qtcp, BorderLayout.CENTER);
    jf.pack();
    jf.show();
  }

  private void sendMyHeader() {
    if ( qtcs.size() > 0 && mode.equals("CW"))  
      manager.sendKeyerMessage("QTC " + cut(qtcnumber) + "/" + cut(qtcgroupsize));
  }

  private String cut(String s) {
    if (cutZeros) return U.cutLeadingZeros(s);
    return s;
  }

}
