package com.loukides.jl.gadgets;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DoubleButtonArray extends JPanel {
  private JButton databuttons[]; 
  private JButton clearbuttons[];
  private JLabel title = new JLabel();
  private JLabel empty = new JLabel(); // to balance the display
  private int SHELFSIZE;
  private static Properties props;

  private ShelfModel m;
  private int i;

  // could be a better distinction between the display and the model
  public DoubleButtonArray( ShelfModel m ){
    String pname = "layout.DoubleButtonArray";
    SHELFSIZE = Integer.parseInt(props.getProperty(pname + ".shelfsize", "6"));
    int buttonheight = Integer.parseInt(props.getProperty(pname + ".buttonheight", "21"));
    int clearwidth = Integer.parseInt(props.getProperty(pname + ".clearwidth", "65"));
    int popwidth = Integer.parseInt(props.getProperty(pname + ".popwidth", "120"));
    Dimension clearsize = new Dimension(clearwidth, buttonheight);
    Dimension popsize = new Dimension(popwidth, buttonheight);
    databuttons = new JButton[SHELFSIZE];
    clearbuttons = new JButton[SHELFSIZE];
    this.m = m;
    m.registerDisplay(this);
    Container p = this;
    JPanel left = new JPanel();
    JPanel right = new JPanel();
    left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
    right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
    left.add(title);
    right.add(empty);
    title.setForeground(Color.black);
    empty.setText("  ");  // make it the right size...
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    for ( i = 0; i < SHELFSIZE; i++) {
      databuttons[i] = new JButton();
      clearbuttons[i] = new JButton();
      databuttons[i].addActionListener( new SwapActionListener(i));
      clearbuttons[i].addActionListener( new ClearActionListener(i));
      clearbuttons[i].setText("X");
      U.setSizes(clearbuttons[i], clearsize);
      U.setSizes(databuttons[i], popsize);
      left.add(databuttons[i]);
      right.add(clearbuttons[i]);
    }
    left.add(Box.createVerticalGlue());
    right.add(Box.createVerticalGlue());
    p.add(left);
    p.add(right);
  }

  public class SwapActionListener implements ActionListener {
    private int button;
    SwapActionListener( int i ) { button = i; }
    public void actionPerformed(ActionEvent e) { m.swap(button); }
  }

  public class ClearActionListener implements ActionListener {
    private int button;
    ClearActionListener( int i ) { button = i; }
    public void actionPerformed(ActionEvent e) { m.clear(button); }
  }

  public void dataUpdated() { dataUpdated(0); }

  public void dataUpdated(int start){
    for (int i = start; i < SHELFSIZE; i++) {
      databuttons[i].setText(m.getHeard(i));
    }
  }

  public void setTitle(String s) { title.setText(s); }

  public static void setProperties(Properties p) { props = p; }

}