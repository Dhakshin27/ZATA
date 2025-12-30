Problem Statement :
Bee populations, especially wild rock bees, face serious threats, but existing monitoring methods are invasive, manual, and unsuitable for real-time tracking. There is a need for a simple, non-invasive digital system to map, monitor, and report bee colonies efficiently using modern sensing and mapping technologies.


Rock-Bee Mapping Application Features:
The mapping application is designed to monitor and display Apis dorsata (rock-bee) colonies through an integrated mobile platform. Key implemented features include:
• Geospatial Visualization and Navigation: The application utilises the OSMDroid library to display bee colonies on an interactive map. It includes functional buttons to centre the map on the user’s location and to automatically navigate to the nearest colony.
• Data Integration and Filtering: The system integrates with Firebase Firestore to fetch colony data. It applies a filtering logic where only markers with a "rockBee" status of true and a "confidence" level of ≥ 0.6 are displayed.
• Custom UI/UX Implementation: A specific "Zata Theme" (deep blue-black and amber) has been applied to the interface. Technical solutions were implemented to convert vector drawables into scaled Bitmaps to ensure custom icons (such as the bee marker) function correctly within the OSMDroid API.
• Automated Map Adjustments: Upon fetching data, the map auto-zooms to encompass all active colony markers.

The technology stack employed in the development of the rock-bee colony mapping application integrates modern mobile development frameworks with real-time cloud services.

### Mobile Application and UI Framework
The application is developed using the **Kotlin** programming language. The frontend architecture is constructed using **Android XML layouts** and **Material Design Components**. The user interface is structured around a `CoordinatorLayout` root, which manages a `FrameLayout` for fragments, a `BottomAppBar` for navigation, and `FloatingActionButtons` (FABs) for interactive tasks such as centering the map or reporting colonies.

### Geospatial and Mapping Engine
The **OSMDroid (OpenStreetMap)** library serves as the primary mapping engine. This library enables multi-touch functionality and allows for the implementation of custom overlays. Key geospatial features include:
*   **MyLocationNewOverlay:** Used for real-time user location tracking and centering.
*   **Custom Marker Logic:** To overcome API limitations regarding vector drawables, the system uses the `Bitmap` and `Canvas` classes to convert and scale SVG/PNG assets into `BitmapDrawable` objects for consistent marker rendering.

### Backend and Database Integration
**Firebase Firestore** is utilised as the backend database for data persistence. The system interacts with a "colonies" collection, fetching documents that are mapped to a `ColonyReport` data class. This model stores critical metadata, including latitude, longitude, rock-bee status, and a confidence metric used for data filtering.


## APP ARCHITECTURE
MainActivity
 ├── BottomNavigation
 │
 ├── MapFragment
 │    ├── Load colony data from Supabase
 │    ├── Display markers on map
 │
 └── ReportFragment
      ├── Fetch GPS location
      ├── Capture image
      ├── ML image analysis
      ├── User confirmation
      └── Upload report to Supabase


The setup and configuration of the rock-bee colony mapping application require the integration of specific mobile development frameworks, geospatial libraries, and cloud-based database services. The following instructions detail the technical requirements and implementation steps based on the provided specifications.

### 1. Development Environment and Tech Stack
The application is built using the **Kotlin** programming language for Android. The development environment must support:
*   **Frontend/UI:** Android XML layouts utilising **Material Design Components**.
*   **Map Engine:** The **OSMDroid (OpenStreetMap)** library.
*   **Backend:** **Firebase Firestore** for real-time data persistence.

### 2. Backend and Data Model Configuration
To facilitate data retrieval, a Firebase Firestore instance must be configured with a collection titled `colonies`. Each document within this collection should follow the `ColonyReport` data model, containing the following fields:
*   `lat` (Double): Latitude coordinate.
*   `lng` (Double): Longitude coordinate.
*   `rockBee` (Boolean): Verification of species type.
*   `confidence` (Double): Statistical reliability of the report.

### 3. UI and Theme Implementation
The application adheres to the "Zata Theme," a high-contrast professional palette. Developers should define the following primary hex codes in the resource files:
*   **Primary:** `#0F172A` (Deep Blue-Black).
*   **Accent:** `#F59E0B` (Amber).
*   **Surface/Background:** `#111827` / `#020617`.

The `activity_main.xml` must be structured using a `CoordinatorLayout` root to manage a `FrameLayout` for fragment transitions and a `BottomAppBar` for navigation.

### 4. Geospatial Engine Setup
The `MapView` component from the OSMDroid library requires specific initialisation parameters within the `MapFragment`:
*   **Default Location:** Coordinates set to Bangalore (12.9716, 77.5946) with a default zoom level of 14.0.
*   **User Location:** Implementation of the `MyLocationNewOverlay` to enable automated tracking and "follow" functionality.
*   **Lifecycle Management:** To ensure system stability, the `onResume()` and `onPause()` methods of the `MapView` must be explicitly handled.

### 5. Custom Marker Implementation
Due to API constraints regarding vector scaling in OSMDroid, a custom conversion utility is required to render bee colony markers. The following logic should be implemented to convert SVG/PNG assets into scaled `BitmapDrawable` objects:
1.  **Scaling:** Convert the desired size from density-independent pixels (24dp) to actual pixels based on the device's display metrics.
2.  **Bitmap Creation:** Initialise a `Bitmap` with `ARGB_8888` configuration and utilise a `Canvas` to draw the vector asset into the bitmap.
3.  **Filtering Logic:** Configure the application to only render markers where the Firestore data indicates `rockBee == true` and the `confidence` level is $\geq 0.6$.

### 6. Interaction and Navigation Logic
The application requires the implementation of functional triggers for the Floating Action Buttons (FABs):
*   **Nearest Colony:** A function to calculate the proximity of colonies relative to the user's coordinates, followed by an animated transition to the closest marker.
*   **Auto-Zoom:** An automated routine to adjust the map's bounding box to encompass all active colony markers upon successful data retrieval from Firestore.
