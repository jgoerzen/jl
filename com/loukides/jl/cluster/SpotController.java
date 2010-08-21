package com.loukides.jl.cluster;

import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.io.*;
import java.lang.Thread; // debugging

public class SpotController {
    private TelnetClient cl = null;
    private ArrayList spots = new ArrayList();
    public static final int MAXSPOTS = 40;
    private AbstractTableModel spotTM = null;
    private ClusterUI ui = null;
    private static BufferedReader keyboard = null;

    // This is a somewhat inelegant way to kick things off, but maybe it's
    // OK.  It's called by the container; it eliminates the problem of 
    // null pointers, since it can be called late; it concentrates
    // all spotting functionality (right now, only knowledge of the callsign)
    // here--arguably not where it should be, maybe it should be in the 
    // container.
    // Right now, takes input from stdin and writes to stdout, but 
    // will eventually do i/o to Swing UI components.
    public SpotController() {
        spotTM = new SpotTableModel(spots);
    }

    public void start(String loginname) {  
        // System.out.println("Starting login with: " + loginname);
        cl.doLogin(loginname);
        /*  Keyboard support no longer needed
        if (keyboard == null)  keyboard =  
            new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            try {
                cl.sendLine(keyboard.readLine());
            } catch (IOException ioe) { System.out.println(ioe); }
        } 
        */
    }

    public void setClusterClient(TelnetClient cl) { this.cl = cl; }
    public void setUI(ClusterUI ui) { this.ui = ui; }

    public TableModel getTableModel() { return spotTM; }

    public void sendCommand(String command) {
        cl.sendLine(command);
    }

    public void newServer(String server) {
        cl.stop();
        cl = new TelnetClient(server);
        cl.setController(this);
        start("w1jq");        
        ui.connectionRestoring();
    }

    public void getMoreSpots() {
        cl.sendLine("sh/fdx");
    }

    public void spotSelected(int spotindex) {
        if ( spotindex >= spots.size()) return; // clicked past end of list
        Spot sp = (Spot)spots.get(spotindex);
        System.out.println("Selected: " + sp.getCall() + 
                           " QSY to: " + sp.getFrequency()); 
    }

    public void connectionFailed() {
        ui.connectionFailed();
    }

    public void messageReceived(String s) { 
        //        System.out.print("|" + s + "|");     // can't be println
        System.out.println(s);
        if ( s == null ) connectionFailed();
        if (s.startsWith("DX")) {
            // I'm worried about efficiency, and this seems terribly 
            // inefficient, though also fairly general.
            // We could also split s up into fixed-position fields, but
            // I think that's asking for trouble
            StringTokenizer t = new StringTokenizer(s);
            int i = 0;
            int qsxtoken = 0;
            String tokens[] = new String[t.countTokens()] ;
            while (t.hasMoreTokens() != false) {
                tokens[i] = t.nextToken();
                if (tokens[i].equalsIgnoreCase("QSX")) qsxtoken = i +1;
                i++;
            }
            // Some clusters put the canonical prefix after the time;
            // others don't.  Logic below isn't quite bulletproof.
            int l = tokens[i-1].length();
            float f = 0f;
            float qsx = 0f;
            String time = ( l > 0 && l < 5 ) ? tokens[i-2] : tokens[i-1];
            try {
                f = Float.parseFloat(tokens[3]);
            } catch (Exception e) { return; } // no valid freq, punt
            if ( qsxtoken != 0 ) { // might have a QSX
                try { qsx = Float.parseFloat(tokens[qsxtoken]); }
                catch (Exception e) {} // 0 OK if we get a format error
            }
            Spot spot = new Spot(tokens[4], f, qsx,                         
                                 time.substring(0,4) );  // whack trailing Z
            // System.out.println(spot.toString());
            spots.add(0, spot);
            if (spots.size() > MAXSPOTS) spots.remove(MAXSPOTS-1);
            spotTM.fireTableDataChanged();  //Shouldn't be here...
        }
    }
}
