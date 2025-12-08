# 🐊 Later Gator  
### A Screen Time Awareness Tool for Intentional Digital Habits

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android_API_34-Tested-brightgreen?logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase_Auth-Enabled-orange?logo=firebase&logoColor=white)
![Release](https://img.shields.io/badge/Status-Prototype_Pre--Release-blue)
![License](https://img.shields.io/badge/License-Educational_Use-lightgrey)

Later Gator is an Android application designed to help users recognize when intentional device use begins to drift into passive scrolling. The app provides real-time awareness through usage monitoring, customizable limits for tracked apps, snooze and exit interventions, and Pomodoro-style focus sessions. This prototype was developed as the final project for **CEN3031: Introduction to Software Engineering** at the University of Florida.

---

## 🌟 Project Vision

For students and young adults  
Who struggle with excessive or unconscious screen time  
**Later Gator** is an Android app  
That tracks app usage, enforces daily and weekly limits, and provides immediate, user-controlled popups when limits are reached.  
Unlike device-native controls buried in system menus, Later Gator places awareness, habit-building, and focus tools front and center.

---

## 📱 Current Prototype Features

- Google Sign-In authentication  
- Real-time app usage monitoring  
- Daily and weekly time-limit settings  
- Snooze logic with accountability ledger  
- App-limit intervention popups (requires overlay permission)  
- Pomodoro Study Mode  
- SQLite-based data storage (30+ tables) 
- Simple, functional UI

---

## 🧱 Architecture Overview

Later Gator follows a layered architecture:

- **Presentation Layer** – UI screens, navigation  
- **Application Layer** – settings handlers, timers, popup controllers  
- **Business & Logic Layer** – limit checking, snooze decisions, report creation  
- **Data Layer** – SQLite tables and queries  
- **External Services** – Firebase Authentication, Android OS APIs

---

## 🧰 Tech Stack

**Languages & Tools**  
- Kotlin  
- Android Studio Hedgehog or newer  

**Android Services Used**  
- AccessibilityService  
- UsageStatsManager  
- Foreground Service + Notifications  
- Overlay Permission (Display Over Other Apps)  
- SQLite database with raw queries

**Authentication**  
- Firebase Authentication (Google Sign-In)

---

## 🚧 Technical Debt & Future Enhancements

- Modularize `DatabaseHelper` into smaller components  
- Expand report visualization  
- Add configurable modes and advanced limit logic  
- Improve UI consistency and animations  
- Increase unit testing coverage  
- Explore cloud sync and multi-device support  

---

## 👥 Team Roles

- **Product Manager:** Samantha Wong  
- **Scrum Master:** Madison Holt  
- **Developers:** Danielle Foege, Carrie Ruble  

---

## 📦 Prototype Pre-Release

A runnable Android Studio project is available in **GitHub Releases** as a ZIP file.

Includes:  
- Android Studio project files  
- Kotlin source code and resources  
- SQLite schema  
- README for installation

Excludes:  
- Course documentation  
- Sprint logs  
- Internal planning materials

---

## 📝 Installation Requirements

To run the prototype:

- Android Studio Hedgehog or newer  
- API Level **34+** (tested on API Level 36)  
- Pixel emulator or physical device  
- Ability to grant:
  - Usage Access  
  - Accessibility Access  
  - Overlay Permission (“Display over other apps”)

---

## 📜 License

This project was created for **educational purposes only** as part of  
**CEN3031 – Introduction to Software Engineering (University of Florida)**.

---

# 🐊 Go Gators!  
We genuinely enjoyed building Later Gator and plan to continue improving it beyond this course.
