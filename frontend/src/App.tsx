import { useState, useEffect } from 'react'
import ReactMarkdown from 'react-markdown'
import { Brain, BarChart3, Search, Loader2, Sparkles, X } from 'lucide-react'
import './App.css'

interface AnalysisData {
  handle: string;
  status: string;
  aiRecommendation?: string;
  topicMetrics?: {
    overallAverageRating: number;
    totalSubmissions: number;
    topics: Array<{
      topic: string;
      solvedCount: number;
      averageDifficulty: number;
      successRate: number;
    }>;
  };
}

function App() {
  const [handle, setHandle] = useState('')
  const [activeTab, setActiveTab] = useState<'roadmap' | 'stats'>('roadmap')
  const [data, setData] = useState<AnalysisData | null>(null)
  const [loading, setLoading] = useState(false)

  // 1. Initial Load: Only run once on mount
  useEffect(() => {
    const savedHandle = localStorage.getItem('last_handle')
    if (savedHandle) {
      setHandle(savedHandle)
      chrome.storage.local.get([`analysis_${savedHandle}`], (result) => {
        if (result[`analysis_${savedHandle}`]) {
          setData(result[`analysis_${savedHandle}`])
        }
      });
    }
  }, [])

  // 2. Continuous Listener: Listen for any storage changes
  useEffect(() => {
    const listener = (changes: { [key: string]: chrome.storage.StorageChange }) => {
      // We only care about the storage key for the CURRENTLY LOADED data
      if (data && changes[`analysis_${data.handle}`]) {
        setData(changes[`analysis_${data.handle}`].newValue)
        if (changes[`analysis_${data.handle}`].newValue.status === 'COMPLETED' || 
            changes[`analysis_${data.handle}`].newValue.status === 'ERROR') {
          setLoading(false)
        }
      }
    };

    chrome.storage.onChanged.addListener(listener)
    return () => chrome.storage.onChanged.removeListener(listener)
  }, [data?.handle])

  const handleAnalyze = () => {
    if (!handle) return
    setLoading(true)
    localStorage.setItem('last_handle', handle)
    // Manually set a placeholder handle in data so the listener knows what to watch
    setData({ handle, status: 'PENDING' })
    chrome.runtime.sendMessage({ type: 'START_ANALYSIS', handle })
  }

  const handleReset = () => {
    setHandle('')
    setData(null)
    setLoading(false)
    localStorage.removeItem('last_handle')
  }

  return (
    <div className="app-container">
      <header>
        <div className="logo" onClick={handleReset} style={{ cursor: 'pointer' }}>
          <Sparkles className="icon-primary" />
          <h1>CF Analyzer Pro</h1>
        </div>
        <div className="search-box">
          <div className="input-wrapper">
            <input 
              type="text" 
              placeholder="Enter CF Handle..." 
              value={handle}
              onChange={(e) => setHandle(e.target.value)}
              autoFocus
            />
            {handle && (
              <X 
                size={16} 
                className="clear-icon" 
                onClick={handleReset}
              />
            )}
          </div>
          <button onClick={handleAnalyze} disabled={loading}>
            {loading ? <Loader2 className="animate-spin" /> : <Search />}
          </button>
        </div>
      </header>

      {data ? (
        <main>
          <div className="tabs">
            <button 
              className={activeTab === 'roadmap' ? 'active' : ''} 
              onClick={() => setActiveTab('roadmap')}
            >
              <Brain size={18} /> Roadmap
            </button>
            <button 
              className={activeTab === 'stats' ? 'active' : ''} 
              onClick={() => setActiveTab('stats')}
            >
              <BarChart3 size={18} /> Stats
            </button>
          </div>

          <div className="content-area">
            {activeTab === 'roadmap' ? (
              <div className="roadmap-view">
                {data.status === 'PROCESSING' || data.status === 'PENDING' ? (
                  <div className="loading-state">
                    <Loader2 className="animate-spin large" />
                    <p>AI is analyzing your submissions...</p>
                    <p className="subtext">This usually takes 10-20 seconds.</p>
                  </div>
                ) : (
                  <div className="markdown-content">
                    <ReactMarkdown>{data.aiRecommendation || ''}</ReactMarkdown>
                  </div>
                )}
              </div>
            ) : (
              <div className="stats-view">
                <div className="stats-summary">
                  <div className="stat-card">
                    <label>Overall Rating</label>
                    <span>{data.topicMetrics?.overallAverageRating.toFixed(0)}</span>
                  </div>
                  <div className="stat-card">
                    <label>Total Subs</label>
                    <span>{data.topicMetrics?.totalSubmissions}</span>
                  </div>
                </div>
                <div className="topic-list">
                  <h3>Topic Breakdown</h3>
                  {data.topicMetrics?.topics.map(topic => (
                    <div key={topic.topic} className="topic-row">
                      <div className="topic-info">
                        <span className="topic-name">{topic.topic}</span>
                        <span className="topic-meta">{topic.solvedCount} solved @ {topic.averageDifficulty.toFixed(0)} avg</span>
                      </div>
                      <div className="topic-bar-bg">
                        <div 
                          className="topic-bar-fill" 
                          style={{ width: `${topic.successRate * 100}%` }}
                        ></div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </main>
      ) : (
        <div className="hero">
          <img src="favicon.svg" alt="Logo" width={64} />
          <h2>Enter your handle to get started</h2>
          <p>We'll analyze your past performance and generate an AI-powered study roadmap.</p>
        </div>
      )}
    </div>
  )
}

export default App
