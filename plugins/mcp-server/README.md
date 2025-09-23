# MCP Server Plugin for Pentaho Kettle

## Overview

The MCP (Model Context Protocol) Server plugin provides a JSON-RPC 2.0 compatible API for creating, manipulating, configuring, and executing Pentaho Kettle transformations (.ktr) and jobs (.kjb) remotely.

## Installation

The MCP server plugin is included as part of the Pentaho Kettle distribution. It will be automatically loaded when the Carte server starts.

## Configuration

The MCP server is available at the following endpoint:
```
POST http://{hostname}:{port}/kettle/mcp
```

## API Methods

### Server Methods

#### server/capabilities
Returns the capabilities of the MCP server.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "server/capabilities",
  "params": {}
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "result": {
    "server": {
      "name": "Pentaho Kettle MCP Server",
      "version": "1.0.0"
    },
    "methods": [
      "transformation/create",
      "transformation/load",
      "transformation/save",
      "transformation/execute",
      "transformation/status",
      "transformation/stop",
      "transformation/list",
      "transformation/delete",
      "job/create",
      "job/load",
      "job/save",
      "job/execute", 
      "job/status",
      "job/stop",
      "job/list",
      "job/delete",
      "server/capabilities",
      "server/status"
    ]
  }
}
```

#### server/status
Returns the current status of the server.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": "2",
  "method": "server/status",
  "params": {}
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": "2",
  "result": {
    "running": true,
    "transformations": ["trans1", "trans2"],
    "jobs": ["job1", "job2"]
  }
}
```

### Transformation Methods

#### transformation/create
Creates a new transformation.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": "3",
  "method": "transformation/create",
  "params": {
    "name": "MyTransformation",
    "description": "A sample transformation"
  }
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": "3",
  "result": {
    "id": "uuid-string",
    "name": "MyTransformation",
    "status": "created"
  }
}
```

#### transformation/load
Loads a transformation from file.

**Request:**
```json
{
  "jsonrpc": "2.0", 
  "id": "4",
  "method": "transformation/load",
  "params": {
    "filename": "/path/to/transformation.ktr"
  }
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": "4", 
  "result": {
    "id": "uuid-string",
    "name": "LoadedTransformation",
    "filename": "/path/to/transformation.ktr",
    "status": "loaded"
  }
}
```

#### transformation/execute
Executes a transformation.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": "5",
  "method": "transformation/execute",
  "params": {
    "id": "uuid-string"
  }
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": "5",
  "result": {
    "id": "uuid-string",
    "name": "MyTransformation",
    "status": "running"
  }
}
```

#### transformation/status
Gets the status of a transformation.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": "6",
  "method": "transformation/status",
  "params": {
    "id": "uuid-string"
  }
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": "6",
  "result": {
    "id": "uuid-string",
    "name": "MyTransformation",
    "status": "finished",
    "finished": true,
    "errors": 0
  }
}
```

#### transformation/stop
Stops a running transformation.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": "7",
  "method": "transformation/stop",
  "params": {
    "id": "uuid-string"
  }
}
```

#### transformation/list
Lists all transformations.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": "8",
  "method": "transformation/list",
  "params": {}
}
```

#### transformation/delete
Deletes a transformation.

**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": "9",
  "method": "transformation/delete",
  "params": {
    "id": "uuid-string"
  }
}
```

### Job Methods

Job methods follow the same pattern as transformation methods, with "job" prefix instead of "transformation":

- `job/create`
- `job/load`
- `job/save`
- `job/execute`
- `job/status`
- `job/stop`
- `job/list`
- `job/delete`

## Error Handling

The MCP server follows JSON-RPC 2.0 error handling conventions:

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "error": {
    "code": -32602,
    "message": "Invalid params",
    "data": "Transformation name is required"
  }
}
```

## Common Error Codes

- `-32700`: Parse error
- `-32600`: Invalid Request
- `-32601`: Method not found
- `-32602`: Invalid params
- `-32603`: Internal error

## Usage Examples

### Python Client Example

```python
import requests
import json

def call_mcp_method(method, params, request_id="1"):
    url = "http://localhost:8080/kettle/mcp"
    payload = {
        "jsonrpc": "2.0",
        "id": request_id,
        "method": method,
        "params": params
    }
    
    response = requests.post(url, json=payload)
    return response.json()

# Create a transformation
result = call_mcp_method("transformation/create", {
    "name": "TestTransformation",
    "description": "A test transformation"
})

print(f"Created transformation: {result}")

# Get server capabilities
capabilities = call_mcp_method("server/capabilities", {})
print(f"Server capabilities: {capabilities}")
```

### curl Example

```bash
curl -X POST http://localhost:8080/kettle/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "server/capabilities",
    "params": {}
  }'
```

## Security Considerations

The MCP server currently runs on the same security context as the Carte server. Ensure proper authentication and authorization mechanisms are in place when deploying in production environments.