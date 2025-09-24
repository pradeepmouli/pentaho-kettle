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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pentaho.di.core.logging.LogChannelInterface;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API servlet for Spoon Web UI backend operations
 */
public class APIServlet extends HttpServlet {
  
  private final LogChannelInterface log;
  private final ObjectMapper objectMapper;
  
  public APIServlet(LogChannelInterface log) {
    this.log = log;
    this.objectMapper = new ObjectMapper();
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
      throws ServletException, IOException {
    
    String pathInfo = req.getPathInfo();
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    
    try {
      if (pathInfo == null || pathInfo.equals("/")) {
        handleRoot(resp);
      } else if (pathInfo.startsWith("/transformations")) {
        handleTransformations(req, resp);
      } else if (pathInfo.startsWith("/jobs")) {
        handleJobs(req, resp);
      } else if (pathInfo.startsWith("/connections")) {
        handleConnections(req, resp);
      } else if (pathInfo.startsWith("/steps")) {
        handleSteps(req, resp);
      } else if (pathInfo.startsWith("/entries")) {
        handleJobEntries(req, resp);
      } else {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        writeError(resp, "Endpoint not found");
      }
    } catch (Exception e) {
      log.logError("API Error: " + e.getMessage(), e);
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      writeError(resp, "Internal server error: " + e.getMessage());
    }
  }
  
  private void handleRoot(HttpServletResponse resp) throws IOException {
    Map<String, Object> info = new HashMap<>();
    info.put("name", "Spoon Web UI API");
    info.put("version", "1.0.0");
    info.put("status", "active");
    objectMapper.writeValue(resp.getWriter(), info);
  }
  
  private void handleTransformations(HttpServletRequest req, HttpServletResponse resp) 
      throws IOException {
    // Mock transformation data
    List<Map<String, Object>> transformations = new ArrayList<>();
    
    Map<String, Object> trans1 = new HashMap<>();
    trans1.put("id", "trans1");
    trans1.put("name", "Sample Transformation");
    trans1.put("description", "A sample transformation for demo");
    trans1.put("created", "2024-01-01");
    transformations.add(trans1);
    
    Map<String, Object> response = new HashMap<>();
    response.put("transformations", transformations);
    objectMapper.writeValue(resp.getWriter(), response);
  }
  
  private void handleJobs(HttpServletRequest req, HttpServletResponse resp) 
      throws IOException {
    // Mock job data
    List<Map<String, Object>> jobs = new ArrayList<>();
    
    Map<String, Object> job1 = new HashMap<>();
    job1.put("id", "job1");
    job1.put("name", "Sample Job");
    job1.put("description", "A sample job for demo");
    job1.put("created", "2024-01-01");
    jobs.add(job1);
    
    Map<String, Object> response = new HashMap<>();
    response.put("jobs", jobs);
    objectMapper.writeValue(resp.getWriter(), response);
  }
  
  private void handleConnections(HttpServletRequest req, HttpServletResponse resp) 
      throws IOException {
    // Mock database connection data
    List<Map<String, Object>> connections = new ArrayList<>();
    
    Map<String, Object> conn1 = new HashMap<>();
    conn1.put("id", "conn1");
    conn1.put("name", "Sample DB");
    conn1.put("type", "MySQL");
    conn1.put("host", "localhost");
    conn1.put("port", 3306);
    connections.add(conn1);
    
    Map<String, Object> response = new HashMap<>();
    response.put("connections", connections);
    objectMapper.writeValue(resp.getWriter(), response);
  }
  
  private void handleSteps(HttpServletRequest req, HttpServletResponse resp) 
      throws IOException {
    // Mock step types data
    Map<String, List<Map<String, Object>>> stepCategories = new HashMap<>();
    
    List<Map<String, Object>> inputSteps = new ArrayList<>();
    inputSteps.add(createStepInfo("TextFileInput", "Text file input", "Read data from text files"));
    inputSteps.add(createStepInfo("CSVInput", "CSV file input", "Read data from CSV files"));
    inputSteps.add(createStepInfo("ExcelInput", "Excel input", "Read data from Excel files"));
    stepCategories.put("Input", inputSteps);
    
    List<Map<String, Object>> outputSteps = new ArrayList<>();
    outputSteps.add(createStepInfo("TextFileOutput", "Text file output", "Write data to text files"));
    outputSteps.add(createStepInfo("DatabaseOutput", "Database output", "Write data to database"));
    stepCategories.put("Output", outputSteps);
    
    List<Map<String, Object>> transformSteps = new ArrayList<>();
    transformSteps.add(createStepInfo("SelectValues", "Select values", "Select and rename fields"));
    transformSteps.add(createStepInfo("FilterRows", "Filter rows", "Filter rows based on conditions"));
    stepCategories.put("Transform", transformSteps);
    
    objectMapper.writeValue(resp.getWriter(), stepCategories);
  }
  
  private void handleJobEntries(HttpServletRequest req, HttpServletResponse resp) 
      throws IOException {
    // Mock job entry types data
    Map<String, List<Map<String, Object>>> entryCategories = new HashMap<>();
    
    List<Map<String, Object>> generalEntries = new ArrayList<>();
    generalEntries.add(createEntryInfo("START", "START", "Start point of the job"));
    generalEntries.add(createEntryInfo("Dummy", "Dummy", "Dummy entry for testing"));
    generalEntries.add(createEntryInfo("Success", "Success", "Always succeeds"));
    entryCategories.put("General", generalEntries);
    
    List<Map<String, Object>> fileEntries = new ArrayList<>();
    fileEntries.add(createEntryInfo("CopyFiles", "Copy files", "Copy files from source to destination"));
    fileEntries.add(createEntryInfo("MoveFiles", "Move files", "Move files from source to destination"));
    entryCategories.put("File Management", fileEntries);
    
    objectMapper.writeValue(resp.getWriter(), entryCategories);
  }
  
  private Map<String, Object> createStepInfo(String id, String name, String description) {
    Map<String, Object> step = new HashMap<>();
    step.put("id", id);
    step.put("name", name);
    step.put("description", description);
    return step;
  }
  
  private Map<String, Object> createEntryInfo(String id, String name, String description) {
    Map<String, Object> entry = new HashMap<>();
    entry.put("id", id);
    entry.put("name", name);
    entry.put("description", description);
    return entry;
  }
  
  private void writeError(HttpServletResponse resp, String message) throws IOException {
    Map<String, Object> error = new HashMap<>();
    error.put("error", message);
    objectMapper.writeValue(resp.getWriter(), error);
  }
}