package com.loukides.jl.gadgets;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;

public class OperationProperty {
  public String labeltext; 
  public JLabel label;
  public String propskey;
  public JComponent inputgadget;
  public Object values = null;
  public ComboBoxModel model = null;

  public OperationProperty(String lt, String key, JComponent jc) {
    this.labeltext = lt;
    this.propskey = key;
    this.label = new JLabel(lt);
    this.inputgadget = jc;
  }

}