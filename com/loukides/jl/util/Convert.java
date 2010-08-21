// Converts text files from UNIX format (nl-ending) to DOS-format
// ALSO converts four-space tabs to two-space tabs
// ALSO (currently disabled) adds a package line, etc., to the front of the file.
package com.loukides.jl.util;

import java.io.*;
import java.util.*;

public class Convert {
  public static void main(String [] args) {
    String line = "";
    String packageline = "package com.loukides.jl.server;";
    int spaces = 0, k = 0;
    String [] clump = { "", "  ", "    ", "      ", "        ",
    "          ", "            "};
    try {
      for (int i = 0; i < args.length; i++) {
        File infile = new File(args[i]);
        File outfile = new File(args[i] + ".out");
        BufferedReader instream = new BufferedReader(new FileReader(infile));
        BufferedWriter outstream = new BufferedWriter(new FileWriter(outfile));
        // outstream.write(packageline);
        // outstream.newLine();
        // outstream.newLine();
        while ( true ) {
          line = instream.readLine();
          System.out.println(line);
          if ( line == null ) break;
          for ( k = 0; k < line.length(); k++) {
            char c = line.charAt(k);
            if ( c == ' ') spaces ++;
            else break;
          }
          line = line.trim();
          line = clump[spaces/4] + line;
          spaces = 0;
          outstream.write(line, 0, line.length());
          outstream.newLine();
        }
        outstream.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}