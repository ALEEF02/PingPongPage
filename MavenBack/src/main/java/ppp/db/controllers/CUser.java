package ppp.db.controllers;

import ppp.db.WebDb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ppp.db.model.OUser;

public class CUser {
    
	/**
	 * Find a user by their email. This does NOT check to see if they are logged in.
	 * DO NOT serve this information to an unauthenticated user!
	 *
	 * @param email The email address to lookup
	 * @return {@code OUser} filled if the user exists
	 */
    public static OUser findBy(String email) {
        OUser s = new OUser();
        try (ResultSet rs = WebDb.get().select(
                "SELECT *  " +
                        "FROM users " +
                        "WHERE email = ? ", email)) {
            if (rs.next()) {
                s = fillRecord(rs);
            }
            rs.getStatement().close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return s;
    }

    public static OUser findById(int internalId) {
        OUser s = new OUser();
        try (ResultSet rs = WebDb.get().select(
                "SELECT *  " +
                        "FROM users " +
                        "WHERE id = ? ", internalId)) {
            if (rs.next()) {
                s = fillRecord(rs);
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
        s.token = rs.getString("token");
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
    
    public static OUser getUserRank(int userId) {
        OUser s = new OUser();
        try (ResultSet rs = WebDb.get().select(
                "WITH Ranks AS " +
                "(SELECT *, RANK() OVER(ORDER BY elo DESC) AS rank FROM users WHERE id > 1) " +
                "SELECT * FROM Ranks WHERE id = ? ", userId)) {
            if (rs.next()) {
                s = fillRecordRanking(rs);
            }
            rs.getStatement().close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return s;
    }
    
    public static List<OUser> getTopRanks(int numPlayers) {
        List<OUser> ret = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select(
                "WITH Ranks AS " +
                "(SELECT *, RANK() OVER(ORDER BY elo DESC) AS rank FROM users WHERE id > 1) " +
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

    public static void update(OUser record) {
        if (record.id == 0) {
            insert(record);
            return;
        }
        try {
            WebDb.get().query(
                    "UPDATE users SET username = ?, email = ?, token = ?, token_expiry_date = ?, elo = ?, join_date = ? , login_date = ? , banned = ? " +
                            "WHERE id = ? ",
                    record.username, record.email, record.token, record.tokenExpiryDate, record.elo, record.signUpDate, record.lastSignIn, record.banned ? 1 : 0, record.id
            );
        } catch (Exception e) {
            e.printStackTrace();
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
