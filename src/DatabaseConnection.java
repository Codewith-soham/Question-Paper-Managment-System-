// Database Connection - Now uses HikariCP connection pooling for better performance
// This class maintains backward compatibility while using the new pool

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    
    /**
     * Get database connection from the connection pool
     * Uses HikariCP for efficient connection management
     */
    public static Connection getConnection() throws SQLException {
        return DatabaseConnectionPool.getConnection();
    }
    
    /**
     * Initialize the connection pool (call at application startup)
     */
    public static void initialize() {
        DatabaseConnectionPool.initialize();
    }
    
    /**
     * Shutdown connection pool (call at application shutdown)
     */
    public static void shutdown() {
        DatabaseConnectionPool.shutdown();
    }
    
    /**
     * Check if database connection is healthy
     */
    public static boolean isHealthy() {
        return DatabaseConnectionPool.isHealthy();
    }
}
