
package campusmarketplace.service;

import campusmarketplace.model.User;
import campusmarketplace.util.PasswordUtil;
import campusmarketplace.dao.UserDAO;

import java.util.Arrays;
import java.sql.SQLException;

public class ProfileService {

    private final UserDAO userDAO = new UserDAO();

    // =====================================================
    // 1. UPDATE PROFILE - ORIGINAL METHOD
    // =====================================================

    public boolean updateProfile(
            int userId,
            String fullName,
            String department) throws SQLException {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid student account.");
        }

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException(
                    "Full name cannot be empty.");
        }

        String cleanName = fullName.trim();

        if (cleanName.length() > 100) {
            throw new IllegalArgumentException(
                    "Full name cannot exceed 100 characters.");
        }

        String cleanDepartment =
                department == null ? "" : department.trim();

        if (cleanDepartment.length() > 100) {
            throw new IllegalArgumentException(
                    "Department cannot exceed 100 characters.");
        }

        // Original method remains compatible
        return userDAO.updateProfile(
                userId,
                cleanName,
                cleanDepartment
        );
    }

    // =====================================================
    // 2. UPDATE PROFILE WITH OPTIONAL ACADEMIC SESSION
    // =====================================================

    public boolean updateProfile(
            int userId,
            String fullName,
            String department,
            String academicSession) throws SQLException {

        // Validate user ID
        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid student account.");
        }

        // Validate full name
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException(
                    "Full name cannot be empty.");
        }

        String cleanName = fullName.trim();

        if (cleanName.length() > 100) {
            throw new IllegalArgumentException(
                    "Full name cannot exceed 100 characters.");
        }

        // Validate department
        String cleanDepartment =
                department == null ? "" : department.trim();

        if (cleanDepartment.length() > 100) {
            throw new IllegalArgumentException(
                    "Department cannot exceed 100 characters.");
        }

        // Academic Session is OPTIONAL
        String cleanSession =
                academicSession == null
                        ? null
                        : academicSession.trim();

        // Empty session means no session recorded
        if (cleanSession != null
                && cleanSession.isEmpty()) {
            cleanSession = null;
        }

        // Validate session only when provided
        if (cleanSession != null) {

            if (!cleanSession.matches("\\d{4}-\\d{2}")) {
                throw new IllegalArgumentException(
                        "Use format: 2024-25");
            }

            int startYear = Integer.parseInt(
                    cleanSession.substring(0, 4));

            int endYear = Integer.parseInt(
                    cleanSession.substring(5, 7));

            if (endYear != (startYear + 1) % 100) {
                throw new IllegalArgumentException(
                        "Use consecutive years, e.g. 2024-25");
            }
        }

        // Save all three fields together in MySQL
        return userDAO.updateProfile(
                userId,
                cleanName,
                cleanDepartment,
                cleanSession
        );
    }

    // =====================================================
    // 3. CHANGE PASSWORD
    // =====================================================

    public boolean changePassword(
            User currentUser,
            char[] currentPassword,
            char[] newPassword,
            char[] confirmPassword) throws SQLException {

        // Check logged-in account
        if (currentUser == null ||
                currentUser.getUserId() <= 0) {

            throw new IllegalArgumentException(
                    "Invalid student account.");
        }

        // All password fields are required
        if (currentPassword == null ||
                currentPassword.length == 0 ||
                newPassword == null ||
                newPassword.length == 0 ||
                confirmPassword == null ||
                confirmPassword.length == 0) {

            throw new IllegalArgumentException(
                    "Please fill in all password fields.");
        }

        // Minimum password length
        if (newPassword.length < 8) {
            throw new IllegalArgumentException(
                    "New password must be at least 8 characters.");
        }

        // Compare new password and confirmation
        if (!Arrays.equals(
                newPassword, confirmPassword)) {

            throw new IllegalArgumentException(
                    "New passwords do not match.");
        }

        // Confirm the database account matches
        User databaseUser =
                userDAO.findByEmail(currentUser.getEmail());

        if (databaseUser == null ||
                databaseUser.getUserId()
                        != currentUser.getUserId()) {

            throw new IllegalArgumentException(
                    "Student account not found.");
        }

        // Retrieve existing password hash
        String storedHash =
                userDAO.findPasswordHashByEmail(
                        currentUser.getEmail());

        // Verify the current password
        if (storedHash == null ||
                !PasswordUtil.verifyPassword(
                        currentPassword, storedHash)) {

            throw new IllegalArgumentException(
                    "Current password is incorrect.");
        }

        // Prevent reusing the current password
        if (PasswordUtil.verifyPassword(
                newPassword, storedHash)) {

            throw new IllegalArgumentException(
                    "New password must be different "
                    + "from your current password.");
        }

        // Generate a new salted password hash
        String newHash =
                PasswordUtil.hashPassword(newPassword);

        // Update only if the stored hash has not changed
        return userDAO.updatePasswordHash(
                currentUser.getUserId(),
                storedHash,
                newHash
        );
    }
}
