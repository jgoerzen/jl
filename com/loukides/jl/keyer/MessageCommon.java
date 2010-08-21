package com.loukides.jl.keyer;
import com.loukides.jl.jl.LogEntry;

import javax.swing.JTextField;
import java.util.Properties;

public abstract class MessageCommon implements Message {
  protected static LogEntry le = null;
  protected static JTextField jtf = null;
  protected static Message currentMessage = null; 
  protected static Properties props = null;
  protected static Thread player = null; 

  public static void setLogEntry(LogEntry logentry) { 
    le = logentry; 
    // System.out.println("LEH: " + le.getRcvd().getCallsign()+" "+le.getSent().getCallsign());
  }

  public static void setMainEntryField(JTextField f) { jtf = f; }

  public static void setProperties(Properties properties) { 
    props = properties; 
  }

}