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
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.www.JobMap;
import org.pentaho.di.www.TransformationMap;

import static org.junit.Assert.*;

/**
 * Test class for MCPTransformationHandler
 */
public class MCPTransformationHandlerTest {
    
    private MCPTransformationHandler handler;
    private ObjectMapper objectMapper;
    
    @Before
    public void setUp() {
        LogChannel log = new LogChannel("Test");
        TransformationMap transformationMap = new TransformationMap();
        objectMapper = new ObjectMapper();
        handler = new MCPTransformationHandler(log, transformationMap, objectMapper);
    }
    
    @Test
    public void testGetToolDefinitions() {
        JsonNode tools = handler.getToolDefinitions();
        assertNotNull("Tool definitions should not be null", tools);
        assertTrue("Should have tools defined", tools.size() > 0);
        
        // Check that we have the expected transformation tools
        boolean hasCreate = false, hasLoad = false, hasExecute = false, hasStatus = false, hasLogs = false;
        
        for (int i = 0; i < tools.size(); i++) {
            JsonNode tool = tools.get(i);
            String name = tool.path("name").asText();
            
            switch (name) {
                case "transformation_create":
                    hasCreate = true;
                    assertEquals("Create tool should have correct description", 
                        "Create a new Kettle transformation (.ktr file)", tool.path("description").asText());
                    break;
                case "transformation_load":
                    hasLoad = true;
                    break;
                case "transformation_execute":
                    hasExecute = true;
                    break;
                case "transformation_status":
                    hasStatus = true;
                    break;
                case "transformation_logs":
                    hasLogs = true;
                    break;
            }
        }
        
        assertTrue("Should have transformation_create tool", hasCreate);
        assertTrue("Should have transformation_load tool", hasLoad);
        assertTrue("Should have transformation_execute tool", hasExecute);
        assertTrue("Should have transformation_status tool", hasStatus);
        assertTrue("Should have transformation_logs tool", hasLogs);
    }
    
    @Test
    public void testCreateTransformation() throws Exception {
        JsonNode arguments = objectMapper.createObjectNode()
            .put("name", "Test Transformation")
            .put("description", "A test transformation");
            
        JsonNode response = handler.handleToolCall("test-id", "transformation_create", arguments);
        
        assertNotNull("Response should not be null", response);
        assertEquals("Should be valid JSON-RPC response", "2.0", response.path("jsonrpc").asText());
        assertEquals("Should have correct ID", "test-id", response.path("id").asText());
        
        JsonNode result = response.path("result");
        assertNotNull("Result should not be null", result);
        assertTrue("Should have transformation ID", result.has("id"));
        assertEquals("Should have correct name", "Test Transformation", result.path("name").asText());
        assertEquals("Should have correct description", "A test transformation", result.path("description").asText());
        assertTrue("Should have XML content", result.has("xml"));
    }
}