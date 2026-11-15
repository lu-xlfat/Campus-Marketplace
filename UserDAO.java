
package campusmarketplace.dao;

import campusmarketplace.model.SellerInfo;
import campusmarketplace.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {

    // =====================================================
    // 1. REGISTER USER - OLD METHOD
    // =====================================================

    public User registerUser(
            String fullName,
            String studentId,
            String email,
            String department,
            String passwordHash) throws SQLException {

        return registerUser(
                fullName,
                studentId,
                email,
                department,
                null,
                passwordHash
        );
    }

    // =====================================================
    // 2. REGISTER USER WITH OPTIONAL ACADEMIC SESSION
    // =====================================================

    public User registerUser(
            String fullName,
            String studentId,
            String email,
            String department,
            String academicSession,
            String passwordHash) throws SQLException {

        String session = normalizeAcademicSession(
                academicSession);

        String sql = """
                INSERT INTO users
                (full_name, student_id, email,
                 department, academic_session, password_hash)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, fullName);
            stmt.setString(2, studentId);
            stmt.setString(3, email);
            stmt.setString(4, department);
            stmt.setString(5, session);
            stmt.setString(6, passwordHash);

            int rows = stmt.executeUpdate();

            if (rows != 1) {
                throw new SQLException(
                        "Registration failed.");
            }

            try (ResultSet keys =
                         stmt.getGeneratedKeys()) {

                if (keys.next()) {

                    int userId = keys.getInt(1);

                    User user = new User(
                            userId,
                            fullName,
                            studentId,
                            email,
                            department
                    );

                    user.setAcademicSession(session);

                    return user;
                }

                throw new SQLException(
                        "Unable to retrieve the new user ID.");
            }
        }
    }

    // =====================================================
    // 3. FIND PASSWORD HASH BY EMAIL
    // =====================================================

    public String findPasswordHashByEmail(
            String email) throws SQLException {

        String sql = """
                SELECT password_hash
                FROM users
                WHERE email = ?
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs =
                         stmt.executeQuery()) {

                if (rs.next()) {
                    return rs.getString("password_hash");
                }

                return null;
            }
        }
    }

    // =====================================================
    // 4. FIND USER BY EMAIL
    // =====================================================

    public User findByEmail(
            String email) throws SQLException {

        String sql = """
                SELECT user_id, full_name, student_id,
                       email, department, academic_session
                FROM users
                WHERE email = ?
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs =
                         stmt.executeQuery()) {

                if (rs.next()) {

                    User user = new User(
                            rs.getInt("user_id"),
                            rs.getString("full_name"),
                            rs.getString("student_id"),
                            rs.getString("email"),
                            rs.getString("department")
                    );

                    user.setAcademicSession(
                            rs.getString("academic_session")
                    );

                    return user;
                }

                return null;
            }
        }
    }

    // =====================================================
    // 5. UPDATE PROFILE - ORIGINAL METHOD
    // =====================================================

    public boolean updateProfile(
            int userId,
            String fullName,
            String department) throws SQLException {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid user account.");
        }

        String sql = """
                UPDATE users
                SET full_name = ?,
                    department = ?
                WHERE user_id = ?
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setString(1, fullName);
            stmt.setString(2, department);
            stmt.setInt(3, userId);

            return stmt.executeUpdate() == 1;
        }
    }

    // =====================================================
    // 6. UPDATE PROFILE WITH OPTIONAL ACADEMIC SESSION
    // =====================================================

    public boolean updateProfile(
            int userId,
            String fullName,
            String department,
            String academicSession) throws SQLException {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid user account.");
        }

        // Blank Academic Session becomes NULL
        String session = normalizeAcademicSession(
                academicSession);

        String sql = """
                UPDATE users
                SET full_name = ?,
                    department = ?,
                    academic_session = ?
                WHERE user_id = ?
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setString(1, fullName);
            stmt.setString(2, department);
            stmt.setString(3, session);
            stmt.setInt(4, userId);

            return stmt.executeUpdate() == 1;
        }
    }

    // =====================================================
    // 7. UPDATE PASSWORD HASH
    // =====================================================

    public boolean updatePasswordHash(
            int userId,
            String oldPasswordHash,
            String newPasswordHash) throws SQLException {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid user account.");
        }

        if (oldPasswordHash == null
                || oldPasswordHash.isBlank()
                || newPasswordHash == null
                || newPasswordHash.isBlank()) {

            throw new IllegalArgumentException(
                    "Invalid password hash.");
        }

        String sql = """
                UPDATE users
                SET password_hash = ?
                WHERE user_id = ?
                  AND password_hash = ?
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);
            stmt.setString(3, oldPasswordHash);

            return stmt.executeUpdate() == 1;
        }
    }

    // =====================================================
    // 8. FIND SELLER INFORMATION
    // =====================================================

 
public SellerInfo findSellerInfoById(
        int sellerId) throws SQLException {

    if (sellerId <= 0) {
        throw new IllegalArgumentException(
                "Invalid seller ID.");
    }

    String sql = """
            SELECT full_name, department, academic_session
            FROM users
            WHERE user_id = ?
            """;

    try (Connection conn =
                 DatabaseConnection.getConnection();
         PreparedStatement stmt =
                 conn.prepareStatement(sql)) {

        stmt.setInt(1, sellerId);

        try (ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {

                return new SellerInfo(
                        rs.getString("full_name"),
                        rs.getString("department"),
                        rs.getString("academic_session")
                );
            }
        }
    }

    return null;
}


    // =====================================================
    // 9. UPDATE ACADEMIC SESSION ONLY
    // =====================================================

    public boolean updateAcademicSession(
            int userId,
            String academicSession) throws SQLException {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid student account.");
        }

        // Allow blank or NULL Academic Session
        String session = normalizeAcademicSession(
                academicSession);

        String sql = """
                UPDATE users
                SET academic_session = ?
                WHERE user_id = ?
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setString(1, session);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() == 1;
        }
    }

    // =====================================================
    // 10. FIND ACADEMIC SESSION BY USER ID
    // =====================================================

    public String findAcademicSessionByUserId(
            int userId) throws SQLException {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid student account.");
        }

        String sql = """
                SELECT academic_session
                FROM users
                WHERE user_id = ?
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs =
                         stmt.executeQuery()) {

                if (rs.next()) {
                    return rs.getString("academic_session");
                }
            }
        }

        return null;
    }

    // =====================================================
    // 11. VALIDATE OPTIONAL ACADEMIC SESSION
    // =====================================================

    private static String normalizeAcademicSession(
            String academicSession) {

        // Session is optional for every student
        if (academicSession == null
                || academicSession.isBlank()) {

            return null;
        }

        String session = academicSession.trim();

        // Check format: YYYY-YY
        if (!session.matches("\\d{4}-\\d{2}")) {

            throw new IllegalArgumentException(
                    "Use format: 2024-25");
        }

        // Check consecutive years
        int startYear = Integer.parseInt(
                session.substring(0, 4));

        int endYear = Integer.parseInt(
                session.substring(5, 7));

        if (endYear != (startYear + 1) % 100) {

            throw new IllegalArgumentException(
                    "Use consecutive years, e.g. 2024-25");
        }

        return session;
    }
}
