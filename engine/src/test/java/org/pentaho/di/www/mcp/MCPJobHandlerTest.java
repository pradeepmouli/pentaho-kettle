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

import static org.junit.Assert.*;

/**
 * Test class for MCPJobHandler
 */
public class MCPJobHandlerTest {
    
    private MCPJobHandler handler;
    private ObjectMapper objectMapper;
    
    @Before
    public void setUp() {
        LogChannel log = new LogChannel("Test");
        JobMap jobMap = new JobMap();
        objectMapper = new ObjectMapper();
        handler = new MCPJobHandler(log, jobMap, objectMapper);
    }
    
    @Test
    public void testGetToolDefinitions() {
        JsonNode tools = handler.getToolDefinitions();
        assertNotNull("Tool definitions should not be null", tools);
        assertTrue("Should have tools defined", tools.size() > 0);
        
        // Check that we have the expected job tools
        boolean hasCreate = false, hasLoad = false, hasExecute = false, hasStatus = false, hasLogs = false;
        
        for (int i = 0; i < tools.size(); i++) {
            JsonNode tool = tools.get(i);
            String name = tool.path("name").asText();
            
            switch (name) {
                case "job_create":
                    hasCreate = true;
                    assertEquals("Create tool should have correct description", 
                        "Create a new Kettle job (.kjb file)", tool.path("description").asText());
                    break;
                case "job_load":
                    hasLoad = true;
                    break;
                case "job_execute":
                    hasExecute = true;
                    break;
                case "job_status":
                    hasStatus = true;
                    break;
                case "job_logs":
                    hasLogs = true;
                    break;
            }
        }
        
        assertTrue("Should have job_create tool", hasCreate);
        assertTrue("Should have job_load tool", hasLoad);
        assertTrue("Should have job_execute tool", hasExecute);
        assertTrue("Should have job_status tool", hasStatus);
        assertTrue("Should have job_logs tool", hasLogs);
    }
    
    @Test
    public void testCreateJob() throws Exception {
        JsonNode arguments = objectMapper.createObjectNode()
            .put("name", "Test Job")
            .put("description", "A test job");
            
        JsonNode response = handler.handleToolCall("test-id", "job_create", arguments);
        
        assertNotNull("Response should not be null", response);
        assertEquals("Should be valid JSON-RPC response", "2.0", response.path("jsonrpc").asText());
        assertEquals("Should have correct ID", "test-id", response.path("id").asText());
        
        JsonNode result = response.path("result");
        assertNotNull("Result should not be null", result);
        assertTrue("Should have job ID", result.has("id"));
        assertEquals("Should have correct name", "Test Job", result.path("name").asText());
        assertEquals("Should have correct description", "A test job", result.path("description").asText());
        assertTrue("Should have XML content", result.has("xml"));
    }
}