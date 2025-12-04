classDiagram
    class User {
        -String id
        -String username
        -String password
        -String fullName
        -String email
        -UserRole role
        -String permissions
        -String status
        -LocalDateTime lastActive
        +authenticate()
        +getRole()
        +getFullName()
        +getEmail()
    }

    class UserRole {
        <<enumeration>>
        SUPER_ADMIN
        ADMIN
        DOCTOR
        STAFF
    }

    class Patient {
        -String id
        -String name
        -int age
        -String contactNumber
        -String gender
        -PriorityLevel priority
        -boolean isSeniorCitizen
        -String bloodType
        -String homeAddress
        -String chiefComplaint
        +getName()
        +getPriority()
        +getAge()
    }

    class PriorityLevel {
        <<enumeration>>
        REGULAR
        SENIOR_CITIZEN
        EMERGENCY
    }

    class Ticket {
        -String visitId
        -String ticketNumber
        -TicketStatus status
        -PriorityLevel priority
        -LocalDateTime createdTime
        -LocalDateTime calledTime
        -Patient patient
        -Doctor assignedDoctor
        -String serviceType
        +getTicketNumber()
        +getStatus()
        +getPriority()
    }

    class TicketStatus {
        <<enumeration>>
        WAITING
        CALLED
        IN_SERVICE
        COMPLETED
        SKIPPED
    }

    class Doctor {
        -String doctorId
        -String name
        -String specialization
        -String department
        -String email
        -User user
        +getName()
        +getDepartment()
        +getSpecialization()
    }

    class Appointment {
        -String appointmentId
        -Patient patient
        -Doctor doctor
        -LocalDate appointmentDate
        -LocalTime appointmentTime
        -AppointmentStatus status
        -String purpose
        +getStatus()
        +getAppointmentDate()
    }

    class AppointmentStatus {
        <<enumeration>>
        SCHEDULED
        CONFIRMED
        COMPLETED
        CANCELLED
        NO_SHOW
    }

    class Prescription {
        -String prescriptionId
        -Patient patient
        -Doctor doctor
        -String medication
        -String dosage
        -String frequency
        -String diagnosis
        -String consultationNotes
        +getMedication()
        +getDosage()
    }

    class QueueManager {
        -PriorityQueue~Ticket~ waitingQueue
        -Ticket currentlyServing
        -TicketDAO ticketDAO
        +enqueue(Ticket) boolean
        +dequeue(String) Ticket
        +syncFromDatabase() void
        +peek() Ticket
        +size() int
    }

    class QueueService {
        +enqueue(Ticket) boolean
        +dequeue(String) Ticket
        +refreshQueueData() void
        +generateNextTicket(String) String
        +syncFromDatabase() void
    }

    class AuthService {
        -User currentUser
        +setCurrentUser(User) void
        +getCurrentUser() User
        +isLoggedIn() boolean
        +isAdmin() boolean
        +logout() void
    }

    class UserDAO {
        +authenticate(String, String) User
        +create(User) boolean
        +update(User) boolean
        +findByUsername(String) User
        +findAll() List~User~
    }

    class TicketDAO {
        +create(Ticket) boolean
        +findWaitingTickets(int) List~Ticket~
        +updateStatus(String, TicketStatus) boolean
        +assignDoctor(String, String) boolean
        +findByVisitId(String) Ticket
    }

    class PatientDAO {
        +create(Patient) boolean
        +update(Patient) boolean
        +findById(String) Patient
        +searchByName(String) List~Patient~
    }

    class DatabaseConnection {
        -Connection connection
        -static DatabaseConnection instance
        +getConnection() Connection
        +testConnection() boolean
        +resetConnection() boolean
        +closeConnection() void
    }

    %% Relationships
    User "1" --> "1" UserRole : has
    Ticket "1" --> "1" Patient : references
    Ticket "1" --> "0..1" Doctor : assigned to
    Ticket "1" --> "1" TicketStatus : has
    Ticket "1" --> "1" PriorityLevel : has
    Patient "1" --> "0..1" PriorityLevel : has
    Appointment "1" --> "1" Patient : for
    Appointment "1" --> "1" Doctor : with
    Appointment "1" --> "1" AppointmentStatus : has
    Prescription "1" --> "1" Patient : for
    Prescription "1" --> "1" Doctor : by
    Doctor "1" --> "0..1" User : linked to
    QueueManager "1" --> "*" Ticket : manages
    QueueService ..> QueueManager : uses
    UserDAO ..> User : manages
    TicketDAO ..> Ticket : manages
    PatientDAO ..> Patient : manages
    UserDAO ..> DatabaseConnection : uses
    TicketDAO ..> DatabaseConnection : uses
    PatientDAO ..> DatabaseConnection : uses