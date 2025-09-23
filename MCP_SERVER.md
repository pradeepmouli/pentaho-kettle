# MCP Server for Pentaho Data Integration (Kettle)

This MCP (Model Context Protocol) server provides programmatic access to create, manipulate, configure, and execute Kettle transformations (.ktr files) and jobs (.kjb files).

## Overview

The MCP server automatically starts when the Carte web server starts, listening on port `CARTE_PORT + 1000`. It implements the MCP 2024-11-05 protocol specification and provides tools for working with Kettle transformations and jobs.

## Connection

The MCP server runs on TCP port `CARTE_PORT + 1000` and uses JSON-RPC 2.0 over line-delimited JSON.

## Available Tools

### Transformation Tools

#### `transformation_create`
Creates a new empty Kettle transformation.

**Parameters:**
- `name` (string, required): Name of the transformation
- `description` (string, optional): Description of the transformation

**Returns:**
- `id` (string): Unique identifier for the transformation
- `name` (string): Name of the transformation  
- `description` (string): Description of the transformation
- `xml` (string): XML representation of the transformation

#### `transformation_load`
Loads a transformation from .ktr file content.

**Parameters:**
- `content` (string, required): XML content of the .ktr file

**Returns:**
- `id` (string): Unique identifier for the transformation
- `name` (string): Name of the transformation
- `description` (string): Description of the transformation

#### `transformation_execute`
Executes a loaded transformation.

**Parameters:**
- `id` (string, required): ID of the loaded transformation
- `parameters` (object, optional): Parameters to pass to the transformation

**Returns:**
- `id` (string): Transformation ID
- `status` (string): Execution status
- `message` (string): Status message

#### `transformation_status`
Gets the status of a transformation.

**Parameters:**
- `id` (string, required): ID of the transformation

**Returns:**
- `id` (string): Transformation ID
- `name` (string): Transformation name
- `status` (string): Current status
- `errors` (number): Number of errors
- `finished` (boolean): Whether execution is finished
- `running` (boolean): Whether execution is running

#### `transformation_logs`
Gets the logs of a transformation.

**Parameters:**
- `id` (string, required): ID of the transformation

**Returns:**
- `id` (string): Transformation ID
- `logs` (string): Log content

### Job Tools

#### `job_create`
Creates a new empty Kettle job.

**Parameters:**
- `name` (string, required): Name of the job
- `description` (string, optional): Description of the job

**Returns:**
- `id` (string): Unique identifier for the job
- `name` (string): Name of the job
- `description` (string): Description of the job
- `xml` (string): XML representation of the job

#### `job_load`
Loads a job from .kjb file content.

**Parameters:**
- `content` (string, required): XML content of the .kjb file

**Returns:**
- `id` (string): Unique identifier for the job
- `name` (string): Name of the job
- `description` (string): Description of the job

#### `job_execute`
Executes a loaded job.

**Parameters:**
- `id` (string, required): ID of the loaded job
- `parameters` (object, optional): Parameters to pass to the job

**Returns:**
- `id` (string): Job ID
- `status` (string): Execution status
- `message` (string): Status message

#### `job_status`
Gets the status of a job.

**Parameters:**
- `id` (string, required): ID of the job

**Returns:**
- `id` (string): Job ID
- `name` (string): Job name
- `status` (string): Current status
- `errors` (number): Number of errors
- `finished` (boolean): Whether execution is finished
- `running` (boolean): Whether execution is running

#### `job_logs`
Gets the logs of a job.

**Parameters:**
- `id` (string, required): ID of the job

**Returns:**
- `id` (string): Job ID
- `logs` (string): Log content

## Example Usage

Here's an example of creating and executing a transformation:

```json
// 1. Initialize connection
{"jsonrpc": "2.0", "id": "1", "method": "initialize", "params": {"protocolVersion": "2024-11-05", "capabilities": {}}}

// 2. List available tools
{"jsonrpc": "2.0", "id": "2", "method": "tools/list"}

// 3. Create a new transformation
{"jsonrpc": "2.0", "id": "3", "method": "tools/call", "params": {"name": "transformation_create", "arguments": {"name": "My Transformation", "description": "Test transformation"}}}

// 4. Execute the transformation (using ID from step 3)
{"jsonrpc": "2.0", "id": "4", "method": "tools/call", "params": {"name": "transformation_execute", "arguments": {"id": "generated-uuid", "parameters": {"param1": "value1"}}}}

// 5. Check status
{"jsonrpc": "2.0", "id": "5", "method": "tools/call", "params": {"name": "transformation_status", "arguments": {"id": "generated-uuid"}}}
```

## Integration

The MCP server is automatically started by the Carte web server and uses the same TransformationMap and JobMap instances, ensuring consistency with the web interface and other Carte features.