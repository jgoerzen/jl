package com.loukides.jl.keyer;

import javax.sound.sampled.*;
import java.util.*;
import java.io.*;

public class PHMessage extends MessageCommon {
  private Object port = null;    // could be Common, but MAYBE diff ports for diff modes
  private String filename = null;
  private int loopdelay = 0;
  private long cliplength = 0;
  private long totaldelay = 0;
  private Clip clip = null;
  private String audiodir = "";
  private static final String base = "keyer.";

  public PHMessage() {
    audiodir = props.getProperty(base + "ph.audioDir", "");
  }

  public void playMessage() {
    // System.out.println("Playing phone message"); 
    // had to implement my own looper to be able to control the delay in software
    // (loop() just loops, with 0 delay between iterations)
    if (player != null && player.isAlive()) player.interrupt();  // stop anything playing
    player = new AudioLooper();
    player.start();  // start new message
  }

  private class AudioLooper extends Thread {
    public void run() {
      this.setPriority(Thread.MAX_PRIORITY -1); 
      try {
        if (loopdelay == 0) {
          clip.setFramePosition(0);
          clip.start();
          sleep(cliplength); // makes it possible to stop!
        } else {
          while (true) {
            clip.setFramePosition(0);
            clip.start();            // start() returns immediately
            sleep(totaldelay);  // sleep for length of clip + loop delay
          }
        }
      } catch (InterruptedException e) { // break the loop on interrupt
          clip.stop();            // stop the clip, if it's currently playing
      }
    }
  }

  public void stopPlaying() {
    if (player == null) return; 
    if (player.isAlive()) player.interrupt();
  }

  public void setParams(String s, Object o, int i) throws Exception {
    filename = audiodir + "/" + s;
    port = o;
    loopdelay = i;
    if (filename.equals("")) throw new Exception("No audio file");
    File soundFile = new File(filename);
    try {
      // System.out.println("Loading audio file: " + filename);
      AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile);
      AudioFormat af = ais.getFormat();
      DataLine.Info lineinfo = new DataLine.Info(Clip.class, af);
      clip = (Clip) AudioSystem.getLine(lineinfo);
      // System.out.println("File: " + soundFile + "\n"
      //                + "AudioInputStream: " + ais + "\n"
      //                + "Line Info: " + lineinfo  + "\n"
      //                + "Clip: " + clip);
      clip.open(ais);
      LineListener ll = MessageFactory.makePTTController();
      if ( ll != null ) clip.addLineListener(ll);
    } catch(Exception e) {
      System.out.println(e);
      throw e;
    }
    cliplength = clip.getMicrosecondLength()/1000;
    totaldelay = cliplength
               + loopdelay * 1000; // total length + delay, millis (what wait() wants)
    // System.out.println("Total delay for clip " + filename + ": " + totaldelay);
  }

}
