// Send log entries (and other information) across the network to a log server.
// The log server will *eventually* be the nexus of multi-multi configuration. 
// Currently sends cab-formatted entries, but should probably send serialized java
// objects, if that's fast enough.
package com.loukides.jl.net;
import com.loukides.jl.jl.*;

import java.util.*;
import java.net.*;
import java.io.*;

public class NetworkLogger {
  private Properties props;
  private int port;
  private InetAddress server;
  private Socket sock = null;
  private ObjectOutputStream osw = null;

  public NetworkLogger(Properties props){
    this.props = props;
    if (props.getProperty("network.doLog", "false").equals("false")) return;
    String portno = props.getProperty("network.port", "");
    if ( portno.equals("") ) {
      System.out.println("Can't read port");
      sock = null;
      return;
    }
    port = Integer.parseInt(portno);
    String servername = props.getProperty("network.server", "");
    if ( servername.equals("") ) {
      System.out.println("Can't read server");
      sock = null;
      return;
    }
    try {
      server = InetAddress.getByName(servername);
      sock = new Socket(server, port);
      // sock.setSendBufferSize(75);
      // System.out.println("Send buffer: " + sock.getSendBufferSize());
      osw = new ObjectOutputStream(sock.getOutputStream());
    } catch (Exception e) {
      // e.printStackTrace();
      System.out.println("Can't open connection to log server at " + servername + 
                         " on port " + port);
      sock = null;
    }
  }

  // implementing separate methods for each thing we might send lets us
  // add hooks as necessary, and gives us some type-safety at a critical juncture.
  public void addLogEntry(Loggable entry) { sendObject(entry); }
  public void setRemoteProperties(Properties p) { sendObject(p); }


  private void sendObject(Object entry) {
    // System.out.println("NL called on socket: " + sock);
    if ( sock == null ) return;  // do nothing if we're not set up right
    try {
      osw.writeObject(entry);
      osw.flush();
      // System.out.println("NL: " + entry);
    } catch (Exception e) {
      // e.printStackTrace(); 
      sock = null;  // stop trying to log.  (Should try to get a new connection?)
      // System.out.println("addLogEntry exception: " + sock);
    }
  }

  public void clear() {}; // Clear the remote log from the current transmitter.
}