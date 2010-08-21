package com.loukides.jl.gadgets;

import java.io.*;
import java.util.*;

public class PropertiesFilter implements FilenameFilter {
  public boolean accept(File d, String s) {
    return s.trim().substring(s.length()-6).equals(".props");
  }
}