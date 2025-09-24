# Spoon Web UI - React-based Interface

This document describes the new React-based web interface for Pentaho Data Integration (Spoon), which provides a modern alternative to the traditional desktop SWT application.

## Overview

The Spoon Web UI is a complete reimplementation of the Spoon interface using modern web technologies:

- **React 18** with TypeScript for the frontend
- **Material-UI** for consistent, modern UI components
- **Embedded Jetty Server** for serving the web application
- **REST API** for backend communication
- **Responsive Design** that works on desktop and mobile devices

## Features

### ✅ Implemented Features

- **Modern Interface**: Clean, intuitive design following Material Design principles
- **Transformation Editor**: Visual canvas with step palette for creating transformations
- **Job Editor**: Visual canvas with job entry palette for creating jobs
- **Navigation Tree**: Left panel showing transformations, jobs, connections, etc.
- **Tabbed Interface**: Multiple tabs for different transformations and jobs
- **Toolbar Actions**: Standard actions like Open, Save, Run, Stop
- **Properties Panel**: Right panel for step/entry configuration
- **Responsive Layout**: Collapsible sidebars and mobile-friendly design

### 🚧 Planned Features

- **Full Kettle Integration**: Real data operations instead of mock data
- **Execution Engine**: Ability to actually run transformations and jobs
- **File Operations**: Open/save .ktr and .kjb files
- **Database Connections**: Manage real database connections
- **Step Configuration**: Full property dialogs for steps and job entries
- **Repository Integration**: Connect to Pentaho repositories
- **Real-time Monitoring**: Live execution monitoring and logging
- **Authentication**: User management and security

## Architecture

```
┌─────────────────────────────────────────────┐
│                 Browser                      │
│  ┌─────────────────────────────────────┐    │
│  │          React Frontend             │    │
│  │  - TypeScript + Material-UI         │    │
│  │  - Transformation/Job Editors       │    │
│  │  - Navigation + Properties          │    │
│  └─────────────────────────────────────┘    │
└─────────────────────────────────────────────┘
                     │ HTTP/REST
┌─────────────────────────────────────────────┐
│              Spoon Web UI Plugin            │
│  ┌─────────────────────────────────────┐    │
│  │         Jetty Web Server            │    │
│  │  - Static file serving              │    │
│  │  - REST API endpoints               │    │
│  │  - SPA routing support              │    │
│  └─────────────────────────────────────┘    │
│  ┌─────────────────────────────────────┐    │
│  │       Spoon Plugin Bridge           │    │
│  │  - Integrates with existing Spoon   │    │
│  │  - Lifecycle management             │    │
│  │  - System property configuration    │    │
│  └─────────────────────────────────────┘    │
└─────────────────────────────────────────────┘
                     │
┌─────────────────────────────────────────────┐
│            Kettle Engine                    │
│  - Transformations, Jobs, Steps             │
│  - Database connections                     │
│  - Execution engine                         │
└─────────────────────────────────────────────┘
```

## Usage Options

### Option 1: Enable in Desktop Spoon

Enable the web UI alongside the traditional desktop interface:

```bash
# Enable web UI with auto-open
spoon.sh -Dspoon.webui.enabled=true -Dspoon.webui.autoopen=true

# Custom port
spoon.sh -Dspoon.webui.enabled=true -Dspoon.webui.port=8090
```

### Option 2: Web-Only Mode

Run without the desktop interface (headless):

```bash
# Web-only mode on default port 8080
java -cp "lib/*:plugins/*" org.pentaho.di.ui.spoon.webui.SpoonWebUI --no-desktop

# Custom port
java -cp "lib/*:plugins/*" org.pentaho.di.ui.spoon.webui.SpoonWebUI --no-desktop --webui-port 9000
```

### Option 3: Hybrid Mode

Start with both desktop and web interfaces:

```bash
# Desktop Spoon with web UI enabled
java -cp "lib/*:plugins/*" org.pentaho.di.ui.spoon.webui.SpoonWebUI --webui-port 8080
```

## Development

### Building the Plugin

```bash
# Build everything including React frontend
cd plugins/react-web-ui
mvn clean package

# The built plugin will be in target/react-web-ui-*.jar
```

