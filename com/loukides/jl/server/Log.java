package com.loukides.jl.server;
import com.loukides.jl.util.*;
import com.loukides.jl.jl.*;

import java.io.*;

public class Log {
  private final static String filename = "logfile.txt";
  private FileOutputStream outputfile;
  private PrintWriter pw;

  public Log() {
    String ofname = "logs" + "/" + filename;
    try {
      outputfile = new FileOutputStream(ofname, true);
      pw = new PrintWriter(outputfile);
    } catch(Exception e) {
      e.printStackTrace();
      U.die("Couldn't open log output file");
    }
  }

  public synchronized void add( LogEntry entry ){
    try {
      pw.print(entry.toCabrilloString());
      pw.println();
      pw.flush();
      // outputfile.writeln();
      // outputfile.flush();
      // System.out.println("Log: " + entry);
    } catch (Exception e) {
      e.printStackTrace();
      U.die("Can't write to output file");
    }

  }
}
