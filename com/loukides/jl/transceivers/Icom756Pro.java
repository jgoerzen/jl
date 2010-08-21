/* THIS IS THE ORIGINAL VERSION WHICH SETS VFO A AND B EXPLICITLY */
// Support for the Icom 756Pro and Icom 7800 transceivers
// These transceivers use the notion of "split" operation with Main and Sub VFOs, 
// rather than VFOs A and B.
// I don't know whether this change applies to the venerable 756.  
// It does NOT apply to the 7000.
package com.loukides.jl.transceivers;

public class Icom756Pro extends Icom {

  public void setSplit(float f) {
    int bytes;
    if ( f == 0.0f ) {
      bytes = doCommand(CLEARSPLITCOMMAND, CLEARSPLITLENGTH);
      return;
    }
//    bytes = doCommand(SYNCVFOCOMMAND, SYNCVFOLENGTH); // DO NOT BELIEVE THIS IS NEEDED
    System.arraycopy(floatToBytes(f), 0, SETFREQCOMMAND, 5, 5);  
    bytes = doCommand(SETSUBVFOCOMMAND, SETVFOLENGTH);             // this code works for IC-765
    bytes = doCommand(SETFREQCOMMAND, SETFREQLENGTH);            // which doesn't have SWAPVFO
    bytes = doCommand(SETMAINVFOCOMMAND, SETVFOLENGTH);
    bytes = doCommand(SETSPLITCOMMAND, SETSPLITLENGTH);
  }

  public void storeMemory(int mem) {
    int bytes;
    int submemtop = memtop/2;
    int submem = submemtop - (memtop - mem);
    bytes = doCommand(SETMAINVFOCOMMAND, SETVFOLENGTH);
    super.storeMemory(mem);
    bytes = doCommand(SETSUBVFOCOMMAND, SETVFOLENGTH);
    super.storeMemory(submem);
    bytes = doCommand(SETMAINVFOCOMMAND, SETVFOLENGTH);
  }
  
  public void setVFOFromMemory(int mem) {
    int bytes;
    int submemtop = memtop/2;
    int submem = submemtop - (memtop - mem);
    bytes = doCommand(SETMAINVFOCOMMAND, SETVFOLENGTH);
    super.setVFOFromMemory(mem);
    bytes = doCommand(SETSUBVFOCOMMAND, SETVFOLENGTH);
    super.setVFOFromMemory(submem);
    bytes = doCommand(SETMAINVFOCOMMAND, SETVFOLENGTH);
  }
}
