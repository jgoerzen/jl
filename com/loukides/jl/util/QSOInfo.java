package com.loukides.jl.util;

public class QSOInfo {
  // just some fields to carry info back and forth

  public String multname = "";
  public String countlabeltext = "";     // first label that also reports serial
  public String multlabeltext = "";  // second label tells about mult
  public String otherlabeltext = ""; // can be used to report score or 2nd mult
  public String dupelabeltext = "";  // reports duplicate
  public boolean isdupe = false;
  public int qsopoints = 0;
}