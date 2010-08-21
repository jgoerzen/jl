package com.loukides.jl.scorereporter;

import com.loukides.jl.contests.AbstractScorer;
import com.loukides.jl.util.U;
import java.util.*;
import java.net.*;
import java.io.*;

public class ScoreReporter extends Thread {
  private URL url = null;
  private int period = 0;
  private AbstractScorer sc = null; 
  private URLConnection uc = null;

  public ScoreReporter(String urlstring, int period, AbstractScorer sc) {
    try {
      url = new URL(urlstring);
      if ( ! url.getProtocol().toLowerCase().startsWith("http")) U.die("Reporter needs http");
    } catch ( Exception e ) { 
      U.die("Malformed scorer url"); 
    }
    this.period = period;
    this.sc = sc;
  }

  public void run() {
    while (true) {
      try {
        uc = url.openConnection();  // need to make sure this doesn't gum things up
        System.out.println(url);
        uc.setDoInput(false);
        uc.setDoOutput(true);
        System.out.println("1");
        uc.connect();
        System.out.println("1.5");
        OutputStreamWriter out = new OutputStreamWriter(uc.getOutputStream());
        System.out.println("2");
        out.write("hahaha");
        sc.summarize(out);
        System.out.println("3");
        out.flush();
        System.out.println("4");
        out.close();
        System.out.println("5");
      } catch (IOException e) { System.out.println("Can't open global scorer connection" + e); }
      try {
        sleep( period * 1000 );
      } catch (InterruptedException e) {}
    }
  }
}