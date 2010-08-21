// Doing this as a separate window is *not* ideal; I'd like to consolidate
// displays.  But at this point, I'm curious about whether it works.
// Threading bug:  DON'T start the timer thread while reading the log.
// Probably not while reprocessing it either.
package com.loukides.jl.gadgets;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;

public class HistoryWindow extends JFrame {

  JScrollPane js;
  JScrollBar vsb;
  JPanel jp;
  final JTable table;
  private static Properties props;

  public HistoryWindow(TableModel elm){
    super("Previous Contacts:");
    String propn = "layout.History.";
    int [] columnWidths = {
      Integer.parseInt(props.getProperty(propn + "serialwidth", "50")),
      Integer.parseInt(props.getProperty(propn + "timewidth", "50")),
      Integer.parseInt(props.getProperty(propn + "modewidth", "50")),
      Integer.parseInt(props.getProperty(propn + "bandwidth", "50")),
      Integer.parseInt(props.getProperty(propn + "sentwidth", "50")),
      Integer.parseInt(props.getProperty(propn + "callwidth", "50")),
      Integer.parseInt(props.getProperty(propn + "rcvdwidth", "50")),
      Integer.parseInt(props.getProperty(propn + "dupewidth", "50")),
      Integer.parseInt(props.getProperty(propn + "multwidth", "50")),
      Integer.parseInt(props.getProperty(propn + "ptswidth", "50"))
    };
    BorderLayout lm = new BorderLayout();
    Container p = getContentPane();

    table = new JTable(elm);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.getSelectionModel()
         .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    p.setLayout(lm);
    p.add(js = new JScrollPane(table), BorderLayout.CENTER);
    propn = "layout.History.";
    setSize(Integer.parseInt(props.getProperty(propn + "size.x", "0")),
            Integer.parseInt(props.getProperty(propn + "size.y", "0")));
    setLocation(Integer.parseInt(props.getProperty(propn + "location.x", "0")),
                Integer.parseInt(props.getProperty(propn + "location.y", "0")));
//    setSize(700,150);
//    setLocation(0,200);
    for ( int i = 0; i < elm.getColumnCount(); i++ ) {
      table.getColumnModel().getColumn(i).setMaxWidth(columnWidths[i]);
      table.getColumnModel().getColumn(i).setMinWidth(columnWidths[i]);
    }

    setVisible(true);
    vsb = js.getVerticalScrollBar(); 
  }

  public static void setProperties(Properties p) { props = p; }

}