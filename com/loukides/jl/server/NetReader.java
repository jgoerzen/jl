package com.loukides.jl.server;
import com.loukides.jl.util.*;
import com.loukides.jl.jl.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class NetReader extends Thread {
  Socket sock;
  InputStream is;
  ObjectInputStream ir;
  ServerDisplay sd;
  ConnectionMonitor cm;
  Log log;

  public NetReader(Socket sock, Log log, ServerDisplay sd){
    this.sock = sock;
    this.log = log;
    this.sd = sd;
    cm = new ConnectionMonitor(sock.getInetAddress());
    sd.addConnectionMonitor(cm);
    try {
      is = sock.getInputStream();
      ir = new ObjectInputStream(is);
    } catch(Exception e) {
      e.printStackTrace();
      U.die("couldn't get input stream from socket");
    }
  }

  public void run(){
    System.out.println("Starting network reader");
    while (true) {
      try {
        Object obj = ir.readObject();
        if ( obj instanceof LogEntry )       processLogEntry((LogEntry)obj);
        else if (obj instanceof Properties)  JLNetLogger.setProperties((Properties)obj);
        else System.out.println("Unrecognized object");
      } catch(Exception e) {
        // e.printStackTrace();
        System.out.println("Disconnected from " + sock);
        sd.removeConnectionMonitor(cm);
        return;
      }
    }
  }

  private void processLogEntry(LogEntry le) {
    log.add(le);
    cm.update(le);
    sd.update(); // make the master display update itself; not sure this is the best way
  }
}
