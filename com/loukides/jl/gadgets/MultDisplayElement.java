package com.loukides.jl.gadgets;

import java.util.*;
import java.awt.*;
import javax.swing.*;

public class MultDisplayElement {
  private JLabel display = new JLabel();
  private String bandmode = "";
  private int mults = 0;
  private int qsos = 0;
  private boolean updateVisual = true; 
  private static Color backgroundColor = null;
  private static Color foregroundColor = null;

  public MultDisplayElement(String bm) { // immutable once set
    display.setOpaque(true);
    backgroundColor = display.getBackground();
    foregroundColor = display.getForeground();
    if ( bandmode.equals("")) bandmode = bm;
    display.setHorizontalAlignment(JLabel.CENTER); 
    display.setText(bm); 
  }

  public void setMults(int v) { 
    mults = v; 
    doDisplay();
  }
  public void setQsos(int v) { 
    mults = v; 
    doDisplay();
  }
  public void setUpdateVisual(boolean b) { 
    updateVisual = b; 
    if (b) doDisplay();
  }

  public  int getMults() { return mults;}
  public int getQsos() { return qsos; }
  public String getBandmode() { return bandmode; }
  public JLabel getLabel() { return display ; }

  public void incrementMults() {
    mults++;
    doDisplay();
  }
  public void incrementQsos() {
    qsos++;
    doDisplay();
  }
  public void clear() {
    mults = 0; 
    qsos = 0;
    doDisplay();
  }

  public void showNeededBand(boolean n) {
    if (n) display.setForeground(Color.red);
    else display.setForeground(foregroundColor);
  }

  public void showNeededBandAlternate(boolean n) {
    if (n) display.setBackground(Color.yellow);
    else display.setBackground(backgroundColor);
  }

  private void doDisplay() {
    String shortband = bandmode;
    if (shortband.startsWith("b")) shortband = shortband.substring(1);
    if (updateVisual) display.setText(shortband + ": " + mults + " M   " + qsos + " Q");
  } 
}