#!/usr/bin/env python3
"""
Demonstration client for the Kettle MCP Server

This script shows how to interact with the MCP server to create and execute
Kettle transformations and jobs.
"""

import json
import socket
import sys

class MCPClient:
    def __init__(self, host='localhost', port=9080):
        """Initialize MCP client. Port is typically Carte port + 1000"""
        self.host = host
        self.port = port
        self.socket = None
        
    def connect(self):
        """Connect to the MCP server"""
        try:
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.socket.connect((self.host, self.port))
            print(f"Connected to MCP server at {self.host}:{self.port}")
            return True
        except Exception as e:
            print(f"Failed to connect: {e}")
            return False
    
    def send_request(self, method, params=None, request_id="1"):
        """Send a JSON-RPC request to the server"""
        request = {
            "jsonrpc": "2.0",
            "id": request_id,
            "method": method
        }
        if params:
            request["params"] = params
            
        message = json.dumps(request) + "\n"
        self.socket.send(message.encode())
        
        response = self.socket.recv(4096).decode().strip()
        return json.loads(response)
    
    def initialize(self):
        """Initialize the MCP connection"""
        params = {
            "protocolVersion": "2024-11-05",
            "capabilities": {}
        }
        return self.send_request("initialize", params)
    
    def list_tools(self):
        """List available tools"""
        return self.send_request("tools/list")
    
    def call_tool(self, tool_name, arguments):
        """Call a specific tool"""
        params = {
            "name": tool_name,
            "arguments": arguments
        }
        return self.send_request("tools/call", params)
    
    def close(self):
        """Close the connection"""
        if self.socket:
            self.socket.close()

def demo():
    """Demonstrate MCP client usage"""
    client = MCPClient()
    
    if not client.connect():
        return
    
    try:
        # Initialize
        print("\n1. Initializing...")
        response = client.initialize()
        print(f"Server: {response['result']['serverInfo']['name']} v{response['result']['serverInfo']['version']}")
        
        # List tools
        print("\n2. Listing available tools...")
        response = client.list_tools()
        tools = response['result']['tools']
        print(f"Available tools ({len(tools)}):")
        for tool in tools:
            print(f"  - {tool['name']}: {tool['description']}")
        
        # Create a transformation
        print("\n3. Creating a transformation...")
        response = client.call_tool("transformation_create", {
            "name": "Demo Transformation",
            "description": "A demonstration transformation created via MCP"
        })
        
        if 'result' in response:
            trans_id = response['result']['id']
            print(f"Created transformation with ID: {trans_id}")
            print(f"Name: {response['result']['name']}")
            
            # Get transformation status
            print("\n4. Checking transformation status...")
            status_response = client.call_tool("transformation_status", {"id": trans_id})
            if 'result' in status_response:
                print(f"Status: {status_response['result']['status']}")
                print(f"Running: {status_response['result']['running']}")
                print(f"Finished: {status_response['result']['finished']}")
        else:
            print(f"Error creating transformation: {response.get('error', 'Unknown error')}")
        
        # Create a job
        print("\n5. Creating a job...")
        response = client.call_tool("job_create", {
            "name": "Demo Job",
            "description": "A demonstration job created via MCP"
        })
        
        if 'result' in response:
            job_id = response['result']['id']
            print(f"Created job with ID: {job_id}")
            print(f"Name: {response['result']['name']}")
            
            # Get job status
            print("\n6. Checking job status...")
            status_response = client.call_tool("job_status", {"id": job_id})
            if 'result' in status_response:
                print(f"Status: {status_response['result']['status']}")
                print(f"Running: {status_response['result']['running']}")
                print(f"Finished: {status_response['result']['finished']}")
        else:
            print(f"Error creating job: {response.get('error', 'Unknown error')}")
            
    except Exception as e:
        print(f"Error during demonstration: {e}")
    finally:
        client.close()
        print("\nDemo completed. Connection closed.")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        port = int(sys.argv[1])
        client = MCPClient(port=port)
    else:
        print("Usage: python3 mcp_demo.py [port]")
        print("Default port is 9080 (assuming Carte on 8080)")
        print("\nThis demo requires the Kettle MCP server to be running.")
        print("Start Carte web server first, then run this demo.")
        demo()