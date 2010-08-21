// An action to stop all keyer messages from playing.  This object is a
// Singleton, so don't call the constructor; call getInstance() to get one.
package com.loukides.jl.keyer;

import java.awt.event.ActionEvent;

public class StopMessageAction extends ActionCommon {
  private static StopMessageAction me = null;

  private StopMessageAction(String text) {
    super(text);
  }

  public void actionPerformed(ActionEvent e) {
    k.stopAllMessages();
  }

  public static StopMessageAction getInstance() {
    if (me == null) me = new StopMessageAction("STOP");
    return me;
  }
}