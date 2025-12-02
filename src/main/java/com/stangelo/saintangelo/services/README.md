# Services Package

This package contains service classes that provide business logic and application-wide functionality.

## Services

### AuthService
**Location:** `AuthService.java`

Manages user authentication and session management throughout the application.

**Features:**
- Singleton pattern for global access
- Stores current logged-in user
- Provides session state checking
- Role-based access checking

**Usage:**
```java
// Get the service instance
AuthService authService = AuthService.getInstance();

// Check if user is logged in
if (authService.isLoggedIn()) {
    User currentUser = authService.getCurrentUser();
    System.out.println("Logged in as: " + currentUser.getFullName());
}

// Check if user is admin
if (authService.isAdmin()) {
    // Admin-only operations
}

// Logout
authService.logout();
```

**Accessing Current User in Controllers:**
```java
import com.stangelo.saintangelo.services.AuthService;

public class SomeController {
    private AuthService authService = AuthService.getInstance();
    
    public void someMethod() {
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            // Use current user information
            String userName = currentUser.getFullName();
            UserRole role = currentUser.getRole();
        }
    }
}
```

## Integration with Login

The `AuthService` is automatically populated when a user successfully logs in through `LoginController`. The service maintains the user session until logout is called.

