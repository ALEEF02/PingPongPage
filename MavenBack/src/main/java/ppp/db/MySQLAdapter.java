package ppp.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;

/**
 * Handles the actual communication with the DB
 */
public class MySQLAdapter {

    private String DB_NAME;
    private String DB_USER;
    private String DB_ADRES;
    private int DB_PORT;
    private String DB_PASSWORD;
    private Connection c;

    public MySQLAdapter(String server, int port, String databaseUser, String databasePassword, String databaseName) {
        DB_ADRES = server;
        DB_USER = databaseUser;
        DB_PASSWORD = databasePassword;
        DB_NAME = databaseName;
        DB_PORT = port;
        System.out.println("Init SQL w/ " + DB_USER + "@" + DB_ADRES + "." + DB_NAME);
    }
    
    /**
     * Connect to the SQL Server
     * @return The {@link Connection} Object
     */
    private Connection createConnection() {
        try {
            MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
            dataSource.setUser(DB_USER);
            dataSource.setPassword(DB_PASSWORD);
            dataSource.setServerName(DB_ADRES);
            dataSource.setPort(DB_PORT);
            dataSource.setDatabaseName(DB_NAME);
            dataSource.setZeroDateTimeBehavior("CONVERT_TO_NULL");
            dataSource.setServerTimezone("UTC");
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            //DiscordBot.LOGGER.error("Can't connect to the database! Make sure the database settings are corrent and the database server is running AND the database `" + DB_NAME + "` exists");
            //Launcher.stop(ExitCode.SHITTY_CONFIG, e);
        }
        return null;
    }

    /**
     * Retrieve the connection to the SQL Server
     * @return The {@link Connection} Object
     */
    public Connection getConnection() {
        if (c == null) {
            c = createConnection();
        }
        return c;
    }

    /**
     * Select from the SQL DB
     * @param sql
     * @param params
     * @return The results
     * @throws SQLException
     */
    public ResultSet select(String sql, Object... params) throws SQLException {
        PreparedStatement query;
        query = getConnection().prepareStatement(sql);
        resolveParameters(query, params);
        return query.executeQuery();
    }

    public int query(String sql) throws SQLException {
        try (Statement stmt = getConnection().createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    private void resolveParameters(PreparedStatement query, Object... params) throws SQLException {
        int index = 1;
        for (Object p : params) {
            if (p instanceof String) {
                query.setString(index, (String) p);
            } else if (p instanceof Integer) {
                query.setInt(index, (int) p);
            } else if (p instanceof Long) {
                query.setLong(index, (Long) p);
            } else if (p instanceof Double) {
                query.setDouble(index, (double) p);
            } else if (p instanceof java.sql.Date) {
                java.sql.Date d = (java.sql.Date) p;
                Timestamp ts = new Timestamp(d.getTime());
                query.setTimestamp(index, ts);
            } else if (p instanceof java.util.Date) {
                java.util.Date d = (java.util.Date) p;
                Timestamp ts = new Timestamp(d.getTime());
                query.setTimestamp(index, ts);
            } else if (p instanceof Calendar) {
                Calendar cal = (Calendar) p;
                Timestamp ts = new Timestamp(cal.getTimeInMillis());
                query.setTimestamp(index, ts);
            } else if (p == null) {
                query.setNull(index, Types.NULL);
            } else {
                throw new SQLException();
            }
            index++;
        }
    }

    public int query(String sql, Object... params) throws SQLException {
        try (PreparedStatement query = getConnection().prepareStatement(sql)) {
            resolveParameters(query, params);
            return query.executeUpdate();
        }
    }

    public int insert(String sql, Object... params) throws SQLException {
        try (PreparedStatement query = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            resolveParameters(query, params);
            query.executeUpdate();
            ResultSet rs = query.getGeneratedKeys();

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }
}
