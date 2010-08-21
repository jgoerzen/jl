// A very simple IOC container for the dx cluster support.  
package com.loukides.jl.cluster;

public class SpotBuilder {
    private TelnetClient client = null;
    private SpotController cont = null;

    public static void main(String [] args) {
        new SpotBuilder(args[0]);
    }

    public SpotBuilder() {
        new SpotBuilder("k1ttt.net");
    }

    public SpotBuilder(String host) {
        ClusterUI ui = new ClusterUI();
        client = new TelnetClient(host);
        cont = new SpotController();
        client.setController(cont);
        cont.setClusterClient(client);
        cont.setUI(ui);
        ui.setController(cont);
        cont.start("w1jq");
    }

}
