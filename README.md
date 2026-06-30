# Walk & Hold: Corporate Territory Capture System

**Walk & Hold** is a real-time, location-based gamification platform designed to encourage physical activity within corporate teams. Users walk in the real world to capture territories on a map, compete with colleagues, and dominate city districts.

## 🚀 Key Features

*   **Real-time Territory Capture:** High-precision path tracking and polygon formation as you move.
*   **Path Clipping Logic:** Unique mechanic where completing a polygon can "cut" or invalidate an opponent's active path if they intersect.
*   **Dynamic Map:** Real-time visibility of colleagues and their zones using Yandex MapKit.
*   **Background Tracking:** Robust GPS tracking via Android Foreground Service (API 26+).
*   **Corporate Focus:** Optimized for private group sessions with real-time leaderboards.

## 🛠 Tech Stack

### Backend
*   **Java / Spring Boot:** Core business logic and REST API.
*   **WebSockets (STOMP):** Real-time position broadcasting and event synchronization.
*   **PostGIS:** Spatial database for persistent storage of captured territories.
*   **JTS (Java Topology Suite):** In-memory geometric calculations (Union, Intersection, Difference) for high-performance path clipping.
*   **JWT:** Secure stateless authentication.

### Mobile (Android)
*   **Java / Android SDK:** Native performance and deep system integration.
*   **Yandex MapKit SDK:** Advanced map rendering and geometry visualization.
*   **Foreground Service:** Ensures uninterrupted GPS tracking even when the app is in the background.
*   **Room DB:** Local caching of routes and user data.

## 🏗 Architecture & Logic

The system shifts heavy geometric computations from the database to the application layer. 
1. **Client** streams GPS points via **STOMP**.
2. **Server** uses **JTS** to calculate real-time intersections.
3. If a capture is completed, **PostGIS** persists the new `Polygon` using `ST_Union` and `ST_Difference`.

## 📦 Deployment
The project is containerized using **Docker Compose**, including the Spring Boot application and a pre-configured PostGIS instance.

```bash
docker-compose up -d
```
## 🖼️ Screenshots
<p align="center">
  <img width="415" height="927" alt="image" src="https://github.com/user-attachments/assets/9c1a52a1-e3d9-4b2b-bd1b-b7f5e0d964e3" />
</p>
<p align="center">
  <img width="407" height="927" alt="image" src="https://github.com/user-attachments/assets/2767c518-0f6e-4abc-84a2-2a21eda504ce" />
</p>
<p align="center">
  <img width="410" height="927" alt="image" src="https://github.com/user-attachments/assets/ae8420d6-126f-4333-b33f-65e4d48532dd" />
</p>
<p align="center">
  <img width="410" height="927" alt="image" src="https://github.com/Sauron971/WolkAndHold-GeoService/blob/master/static/UI_Interacting.gif" />
</p>
<p align="center">
  <img width="410" height="927" alt="image" src="https://github.com/Sauron971/WolkAndHold-GeoService/blob/master/static/Process_grab.gif" />
</p>


