# Database Authentication Implementation Guide

This document explains how the database authentication system works in the SaintAngelo Hospital Queue Management System.

## Overview

The login process has been updated to authenticate users against the MySQL database instead of using hardcoded credentials. The system uses a layered architecture:

1. **LoginController** - Handles UI interactions
2. **UserDAO** - Performs database queries
3. **AuthService** - Manages user session
4. **DatabaseConnection** - Handles database connectivity

## Components

### 1. LoginController (`src/main/java/com/stangelo/saintangelo/controllers/LoginController.java`)

**Key Features:**
- Validates user input (username and password)
- Authenticates against database using `UserDAO`
- Sets user session via `AuthService`
- Routes to appropriate dashboard based on user role
- Handles database connection errors gracefully

**Login Flow:**
```
User enters credentials
    ↓
Validate input (not empty)
    ↓
Call UserDAO.authenticate()
    ↓
If authenticated:
    - Set user in AuthService
    - Load dashboard based on role
    - Close login window
If failed:
    - Show error message
    - Clear password field
```

### 2. UserDAO (`src/main/java/com/stangelo/saintangelo/dao/UserDAO.java`)

**Authentication Method:**
```java
public User authenticate(String username, String password)
```

**What it does:**
- Queries database for user with matching username and password
- Checks if user status is 'Active'
- Updates last_active timestamp on successful login
- Returns User object if authenticated, null otherwise

**SQL Query:**
```sql
SELECT * FROM users 
WHERE username = ? 
AND password = ? 
AND status = 'Active'
```

### 3. AuthService (`src/main/java/com/stangelo/saintangelo/services/AuthService.java`)

**Purpose:**
- Singleton service to manage current user session
- Provides global access to logged-in user information
- Stores user object throughout application lifecycle

**Key Methods:**
- `setCurrentUser(User user)` - Sets the logged-in user
- `getCurrentUser()` - Gets current user
- `isLoggedIn()` - Checks if user is logged in
- `isAdmin()` - Checks if current user is admin
- `logout()` - Clears current user session

### 4. DatabaseConnection (`src/main/java/com/stangelo/saintangelo/utils/DatabaseConnection.java`)

**Purpose:**
- Manages database connections
- Provides connection testing
- Handles connection retry logic

## User Role Mapping

The system maps user roles to dashboards as follows:

| User Role | Dashboard FXML | Dashboard Title |
|-----------|---------------|-----------------|
| STAFF | `/fxml/receptionist-dashboard-view.fxml` | Receptionist Dashboard |
| DOCTOR | `/fxml/doctor-dashboard-view.fxml` | Doctor Dashboard |
| ADMIN | `/fxml/admin-dashboard-view.fxml` | Admin Dashboard |
| SUPER_ADMIN | `/fxml/admin-dashboard-view.fxml` | Admin Dashboard |

## Database Requirements

### Users Table Structure
The authentication requires the following fields in the `users` table:
- `user_id` (VARCHAR) - Primary key
- `username` (VARCHAR) - Unique, for login
- `password` (VARCHAR) - Password (currently plain text, should be hashed in production)
- `full_name` (VARCHAR) - User's full name
- `role` (ENUM) - UserRole: SUPER_ADMIN, ADMIN, DOCTOR, STAFF
- `status` (ENUM) - 'Active' or 'Inactive'
- `last_active` (DATETIME) - Last login timestamp

### Sample Users

The database schema includes sample users:
- **admin** / password - ADMIN role
- **doctor** / password - DOCTOR role
- **reception** / password - STAFF role

## Usage Example

### In LoginController
```java
// User enters credentials and clicks login
User authenticatedUser = userDAO.authenticate(username, password);

if (authenticatedUser != null) {
    authService.setCurrentUser(authenticatedUser);
    // Load dashboard...
}
```

### Accessing Current User in Other Controllers
```java
import com.stangelo.saintangelo.services.AuthService;

public class SomeController {
    private AuthService authService = AuthService.getInstance();
    
    public void someMethod() {
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            String userName = currentUser.getFullName();
            UserRole role = currentUser.getRole();
        }
    }
}
```

### Logout Implementation
```java
// In logout handler
authService.logout();
// Then navigate to login screen
```

## Security Considerations

### Current Implementation
- ✅ Passwords stored in database
- ✅ User status checking (Active/Inactive)
- ✅ Role-based access control
- ✅ Session management
- ⚠️ **Passwords are stored in plain text** (NOT SECURE for production)

### Production Recommendations

1. **Password Hashing**
    - Use BCrypt or Argon2 for password hashing
    - Never store plain text passwords
    - Example:
   ```java
   // Hash password before storing
   String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
   
   // Verify password
   boolean matches = BCrypt.checkpw(inputPassword, storedHash);
   ```

2. **Password Policy**
    - Enforce minimum password length
    - Require special characters
    - Implement password expiration

3. **Session Security**
    - Implement session timeout
    - Use secure session tokens
    - Log all login attempts

4. **Database Security**
    - Use parameterized queries (already implemented)
    - Limit database user permissions
    - Enable SSL for database connections

## Error Handling

The system handles various error scenarios:

1. **Database Connection Failure**
    - Shows alert on login screen initialization
    - Provides troubleshooting steps

2. **Invalid Credentials**
    - Shows "Invalid username or password" message
    - Clears password field

3. **Inactive User**
    - UserDAO only returns active users
    - Inactive users cannot login

4. **Missing Dashboard**
    - Shows error if FXML file not found
    - Logs error for debugging

## Testing

### Test Login Credentials
Use the sample data from the database schema:

1. **Admin Login:**
    - Username: `admin`
    - Password: `password`

2. **Doctor Login:**
    - Username: `doctor`
    - Password: `password`

3. **Receptionist Login:**
    - Username: `reception`
    - Password: `password`

### Testing Steps

1. **Start Database:**
    - Ensure XAMPP MySQL is running
    - Verify database `saintangelo_hospital` exists
    - Check sample users are inserted

2. **Test Login:**
    - Enter valid credentials
    - Should navigate to appropriate dashboard
    - Check that user name appears in dashboard title

3. **Test Invalid Login:**
    - Enter wrong password
    - Should show error message
    - Password field should clear

4. **Test Database Connection:**
    - Stop MySQL service
    - Launch application
    - Should show connection error on login screen

## Troubleshooting

### "Cannot connect to database" Error
- Check XAMPP MySQL is running
- Verify database exists: `saintangelo_hospital`
- Check connection settings in `database.properties`
- Verify MySQL port (default: 3306)

### "Invalid username or password" Error
- Verify user exists in database
- Check user status is 'Active'
- Verify password matches (case-sensitive)
- Check database connection is working

### Dashboard Not Loading
- Verify FXML file exists in `/src/main/resources/fxml/`
- Check file name matches exactly (case-sensitive)
- Verify user role is mapped correctly

## Future Enhancements

1. **Password Hashing** - Implement BCrypt
2. **Remember Me** - Optional feature
3. **Password Reset** - Forgot password functionality
4. **Two-Factor Authentication** - Additional security layer
5. **Login Attempts Tracking** - Prevent brute force attacks
6. **Session Timeout** - Auto-logout after inactivity

