# 📱 FoundIT - Lost & Found Mobile Application

![FoundIT](https://img.shields.io/badge/FoundIT-Lost%20%26%20Found-blue)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-purple)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.9.0-blue)
![Firebase](https://img.shields.io/badge/Firebase-Latest-orange)
![License](https://img.shields.io/badge/License-MIT-green)
![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)

A modern Android lost-and-found application built with Kotlin and Jetpack Compose. Originally developed for educational institutions, designed to scale for any community. Features real-time chat, notifications, and smart filtering - all built with 100% free-tier services.

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

### 🛡️ Technical Highlights

- 💰 **100% FREE** - Uses Firebase Spark Plan + Render.com free hosting
- ⚡ **Real-time Updates** - Firebase Firestore synchronization
- 🔧 **Modern Architecture** - MVVM with Jetpack Compose
- 📱 **Optimized** - Base64 image compression, efficient queries

## 📸 Screenshots

| Login Screen | Signup Screen | Dashboard |
|:---:|:---:|:---:|
| <img src="https://github.com/DJNavz1423/FoundIT/blob/0602d09c87e0e5526d9fbd3d1b1acac18018910a/screenshots/login-frame.png" width="200"> | <img src="https://github.com/DJNavz1423/FoundIT/blob/0602d09c87e0e5526d9fbd3d1b1acac18018910a/screenshots/signup-frame.png" width="200"> | <img src="https://github.com/DJNavz1423/FoundIT/blob/0602d09c87e0e5526d9fbd3d1b1acac18018910a/screenshots/dashb-frame.png" width="200"> |

| Create Post | Chat Screen | Profile |
|:---:|:---:|:---:|
| <img src="https://github.com/DJNavz1423/FoundIT/blob/0602d09c87e0e5526d9fbd3d1b1acac18018910a/screenshots/createPost_frame.png" width="200"> | <img src="https://github.com/DJNavz1423/FoundIT/blob/0602d09c87e0e5526d9fbd3d1b1acac18018910a/screenshots/chat-frame.png" width="200"> | <img src="https://github.com/DJNavz1423/FoundIT/blob/0602d09c87e0e5526d9fbd3d1b1acac18018910a/screenshots/profile-frame.png" width="200"> |

## 🚀 Quick Installation

### 📱 For USERS (Download & Install):
1. **Download APK** from our latest release: https://github.com/djnavz1423/FoundIT/releases/tag/v1.0.0
2. **Enable Unknown Sources** (if required):
- Settings → Security → Unknown Sources → Enable

3. **Install APK**:
- Open downloaded `FoundIT.apk`
- Tap "Install"
- Launch the app!

### 🔧 For DEVELOPERS (Build from Source):

#### Prerequisites

- Android Studio (2023.3.1+)
- Firebase Account (Free)
- Android Device/Emulator (API 34+)

#### Setup Steps:
1. **Clone & Configure:**
```bash
git clone https://github.com/djnavz1423/FoundIT.git
cd FoundIT

# Copy template and configure
cp app/google-services.json.template app/google-services.json
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

## Firebase Setup

1. Create Firebase project
2. Enable required services:
   - Authentication (Email/Password)
   - Firestore Database
   - Cloud Messaging
3. Download `google-services.json` → `app/` folder


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

### Notifications
- Push notifications via FCM
- Badge synchronization
- Background/foreground handling

## 📊 Performance

### Image Optimization
- Base64 compression (800px max)
- 70% JPEG quality
- <900KB file size limit
- Lazy loading in lists

### Network Efficiency
- Firestore query optimization
- Efficient real-time listeners
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
| Project Lead | John Ralph V. Sarsaba | Documentation |
| Lead Developer | Daniel Josh L. Navarro | Android Development |
| UI/UX & Docs | John Riche D. Marchan | Design & Documentation |

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

## 🙏 Acknowledgments

- **St. John Paul II College of Davao** - For support & opportunity
- **Firebase Team** - Excellent free-tier services
- **Jetpack Compose Team** - Modern Android UI framework
- **Render.com** - Free server hosting
- **Android Community** - Valuable resources & support

## 📞 Support

- **Repository:** [github.com/djnavz1423/foundit-app](#)
- **Issues:** [GitHub Issues](#)
- **Email:** danielnavarro2444@gmail.com


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
