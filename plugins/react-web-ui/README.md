# Spoon Web UI Plugin

A React-based web interface that serves as a modern replacement for the traditional desktop Spoon application in Pentaho Data Integration.

## Overview

This plugin provides a web-based interface for Pentaho Data Integration (Kettle/Spoon) that can be accessed through a web browser instead of the traditional desktop SWT application. It maintains the core functionality of Spoon while providing a modern, responsive web interface.

## Features

- **React-based UI**: Modern, component-based interface built with React and Material-UI
- **Transformation Editor**: Visual canvas for creating and editing transformations with drag-and-drop step palette
- **Job Editor**: Visual canvas for creating and editing jobs with job entry palette
- **REST API**: Backend API for managing transformations, jobs, connections, and other Kettle objects
- **Responsive Design**: Works on desktop and mobile devices
- **Embedded Web Server**: Self-contained Jetty server that serves the web interface

## Architecture

The plugin consists of several key components:

1. **React Frontend** (`src/main/webapp/`): The user interface built with React, TypeScript, and Material-UI
2. **Web Server** (`WebUIServer.java`): Embedded Jetty server that serves the React application
3. **REST API** (`APIServlet.java`): Backend API endpoints for data operations
4. **Spoon Plugin Integration** (`SpoonWebUIPlugin.java`): Integration with the existing Spoon plugin system
5. **Standalone Launcher** (`SpoonWebUI.java`): Alternative entry point for web-only mode

## Usage

### Option 1: Enable Web UI in Desktop Spoon

Start Spoon with web UI enabled alongside the desktop interface:

```bash
java -Dspoon.webui.enabled=true -Dspoon.webui.autoopen=true -cp ... org.pentaho.di.ui.spoon.Spoon
```

### Option 2: Web-Only Mode

Start Spoon in web-only mode (no desktop interface):

```bash
java -cp ... org.pentaho.di.ui.spoon.webui.SpoonWebUI --no-desktop --webui-port 8080
```

### Option 3: Use the Plugin Launcher

```bash
java -cp ... org.pentaho.di.ui.spoon.webui.SpoonWebUI --webui-port 8080
```

## Configuration

The following system properties can be used to configure the web UI:

- `spoon.webui.enabled`: Enable/disable the web UI (default: false)
- `spoon.webui.port`: Port for the web server (default: 8080)
- `spoon.webui.autoopen`: Automatically open web UI in browser (default: false)

## Building

The plugin uses Maven for building and includes a frontend build process:

```bash
# Build the entire plugin including React frontend
mvn clean package

# Development mode - build React app in watch mode
cd src/main/webapp
npm start
```

## Development

### Frontend Development

The React application is located in `src/main/webapp/` and can be developed independently:

```bash
cd src/main/webapp
npm install
npm start  # Starts development server on port 3000
```

### Backend Development

The Java backend can be developed and tested independently. The main classes are:

- `WebUIServer`: Handles the embedded web server
- `APIServlet`: Provides REST API endpoints
- `SpoonWebUIPlugin`: Integrates with Spoon's plugin system

## REST API Endpoints

The plugin provides several REST API endpoints:

- `GET /api/` - API information
- `GET /api/transformations` - List transformations
- `GET /api/jobs` - List jobs
- `GET /api/connections` - List database connections
- `GET /api/steps` - List available step types
- `GET /api/entries` - List available job entry types

## Browser Support

The web interface supports modern browsers including:

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Known Limitations

This is an initial implementation with the following limitations:

1. **Read-only interface**: Currently provides visualization and basic interaction
2. **Mock data**: Uses mock data instead of real Kettle objects
3. **Limited functionality**: Subset of full Spoon functionality
4. **No execution**: Cannot execute transformations/jobs yet
5. **No file operations**: Cannot open/save .ktr/.kjb files yet

## Future Enhancements

Planned improvements include:

1. Full integration with Kettle engine for real data operations
2. Transformation and job execution capabilities
3. File system integration for opening/saving files
4. Advanced step and job entry configuration dialogs
5. Real-time execution monitoring and logging
6. Multi-user support and authentication
7. Mobile-optimized interface
8. Integration with Pentaho repository

## Contributing

To contribute to this plugin:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This plugin is licensed under the same terms as Pentaho Data Integration.