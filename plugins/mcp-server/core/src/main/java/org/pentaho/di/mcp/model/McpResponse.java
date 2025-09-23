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

/**
 * Represents an MCP (Model Context Protocol) response message
 */
public class McpResponse {
  @JsonProperty("jsonrpc")
  private String jsonrpc = "2.0";
  
  @JsonProperty("id")
  private String id;
  
  @JsonProperty("result")
  private Object result;
  
  @JsonProperty("error")
  private McpError error;

  public McpResponse() {}

  public McpResponse(String id, Object result) {
    this.id = id;
    this.result = result;
  }

  public McpResponse(String id, McpError error) {
    this.id = id;
    this.error = error;
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

  public Object getResult() {
    return result;
  }

  public void setResult(Object result) {
    this.result = result;
  }

  public McpError getError() {
    return error;
  }

  public void setError(McpError error) {
    this.error = error;
  }

  public static class McpError {
    @JsonProperty("code")
    private int code;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private Object data;

    public McpError() {}

    public McpError(int code, String message) {
      this.code = code;
      this.message = message;
    }

    public McpError(int code, String message, Object data) {
      this.code = code;
      this.message = message;
      this.data = data;
    }

    public int getCode() {
      return code;
    }

    public void setCode(int code) {
      this.code = code;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public Object getData() {
      return data;
    }

    public void setData(Object data) {
      this.data = data;
    }
  }
}