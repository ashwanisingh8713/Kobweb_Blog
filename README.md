# 📝 Kobweb Blog

A **Full-Stack Technical Blogging Platform** built using the **Kobweb Framework (Kotlin Multiplatform)**.  
It allows users to create, explore, and manage technical blogs under multiple categories — all with a clean, modern UI and seamless backend integration.

---

## 🚀 Features

- 🧠 **Full-Stack Kotlin Multiplatform App** powered by **Kobweb**
- 🗂️ Blog Categories: **Technology**, **Programming**, and **Design**
- 🔍 Search functionality to find posts quickly
- 👤 **User Authentication:** Sign In / Sign Up
- ✍️ Create and manage blog posts with category tagging
- 🧭 Smooth **page navigation** using Kobweb routing
- 💾 **MongoDB integration** for data persistence
- 🔗 REST-style **API integration** via Kobweb server module
- 🪄 Responsive **Compose for Web UI** optimized for desktop and mobile
- 🧰 Built with clean Kotlin architecture — easy to maintain and extend

---

## 🧱 Project Structure
Kobweb_Blog/
├── .kobweb/                # Kobweb configuration and project metadata
├── gradle/                 # Gradle build configuration files
├── site/                   # Main Kobweb application module
│   ├── commonMain/         # Shared models, DTOs, and utility code
│   ├── jsMain/             # Frontend - Compose for Web UI components
│   │   ├── components/     # Reusable UI components (Navbar, Cards, etc.)
│   │   ├── pages/          # Blog pages, category pages, sign-in/sign-up forms
│   │   └── utils/          # Frontend helpers and constants
│   └── jvmMain/            # Backend - Server logic, routes, and APIs
│       ├── api/            # REST API endpoints
│       ├── db/             # MongoDB connection and data models
│       └── services/       # Authentication, post management, etc.
├── build.gradle.kts        # Project build configuration
├── settings.gradle.kts     # Gradle settings
└── README.md               # Project documentation (this file)

---

## ⚙️ Tech Stack

| Layer | Technology | Description |
|-------|-------------|-------------|
| **Language** | Kotlin Multiplatform | Shared codebase for frontend and backend |
| **Framework** | [Kobweb](https://github.com/varabyte/kobweb) | Full-stack Kotlin web framework |
| **Frontend** | Compose for Web | Reactive UI built with Kotlin |
| **Backend** | Kobweb Server (Ktor-based) | Server-side API and routing |
| **Database** | MongoDB | Stores users, blog posts, and metadata |
| **Build System** | Gradle Kotlin DSL | For dependency management and builds |
| **Authentication** | Custom (JWT or session-based) | User sign-in and registration |
| **Version Control** | Git + GitHub | Source code and issue management |
| **IDE** | IntelliJ IDEA / Android Studio | Recommended development environment |

---

## 🎯 Purpose

Kobweb Blog serves as a full-stack **technical blogging platform** built entirely with **Kotlin**.  
It demonstrates how to develop scalable, maintainable web applications using the **Kobweb Framework**, integrating both frontend and backend logic seamlessly.

---

## 🧑‍💻 Author

**Ashwani Kumar Singh**  
📍 India  
💼 [GitHub Profile](https://github.com/ashwanisingh8713)

---

## 🪄 Future Enhancements

- 🗨️ Comment and Like system
- 🌓 Dark / Light theme switcher
- 📝 Markdown-based blog editor
- ☁️ Deployment to cloud platforms (Vercel, Netlify, or Render)
- 🔐 Role-based admin panel for post management

---

⭐ **If you like this project, please give it a star!**  
Your support helps in improving and adding new features 😊