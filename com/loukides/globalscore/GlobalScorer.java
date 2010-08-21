package com.loukides.globalscore;

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
import java.util.Properties;

public class GlobalScorer extends HttpServlet {
  private Properties reportedHeader = null;

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
      throws ServletException, IOException {
    System.out.println("Got it");
    resp.setContentType("text/html");
    OutputStream os = resp.getOutputStream();
    PrintWriter out = new PrintWriter(os, true);
    out.println("<html><body>");
    out.println("<h1>Hello blah" + "</h1>");
    out.println("<p>");
    reportedHeader.store(os, "Incoming Properties");
    out.println("</body></html>");
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    System.out.println("post: " + req.getMethod());
    InputStream is = req.getInputStream();
    reportedHeader = new Properties();
    try {
       reportedHeader.load(is);
    } catch (Exception e) {}   
  }

}