# 🐝 Bee Pollen & Plant Management System

A comprehensive, full-stack Spring Boot application designed to manage apiaries, bee colonies, plant flora, and track pollen harvesting yields. The system integrates IoT data simulation for real-time tracking, AI-powered assistance using Google Gemini, and a robust role-based access control architecture.

---

## 🌟 Features

### 🔐 Security & User Management
*   **Hybrid Authentication**: Supports both Form-based Session Login (for Web UI) and Stateless JWT Bearer Tokens (for REST APIs).
*   **Role-Based Access Control (RBAC)**:
    *   `ADMIN`: Full access to all modules and configurations.
    *   `BEEKEEPER`: Manage bee colonies, track collections, and view plant/pollen databases.
    *   `RESEARCHER`: Read-only access to plant and pollen botanical data.
    *   `USER`: Default role.
*   **Secure Password Recovery**: Built-in "Forgot Password" flow utilizing one-time, time-limited reset tokens and simulated email delivery.
*   **User Activity Log (Audit Trail)**: Powered by Spring Data JPA Auditing, automatically tracking `CreatedBy` and `UpdatedBy` actions across the platform to build a chronological user activity feed.

### 🌼 Core Domain Modules
*   **Flora & Plant Management**: Catalog botanical data, flowering seasons, and regions.
*   **Pollen Profiles**: Manage pollen types, microscopic shapes, and color codes. Maps Many-to-Many with Plants.
*   **Apiary & Bee Colonies**: Track colony health status (`HEALTHY`, `SICK`, `WEAK`), species, GPS coordinates, and estimated populations.
*   **Harvest Tracking**: Record and aggregate pollen collection yields (in grams) mapped to specific colonies and pollen types.

### 🤖 Smart Features
*   **IoT Simulator**: A built-in background scheduler (`IotDeviceSimulator`) that mimics smart scales/sensors in the apiary, automatically reporting random pollen harvest data periodically.
*   **AI Assistant**: Integration with **Google Gemini AI** to provide smart chatbot capabilities or contextual botanical information retrieval directly within the app.

### 📊 Dashboard & UI
*   **Responsive UI**: Built with Thymeleaf Layout Dialect, Bootstrap 5, and Bootstrap Icons.
*   **Analytics Dashboard**: Visualizes top-collected pollen types, total yields, and colony health distributions.
*   **Dynamic Navigation**: UI elements and action buttons (Add, Edit, Delete) dynamically adapt based on the user's security role.

---

## 🛠️ Technology Stack

**Backend**
*   **Java 17**
*   **Spring Boot 3.2.x** (Web, Data JPA, Security, Mail, Validation)
*   **Spring Security 6** (JWT + BCrypt Password Encoding)
*   **Hibernate** (ORM)
*   **MySQL** (Relational Database)
*   **Lombok** (Boilerplate reduction)

**Frontend**
*   **Thymeleaf** (Server-side rendering)
*   **Bootstrap 5** (CSS Framework)
*   **Thymeleaf Extras Spring Security 6** (UI Security tags)

---

## 🚀 Getting Started

### Prerequisites
*   **JDK 17** or higher
*   **Maven** 3.8+
*   **MySQL Server** (Running on `localhost:3306`)

### Configuration
1. Open `src/main/resources/application.yml`.
2. Configure your MySQL database credentials:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/bee_pollen_db?createDatabaseIfNotExist=true
       username: root
       password: yourpassword
   ```
3. *(Optional)* Configure the **Gemini API Key** to enable AI features:
   ```yaml
   gemini:
     api:
       key: YOUR_GEMINI_API_KEY
   ```
4. *(Optional)* Enable Email Sending for the Forgot Password feature by uncommenting the `spring.mail` block in `application.yml` and adding your SMTP credentials.

### Running the Application

To fully utilize the AI features, set your Gemini API key as an environment variable before running the application.

**For Windows (PowerShell):**
```powershell
$env:GEMINI_API_KEY="your_api_key_here"
mvn clean spring-boot:run
```

**For macOS/Linux (Bash/Zsh):**
```bash
export GEMINI_API_KEY="your_api_key_here"
./mvnw clean spring-boot:run
```

*(Note: If you do not set the `GEMINI_API_KEY`, the application will still start successfully, but the AI Assistant features will not work).*

The application will automatically:
1. Create the database schema (`ddl-auto: update`).
2. Seed initial mock data (Admin, Beekeeper, and Researcher accounts, along with sample plants and colonies) via `DataInitializer.java`.

### Default Accounts
You can log in with the following seeded accounts:
*   **Admin**: `admin` / `admin123`
*   **Beekeeper**: `beekeeper` / `beekeeper123`
*   **Researcher**: `researcher` / `researcher123`

Access the web interface at: **http://localhost:8080**

---

## 📂 Project Structure

```text
src/main/java/com/beepollen/
├── ai/                 # Google Gemini AI client integration
├── config/             # Spring Security, JPA Auditing, and Data Seeding configs
├── controller/         # Spring MVC Web Controllers
├── dto/                # Data Transfer Objects for Auth & Requests
├── entity/             # JPA Domain Models (Plant, Colony, User, etc.)
├── exception/          # Custom Global Exceptions
├── iot/                # IoT Device Simulation Scheduler
├── repository/         # Spring Data JPA Repositories
├── security/           # JWT Filters, UserDetails implementations
└── service/            # Core Business Logic & Activity Logging
```

---

## 📄 License
This project is for educational and demonstrative purposes, built by an AI assistant!
