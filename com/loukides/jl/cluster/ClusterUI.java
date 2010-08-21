package com.loukides.jl.cluster; 

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.TableModel;

public class ClusterUI {
    private JFrame f = new JFrame();
    private JTable display = new JTable();
    private JTextField server = new JTextField(15);
    private JTextField command = new JTextField(15);
    private JButton getspots = new JButton("Get");
    private Container p = null;
    private SpotController sc = null;
    private ListSelectionModel rsm = null;

    public ClusterUI() {
        p = f.getContentPane();
        p.setLayout(new BorderLayout());
        JPanel serverpanel = new JPanel();
        serverpanel.add(new JLabel("Server: "));
        serverpanel.add(server);
        server.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    sc.newServer(server.getText());
                }
            });
        JPanel commandpanel = new JPanel();
        commandpanel.add(new JLabel("Command: "));
        commandpanel.add(command);
        command.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    sc.sendCommand(command.getText());
                }
            });
        commandpanel.add(getspots);
        getspots.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    sc.getMoreSpots();
                }
            });
        p.add(serverpanel, BorderLayout.NORTH);
        p.add(commandpanel, BorderLayout.SOUTH);
        JScrollPane jsp = new JScrollPane(display);
        p.add(jsp, BorderLayout.CENTER); // needs a scrollpane to get col names
        f.pack();
        f.show();
    }

    public void setController(SpotController sc) { 
        this.sc = sc; 
        display.setModel(sc.getTableModel());
        display.setRowSelectionAllowed(true);
        display.setColumnSelectionAllowed(false);
        rsm = display.getSelectionModel();
        rsm.addListSelectionListener(new SpotSelectionListener(display, sc));
        rsm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public void connectionFailed() {  // good idea, doesn't work
        //        server.setBackground(Color.RED); 
    }

    public void connectionRestoring() {
        server.setBackground(Color.WHITE); 
    }

    public JFrame getFrame() { return f; }

    private class SpotSelectionListener implements ListSelectionListener {
        JTable table = null; 
        SpotController sc = null;

        public SpotSelectionListener(JTable t, SpotController sc) {
            this.table = t;
            this.sc = sc;
        }
        public void valueChanged(ListSelectionEvent lse) {
            // HMMM.   I SEE RACE CONDITIONS
            if ( ! lse.getValueIsAdjusting() ) {
                int index = table.getSelectionModel().getMinSelectionIndex();
                if ( index >= 0) {
                    sc.spotSelected(index);
                    // System.out.println("Row selected" + index);
                }
            }
        }
    }

}
