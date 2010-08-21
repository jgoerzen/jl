// scorer for ARRL VHF SS and other ARRL VHF contests (June, Sept, Aug. UHF)
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.U;
import java.util.*;

public class VHFScorer extends GenericScorer {
  private static int b6pts = 0;
  private static int b2pts = 0;
  private static int b220pts = 0;
  private static int b432pts = 0;
  private static int b902pts = 0;
  private static int b1296pts = 0;
  private static int b2300pts = 0;
  private static int bOver2300pts = 0;
  private static String myGrid = "";

  public void setProperties(Properties props) {
    super.setProperties(props);
    b6pts = Integer.parseInt(props.getProperty("b6pts", "0"));
    b2pts = Integer.parseInt(props.getProperty("b2pts", "0"));
    b220pts = Integer.parseInt(props.getProperty("b220pts", "0"));
    b432pts = Integer.parseInt(props.getProperty("b432pts", "0"));
    b902pts = Integer.parseInt(props.getProperty("b902pts", "0"));
    b1296pts = Integer.parseInt(props.getProperty("b1296pts", "0"));
    b2300pts = Integer.parseInt(props.getProperty("b2300pts", "0"));
    bOver2300pts = Integer.parseInt(props.getProperty("bOver2300pts", "0"));
    myGrid = props.getProperty("gridSquare6");
  }

  protected int findPoints(LogEntry le) {
    String band = le.getBand();
    if ( band.equals("b6") )         return b6pts;
    else if ( band.equals("b2") )    return b2pts;
    else if ( band.equals("b220") )  return b220pts;
    else if ( band.equals("b432") )  return b432pts;
    else if ( band.equals("b902") )  return b902pts;
    else if ( band.equals("b1296") ) return b1296pts;
    else if ( band.equals("b2300") ) return b2300pts;
    // bands past 2300 also get 8; is it OK to "else" them?
    else return bOver2300pts;
  }

  protected String getMultName(LogEntry le) {
    if ( ! mult.isValidMult(le) ) return "?";
    return le.getRcvd().getMultiplierField() + " " + findHeading(le);
  }

  private String findHeading(LogEntry le) {
    String hisGrid = ((VHFExchange)le.getRcvd()).getGrid();
    double heading = U.findHeadingDegrees(myGrid, hisGrid);
    return U.degreeFormat.format(heading);
  }

  // return the vector of keys that don't have valid values for this contest
  public static class PropertyValidator extends AbstractScorer.PropertyValidator {
    public Vector validateOperation(Properties props) {
      Vector v = super.validateOperation(props);
      if (props.getProperty("arrlSection", "").equals("")) v.add("arrlSection");
      if (props.getProperty("state", "").equals("")) v.add("state");
      if (props.getProperty("gridSquare", "").equals("")) v.add("gridSquare");
      String pwr = props.getProperty("powerCategory", "");
      String cat = props.getProperty("category", "");
      if (pwr.equals("") || pwr.equals("QRP")) v.add("powerCategory"); // no QRP
      if (cat.equals("") || cat.equals("MULTI-ONE") || cat.equals("MULTI-TWO") || 
          cat.equals("MULTI-MULTI") || cat.equals("SINGLE-OP-ASSISTED") ||
          cat.equals("SCHOOL-CLUB") ) {  // only m-l, m-u, s, s-p, and r
            v.add("category");
      }
      int nops = (new StringTokenizer(props.getProperty("operators", ""))).countTokens();
      if ( nops >= 2 && ! ( cat.equals("MULTI-LIMITED") 
                         || cat.equals("MULTI-UNLIMITED")
                         || cat.equals("ROVER")           ) ){
        v.add("category");
        v.add("operators");
      }
      if ( nops > 2 && cat.equals("ROVER")){ //rovers can only have two ops
        v.add("category");
        v.add("operators");
      }
      // System.out.println("Validator called (SSSCorer) " + v);
      return v;
    }
  }

  public java.awt.Component getSummaryDisplay() { return sd; }

}