package com.loukides.jl.cluster;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TelnetClient {
    private String clusterAddress = "na2na.net";
    private int port = 23;
    private static final int CLUSTER_PRIO=Thread.MIN_PRIORITY;
    private SpotController cont = null;
    private ClusterReader cr = null;  // needs to be a thread
    private BufferedWriter writer = null;  // doesn't need to be a thread
    private boolean keepgoing = true;
    private Socket sock = null;
    private Reader reader = null;

    public static void main(String [] argv){
        new TelnetClient(argv[0]);
    }

    public TelnetClient(String addr) {
        // System.out.println("Connecting to: " + addr);
        try {
            if (addr != "" ) clusterAddress = addr;
            InetAddress cluster = InetAddress.getByName(clusterAddress);
            sock = new Socket(cluster, port);
            reader = 
                new InputStreamReader(sock.getInputStream());
            writer = new BufferedWriter(
                new OutputStreamWriter(sock.getOutputStream()));
            cr = new ClusterReader(reader);
        } catch(Exception e) { 
            System.out.println("Couldn't open cluster\n" + e);
            cont.connectionFailed();
        }
    }

    public void doLogin(String loginname) { 
        cr.drainUntil("call: ");      // need to drain match the END
        sendLine(loginname);   // (not sure that's reliable)
        cr.start();           // kick off normal activity
        // System.out.println("Started: " + cr);
    }

    public void setController(SpotController sc) { this.cont = sc; }

    // sends line to server.  ONLY SEND NON-NEWLINE STRINGS TO THIS METHOD
    public void sendLine(String s) {
        try {
            writer.write(s + "\r\n");
            writer.flush();
        } catch(IOException ioe) { 
            System.out.println("Sendline: " + ioe); 
            cont.connectionFailed();
        }
    }        

    public void stop() {
        keepgoing = false;
        try {
            writer.close();
            reader.close();
        } catch (IOException e) { 
            System.out.println("Couldn't close"); 
            cont.connectionFailed();
        }
    }

    private class ClusterReader extends Thread {
        private Reader reader = null; 
        char [] buf = new char[80];
        BufferedReader br = null;
        private boolean wantfullline = false;
        
        public ClusterReader(Reader r) {
            reader = r;
            br = new BufferedReader(r);
        }

        public void run() {
            setPriority(CLUSTER_PRIO);
            wantfullline=true;
            while (keepgoing) {
                handleLine();
            }
        }

        public void drainUntil(String prompttail) {
            String s = "";
            while ( ! s.endsWith(prompttail) ) {
                s = handleLine();
            }
        }

        public String handleLine() {
            // System.out.println("Reader: " + reader + "Controller: " + cont);
            String s = null;
            try {
                if (wantfullline) {
                    s = br.readLine();
                } else {
                    int n = reader.read(buf, 0, buf.length);
                    if ( n < 0 ) return "";  // was null
                    s  = new String(buf, 0, n);
                }
                cont.messageReceived(s);
            } catch(IOException ioe) {
                System.out.println("read failed");
                cont.connectionFailed();
                return("");
            }
            return s;        
        }
    }

}
