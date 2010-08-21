package com.loukides.jl.util;

import java.io.*;

public class CountryRecord implements Serializable {

  String canonicalPx;
  String zone;
  String name;
  String continent;

  public static final CountryRecord NOCOUNTRY = new CountryRecord( "", "", "", "", "" );

  public CountryRecord( String r, String p, String z, String n, String c) {
    canonicalPx = p;
    zone = z;
    name = n;
    continent = c;
  }

  public String getCanonicalPx() { return canonicalPx; }
  public String getZone() { return zone; }
  public String getName() { return name; }
  public String getContinent() { return continent; }

  public String toString() {
    return "|" + canonicalPx + "|" +zone + "|" + name + "|" + continent;
  }
}