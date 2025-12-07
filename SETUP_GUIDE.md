# 🚀 FoundIT - Setup Guide

## Quick Setup (3 Steps)

### 1. Firebase Setup
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create new project "LostAndFound"
3. Add Android app with package: `com.example.lostandfound`
4. Download `google-services.json`
5. Place it in `app/` folder

### 2. Enable Firebase Services
- ✅ **Authentication** → Email/Password
- ✅ **Firestore Database** → Start in test mode
- ✅ **Firebase Cloud Messaging**

### 3. Build & Run
```bash
./gradlew build
