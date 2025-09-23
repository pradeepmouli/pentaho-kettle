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

package org.pentaho.di.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Represents an MCP (Model Context Protocol) request message
 */
public class McpRequest {
  @JsonProperty("jsonrpc")
  private String jsonrpc = "2.0";
  
  @JsonProperty("id")
  private String id;
  
  @JsonProperty("method")
  private String method;
  
  @JsonProperty("params")
  private Map<String, Object> params;

  public McpRequest() {}

  public McpRequest(String id, String method, Map<String, Object> params) {
    this.id = id;
    this.method = method;
    this.params = params;
  }

  public String getJsonrpc() {
    return jsonrpc;
  }

  public void setJsonrpc(String jsonrpc) {
    this.jsonrpc = jsonrpc;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public void setParams(Map<String, Object> params) {
    this.params = params;
  }
}