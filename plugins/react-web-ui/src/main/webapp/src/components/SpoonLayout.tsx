import React, { useState } from 'react';
import {
  Box,
  AppBar,
  Toolbar,
  Typography,
  Drawer,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  IconButton,
  Divider,
  Tabs,
  Tab,
} from '@mui/material';
import {
  Menu as MenuIcon,
  AccountTree,
  Work,
  Storage,
  Settings,
  Dashboard,
  PlayArrow,
  Stop,
  Save,
  FolderOpen,
} from '@mui/icons-material';

const drawerWidth = 300;

interface SpoonLayoutProps {
  children: React.ReactNode;
}

const SpoonLayout: React.FC<SpoonLayoutProps> = ({ children }) => {
  const [leftDrawerOpen, setLeftDrawerOpen] = useState(true);
  const [selectedTab, setSelectedTab] = useState(0);

  const handleDrawerToggle = () => {
    setLeftDrawerOpen(!leftDrawerOpen);
  };

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setSelectedTab(newValue);
  };

  const treeItems = [
    { text: 'Transformations', icon: <AccountTree />, path: '/transformations' },
    { text: 'Jobs', icon: <Work />, path: '/jobs' },
    { text: 'Database Connections', icon: <Storage />, path: '/connections' },
    { text: 'Slave Servers', icon: <Settings />, path: '/slaves' },
    { text: 'Clusters', icon: <Dashboard />, path: '/clusters' },
  ];

  return (
    <Box sx={{ display: 'flex', height: '100vh' }}>
      {/* Top AppBar */}
      <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            onClick={handleDrawerToggle}
            edge="start"
            sx={{ mr: 2 }}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
            Pentaho Data Integration - Spoon Web UI
          </Typography>
          
          {/* Toolbar buttons */}
          <IconButton color="inherit" title="Open">
            <FolderOpen />
          </IconButton>
          <IconButton color="inherit" title="Save">
            <Save />
          </IconButton>
          <Divider orientation="vertical" flexItem sx={{ mx: 1, backgroundColor: 'white' }} />
          <IconButton color="inherit" title="Run">
            <PlayArrow />
          </IconButton>
          <IconButton color="inherit" title="Stop">
            <Stop />
          </IconButton>
        </Toolbar>
      </AppBar>

      {/* Left Drawer */}
      <Drawer
        variant="persistent"
        open={leftDrawerOpen}
        sx={{
          width: drawerWidth,
          flexShrink: 0,
          '& .MuiDrawer-paper': {
            width: drawerWidth,
            boxSizing: 'border-box',
          },
        }}
      >
        <Toolbar />
        <Box sx={{ overflow: 'auto' }}>
          <Typography variant="h6" sx={{ p: 2 }}>
            View
          </Typography>
          <Divider />
          <List>
            {treeItems.map((item, index) => (
              <ListItem key={item.text} sx={{ cursor: 'pointer' }}>
                <ListItemIcon>
                  {item.icon}
                </ListItemIcon>
                <ListItemText primary={item.text} />
              </ListItem>
            ))}
          </List>
        </Box>
      </Drawer>

      {/* Main content area */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          display: 'flex',
          flexDirection: 'column',
          marginLeft: leftDrawerOpen ? 0 : `-${drawerWidth}px`,
          transition: 'margin-left 0.3s ease',
        }}
      >
        <Toolbar />
        
        {/* Tab area */}
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={selectedTab} onChange={handleTabChange}>
            <Tab label="Welcome" />
            <Tab label="New Transformation" />
            <Tab label="New Job" />
          </Tabs>
        </Box>

        {/* Content area */}
        <Box sx={{ flexGrow: 1, p: 2, overflow: 'auto' }}>
          {children}
        </Box>
      </Box>
    </Box>
  );
};

export default SpoonLayout;