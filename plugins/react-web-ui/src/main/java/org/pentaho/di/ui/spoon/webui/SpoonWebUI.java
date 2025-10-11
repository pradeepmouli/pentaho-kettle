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

/**
 * Main launcher class for Spoon Web UI mode
 * 
 * This class provides an alternative entry point to launch Spoon in web UI mode
 * instead of the traditional desktop SWT interface.
 */
public class SpoonWebUI {
  
  /**
   * Main method to launch Spoon in web UI mode
   * 
   * Usage:
   *   java -cp ... org.pentaho.di.ui.spoon.webui.SpoonWebUI [options]
   *   
   * Options:
   *   --webui-port <port>    Port for the web server (default: 8080)
   *   --no-desktop          Disable desktop SWT interface entirely
   *   
   * @param args command line arguments
   */
  public static void main(String[] args) {
    System.out.println("Starting Pentaho Data Integration - Spoon Web UI");
    
    // Check for no-desktop mode
    boolean noDesktop = false;
    for (String arg : args) {
      if ("--no-desktop".equals(arg)) {
        noDesktop = true;
        break;
      }
    }
    
    if (noDesktop) {
      // Start in web-only mode
      startWebOnlyMode(args);
    } else {
      // Start normal Spoon with web UI enabled
      SpoonWebUIPlugin.startWebUIMode(args);
    }
  }
  
  private static void startWebOnlyMode(String[] args) {
    System.out.println("Starting in web-only mode (no desktop interface)");
    
    try {
      // Parse port argument
      int port = 8080;
      for (int i = 0; i < args.length - 1; i++) {
        if ("--webui-port".equals(args[i])) {
          port = Integer.parseInt(args[i + 1]);
          break;
        }
      }
      
      // Create and start web server
      WebUIServer server = new WebUIServer(port, new ConsoleLogger());
      server.start();
      
      System.out.println("Spoon Web UI is running at: http://localhost:" + port);
      System.out.println("Press Ctrl+C to stop the server");
      
      // Keep the application running
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        try {
          System.out.println("\nShutting down Spoon Web UI server...");
          server.stop();
        } catch (Exception e) {
          System.err.println("Error during shutdown: " + e.getMessage());
        }
      }));
      
      // Wait for shutdown
      Thread.currentThread().join();
      
    } catch (Exception e) {
      System.err.println("Failed to start Spoon Web UI: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  /**
   * Simple console logger implementation for web-only mode
   */
  private static class ConsoleLogger implements org.pentaho.di.core.logging.LogChannelInterface {
    
    @Override
    public String getLogChannelId() {
      return "SpoonWebUI";
    }
    
    @Override
    public void logMinimal(String message) {
      System.out.println("[INFO] " + message);
    }
    
    @Override
    public void logBasic(String message) {
      System.out.println("[INFO] " + message);
    }
    
    @Override
    public void logDetailed(String message) {
      System.out.println("[DEBUG] " + message);
    }
    
    @Override
    public void logDebug(String message) {
      System.out.println("[DEBUG] " + message);
    }
    
    @Override
    public void logRowlevel(String message) {
      System.out.println("[TRACE] " + message);
    }
    
    @Override
    public void logError(String message) {
      System.err.println("[ERROR] " + message);
    }
    
    @Override
    public void logError(String message, Throwable e) {
      System.err.println("[ERROR] " + message);
      e.printStackTrace();
    }
    
    // Stub implementations for other LogChannelInterface methods
    @Override public boolean isBasic() { return true; }
    @Override public boolean isDetailed() { return false; }
    @Override public boolean isDebug() { return false; }
    @Override public boolean isRowLevel() { return false; }
    @Override public boolean isError() { return true; }
    @Override public String getFilter() { return null; }
    @Override public void setFilter(String filter) {}
    @Override public org.pentaho.di.core.logging.LogLevel getLogLevel() { 
      return org.pentaho.di.core.logging.LogLevel.BASIC; 
    }
    @Override public void setLogLevel(org.pentaho.di.core.logging.LogLevel logLevel) {}
    @Override public String getContainerObjectId() { return null; }
    @Override public void setContainerObjectId(String containerObjectId) {}
    @Override public void logMinimal(String s, Object... objects) { logMinimal(s); }
    @Override public void logBasic(String s, Object... objects) { logBasic(s); }
    @Override public void logDetailed(String s, Object... objects) { logDetailed(s); }
    @Override public void logDebug(String s, Object... objects) { logDebug(s); }
    @Override public void logRowlevel(String s, Object... objects) { logRowlevel(s); }
    @Override public void logError(String s, Object... objects) { logError(s); }
    @Override public void snap(org.pentaho.di.core.logging.LogLevel logLevel, String message) {}
    @Override public void snap(org.pentaho.di.core.logging.LogLevel logLevel, String message, Object... arguments) {}
  }
}