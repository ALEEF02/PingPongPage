package ppp.db;

import ppp.ServerConfig;

import java.sql.SQLException;
import java.util.HashMap;

public class WebDb {

    private static final String DEFAULT_CONNECTION = "main";
    private static HashMap<String, MySQLAdapter> connections = new HashMap<>();

    public static MySQLAdapter get(String key) {
        if (connections.containsKey(key)) {
            return connections.get(key);
        }
        System.out.println(String.format("The MySQL connection '%s' is not set!", key));
        return null;
    }

    public static MySQLAdapter get() {
        return connections.get(DEFAULT_CONNECTION);
    }

    public static void init() {
        int cores = Runtime.getRuntime().availableProcessors();
        connections.clear();
        connections.put(DEFAULT_CONNECTION, new MySQLAdapter(ServerConfig.DB_HOST, ServerConfig.DB_PORT, ServerConfig.DB_USER, ServerConfig.DB_PASS, ServerConfig.DB_NAME));
       /* for (int i = 1; i < cores; i++) {
            connections.put("discord" + i, new MySQLAdapter(ServerConfig.DB_HOST, ServerConfig.DB_PORT, ServerConfig.DB_USER, ServerConfig.DB_PASS, ServerConfig.DB_NAME));
        }*/
        try {
            get().query("SET NAMES utf8mb4");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("COULD NOT SET utf8mb4");
        }
    }
}