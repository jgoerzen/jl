package com.loukides.jl.jl;

import java.util.*;

public interface Transceiver {

  public void setProperties(Properties p) throws Exception;

  // read and set the currently active VFO
  // freq. handled as a float to avoid "." ambiguity and force rig-independence
  // frequency is always in khz
  public float getFrequency();
  public void setFrequency(float freq);
  public void setSplit(float freq);  // set split frequency

  // read and set the mode
  public String getMode();
  public void setMode(String mode);

  // (for use with the shelf) get a memory; release it; load it; setVFO
  // This could be implemented entirely in the Transceiver implementation, probably
  // giving some more versatility, but it would be a pain: the rig automatically 
  // handles multiple vfos, split mode, current mode, and maybe more 
  public void storeMemory(int i);
  public void setVFOFromMemory(int i);
  public int getAvailableMemory();
  public void releaseMemory(int i);

}