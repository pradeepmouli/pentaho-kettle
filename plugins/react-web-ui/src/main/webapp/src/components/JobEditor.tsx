import React, { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Toolbar,
  IconButton,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Divider,
} from '@mui/material';
import {
  Add,
  Delete,
  Edit,
  PlayArrow,
  Pause,
  Stop,
  Start,
  CheckCircle,
  Error,
  Email,
} from '@mui/icons-material';

const JobEditor: React.FC = () => {
  const [selectedEntry, setSelectedEntry] = useState<string | null>(null);

  // Mock job entry types data
  const entryCategories = [
    {
      name: 'General',
      icon: <Start />,
      entries: ['START', 'Dummy', 'Success', 'Abort job']
    },
    {
      name: 'File Management',
      icon: <CheckCircle />,
      entries: ['Copy files', 'Move files', 'Delete files', 'Check files exist']
    },
    {
      name: 'Conditions',
      icon: <Error />,
      entries: ['Simple evaluation', 'Check DB connections', 'Evaluate rows number']
    },
    {
      name: 'Mail',
      icon: <Email />,
      entries: ['Mail', 'Get mails (POP3/IMAP)', 'Delete mails (POP3)']
    }
  ];

  return (
    <Box sx={{ display: 'flex', height: '100%' }}>
      {/* Left panel - Job entry palette */}
      <Paper sx={{ width: 250, mr: 2, overflow: 'auto' }}>
        <Toolbar variant="dense">
          <Typography variant="h6">Job Entries</Typography>
        </Toolbar>
        <Divider />
        {entryCategories.map((category) => (
          <Box key={category.name}>
            <ListItem>
              <ListItemIcon>{category.icon}</ListItemIcon>
              <ListItemText primary={category.name} />
            </ListItem>
            <List sx={{ pl: 4 }}>
              {category.entries.map((entry) => (
                <ListItem 
                  key={entry}
                  sx={{ py: 0.5, cursor: 'pointer', backgroundColor: selectedEntry === entry ? 'action.selected' : 'transparent' }}
                  onClick={() => setSelectedEntry(entry)}
                >
                  <ListItemText 
                    primary={entry} 
                    primaryTypographyProps={{ fontSize: '0.875rem' }}
                  />
                </ListItem>
              ))}
            </List>
          </Box>
        ))}
      </Paper>

      {/* Main canvas area */}
      <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
        {/* Canvas toolbar */}
        <Paper sx={{ mb: 2 }}>
          <Toolbar variant="dense">
            <IconButton title="Add Job Entry">
              <Add />
            </IconButton>
            <IconButton title="Delete Job Entry">
              <Delete />
            </IconButton>
            <IconButton title="Edit Job Entry">
              <Edit />
            </IconButton>
            <Divider orientation="vertical" flexItem sx={{ mx: 1 }} />
            <IconButton title="Run Job" color="primary">
              <PlayArrow />
            </IconButton>
            <IconButton title="Pause">
              <Pause />
            </IconButton>
            <IconButton title="Stop" color="error">
              <Stop />
            </IconButton>
          </Toolbar>
        </Paper>

        {/* Canvas */}
        <Paper 
          sx={{ 
            flexGrow: 1, 
            position: 'relative',
            background: 'linear-gradient(0deg, transparent 24%, rgba(255,255,255,.05) 25%, rgba(255,255,255,.05) 26%, transparent 27%, transparent 74%, rgba(255,255,255,.05) 75%, rgba(255,255,255,.05) 76%, transparent 77%, transparent), linear-gradient(90deg, transparent 24%, rgba(255,255,255,.05) 25%, rgba(255,255,255,.05) 26%, transparent 27%, transparent 74%, rgba(255,255,255,.05) 75%, rgba(255,255,255,.05) 76%, transparent 77%, transparent)',
            backgroundSize: '20px 20px'
          }}
        >
          <Box
            sx={{
              position: 'absolute',
              top: '50%',
              left: '50%',
              transform: 'translate(-50%, -50%)',
              textAlign: 'center',
              color: 'text.secondary'
            }}
          >
            <Typography variant="h6" gutterBottom>
              Job Canvas
            </Typography>
            <Typography variant="body2">
              Drag job entries from the palette to create your job
            </Typography>
          </Box>
        </Paper>
      </Box>

      {/* Right panel - Properties */}
      <Paper sx={{ width: 300, ml: 2, overflow: 'auto' }}>
        <Toolbar variant="dense">
          <Typography variant="h6">Properties</Typography>
        </Toolbar>
        <Divider />
        <Box sx={{ p: 2 }}>
          {selectedEntry ? (
            <Box>
              <Typography variant="subtitle1" gutterBottom>
                {selectedEntry}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Configure the properties for this job entry here.
              </Typography>
            </Box>
          ) : (
            <Typography variant="body2" color="text.secondary">
              Select a job entry to view its properties
            </Typography>
          )}
        </Box>
      </Paper>
    </Box>
  );
};

export default JobEditor;