### Frontend Development

```bash
# Navigate to React app directory
cd plugins/react-web-ui/src/main/webapp

# Install dependencies
npm install

# Start development server (with hot reload)
npm start

# Build for production
npm run build
```

### Backend Development

The Java backend consists of:

- `SpoonWebUIPlugin.java` - Main plugin class
- `WebUIServer.java` - Embedded Jetty server
- `APIServlet.java` - REST API endpoints
- `SpoonWebUI.java` - Alternative launcher

## Configuration

### System Properties

| Property | Default | Description |
|----------|---------|-------------|
| `spoon.webui.enabled` | `false` | Enable the web UI server |
| `spoon.webui.port` | `8080` | Port for the web server |
| `spoon.webui.autoopen` | `false` | Auto-open browser on startup |

### Environment Variables

```bash
export SPOON_WEBUI_ENABLED=true
export SPOON_WEBUI_PORT=8080
export SPOON_WEBUI_AUTOOPEN=true
```

## REST API

The web UI communicates with the backend through REST endpoints:

### Available Endpoints

- `GET /api/` - API status and information
- `GET /api/transformations` - List all transformations
- `GET /api/jobs` - List all jobs
- `GET /api/connections` - List database connections
- `GET /api/steps` - Get available step types
- `GET /api/entries` - Get available job entry types

### Example Response

```json
{
  "transformations": [
    {
      "id": "trans1",
      "name": "Sample Transformation",
      "description": "A sample transformation for demo",
      "created": "2024-01-01"
    }
  ]
}
```

## Browser Compatibility

The web UI supports modern browsers:

- **Chrome** 90+
- **Firefox** 88+
- **Safari** 14+
- **Edge** 90+

## Performance Considerations

- **Lazy Loading**: Components load on demand
- **Virtual Scrolling**: For large lists of steps/entries
- **Optimized Builds**: Production builds are minified and optimized
- **Caching**: Static assets are cached for better performance

## Security

Current security considerations:

- **Local Access**: Default configuration only allows localhost access
- **No Authentication**: Currently no user authentication (planned)
- **CORS**: Configured for local development

For production deployment, consider:

- Reverse proxy with SSL termination
- Authentication and authorization
- Network security and firewalls
- Input validation and sanitization

## Troubleshooting

### Common Issues

**Port Already in Use**
```bash
# Check what's using the port
netstat -tulpn | grep 8080

# Use a different port
-Dspoon.webui.port=8081
```

**React App Won't Load**
```bash
# Check if webapp resources exist
ls -la plugins/react-web-ui/target/classes/webapp/

# Rebuild if missing
cd plugins/react-web-ui && mvn clean package
```

**API Endpoints Return 404**
```bash
# Check server logs for errors
# Verify API servlet is properly registered
```

### Debugging

Enable debug logging:
```bash
-Dlog4j.logger.org.pentaho.di.ui.spoon.webui=DEBUG
```

## Contributing

To contribute to the Spoon Web UI:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Make changes to React frontend (`src/main/webapp/src/`)
4. Make changes to Java backend (`src/main/java/`)
5. Test your changes: `npm test` and `mvn test`
6. Submit a pull request

### Development Workflow

1. **Frontend Changes**: Use `npm start` for hot reload during development
2. **Backend Changes**: Rebuild plugin with `mvn compile`
3. **Full Build**: Use `mvn package` to build complete plugin
4. **Testing**: Test both web-only and hybrid modes

## Roadmap

### Phase 1: Foundation ✅
- Basic React UI structure
- Embedded web server
- Plugin integration
- Mock API endpoints

### Phase 2: Core Features 🚧
- Real Kettle integration
- File operations (open/save)
- Database connections
- Step configuration dialogs

### Phase 3: Advanced Features 📋
- Transformation/job execution
- Real-time monitoring
- Repository integration
- Multi-user support

### Phase 4: Enterprise Features 📋
- Authentication/authorization
- Advanced security
- Performance optimization
- Mobile enhancements

## License

This plugin is licensed under the same terms as Pentaho Data Integration.