// action for playing a message (attached to buttons or to function keys)
// Don't call the constructor; maintains a cache, eliminating multiple equivalent objects.
// To get an action for message n, call getInstance(n).
package com.loukides.jl.keyer;

import java.awt.event.ActionEvent;
import java.util.*;

public class PlayMessageAction extends ActionCommon {
  private int message = -1;
  private static HashMap playactions = new HashMap();

  private PlayMessageAction(int message, String messagelabel) {
    super(messagelabel);
    this.message = message;
    playactions.put(new Integer(message), this);
 }

  public void actionPerformed(ActionEvent e) {
    k.startMessage(message);
  }

  public static PlayMessageAction getInstance(int message) {
    Object action = playactions.get(new Integer(message));
    if (action == null) {
      String messagelabel = p.getProperty("keyer.label." + message);
      action = new PlayMessageAction(message, messagelabel);
    }
    return (PlayMessageAction)action;
  }
}