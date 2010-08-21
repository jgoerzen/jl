 package com.loukides.jl.qtc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Properties;
import com.loukides.jl.util.U;

public class QTCLine extends JPanel {
  private JButton sendButton = new JButton("Send");
  //  private JButton rcvdButton = new JButton("Rcvd");  // button not useful
  private JLabel seriallabel;
  private JLabel qtcinfolabel;
  private QTCEntry q;
  private OriginatedQTCManager qtcm;
  private static Properties props = null;
  private static int serialwidth = 0, 
                     infowidth = 0,
                     allheight = 0;
  private static boolean cutZeros = false;

  public QTCLine(QTCEntry q, OriginatedQTCManager qtcm) {
    this.q = q;
    this.qtcm = qtcm;
    seriallabel = new JLabel(q.getGroupNumberString() + "/" 
                                  + q.getGroupTotalString() + ":");
    qtcinfolabel = new JLabel(q.getQSOTime() + " / " 
                                + q.getAboutCallsign() + " / " 
                                + q.getQSOSerial());
    U.setSizes(seriallabel, new Dimension(serialwidth, allheight));
    U.setSizes(qtcinfolabel, new Dimension(infowidth, allheight));
    sendButton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent e) { 
        sendMyMessage(); 
        qtcinfolabel.setEnabled(false);
      }
    });
    //     rcvdButton.addActionListener( new ActionListener() {
    //    public void actionPerformed(ActionEvent e) { disableLine(); }
    //  });
    add(seriallabel);
    add(qtcinfolabel);
    add(sendButton);
    //    add(rcvdButton);    
  }

  public static void setProperties(Properties pr) { 
    props = pr; 
    serialwidth = Integer.parseInt(props.getProperty("layout.qtcline.serial.width", "150"));
    infowidth = Integer.parseInt(props.getProperty("layout.qtcline.info.width", "150"));
    // in practice, height doesn't have an effect; the buttons don't have their dimensions
    // set, and they effectively govern the height.
    allheight = Integer.parseInt(props.getProperty("layout.qtcline.allitems.height", "25"));
    cutZeros = props.getProperty("keyer.cw.useCutZerosInQTCs", "false").equals("true");
  }

  private void sendMyMessage() {
    if (q.getMode().equals("CW"))
      qtcm.sendKeyerMessage(
        q.getQSOTime() + "/" + cut(q.getAboutCallsign()) + "/" + cut(q.getQSOSerial()));
  }

  private String cut(String s) {
    if (cutZeros) return U.cutLeadingZeros(s);
    return s;
  }

    //   private void disableLine() {
    // rcvdButton.setEnabled(false);
    // rcvdButton.setText("Sent");
    // qtcinfolabel.setEnabled(false);
    // }

}
