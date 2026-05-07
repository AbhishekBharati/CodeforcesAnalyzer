const BACKEND_URL = 'http://localhost:8080/api/cf';

interface AnalysisResponse {
  handle: string;
  status: string;
  aiRecommendation?: string;
  topicMetrics?: any;
}

chrome.runtime.onMessage.addListener((request, _sender, sendResponse) => {
  if (request.type === 'START_ANALYSIS') {
    const { handle } = request;
    startAnalysisFlow(handle);
    sendResponse({ status: 'started' });
  }
  return true;
});

async function startAnalysisFlow(handle: string) {
  try {
    console.log(`SW: Starting analysis for ${handle}`);
    
    // 1. Trigger the analysis
    const response = await fetch(`${BACKEND_URL}/analyze?handle=${handle}`, {
      method: 'POST'
    });
    
    if (!response.ok && response.status !== 202) {
      throw new Error('Failed to start analysis');
    }

    // 2. Start polling
    pollStatus(handle);

  } catch (error) {
    console.error('SW Error:', error);
    chrome.storage.local.set({ 
      [`analysis_${handle}`]: { status: 'ERROR', error: (error as Error).message } 
    });
  }
}

async function pollStatus(handle: string, delay = 2000) {
  try {
    console.log(`SW: Polling status for ${handle} (next check in ${delay}ms)`);
    
    const response = await fetch(`${BACKEND_URL}/status/${handle}`);
    const data: AnalysisResponse = await response.json();

    // Update storage so the UI can react
    await chrome.storage.local.set({ [`analysis_${handle}`]: data });

    if (data.status === 'COMPLETED') {
      console.log(`SW: Analysis completed for ${handle}`);
      // Show notification to user
      chrome.notifications?.create({
        type: 'basic',
        iconUrl: 'favicon.svg',
        title: 'Analysis Complete!',
        message: `Your personalized roadmap for ${handle} is ready.`
      });
      return;
    }

    if (data.status === 'ERROR') {
      console.error(`SW: Backend reported error for ${handle}`);
      return;
    }

    // Exponential backoff: Max delay of 30 seconds
    const nextDelay = Math.min(delay * 1.5, 30000);
    setTimeout(() => pollStatus(handle, nextDelay), delay);

  } catch (error) {
    console.error('SW Polling Error:', error);
    // Retry after a fixed delay on network error
    setTimeout(() => pollStatus(handle, 5000), 5000);
  }
}
