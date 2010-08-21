package com.loukides.jl.jl;

import java.util.*;
import java.io.Serializable;

public interface Loggable extends Serializable {

  public void setFrequency(float f);
  public void setFrequency(int f);
  public float getFrequency();

  public void setBand(String band);
  public String getBand();

  public void setMode(String mode);
  public String getMode();

  public void setDate(Date d);
  public Date getDate();
  public String timeToGMT(Date d);

  public String toCabrilloString();

  public void parseCabrilloString(String s);

  public boolean isComplete();

}