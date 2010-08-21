// Represents a spot.  
// Undecided whether it should create a Callsign object, 
// or just treat the Callsign as a string.  Can't create a Callsign without
// initializing Callsign first, which is a pain, so for now it's a String.
// Discards info we don't (now) care about;
// it will probably be best just to take the cluster's notion of the time.
// (Or, since the yukkiest part of the code is extracting the time, should
// we just stamp the time recieved?  That may be simpler...

package com.loukides.jl.cluster;

// import com.loukides.jl.jl.Callsign;
import com.loukides.jl.util.U;
import java.util.GregorianCalendar;


public class Spot {
    //    private Callsign call = null;
    private String call = null;
    private float freq = 0f;
    private float qsx = 0f;
    private String timespotted = null;  // maybe should be a Calendar

    public Spot(String callsign, float f, float q, String d) {
        //        call = new Callsign(callsign);
        call = callsign;
        freq = f;
        timespotted = d;
        qsx = q;
    }

    //    public String getCall() { return call.getCallsign(); }
    public String getCall() { return call; }
    //    public String getCountry() { return call.getCountry(); }
    public float  getFrequency() { return freq; }
    public float  getQSX() { return qsx; }
    public String getTimeSpotted() { return timespotted; }

    public String toString() {
        return getCall() + " " + getFrequency() + " " + getTimeSpotted();
    }

    public boolean equals(Object o) {
        if ( ! (o instanceof Spot)) return false;
        if ( U.toMeters(getFrequency()) != 
             U.toMeters(((Spot)o).getFrequency()))   return false;
        // this one is problematic...
        if ( getCall() != ((Spot)o).getCall())       return false;
        return true; 
    }

}



