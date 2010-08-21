// common features for keyer actions (currently only two)
package com.loukides.jl.keyer;

import javax.swing.AbstractAction;
import java.util.Properties; 

public abstract class ActionCommon extends AbstractAction {
  protected static Properties p = null;
  protected static Keyer k = null;

  protected ActionCommon(String s) { super(s); }

  protected static void setPropertiesAndKeyer(Keyer keyer, Properties props ){
    p = props;
    k = keyer;
  }
}