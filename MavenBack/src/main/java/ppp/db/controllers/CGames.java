package ppp.db.controllers;

import ppp.db.WebDb;
import ppp.db.model.OGame;
import ppp.meta.GlickoTwo;
import ppp.meta.StatusEnum;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the communication between the DB and Client for Game information
 */
public class CGames {
	
	// Since our application runs on a single machine at once, it is viable to use a local cache to speed up API times.
    private static Map<Integer, OGame> gamesCache = new ConcurrentHashMap<>();
    
    /**
     * Dump (if there's games already) and fill the cache
     */
    public static void init() {
    	Map<Integer, OGame> tempGamesCache = new ConcurrentHashMap<>();
    	List<OGame> games = getALLGames();
    	for (OGame game : games) {
    		tempGamesCache.put(game.id, game);
    	}
    	gamesCache = tempGamesCache;
    }
    
    /**
     * Properly update a game in the local game cache
     * @param toAdd the updated game object
     */
    private static void updateCachedGame(OGame toAdd) {
    	if (toAdd.id != 0) {
    		gamesCache.put(toAdd.id, toAdd);
    	}
    }
    
    /**
     * Get every game, regardless of status, in the local cache
     * @return a List of games
     */
    private static List<OGame> getAllCachedGames() {
    	List<OGame> ret = new ArrayList<>(gamesCache.values());
    	return ret;
    }
    
