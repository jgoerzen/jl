package com.loukides.jl.gadgets;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class InitialChoices extends JFrame {

  String newpropsfile = "";

  // returns a filename or empty string
  public String getContestPropertiesFile() {
    File contestdir = new File("contests");
    String [] contestprops = contestdir.list(new PropertiesFilter());
    for ( int i = 0; i < contestprops.length; i++) 
      contestprops[i] = contestprops[i].substring(0, contestprops[i].length()-6);
    String choice = (String) JOptionPane.showInputDialog(null, 
                   "Select a Contest", "JL: Contest selection",
                    JOptionPane.QUESTION_MESSAGE, null,
                    contestprops, contestprops[0]);                                
    // System.out.println(choice);
    return choice + ".props";
  }

  // returns a filename or empty string
  public String getOperationPropertiesFile() {
    File opsdir = new File("operations");
    String [] opsprops = opsdir.list(new PropertiesFilter());
    String [] opsnames = new String[opsprops.length + 1];
    for ( int i = 0; i < opsprops.length; i++) // copy, stripping .props suffix as we go
      opsnames[i] = opsprops[i].substring(0, opsprops[i].length()-6);
    opsnames[opsnames.length -1] = "New operation";
    String choice = (String) JOptionPane.showInputDialog(null, 
                   "Select an Operation, or New Operation", "JL: Operation selection",
                    JOptionPane.QUESTION_MESSAGE, null,
                    opsnames, opsnames[0]);
    // System.out.println(choice);
    if (choice == null || choice.equals("New operation") ) return "";
    return choice + ".props";
  }

  public String makeOperationPropertiesFile(Properties contest, String filename) {
    OperationBuilder opb = new OperationBuilder(contest, filename);
    newpropsfile = showMyMessageDialog( null, opb, "JL: Operation Builder", 
                          JOptionPane.PLAIN_MESSAGE);
    // System.out.println(newpropsfile);
    return newpropsfile ;
  }

  // a wrapper to showMessageDialog that gives us a change to clean up afterwards.
  public static String showMyMessageDialog( Component p, Object m, String t, int i) {
    JOptionPane.showMessageDialog( p, m, t, i);
    // Make sure we get one last whack at the data!
    return ((OperationBuilder)m).writeAndExit();
  }

}