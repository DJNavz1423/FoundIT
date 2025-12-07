# 📱 FoundIT - Lost & Found Mobile Application

![FoundIT](https://img.shields.io/badge/FoundIT-Lost%20%26%20Found-blue)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-purple)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.0-blue)
![Firebase](https://img.shields.io/badge/Firebase-Latest-orange)
![License](https://img.shields.io/badge/License-MIT-green)
![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)

A modern Android lost-and-found application built with Kotlin and Jetpack Compose. Originally developed for educational institutions, designed to scale for any community. Features real-time chat, notifications, and smart filtering - all built with 100% free-tier services.

## ✨ Live Demo

- **APK Download:** [Download FoundIT](#)
- **Demo Video:** [Watch Demo](#)

## 🎯 Features

### 🚀 Core Features

- 🔍 **Smart Posting** - Report lost/found items with photos & descriptions
- 💬 **Real-time Chat** - Direct messaging between users
- 🔔 **Push Notifications** - Get notified of new messages
- 👤 **User Profiles** - Manage your account and posts
- 📊 **Dashboard Filtering** - Filter by Lost/Found status

### ⚡ Advanced Features

- 🖼️ **Base64 Image Storage** - No Firebase Storage costs!
- 🔴 **Unread Message Badges** - Visual indicators for new chats
- ✏️ **Profile CRUD** - Edit name & delete posts
- 👁️ **Password Toggle** - Show/hide password during login
- 🎨 **Custom UI** - Material Design 3 with modern animations

### 🛡️ Technical Highlights

- 💰 **100% FREE** - Uses Firebase Spark Plan + Render.com free hosting
- ⚡ **Real-time Updates** - Firebase Firestore synchronization
- 🔧 **Modern Architecture** - MVVM with Jetpack Compose
- 📱 **Optimized** - Base64 image compression, efficient queries

## 📸 Screenshots

| Login Screen | Dashboard | Create Post |
|:---:|:---:|:---:|
| <img src="screenshots/login.jpg" width="200"> | <img src="screenshots/dashboard.jpg" width="200"> | <img src="screenshots/create_post.jpg" width="200"> |

| Chat Screen | Profile | Notifications |
|:---:|:---:|:---:|
| <img src="screenshots/chat.jpg" width="200"> | <img src="screenshots/profile.jpg" width="200"> | <img src="screenshots/notifications.jpg" width="200"> |

## 🚀 Quick Start

### Prerequisites

- Android Studio (2023.3.1+)
- Firebase Account (Free)
- Android Device/Emulator (API 34+)

### Installation (3 Minutes)

1. **Clone the repository**

```bash
git clone https://github.com/yourusername/foundit-app.git
cd foundit-app
```

2. **Set up Firebase**

   - Create project at [Firebase Console](https://console.firebase.google.com/)
   - Enable: Authentication, Firestore, Cloud Messaging
   - Download `google-services.json` → Place in `app/` folder

3. **Build & Run**

```bash
./gradlew build
```

Run on emulator or physical device

### 📱 One-Command Setup

```bash
# Run setup script (if available)
chmod +x setup.sh
./setup.sh
```

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Jetpack       │    │     MVVM        │    │    Firebase     │
│   Compose UI    │◄───┤   ViewModel     │◄───┤   Repository    │
│                 │    │                 │    │                 │
│ • Screens       │    │ • State         │    │ • Firestore     │
│ • Components    │    │ • Logic         │    │ • Auth          │
│ • Navigation    │    │ • Events        │    │ • Storage       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                        │                       │
         └────────────────────────┼───────────────────────┘
                                  ▼
                       ┌─────────────────┐
                       │    Node.js      │
                       │  Server (FCM)   │
                       │  Render.com     │
                       └─────────────────┘
```

## 🛠️ Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| UI | Jetpack Compose | Modern declarative UI |
| Language | Kotlin | Primary development language |
| Database | Firebase Firestore | Real-time NoSQL database |
| Auth | Firebase Authentication | User management |
| Storage | Base64 in Firestore | Cost-effective image storage |
| Notifications | FCM + Node.js | Push notifications |
| Hosting | Render.com | Free server hosting |
| Architecture | MVVM | Clean separation of concerns |

## 📁 Project Structure

```
foundit-app/
├── app/                          # Android Application
│   ├── src/main/java/com/foundit/
│   │   ├── data/                # Data layer
│   │   │   ├── models/          # Data classes
│   │   │   ├── repository/      # Firebase repositories
│   │   │   └── firebase/        # Firebase implementations
│   │   ├── domain/              # Business logic
│   │   │   └── usecases/        # Use cases
│   │   ├── presentation/        # UI layer
│   │   │   ├── screens/         # All app screens
│   │   │   ├── components/      # Reusable composables
│   │   │   ├── viewmodels/      # ViewModels
│   │   │   └── navigation/      # Navigation graph
│   │   └── di/                  # Dependency injection
│   ├── google-services.json     # Firebase config (gitignored)
│   └── google-services.json.template  # Template for setup
├── server/                      # Node.js notification server
│   ├── index.js                # Server logic
│   ├── package.json            # Dependencies
│   ├── .env.template           # Environment template
│   └── .env                    # Actual env (gitignored)
├── docs/                       # Documentation
├── screenshots/                # App screenshots
├── .gitignore                  # Git ignore rules
└── README.md                   # This file
```

## 🔧 Configuration

### 1. Firebase Setup

1. Create Firebase project
2. Enable required services:
   - Authentication (Email/Password)
   - Firestore Database
   - Cloud Messaging
3. Download `google-services.json` → `app/` folder

### 2. Notification Server (Optional)

```bash
cd server
cp .env.template .env
# Edit .env with your Firebase credentials
npm install
# Deploy to Render.com (free)
```

### 3. Build Configuration

Edit `app/build.gradle.kts` if needed:

```kotlin
android {
    compileSdk = 34
    defaultConfig {
        applicationId = "com.foundit"
        minSdk = 24
        targetSdk = 34
    }
}
```

## 📱 Features in Detail

### Authentication
- Email/password login & signup
- Input validation
- Password visibility toggle
- Session persistence

### Post Management
- Create posts with images (Base64)
- Real-time updates
- Smart filtering (Lost/Found/All)
- Location & category tagging
- Delete/Edit posts

### Messaging
- Real-time one-on-one chat
- Unread message badges
- Message history
- Typing indicators

### Notifications
- Push notifications via FCM
- Badge synchronization
- Background/foreground handling

## 🧪 Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Test coverage report
./gradlew jacocoTestReport
```

## 🚢 Deployment

### Generate Release APK

```bash
# Build release APK
./gradlew assembleRelease

# Build AAB for Play Store
./gradlew bundleRelease
```

### Play Store Checklist

- [ ] Update version code & name
- [ ] Generate signed APK/AAB
- [ ] Test on multiple devices
- [ ] Update Firebase rules
- [ ] Prepare store listing
- [ ] Screenshots & description

## 📊 Performance

### Image Optimization
- Base64 compression (800px max)
- 70% JPEG quality
- <900KB file size limit
- Lazy loading in lists

### Network Efficiency
- Firestore query optimization
- Efficient real-time listeners
- Offline capability
- Request batching

### Memory Management
- Proper ViewModel lifecycle
- Image resource cleanup
- Efficient list rendering
- Leak prevention

## 🔍 Troubleshooting

### Common Issues

**"Missing google-services.json"**

```bash
# Download from Firebase Console
# Place in app/ folder
```

**Build Errors**

```bash
./gradlew clean
./gradlew build
```

**Firebase Connection Issues**
- Check internet connection
- Verify Firebase project is active
- Confirm package name matches

**Notifications Not Working**
- Verify FCM token generation
- Check notification server status
- Review Firebase Cloud Messaging setup

### Debug Mode

Enable in `BuildConfig.kt`:

```kotlin
const val DEBUG_MODE = true  // Set to false for release
```

## 👥 Team

| Role | Name | Contribution |
|------|------|--------------|
| Project Lead | John Riche D. Marchan | Architecture & Backend |
| Lead Developer | Daniel Josh L. Navarro | Android Development |
| UI/UX & Docs | John Ralph V. Sarsaba | Design & Documentation |
| Research Adviser | John Patrick Eleria | Guidance & Review |

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable names
- Add comments for complex logic
- Write unit tests for new features

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2025 FoundIT Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 🙏 Acknowledgments

- **St. John Paul II College of Davao** - For support & opportunity
- **Firebase Team** - Excellent free-tier services
- **Jetpack Compose Team** - Modern Android UI framework
- **Render.com** - Free server hosting
- **Android Community** - Valuable resources & support

## 📞 Support

- **Repository:** [github.com/yourusername/foundit-app](#)
- **Issues:** [GitHub Issues](#)
- **Email:** your-team@example.com
- **Documentation:** [Docs Folder](docs/)

## 📚 Documentation Links

- [Architecture Guide](docs/architecture.md)
- [API Documentation](docs/api.md)
- [Deployment Guide](docs/deployment.md)
- [User Manual](docs/user-manual.md)

---

<div align="center">

### 🏆 College Capstone Project
**St. John Paul II College of Davao**  
**College of Information and Technology**  
**December 2025**

*"Making Lost Things Found"* 🔍✨

[⬆ Back to Top](#-foundit---lost--found-mobile-application)

</div>

## 📊 Project Status

- **Version:** 1.0.0
- **Status:** ✅ Production Ready
- **Last Updated:** December 2025
- **Maintenance:** Active

## 🌟 Star History

[![Star History Chart](https://api.star-history.com/svg?repos=yourusername/foundit-app&type=Date)](https://star-history.com/#yourusername/foundit-app&Date)