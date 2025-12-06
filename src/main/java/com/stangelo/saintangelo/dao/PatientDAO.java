package com.stangelo.saintangelo.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.stangelo.saintangelo.models.Patient;

/**
 * Data Access Object for Patient entity
 * Handles all database operations related to patients
 *
 * @author SaintAngelo Development Team
 * @version 1.0
 */
public class PatientDAO extends BaseDAO {

    /**
     * Finds a patient by ID
     *
     * @param patientId Patient ID
     * @return Patient object if found, null otherwise
     */
    public Patient findById(String patientId) {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPatient(rs);
                }
            }
        } catch (SQLException e) {
            logError("Error finding patient by ID: " + patientId, e);
        }
        return null;
    }

    /**
     * Finds patients by name (first name or last name)
     *
     * @param name Name to search for
     * @return List of matching patients
     */
    public List<Patient> findByName(String name) {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE first_name LIKE ? OR last_name LIKE ? ORDER BY last_name, first_name";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + name + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            logError("Error finding patients by name: " + name, e);
        }
        return patients;
    }

    /**
     * Finds a patient by phone number
     *
     * @param phoneNumber Phone number
     * @return Patient object if found, null otherwise
     */
    public Patient findByPhoneNumber(String phoneNumber) {
        String sql = "SELECT * FROM patients WHERE phone_number = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, phoneNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPatient(rs);
                }
            }
        } catch (SQLException e) {
            logError("Error finding patient by phone: " + phoneNumber, e);
        }
        return null;
    }

    /**
     * Gets all patients
     *
     * @return List of all patients
     */
    public List<Patient> findAll() {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients ORDER BY last_name, first_name";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                patients.add(mapResultSetToPatient(rs));
            }
        } catch (SQLException e) {
            logError("Error finding all patients", e);
        }
        return patients;
    }

    /**
     * Creates a new patient
     *
     * @param patient Patient object to create
     * @return true if successful, false otherwise
     */
    public boolean create(Patient patient) {
        String sql = "INSERT INTO patients (patient_id, first_name, last_name, age, phone_number, gender, " +
                "home_address, chief_complaint, priority, emergency_contact_person, emergency_contact_number, " +
                "is_senior_citizen, blood_type, registration_date, last_visit_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patient.getId());
            stmt.setString(2, extractFirstName(patient.getName()));
            stmt.setString(3, extractLastName(patient.getName()));
            stmt.setInt(4, patient.getAge());
            stmt.setString(5, patient.getContactNumber());
            stmt.setString(6, patient.getGender());
            stmt.setString(7, patient.getHomeAddress());
            // Note: Patient model needs getChiefComplaint method
            // Using notes field as temporary storage
            stmt.setString(8, patient.getNotes());
            stmt.setString(9, mapPriorityToString(patient));
            stmt.setString(10, patient.getEmergencycontactPerson());
            stmt.setString(11, patient.getEmergencycontactNumber());
            stmt.setBoolean(12, patient.isSeniorCitizen());
            stmt.setString(13, patient.getBloodType());
            stmt.setDate(14, Date.valueOf(LocalDate.now()));
            stmt.setDate(15, patient.getLastVisitDate() != null ?
                    Date.valueOf(LocalDate.parse(patient.getLastVisitDate())) : null);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error creating patient: " + patient.getId(), e);
            return false;
        }
    }

    /**
     * Updates an existing patient
     *
     * @param patient Patient object with updated information
     * @return true if successful, false otherwise
     */
    public boolean update(Patient patient) {
        String sql = "UPDATE patients SET first_name = ?, last_name = ?, age = ?, phone_number = ?, gender = ?, " +
                "home_address = ?, chief_complaint = ?, priority = ?, emergency_contact_person = ?, " +
                "emergency_contact_number = ?, is_senior_citizen = ?, blood_type = ?, last_visit_date = ? " +
                "WHERE patient_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, extractFirstName(patient.getName()));
            stmt.setString(2, extractLastName(patient.getName()));
            stmt.setInt(3, patient.getAge());
            stmt.setString(4, patient.getContactNumber());
            stmt.setString(5, patient.getGender());
            stmt.setString(6, patient.getHomeAddress());
            // Note: Patient model needs getChiefComplaint method
            stmt.setString(7, patient.getNotes());
            stmt.setString(8, mapPriorityToString(patient));
            stmt.setString(9, patient.getEmergencycontactPerson());
            stmt.setString(10, patient.getEmergencycontactNumber());
            stmt.setBoolean(11, patient.isSeniorCitizen());
            stmt.setString(12, patient.getBloodType());
            stmt.setDate(13, patient.getLastVisitDate() != null ?
                    Date.valueOf(LocalDate.parse(patient.getLastVisitDate())) : null);
            stmt.setString(14, patient.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error updating patient: " + patient.getId(), e);
            return false;
        }
    }

    /**
     * Updates patient's last visit date
     *
     * @param patientId Patient ID
     * @param visitDate Visit date
     * @return true if successful, false otherwise
     */
    public boolean updateLastVisitDate(String patientId, LocalDate visitDate) {
        String sql = "UPDATE patients SET last_visit_date = ? WHERE patient_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(visitDate));
            stmt.setString(2, patientId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logError("Error updating last visit date for patient: " + patientId, e);
            return false;
        }
    }

    /**
     * Maps a ResultSet row to a Patient object
     */
    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        String id = rs.getString("patient_id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String name = firstName + " " + lastName;
        int age = rs.getInt("age");
        String phoneNumber = rs.getString("phone_number");
        String gender = rs.getString("gender");
        String homeAddress = rs.getString("home_address");
        String chiefComplaint = rs.getString("chief_complaint");
        String emergencyContactPerson = rs.getString("emergency_contact_person");
        String emergencyContactNumber = rs.getString("emergency_contact_number");
        boolean isSeniorCitizen = rs.getBoolean("is_senior_citizen");
        String bloodType = rs.getString("blood_type");

        Date lastVisitDate = rs.getDate("last_visit_date");
        String lastVisitDateStr = lastVisitDate != null ? lastVisitDate.toString() : null;

        Date registrationDate = rs.getDate("registration_date");
        LocalDate regDate = registrationDate != null ? registrationDate.toLocalDate() : null;

        Patient patient = new Patient(id, name, age, phoneNumber, homeAddress, gender,
                emergencyContactPerson, emergencyContactNumber, isSeniorCitizen,
                null, null, null, null, null, null, null, null, null, null, null, lastVisitDateStr, bloodType);
        patient.setRegistrationDate(regDate);

        // Note: Patient model needs setChiefComplaint method
        // For now, we'll store it in notes or another field
        if (chiefComplaint != null) {
            patient.setNotes(chiefComplaint);
        }

        return patient;
    }

    /**
     * Helper method to extract first name from full name
     */
    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 0 ? parts[0] : "";
    }

    /**
     * Helper method to extract last name from full name
     */
    private String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length > 1) {
            return parts[parts.length - 1];
        }
        return "";
    }

    /**
     * Helper method to map priority from Patient to database string
     */
    private String mapPriorityToString(Patient patient) {
        if (patient.isSeniorCitizen()) {
            return "SENIOR_CITIZEN";
        }
        return "REGULAR";
    }

    /**
     * Gets patient's chief complaint from database
     */
    public String getChiefComplaint(String patientId) {
        String sql = "SELECT chief_complaint FROM patients WHERE patient_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("chief_complaint");
                }
            }
        } catch (SQLException e) {
            logError("Error getting chief complaint for patient: " + patientId, e);
        }
        return null;
    }

    /**
     * Gets total number of patients in the system.
     *
     * @return total patient count
     */
    public int countAllPatients() {
        String sql = "SELECT COUNT(*) FROM patients";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logError("Error counting all patients", e);
        }

        return 0;
    }

    /**
     * Gets daily patient registration counts for the last N days.
     *
     * @param days The number of days to look back.
     * @return A map where the key is the date and the value is the count of new patients.
     */
    public Map<LocalDate, Integer> getDailyPatientCounts(int days) {
        Map<LocalDate, Integer> patientCounts = new LinkedHashMap<>();
        String sql = "SELECT DATE(registration_date) as date, COUNT(*) as count " +
                     "FROM patients " +
                     "WHERE registration_date >= CURDATE() - INTERVAL ? DAY " +
                     "GROUP BY DATE(registration_date) " +
                     "ORDER BY DATE(registration_date)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, days - 1);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("date").toLocalDate();
                    int count = rs.getInt("count");
                    patientCounts.put(date, count);
                }
            }
        } catch (SQLException e) {
            logError("Error getting daily patient counts", e);
        }

        // Fill in missing days with 0
        for (int i = 0; i < days; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            patientCounts.putIfAbsent(date, 0);
        }

        return patientCounts;
    }

    /**
     * Gets the total number of patients created in a given period.
     *
     * @param start The start date of the period.
     * @param end The end date of the period.
     * @return The total number of patients created.
     */
    public int getPatientCountInPeriod(LocalDate start, LocalDate end) {
        String sql = "SELECT COUNT(*) FROM patients WHERE registration_date BETWEEN ? AND ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, start);
            stmt.setObject(2, end);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logError("Error getting patient count in period", e);
        }
        return 0;
    }

    /**
     * Represents a patient record with consultation count and status for the records view
     */
    public static class PatientRecord {
        private Patient patient;
        private int consultationCount;
        private String status;
        private LocalDate lastVisitDate;

        public PatientRecord(Patient patient, int consultationCount, String status, LocalDate lastVisitDate) {
            this.patient = patient;
            this.consultationCount = consultationCount;
            this.status = status;
            this.lastVisitDate = lastVisitDate;
        }

        public Patient getPatient() { return patient; }
        public int getConsultationCount() { return consultationCount; }
        public String getStatus() { return status; }
        public LocalDate getLastVisitDate() { return lastVisitDate; }
    }

    /**
     * Gets patient records with consultation count and status for the records view
     * 
     * @param searchTerm Search term for patient ID, name, or complaint (null for all)
     * @param statusFilter Status filter: "All", "Active", "Under Treatment", "Discharged" (null for all)
     * @param offset Offset for pagination
     * @param limit Limit for pagination
     * @return List of patient records
     */
    public List<PatientRecord> getPatientRecords(String searchTerm, String statusFilter, int offset, int limit) {
        List<PatientRecord> records = new ArrayList<>();
        
        // Build the query with subquery for status calculation
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.patient_id, p.first_name, p.last_name, p.age, p.phone_number, ");
        sql.append("p.chief_complaint, p.last_visit_date, ");
        sql.append("COALESCE(COUNT(DISTINCT pr.prescription_id), 0) as consultation_count, ");
        sql.append("CASE ");
        sql.append("WHEN EXISTS (SELECT 1 FROM discharges d WHERE d.patient_id = p.patient_id AND d.status IN ('DISCHARGED', 'CLEARED')) THEN 'Discharged' ");
        sql.append("WHEN EXISTS (SELECT 1 FROM tickets t WHERE t.patient_id = p.patient_id AND t.status = 'IN_SERVICE') THEN 'Under Treatment' ");
        sql.append("ELSE 'Active' ");
        sql.append("END as patient_status ");
        sql.append("FROM patients p ");
        sql.append("LEFT JOIN prescriptions pr ON p.patient_id = pr.patient_id ");
        sql.append("WHERE 1=1 ");
        
        List<Object> params = new ArrayList<>();
        
        // Add search filter
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append("AND (p.patient_id LIKE ? OR p.first_name LIKE ? OR p.last_name LIKE ? ");
            sql.append("OR CONCAT(p.first_name, ' ', p.last_name) LIKE ? OR p.chief_complaint LIKE ?) ");
            String searchPattern = "%" + searchTerm.trim() + "%";
            for (int i = 0; i < 5; i++) {
                params.add(searchPattern);
            }
        }
        
        sql.append("GROUP BY p.patient_id, p.first_name, p.last_name, p.age, p.phone_number, ");
        sql.append("p.chief_complaint, p.last_visit_date ");
        
        // Add status filter using HAVING
        if (statusFilter != null && !statusFilter.equals("All") && !statusFilter.isEmpty()) {
            sql.append("HAVING patient_status = ? ");
            params.add(statusFilter);
        }
        
        sql.append("ORDER BY COALESCE(p.last_visit_date, '1900-01-01') DESC, p.patient_id DESC ");
        sql.append("LIMIT ? OFFSET ? ");
        params.add(limit);
        params.add(offset);
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            for (Object param : params) {
                if (param instanceof String) {
                    stmt.setString(paramIndex, (String) param);
                } else if (param instanceof Integer) {
                    stmt.setInt(paramIndex, (Integer) param);
                }
                paramIndex++;
            }
            
            // Debug: Print the SQL query
            System.out.println("Executing SQL: " + sql.toString());
            System.out.println("Parameters: " + params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                    String patientId = rs.getString("patient_id");
                    System.out.println("Found patient: " + patientId);
                    
                    // Construct Patient object directly from ResultSet to avoid closing it
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String name = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                    name = name.trim();
                    int age = rs.getInt("age");
                    String phoneNumber = rs.getString("phone_number");
                    String chiefComplaint = rs.getString("chief_complaint");
                    
                    // Create Patient object with available data
                    Patient patient = new Patient(
                        patientId,
                        name,
                        age,
                        phoneNumber,
                        null, // homeAddress - not in query
                        null, // gender - not in query
                        null, // emergencycontactPerson - not in query
                        null, // emergencycontactNumber - not in query
                        false, // isSeniorCitizen - not in query
                        null, // currentMedications
                        null, // allergies
                        null, // diagnosis
                        null, // treatmentPlan
                        chiefComplaint, // notes - using chief_complaint
                        null, // roomNumber
                        null, // admissionDate
                        null, // dischargeDate
                        null, // attendingPhysician
                        null, // status
                        null, // nextAppointmentDate
                        null, // lastVisitDate - will be set separately
                        null  // bloodType - not in query
                    );
                    
                    // Set last visit date if available
                    Date lastVisitDate = rs.getDate("last_visit_date");
                    if (lastVisitDate != null) {
                        patient.setLastVisitDate(lastVisitDate.toString());
                    }
                    
                    int consultationCount = rs.getInt("consultation_count");
                    String status = rs.getString("patient_status");
                    LocalDate lastVisit = lastVisitDate != null ? lastVisitDate.toLocalDate() : null;
                    
                    records.add(new PatientRecord(patient, consultationCount, status, lastVisit));
                }
                System.out.println("Total rows returned from query: " + rowCount);
                System.out.println("Records added to list: " + records.size());
            }
        } catch (SQLException e) {
            System.err.println("SQL Error getting patient records!");
            System.err.println("SQL: " + sql.toString());
            System.err.println("Parameters: " + params);
            logError("Error getting patient records. SQL: " + sql.toString(), e);
            e.printStackTrace(); // Print stack trace for debugging
        }
        
        return records;
    }

    /**
     * Gets total count of patient records matching the search and filter criteria
     * 
     * @param searchTerm Search term for patient ID, name, or complaint (null for all)
     * @param statusFilter Status filter: "All", "Active", "Under Treatment", "Discharged" (null for all)
     * @return Total count of matching records
     */
    public int getPatientRecordsCount(String searchTerm, String statusFilter) {
        // Use a subquery approach for counting
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ( ");
        sql.append("SELECT p.patient_id, ");
        sql.append("CASE ");
        sql.append("WHEN EXISTS (SELECT 1 FROM discharges d WHERE d.patient_id = p.patient_id AND d.status IN ('DISCHARGED', 'CLEARED')) THEN 'Discharged' ");
        sql.append("WHEN EXISTS (SELECT 1 FROM tickets t WHERE t.patient_id = p.patient_id AND t.status = 'IN_SERVICE') THEN 'Under Treatment' ");
        sql.append("ELSE 'Active' ");
        sql.append("END as patient_status ");
        sql.append("FROM patients p ");
        sql.append("WHERE 1=1 ");
        
        List<Object> params = new ArrayList<>();
        
        // Add search filter
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append("AND (p.patient_id LIKE ? OR p.first_name LIKE ? OR p.last_name LIKE ? ");
            sql.append("OR CONCAT(p.first_name, ' ', p.last_name) LIKE ? OR p.chief_complaint LIKE ?) ");
            String searchPattern = "%" + searchTerm.trim() + "%";
            for (int i = 0; i < 5; i++) {
                params.add(searchPattern);
            }
        }
        
        sql.append(") AS patient_statuses ");
        
        // Add status filter
        if (statusFilter != null && !statusFilter.equals("All") && !statusFilter.isEmpty()) {
            sql.append("WHERE patient_status = ? ");
            params.add(statusFilter);
        }
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            for (Object param : params) {
                stmt.setString(paramIndex, (String) param);
                paramIndex++;
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logError("Error getting patient records count", e);
        }
        
        return 0;
    }
}
