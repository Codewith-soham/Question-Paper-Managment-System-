-- Migration Script: Add Academic Year and Exam Month fields
-- Run this if you're upgrading from an older version

USE questionpaper;

-- Check if columns already exist before adding
SET @dbname = DATABASE();
SET @tablename = 'question_paper';
SET @columnname1 = 'academic_year';
SET @columnname2 = 'exam_month';

-- Add academic_year column if it doesn't exist
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname1)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname1, ' VARCHAR(20) NOT NULL DEFAULT "2nd Year" AFTER subject')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add exam_month column if it doesn't exist
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname2)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname2, ' VARCHAR(20) NOT NULL DEFAULT "May" AFTER academic_year')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Recreate the index with new columns
DROP INDEX IF EXISTS idx_subject_year_sem ON question_paper;
CREATE INDEX idx_subject_year_sem ON question_paper(subject, academic_year, exam_month, year, semester);

-- Display success message
SELECT 'Migration completed successfully! academic_year and exam_month columns added.' AS Status;

-- Show updated table structure
DESCRIBE question_paper;
