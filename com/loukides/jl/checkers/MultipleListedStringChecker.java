// A checker that's like ListedStringChecker, but that tolerates multiplier /-separated
// fields.  Used particularly or CQP, and probably useful in other state QPs.
//
// MOTE:  Also, the checker sets the multiplier property of le, but not
// the multChar.  (This is one reason that it's important for CountryChecker
// to override--countries have their own countryMultiplier field, which
// has to be maintained separately.) 
//
package com.loukides.jl.checkers;
import com.loukides.jl.jl.*;
import com.loukides.jl.gadgets.*;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;

public class MultipleListedStringChecker extends ListedStringChecker {

  protected String delimiter = "/";

  // Probably would have been smart to do all the MakeMultKeys like this...
  protected String makeMultKey(String multiplier, String band, String mode) {
    String b = "";
    String m = "";
    if ( perbandmults ) b = band;
    if ( permodemults ) m = mode;
    // System.out.println("perband: " +perbandmults+" permode: "+permodemults);
    // System.out.println("makekey: " + m + " " + le.getMode());
    // band first to facilitate dupe sheet
    return b + " " + m + " " + multiplier.toLowerCase(); 
  }

  // We return "new" if ANY multiplier is new.
  // No way to tell how many.
  public boolean isNew( LogEntry le ) {
    le.setMultiplier(false);
    String multstack = le.getRcvd().getMultiplierField(); 
    String multiplier = "";
    StringTokenizer st = new StringTokenizer(multstack, delimiter);
    while (st.hasMoreTokens()) {
      multiplier = st.nextToken();
      String k = makeMultKey( multiplier, le.getBand(), le.getMode());
      if ( (! worked.containsKey(k)) && isValidMult(le)) le.setMultiplier(true);
    }
    return le.isMultiplier();
  }
 
  // like other methods, sets as many of the fields of the log entry
  // as we know about
  public void addEntry(LogEntry le) {
    le.setMultiplier(false);          // start by initializing to false;
    if ( ! le.isComplete() ) {
      // whatever it might have said, it doesn't
      // count if it's not complete
      // System.out.println("AddEntry-incomplete");
      return;
    }
    String multstack = le.getRcvd().getMultiplierField(); 
    String multiplier = "";
    StringTokenizer st = new StringTokenizer(multstack, delimiter);
    if ( ! isValidMult(le) )  return;        // if the multiplier token is bad
    while (st.hasMoreTokens()) {
      multiplier = st.nextToken().toLowerCase();
      String k = makeMultKey( multiplier, le.getBand(), le.getMode());
      // System.out.println("multkey: " + k);
      if ( ! worked.containsKey(k) ) { // and it's a new multiplier
        le.setMultiplier(true);      // mark the entry as a mult
        worked.put (k, le);          // and add it to the mult table
        if (displayMultAbbrev)       // and display it
          updateDisplay(le, multiplier);
        else 
          updateDisplay(le, (String)mults.get(multiplier));
      }
    } 
  } 

  // Note on setting the name field:
  // IF the multiplier isn't valid, we set the name to the multiplier list, as typed
  //    (so the user can maybe do something about it)
  // IF we're not told to display abbreviations, we display the name after looking it up
  //    PROVIDED THAT there's only one.  If we're handed two or more mults, no point;
  //    it's too long.  SO we fall back to displaying what the user typed.
  public boolean isValidMult(LogEntry le) { 
    String multiplier = "";
    String name = "";
    String multipliers = le.getRcvd().getMultiplierField();
    le.setName("");
    if ( multipliers.equals("") || multipliers == null ) return false;
    StringTokenizer st = new StringTokenizer(multipliers, delimiter + "\n\r"); 
    int toks = st.countTokens();
    while ( st.hasMoreTokens() ) {
      multiplier = st.nextToken();
      // System.out.println("Multimult: " + multipliers + "|" + multiplier);
      name = (String)mults.get(multiplier.toLowerCase());
      if ( name == null ) {
        // System.out.println("invalid mult: " + multiplier + " " + name);
        le.setName( multipliers );
        return false;
      }
    }
    if ( toks == 1 && ! displayMultAbbrev ) le.setName(name);
    else le.setName(multipliers);
    return true;
  }

  public Vector getNeeded( LogEntry le) {
    // System.out.println("AC::getNeeded");
    Vector v = new Vector();
    StringTokenizer st = new StringTokenizer(bands);
    String currentband = "";
    String hashkey;
    String multiplier = le.getRcvd().getMultiplierField();
    // don't do anything if there are multiple multipliers nested in here....
    // next line untested--not needed in the only contest that usere MLSC
    if ( -1 != multiplier.indexOf(delimiter)) return v; 
    // we have at most one multiplier; see where we need it.
    while ( st.hasMoreTokens()) {
      currentband = st.nextToken();
      hashkey = makeMultKey( multiplier, currentband, le.getMode() );
      if ( ! worked.containsKey(hashkey) ) v.add(currentband);
      // System.out.println("|" + currentband + "|" + hashkey + "|" + worked.containsKey(hashkey));
    }
    // System.out.println(v);
    return v;
  }

}