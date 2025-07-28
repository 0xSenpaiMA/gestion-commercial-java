# Gestion Commerciale - Professional Java Swing Desktop Application

## 📋 Description

**Gestion Commerciale** is a comprehensive, professional-grade Java Swing desktop application designed for commercial management. It provides a complete solution for small to medium enterprises to manage their commercial operations including authentication, inventory, billing, statistics, contracts, stock, user permissions, and banking features.

## 🚀 Features

### Phase 1: Application Framework & Core Menus ✅
- [x] Main application structure with professional MVC architecture
- [x] User authentication system with role-based access control
- [x] Modern, intuitive Swing UI with custom styling
- [x] Database integration with SQLite/MySQL support
- [x] Logging and error handling

### Core Modules (Ready for Implementation)
- **Authentication**: User management with roles (Admin, Manager, Employee, Viewer)
- **Company Information**: Complete company details management
- **Clients/Suppliers**: Customer and supplier management
- **Articles**: Product catalog with pricing and stock tracking
- **Quotations**: Quote generation and management
- **Delivery Notes**: Delivery documentation
- **Invoices**: Invoice generation with PDF export
- **Purchase Orders**: Purchase order management
- **Statistics Dashboard**: Business analytics and reports
- **Stock Management**: Real-time inventory tracking
- **Banking**: Bank account and payment management

## 🛠️ Technology Stack

- **Language**: Java SE 11+
- **GUI Framework**: Java Swing
- **Architecture**: MVC (Model-View-Controller)
- **Database**: SQLite (default) / MySQL
- **ORM**: Hibernate/JPA
- **Build Tool**: Maven
- **PDF Generation**: iText
- **Charts**: JFreeChart
- **Email**: JavaMail API

## 📁 Project Structure

```
GestionCommerciale/
├── src/main/java/com/gestioncommerciale/
│   ├── GestionCommercialeApp.java          # Main application entry point
│   ├── config/
│   │   ├── DatabaseConfig.java             # Database configuration
│   │   └── SQLiteDialect.java              # Custom SQLite dialect
│   ├── model/                              # Entity classes
│   │   ├── User.java                       # User entity
│   │   ├── CompanyInfo.java                # Company information
│   │   ├── Client.java                     # Client/Supplier entity
│   │   └── Article.java                    # Product/Article entity
│   ├── service/                            # Business logic layer
│   │   ├── AuthService.java                # Authentication service
│   │   └── UserService.java                # User management service
│   ├── view/                               # User interface layer
│   │   ├── LoginFrame.java                 # Login window
│   │   └── MainFrame.java                  # Main application window
│   └── util/
│       └── UIUtils.java                    # UI utility methods
├── src/main/resources/
│   ├── META-INF/persistence.xml            # JPA configuration
│   └── application.properties              # Application settings
└── pom.xml                                 # Maven dependencies
```

## 🔧 Setup & Installation

### Prerequisites
- Java 11 or higher
- Maven 3.6+

### Installation Steps

1. **Clone or download the project**
   ```bash
   # Navigate to the project directory
   cd GestionCommerciale
   ```

2. **Build the project**
   ```bash
   mvn clean compile
   ```

3. **Run the application**
   ```bash
   mvn exec:java -Dexec.mainClass="com.gestioncommerciale.GestionCommercialeApp"
   ```

### Default Login Credentials
- **Username**: `admin`
- **Password**: `admin123`

## 📊 Database

The application uses SQLite by default with automatic database creation. The database file (`gestion_commerciale.db`) will be created in the project root directory on first run.

### Entities
- **Users**: Authentication and user management
- **Company Info**: Business information
- **Clients**: Customer and supplier data
- **Articles**: Product catalog with pricing

## 🎯 Development Phases

### ✅ Completed
- [x] **Phase 1**: Application framework, authentication, and core navigation
- [x] Project structure with Maven configuration
- [x] Database setup with Hibernate/JPA
- [x] User authentication with role-based access
- [x] Professional UI with custom styling
- [x] Main navigation menu

### 🔄 Ready for Implementation
- [ ] **Phase 2**: CRUD operations for core entities
- [ ] **Phase 3**: Quotations module
- [ ] **Phase 4**: Delivery notes module
- [ ] **Phase 5**: Invoicing with PDF export
- [ ] **Phase 6**: Purchase orders
- [ ] **Phase 7**: Statistics and reports
- [ ] **Phase 8**: Payment tracking
- [ ] **Phase 9**: Assistance contracts
- [ ] **Phase 10**: Fiscal year closure
- [ ] **Phase 11**: Advanced stock management
- [ ] **Phase 12**: Email integration
- [ ] **Phase 13**: Advanced user permissions
- [ ] **Phase 14**: Banking module

## 🔐 Security Features

- Password hashing with SHA-256
- Role-based access control (RBAC)
- Session management
- Input validation and sanitization

## 📱 User Interface

- Modern, professional Swing interface
- Responsive layout design
- Custom color scheme and fonts
- Intuitive navigation
- Context-sensitive menus based on user roles

## 🏗️ Architecture

The application follows clean MVC architecture:

- **Model**: JPA entities representing business data
- **View**: Swing components for user interface
- **Controller**: Service classes containing business logic
- **Configuration**: Database and application settings

## 🤝 Contributing

This is a professional commercial management system designed for real-world use. The architecture is modular and extensible, making it easy to add new features and modules.

## 📄 License

Professional Commercial Management System - All rights reserved.

## 📞 Support

For questions or support regarding this application, please refer to the documentation or contact the development team.

---

**Version**: 1.0.0  
**Last Updated**: July 2025  
**Status**: Phase 1 Complete - Ready for Phase 2 Implementation
