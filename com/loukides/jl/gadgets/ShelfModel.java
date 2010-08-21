// The shelf model is a data model for the "shelf": the modified stack of deferred
// QSO data.  Items can be pushed or popped; items can be swapped with the log 
// entry that's in progress (one deviation from a standard stack); and items can 
// be cleared.
// The size of the shelf is set by the property layout.DoubleButtonArray.shelfsize, and
// if you push too much onto the stack, the bottom entry is dropped off.
// The shelf is unfortunately involved in memory management, and hence has
// to know about the Transceiver class.  Memory management is (for the most part)
// done by the MainWindow, but there are two cases in which a shelf entry is 
// deallocated (and must release its memory on the transceiver):  when the bottom
// item is pushed off the stack, and when an item is cleared.  It seemed to make more
// sense to make the model handle these cases than to have some sort of callback
// to the main window.  (Though that could have been done, too.)

package com.loukides.jl.gadgets;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.*;

import java.util.*;

public class ShelfModel{
  private ShelvableQSO shelf[];
  private DoubleButtonArray d = null;
  private MainWindow main;
  private Transceiver transceiver;
  private int shelfsize;

  public ShelfModel(MainWindow m, Transceiver t, Properties p) {
    main = m;
    shelfsize = Integer.parseInt(
      p.getProperty("layout.DoubleButtonArray.shelfsize", "0"));
    shelf = new ShelvableQSO[shelfsize];
    for ( int i = 0; i < shelfsize; i++) {
      shelf[i] = ShelvableQSO.NULLQSO;
    }
    transceiver = t;
  }

  //  swaps the indicated shelf slot with the QSO in the main display
  public void swap(int i) {
    ShelvableQSO q = shelf[i];
    shelf[i] = main.swapWithMain(q);
    if (shelf[i].isNull()) clear(i);
    d.dataUpdated(i);
  }

  public void clear(int button) {
    //  System.out.println("SM:clear " + button);
    // In case of a clear, we release the QSO's memory slot here
    transceiver.releaseMemory(shelf[button].getMemory());
    for (int i = button; i < shelfsize - 1; i++ ) {
      shelf[i] = shelf [i+1];
    }
    shelf[shelfsize -1] = ShelvableQSO.NULLQSO;
    d.dataUpdated(button);
  }

  public void push(ShelvableQSO q) {
    // System.out.println("SM::put: "+ q.getHeard());
    // We might be forcing an entry off the end of the shelf.  So make the 
    // last entry release its memory.  If this slot is empty, we don't care...
    transceiver.releaseMemory(shelf[shelfsize-1].getMemory());
    for (int i = shelfsize -2; i >=0 ; i--) {
      shelf[i+1] = shelf[i];
      // System.out.println(getHeard(0) + " " +getHeard(1) + " " + getHeard(2) + " "
      //                  + getHeard(3));
    }
    shelf[0] = q;
    d.dataUpdated();
  }

  public ShelvableQSO pop() {
    ShelvableQSO q = shelf[0];
    for ( int i = 0; i < shelfsize -1 ; i++) {
      shelf[i] = shelf[i+1];
    }
    shelf[shelfsize-1] = ShelvableQSO.NULLQSO;
    d.dataUpdated();
    return q;
  }

  public String getHeard(int i) {
    return shelf[i].getHeard();
  }

  public void registerDisplay(DoubleButtonArray d) { 
    this.d = d; 
  }
}