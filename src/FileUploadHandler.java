// File Upload Handler - Handles multipart/form-data PDF uploads
// Provides validation, storage, and file management for librarian uploads

import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileUploadHandler {
    
    private static final long MAX_FILE_SIZE = ConfigManager.getMaxFileSize();
    private static final String UPLOAD_DIR = ConfigManager.getUploadDirectory();
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList("pdf"));
    
    /**
     * Parse multipart/form-data from HTTP request
     */
    public static Map<String, Object> parseMultipartFormData(HttpExchange exchange) throws IOException {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> formFields = new HashMap<>();
        
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            throw new IOException("Content-Type must be multipart/form-data");
        }
        
        // Extract boundary from content type
        String boundary = extractBoundary(contentType);
        if (boundary == null) {
            throw new IOException("No boundary found in Content-Type");
        }
        
        // Read the entire request body
        InputStream inputStream = exchange.getRequestBody();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int bytesRead;
        long totalBytes = 0;
        
        while ((bytesRead = inputStream.read(data)) != -1) {
            totalBytes += bytesRead;
            if (totalBytes > MAX_FILE_SIZE + 100000) { // Extra buffer for form data
                throw new IOException("Upload size exceeds maximum allowed size");
            }
            buffer.write(data, 0, bytesRead);
        }
        
        byte[] requestData = buffer.toByteArray();
        
        // Parse multipart data
        parseParts(requestData, boundary, formFields, result);
        
        result.put("formFields", formFields);
        return result;
    }
    
    /**
     * Extract boundary string from Content-Type header
     */
    private static String extractBoundary(String contentType) {
        String[] parts = contentType.split(";");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.startsWith("boundary=")) {
                return "--" + trimmed.substring(9);
            }
        }
        return null;
    }
    
    /**
     * Parse individual parts of multipart data
     */
    private static void parseParts(byte[] data, String boundary, Map<String, String> formFields, Map<String, Object> result) throws IOException {
        byte[] boundaryBytes = boundary.getBytes();
        int pos = 0;
        
        while (pos < data.length) {
            // Find next boundary
            int boundaryPos = indexOf(data, boundaryBytes, pos);
            if (boundaryPos == -1) break;
            
            // Move past boundary and CRLF
            pos = boundaryPos + boundaryBytes.length;
            if (pos + 2 > data.length) break;
            pos += 2; // Skip CRLF
            
            // Find end of headers (double CRLF)
            int headerEnd = indexOf(data, "\r\n\r\n".getBytes(), pos);
            if (headerEnd == -1) break;
            
            // Parse headers
            String headers = new String(Arrays.copyOfRange(data, pos, headerEnd));
            pos = headerEnd + 4; // Skip double CRLF
            
            // Find next boundary to get content
            int nextBoundary = indexOf(data, boundaryBytes, pos);
            if (nextBoundary == -1) nextBoundary = data.length;
            
            // Content is between pos and nextBoundary (minus CRLF before boundary)
            int contentEnd = nextBoundary - 2;
            if (contentEnd < pos) contentEnd = nextBoundary;
            
            // Process based on Content-Disposition
            if (headers.contains("filename=")) {
                // This is a file upload
                String filename = extractFilename(headers);
                if (filename != null && !filename.isEmpty()) {
                    byte[] fileContent = Arrays.copyOfRange(data, pos, contentEnd);
                    result.put("filename", filename);
                    result.put("filedata", fileContent);
                    result.put("filesize", fileContent.length);
                }
            } else {
                // This is a regular form field
                String fieldName = extractFieldName(headers);
                String fieldValue = new String(Arrays.copyOfRange(data, pos, contentEnd));
                if (fieldName != null) {
                    formFields.put(fieldName, fieldValue.trim());
                }
            }
            
            pos = nextBoundary;
        }
    }
    
    /**
     * Find index of pattern in byte array
     */
    private static int indexOf(byte[] data, byte[] pattern, int start) {
        if (pattern.length == 0 || pattern.length > data.length - start) {
            return -1;
        }
        
        for (int i = start; i <= data.length - pattern.length; i++) {
            boolean found = true;
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }
    
    /**
     * Extract filename from Content-Disposition header
     */
    private static String extractFilename(String headers) {
        // Only look at the Content-Disposition line, stop at newline
        String dispositionLine = headers.split("\r\n")[0];
        
        String[] parts = dispositionLine.split(";");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.startsWith("filename=")) {
                String filename = trimmed.substring(9);
                // Remove quotes and clean up
                filename = filename.replaceAll("\"", "").trim();
                // Handle filename* (RFC 5987) format
                if (filename.startsWith("UTF-8''")) {
                    filename = filename.substring(7);
                }
                System.out.println("DEBUG: Clean filename extracted: '" + filename + "'");
                return filename;
            }
        }
        return null;
    }
    
    /**
     * Extract field name from Content-Disposition header
     */
    private static String extractFieldName(String headers) {
        String[] parts = headers.split(";");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.startsWith("name=")) {
                String name = trimmed.substring(5);
                return name.replaceAll("\"", "");
            }
        }
        return null;
    }
    
    /**
     * Save uploaded file with validation
     */
    public static String saveUploadedFile(byte[] fileData, String originalFilename, QuestionPaper paper) throws IOException {
        // Validate file
        validateFile(fileData, originalFilename);
        
        // Use sanitized original filename (no forced naming strategy)
        String safeFilename = sanitizeFilename(originalFilename);
        
        // Determine storage path
        Path uploadPath = determineStoragePath(paper);
        
        // Ensure directory exists
        Files.createDirectories(uploadPath);
        
        // Save file
        Path filePath = uploadPath.resolve(safeFilename);
        Files.write(filePath, fileData);
        
        System.out.println("✓ File saved: " + filePath);
        
        // Return relative path for database (relative to PDF folder, without PDF/ prefix)
        StringBuilder relativePath = new StringBuilder();
        if (ConfigManager.shouldOrganizeByYear()) {
            relativePath.append(paper.getYear()).append("/");
        }
        relativePath.append(safeFilename);
        return relativePath.toString();
    }
    
    /**
     * Validate uploaded file
     */
    private static void validateFile(byte[] fileData, String filename) throws IOException {
        System.out.println("DEBUG: Validating file: " + filename);
        System.out.println("DEBUG: File size: " + fileData.length + " bytes");
        
        // Check file size
        if (fileData.length == 0) {
            throw new IOException("File is empty");
        }
        
        if (fileData.length > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }
        
        // Check file extension
        String extension = getFileExtension(filename);
        System.out.println("DEBUG: Extracted extension: '" + extension + "'");
        System.out.println("DEBUG: Allowed extensions: " + ALLOWED_EXTENSIONS);
        
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IOException("Only PDF files are allowed");
        }
        
        // Validate PDF signature (magic bytes)
        if (!isPdfFile(fileData)) {
            throw new IOException("File is not a valid PDF");
        }
    }
    
    /**
     * Check if file data is a valid PDF
     */
    private static boolean isPdfFile(byte[] data) {
        if (data.length < 4) return false;
        
        // PDF files start with %PDF
        return data[0] == 0x25 && // %
               data[1] == 0x50 && // P
               data[2] == 0x44 && // D
               data[3] == 0x46;   // F
    }
    
    /**
     * Get file extension
     */
    private static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "";
    }
    
    /**
     * Sanitize filename to remove unsafe characters but preserve original naming
     */
    private static String sanitizeFilename(String filename) {
        // Remove path separators and dangerous characters, but preserve original structure
        return filename.replaceAll("[/\\\\]", "_")  // Remove path separators
                      .replaceAll("[<>:\"|?*]", "_") // Remove illegal characters
                      .trim();
    }
    
    /**
     * Determine storage path based on organization strategy
     */
    private static Path determineStoragePath(QuestionPaper paper) {
        Path basePath = Paths.get(UPLOAD_DIR);
        
        if (ConfigManager.shouldOrganizeByYear()) {
            // Organize by year: PDF/2025/
            basePath = basePath.resolve(String.valueOf(paper.getYear()));
        }
        
        return basePath;
    }
    
    /**
     * Delete uploaded file
     */
    public static boolean deleteFile(String filepath) {
        try {
            Path path = Paths.get(UPLOAD_DIR, filepath);
            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println("✓ File deleted: " + path);
                return true;
            }
        } catch (IOException e) {
            System.err.println("✗ Error deleting file: " + e.getMessage());
        }
        return false;
    }
}
