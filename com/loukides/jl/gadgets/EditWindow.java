package com.loukides.jl.gadgets;
import com.loukides.jl.jl.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;

public class EditWindow extends JFrame {

  private JScrollPane js;
  private JScrollBar vsb;
  private JPanel jp;
  private final JTextField searchfield = new JTextField(10);
  private final JButton deletebutton = new JButton("Delete");
  private final JButton nextbutton = new JButton("Next: ");
  private final JButton rescorebutton = new JButton("Rescore");
  private final JButton summarizebutton = new JButton("Summarize");
  private final JLabel searchlabel = new JLabel("Search: ");
  private final Logger lgr;
  private final JTable table;
  private TableModel elm;
  private static Properties props;
  private Canceller cancel = null;

  String searchtext = "";
  int row;

  public EditWindow(Logger logger, Canceller cancel){
    super("Log Editor");
    lgr = logger;
    this.cancel = cancel;
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

    logger.setEditor(this);   
    elm = logger.getLogModel();

    table = new JTable(elm);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.getSelectionModel()
         .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    p.setLayout(lm);
    p.add(js = new JScrollPane(table), BorderLayout.CENTER);
    js.getViewport().setBackingStoreEnabled(true);  // deprecated
    propn = "layout.Edit.";
    setSize(Integer.parseInt(props.getProperty(propn + "size.x", "0")),
            Integer.parseInt(props.getProperty(propn + "size.y", "0")));
    setLocation(Integer.parseInt(props.getProperty(propn + "location.x", "0")),
                Integer.parseInt(props.getProperty(propn + "location.y", "0")));
    for ( int i = 0; i < elm.getColumnCount(); i++ ) {
      table.getColumnModel().getColumn(i).setMaxWidth(columnWidths[i]);
      table.getColumnModel().getColumn(i).setMinWidth(columnWidths[i]);
    }

    jp = new JPanel();
    jp.add(searchlabel);
    jp.add(searchfield);
    jp.add(nextbutton);
    jp.add(deletebutton);
    jp.add(rescorebutton);
    jp.add(summarizebutton);

    p.add(jp, BorderLayout.SOUTH);

    searchfield.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        String lookfor = searchfield.getText();
        if (lookfor.startsWith("/")) lookfor = lookfor.substring(1);
        if (lookfor.equals("")) 
          next();
        else 
          search(lookfor);
        searchfield.setText("");
      }
    });

    nextbutton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        next();
      }
    });

    deletebutton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        int killrow = table.getSelectedRow();
        lgr.deleteLogEntry(killrow);
        // reset to allow sequential deletions; remember two dummy rows at end
        if ( killrow == elm.getRowCount() -2 ) killrow --;
        if ( killrow > 0) table.addRowSelectionInterval(killrow, killrow);
      }
    });

   rescorebutton.addActionListener( new ActionListener() {
     public void actionPerformed(ActionEvent ae) {
       rescore();
     }
   });

   summarizebutton.addActionListener( new ActionListener() {
     public void actionPerformed(ActionEvent ae) {
       summarize();
     }
   });
 
    setVisible(true);
    vsb = js.getVerticalScrollBar(); 
  }
 
  private void rescore() {
    lgr.redoLog();
    cancel.cancel();
  }  

  private void summarize() {
    lgr.summarize();
    lgr.report();
  }

  public void search(String s) {
    searchtext = s;
    table.clearSelection();
    row = lgr.search( s, true );
//    System.out.println( "found: " + row );
    table.addRowSelectionInterval( row, row );
    scrollToRow(row);
    nextbutton.setText("Next: " + s);
  }

  public void next() {
    table.clearSelection();
    row = lgr.search( searchtext, false );
//    System.out.println( "found: " + row );
    table.addRowSelectionInterval( row, row );
    scrollToRow(row);  
  }

  public void scrollToRow(int row) {
    int max = vsb.getMaximum();
    float value = ( (float) row / (float) elm.getRowCount() ) *  max;
//    System.out.println( "scroll to: "+ row + " " + max + " " + value); 
    vsb.setValue(Math.round(value));    
  }

  public void scrollToEnd() {
    int max = vsb.getMaximum();
    vsb.setValue(max + 100);        
  }

  public static void setProperties(Properties p) { props = p; }

}