package com.loukides.jl.keyer;

public interface Message {
  public abstract void playMessage();
  public abstract void stopPlaying();
  // Messages are normally instantiated through newInstance(), so we need some way to get
  // the parameters to the message.  The constructor won't work.
  public abstract void setParams(String message, Object port, int loopdelay) 
    throws Exception;
}