package com.loukides.jl.transceivers;
import com.loukides.jl.jl.*;

import java.util.*;

// abstract superclass for all transceivers.  
// Implements functions for dealing with memories for the QSO shelf.
public abstract class AbstractTransceiver implements Transceiver {
  protected Properties props = null;
  protected int membase;
  protected int memtop;
  protected int memnumber;
  protected boolean [] mems;

  public AbstractTransceiver() {}

  public void setProperties(Properties p) throws Exception {
    props = p;
    // Read the "high" memory slot (top memory number to use); allocate
    // some number of slots (one more than the size of the QSO shelf) 
    // below that.  If the base memory number is negative,
    // we'll just disable the memory feature by returning a bogus (-1) memory
    // number each time.  You can guarantee that the base memory is negative
    // by setting transceiver.highmem to 0, or leaving it unset.
    memtop = Integer.parseInt(p.getProperty("transceiver.highmem", "0"));
    // can conceivably need one extra slot: when swapping, the 
    // new memory slot is allocated before the old one is cleared
    memnumber = 1 + Integer.parseInt(p.getProperty(
                  "layout.DoubleButtonArray.shelfsize", "0"));
    membase = memtop - memnumber;
    mems = new boolean[memnumber];
    if (membase < 0) System.out.println("Invalid high memory slot; not using memory");
  }

  // read and set the currently active VFO
  // Should freq. be returned as an int or a float?
  public float getFrequency() { return (float)0.0; }
  public void setFrequency(float freq) {}
  public void setSplit(float freq) {}

  public String getMode() { return ""; }
  public void setMode(String mode) {};

  // manage some number (memnumber) of rig memories, ranging between
  // memtop and membase.  mems represents whether any given memory is in use (true)
  // or available.  -1 is an (intentionally) bogus number.
  public void storeMemory(int i) {
    if ( i == -1) return;  // really, just a reminder
  }
  public void setVFOFromMemory(int i) {
    if ( i == -1) return;
  }
  public int getAvailableMemory() {
    // System.out.println("Memory stuff: " + membase + " " + memtop + " " 
    //    + memnumber + " " + mems[0] + " " + mems[1]);
    if ( membase < 0 ) return -1;
    for ( int i = 0; i < mems.length; i++) {
      if (mems[i] == false) {
        mems[i] = true;
        printMemoryState();
        return membase + i;
      }
    }
    printMemoryState();
    return -1;  // nothing available
  }

  // we don't actually clear the memory on the transceiver, just mark it
  // re-usable
  public void releaseMemory(int i) {
    // System.out.println(i);
    if ( i == -1 ) return;
    mems[i - membase] = false;
    printMemoryState();
  }

  private void printMemoryState() { // for debugging
    // for (int i = 0; i < mems.length; i++) System.out.print(i+": "+mems[i]+"::  ");
    // System.out.println();
  }

}