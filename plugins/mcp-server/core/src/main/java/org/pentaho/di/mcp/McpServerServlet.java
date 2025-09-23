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

package org.pentaho.di.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pentaho.di.core.annotations.CarteServlet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.mcp.handler.TransformationHandler;
import org.pentaho.di.mcp.handler.JobHandler;
import org.pentaho.di.mcp.model.McpRequest;
import org.pentaho.di.mcp.model.McpResponse;
import org.pentaho.di.www.BaseCartePlugin;
import org.pentaho.di.www.CartePluginInterface;
import org.pentaho.di.www.CarteRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * MCP (Model Context Protocol) Server for Kettle transformations and jobs
 * Provides a JSON-RPC 2.0 compatible API for creating, manipulating, configuring and executing
 * Kettle transformations (.ktr) and jobs (.kjb)
 */
@CarteServlet(
  id = "mcp", 
  name = "MCP Server",
  description = "Model Context Protocol server for Kettle transformations and jobs"
)
public class McpServerServlet extends BaseCartePlugin implements CartePluginInterface {

  private static final String CONTEXT_PATH = "/kettle/mcp";
  
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Map<String, McpMethodHandler> methodHandlers = new HashMap<>();
  
  public McpServerServlet() {
    initializeHandlers();
  }

  private void initializeHandlers() {
    TransformationHandler transformationHandler = new TransformationHandler(transformationMap, log);
    JobHandler jobHandler = new JobHandler(jobMap, log);
    
    // Transformation methods
    methodHandlers.put("transformation/create", transformationHandler::createTransformation);
    methodHandlers.put("transformation/load", transformationHandler::loadTransformation);
    methodHandlers.put("transformation/save", transformationHandler::saveTransformation);
    methodHandlers.put("transformation/execute", transformationHandler::executeTransformation);
    methodHandlers.put("transformation/status", transformationHandler::getTransformationStatus);
    methodHandlers.put("transformation/stop", transformationHandler::stopTransformation);
    methodHandlers.put("transformation/list", transformationHandler::listTransformations);
    methodHandlers.put("transformation/delete", transformationHandler::deleteTransformation);
    
    // Job methods  
    methodHandlers.put("job/create", jobHandler::createJob);
    methodHandlers.put("job/load", jobHandler::loadJob);
    methodHandlers.put("job/save", jobHandler::saveJob);
    methodHandlers.put("job/execute", jobHandler::executeJob);
    methodHandlers.put("job/status", jobHandler::getJobStatus);
    methodHandlers.put("job/stop", jobHandler::stopJob);
    methodHandlers.put("job/list", jobHandler::listJobs);
    methodHandlers.put("job/delete", jobHandler::deleteJob);
    
    // Server methods
    methodHandlers.put("server/capabilities", this::getServerCapabilities);
    methodHandlers.put("server/status", this::getServerStatus);
  }

  @Override
  public void handleRequest(CarteRequest request) throws IOException {
    if (!"POST".equalsIgnoreCase(request.getMethod())) {
      request.respond(405).withMessage("Method not allowed. Use POST for MCP requests.");
      return;
    }

    try {
      // Parse MCP request
      McpRequest mcpRequest = objectMapper.readValue(request.getInputStream(), McpRequest.class);
      
      // Handle the request
      McpResponse response = handleMcpRequest(mcpRequest);
      
      // Send response
      request.respond(200).with("application/json", writer -> {
        objectMapper.writeValue(writer, response);
      });
      
    } catch (Exception e) {
      logError("Error handling MCP request: " + e.getMessage(), e);
      
      McpResponse errorResponse = new McpResponse(
        null, 
        new McpResponse.McpError(-32603, "Internal error: " + e.getMessage())
      );
      
      request.respond(500).with("application/json", writer -> {
        objectMapper.writeValue(writer, errorResponse);
      });
    }
  }

  private McpResponse handleMcpRequest(McpRequest request) {
    try {
      String method = request.getMethod();
      McpMethodHandler handler = methodHandlers.get(method);
      
      if (handler == null) {
        return new McpResponse(
          request.getId(),
          new McpResponse.McpError(-32601, "Method not found: " + method)
        );
      }
      
      Object result = handler.handle(request.getParams());
      return new McpResponse(request.getId(), result);
      
    } catch (Exception e) {
      logError("Error executing MCP method: " + e.getMessage(), e);
      return new McpResponse(
        request.getId(),
        new McpResponse.McpError(-32603, "Internal error: " + e.getMessage())
      );
    }
  }

  private Object getServerCapabilities(Map<String, Object> params) {
    Map<String, Object> capabilities = new HashMap<>();
    capabilities.put("server", Map.of(
      "name", "Pentaho Kettle MCP Server",
      "version", "1.0.0"
    ));
    
    capabilities.put("methods", new String[] {
      "transformation/create", "transformation/load", "transformation/save", 
      "transformation/execute", "transformation/status", "transformation/stop",
      "transformation/list", "transformation/delete",
      "job/create", "job/load", "job/save",
      "job/execute", "job/status", "job/stop",
      "job/list", "job/delete",
      "server/capabilities", "server/status"
    });
    
    return capabilities;
  }

  private Object getServerStatus(Map<String, Object> params) throws KettleException {
    Map<String, Object> status = new HashMap<>();
    status.put("running", true);
    status.put("transformations", transformationMap.getTransformationNames());
    status.put("jobs", jobMap.getJobNames());
    return status;
  }

  @Override
  public String getContextPath() {
    return CONTEXT_PATH;
  }

  @FunctionalInterface
  public interface McpMethodHandler {
    Object handle(Map<String, Object> params) throws Exception;
  }
}