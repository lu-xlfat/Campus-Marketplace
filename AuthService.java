
package campusmarketplace.service;

import campusmarketplace.dao.UserDAO;
import campusmarketplace.model.User;
import campusmarketplace.util.PasswordUtil;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public User register(
        String fullName,
        String studentId,
        String email,
        String department,
        String academicSession,
        char[] password) throws SQLException {

        if (fullName == null || fullName.isBlank()
                || fullName.trim().length() > 100) {
            throw new IllegalArgumentException("Enter a valid full name.");
        }

        if (studentId == null || studentId.isBlank()
                || studentId.trim().length() > 30) {
            throw new IllegalArgumentException("Enter a valid student ID.");
        }

        if (email == null || !email.trim().matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
                || email.trim().length() > 100) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }

        if (department != null && department.trim().length() > 100) {
            throw new IllegalArgumentException("Department name is too long.");
        }

        if (password == null || password.length < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters.");
        }

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        
    // Keep all your existing registration validations above.

    // STEP 2: Validate Academic Session
    String cleanSession = null;

    if (academicSession != null
            && !academicSession.isBlank()) {

        cleanSession = academicSession.trim();

        if (!cleanSession.matches("\\d{4}-\\d{2}")) {
            throw new IllegalArgumentException(
                    "Academic Session must follow YYYY-YY, "
                    + "for example 2024-25.");
        }

        int startYear = Integer.parseInt(
                cleanSession.substring(0, 4));

        int endYear = Integer.parseInt(
                cleanSession.substring(5, 7));

        if (endYear != (startYear + 1) % 100) {
            throw new IllegalArgumentException(
                    "Academic Session must contain consecutive years.");
        }
    }

    // Your existing password hashing comes AFTER validation
    String passwordHash =
            PasswordUtil.hashPassword(password);

        

        
return userDAO.registerUser(
        fullName.trim(),
        studentId.trim(),
        normalizedEmail,
        department == null ? "" : department.trim(),
        cleanSession,
        passwordHash
);

    }

    public User login(String email, char[] password)
            throws SQLException {

        if (email == null || email.isBlank()
                || password == null || password.length == 0) {
            throw new IllegalArgumentException(
                    "Email and password are required.");
        }

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        String storedHash =
                userDAO.findPasswordHashByEmail(normalizedEmail);

        if (storedHash == null
                || !PasswordUtil.verifyPassword(password, storedHash)) {
            throw new IllegalArgumentException(
                    "Invalid email or password.");
        }

        return userDAO.findByEmail(normalizedEmail);
    }
    
    
// Compatibility with the existing registration screen
public User register(
        String fullName,
        String studentId,
        String email,
        String department,
        char[] password) throws SQLException {

    return register(
            fullName,
            studentId,
            email,
            department,
            null,
            password
    );
}

}
