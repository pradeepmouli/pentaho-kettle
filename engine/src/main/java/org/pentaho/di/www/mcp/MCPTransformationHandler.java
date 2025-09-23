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
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.www.CarteObjectEntry;
import org.pentaho.di.www.TransformationMap;

import java.io.*;
import java.util.Map;
import java.util.UUID;

/**
 * Handles MCP tool calls for Kettle transformations (.ktr files)
 */
public class MCPTransformationHandler {
    
    private final LogChannelInterface log;
    private final TransformationMap transformationMap;
    private final ObjectMapper objectMapper;
    
    public MCPTransformationHandler(LogChannelInterface log, TransformationMap transformationMap, ObjectMapper objectMapper) {
        this.log = log;
        this.transformationMap = transformationMap;
        this.objectMapper = objectMapper;
    }
    
    public ArrayNode getToolDefinitions() {
        ArrayNode tools = objectMapper.createArrayNode();
        
        // Create transformation
        ObjectNode createTool = objectMapper.createObjectNode();
        createTool.put("name", "transformation_create");
        createTool.put("description", "Create a new Kettle transformation (.ktr file)");
        ObjectNode createSchema = objectMapper.createObjectNode();
        createSchema.put("type", "object");
        ObjectNode createProps = objectMapper.createObjectNode();
        ObjectNode nameSchema = objectMapper.createObjectNode();
        nameSchema.put("type", "string");
        nameSchema.put("description", "Name of the transformation");
        createProps.set("name", nameSchema);
        ObjectNode descSchema = objectMapper.createObjectNode();
        descSchema.put("type", "string");
        descSchema.put("description", "Description of the transformation");
        createProps.set("description", descSchema);
        createSchema.set("properties", createProps);
        ArrayNode createRequired = objectMapper.createArrayNode();
        createRequired.add("name");
        createSchema.set("required", createRequired);
        createTool.set("inputSchema", createSchema);
        tools.add(createTool);
        
        // Load transformation
        ObjectNode loadTool = objectMapper.createObjectNode();
        loadTool.put("name", "transformation_load");
        loadTool.put("description", "Load a transformation from .ktr file content");
        ObjectNode loadSchema = objectMapper.createObjectNode();
        loadSchema.put("type", "object");
        ObjectNode loadProps = objectMapper.createObjectNode();
        ObjectNode contentSchema = objectMapper.createObjectNode();
        contentSchema.put("type", "string");
        contentSchema.put("description", "XML content of the .ktr file");
        loadProps.set("content", contentSchema);
        loadSchema.set("properties", loadProps);
        ArrayNode loadRequired = objectMapper.createArrayNode();
        loadRequired.add("content");
        loadSchema.set("required", loadRequired);
        loadTool.set("inputSchema", loadSchema);
        tools.add(loadTool);
        
        // Execute transformation
        ObjectNode executeTool = objectMapper.createObjectNode();
        executeTool.put("name", "transformation_execute");
        executeTool.put("description", "Execute a loaded transformation");
        ObjectNode executeSchema = objectMapper.createObjectNode();
        executeSchema.put("type", "object");
        ObjectNode executeProps = objectMapper.createObjectNode();
        ObjectNode idSchema = objectMapper.createObjectNode();
        idSchema.put("type", "string");
        idSchema.put("description", "ID of the loaded transformation");
        executeProps.set("id", idSchema);
        ObjectNode paramsSchema = objectMapper.createObjectNode();
        paramsSchema.put("type", "object");
        paramsSchema.put("description", "Parameters to pass to the transformation");
        executeProps.set("parameters", paramsSchema);
        executeSchema.set("properties", executeProps);
        ArrayNode executeRequired = objectMapper.createArrayNode();
        executeRequired.add("id");
        executeSchema.set("required", executeRequired);
        executeTool.set("inputSchema", executeSchema);
        tools.add(executeTool);
        
        // Get transformation status
        ObjectNode statusTool = objectMapper.createObjectNode();
        statusTool.put("name", "transformation_status");
        statusTool.put("description", "Get the status of a transformation");
        ObjectNode statusSchema = objectMapper.createObjectNode();
        statusSchema.put("type", "object");
        ObjectNode statusProps = objectMapper.createObjectNode();
        statusProps.set("id", idSchema);
        statusSchema.set("properties", statusProps);
        ArrayNode statusRequired = objectMapper.createArrayNode();
        statusRequired.add("id");
        statusSchema.set("required", statusRequired);
        statusTool.set("inputSchema", statusSchema);
        tools.add(statusTool);
        
        // Get transformation logs
        ObjectNode logsTool = objectMapper.createObjectNode();
        logsTool.put("name", "transformation_logs");
        logsTool.put("description", "Get the logs of a transformation");
        logsTool.set("inputSchema", statusSchema);  // Same schema as status
        tools.add(logsTool);
        
        return tools;
    }
    
