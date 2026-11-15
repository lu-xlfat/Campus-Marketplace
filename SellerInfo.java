
package campusmarketplace.model;

public class SellerInfo {

    private final String fullName;
    private final String department;
    private final String academicSession;

    // Existing constructor - kept for compatibility
    public SellerInfo(
            String fullName,
            String department) {

        this(fullName, department, null);
    }

    // New constructor with Academic Session
    public SellerInfo(
            String fullName,
            String department,
            String academicSession) {

        this.fullName = fullName;
        this.department = department;
        this.academicSession = academicSession;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDepartment() {
        return department;
    }

    public String getAcademicSession() {
        return academicSession;
    }
}
