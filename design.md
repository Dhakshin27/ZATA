The following documentation provides a comprehensive technical design for the Rock Bee Colony Mapping Application, as detailed in the technical specifications.

---

# Design Documentation: Rock Bee Colony Mapping Application

## 1. Project Overview
The primary objective of this application is to facilitate the monitoring and spatial visualisation of rock-bee (*Apis dorsata*) colonies. The system integrates real-time cloud data with a mobile Geographic Information System (GIS) to provide users with tools for colony identification, location tracking, and reporting.

## 2. Architectural Design
The application is developed for the Android platform using **Kotlin** and follows a modular design pattern integrating the following components:
*   **Frontend:** Android XML layouts and Material Design Components.
*   **Mapping Engine:** OSMDroid (OpenStreetMap) for geospatial rendering.
*   **Backend/Database:** Firebase Firestore for dynamic data persistence and retrieval.

## 3. User Interface (UI) and Experience (UX)
### 3.1 The "Zata" Theme
The application employs a high-contrast professional palette designed for field visibility:
*   **Primary:** `#0F172A` (Deep Blue-Black).
*   **Accent:** `#F59E0B` (Amber), reflecting the bee-centric theme.
*   **Background/Surface:** `#020617` (Near-black) and `#111827` (Card surface).

### 3.2 Layout Structure
The interface is managed via a `CoordinatorLayout` root to handle complex view interactions:
*   **Navigation:** A `BottomAppBar` houses a `BottomNavigationView` for fragment transitions.
*   **Interaction:** Floating Action Buttons (FABs) are strategically positioned at the bottom corners to prevent overlap with map markers or navigation elements.
*   **Content:** A `FrameLayout` serves as the container for dynamic fragments, with specific padding to ensure UI elements do not obstruct the navigation bar.

## 4. Mapping and Geospatial Logic
### 4.1 Engine Configuration
The **OSMDroid** library is configured with the following parameters to ensure optimal performance:
*   **Multi-touch:** Enabled for intuitive navigation.
*   **Default Location:** Initialised to Bangalore (12.9716, 77.5946) at a zoom level of 14.0.
*   **User Tracking:** Utilises `MyLocationNewOverlay` for automated "follow" functionality and real-time positioning.

### 4.2 Marker Implementation and Filtering
Colony data is fetched dynamically from the `colonies` collection in Firestore. To maintain data integrity, the application applies strict filtering logic:
*   **Inclusion Criteria:** Markers are only rendered if `rockBee == true` and the confidence level is $\geq 0.6$ (60%).
*   **Visual Assets:** Custom icons (`ic_bee_marker`) are used. To ensure compatibility with the OSMDroid API, vector assets (SVG/PNG) are programmatically converted into scaled **Bitmaps** (24dp) to maintain a fixed size regardless of zoom.

## 5. Functional Design
### 5.1 Data Model
The system utilises a structured data class for colony reports:
```kotlin
data class ColonyReport(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val rockBee: Boolean = false,
    val confidence: Double = 0.0
)
```

### 5.2 Key Features
*   **Nearest Colony Navigation:** The application calculates the proximity of identified colonies to the user's location and provides an animated transition to the closest marker.
*   **Auto-Zoom:** Upon data retrieval, the map automatically adjusts its bounding box to encompass all filtered colony markers.
*   **Lifecycle Management:** Explicit handling of `onResume` and `onPause` for the `MapView` prevents system crashes and memory leaks.

## 6. Future Technical Roadmap
To enhance the system's scalability and utility, the following implementations are planned:
*   **Clustering:** Integration of `RadiusMarkerClusterer` to manage high-density marker areas.
*   **Dynamic Scaling:** Developing logic for markers to scale proportionally with map zoom levels.
*   **Integrated Reporting:** Implementation of the Firestore write logic and form interface for the "Report Colony" FAB.
