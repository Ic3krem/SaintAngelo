# Model Classes Required for SaintAngelo Hospital Queue Management System

Based on analysis of FXML files and controllers, here are the model classes you need to create:

## 1. **User** (Enhanced)
**Location:** `src/main/java/com/stangelo/saintangelo/models/User.java`
**Based on:** `admin-usermanage-view.fxml`, `LoginController.java`

### Fields Required:
- `String userId` - User ID (e.g., "U001", "U002")
- `String username` - Login username
- `String password` - Hashed password
- `String fullName` - Full name (e.g., "Dr. John Diaz", "Angelo Castro")
- `String email` - Email address (e.g., "john.diaz@stangelo.com")
- `UserRole role` - Role enum (ADMIN, DOCTOR, STAFF)
- `String permissions` - Permissions description (e.g., "Full Medical Access", "Registration & Queue")
- `String status` - Status (e.g., "Active", "Inactive")
- `LocalDateTime lastActive` - Last active timestamp

---

## 2. **Patient** (Enhanced)
**Location:** `src/main/java/com/stangelo/saintangelo/models/Patient.java`
**Based on:** `receptionist-registration-view.fxml`, `receptionist-queueManagement-view.fxml`, `doctor-dashboard-view.fxml`

### Fields Required:
- `String patientId` - Patient ID (e.g., "A145", "Q001")
- `String firstName` - First name
- `String lastName` - Last name
- `int age` - Age
- `String phoneNumber` - Contact number (e.g., "0912-345-6789")
- `String gender` - Gender
- `String homeAddress` - Home address
- `String chiefComplaint` - Reason for visit (e.g., "Fever", "Backburner", "Chest Pain")
- `PriorityLevel priority` - Priority enum (REGULAR, SENIOR_CITIZEN, EMERGENCY)
- `String emergencyContactPerson` - Emergency contact name
- `String emergencyContactNumber` - Emergency contact phone
- `boolean isSeniorCitizen` - Senior citizen flag
- `String bloodType` - Blood type
- `LocalDate registrationDate` - Date of registration
- `LocalDate lastVisitDate` - Last visit date

---

## 3. **Ticket/Queue** (Enhanced)
**Location:** `src/main/java/com/stangelo/saintangelo/models/Ticket.java`
**Based on:** `receptionist-queueManagement-view.fxml`, `QueueService.java`, `PublicViewController.java`

### Fields Required:
- `String visitId` - Visit ID (e.g., "Q001", "A145")
- `String ticketNumber` - Formatted ticket number (e.g., "A1", "B5")
- `Patient patient` - Reference to Patient object
- `TicketStatus status` - Status enum (WAITING, CALLED, IN_SERVICE, COMPLETED, SKIPPED)
- `PriorityLevel priority` - Priority level
- `LocalDateTime createdTime` - Ticket creation time
- `LocalDateTime calledTime` - When doctor called the patient
- `LocalDateTime completedTime` - When consultation completed
- `String serviceType` - Service type
- `int waitTimeMinutes` - Wait time in minutes
- `Doctor assignedDoctor` - Assigned doctor (optional)

---

## 4. **Appointment** (NEW - Required)
**Location:** `src/main/java/com/stangelo/saintangelo/models/Appointment.java`
**Based on:** `receptionist-appointments-view.fxml`

### Fields Required:
- `String appointmentId` - Appointment ID
- `Patient patient` - Reference to Patient
- `Doctor doctor` - Reference to Doctor
- `LocalDate appointmentDate` - Appointment date
- `LocalTime appointmentTime` - Appointment time (e.g., "10:00 AM")
- `String purpose` - Reason for appointment
- `AppointmentStatus status` - Status enum (SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW)
- `LocalDateTime createdAt` - When appointment was created
- `String notes` - Additional notes

---

## 5. **Doctor** (Enhanced)
**Location:** `src/main/java/com/stangelo/saintangelo/models/Doctor.java`
**Based on:** `admin-usermanage-view.fxml`, `receptionist-appointments-view.fxml`, `doctor-dashboard-view.fxml`

### Fields Required:
- `String doctorId` - Doctor ID (e.g., "U001")
- `String name` - Full name (e.g., "Dr. John Diaz")
- `String email` - Email address
- `String specialization` - Specialization/Department (e.g., "Cardiology", "General")
- `String department` - Department name
- `int buildingNumber` - Building number
- `String status` - Status (Active/Inactive)
- `User user` - Reference to User account (optional, for login)

---

## 6. **Prescription** (NEW - Required)
**Location:** `src/main/java/com/stangelo/saintangelo/models/Prescription.java`
**Based on:** `doctor-dashboard-view.fxml`, `receptionist-discharge-view.fxml`

### Fields Required:
- `String prescriptionId` - Prescription ID
- `Patient patient` - Reference to Patient
- `Doctor doctor` - Reference to Doctor
- `String medication` - Medication name
- `String dosage` - Dosage (e.g., "500 mg")
- `String frequency` - Frequency (e.g., "3x daily")
- `String consultationNotes` - Doctor's notes
- `LocalDateTime consultationDate` - Date of consultation
- `String diagnosis` - Diagnosis
- `String treatmentPlan` - Treatment plan