    /**
     * Fill a new User Object from an SQL ResultSet
     * @param resultset The SQL ResultSet
     * @return A filled User Object
     * @throws SQLException
     */
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
        bank.ratingCycle = resultset.getInt("rating_cycle");
        return bank;
    }
    
    /**
     * Get every game, regardless of status, from the DB
     * 
     * @returns Every game
     */
    public static List<OGame> getALLGames() {
        List<OGame> ret = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select(
                "SELECT * " +
                        "FROM games " +
                        "ORDER BY id DESC")) {
            while (rs.next()) {
                ret.add(fillRecord(rs));
            }
            rs.getStatement().close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return ret;
    }
    
    /**
     * Get the number of games that need to be Accepted until Glicko2 is run
     * 
     * @returns int - the # of games
     */
    public static int getNumOfGamesUntilRating() {
    	int games = 0;
    	try (ResultSet rs = WebDb.get().select(
	            "SELECT COUNT(id) " +
	                    "FROM games " +
	                    "WHERE status = 2")) {
    		rs.next();
	        games = rs.getInt(1);
	        rs.getStatement().close();
	    } catch (Exception e) {
	        System.out.println(e);
	    }
    	return GlickoTwo.RATING_PERIOD - games;
    }
    
    /**
     * Get the number of games that a user has played
     * 
     * @param userId the id of the user
     * @param status the status of the games
     * 
     * @returns int - the # of games
     */
    public static int getNumOfGamesForUser(int userId, StatusEnum.Status status) {
    	return getNumOfGamesForUser(userId, status, false);
    }

    /**
     * Get the number of games that a user has played
     * 
     * @param userId the id of the user
     * @param status the status of the games
     * @param userCache whether to user the cache or not
     * 
     * @returns int - the # of games
     */
    public static int getNumOfGamesForUser(int userId, StatusEnum.Status status, boolean useCache) {
    	int games = 0;
    	if (!useCache) {
    		
	        if (status == StatusEnum.Status.ANY) {
	        	
	        	try (ResultSet rs = WebDb.get().select(
	    	            "SELECT COUNT(id) " +
	    	                    "FROM games " +
	    	                    "WHERE (receiver = ? OR sender = ?)", userId, userId)) {
	    	        rs.next();
	    	        games = rs.getInt(1);
	    	        rs.getStatement().close();
	    	    } catch (Exception e) {
	    	        System.out.println(e);
	    	    }
	        	
	        } else if (status == StatusEnum.Status.FILED) { // Must be accepted or calculated
	        	
	        	try (ResultSet rs = WebDb.get().select(
	    	            "SELECT COUNT(id) " +
	    	                    "FROM games " +
	    	                    "WHERE status > 1 AND (receiver = ? OR sender = ?)", userId, userId)) {
	        		rs.next();
	    	        games = rs.getInt(1);
	    	        rs.getStatement().close();
	    	    } catch (Exception e) {
	    	        System.out.println(e);
	    	    }
	        	
	        } else {
	    	
				try (ResultSet rs = WebDb.get().select(
			            "SELECT COUNT(id) " +
			                    "FROM games " +
			                    "WHERE status = ? AND (receiver = ? OR sender = ?)", status.getNum(), userId, userId)) {
					rs.next();
	    	        games = rs.getInt(1);
			        rs.getStatement().close();
			    } catch (Exception e) {
			        System.out.println(e);
			    }
				
	        }
	        
    	} else { // Same thing as above, but retreiving from the cache
    		
    		List<OGame> allGames = getAllCachedGames();
    		if (status == StatusEnum.Status.ANY) {
	        	
    			for (int i = 0; i < allGames.size(); i++) {
    				OGame game = allGames.get(i);
    				if (game.receiver == userId || game.sender == userId) games++;
    			}
	        	
	        } else if (status == StatusEnum.Status.FILED) {
	        	
	        	for (int i = 0; i < allGames.size(); i++) {
    				OGame game = allGames.get(i);
    				if (game.status.getNum() > 1 && (game.receiver == userId || game.sender == userId)) games++;
    			}
	        	
	        } else {
	    	
	        	for (int i = 0; i < allGames.size(); i++) {
    				OGame game = allGames.get(i);
    				if (game.status.getNum() == status.getNum() && (game.receiver == userId || game.sender == userId)) games++;
    			}
				
	        }
    		
    	}
	    return games;
    }
    
    /**
     * Find a specific game from the DB
     * @param id of the game in question
     * @return The game object, filled if it exists, {@code id=0} if it doesn't
     */
    public static OGame getGameById(int id) {
        OGame ret = new OGame();
        try (ResultSet rs = WebDb.get().select(
                "SELECT * " +
                        "FROM games " +
                        "WHERE id = ?", id)) {
            if (rs.next()) {
                ret = fillRecord(rs);
            }
            rs.getStatement().close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return ret;
    }
    
    /**
     * Get the 20 latest games from the DB
     * @return A list of Game Objects
     */
    public static List<OGame> getLatestGames() {
    	return getLatestGames(20);
    }
    
    /**
     * Get the latest games from the DB
     * @param limit The number of games to retrieve. Maximum 200
     * @return A list of Game Objects
     */
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
    
    /**
     * Get the latest games from the DB that fit to a specified status
     * @param status The status of the games to find
     * @return A list of Game Objects
     */
    public static List<OGame> getLatestGamesByStatus(StatusEnum.Status status) {
    	return getLatestGamesByStatus(status, 20);
    }
    
    /**
     * Get the latest games from the DB that fit to a specified status
     * @param status The status of the games to find
     * @param limit The number of games to retrieve. Maximum 200
     * @return A list of Game Objects
     */
    public static List<OGame> getLatestGamesByStatus(StatusEnum.Status status, int limit) {
    	if (limit < 1 || limit > 200) limit = 20;
    	if (status == StatusEnum.Status.ANY) return getLatestGames(limit);
    	if (status == StatusEnum.Status.FILED) {
    		List<OGame> ret = new ArrayList<>();
            try (ResultSet rs = WebDb.get().select(
                    "SELECT * " +
                            "FROM games " +
                    		"WHERE status > 1 " +
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
    
    /**
     * Get every game from the DB that matches the status
     * 
     * @param status The status of the games to find
     * @return A list of every Game Object that matches. Can be empty.
     */
    public static List<OGame> getAllGamesByStatus(StatusEnum.Status status) {
    	if (status == StatusEnum.Status.ANY) return getALLGames();
    	if (status == StatusEnum.Status.FILED) {
    		List<OGame> ret = new ArrayList<>();
            try (ResultSet rs = WebDb.get().select(
                    "SELECT * " +
                            "FROM games " +
                    		"WHERE status > 1 " +
                            "ORDER BY id DESC")) {
                while (rs.next()) {
                    ret.add(fillRecord(rs));
                }
                rs.getStatement().close();
            } catch (Exception e) {
                System.out.println(e);
            }
            return ret;
    	}
    	
        List<OGame> ret = new ArrayList<>();
        try (ResultSet rs = WebDb.get().select(
                "SELECT * " +
                        "FROM games " +
                		"WHERE status = ? " +
                        "ORDER BY id DESC", status.getNum())) {
            while (rs.next()) {
                ret.add(fillRecord(rs));
            }
            rs.getStatement().close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return ret;
    }

    /**
     * Get every game involving a specific user from the DB
     * 
     * @param userId the internal ID of the user
     * @return A list of every Game Object that matches. Can be empty.
     */
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
    
    /**
     * Get every game involving a specific user from the DB
     * 
     * @param userId The internal ID of the user
     * @param status The status of the games to find
     * @return A list of every Game Object that matches. Can be empty.
     */
    public static List<OGame> getGamesForUserByStatus(int userId, StatusEnum.Status status) {
        List<OGame> ret = new ArrayList<>();
        if (status == StatusEnum.Status.ANY) {
        	return getGamesForUser(userId);
        }
        
        if (status == StatusEnum.Status.FILED) {
            try (ResultSet rs = WebDb.get().select(
            		"SELECT * " +
    	                    "FROM games " + 
    	                    "WHERE status > 1 AND (receiver = ? OR sender = ?) " +
    	            		"ORDER BY id DESC ", userId, userId)) {
                while (rs.next()) {
                    ret.add(fillRecord(rs));
                }
                rs.getStatement().close();
            } catch (Exception e) {
                System.out.println(e);
            }
            return ret;
    	}
        
		try (ResultSet rs = WebDb.get().select(
	            "SELECT * " +
	                    "FROM games " +
	                    "WHERE status = ? AND (receiver = ? OR sender = ?) " + 
	            		"ORDER BY id DESC", status.getNum(), userId, userId)) {
	        while (rs.next()) {
	            ret.add(fillRecord(rs));
	        }
	        rs.getStatement().close();
	    } catch (Exception e) {
	        System.out.println(e);
	    }
	    return ret;
    }

    /**
     * Get every game a specific user sent from the DB
     * @param userId The internal ID of the user
     * @param status The status of the games to find
     * @return A list of every Game Object that matches. Can be empty.
     */
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
        } else if (status == StatusEnum.Status.FILED) {
            try (ResultSet rs = WebDb.get().select(
                    "SELECT * " +
                            "FROM games " +
                    		"WHERE status > 1 AND sender = ?" +
                            "ORDER BY id DESC LIMIT ?", userId)) {
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
    
    /**
     * Get every game a specific user received from the DB
     * @param userId The internal ID of the user
     * @param status The status of the games to find
     * @return A list of every Game Object that matches. Can be empty.
     */
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
        } else if (status == StatusEnum.Status.FILED) {
            try (ResultSet rs = WebDb.get().select(
                    "SELECT * " +
                            "FROM games " +
                    		"WHERE status > 1 AND receiver = ?" +
                            "ORDER BY id DESC LIMIT ?", userId)) {
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
    
    /**
	 * Get all games between two users, regardless of status
	 *
	 * @param userId1 One userId
	 * @param userId2 Another userId
	 * @return A list of every Game Object that matches. Can be empty.
	 */
    public static List<OGame> getGamesBetweenUsers(int userId1, int userId2) {
    	return getGamesBetweenUsers(userId2, userId2, StatusEnum.Status.ANY);
    }
    
    /**
	 * Get all games between two users, by status
	 *
	 * @param userId1 One userId
	 * @param userId2 Another userId
	 * @param status Status of game to find
	 * @return A list of every Game Object that matches. Can be empty.
	 */
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
        } else if (status == StatusEnum.Status.FILED) {
            try (ResultSet rs = WebDb.get().select(
            		"SELECT * " +
	                        "FROM games " +
	                        "WHERE status > 1 AND ((receiver = ? AND sender = ?) OR (receiver = ? AND sender = ?))", userId1, userId2, userId2, userId1)) {
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
	                        "WHERE status = ? AND ((receiver = ? AND sender = ?) OR (receiver = ? AND sender = ?))", status.getNum(), userId1, userId2, userId2, userId1)) {
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
    
    /**
     * Creates a new Game Object and inserts it into the DB. This does not check for score validity.<br>
     * The Game object's {@code id} is updated once the request is complete.
     * @param sender The sender's ID
     * @param receiver The receiver's ID
     * @param winner The winner's ID
     * @param winnerScore The winner's score
     * @param loserScore The loser's score
     */
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

    /**
     * Inserts a new Game Object into the DB. The Game object's {@code id} is updated once the request is complete.
     * @param rec The OGame record to be inserted
     */
    public static void insert(OGame rec) {
        try {
            if (rec.date == null) {
                rec.date = new Timestamp(System.currentTimeMillis());
            }
            rec.id = WebDb.get().insert(
                    "INSERT INTO games(date, status, sender, receiver, winner, winner_score, loser_score, rating_cycle) " +
                            "VALUES (?,?,?,?,?,?,?,?)",
                    rec.date, rec.status.getNum(), rec.sender, rec.receiver, rec.winner, rec.winnerScore, rec.loserScore, rec.ratingCycle);
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateCachedGame(rec);
    }
    
    /**
	 * Updates the database record to the one provided.
	 *
	 * @param record The OGame record
	 */
    public static void update(OGame record) {
        try {
            WebDb.get().query(
                    "UPDATE games SET date = ?, status = ?, sender = ?, receiver = ?, winner = ?, winner_score = ?, loser_score = ?, rating_cycle = ? WHERE id = ?",
                    record.date, record.status.getNum(), record.sender, record.receiver, record.winner, record.winnerScore, record.loserScore, record.ratingCycle, record.id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateCachedGame(record);
    }
    
    /**
	 * Deletes the database record of the provided game
	 *
	 * @param record The OGame record
	 */
	public static void delete(OGame record) {
		if (record.id == 0) return;
        try {
            WebDb.get().query(
                    "DELETE FROM games WHERE id = ? ",
                    record.id
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        gamesCache.remove(record.id);
    }
}
