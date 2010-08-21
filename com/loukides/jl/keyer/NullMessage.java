package com.loukides.jl.keyer;

public class NullMessage extends MessageCommon {

  // note: we can't play anything, but we still need to be able to stop things!
  public void playMessage() {
    //  System.out.println("Playing null message"); 
    stopPlaying();
  }

  public void stopPlaying() {
    if (player != null && player.isAlive()) player.interrupt();  // don't play same msg twice!
  }

  public void setParams(String s, Object o, int i) throws Exception {}
}