---

## 7. **Discharge** (NEW - Required)
**Location:** `src/main/java/com/stangelo/saintangelo/models/Discharge.java`
**Based on:** `receptionist-discharge-view.fxml`

### Fields Required:
- `String dischargeId` - Discharge ID
- `Patient patient` - Reference to Patient
- `String department` - Department (e.g., "Cardiology", "General", "Orthopedics")
- `DischargeStatus status` - Status enum (READY, PENDING_REVIEW, CLEARED, DISCHARGED)
- `LocalDateTime dischargeDate` - Discharge date
- `Prescription prescription` - Reference to Prescription
- `String billingAmount` - Billing amount (optional)
- `String notes` - Discharge notes

---

## 8. **ActivityLog** (NEW - Required)
**Location:** `src/main/java/com/stangelo/saintangelo/models/ActivityLog.java`
**Based on:** `admin-activity-view.fxml`

### Fields Required:
- `String logId` - Log ID
- `User user` - Reference to User who performed action
- `String action` - Action performed (e.g., "Login", "Create Patient", "Update Queue")
- `String details` - Action details
- `LocalDateTime timestamp` - When action occurred
- `String ipAddress` - IP address (optional)
- `ActivityType type` - Type enum (LOGIN, PATIENT, QUEUE, APPOINTMENT, DISCHARGE, USER_MANAGEMENT)

---

## 9. **Report** (NEW - Optional but Recommended)
**Location:** `src/main/java/com/stangelo/saintangelo/models/Report.java`
**Based on:** `admin-generaterReport-view.fxml`

### Fields Required:
- `String reportId` - Report ID
- `ReportType type` - Type enum (DAILY_STATISTICS, WEEKLY_STATISTICS, MONTHLY_STATISTICS, CUSTOM)
- `LocalDate startDate` - Start date
- `LocalDate endDate` - End date
- `ReportFormat format` - Format enum (PDF, EXCEL, CSV)
- `LocalDateTime generatedAt` - Generation timestamp
- `User generatedBy` - User who generated report
- `String filePath` - Path to generated report file

---

## 10. **Enums Required**

### PriorityLevel (NEW)
**Location:** `src/main/java/com/stangelo/saintangelo/models/PriorityLevel.java`
```java
public enum PriorityLevel {
    REGULAR,
    SENIOR_CITIZEN,
    EMERGENCY
}
```

### AppointmentStatus (NEW)
**Location:** `src/main/java/com/stangelo/saintangelo/models/AppointmentStatus.java`
```java
public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
```

### DischargeStatus (NEW)
**Location:** `src/main/java/com/stangelo/saintangelo/models/DischargeStatus.java`
```java
public enum DischargeStatus {
    READY,
    PENDING_REVIEW,
    CLEARED,
    DISCHARGED
}
```

### ActivityType (NEW)
**Location:** `src/main/java/com/stangelo/saintangelo/models/ActivityType.java`
```java
public enum ActivityType {
    LOGIN,
    PATIENT,
    QUEUE,
    APPOINTMENT,
    DISCHARGE,
    USER_MANAGEMENT,
    REPORT
}
```

### ReportType (NEW)
**Location:** `src/main/java/com/stangelo/saintangelo/models/ReportType.java`
```java
public enum ReportType {
    DAILY_STATISTICS,
    WEEKLY_STATISTICS,
    MONTHLY_STATISTICS,
    CUSTOM
}
```

### ReportFormat (NEW)
**Location:** `src/main/java/com/stangelo/saintangelo/models/ReportFormat.java`
```java
public enum ReportFormat {
    PDF,
    EXCEL,
    CSV
}
```

---

## Summary

### Models to Create/Enhance:
1. ✅ **User** - Enhance existing (add email, permissions, status, lastActive)
2. ✅ **Patient** - Enhance existing (add firstName, lastName, chiefComplaint, priority)
3. ✅ **Ticket** - Enhance existing (add priority, waitTime, assignedDoctor)
4. ⭐ **Appointment** - CREATE NEW
5. ✅ **Doctor** - Enhance existing (add email, department, status)
6. ⭐ **Prescription** - CREATE NEW
7. ⭐ **Discharge** - CREATE NEW
8. ⭐ **ActivityLog** - CREATE NEW
9. ⭐ **Report** - CREATE NEW (optional)

### Enums to Create:
1. ⭐ **PriorityLevel** - CREATE NEW
2. ⭐ **AppointmentStatus** - CREATE NEW
3. ⭐ **DischargeStatus** - CREATE NEW
4. ⭐ **ActivityType** - CREATE NEW
5. ⭐ **ReportType** - CREATE NEW
6. ⭐ **ReportFormat** - CREATE NEW

### Existing Enums (Keep):
- ✅ **UserRole** - Already exists
- ✅ **TicketStatus** - Already exists

---

## Notes:
- All date/time fields should use `LocalDate`, `LocalTime`, or `LocalDateTime` from `java.time`
- Use proper relationships between models (e.g., Patient reference in Ticket, Doctor reference in Appointment)
- Consider adding validation annotations if using a validation framework
- All IDs should be String type for flexibility (can be auto-generated or manual)

