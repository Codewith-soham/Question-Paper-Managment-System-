// Database Connection Manager with HikariCP Connection Pooling
// Provides efficient connection management for production use

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionPool {
    private static HikariDataSource dataSource;
    private static boolean initialized = false;

    /**
     * Initialize connection pool with configuration
     */
    public static void initialize() {
        if (initialized) {
            return;
        }

        try {
            HikariConfig config = new HikariConfig();
            
            // Database connection settings from config
            config.setJdbcUrl(ConfigManager.getDbUrl());
            config.setUsername(ConfigManager.getDbUsername());
            config.setPassword(ConfigManager.getDbPassword());
            
            // Connection pool settings
            config.setMaximumPoolSize(ConfigManager.getDbMaxPoolSize());
            config.setMinimumIdle(ConfigManager.getDbMinIdle());
            config.setConnectionTimeout(30000); // 30 seconds
            config.setIdleTimeout(600000); // 10 minutes
            config.setMaxLifetime(1800000); // 30 minutes
            
            // Performance settings
            config.setAutoCommit(true);
            config.setConnectionTestQuery("SELECT 1");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            
            // Pool name for monitoring
            config.setPoolName("QPMS-HikariCP-Pool");
            
            dataSource = new HikariDataSource(config);
            initialized = true;
            
            System.out.println("✓ Database connection pool initialized successfully");
            System.out.println("  - Max Pool Size: " + ConfigManager.getDbMaxPoolSize());
            System.out.println("  - Database: " + ConfigManager.getDbUrl());
            
        } catch (Exception e) {
            System.err.println("✗ Failed to initialize connection pool: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Cannot initialize database connection pool", e);
        }
    }

    /**
     * Get connection from the pool
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initialize();
        }
        
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized");
        }
        
        return dataSource.getConnection();
    }

    /**
     * Close the connection pool (call on application shutdown)
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("✓ Database connection pool closed");
        }
        initialized = false;
    }

    /**
     * Get pool status for monitoring
     */
    public static String getPoolStatus() {
        if (dataSource == null) {
            return "Pool not initialized";
        }
        
        return String.format(
            "Pool Status - Active: %d, Idle: %d, Total: %d, Waiting: %d",
            dataSource.getHikariPoolMXBean().getActiveConnections(),
            dataSource.getHikariPoolMXBean().getIdleConnections(),
            dataSource.getHikariPoolMXBean().getTotalConnections(),
            dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }

    /**
     * Check if pool is healthy
     */
    public static boolean isHealthy() {
        if (dataSource == null || dataSource.isClosed()) {
            return false;
        }
        
        try (Connection conn = getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
}
