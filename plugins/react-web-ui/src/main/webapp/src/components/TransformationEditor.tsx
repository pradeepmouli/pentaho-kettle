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
  Input as InputIcon,
  Output as OutputIcon,
  Transform,
} from '@mui/icons-material';

const TransformationEditor: React.FC = () => {
  const [selectedStep, setSelectedStep] = useState<string | null>(null);

  // Mock step types data
  const stepCategories = [
    {
      name: 'Input',
      icon: <InputIcon />,
      steps: ['Text file input', 'CSV file input', 'Excel input', 'Database input']
    },
    {
      name: 'Output',
      icon: <OutputIcon />,
      steps: ['Text file output', 'Database output', 'Excel output']
    },
    {
      name: 'Transform',
      icon: <Transform />,
      steps: ['Select values', 'Filter rows', 'Sort rows', 'Group by']
    }
  ];

  return (
    <Box sx={{ display: 'flex', height: '100%' }}>
      {/* Left panel - Step palette */}
      <Paper sx={{ width: 250, mr: 2, overflow: 'auto' }}>
        <Toolbar variant="dense">
          <Typography variant="h6">Steps</Typography>
        </Toolbar>
        <Divider />
        {stepCategories.map((category) => (
          <Box key={category.name}>
            <ListItem>
              <ListItemIcon>{category.icon}</ListItemIcon>
              <ListItemText primary={category.name} />
            </ListItem>
            <List sx={{ pl: 4 }}>
              {category.steps.map((step) => (
                <ListItem 
                  key={step}
                  sx={{ py: 0.5, cursor: 'pointer', backgroundColor: selectedStep === step ? 'action.selected' : 'transparent' }}
                  onClick={() => setSelectedStep(step)}
                >
                  <ListItemText 
                    primary={step} 
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
            <IconButton title="Add Step">
              <Add />
            </IconButton>
            <IconButton title="Delete Step">
              <Delete />
            </IconButton>
            <IconButton title="Edit Step">
              <Edit />
            </IconButton>
            <Divider orientation="vertical" flexItem sx={{ mx: 1 }} />
            <IconButton title="Run Transformation" color="primary">
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
              Transformation Canvas
            </Typography>
            <Typography variant="body2">
              Drag steps from the palette to create your transformation
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
          {selectedStep ? (
            <Box>
              <Typography variant="subtitle1" gutterBottom>
                {selectedStep}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Configure the properties for this step here.
              </Typography>
            </Box>
          ) : (
            <Typography variant="body2" color="text.secondary">
              Select a step to view its properties
            </Typography>
          )}
        </Box>
      </Paper>
    </Box>
  );
};

export default TransformationEditor;