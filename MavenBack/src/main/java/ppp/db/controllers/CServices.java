package ppp.db.controllers;

import ppp.db.WebDb;
import ppp.db.model.OService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the communication between the DB and Client for Service information
 */
public class CServices {
    private static Map<String, Integer> serviceCache = new ConcurrentHashMap<>();

    /**
     * Get the Cached ID of a service
     * @param serviceName The name of the service
     * @return the ID
     */
    public static int getCachedId(String serviceName) {
        if (!serviceCache.containsKey(serviceName)) {
            OService service = findBy(serviceName);
            if (service.id == 0) {
                service.name = serviceName;
                insert(service);
            }
            serviceCache.put(serviceName, service.id);
        }
        return serviceCache.get(serviceName);
    }

    /**
     * Get every service that is active from the DB
     * @return A List of Service Objects
     */
    public static List<OService> getAllActive() {
        ArrayList<OService> list = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select("SELECT * FROM services WHERE activated = 1")) {
            while (rs.next()) {
                list.add(fillRecord(rs));
            }
            rs.getStatement().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Find a service by its Name in the DB
     * @param name The name of the service
     * @return The Service Object, filled if it exists
     */
    public static OService findBy(String name) {
        OService token = new OService();
        try (ResultSet rs = WebDb.get().select(
                "SELECT id, name, display_name, description, activated  " +
                        "FROM services " +
                        "WHERE name = ? ", name)) {
            if (rs.next()) {
                token = fillRecord(rs);
            }
            rs.getStatement().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }

    /**
     * Fill a new Service Object from an SQL {@link ResultSet}
     * @param resultset The SQL {@link ResultSet}
     * @return A filled Service Object
     * @throws SQLException
     */
    private static OService fillRecord(ResultSet resultset) throws SQLException {
        OService service = new OService();
        service.id = resultset.getInt("id");
        service.name = resultset.getString("name");
        service.displayName = resultset.getString("display_name");
        service.description = resultset.getString("description");
        service.activated = resultset.getInt("activated");
        return service;
    }

    /**
	 * Updates the database record to the one provided.
	 *
	 * @param record The OService record.
	 */
    public static void update(OService record) {
        if (record.id == 0) {
            insert(record);
            return;
        }
        try {
            WebDb.get().query(
                    "UPDATE services SET name = ?, display_name = ?, description = ?, activated = ? " +
                            "WHERE id = ? ",
                    record.name, record.displayName, record.description, record.activated, record.id
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	 * Inserts a Service object into the database.The Service object's {@code id} is updated once the request is complete.
	 *
	 * @param record The OService record.
	 */
    public static void insert(OService record) {
        try {
            record.id = WebDb.get().insert(
                    "INSERT INTO services(name, display_name, description, activated) " +
                            "VALUES (?,?,?,?)",
                    record.name, record.displayName, record.description, record.activated);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}