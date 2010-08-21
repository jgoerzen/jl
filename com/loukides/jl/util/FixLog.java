package com.loukides.jl.util;

import java.util.*;
import java.io.*;
import java.text.*;

public class FixLog {
  public static void main(String [] args) {
    String line = null;
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HHmm");
    df.setTimeZone(TimeZone.getTimeZone("GMT"));
    long HOURmillis = 60 * 60 * 1000;
    System.out.println(args[0]);
    try {
      BufferedReader reader = new BufferedReader(new FileReader(args[0]));
      while ( (line = reader.readLine()) != null ) {
        if ( ! line.startsWith("QSO:")) System.out.println(line); 
        else {
          String start = line.substring(0,14);
          String time = line.substring(14, 29);
          String end = line.substring(29);
          // System.out.println("|" + start + "|" + time + "|" + end + "|");
          Date d = df.parse(time);
          long tm = d.getTime() + HOURmillis;
          d = new Date(tm);
          String newtime = df.format(d);
          System.out.println(start + newtime + end);
        }
      }
    } catch (Exception e) {System.out.println(e);}
  } 
}