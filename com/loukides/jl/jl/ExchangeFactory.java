package com.loukides.jl.jl;
import com.loukides.jl.util.*;

import java.util.*;

public class ExchangeFactory {
  private Properties p;
  private String base;

  public ExchangeFactory(Properties p) {
    this.p = p;
    base = "com.loukides.jl.contests." +
       p.getProperty( "classBasename" );
  }

  public Exchange getInstance() {
    try {
      return (Exchange) Class.forName( base + "Exchange" ).newInstance();
    } catch (Exception e) { 
      System.out.println(e); 
      U.die("couldn't instantiate exchange");
    }
    return null;
  }

}