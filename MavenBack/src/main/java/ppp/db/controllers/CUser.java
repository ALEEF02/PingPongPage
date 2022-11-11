package ppp.db.controllers;

import ppp.db.WebDb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ppp.db.model.OUser;
import ppp.meta.GlickoTwo;

public class CUser {
	
    private static Map<Integer, OUser> userCache = new ConcurrentHashMap<>(); // Our cached users will be tokenless.
    
    public static void init() {
    	List<OUser> users = getALLUsers();
    	for (OUser user : users) {
    		userCache.put(user.id, user);
    	}
    }
	
    /**
     * Get a user from the INTERNAL CACHE
     * 
     * @param internalId the id of the user
     * @return OUser found from the cache, id = 0 if it doesn't exist
     */
    public static OUser getCachedUser(int internalId) {
    	if (!userCache.containsKey(internalId)) {
    		OUser toAdd = findById(internalId, false);
    		userCache.put(internalId, toAdd);
    	}
    	return userCache.get(internalId);
    }
    
    private static void updateCachedUser(OUser toAdd) {
    	if (toAdd.id != 0) {
    		userCache.put(toAdd.id, toAdd);
    	}
    }
    
	/**
	 * Find a user by their username. This does NOT check to see if they are logged in.
	 * DOES NOT serve token info
	 *
	 * @param username the username to lookup
	 * @return {@code OUser} filled if the user exists
	 */
    public static OUser findByUsername(String username) {
        OUser s = new OUser();
        try (ResultSet rs = WebDb.get().select(
                "SELECT *  " +
                        "FROM users " +
                        "WHERE username = ? ", username)) {
            if (rs.next()) {
        		s = fillRecord(rs);
            }
            rs.getStatement().close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return s;
    }
    
    /**
	 * Find a user by their email. This does NOT check to see if they are logged in.
	 * Does NOT serve the token.
	 *
	 * @param email The email address to lookup
	 * @return {@code OUser} filled if the user exists, no token
	 */
    public static OUser findByEmail(String email) {
    	return findByEmail(email, false);
    }
    
	/**
	 * Find a user by their email. This does NOT check to see if they are logged in.
	 * DO NOT serve this information to an unauthenticated user!
	 *
	 * @param email The email address to lookup
	 * @param withToken Whether to serve the token too. For authentication purposes!
	 * @return {@code OUser} filled if the user exists
	 */
    public static OUser findByEmail(String email, boolean withToken) {
        OUser s = new OUser();
        try (ResultSet rs = WebDb.get().select(
                "SELECT *  " +
                        "FROM users " +
                        "WHERE email = ? ", email)) {
            if (rs.next()) {
            	if (!withToken) {
            		s = fillRecord(rs);
            	} else {
            		s = fillRecordToken(rs);
            	}
            }
            rs.getStatement().close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return s;
    }

    /**
	 * Find a user by their id. This does NOT check to see if they are logged in.
	 *
	 * @param internalId the user's id
	 * @param withToken Whether to grab the token, too
	 * @return {@link OUser} filled if the user exists, possibly with a token
	 */
    public static OUser findById(int internalId, boolean withToken) {
        OUser s = new OUser();
        try (ResultSet rs = WebDb.get().select(
                "SELECT *  " +
                        "FROM users " +
                        "WHERE id = ? ", internalId)) {
            if (rs.next()) {
            	if (!withToken) {
            		s = fillRecord(rs);
            	} else {
            		s = fillRecordToken(rs);
            	}
            }
            rs.getStatement().close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return s;
    }

    private static OUser fillRecord(ResultSet rs) throws SQLException {
        OUser s = new OUser();
        s.id = rs.getInt("id");
        s.username = rs.getString("username");
        s.email = rs.getString("email");
        s.tokenExpiryDate = rs.getTimestamp("token_expiry_date");
        s.rating = rs.getDouble("rating");
        s.rd = rs.getDouble("rd");
        s.volatility = rs.getDouble("volatility");
        s.signUpDate = rs.getTimestamp("join_date");
        s.lastSignIn = rs.getTimestamp("login_date");
        s.banned = rs.getBoolean("banned");
        return s;
    }
    
    private static OUser fillRecordRanking(ResultSet rs) throws SQLException {
        OUser s = fillRecord(rs);
        s.rank = rs.getInt("rank");
        return s;
    }
    
    private static OUser fillRecordToken(ResultSet rs) throws SQLException {
        OUser s = fillRecord(rs);
        s.token = rs.getString("token");
        return s;
    }

    public static List<OUser> getBannedUsers() {
        List<OUser> list = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select("SELECT * FROM users WHERE banned = 1")) {
            while (rs.next()) {
                list.add(fillRecord(rs));
            }
            rs.getStatement().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public static List<OUser> getCachedAllNotBannedUsers() {
    	List<OUser> users = new ArrayList<>(userCache.values());
    	users.forEach(user -> {
    		if (user.banned) {
    			users.remove(user);
    		}
    	});
    	return users;
    }
    
    public static List<OUser> getAllNotBannedUsers() {
        List<OUser> list = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select("SELECT * FROM users WHERE banned = 0")) {
            while (rs.next()) {
                list.add(fillRecord(rs));
            }
            rs.getStatement().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public static List<OUser> getALLUsers() {
        List<OUser> list = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select("SELECT * FROM users")) {
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
	 * Returns the elo ranking of the specified user.
	 *
	 * @param userId the internal user ID
	 * @return int the rank, -1 if it wasn't found (most likely {@code userId} doesn't exist)
	 */
    public static int getUserRank(int userId) {
        int rank = -1;
        try (ResultSet rs = WebDb.get().select(
                "WITH Ranks AS " +
                "(SELECT *, RANK() OVER(ORDER BY rating DESC) AS rank FROM users) " +
                "SELECT * FROM Ranks WHERE id = ? ", userId)) {
            if (rs.next()) {
            	rank = rs.getInt("rank");
            }
            rs.getStatement().close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return rank;
    }
    
    /**
     * 
     * @param numPlayers
     * @return Top # users, from highest to lowest rank.
     */
    public static List<OUser> getTopRanks(int numPlayers) {
    	return getTopRanks(numPlayers, 400);
    }
    
    public static List<OUser> getTopRanks(int numPlayers, boolean useCache) {
    	return getTopRanks(numPlayers, 400, useCache);
    }

    public static List<OUser> getTopRanks(int numPlayers, int maxRD) {
    	return getTopRanks(numPlayers, maxRD, false);
    }
    
    public static List<OUser> getTopRanks(int numPlayers, int maxRD, boolean useCache) {
    	if (numPlayers < 1 || numPlayers > 50) numPlayers = 10;
        List<OUser> ret = new ArrayList<>();
        
        if (!useCache) {
	    	if (maxRD < GlickoTwo.BASE_RD) {
	    		
		        try (ResultSet rs = WebDb.get().select(
		                "WITH Ranks AS " +
		                "(SELECT *, RANK() OVER(ORDER BY rating DESC) AS rank FROM users) " +
		                "SELECT * FROM Ranks " + 
		                "WHERE rd < ? " + 
		                "LIMIT ? ", maxRD, numPlayers)) {
		        	while (rs.next()) {
		                ret.add(fillRecordRanking(rs));
		            }
		            rs.getStatement().close();
		        } catch (Exception e) {
		            System.out.println(e);
		        }
		        
	    	} else {
	    		
	            try (ResultSet rs = WebDb.get().select(
	                    "WITH Ranks AS " +
	                    "(SELECT *, RANK() OVER(ORDER BY rating DESC) AS rank FROM users) " +
	                    "SELECT * FROM Ranks LIMIT ? ", numPlayers)) {
	            	while (rs.next()) {
	                    ret.add(fillRecordRanking(rs));
	                }
	                rs.getStatement().close();
	            } catch (Exception e) {
	                System.out.println(e);
	            }
	            
	    	}
        } else {
        	List<OUser> startRet = getCachedAllNotBannedUsers();
            Collections.sort(startRet, new SortRating());
            for (int i = 0; i < Math.min(startRet.size(), numPlayers); i++) {
            	if (startRet.get(i).rd <= maxRD) ret.add(startRet.get(i));
            }
            for (int i = 0; i < ret.size(); i++) {
            	OUser user = ret.get(i);
            	user.rank = i + 1;
            	ret.set(i, user);
            }
        }
    	
        return ret;
    }
    
    /**
	 * Updates the database record to the one provided. Will NOT modify the token.
	 *
	 * @param record The OUser record.
	 */
    public static void update(OUser record) {
    	update(record, false);
    }

    /**
	 * Updates the database record to the one provided. Can modify the token
	 *
	 * @param record The OUser record.
	 * @param updateToken Should we also modify the token?
	 */
    public static void update(OUser record, boolean updateToken) {
        if (record.id == 0) {
            insert(record);
            return;
        }
        updateCachedUser(record);
        if (updateToken) {
	        try {
	            WebDb.get().query(
	                    "UPDATE users SET username = ?, email = ?, token = ?, token_expiry_date = ?, rating = ?, rd = ?, volatility = ?, join_date = ? , login_date = ? , banned = ? " +
	                            "WHERE id = ? ",
	                    record.username, record.email, record.token, record.tokenExpiryDate, record.rating, record.rd, record.volatility, record.signUpDate, record.lastSignIn, record.banned ? 1 : 0, record.id
	            );
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
        } else {
        	try {
	            WebDb.get().query(
	                    "UPDATE users SET username = ?, email = ?, rating = ?, rd = ?, volatility = ?, join_date = ? , login_date = ? , banned = ? " +
	                            "WHERE id = ? ",
	                    record.username, record.email, record.rating, record.rd, record.volatility, record.signUpDate, record.lastSignIn, record.banned ? 1 : 0, record.id
	            );
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
        }
    }

    public static void insert(OUser record) {
        try {
            record.id = WebDb.get().insert(
                    "INSERT INTO users(username, email, token, token_expiry_date, rating, rd, volatility, join_date, login_date, banned) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?)",
                            record.username, record.email, record.token, record.tokenExpiryDate, record.rating, record.rd, record.volatility, record.signUpDate, record.lastSignIn, record.banned ? 1 : 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateCachedUser(record);
    }
}

class SortRating implements Comparator<OUser> {  
	// Used for sorting in ascending order of ID  
	public int compare(OUser a, OUser b) {  
		if (a.rating - b.rating < 0) return 1;
		if (a.rating - b.rating == 0) return 0;
		return -1;
    }  
}