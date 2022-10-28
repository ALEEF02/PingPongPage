package ppp.db.controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ppp.db.WebDb;
import ppp.db.model.OGlicko;
import ppp.db.model.OUser;

public class CGlicko {
	
	public static List<OGlicko> findById(int internalId) {
        List<OGlicko> list = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select(
                "SELECT *  " +
                        "FROM glicko " +
                        "WHERE userId = ? ", internalId)) {
        	while (rs.next()) {
        		list.add(fillRecord(rs));
            }
            rs.getStatement().close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return list;
    }
	
	private static OGlicko fillRecord(ResultSet rs) throws SQLException {
        OGlicko s = new OGlicko();
        s.id = rs.getInt("id");
        s.userId = rs.getInt("userId");
        s.date = rs.getTimestamp("date");
        s.rating = rs.getDouble("rating");
        s.rd = rs.getDouble("rd");
        s.volatility = rs.getDouble("volatility");
        return s;
    }
	
	public static void insert(OGlicko record) {
        try {
            record.id = WebDb.get().insert(
                    "INSERT INTO glicko(userId, date, rating, rd, volatility) " +
                            "VALUES (?,?,?,?,?)",
                            record.userId, record.date, record.rating, record.rd, record.volatility);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
