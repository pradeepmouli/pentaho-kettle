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

import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.ui.spoon.*;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;

import java.awt.*;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Spoon plugin that provides a React-based web UI as an alternative to the desktop interface
 */
@SpoonPlugin(
  id = "SpoonWebUIPlugin",
  name = "Spoon Web UI",
  description = "React-based web interface for Spoon",
  image = "ui/images/spoon_web.svg"
)
public class SpoonWebUIPlugin implements SpoonPluginInterface, SpoonLifecycleListener, SpoonUiExtenderPluginInterface {
  
  private WebUIServer webServer;
  private LogChannelInterface log;
  private int serverPort = 8080;
  
  @Override
  public void applyToContainer(String category, XulDomContainer container) throws XulException {
    // Add web UI menu items and controls to the existing Spoon interface
  }
  
  @Override
  public SpoonLifecycleListener getLifecycleListener() {
    return this;
  }
  
  @Override
  public SpoonPerspective getPerspective() {
    return null; // We don't add a new perspective, we provide an alternative interface
  }
  
  // SpoonLifecycleListener methods
  @Override
  public void onStart(Spoon spoon) {
    this.log = spoon.getLog();
    
    // Check if web UI should be enabled
    String enableWebUI = System.getProperty("spoon.webui.enabled", "false");
    if ("true".equalsIgnoreCase(enableWebUI)) {
      startWebServer();
    }
  }
  
  @Override
  public void onExit(Spoon spoon) {
    stopWebServer();
  }
  
  // SpoonUiExtenderPluginInterface methods
  @Override
  public Map<Class<?>, Set<String>> respondsTo() {
    Map<Class<?>, Set<String>> responses = new HashMap<>();
    Set<String> events = new HashSet<>();
    events.add("menu-open-web-ui");
    responses.put(Spoon.class, events);
    return responses;
  }
  
  @Override
  public void uiEvent(Object subject, String event) {
    if ("menu-open-web-ui".equals(event) && subject instanceof Spoon) {
      openWebUI();
    }
  }
  
  private void startWebServer() {
    try {
      // Try to find an available port
      String portProperty = System.getProperty("spoon.webui.port", "8080");
      serverPort = Integer.parseInt(portProperty);
      
      webServer = new WebUIServer(serverPort, log);
      webServer.start();
      
      log.logBasic("Spoon Web UI is available at: http://localhost:" + serverPort);
      
      // Auto-open web UI if requested
      String autoOpen = System.getProperty("spoon.webui.autoopen", "false");
      if ("true".equalsIgnoreCase(autoOpen)) {
        openWebUI();
      }
      
    } catch (Exception e) {
      log.logError("Failed to start Spoon Web UI server", e);
    }
  }
  
  private void stopWebServer() {
    if (webServer != null) {
      try {
        webServer.stop();
      } catch (Exception e) {
        log.logError("Error stopping web UI server", e);
      }
    }
  }
  
  private void openWebUI() {
    if (webServer != null && webServer.isStarted()) {
      try {
        String url = "http://localhost:" + serverPort;
        if (Desktop.isDesktopSupported()) {
          Desktop.getDesktop().browse(new URI(url));
          log.logBasic("Opened Spoon Web UI in default browser: " + url);
        } else {
          log.logBasic("Desktop not supported. Please open manually: " + url);
        }
      } catch (Exception e) {
        log.logError("Failed to open web UI in browser", e);
      }
    } else {
      log.logError("Web UI server is not running. Start with -Dspoon.webui.enabled=true");
    }
  }
  
  /**
   * Static method to start web UI mode directly
   */
  public static void startWebUIMode(String[] args) {
    System.setProperty("spoon.webui.enabled", "true");
    System.setProperty("spoon.webui.autoopen", "true");
    
    // Parse command line arguments for port
    for (int i = 0; i < args.length - 1; i++) {
      if ("--webui-port".equals(args[i])) {
        System.setProperty("spoon.webui.port", args[i + 1]);
        break;
      }
    }
    
    try {
      // Start Spoon normally - the plugin will handle web UI initialization
      Spoon.main(args);
    } catch (Exception e) {
      System.err.println("Failed to start Spoon with Web UI: " + e.getMessage());
      e.printStackTrace();
    }
  }
}