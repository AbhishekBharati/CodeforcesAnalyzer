# Codeforces Analyzer Pro

**Codeforces Analyzer Pro** is a high-performance, distributed AI analysis platform designed to provide competitive programmers with surgical, data-driven coaching. By transforming thousands of raw submissions into structured intelligence, the system generates personalized 4-week study roadmaps using GPT-4.

---

## Key Features

- **Asynchronous Data Ingestion:** Handles massive (3MB+) Codeforces submission payloads without blocking the UI using the **HTTP 202 Accepted** pattern.
- **Distributed "Brain" Architecture:** Separates data collection from heavy AI processing using **RabbitMQ**, ensuring the system scales horizontally.
- **Math-Driven Aggregator:** A custom metrics engine that distills raw JSON into topic-wise failure rates, average ratings, and performance plateaus.
- **AI-Powered Coaching:** Integrates **OpenAI GPT-4** to interpret performance metrics and generate professional, week-by-week study roadmaps.
- **Real-Time Dashboard:** A modern Chrome Extension UI featuring dual-tabs for **AI Roadmap** and **Topic Stats**, synchronized via a Background Service Worker.
- **Resilient Polling:** Background scripts utilize **Exponential Backoff** to monitor analysis status, providing a seamless "Set it and forget it" experience.

---

## Architecture

The project is built using a **Tier-1 Distributed Microservices** pattern:

1.  **Frontend (Chrome Extension):** Vite + React + TypeScript. Uses a Service Worker for background state management.
2.  **Backend (Producer):** Spring Boot (Java 21). Handles API requests, MongoDB ingestion, and RabbitMQ task dispatching.
3.  **Worker (Consumer):** Spring Boot (Java 21). Processes heavy data, performs math aggregation, and orchestrates AI calls.
4.  **Message Queue:** RabbitMQ for decoupled, event-driven communication.
5.  **Persistence:** MongoDB for landing raw data and storing final analysis results.

---

## 🛠️ Tech Stack

- **Languages:** Java 21, TypeScript, SQL-like MongoDB queries.
- **Frameworks:** Spring Boot 3.4, React 18.
- **Messaging:** RabbitMQ (AMQP).
- **Database:** MongoDB (with Mongo Express for live data visualization).
- **AI:** OpenAI GPT-4 API.
- **DevOps:** Docker, Docker Compose, Multi-stage Builds.

---

## Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 21 (JDK)
- Node.js & npm
- OpenAI API Key

### 1. Infrastructure Setup
Start the databases and management UIs:
```bash
docker-compose up -d
```
- **Mongo Express:** `http://localhost:8081`
- **RabbitMQ Management:** `http://localhost:15672` (guest/guest)

### 2. Backend Setup
```bash
cd Backend
./mvnw spring-boot:run
```

### 3. Worker Setup
Set your OpenAI key and start the brain:
```bash
export OPENAI_API_KEY=sk-your-key-here
cd Worker
./mvnw spring-boot:run
```

### 4. Frontend Setup
```bash
cd frontend
npm install
npm run build
```
**To Install:**
1. Open Chrome and go to `chrome://extensions/`.
2. Enable **Developer mode**.
3. Click **Load unpacked** and select the `frontend/dist` folder.

---

## Testing the Flow
1. Open the extension and enter a handle (e.g., `tourist`).
2. Click **Analyze**.
3. You will receive a notification (or see the UI update) once the AI has finished its analysis.
4. View your **Roadmap** and **Topic Stats** in the dashboard!

---

Developed with ❤️ for the Competitive Programming Community.
