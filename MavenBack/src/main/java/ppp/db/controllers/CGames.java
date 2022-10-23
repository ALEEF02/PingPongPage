package ppp.db.controllers;

import ppp.db.WebDb;
import ppp.db.model.OGame;
import ppp.meta.StatusEnum;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CGames {

    private static OGame fillRecord(ResultSet resultset) throws SQLException {
        OGame bank = new OGame();
        bank.id = resultset.getInt("id");
        bank.date = resultset.getTimestamp("date");
        bank.status = StatusEnum.Status.fromNum(resultset.getInt("status"));
        bank.sender = resultset.getInt("sender");
        bank.receiver = resultset.getInt("receiver");
        bank.winner = resultset.getInt("winner");
        bank.winnerScore = resultset.getInt("winner_score");
        bank.loserScore = resultset.getInt("loser_score");
        return bank;
    }
    
    public static List<OGame> getLatestGames() {
    	return getLatestGames(20);
    }
    
    public static List<OGame> getLatestGames(int limit) {
    	if (limit < 1 || limit > 200) limit = 20;
        List<OGame> ret = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select(
                "SELECT * " +
                        "FROM games " +
                        "ORDER BY id DESC LIMIT ?", limit)) {
            while (rs.next()) {
                ret.add(fillRecord(rs));
            }
            rs.getStatement().close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return ret;
    }
    
    public static List<OGame> getLatestGamesByStatus(StatusEnum.Status status) {
    	return getLatestGamesByStatus(status, 20);
    }
    
    public static List<OGame> getLatestGamesByStatus(StatusEnum.Status status, int limit) {
    	if (limit < 1 || limit > 200) limit = 20;
    	if (status == StatusEnum.Status.ANY) return getLatestGames(limit);
    	
        List<OGame> ret = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select(
                "SELECT * " +
                        "FROM games " +
                		"WHERE status = ? " +
                        "ORDER BY id DESC LIMIT ?", status.getNum(), limit)) {
            while (rs.next()) {
                ret.add(fillRecord(rs));
            }
            rs.getStatement().close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return ret;
    }

    public static List<OGame> getGamesForUser(int userId) {
        List<OGame> ret = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select(
                "SELECT * " +
                        "FROM games " +
                        "WHERE (sender = ? OR receiver = ?) ", userId, userId)) {
            while (rs.next()) {
                ret.add(fillRecord(rs));
            }
            rs.getStatement().close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return ret;
    }

    public static List<OGame> getUsersSentGames(int userId, StatusEnum.Status status) {
        List<OGame> ret = new ArrayList<>();
        if (status == StatusEnum.Status.ANY) {
	        try (ResultSet rs = WebDb.get().select(
	                "SELECT * " +
	                        "FROM games " +
	                        "WHERE sender = ?", userId)) {
	            while (rs.next()) {
	                ret.add(fillRecord(rs));
	            }
	            rs.getStatement().close();
	        } catch (Exception e) {
	            System.out.println(e);
	        }
	        return ret;
        } else {
        	try (ResultSet rs = WebDb.get().select(
	                "SELECT * " +
	                        "FROM games " +
	                        "WHERE sender = ? AND status = ?", userId, status.getNum())) {
	            while (rs.next()) {
	                ret.add(fillRecord(rs));
	            }
	            rs.getStatement().close();
	        } catch (Exception e) {
	            System.out.println(e);
	        }
	        return ret;
        }
    }
    
    public static List<OGame> getUsersReceivedGames(int userId, StatusEnum.Status status) {
        List<OGame> ret = new ArrayList<>();
        if (status == StatusEnum.Status.ANY) {
	        try (ResultSet rs = WebDb.get().select(
	                "SELECT * " +
	                        "FROM games " +
	                        "WHERE receiver = ?", userId)) {
	            while (rs.next()) {
	                ret.add(fillRecord(rs));
	            }
	            rs.getStatement().close();
	        } catch (Exception e) {
	            System.out.println(e);
	        }
	        return ret;
        } else {
        	try (ResultSet rs = WebDb.get().select(
	                "SELECT * " +
	                        "FROM games " +
	                        "WHERE receiver = ? AND status = ?", userId, status.getNum())) {
	            while (rs.next()) {
	                ret.add(fillRecord(rs));
	            }
	            rs.getStatement().close();
	        } catch (Exception e) {
	            System.out.println(e);
	        }
	        return ret;
        }
    }

    public static List<OGame> getGamesForUserByStatus(int userId, StatusEnum.Status status) {
        List<OGame> ret = new ArrayList<>();
        if (status == StatusEnum.Status.ANY) {
        	return getGamesForUser(userId);
        }
        
		try (ResultSet rs = WebDb.get().select(
	            "SELECT * " +
	                    "FROM games " +
	                    "WHERE status = ?", status.getNum())) {
	        while (rs.next()) {
	            ret.add(fillRecord(rs));
	        }
	        rs.getStatement().close();
	    } catch (Exception e) {
	        System.out.println(e);
	    }
	    return ret;
    }
    
    public static List<OGame> getGamesBetweenUsers(int userId1, int userId2) {
    	return getGamesBetweenUsers(userId2, userId2, StatusEnum.Status.ANY);
    }
    
    public static List<OGame> getGamesBetweenUsers(int userId1, int userId2, StatusEnum.Status status) {
        List<OGame> ret = new ArrayList<>();
        if (status == StatusEnum.Status.ANY) {
	        try (ResultSet rs = WebDb.get().select(
	                "SELECT * " +
	                        "FROM games " +
	                        "WHERE (receiver = ? AND sender = ?) OR (receiver = ? AND sender = ?)", userId1, userId2, userId2, userId1)) {
	            while (rs.next()) {
	                ret.add(fillRecord(rs));
	            }
	            rs.getStatement().close();
	        } catch (Exception e) {
	            System.out.println(e);
	        }
        } else {
        	try (ResultSet rs = WebDb.get().select(
	                "SELECT * " +
	                        "FROM games " +
	                        "WHERE status = ? AND ((receiver = ? AND sender = ?) OR (receiver = ? AND sender = ?))", userId1, userId2, userId2, userId1, status.getNum())) {
	            while (rs.next()) {
	                ret.add(fillRecord(rs));
	            }
	            rs.getStatement().close();
	        } catch (Exception e) {
	            System.out.println(e);
	        }
        }
        return ret;
    }

    public static void insert(int sender, int receiver, int winner, int winnerScore, int loserScore) {
    	OGame rec = new OGame();
        rec.status = StatusEnum.Status.PENDING;
        rec.sender = sender;
        rec.receiver = receiver;
        rec.winner = winner;
        rec.winnerScore = winnerScore;
        rec.loserScore = loserScore;
        insert(rec);
    }

    public static void insert(OGame rec) {
        try {
            if (rec.date == null) {
                rec.date = new Timestamp(System.currentTimeMillis());
            }
            rec.id = WebDb.get().insert(
                    "INSERT INTO games(date, status, sender, receiver, winner, winner_score, loser_score) " +
                            "VALUES (?,?,?,?,?,?,?)",
                    rec.date, rec.status.getNum(), rec.sender, rec.receiver, rec.winner, rec.winnerScore, rec.loserScore);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void update(OGame record) {
        try {
            WebDb.get().query(
                    "UPDATE games SET date = ?, status = ?, sender = ?, receiver = ?, winner = ?, winner_score = ?, loser_score = ? WHERE id = ?",
                    record.date, record.status.getNum(), record.sender, record.receiver, record.winner, record.winnerScore, record.loserScore, record.id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	public static void delete(OGame record) {
        try {
            WebDb.get().query(
                    "DELETE FROM games WHERE id = ? ",
                    record.id
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}