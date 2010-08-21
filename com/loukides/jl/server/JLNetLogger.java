// The "main" of the network (multi) logging server.
// General thoughts about network logging:
// ** NETWORK UNRELIABILITY CAN BE A PROBLEM IN THE PRESENCE OF RF.
// Lots of potential trouble with rescoring.
// ASSUMPTIONS:
// Clients DO NOT send the initial log to the server.  They assume the server already
//   has it.  A client may, of course, need to be restarted (and read its log).  
//   The server may also need to be restarted, but presumably it can also re-read
//   its log.
// Clients WILL NOT send a "major update" by sending individual log entries.  They
//   will most likely send a dupe sheet.  Or maybe even a Vector with the whole log.
//   But they don't send single entries en-masse.  So you don't need to propagate
//   "update visual" and its equivalent from the clients to the server.   (Note that
//   the code currently DOES NOT do this.  But it's far from functional.)
//
package com.loukides.jl.server;
import com.loukides.jl.util.*;
import com.loukides.jl.jl.*;
import com.loukides.jl.contests.*;

import java.net.*;
import java.util.*;
import java.io.*;

public class JLNetLogger {
  private static int port;
  private static ServerSocket ss;
  private static Properties props=new Properties();
  private static Log log = new Log();
  private static boolean propsalreadyset = false;
  private static Properties clientprops;
  private static ServerDisplay sd = new ServerDisplay();

  public static void main(String [] args) {
    File netpropsfile = new File("configuration/network.props");
    loadProps(netpropsfile);
    String s = props.getProperty("network.port", "");
    if (s.equals("")) U.die("Couldn't read port");
    System.out.println("JLNetLogger started");
    port = Integer.parseInt(s);
    sd.getDisplay().show();
    try {
      ss = new ServerSocket(port);
      while (true) {
        Socket sock = ss.accept();
        System.out.println("Accepting connection from " + sock);
        NetReader nr = new NetReader(sock, log, sd);
        nr.start();
      }
    } catch(Exception e) {
      e.printStackTrace();
      U.die("socket error");
    }
  }

  public static void setProperties(Properties p) {
    if (propsalreadyset) return; // every client will try to set the props; first one
    propsalreadyset=true;        // wins.  (Better would be to find out if the props
                                 // are all the same.
    clientprops = p;
    Callsign.setProperties(clientprops);
    AbstractExchange.setProperties(clientprops);
  }

  private synchronized static void loadProps(File propsfile) {
    try {
      FileInputStream fis = new FileInputStream(propsfile);
      props.load(fis);
      fis.close();
    } catch (IOException e) {
      U.die("can't load props file " + propsfile);
    }
  }

}
