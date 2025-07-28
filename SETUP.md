# Quick Setup Guide - Gestion Commerciale

## 🚀 Running in IntelliJ IDEA (Recommended)

### Prerequisites
- Java 17 or higher
- IntelliJ IDEA Community Edition
- Maven (optional, can be managed by IntelliJ)

### Setup Steps

#### 1. Open Project in IntelliJ IDEA
1. Open IntelliJ IDEA
2. Click "Open" or "File → Open"
3. Navigate to the project folder: `GestionCommerciale`
4. Select the folder and click "OK"
5. Wait for IntelliJ to import the Maven project

#### 2. Configure Project SDK
1. Go to "File → Project Structure" (Ctrl+Alt+Shift+S)
2. Under "Project Settings → Project"
3. Set "Project SDK" to Java 17 or higher
4. Set "Project language level" to "17 - Sealed types, always-strict floating-point semantics"
5. Click "OK"

#### 3. Import Maven Dependencies
1. IntelliJ should automatically detect the Maven project
2. If prompted, click "Import Maven Projects"
3. Wait for dependencies to download (check progress in bottom status bar)
4. If needed, click the Maven refresh button in the Maven tool window

#### 4. Run the Application
**Option A: Use Pre-configured Run Configuration**
1. Look for "GestionCommercialeApp" in the run configurations dropdown (top right)
2. Click the green "Run" button

**Option B: Run from Main Class**
1. Navigate to `src/main/java/com/gestioncommerciale/GestionCommercialeApp.java`
2. Right-click on the file
3. Select "Run 'GestionCommercialeApp.main()'"

**Option C: Run from Terminal in IntelliJ**
1. Open Terminal in IntelliJ (Alt+F12)
2. Run: `mvn compile exec:java -Dexec.mainClass="com.gestioncommerciale.GestionCommercialeApp"`

### 5. First Login
- **Username**: `admin`
- **Password**: `admin123`

---

## 💻 Running from Command Line

### Prerequisites
- Java 17+ installed and in PATH
- Maven installed (optional)

### Using Maven
```bash
# Navigate to project directory
cd GestionCommerciale

# Compile and run
mvn compile exec:java -Dexec.mainClass="com.gestioncommerciale.GestionCommercialeApp"
```

### Using Provided Scripts
**Windows:**
```cmd
# Build the project
build.bat

# Run the application
run.bat
```

**Linux/Mac:**
```bash
# Make scripts executable
chmod +x run.sh

# Run the application
./run.sh
```

---

## 🎯 Testing the Application

### 1. Login Module
- Launch the application
- A splash screen will appear for 2 seconds
- Login window opens automatically
- Use credentials: `admin` / `admin123`

### 2. Main Dashboard Navigation
After successful login:
- **File Menu**: Company info, logout, exit
- **Management Menu**: Users, clients, articles (requires EMPLOYEE+ role)
- **Documents Menu**: Quotations, delivery notes, invoices (requires EMPLOYEE+ role)  
- **Statistics Menu**: Dashboard, reports (requires MANAGER+ role)
- **Tools Menu**: Stock, banking (requires MANAGER+ role)
- **Help Menu**: About dialog

### 3. User Management (Admin Only)
- Login as admin
- Go to "Gestion → Utilisateurs"
- Currently shows placeholder message (Phase 2 implementation)

### 4. Test Users Available
All users have password: `admin123`
- **admin** (ADMIN): Full access to all features
- **manager** (MANAGER): Access to management and statistics
- **employee** (EMPLOYEE): Access to basic management features  
- **viewer** (VIEWER): Read-only access

---

## 🗄️ Database Information

### SQLite Database
- **File**: `gestion_commerciale.db` (created automatically)
- **Location**: Project root directory
- **Sample Data**: Automatically loaded on first run

### Database Schema
- **users**: User accounts and authentication
- **company_info**: Business information
- **clients**: Customer/supplier data
- **articles**: Product catalog

### Sample Data Included
- 4 test users with different roles
- 1 sample company profile
- 3 sample clients/suppliers
- 5 sample articles/products

---

## 🔧 Troubleshooting

### Common Issues

#### Maven Dependencies Not Loading
1. Refresh Maven project: "View → Tool Windows → Maven → Refresh"
2. Reimport: "File → Invalidate Caches and Restart"

#### "Cannot find symbol" Errors
1. Ensure Project SDK is set to Java 17+
2. Check that Maven dependencies are downloaded
3. Rebuild project: "Build → Rebuild Project"

#### Database Connection Issues
1. Check that SQLite JDBC driver is in dependencies
2. Verify write permissions in project directory
3. Check logs in `gestion_commerciale.log`

#### Application Won't Start
1. Verify Java 17+ is installed: `java -version`
2. Check that main class exists: `GestionCommercialeApp.java`
3. Look for error messages in console/logs

### Logs and Debugging
- **Log file**: `gestion_commerciale.log` (created in project root)
- **Console output**: Check IntelliJ Run window
- **Database file**: `gestion_commerciale.db` (SQLite database)

---

## 📋 Next Steps

This is **Phase 1** of the application - the framework is complete with:
- ✅ Authentication system
- ✅ Database setup with sample data  
- ✅ Navigation structure
- ✅ Role-based access control

**Phase 2** will implement:
- Full CRUD operations for all entities
- Forms for data entry and editing
- Data validation and business logic

The application is ready for development and can be extended with additional features as needed.
