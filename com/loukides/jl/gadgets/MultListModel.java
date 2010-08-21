package com.loukides.jl.gadgets;
import com.loukides.jl.jl.*;

import javax.swing.*;
import javax.swing.table.*;
import java.util.*;


public interface MultListModel extends TableModel {

  public void setupVisual(); 

  public void setUpdateVisual(boolean b) ;

  public void updateDisplay(LogEntry le, String multvalue) ;

}