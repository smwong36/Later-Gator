# 🐊 Later Gator — Prototype Release (Pre-Release)
**CEN3031: Introduction to Software Engineering**  
**Team Later Gator**

Welcome to the **Later Gator Prototype Pre-Release**.  
This ZIP contains the standalone Android Studio project used for our final submission. It includes only the runnable code and project files, not the full course repository or documentation.

Later Gator is a screen-time awareness tool designed to help users recognize when intentional use shifts into passive scrolling. The prototype demonstrates core functionality such as usage monitoring, time-limit popups, Pomodoro Study Mode, and snooze logic.

---

## ⚙️ Requirements

Before opening this project, ensure the following are installed:

### 1. Android Studio (Hedgehog or newer)
Includes built-in SDK Manager and emulator tools.

### 2. Android SDK Platform
- **Minimum recommended API Level:** **34+**
- The team primarily tested on **API Level 36** (Android 16).

### 3. An Emulator or Physical Android Device
- Pixel-class emulator recommended (Pixel 9 or similar).
- Physical devices must have:
    - USB debugging enabled
    - Google Play Services (for Firebase authentication)

No additional dependencies are required.

---

## 📦 What’s Included in the ZIP

```
app/
gradle/
gradlew
gradlew.bat
build.gradle
settings.gradle
README.md
```

These files form a complete Android Studio project.  
There are **no class materials, SCRUM logs, PDFs, or documentation files** in the ZIP.

---

## 🚀 How to Open and Run the Project

### 1. Extract the ZIP
Unzip the downloaded file to any directory.

### 2. Open the Project in Android Studio
- Launch Android Studio
- Select **“Open an Existing Project”**
- Choose the extracted project folder
- Allow Gradle to sync (first sync may take a minute)

### 3. Set Up a Device or Emulator
- Create a new Android Virtual Device (AVD)
- Or connect a physical Android device via USB

### 4. Run the App
- Click **Run ▶** in Android Studio
- The app will install on the selected device

---

## 🔐 Permissions Required on First Launch

Later Gator requires **three** device-level permissions:

### 1. Usage Access
Allows Later Gator to detect which app is currently in the foreground.

### 2. Accessibility Access
Enables the monitoring service that detects app switches in real time.

### 3. Overlay Permission (“Display Over Other Apps”)
Allows the time-limit popup to appear on top of other running apps.  
Without this permission, the user will not see intervention popups when limits are reached.

These permissions are essential for time limits, snoozes, monitoring, and real-time feedback.

---

## 🔑 Login & Authentication

Later Gator uses **Google Sign-In via Firebase Authentication**.

You may log in using:

- Any personal Gmail account
- A test Google account

> No behavioral data is uploaded.  
> All usage, snooze, limit, and Pomodoro data is stored **locally** on the device via SQLite.

---

## 📚 Key Features Included in This Prototype

- Google Sign-In authentication
- Daily time-limit settings per tracked app
- Snooze logic with database tracking
- App-limit intervention popups (requires overlay)
- Real-time monitoring via AccessibilityService + UsageStatsManager + Foreground Service
- Pomodoro Study Mode (front-end)
- Local SQLite database with more than 30 pre-defined tables
- Basic home screen and settings navigation

---

## 🧰 Development Notes

This prototype was originally started in React Native but migrated to **native Kotlin + Android Studio** after dependency conflicts blocked progress.  
The architecture now follows a layered structure:

- Presentation Layer
- Application Layer
- Business & Logic Layer
- Data Layer
- External Services Layer

---

## 📅 About the Pre-Release

This ZIP is intended for grading and review for the CEN3031 Final Deliverable.  
It is not a production build.

A future sprint would include:

- Improved reporting visualizations
- UI refinements
- Refactoring the DatabaseHelper into modular components
- Additional testing
- Advanced limit scheduling and modes

---

## 👥 Team Roles
- **Product Manager:** Samantha Wong
- **Scrum Master:** Madison Holt
- **Developers:** Danielle Foege and Carrie Ruble

---

## 📜 License
This project is for educational purposes (CEN3031 – Intro to Software Engineering).

---

## 🙏 Acknowledgments

Thank you for reviewing our project.  
Our team genuinely enjoyed building Later Gator and plans to continue developing it beyond this course.

Go Gators! 🐊💙🧡
