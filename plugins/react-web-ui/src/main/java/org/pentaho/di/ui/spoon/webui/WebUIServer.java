/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.ui.spoon.webui;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

/**
 * Embedded web server to serve the React-based Spoon Web UI
 */
public class WebUIServer {
  
  private static final LoggingObjectInterface loggingObject = new SimpleLoggingObject(
    "WebUIServer", LoggingObjectType.SPOON, null );
  
  private Server server;
  private int port;
  private LogChannelInterface log;
  
  public WebUIServer(int port, LogChannelInterface log) {
    this.port = port;
    this.log = log;
  }
  
  public void start() throws Exception {
    server = new Server(port);
    
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    server.setHandler(context);
    
    // Serve static React build files
    ServletHolder staticHolder = new ServletHolder("static", DefaultServlet.class);
    
    // Find webapp resources
    URL webappResource = getClass().getClassLoader().getResource("webapp");
    if (webappResource != null) {
      Resource baseResource = Resource.newResource(webappResource);
      context.setBaseResource(baseResource);
      context.addServlet(staticHolder, "/*");
      context.setWelcomeFiles(new String[]{"index.html"});
      
      log.logBasic("Serving React Web UI from: " + webappResource.toString());
    } else {
      log.logError("Could not find webapp resources");
    }
    
    // Add REST API endpoints
    context.addServlet(new ServletHolder(new APIServlet(log)), "/api/*");
    
    // Add SPA routing support - redirect all non-API requests to index.html
    context.addServlet(new ServletHolder(new SPAServlet()), "/transformation/*");
    context.addServlet(new ServletHolder(new SPAServlet()), "/job/*");
    
    server.start();
    log.logBasic("Spoon Web UI server started on port: " + port);
    log.logBasic("Access the web interface at: http://localhost:" + port);
  }
  
  public void stop() throws Exception {
    if (server != null) {
      server.stop();
      log.logBasic("Spoon Web UI server stopped");
    }
  }
  
  public boolean isStarted() {
    return server != null && server.isStarted();
  }
  
  /**
   * Servlet to handle Single Page Application routing
   */
  private static class SPAServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
        throws ServletException, IOException {
      req.getRequestDispatcher("/index.html").forward(req, resp);
    }
  }
}