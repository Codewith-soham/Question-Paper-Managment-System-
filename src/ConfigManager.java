// Configuration Manager - Loads and manages application settings
import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private static Properties properties = new Properties();
    private static final String CONFIG_FILE = "config.properties";
    private static boolean loaded = false;

    // Load configuration on first access
    static {
        loadConfig();
    }

    /**
     * Load configuration from config.properties file
     */
    public static void loadConfig() {
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    properties.load(fis);
                    loaded = true;
                    System.out.println("✓ Configuration loaded from " + CONFIG_FILE);
                }
            } else {
                System.err.println("⚠ Warning: " + CONFIG_FILE + " not found, using defaults");
                loadDefaults();
            }
        } catch (IOException e) {
            System.err.println("✗ Error loading configuration: " + e.getMessage());
            loadDefaults();
        }
    }

    /**
     * Load default configuration values
     */
    private static void loadDefaults() {
        // Database defaults
        properties.setProperty("db.url", "jdbc:mysql://localhost:3306/questionpaper");
        properties.setProperty("db.username", "root");
        properties.setProperty("db.password", "root");
        properties.setProperty("db.pool.maximumPoolSize", "10");
        
        // Server defaults
        properties.setProperty("server.port", "8080");
        
        // Upload defaults
        properties.setProperty("upload.directory", "PDF");
        properties.setProperty("upload.maxFileSize", "10485760"); // 10MB
        
        loaded = true;
    }

    /**
     * Get configuration value by key
     */
    public static String get(String key) {
        return get(key, null);
    }

    /**
     * Get configuration value with default fallback
     */
    public static String get(String key, String defaultValue) {
        String value = System.getenv(key.toUpperCase().replace('.', '_'));
        if (value != null) {
            return value;
        }
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Get integer configuration value
     */
    public static int getInt(String key, int defaultValue) {
        try {
            String value = get(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Get long configuration value
     */
    public static long getLong(String key, long defaultValue) {
        try {
            String value = get(key);
            return value != null ? Long.parseLong(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Get boolean configuration value
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    // Database Configuration
    public static String getDbUrl() {
        return get("db.url", "jdbc:mysql://localhost:3306/questionpaper");
    }

    public static String getDbUsername() {
        return get("db.username", "root");
    }

    public static String getDbPassword() {
        return get("db.password", "root");
    }

    public static int getDbMaxPoolSize() {
        return getInt("db.pool.maximumPoolSize", 10);
    }

    public static int getDbMinIdle() {
        return getInt("db.pool.minimumIdle", 2);
    }

    // Server Configuration
    public static int getServerPort() {
        return getInt("server.port", 8080);
    }

    public static String getServerHost() {
        return get("server.host", "0.0.0.0");
    }

    // Upload Configuration
    public static String getUploadDirectory() {
        return get("upload.directory", "PDF");
    }

    public static long getMaxFileSize() {
        return getLong("upload.maxFileSize", 10485760L); // 10MB default
    }

    public static boolean shouldOrganizeByYear() {
        // Default to flat storage unless explicitly enabled
        return getBoolean("upload.organizeByYear", false);
    }

    public static String getNamingStrategy() {
        return get("upload.namingStrategy", "timestamp");
    }

    // Email Configuration
    public static String getSmtpHost() {
        return get("smtp.host", "smtp.gmail.com");
    }

    public static int getSmtpPort() {
        return getInt("smtp.port", 587);
    }

    public static String getSmtpUsername() {
        return get("smtp.username", "");
    }

    public static String getSmtpPassword() {
        return get("smtp.password", "");
    }

    public static boolean isSmtpAuthEnabled() {
        return getBoolean("smtp.auth", true);
    }

    public static boolean isSmtpTlsEnabled() {
        return getBoolean("smtp.starttls.enable", true);
    }

    // Logging Configuration
    public static String getLogLevel() {
        return get("logging.level", "INFO");
    }

    /**
     * Check if configuration is loaded
     */
    public static boolean isLoaded() {
        return loaded;
    }

    /**
     * Reload configuration from file
     */
    public static void reload() {
        properties.clear();
        loaded = false;
        loadConfig();
    }

    /**
     * Print current configuration (for debugging)
     */
    public static void printConfig() {
        System.out.println("========================================");
        System.out.println("Current Configuration:");
        System.out.println("========================================");
        System.out.println("Database URL: " + getDbUrl());
        System.out.println("Database User: " + getDbUsername());
        System.out.println("Server Port: " + getServerPort());
        System.out.println("Upload Directory: " + getUploadDirectory());
        System.out.println("Max File Size: " + (getMaxFileSize() / 1024 / 1024) + "MB");
        System.out.println("SMTP Host: " + getSmtpHost());
        System.out.println("========================================");
    }
}
