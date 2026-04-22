import { useState, useEffect, useCallback } from 'react';
import Editor from '@monaco-editor/react';
import axios from 'axios';

const API_BASE = 'http://localhost:8080/api';

// Default code templates
const DEFAULT_CODE = {
  java: `public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, CodeArena!");
    }
}`,
  python: `print("Hello, CodeArena!")`,
  cpp: `#include <iostream>
using namespace std;

int main() {
    cout << "Hello, CodeArena!" << endl;
    return 0;
}`
};

const LANGUAGE_MAP = {
  java: 'java',
  python: 'python',
  cpp: 'cpp'
};

function App() {
  // Auth state
  const [user, setUser] = useState(null);
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [authMode, setAuthMode] = useState('login');
  const [authError, setAuthError] = useState('');

  // Editor state
  const [language, setLanguage] = useState('java');
  const [code, setCode] = useState(DEFAULT_CODE.java);
  const [programName, setProgramName] = useState('Untitled');
  const [currentProgramId, setCurrentProgramId] = useState(null);

  // Programs state
  const [programs, setPrograms] = useState([]);
  const [loading, setLoading] = useState(false);

  // Output state
  const [output, setOutput] = useState('');
  const [executionTime, setExecutionTime] = useState(0);
  const [status, setStatus] = useState('');
  const [isRunning, setIsRunning] = useState(false);

  // Load user from localStorage on mount
  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      setUser(JSON.parse(savedUser));
    }
  }, []);

  // Load programs when user logs in
  useEffect(() => {
    if (user) {
      fetchPrograms();
    }
  }, [user]);

  // Update code when language changes
  useEffect(() => {
    if (!currentProgramId) {
      setCode(DEFAULT_CODE[language]);
    }
  }, [language, currentProgramId]);

  const fetchPrograms = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`${API_BASE}/programs`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setPrograms(response.data);
    } catch (error) {
      console.error('Error fetching programs:', error);
    }
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setAuthError('');
    const username = e.target.username.value;
    const password = e.target.password.value;

    try {
      const response = await axios.post(`${API_BASE}/auth/login`, { username, password });
      const { token, username: name, email } = response.data;
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify({ username: name, email }));
      setUser({ username: name, email });
      setShowAuthModal(false);
    } catch (error) {
      setAuthError(error.response?.data?.message || 'Login failed');
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setAuthError('');
    const username = e.target.username.value;
    const email = e.target.email.value;
    const password = e.target.password.value;

    try {
      const response = await axios.post(`${API_BASE}/auth/register`, { username, email, password });
      const { token, username: name, email: userEmail } = response.data;
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify({ username: name, email: userEmail }));
      setUser({ username: name, email: userEmail });
      setShowAuthModal(false);
    } catch (error) {
      setAuthError(error.response?.data?.message || 'Registration failed');
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
    setPrograms([]);
    setCurrentProgramId(null);
    setProgramName('Untitled');
    setCode(DEFAULT_CODE.java);
    setLanguage('java');
  };

  const handleRunCode = async () => {
    setIsRunning(true);
    setOutput('Running...');
    setStatus('');
    setExecutionTime(0);

    try {
      const response = await axios.post(`${API_BASE}/execute`, {
        language,
        code
      });
      
      const { output: result, executionTime: time, status: execStatus, error } = response.data;
      setOutput(error || result);
      setExecutionTime(time);
      setStatus(execStatus);
    } catch (error) {
      setOutput('Error: ' + (error.response?.data?.message || error.message));
      setStatus('ERROR');
    } finally {
      setIsRunning(false);
    }
  };

  const handleSave = async () => {
    if (!user) {
      setShowAuthModal(true);
      return;
    }

    try {
      const token = localStorage.getItem('token');
      if (currentProgramId) {
        await axios.put(`${API_BASE}/programs/${currentProgramId}`, {
          name: programName,
          language,
          code
        }, {
          headers: { Authorization: `Bearer ${token}` }
        });
      } else {
        const response = await axios.post(`${API_BASE}/programs`, {
          name: programName,
          language,
          code
        }, {
          headers: { Authorization: `Bearer ${token}` }
        });
        setCurrentProgramId(response.data.id);
      }
      fetchPrograms();
    } catch (error) {
      console.error('Error saving program:', error);
    }
  };

  const handleLoadProgram = async (program) => {
    setCurrentProgramId(program.id);
    setProgramName(program.name);
    setLanguage(program.language);
    setCode(program.code);
    setOutput('');
    setStatus('');
  };

  const handleDeleteProgram = async (e, id) => {
    e.stopPropagation();
    try {
      const token = localStorage.getItem('token');
      await axios.delete(`${API_BASE}/programs/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (currentProgramId === id) {
        setCurrentProgramId(null);
        setProgramName('Untitled');
        setCode(DEFAULT_CODE[language]);
      }
      fetchPrograms();
    } catch (error) {
      console.error('Error deleting program:', error);
    }
  };

  const handleNewProgram = () => {
    setCurrentProgramId(null);
    setProgramName('Untitled');
    setCode(DEFAULT_CODE[language]);
    setOutput('');
    setStatus('');
  };

  const clearOutput = () => {
    setOutput('');
    setStatus('');
    setExecutionTime(0);
  };

  const getStatusClass = () => {
    if (status === 'SUCCESS') return 'status-success';
    if (status === 'ERROR') return 'status-error';
    if (status === 'TIMEOUT') return 'status-timeout';
    return '';
  };

  return (
    <div className="app">
      {/* Header */}
      <header className="header">
        <div className="logo">
          <span className="logo-icon">⚡</span>
          <span>CodeArena</span>
        </div>
        <div className="header-actions">
          {user ? (
            <div className="user-info">
              <span className="user-name">@{user.username}</span>
              <button className="btn btn-secondary" onClick={handleLogout}>
                Logout
              </button>
            </div>
          ) : (
            <button className="btn btn-primary" onClick={() => setShowAuthModal(true)}>
              Sign In
            </button>
          )}
        </div>
      </header>

      {/* Main Container */}
      <div className="main-container">
        {/* Sidebar */}
        <aside className="sidebar">
          <div className="sidebar-header">
            <button className="btn btn-success" onClick={handleNewProgram} style={{ width: '100%' }}>
              + New Program
            </button>
          </div>
          <div className="program-list">
            {programs.length > 0 ? (
              programs.map((program) => (
                <div
                  key={program.id}
                  className={`program-item ${currentProgramId === program.id ? 'active' : ''}`}
                  onClick={() => handleLoadProgram(program)}
                >
                  <div>
                    <span className="program-name">{program.name}</span>
                    <span className="program-lang">{program.language}</span>
                  </div>
                  <button
                    className="btn btn-danger btn-small program-delete"
                    onClick={(e) => handleDeleteProgram(e, program.id)}
                  >
                    ✕
                  </button>
                </div>
              ))
            ) : (
              <div className="empty-state">
                <div className="empty-icon">📁</div>
                <p>No saved programs</p>
                <p style={{ fontSize: '12px', marginTop: '8px' }}>Save your code to see it here</p>
              </div>
            )}
          </div>
        </aside>

        {/* Editor Panel */}
        <main className="editor-panel">
          <div className="editor-toolbar">
            <div className="language-selector">
              <label>Language:</label>
              <select
                className="language-select"
                value={language}
                onChange={(e) => setLanguage(e.target.value)}
              >
                <option value="java">Java</option>
                <option value="python">Python</option>
                <option value="cpp">C++</option>
              </select>
            </div>
            <input
              type="text"
              className="program-name-input"
              value={programName}
              onChange={(e) => setProgramName(e.target.value)}
              placeholder="Program name"
            />
            <div style={{ display: 'flex', gap: '12px' }}>
              <button className="btn btn-secondary" onClick={handleSave}>
                Save
              </button>
              <button
                className="btn btn-success"
                onClick={handleRunCode}
                disabled={isRunning}
              >
                {isRunning ? 'Running...' : '▶ Run'}
              </button>
            </div>
          </div>

          <div className="editor-container">
            <Editor
              height="100%"
              language={LANGUAGE_MAP[language]}
              value={code}
              onChange={(value) => setCode(value || '')}
              theme="vs-dark"
              options={{
                fontSize: 14,
                fontFamily: "'JetBrains Mono', monospace",
                minimap: { enabled: false },
                scrollBeyondLastLine: false,
                automaticLayout: true,
                padding: { top: 16 }
              }}
            />
          </div>

          {/* Output Console */}
          <div className="output-console">
            <div className="console-header">
              <span className="console-title">Output</span>
              <div className="console-stats">
                <span className="stat-item">
                  Time: <span className="stat-value">{executionTime}ms</span>
                </span>
                <span className="stat-item">
                  Status: <span className={`stat-value ${getStatusClass()}`}>{status || '—'}</span>
                </span>
                <button className="btn btn-secondary btn-small" onClick={clearOutput}>
                  Clear
                </button>
              </div>
            </div>
            <div className={`console-output ${status === 'SUCCESS' ? 'success' : status === 'ERROR' ? 'error' : ''}`}>
              {output || 'Run your code to see output here...'}
            </div>
          </div>
        </main>
      </div>

      {/* Auth Modal */}
      {showAuthModal && (
        <div className="modal-overlay" onClick={() => setShowAuthModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2 className="modal-title">
                {authMode === 'login' ? 'Welcome Back' : 'Join CodeArena'}
              </h2>
              <button className="modal-close" onClick={() => setShowAuthModal(false)}>
                ×
              </button>
            </div>
            <div className="modal-tabs">
              <button
                className={`modal-tab ${authMode === 'login' ? 'active' : ''}`}
                onClick={() => setAuthMode('login')}
              >
                Login
              </button>
              <button
                className={`modal-tab ${authMode === 'register' ? 'active' : ''}`}
                onClick={() => setAuthMode('register')}
              >
                Register
              </button>
            </div>
            {authMode === 'login' ? (
              <form className="modal-form" onSubmit={handleLogin}>
                <div className="form-group">
                  <label className="form-label">Username</label>
                  <input
                    type="text"
                    name="username"
                    className="form-input"
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Password</label>
                  <input
                    type="password"
                    name="password"
                    className="form-input"
                    required
                  />
                </div>
                {authError && <p className="form-error">{authError}</p>}
                <div className="modal-submit">
                  <button type="submit" className="btn btn-primary">
                    Login
                  </button>
                </div>
              </form>
            ) : (
              <form className="modal-form" onSubmit={handleRegister}>
                <div className="form-group">
                  <label className="form-label">Username</label>
                  <input
                    type="text"
                    name="username"
                    className="form-input"
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Email</label>
                  <input
                    type="email"
                    name="email"
                    className="form-input"
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Password</label>
                  <input
                    type="password"
                    name="password"
                    className="form-input"
                    required
                  />
                </div>
                {authError && <p className="form-error">{authError}</p>}
                <div className="modal-submit">
                  <button type="submit" className="btn btn-primary">
                    Register
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default App;

