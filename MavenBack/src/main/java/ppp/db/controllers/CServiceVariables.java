package ppp.db.controllers;

import ppp.db.WebDb;
import ppp.db.model.OServiceVariable;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * data communication with the controllers `service_variables`
 */
public class CServiceVariables {

    public static OServiceVariable findBy(String serviceName, String variable) {
        return findBy(CServices.getCachedId(serviceName), variable);
    }

    public static OServiceVariable findBy(int serviceId, String variable) {
        OServiceVariable record = new OServiceVariable();
        try (ResultSet rs = WebDb.get().select(
                "SELECT *  " +
                        "FROM service_variables " +
                        "WHERE service_id = ? AND variable = ? ", serviceId, variable)) {
            if (rs.next()) {
                record = fillRecord(rs);
            }
            rs.getStatement().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return record;
    }
    
    /**
     * Fill a new ServiceVariable Object from an SQL {@link ResultSet}
     * @param resultset The SQL {@link ResultSet}
     * @return A filled ServiceVariable Object
     * @throws SQLException
     */
    private static OServiceVariable fillRecord(ResultSet resultset) throws SQLException {
        OServiceVariable record = new OServiceVariable();
        record.serviceId = resultset.getInt("service_id");
        record.variable = resultset.getString("variable");
        record.value = resultset.getString("value");
        return record;
    }

    /**
	 * Updates a ServiceVariable object into the database or inserts it, if it doesn't exist.The ServiceVariable object's {@code id} is updated once the request is complete.
	 *
	 * @param record The OServiceVariable record.
	 */
    public static void insertOrUpdate(OServiceVariable record) {
        try {
            WebDb.get().insert(
                    "INSERT INTO service_variables(service_id, variable, value) " +
                            "VALUES (?,?,?) ON DUPLICATE KEY UPDATE value = ?",
                    record.serviceId, record.variable, record.value, record.value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
