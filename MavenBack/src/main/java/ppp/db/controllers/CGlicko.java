package ppp.db.controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ppp.db.WebDb;
import ppp.db.model.OGlicko;
import ppp.meta.GlickoTwo;

/**
 * Handles the communication between the DB and Client for Glicko information
 */
public class CGlicko {
	
	/**
	 * [userId, their Glickos ordered from earliest to latest]
	 */
	private static Map<Integer, List<OGlicko>> userGlickoCache = new ConcurrentHashMap<>();
	
	/**
     * Grab all Glicko records from the DB and put them into the cache
     */
    public static void init() {
    	List<OGlicko> glickoLogs = getALLRecords();
    	for (OGlicko entry : glickoLogs) {
    		addCachedGlicko(entry);
    	}
    }
    
    /**
     * Add a Glicko Object to the local cache
     * @param toAdd
     */
    private static void addCachedGlicko(OGlicko toAdd) {
    	if (toAdd.id != 0) {
    		if (userGlickoCache.get(toAdd.userId) != null) { // there exists a set of Glickos for this user in the cache already
    			List<OGlicko> glickos = userGlickoCache.get(toAdd.userId);
    			glickos.add(toAdd);
    			userGlickoCache.replace(toAdd.userId, glickos);
    		} else { // we need to make a new List of Glicko
    			List<OGlicko> glickos = new ArrayList<OGlicko>();
    			glickos.add(toAdd);
        		userGlickoCache.put(toAdd.userId, glickos);
    		}
    	}
    }
	
    /**
     * Get every glicko record for a specified user from the DB
     * @param internalId The ID of the User
     * @return A List of the user's Glicko Objects
     */
	public static List<OGlicko> findByUserId(int internalId) {
		List<OGlicko> list = userGlickoCache.get(internalId);
		if (list == null) {
			list = new ArrayList<OGlicko>();
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
		}
        return list;
    }
	
	/**
	 * Get the user's most recent Glicko Record BEFORE the specified time. Useful for displaying win probabilities of games before a recent Glicko Cycle.
	 * @param internalId the ID of the User
	 * @param date The Timestamp to search
	 * @return A Glicko Object, filled if one exists, the most recent if it doesn't
	 */
	public static OGlicko getAtTime(int internalId, Timestamp date) {
		OGlicko ret = new OGlicko();
		/*
        try (ResultSet rs = WebDb.get().select(
                "SELECT *  " +
                        "FROM glicko " +
                        "WHERE (userId = ? AND DATE <= '?') " + 
                        "ORDER BY id DESC LIMIT 1 ", internalId)) { // TODO: add date
        	if (rs.next()) {
        		ret = fillRecord(rs);
            }
            rs.getStatement().close();
        } catch (Exception e) {
            System.out.println(e);
        }*/
		
		List<OGlicko> glickos = userGlickoCache.get(internalId);
		if (glickos == null) return ret;
		ret = glickos.get(glickos.size() - 1); // Start with the earliest record, as a safety
		for (OGlicko glicko : glickos) {
			if (glicko.date.before(date)) { // Loop through, setting the return to the latest Glicko BEFORE the specified date.
				ret = glicko;
			} else {
				break;
			}
		}
		
        return ret;
	}
	
	/**
	 * Retrieve EVERY Glicko Record from the DB
	 * @return A List of Glicko Objects
	 */
	private static List<OGlicko> getALLRecords() {
        List<OGlicko> list = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select("SELECT * FROM glicko")) {
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
	 * Fill a new Glicko Object from an SQL {@link ResultSet}
	 * @param rs The SQL {@link ResultSet}
	 * @return A filled Glicko Object
	 * @throws SQLException
	 */
	private static OGlicko fillRecord(ResultSet rs) throws SQLException {
        OGlicko s = new OGlicko();
        s.id = rs.getInt("id");
        s.userId = rs.getInt("userId");
        s.date = rs.getTimestamp("date");
        s.rating = rs.getDouble("rating");
        s.rd = rs.getDouble("rd");
        s.volatility = rs.getDouble("volatility");
        s.ratingCycle = rs.getInt("rating_cycle");
        return s;
    }
	
	/**
	 * Inserts a Glicko object into the database. The OGlicko object's {@code id} is updated once the request is complete.
	 *
	 * @param record The OGlicko record.
	 */
	public static void insert(OGlicko record) {
		if (record.ratingCycle == 0) {
			System.out.print("OGlicko for " + record.userId + " came with no rating cycle. ");
			record.ratingCycle = GlickoTwo.getRatingCycle();
			System.out.println("Now it's " + record.ratingCycle + ".");
		}
        try {
            record.id = WebDb.get().insert(
                    "INSERT INTO glicko(userId, date, rating, rd, volatility, rating_cycle) " +
                            "VALUES (?,?,?,?,?,?)",
                            record.userId, record.date, record.rating, record.rd, record.volatility, record.ratingCycle);
        } catch (Exception e) {
            e.printStackTrace();
        }
        addCachedGlicko(record);
    }
}
