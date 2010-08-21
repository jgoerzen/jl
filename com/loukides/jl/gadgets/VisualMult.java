package com.loukides.jl.gadgets;
import com.loukides.jl.jl.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;

public class VisualMult extends JFrame {
  private JScrollPane js;
  private JScrollBar vsb;
  private final static int columnWidth =  175 ;
  private static int displayNumber = 0;
  private static Properties props;

  public VisualMult(TableModel m) {
    super("Multipliers");
    displayNumber++;
    Container p = getContentPane();
    JTable table = new JTable(m);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.getColumnModel().getColumn(0).setMaxWidth(columnWidth);
    table.getColumnModel().getColumn(0).setMinWidth(columnWidth);
    p.add(js = new JScrollPane(table));
    String propertyname = "layout.VisualMult." + displayNumber;
    int xsize = Integer.parseInt(props.getProperty(propertyname + ".size.x", "0"));
    int ysize = Integer.parseInt(props.getProperty(propertyname + ".size.y", "0"));
    int xloc  = Integer.parseInt(props.getProperty(propertyname + ".location.x", "0"));
    int yloc  = Integer.parseInt(props.getProperty(propertyname + ".location.y", "0"));
    setSize(xsize, ysize);
    setLocation(xloc, yloc);
    setVisible(true);
    vsb = js.getVerticalScrollBar();
  }

  public static void setProperties(Properties p) { props = p; }

  // scroll the display to the group of multipliers represented by le
  // AT PRESENT, NOT USED
  public void scrollTo(LogEntry le){
    int max = vsb.getMaximum();
    float value = 0;
    vsb.setValue(Math.round(value)); 
  }

}