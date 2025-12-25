// Represents a question paper with subject, year, and file info

public class QuestionPaper {
    private int id;
    private String subject;
    private String academicYear;  // e.g., "2nd Year", "3rd Year"
    private String examMonth;      // allowed: "May" or "December"
    private int year;
    private int semester;
    private String filePath;
    private String status;

    // Constructor without ID (for inserting)
    public QuestionPaper(String subject, String academicYear, String examMonth, int year, int semester, String filePath, String status) {
        this.subject = subject;
        this.academicYear = academicYear;
        this.examMonth = examMonth;
        this.year = year;
        this.semester = semester;
        this.filePath = filePath;
        this.status = status;
    }

    // Constructor with ID (for retrieving)
    public QuestionPaper(int id, String subject, String academicYear, String examMonth, int year, int semester, String filePath, String status) {
        this.id = id;
        this.subject = subject;
        this.academicYear = academicYear;
        this.examMonth = examMonth;
        this.year = year;
        this.semester = semester;
        this.filePath = filePath;
        this.status = status;
    }

    // Getters & Setters
    public int getId() { return id; }
    public String getSubject() { return subject; }
    public String getAcademicYear() { return academicYear; }
    public String getExamMonth() { return examMonth; }
    public int getYear() { return year; }
    public int getSemester() { return semester; }
    public String getFilePath() { return filePath; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return id + " | " + subject + " | " + academicYear + " | " + examMonth + " | " + year + " | Sem " + semester + " | " + status + " | " + filePath;
    }
}
