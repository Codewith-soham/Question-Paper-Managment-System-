# Question Paper Management System - Deployment Guide

## 🎯 Overview
This is a deployment-ready Question Paper Management System for college librarians to manage and distribute question papers efficiently. The system now includes Academic Year and Exam Month fields for better organization.

## 📋 Prerequisites

### Required Software
1. **Java Development Kit (JDK) 11 or higher**
   - Download from: https://www.oracle.com/java/technologies/downloads/
   - Verify installation: `java -version`

2. **MySQL Server 8.0 or higher**
   - Download from: https://dev.mysql.com/downloads/mysql/
   - Default credentials will be needed during setup

3. **Web Browser**
   - Google Chrome, Firefox, Edge, or Safari (latest version)

### Required Libraries
The following JAR files are needed in the `lib/` folder:
- `mysql-connector-j-8.x.x.jar` (MySQL JDBC Driver)
- `jackson-databind-2.x.x.jar` (JSON processing)
- `jackson-core-2.x.x.jar` (JSON processing)
- `jackson-annotations-2.x.x.jar` (JSON processing)
- `javax.mail.jar` (Email functionality)
- `activation.jar` (Email functionality)

## 🚀 Step-by-Step Deployment

### Step 1: Database Setup

1. **Start MySQL Server**
   ```
   Windows: Start MySQL from Services or MySQL Workbench
   ```

2. **Create Database and User**
   - Open MySQL Workbench or MySQL Command Line
   - Run the setup script:
   ```sql
   mysql -u root -p < setup-database.sql
   ```
   
   Or manually execute the SQL commands in `setup-database.sql`

3. **Verify Database Creation**
   ```sql
   USE questionpaper;
   SHOW TABLES;
   DESCRIBE question_paper;
   ```

### Step 2: Configure Database Connection

1. Open `src\DatabaseConnection.java`
2. Update the following credentials if different from defaults:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/questionpaper";
   private static final String USER = "root";
   private static final String PASSWORD = "your_mysql_password";
   ```

### Step 3: Configure Email Service (Optional)

For email functionality to work:

1. **Set Environment Variables** (Windows):
   ```cmd
   setx SMTP_USER "your-email@gmail.com"
   setx SMTP_PASS "your-app-password"
   ```

2. **For Gmail Users**:
   - Enable 2-Factor Authentication
   - Generate App Password: https://myaccount.google.com/apppasswords
   - Use the app password (not your regular password)

3. **Restart Command Prompt** after setting environment variables

### Step 4: Compile the Application

1. Open Command Prompt in the project directory
2. Create the target directory if it doesn't exist:
   ```cmd
   mkdir target\classes
   ```

3. Compile all Java files:
   ```cmd
   javac -d target\classes -cp "lib\*" src\*.java
   ```

4. Verify compilation:
   ```cmd
   dir target\classes\*.class
   ```

### Step 5: Create PDF Directory

```cmd
mkdir PDF
```

This folder will store all uploaded question paper PDFs.

### Step 6: Start the Application

**Option 1: Using the batch file (Recommended)**
```cmd
start-server.bat
```

**Option 2: Manual start**
```cmd
java -cp "target\classes;lib\*" LaunchQPMS
```

The server will start on `http://localhost:8080`

### Step 7: Access the Application

1. Open your web browser
2. Navigate to: `http://localhost:8080/frontend/index.html`
3. You should see the Question Paper Management dashboard

## 📚 User Guide for Librarians

### Adding a Question Paper

1. Click "Add Paper" in the navigation menu
2. Fill in the form:
   - **Subject**: e.g., "Database Management System"
   - **Academic Year**: Select from dropdown (1st Year, 2nd Year, 3rd Year, 4th Year)
   - **Exam Month**: Select from dropdown (May or December)
   - **Year**: Calendar year (e.g., 2025)
   - **Semester**: Semester number (e.g., 3)
   - **File Name**: Name of PDF file (e.g., `dbms_jan_2025.pdf`)
   - **Status**: AVAILABLE or NOT AVAILABLE
3. Click "Add Paper"
4. Manually copy the PDF file to the `PDF` folder in the project directory

### Searching for Question Papers

1. Click "Search" in the navigation menu
2. Enter search criteria:
   - **Subject**: Enter the subject name
   - **Academic Year**: Select academic year
   - **Exam Month**: Select exam month
   - **Year**: Enter the calendar year
   - **Semester**: Enter semester number
3. Click "Search"
4. Results will display with options to:
   - View PDF (click filename)
   - Send via Email (click "Send to Email" button)

### Viewing All Papers

1. Click "Dashboard" or "View All" in the navigation
2. Scroll to "All Papers" section
3. Click "Refresh" to reload the list
4. Each paper shows:
   - ID, Subject, Academic Year, Exam Month, Year, Semester, Status, File
   - Actions: Send to Email, Delete

### Sending Papers via Email

1. Click "Send to Email" button for any paper
2. Enter recipient's email address
3. Click "Send"
4. Email will be sent with PDF attachment (requires email configuration)

