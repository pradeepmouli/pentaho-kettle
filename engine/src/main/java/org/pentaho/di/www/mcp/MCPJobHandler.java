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
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobConfiguration;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.www.CarteObjectEntry;
import org.pentaho.di.www.JobMap;

import java.io.StringReader;
import java.util.UUID;

/**
 * Handles MCP tool calls for Kettle jobs (.kjb files)
 */
public class MCPJobHandler {
    
    private final LogChannelInterface log;
    private final JobMap jobMap;
    private final ObjectMapper objectMapper;
    
    public MCPJobHandler(LogChannelInterface log, JobMap jobMap, ObjectMapper objectMapper) {
        this.log = log;
        this.jobMap = jobMap;
        this.objectMapper = objectMapper;
    }
    
    public ArrayNode getToolDefinitions() {
        ArrayNode tools = objectMapper.createArrayNode();
        
        // Create job
        ObjectNode createTool = objectMapper.createObjectNode();
        createTool.put("name", "job_create");
        createTool.put("description", "Create a new Kettle job (.kjb file)");
        ObjectNode createSchema = objectMapper.createObjectNode();
        createSchema.put("type", "object");
        ObjectNode createProps = objectMapper.createObjectNode();
        ObjectNode nameSchema = objectMapper.createObjectNode();
        nameSchema.put("type", "string");
        nameSchema.put("description", "Name of the job");
        createProps.set("name", nameSchema);
        ObjectNode descSchema = objectMapper.createObjectNode();
        descSchema.put("type", "string");
        descSchema.put("description", "Description of the job");
        createProps.set("description", descSchema);
        createSchema.set("properties", createProps);
        ArrayNode createRequired = objectMapper.createArrayNode();
        createRequired.add("name");
        createSchema.set("required", createRequired);
        createTool.set("inputSchema", createSchema);
        tools.add(createTool);
        
        // Load job
        ObjectNode loadTool = objectMapper.createObjectNode();
        loadTool.put("name", "job_load");
        loadTool.put("description", "Load a job from .kjb file content");
        ObjectNode loadSchema = objectMapper.createObjectNode();
        loadSchema.put("type", "object");
        ObjectNode loadProps = objectMapper.createObjectNode();
        ObjectNode contentSchema = objectMapper.createObjectNode();
        contentSchema.put("type", "string");
        contentSchema.put("description", "XML content of the .kjb file");
        loadProps.set("content", contentSchema);
        loadSchema.set("properties", loadProps);
        ArrayNode loadRequired = objectMapper.createArrayNode();
        loadRequired.add("content");
        loadSchema.set("required", loadRequired);
        loadTool.set("inputSchema", loadSchema);
        tools.add(loadTool);
        
        // Execute job
        ObjectNode executeTool = objectMapper.createObjectNode();
        executeTool.put("name", "job_execute");
        executeTool.put("description", "Execute a loaded job");
        ObjectNode executeSchema = objectMapper.createObjectNode();
        executeSchema.put("type", "object");
        ObjectNode executeProps = objectMapper.createObjectNode();
        ObjectNode idSchema = objectMapper.createObjectNode();
        idSchema.put("type", "string");
        idSchema.put("description", "ID of the loaded job");
        executeProps.set("id", idSchema);
        ObjectNode paramsSchema = objectMapper.createObjectNode();
        paramsSchema.put("type", "object");
        paramsSchema.put("description", "Parameters to pass to the job");
        executeProps.set("parameters", paramsSchema);
        executeSchema.set("properties", executeProps);
        ArrayNode executeRequired = objectMapper.createArrayNode();
        executeRequired.add("id");
        executeSchema.set("required", executeRequired);
        executeTool.set("inputSchema", executeSchema);
        tools.add(executeTool);
        
        // Get job status
        ObjectNode statusTool = objectMapper.createObjectNode();
        statusTool.put("name", "job_status");
        statusTool.put("description", "Get the status of a job");
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
        
        // Get job logs
        ObjectNode logsTool = objectMapper.createObjectNode();
        logsTool.put("name", "job_logs");
        logsTool.put("description", "Get the logs of a job");
        logsTool.set("inputSchema", statusSchema);  // Same schema as status
        tools.add(logsTool);
        
        return tools;
    }
    
