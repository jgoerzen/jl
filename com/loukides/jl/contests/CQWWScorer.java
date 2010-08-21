package com.loukides.jl.contests;
import com.loukides.jl.jl.LogEntry;
import java.util.*;

public class CQWWScorer extends GenericDoubleMultScorer {

  // ALWAYS use both scorers
  protected boolean useMultChecker1(LogEntry le) { return true; }
  protected boolean useMultChecker2(LogEntry le) { return true; }

  protected void showNeeded(LogEntry le) {
    if (updateVisual) sd.showNeededBands(mult1.getNeeded(le)); 
    if (updateVisual) sd.showNeededBandsAlternate(mult2.getNeeded(le)); 
  }

  protected void incrementMultAndQSOCounter(LogEntry le) {
    sd.contact(le.getBand(), le.getMode(),   // increment if either z or c mult
      (le.isCountryMultiplier() && ! le.isCountryQuery()) || le.isMultiplier() );
    sd.extraMult(le.getBand(), le.getMode(), // increment again if both z and c mult
      (le.isCountryMultiplier() && ! le.isCountryQuery()) && le.isMultiplier() );
  }

  // should be location-independent
  protected int findPoints(LogEntry le) {
    if ( le.getSent().getCountry().equalsIgnoreCase(le.getRcvd().getCountry()) ) return 0;
    else if ( le.getSent().getContinent()
                          .equalsIgnoreCase(le.getRcvd().getContinent()) ) 
      if (le.getSent().getContinent().equalsIgnoreCase("NA")) return 2;
      else return 1;
    else return 3;
  }

  // needs: show multtotaltext (to show both z and c mults)
  //        a good way to show new z AND new c
  protected String getMultName(LogEntry le) {
    if ( le.isMultiplier() && le.isCountryMultiplier() ) return            // both
      "<font color=red>" + le.getName() + " : " + le.getCountry() + "</font>";
    else if ( (!le.isMultiplier()) && (!le.isCountryMultiplier()) ) return // neither
      "<font color=black>" + le.getName() + " : " + le.getCountry() + "</font>";
    else if ( le.isMultiplier() && (!le.isCountryMultiplier()) ) return    // counry
      "<font color=red>" + le.getName() + "</font> : <font color=black>" 
                         + le.getCountry() + "</font>";
    else if ( (!le.isMultiplier()) && le.isCountryMultiplier() ) return    // zone
      "<font color=black>  New: " + le.getName() + "</font> : <font color=red>" // New: hack
                         + le.getCountry() + "</font>"; 
    else return "";
  }

  protected char encodeMult(LogEntry le) {
    boolean c = le.isCountryMultiplier();
    boolean z = le.isMultiplier();
    boolean q = le.isCountryQuery();    
    if ( c == false && z == false & q == false ) return '_';
    if ( c == true  && z == false & q == false ) return 'c';
    if ( c == true  && z == true  & q == false ) return 'b';
    if ( c == false && z == true  & q == false ) return 'z';
    if ( c == false && z == false & q == true  ) return '?';
    if ( c == false && z == true  & q == true  ) return '!';
    if ( c == true  && z == false & q == true  ) return '?';
    if ( c == true  && z == true  & q == true  ) return '!';
    return ' ';
  }

  public static class PropertyValidator extends AbstractScorer.PropertyValidator {
    public Vector validateOperation(Properties p) {
      Vector v = super.validateOperation(p);
      if (p.getProperty("state", "").equals("")) v.add("state");
      if (p.getProperty("arrlSection", "").equals("")) v.add("arrlSection");
      String pwr = p.getProperty("powerCategory", "");
      String cat = p.getProperty("category", "");
      String mode = p.getProperty("modeCategory", "");
      if (pwr.equals("")) v.add("powerCategory");
      if (cat.equals("") || cat.equals("MULTI-UNLIMITED") ||
          cat.equals("MULTI-LIMITED") || cat.equals("ROVER") ||
          cat.equals("SCHOOL-CLUB")  || cat.equals("SINGLE-OP-PORTABLE")) 
        v.add("category");
      int nops = new StringTokenizer(p.getProperty("operators", "")).countTokens();
      if ( nops >= 2 && ! ( cat.equals("MULTI-ONE") || 
                            cat.equals("MULTI-TWO") || 
                            cat.equals("MULTI-MULTI")) ) {
        v.add("category");
        v.add("operators");
      }
      if ( nops == 1 &&   ( cat.equals("MULTI-ONE") || 
                            cat.equals("MULTI-TWO") || 
                            cat.equals("MULTI-MULTI")) ) {
        v.add("category");
        v.add("operators");
      }
      if (p.getProperty("modeCategory", "").equals("")) v.add("modeCategory");
      if (p.getProperty("cqZone", "").equals("")) v.add("cqZone");
      // System.out.println("Validator called (CQWWScorer) " + v);
      return v;
    }
  }

  public java.awt.Component getSummaryDisplay() { return sd; }

}