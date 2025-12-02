# DAO (Data Access Object) Package

This package contains all Data Access Object classes for database operations in the SaintAngelo Hospital Queue Management System.

## Overview

DAO classes provide a clean separation between business logic and database operations. Each DAO class handles CRUD (Create, Read, Update, Delete) operations for a specific entity.

## Structure

### Base Class
- **`BaseDAO.java`** - Base class providing common database operations and connection management

### DAO Classes

1. **`UserDAO.java`** - User management
    - Authentication
    - User CRUD operations
    - Find by username, role, etc.

2. **`PatientDAO.java`** - Patient management
    - Patient CRUD operations
    - Search by name, phone number
    - Update last visit date

3. **`DoctorDAO.java`** - Doctor management
    - Doctor CRUD operations
    - Find by department, user ID
    - Get active doctors

4. **`TicketDAO.java`** - Queue ticket management
    - Ticket CRUD operations
    - Find active tickets
    - Update ticket status
    - Assign doctors to tickets

5. **`AppointmentDAO.java`** - Appointment management
    - Appointment CRUD operations
    - Find by date, doctor, patient
    - Update appointment status

6. **`PrescriptionDAO.java`** - Prescription management
    - Prescription CRUD operations
    - Find by patient

7. **`DischargeDAO.java`** - Discharge management
    - Discharge CRUD operations
    - Find by status
    - Update discharge status

8. **`ActivityLogDAO.java`** - Activity logging
    - Create activity logs
    - Find by user, type

## Usage Example

```java
import com.stangelo.saintangelo.dao.UserDAO;
import com.stangelo.saintangelo.models.User;

// Authenticate user
UserDAO userDAO = new UserDAO();
User user = userDAO.authenticate("admin", "password");

if (user != null) {
    System.out.println("Login successful: " + user.getFullName());
}

// Find all patients
PatientDAO patientDAO = new PatientDAO();
List<Patient> patients = patientDAO.findAll();

// Create a new ticket
TicketDAO ticketDAO = new TicketDAO();
Ticket ticket = new Ticket(visitId, ticketNumber, patient, TicketStatus.WAITING, LocalDateTime.now(), "Consultation");
boolean success = ticketDAO.create(ticket);
```

## Best Practices

1. **Always use try-with-resources** - DAOs handle connections internally, but ensure proper resource cleanup
2. **Handle null returns** - DAO methods return `null` when records are not found
3. **Check return values** - Create/Update/Delete methods return `boolean` indicating success
4. **Use transactions** - For complex operations involving multiple tables, consider using transactions
5. **Error handling** - All SQL exceptions are logged; handle them appropriately in your service layer

## Notes

- All DAO classes extend `BaseDAO` for common functionality
- Connections are managed through `DatabaseConnection` utility
- All methods handle SQL exceptions internally and log errors
- DAOs use PreparedStatements to prevent SQL injection
- ResultSet mapping is handled internally in each DAO

## Future Enhancements

- Connection pooling for better performance
- Transaction management utilities
- Batch operations for bulk inserts/updates
- Query builder for complex queries
- Caching layer for frequently accessed data

