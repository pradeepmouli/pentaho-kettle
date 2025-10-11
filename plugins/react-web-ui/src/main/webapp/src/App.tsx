import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import SpoonLayout from './components/SpoonLayout';
import TransformationEditor from './components/TransformationEditor';
import JobEditor from './components/JobEditor';
import './App.css';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <SpoonLayout>
          <Routes>
            <Route path="/" element={<div>Welcome to Spoon Web UI</div>} />
            <Route path="/transformation/:id?" element={<TransformationEditor />} />
            <Route path="/job/:id?" element={<JobEditor />} />
          </Routes>
        </SpoonLayout>
      </Router>
    </ThemeProvider>
  );
}

export default App;
