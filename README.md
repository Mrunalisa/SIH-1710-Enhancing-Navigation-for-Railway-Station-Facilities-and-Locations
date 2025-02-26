# SIH-1710-Enhancing-Navigation-for-Railway-Station-Facilities-and-Locations

Overview
This project aims to revolutionize indoor navigation at railway stations using Augmented Reality (AR) and real-time pathfinding. By leveraging ARCore, users can navigate large, unfamiliar station premises with AR overlays guiding them to their destination.

Key Features
✅ AR-Based Indoor Mapping – Uses Google ARCore to scan and tag key locations.
✅ Real-Time Navigation – Overlays AR directional arrows for intuitive guidance.
✅ Dynamic Indoor Mapping – Captures railway station layouts dynamically (no preloaded blueprints required).
✅ A Pathfinding Algorithm* – Computes the shortest route efficiently.
✅ Firebase Database – Stores indoor maps, locations, and user data.
✅ User-Friendly UI/UX – Designed using Figma for an intuitive experience.



Technology Stack
![image](https://github.com/user-attachments/assets/e903c8f3-414d-4562-aef6-840d86646c42)

Project Workflow
1. Mapping the Railway Station (Admin Mode)
[Start ARCore Session]  
       │  
       ▼  
[Walk Through the Station & Detect Surfaces]  
       │  
       ▼  
[Drop AR Anchors at Key Locations (Platforms, Exits, Ticket Counters)]  
       │  
       ▼  
[Log Paths & Generate Navigation Graph]  
       │  
       ▼  
[Save Map Data in Firebase for Future Use]

2. User Navigation Mode
[User Opens App]  
       │  
       ▼  
[Load Indoor Map from Firebase]  
       │  
       ▼  
[User Selects Destination]  
       │  
       ▼  
[Compute Best Route Using A* Algorithm]  
       │  
       ▼  
[Overlay AR Navigation Guides on Camera]  
       │  
       ▼  
[User Follows AR Directions]  
       │  
       ▼  
[Recalculate Path If User Deviates]  

