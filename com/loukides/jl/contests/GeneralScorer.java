// A "scorer" to make things work for a general purpose logger.
// All the scoring functionality is gone.  What's left is really just some hooks
// on which to hang the note-taking system.
package com.loukides.jl.contests;
import com.loukides.jl.jl.*;
import com.loukides.jl.util.QSOInfo;
import com.loukides.jl.gadgets.QsoEditor;
import com.loukides.jl.checkers.DupeChecker;

public class GeneralScorer extends AbstractScorer {
  boolean updateVisual;
  Checker dupe = new DupeChecker();
  QsoEditor qe = new QsoEditor();
  Rescorable [] t = { dupe, qe };

 public GeneralScorer() { 
    tools = t; 
  }

  public QSOInfo check( LogEntry le ) {
    QSOInfo q = new QSOInfo();
    if ( le != null ) {
      dupe.isNew(le);
      qe.makeQsoEditor(le);
      q.multlabeltext = le.getRcvd().getCountry();
    }
    return q;
  }

  public QSOInfo score( LogEntry le ) {
    QSOInfo q = new QSOInfo();
    if ( le != null ) dupe.addEntry(le);
    if ( le == null ) qe.dontsaveAndClose();
    return q;
  }

}