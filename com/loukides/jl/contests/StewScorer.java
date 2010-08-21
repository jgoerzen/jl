package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.U;

public class StewScorer extends GenericScorer {

  protected int findPoints(LogEntry le) { 
    int powermult = 1;
    String power = p.getProperty("powerCategory", "");
    if (power.equals("QRP")) powermult = 4;
    else if (power.equals("LOW")) powermult = 2;
    String sentgrid = le.getSent().getMultiplierField().substring(0,4);
    String rcvdgrid = le.getRcvd().getMultiplierField().substring(0,4);
    double distance = U.findDistanceKM(sentgrid, rcvdgrid); 
    System.out.println( "Distance: " + distance);
    int pts = (int)(1 + (Math.floor(distance/500))); // < 500: 1; 1000: 2; ...
    return powermult * pts;
  }

  protected int getNewMultTotal(LogEntry le) { return 1; }

}