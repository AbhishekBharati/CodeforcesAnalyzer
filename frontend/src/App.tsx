import { useState } from 'react'
import './App.css'

function App() {
  const [handle, setHandle] = useState('')

  const handleAnalyze = () => {
    console.log('Analyzing handle:', handle)
    // This will be implemented later
  }

  return (
    <div className="container">
      <h1>Codeforces Analyzer</h1>
      <div className="input-group">
        <input 
          type="text" 
          placeholder="Enter CF Handle" 
          value={handle}
          onChange={(e) => setHandle(e.target.value)}
        />
        <button onClick={handleAnalyze}>Analyze</button>
      </div>
      <div className="status">
        <p>Status: Idle</p>
      </div>
    </div>
  )
}

export default App
