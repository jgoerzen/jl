package com.loukides.jl.jl;
import com.loukides.jl.util.*;
import com.loukides.jl.transceivers.*;

import java.util.*;

public class TransceiverFactory {
  private Properties p;
  private static final String transceiverpkg = "com.loukides.jl.transceivers.";
  private String transceiver;
  private Transceiver tx;

  public TransceiverFactory(Properties p) {
    this.p = p;
    transceiver = p.getProperty("transceiver", "None");
    // System.out.println(p.getProperty("transceiver.transceiver"));
  }

  public Transceiver getInstance() {
    try {
      if (transceiver.equals("") || transceiver.equalsIgnoreCase("None")) 
        tx = (Transceiver)(Class.forName(transceiverpkg + "NullTx").newInstance());
      else if (transceiver.startsWith("Icom756Pro"))
	tx = (Transceiver)(Class.forName(transceiverpkg + "Icom756Pro").newInstance());
      else if  (transceiver.startsWith("Icom7800"))
	tx = (Transceiver)(Class.forName(transceiverpkg + "Icom7800").newInstance());
      else if (transceiver.startsWith("Icom"))
        tx = (Transceiver)(Class.forName(transceiverpkg + "Icom").newInstance());
      else
        tx = (Transceiver)(Class.forName(transceiverpkg + transceiver).newInstance());
      tx.setProperties(p);
      System.out.println("Initialized transceiver " + transceiver + " successfully");
    }
    // if the transceiver can't be instantiated for ANY reason, return a null transceiver
    // so the logger can continue normally (without rig control)
    catch (Exception e) {
      // e.printStackTrace();
      System.out.println("Couldn't instantiate transceiver " + transceiver + 
                         "; returning null");
      tx = new NullTx();
      try {
        tx.setProperties(p);
      } catch (Exception e2) {} // NullTx can't throw an exception
    }
    // catching Error *should* allow this to run (without rig control)
    // machines without javax.comm installed.
    catch (Error e1) {
      // e.printStackTrace();
      System.out.println("Couldn't instantiate transceiver " + transceiver + 
                         "; returning null");
      tx = new NullTx();
      try {
        tx.setProperties(p);
      } catch (Exception e2) {} // NullTx can't throw an exception
    }
    return tx;
  }

}