    public JsonNode handleToolCall(String id, String toolName, JsonNode arguments) {
        try {
            switch (toolName) {
                case "job_create":
                    return handleCreate(id, arguments);
                case "job_load":
                    return handleLoad(id, arguments);
                case "job_execute":
                    return handleExecute(id, arguments);
                case "job_status":
                    return handleStatus(id, arguments);
                case "job_logs":
                    return handleLogs(id, arguments);
                default:
                    return createErrorResponse(id, "Unknown job tool: " + toolName);
            }
        } catch (Exception e) {
            log.logError("Error handling job tool call: " + toolName, e);
            return createErrorResponse(id, "Error: " + e.getMessage());
        }
    }
    
    private JsonNode handleCreate(String id, JsonNode arguments) throws KettleException {
        String name = arguments.path("name").asText();
        String description = arguments.path("description").asText("");
        
        JobMeta jobMeta = new JobMeta();
        jobMeta.setName(name);
        jobMeta.setDescription(description);
        
        String jobId = UUID.randomUUID().toString();
        CarteObjectEntry entry = new CarteObjectEntry(jobId, null);
        
        JobConfiguration jobConfig = new JobConfiguration(jobMeta, new JobExecutionConfiguration());
        jobMap.addJob(entry.getName(), entry.getId(), null, jobConfig);
        
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("id", jobId);
        result.put("name", name);
        result.put("description", description);
        result.put("xml", jobMeta.getXML());
        response.set("result", result);
        
        return response;
    }
    
    private JsonNode handleLoad(String id, JsonNode arguments) throws KettleException {
        String content = arguments.path("content").asText();
        
        JobMeta jobMeta = new JobMeta(new StringReader(content), null, null);
        
        String jobId = UUID.randomUUID().toString();
        CarteObjectEntry entry = new CarteObjectEntry(jobId, null);
        
        JobConfiguration jobConfig = new JobConfiguration(jobMeta, new JobExecutionConfiguration());
        jobMap.addJob(entry.getName(), entry.getId(), null, jobConfig);
        
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("id", jobId);
        result.put("name", jobMeta.getName());
        result.put("description", jobMeta.getDescription());
        response.set("result", result);
        
        return response;
    }
    
    private JsonNode handleExecute(String id, JsonNode arguments) throws KettleException {
        String jobId = arguments.path("id").asText();
        JsonNode params = arguments.path("parameters");
        
        CarteObjectEntry entry = jobMap.getJobMap().keySet().stream()
            .filter(e -> e.getId().equals(jobId))
            .findFirst()
            .orElseThrow(() -> new KettleException("Job not found: " + jobId));
        
        Job job = jobMap.getJob(entry);
        if (job == null) {
            throw new KettleException("Job not found in map: " + jobId);
        }
        
        // Set parameters if provided
        if (params != null && params.isObject()) {
            params.fields().forEachRemaining(field -> {
                try {
                    job.setParameterValue(field.getKey(), field.getValue().asText());
                } catch (KettleException e) {
                    log.logError("Error setting parameter " + field.getKey(), e);
                }
            });
        }
        
        job.start();
        
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("id", jobId);
        result.put("status", "STARTED");
        result.put("message", "Job execution started");
        response.set("result", result);
        
        return response;
    }
    
    private JsonNode handleStatus(String id, JsonNode arguments) throws KettleException {
        String jobId = arguments.path("id").asText();
        
        CarteObjectEntry entry = jobMap.getJobMap().keySet().stream()
            .filter(e -> e.getId().equals(jobId))
            .findFirst()
            .orElseThrow(() -> new KettleException("Job not found: " + jobId));
        
        Job job = jobMap.getJob(entry);
        if (job == null) {
            throw new KettleException("Job not found in map: " + jobId);
        }
        
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("id", jobId);
        result.put("name", job.getJobMeta().getName());
        result.put("status", job.getStatus());
        result.put("errors", job.getErrors());
        result.put("finished", job.isFinished());
        result.put("running", job.isRunning());
        response.set("result", result);
        
        return response;
    }
    
    private JsonNode handleLogs(String id, JsonNode arguments) throws KettleException {
        String jobId = arguments.path("id").asText();
        
        CarteObjectEntry entry = jobMap.getJobMap().keySet().stream()
            .filter(e -> e.getId().equals(jobId))
            .findFirst()
            .orElseThrow(() -> new KettleException("Job not found: " + jobId));
        
        Job job = jobMap.getJob(entry);
        if (job == null) {
            throw new KettleException("Job not found in map: " + jobId);
        }
        
        String logText = job.getLogChannel().getLogChannelId();
        
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("id", jobId);
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