### Deleting Papers

1. Find the paper in the "All Papers" list
2. Click the "Delete" button
3. Confirm deletion
4. The database record and PDF file will be removed

## 🔧 Troubleshooting

### Database Connection Issues
```
Error: Access denied for user 'root'@'localhost'
Solution: Check MySQL credentials in DatabaseConnection.java
```

### Port 8080 Already in Use
```
Error: Address already in use: bind
Solution: 
1. Find process: netstat -ano | findstr :8080
2. Kill process: taskkill /PID <process_id> /F
3. Or change port in WebServer.java (line: private static final int PORT = 8080)
```

### PDF Not Found
```
Error: PDF not found
Solution: 
1. Ensure PDF file is in the PDF folder
2. Check file name matches database entry exactly (case-sensitive)
```

### Email Not Sending
```
Solution:
1. Verify SMTP_USER and SMTP_PASS environment variables are set
2. For Gmail, use App Password, not regular password
3. Restart Command Prompt after setting variables
```

### Compilation Errors
```
Solution:
1. Ensure all JAR files are in lib/ folder
2. Check Java version: java -version (must be 11+)
3. Clean and recompile: 
   rmdir /s /q target\classes
   mkdir target\classes
   javac -d target\classes -cp "lib\*" src\*.java
```

## 🔒 Security Recommendations

1. **Change Default Database Password**
   ```sql
   ALTER USER 'root'@'localhost' IDENTIFIED BY 'strong_password';
   ```

2. **Restrict Network Access**
   - Configure MySQL to accept connections only from localhost
   - Use firewall rules to restrict access to port 8080

3. **Enable HTTPS** (Production)
   - Use a reverse proxy (Apache/Nginx) with SSL certificate
   - Redirect HTTP to HTTPS

4. **Regular Backups**
   ```cmd
   mysqldump -u root -p questionpaper > backup_YYYYMMDD.sql
   ```

5. **File Upload Security**
   - Validate PDF files before storage
   - Implement file size limits
   - Scan for malware

## 📱 Access from Other Computers

### On Local Network

1. **Find Server IP Address**:
   ```cmd
   ipconfig
   ```
   Look for IPv4 Address (e.g., 192.168.1.100)

2. **Update API URL** in `frontend\js\main.js`:
   ```javascript
   const API_BASE_URL = 'http://192.168.1.100:8080/papers';
   ```

3. **Configure Firewall**:
   - Allow incoming connections on port 8080
   - Windows Firewall: Add inbound rule for port 8080

4. **Access from Client**:
   ```
   http://192.168.1.100:8080/frontend/index.html
   ```

## 🔄 Updating the Database Schema

If you're updating from an older version without academic_year and exam_month:

```sql
USE questionpaper;

-- Add new columns
ALTER TABLE question_paper 
ADD COLUMN academic_year VARCHAR(20) NOT NULL DEFAULT '1st Year' AFTER subject,
ADD COLUMN exam_month VARCHAR(20) NOT NULL DEFAULT 'May' AFTER academic_year;

-- Update index
DROP INDEX idx_subject_year_sem ON question_paper;
CREATE INDEX idx_subject_year_sem ON question_paper(subject, academic_year, exam_month, year, semester);

-- Update existing records with default values
UPDATE question_paper 
SET academic_year = '2nd Year', exam_month = 'May' 
WHERE academic_year = '1st Year';
```

## 📞 Support

For issues or questions:
1. Check the troubleshooting section above
2. Review error messages in the console
3. Check MySQL logs for database issues
4. Verify all JAR files are present in lib/

## 📝 System Requirements

### Minimum Requirements
- **CPU**: Intel Core i3 or equivalent
- **RAM**: 4 GB
- **Storage**: 10 GB free space
- **OS**: Windows 10/11, Linux, macOS

### Recommended Requirements
- **CPU**: Intel Core i5 or better
- **RAM**: 8 GB or more
- **Storage**: 50 GB free space (for PDF storage)
- **Network**: 100 Mbps for multi-user access

## 🎓 Features Summary

✅ Add question papers with Academic Year and Exam Month
✅ Search by Subject, Academic Year, Exam Month, Year, and Semester
✅ View all papers in organized table
✅ Send papers via email
✅ Delete papers (removes from database and file system)
✅ Export search results to CSV
✅ PDF preview in browser
✅ Responsive design for mobile/tablet
✅ Professional UI for librarians

## 🏁 Quick Start Checklist

- [ ] MySQL installed and running
- [ ] Database created using setup-database.sql
- [ ] DatabaseConnection.java configured with correct credentials
- [ ] All JAR files in lib/ folder
- [ ] Java code compiled successfully
- [ ] PDF directory created
- [ ] Email environment variables set (optional)
- [ ] Server started successfully
- [ ] Browser can access http://localhost:8080/frontend/index.html
- [ ] Test add, search, and view functionality

---

**Version**: 2.0  
**Last Updated**: December 2025  
**Author**: Question Paper Management System Team
