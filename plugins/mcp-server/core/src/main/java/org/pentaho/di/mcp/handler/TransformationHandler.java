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

package org.pentaho.di.mcp.handler;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.www.TransformationMap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles MCP requests for transformation operations
 */
public class TransformationHandler {
  
  private final TransformationMap transformationMap;
  private final LogChannelInterface log;
  
  public TransformationHandler(TransformationMap transformationMap, LogChannelInterface log) {
    this.transformationMap = transformationMap;
    this.log = log;
  }

  public Object createTransformation(Map<String, Object> params) throws KettleException {
    String name = (String) params.get("name");
    if (StringUtil.isEmpty(name)) {
      throw new KettleException("Transformation name is required");
    }
    
    // Create a new transformation
    TransMeta transMeta = new TransMeta();
    transMeta.setName(name);
    
    // Set optional description
    String description = (String) params.get("description");
    if (!StringUtil.isEmpty(description)) {
      transMeta.setDescription(description);
    }
    
    // Generate unique ID for this transformation
    String transId = UUID.randomUUID().toString();
    
    // Create and register the transformation
    Trans trans = new Trans(transMeta);
    transformationMap.addTransformation(name, transId, trans, null);
    
    Map<String, Object> result = new HashMap<>();
    result.put("id", transId);
    result.put("name", name);
    result.put("status", "created");
    
    log.logBasic("Created transformation: " + name + " with ID: " + transId);
    return result;
  }

  public Object loadTransformation(Map<String, Object> params) throws KettleException {
    String filename = (String) params.get("filename");
    String name = (String) params.get("name");
    
    if (StringUtil.isEmpty(filename) && StringUtil.isEmpty(name)) {
      throw new KettleException("Either filename or name must be provided");
    }
    
    TransMeta transMeta;
    if (!StringUtil.isEmpty(filename)) {
      transMeta = new TransMeta(filename);
    } else {
      // Load from repository by name - would need repository implementation
      throw new KettleException("Loading by name from repository not yet implemented");
    }
    
    String transId = UUID.randomUUID().toString();
    Trans trans = new Trans(transMeta);
    transformationMap.addTransformation(transMeta.getName(), transId, trans, null);
    
    Map<String, Object> result = new HashMap<>();
    result.put("id", transId);
    result.put("name", transMeta.getName());
    result.put("filename", transMeta.getFilename());
    result.put("status", "loaded");
    
    log.logBasic("Loaded transformation: " + transMeta.getName() + " with ID: " + transId);
    return result;
  }

  public Object saveTransformation(Map<String, Object> params) throws KettleException {
    String transId = (String) params.get("id");
    String filename = (String) params.get("filename");
    
    if (StringUtil.isEmpty(transId)) {
      throw new KettleException("Transformation ID is required");
    }
    
    Trans trans = transformationMap.getTransformation(transId);
    if (trans == null) {
      throw new KettleException("Transformation not found: " + transId);
    }
    
    if (!StringUtil.isEmpty(filename)) {
      trans.getTransMeta().setFilename(filename);
      trans.getTransMeta().saveSharedObjects();
      trans.getTransMeta().saveToRepository(null);
    }
    
    Map<String, Object> result = new HashMap<>();
    result.put("id", transId);
    result.put("name", trans.getTransMeta().getName());
    result.put("filename", trans.getTransMeta().getFilename());
    result.put("status", "saved");
    
    log.logBasic("Saved transformation: " + trans.getTransMeta().getName());
    return result;
  }

  public Object executeTransformation(Map<String, Object> params) throws KettleException {
    String transId = (String) params.get("id");
    
    if (StringUtil.isEmpty(transId)) {
      throw new KettleException("Transformation ID is required");
    }
    
    Trans trans = transformationMap.getTransformation(transId);
    if (trans == null) {
      throw new KettleException("Transformation not found: " + transId);
    }
    
    // Start the transformation
    trans.prepareExecution(null);
    trans.startThreads();
    
    Map<String, Object> result = new HashMap<>();
    result.put("id", transId);
    result.put("name", trans.getTransMeta().getName());
    result.put("status", "running");
    
    log.logBasic("Started transformation: " + trans.getTransMeta().getName());
    return result;
  }

  public Object getTransformationStatus(Map<String, Object> params) throws KettleException {
    String transId = (String) params.get("id");
    
    if (StringUtil.isEmpty(transId)) {
      throw new KettleException("Transformation ID is required");
    }
    
    Trans trans = transformationMap.getTransformation(transId);
    if (trans == null) {
      throw new KettleException("Transformation not found: " + transId);
    }
    
    Map<String, Object> result = new HashMap<>();
    result.put("id", transId);
    result.put("name", trans.getTransMeta().getName());
    result.put("status", trans.getStatus());
    result.put("finished", trans.isFinished());
    result.put("errors", trans.getErrors());
    
    return result;
  }

  public Object stopTransformation(Map<String, Object> params) throws KettleException {
    String transId = (String) params.get("id");
    
    if (StringUtil.isEmpty(transId)) {
      throw new KettleException("Transformation ID is required");
    }
    
    Trans trans = transformationMap.getTransformation(transId);
    if (trans == null) {
      throw new KettleException("Transformation not found: " + transId);
    }
    
    trans.stopAll();
    
    Map<String, Object> result = new HashMap<>();
    result.put("id", transId);
    result.put("name", trans.getTransMeta().getName());
    result.put("status", "stopped");
    
    log.logBasic("Stopped transformation: " + trans.getTransMeta().getName());
    return result;
  }

  public Object listTransformations(Map<String, Object> params) {
    return transformationMap.getTransformationNames();
  }

  public Object deleteTransformation(Map<String, Object> params) throws KettleException {
    String transId = (String) params.get("id");
    
    if (StringUtil.isEmpty(transId)) {
      throw new KettleException("Transformation ID is required");
    }
    
    Trans trans = transformationMap.getTransformation(transId);
    if (trans == null) {
      throw new KettleException("Transformation not found: " + transId);
    }
    
    String name = trans.getTransMeta().getName();
    
    // Stop if running
    if (!trans.isFinished()) {
      trans.stopAll();
    }
    
    // Remove from map
    transformationMap.removeTransformation(transId);
    
    Map<String, Object> result = new HashMap<>();
    result.put("id", transId);
    result.put("name", name);
    result.put("status", "deleted");
    
    log.logBasic("Deleted transformation: " + name);
    return result;
  }
}