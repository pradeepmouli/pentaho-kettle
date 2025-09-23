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

package org.pentaho.di.www.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.www.JobMap;
import org.pentaho.di.www.TransformationMap;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MCP (Model Context Protocol) Server for Kettle transformations and jobs
 * Provides programmatic access to create, manipulate, configure and execute
 * .ktr (transformation) and .kjb (job) files
 */
public class MCPServer {
    
    private static final String MCP_VERSION = "2024-11-05";
    private static final String SERVER_NAME = "kettle-mcp";
    private static final String SERVER_VERSION = "1.0.0";
    
    private final LogChannelInterface log;
    private final TransformationMap transformationMap;
    private final JobMap jobMap;
    private final ObjectMapper objectMapper;
    private final ExecutorService executor;
    private final MCPTransformationHandler transformationHandler;
    private final MCPJobHandler jobHandler;
    
    private ServerSocket serverSocket;
    private boolean running = false;
    private int port;
    
    public MCPServer(LogChannelInterface log, TransformationMap transformationMap, JobMap jobMap, int port) {
        this.log = log;
        this.transformationMap = transformationMap;
        this.jobMap = jobMap;
        this.port = port;
        this.objectMapper = new ObjectMapper();
        this.executor = Executors.newCachedThreadPool();
        this.transformationHandler = new MCPTransformationHandler(log, transformationMap, objectMapper);
        this.jobHandler = new MCPJobHandler(log, jobMap, objectMapper);
    }
    
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        log.logBasic("MCP Server started on port " + port);
        
        executor.submit(() -> {
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    executor.submit(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    if (running) {
                        log.logError("Error accepting MCP client connection", e);
                    }
                }
            }
        });
    }
    
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            executor.shutdown();
            log.logBasic("MCP Server stopped");
        } catch (IOException e) {
            log.logError("Error stopping MCP Server", e);
        }
    }
    
    private void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    JsonNode request = objectMapper.readTree(line);
                    JsonNode response = handleRequest(request);
                    writer.println(objectMapper.writeValueAsString(response));
                } catch (Exception e) {
                    log.logError("Error processing MCP request", e);
                    JsonNode errorResponse = createErrorResponse("Invalid request: " + e.getMessage());
                    writer.println(objectMapper.writeValueAsString(errorResponse));
                }
            }
        } catch (IOException e) {
            log.logError("Error handling MCP client", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                log.logError("Error closing client socket", e);
            }
        }
    }
    
    private JsonNode handleRequest(JsonNode request) {
        String method = request.path("method").asText();
        JsonNode params = request.path("params");
        String id = request.path("id").asText();
        
        try {
            switch (method) {
                case "initialize":
                    return handleInitialize(id, params);
                case "tools/list":
                    return handleToolsList(id);
                case "tools/call":
                    return handleToolsCall(id, params);
                default:
                    return createErrorResponse(id, "Unknown method: " + method);
            }
        } catch (Exception e) {
            log.logError("Error handling method: " + method, e);
            return createErrorResponse(id, "Error processing request: " + e.getMessage());
        }
    }
    
    private JsonNode handleInitialize(String id, JsonNode params) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("protocolVersion", MCP_VERSION);
        
        ObjectNode serverInfo = objectMapper.createObjectNode();
        serverInfo.put("name", SERVER_NAME);
        serverInfo.put("version", SERVER_VERSION);
        result.set("serverInfo", serverInfo);
        
        ObjectNode capabilities = objectMapper.createObjectNode();
        ObjectNode tools = objectMapper.createObjectNode();
        tools.put("listChanged", false);
        capabilities.set("tools", tools);
        result.set("capabilities", capabilities);
        
        response.set("result", result);
        return response;
    }
    
    private JsonNode handleToolsList(String id) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        
        ArrayNode tools = objectMapper.createArrayNode();
        
        // Add transformation tools
        tools.addAll(transformationHandler.getToolDefinitions());
        
        // Add job tools  
        tools.addAll(jobHandler.getToolDefinitions());
        
        ObjectNode result = objectMapper.createObjectNode();
        result.set("tools", tools);
        response.set("result", result);
        
        return response;
    }
    
    private JsonNode handleToolsCall(String id, JsonNode params) {
        String toolName = params.path("name").asText();
        JsonNode arguments = params.path("arguments");
        
        if (toolName.startsWith("transformation_")) {
            return transformationHandler.handleToolCall(id, toolName, arguments);
        } else if (toolName.startsWith("job_")) {
            return jobHandler.handleToolCall(id, toolName, arguments);
        } else {
            return createErrorResponse(id, "Unknown tool: " + toolName);
        }
    }
    
    private JsonNode createErrorResponse(String message) {
        return createErrorResponse(null, message);
    }
    
    private JsonNode createErrorResponse(String id, String message) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        if (id != null) {
            response.put("id", id);
        }
        
        ObjectNode error = objectMapper.createObjectNode();
        error.put("code", -32000);
        error.put("message", message);
        response.set("error", error);
        
        return response;
    }
}