    public JsonNode handleToolCall(String id, String toolName, JsonNode arguments) {
        try {
            switch (toolName) {
                case "transformation_create":
                    return handleCreate(id, arguments);
                case "transformation_load":
                    return handleLoad(id, arguments);
                case "transformation_execute":
                    return handleExecute(id, arguments);
                case "transformation_status":
                    return handleStatus(id, arguments);
                case "transformation_logs":
                    return handleLogs(id, arguments);
                default:
                    return createErrorResponse(id, "Unknown transformation tool: " + toolName);
            }
        } catch (Exception e) {
            log.logError("Error handling transformation tool call: " + toolName, e);
            return createErrorResponse(id, "Error: " + e.getMessage());
        }
    }
    
    private JsonNode handleCreate(String id, JsonNode arguments) throws KettleException {
        String name = arguments.path("name").asText();
        String description = arguments.path("description").asText("");
        
        TransMeta transMeta = new TransMeta();
        transMeta.setName(name);
        transMeta.setDescription(description);
        
        String transId = UUID.randomUUID().toString();
        CarteObjectEntry entry = new CarteObjectEntry(transId, null);
        
        TransConfiguration transConfig = new TransConfiguration(transMeta, new TransExecutionConfiguration());
        transformationMap.addTransformation(entry.getName(), entry.getId(), null, transConfig);
        
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("id", transId);
        result.put("name", name);
        result.put("description", description);
        result.put("xml", transMeta.getXML());
        response.set("result", result);
        
        return response;
    }
    
    private JsonNode handleLoad(String id, JsonNode arguments) throws KettleException {
        String content = arguments.path("content").asText();
        
        TransMeta transMeta = new TransMeta(new StringReader(content), null, false, null, null);
        
        String transId = UUID.randomUUID().toString();
        CarteObjectEntry entry = new CarteObjectEntry(transId, null);
        
        TransConfiguration transConfig = new TransConfiguration(transMeta, new TransExecutionConfiguration());
        transformationMap.addTransformation(entry.getName(), entry.getId(), null, transConfig);
        
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("id", transId);
        result.put("name", transMeta.getName());
        result.put("description", transMeta.getDescription());
        response.set("result", result);
        
        return response;
    }
    
    private JsonNode handleExecute(String id, JsonNode arguments) throws KettleException {
        String transId = arguments.path("id").asText();
        JsonNode params = arguments.path("parameters");
        
        CarteObjectEntry entry = transformationMap.getTransformationMap().keySet().stream()
            .filter(e -> e.getId().equals(transId))
            .findFirst()
            .orElseThrow(() -> new KettleException("Transformation not found: " + transId));
        
        Trans trans = transformationMap.getTransformation(entry);
        if (trans == null) {
            throw new KettleException("Transformation not found in map: " + transId);
        }
        
        // Set parameters if provided
        if (params != null && params.isObject()) {
            params.fields().forEachRemaining(field -> {
                try {
                    trans.setParameterValue(field.getKey(), field.getValue().asText());
                } catch (KettleException e) {
                    log.logError("Error setting parameter " + field.getKey(), e);
                }
            });
        }
        
        trans.execute(null);
        
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("id", transId);
        result.put("status", "STARTED");
        result.put("message", "Transformation execution started");
        response.set("result", result);
        
        return response;
    }
    
    private JsonNode handleStatus(String id, JsonNode arguments) throws KettleException {
        String transId = arguments.path("id").asText();
        
        CarteObjectEntry entry = transformationMap.getTransformationMap().keySet().stream()
            .filter(e -> e.getId().equals(transId))
            .findFirst()
            .orElseThrow(() -> new KettleException("Transformation not found: " + transId));
        
        Trans trans = transformationMap.getTransformation(entry);
        if (trans == null) {
            throw new KettleException("Transformation not found in map: " + transId);
        }
        
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("id", transId);
        result.put("name", trans.getTransMeta().getName());
        result.put("status", trans.getStatus());
        result.put("errors", trans.getErrors());
        result.put("finished", trans.isFinished());
        result.put("running", trans.isRunning());
        response.set("result", result);
        
        return response;
    }
    
    private JsonNode handleLogs(String id, JsonNode arguments) throws KettleException {
        String transId = arguments.path("id").asText();
        
        CarteObjectEntry entry = transformationMap.getTransformationMap().keySet().stream()
            .filter(e -> e.getId().equals(transId))
            .findFirst()
            .orElseThrow(() -> new KettleException("Transformation not found: " + transId));
        
        Trans trans = transformationMap.getTransformation(entry);
        if (trans == null) {
            throw new KettleException("Transformation not found in map: " + transId);
        }
        
        String logText = trans.getLogChannel().getLogChannelId();
        
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("id", transId);
        result.put("logs", logText);
        response.set("result", result);
        
        return response;
    }
    
    private JsonNode createErrorResponse(String id, String message) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        
        ObjectNode error = objectMapper.createObjectNode();
        error.put("code", -32000);
        error.put("message", message);
        response.set("error", error);
        
        return response;
    }
}