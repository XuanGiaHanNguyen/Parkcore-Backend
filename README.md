# Parking Simulation — Run Instructions

## Prerequisites

Make sure you have the following installed before running anything:

- **Java 21+** — [Download](https://adoptium.net/)
- **Maven** — comes bundled as `mvnw` in the project, no install needed
- **Node.js 18+** — [Download](https://nodejs.org/)
- **npm** — comes with Node.js

---

## Project Structure

```
parking-sim/
├── parkingsim-backend/     ← Spring Boot (Java / Maven)
└── parkingsim-frontend/    ← Next.js (TypeScript / Tailwind)
```

---

## 1. Backend (Spring Boot)

### Setup

Ensure `src/main/resources/application.properties` contains:

```properties
server.port=8080
```

### Run

Navigate to the backend folder:

```bash
cd parkingsim-backend
```

On Mac / Linux:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

### Confirm it's running

You should see this in the terminal:

```
Tomcat started on port(s): 8080
Started ParkingSimApplication in 2.3 seconds
```

---

## 2. Frontend (Next.js)

### Setup

Create a `.env.local` file in the frontend root if it doesn't exist:

```bash
cd parkingsim-frontend
touch .env.local
```

Add this line to `.env.local`:

```
NEXT_PUBLIC_WS_URL=ws://localhost:8080/ws/simulation
```

Install dependencies (first time only):

```bash
npm install
```

### Run

```bash
npm run dev
```

You should see:

```
▲ Next.js 14.x.x
- Local: http://localhost:3000
```

Open your browser at [http://localhost:3000](http://localhost:3000).

---

## 3. Verify Everything Works

| What to check | Where to look | Expected |
|---|---|---|
| Backend running | Terminal | `Tomcat started on port 8080` |
| Frontend running | Terminal | `Local: http://localhost:3000` |
| WebSocket connected | Canvas badge (top-right) | 🟢 `CONNECTED` |
| Vehicles rendering | Canvas | Vehicles moving on the lot |
| WS messages flowing | DevTools → Network → WS tab | Messages appearing in real time |

---

## 4. Checking WebSocket in DevTools

1. Open `http://localhost:3000` in Chrome
2. Open DevTools (`F12` or `Cmd+Opt+I`)
3. Go to **Network** tab → filter by **WS**
4. Click the `ws://localhost:8080/ws/simulation` entry
5. Click the **Messages** tab — you should see JSON payloads arriving every ~100ms

---

## 5. Common Issues

**Badge stays yellow (`CONNECTING`)**
- Backend is not running — start it first before the frontend
- Wrong port — confirm `application.properties` has `server.port=8080`
- `.env.local` missing or has wrong URL

**Vehicles not appearing on canvas**
- WebSocket not connected (see above)
- Backend is not broadcasting yet — make sure `SimulationBroadcaster.java` exists and `@EnableScheduling` is on your main class
- Check browser console for errors

**CORS error in browser console**
- Add `.setAllowedOriginPatterns("*")` to your `WebSocketConfig.java` endpoint registration

**Port 8080 already in use**
```bash
# Mac / Linux — find and kill the process
lsof -i :8080
kill -9 <PID>

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

---

## 6. Stopping the App

- **Backend:** `Ctrl + C` in the backend terminal
- **Frontend:** `Ctrl + C` in the frontend terminal
