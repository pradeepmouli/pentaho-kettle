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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pentaho.di.mcp.model.McpRequest;
import org.pentaho.di.mcp.model.McpResponse;
import org.pentaho.di.www.JobMap;
import org.pentaho.di.www.TransformationMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for McpServerServlet
 */
public class McpServerServletTest {

  @Mock
  private TransformationMap transformationMap;
  
  @Mock
  private JobMap jobMap;
  
  private McpServerServlet servlet;
  private ObjectMapper objectMapper = new ObjectMapper();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    servlet = new McpServerServlet();
    // Note: In real tests, we'd need to set the transformation and job maps
    // servlet.setTransformationMap(transformationMap);
    // servlet.setJobMap(jobMap);
  }

  @Test
  public void testGetContextPath() {
    assertEquals("/kettle/mcp", servlet.getContextPath());
  }

  @Test
  public void testCreateMcpRequest() throws IOException {
    Map<String, Object> params = new HashMap<>();
    params.put("name", "test-transformation");
    
    McpRequest request = new McpRequest("1", "transformation/create", params);
    
    assertEquals("2.0", request.getJsonrpc());
    assertEquals("1", request.getId());
    assertEquals("transformation/create", request.getMethod());
    assertEquals("test-transformation", request.getParams().get("name"));
  }

  @Test
  public void testCreateMcpResponse() {
    Map<String, Object> result = new HashMap<>();
    result.put("id", "123");
    result.put("status", "created");
    
    McpResponse response = new McpResponse("1", result);
    
    assertEquals("2.0", response.getJsonrpc());
    assertEquals("1", response.getId());
    assertNotNull(response.getResult());
    assertNull(response.getError());
  }

  @Test
  public void testCreateMcpErrorResponse() {
    McpResponse.McpError error = new McpResponse.McpError(-32602, "Invalid params");
    McpResponse response = new McpResponse("1", error);
    
    assertEquals("2.0", response.getJsonrpc());
    assertEquals("1", response.getId());
    assertNull(response.getResult());
    assertNotNull(response.getError());
    assertEquals(-32602, response.getError().getCode());
    assertEquals("Invalid params", response.getError().getMessage());
  }
}