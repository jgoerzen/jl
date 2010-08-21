package com.loukides.jl.util;

import java.text.DateFormat;
import java.util.Date;

public class TimeInterval {
  private Date start = null;
  private Date end = null;
  private boolean periodstart, periodend;

  public TimeInterval(Date start, Date end) {
    this(start, end, false, false);
  }

  public TimeInterval(Date start, Date end, boolean periodstart, boolean periodend) {
    this.start = start; this.end = end;
    this.periodstart = periodstart; this.periodend = periodend;
  }

  public Date getStart() { return start; }
  public Date getEnd() { return end; }
  public long getDifference() { return end.getTime() - start.getTime(); }
  public String toString() { return "start: " + start + " end: " + end; }
  public boolean isPeriodStart() { return periodstart; }
  public boolean isPeriodEnd() { return periodend; }
    
}
