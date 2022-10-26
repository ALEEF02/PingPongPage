package ppp.db.controllers;

import ppp.db.WebDb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ppp.db.model.OUser;

public class CUser {
	
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
	 * Find a user by their email. This does NOT check to see if they are logged in.
	 * Does NOT serve the token.
	 *
	 * @param email The email address to lookup
	 * @return {@code OUser} filled if the user exists, no token
	 */
    public static OUser findByEmail(String email) {
    	return findByEmail(email, false);
    }

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
        s.elo = rs.getInt("elo");
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
                "(SELECT *, RANK() OVER(ORDER BY elo DESC) AS rank FROM users) " +
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
    
    public static List<OUser> getTopRanks(int numPlayers) {
    	if (numPlayers < 1 || numPlayers > 50) numPlayers = 10;
        List<OUser> ret = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select(
                "WITH Ranks AS " +
                "(SELECT *, RANK() OVER(ORDER BY elo DESC) AS rank FROM users) " +
                "SELECT * FROM Ranks LIMIT ? ", numPlayers)) {
        	while (rs.next()) {
                ret.add(fillRecordRanking(rs));
            }
            rs.getStatement().close();
        } catch (Exception e) {
            System.out.println(e);
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
        if (updateToken) {
	        try {
	            WebDb.get().query(
	                    "UPDATE users SET username = ?, email = ?, token = ?, token_expiry_date = ?, elo = ?, join_date = ? , login_date = ? , banned = ? " +
	                            "WHERE id = ? ",
	                    record.username, record.email, record.token, record.tokenExpiryDate, record.elo, record.signUpDate, record.lastSignIn, record.banned ? 1 : 0, record.id
	            );
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
        } else {
        	try {
	            WebDb.get().query(
	                    "UPDATE users SET username = ?, email = ?, elo = ?, join_date = ? , login_date = ? , banned = ? " +
	                            "WHERE id = ? ",
	                    record.username, record.email, record.elo, record.signUpDate, record.lastSignIn, record.banned ? 1 : 0, record.id
	            );
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
        }
    }

    public static void insert(OUser record) {
        try {
            record.id = WebDb.get().insert(
                    "INSERT INTO users(username, email, token, token_expiry_date, elo, join_date, login_date, banned) " +
                            "VALUES (?,?,?,?,?,?,?,?)",
                            record.username, record.email, record.token, record.tokenExpiryDate, record.elo, record.signUpDate, record.lastSignIn, record.banned ? 1 : 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
