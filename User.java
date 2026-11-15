
package campusmarketplace.model;

public class User {

    private int userId;
    private String fullName;
    private String studentId;
    private String email;
    private String department;
    
    private String academicSession;


    public User(int userId, String fullName,
                String studentId, String email,
                String department) {
        this.userId = userId;
        this.fullName = fullName;
        this.studentId = studentId;
        this.email = email;
        this.department = department;
    }

    public int getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartment() {
        return department;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

public String getAcademicSession() {
    return academicSession;
}

public void setAcademicSession(String academicSession) {
    this.academicSession = academicSession;
}

}
