package ppp.db.model;

import ppp.db.AbstractModel;
import ppp.db.controllers.CGames;
import ppp.db.controllers.CGlicko;
import ppp.db.controllers.CUser;
import ppp.meta.GlickoTwo;
import ppp.meta.StatusEnum;

import java.sql.Timestamp;
import java.util.List;

/**
 * A User object
 */
public class OUser extends AbstractModel {
    public int id = 0;
    public String username = "";
    public String email = "";
    public String token = "";
    public Timestamp tokenExpiryDate = null;
    public double rating = GlickoTwo.BASE_RATING;
    public double rd = GlickoTwo.BASE_RD;
    public double volatility = GlickoTwo.BASE_VOLATILITY;
    public Timestamp signUpDate = null;
    public Timestamp lastSignIn = null;
    public boolean banned = false;
    public int rank = -1;
    
    public double getMu() {
    	return (rating - GlickoTwo.BASE_RATING) / GlickoTwo.GLICKO2_CONV;
    }
    
    public double getPhi() {
    	return rd / GlickoTwo.GLICKO2_CONV;
    }
    
    /**
	 * Returns a JSON Object as a String with insensitive info
	 *
	 * @return {@code String} JSON with insensitive info
	 */
    public String toPublicJSON() {
    	return toPublicJSON(false, false);
    }
    
	/**
	 * Returns a JSON Object as a String with insensitive info
	 *
	 * @param numGamesPlayedInCycle if true, get the number of games they've played in the cycle
	 * @return {@code String} JSON with insensitive info
	 */
    public String toPublicJSON(boolean numGamesPlayedInCycle, boolean withHistories) {
    	
    	if (!numGamesPlayedInCycle && !withHistories) {
        	return "{\"id\":\"" + id + 
        			"\",\"username\":\"" + username + 
        			"\",\"elo\":\"" + rating + 
        			"\",\"rd\":\"" + rd + 
        			"\",\"vol\":\"" + volatility + 
        			"\",\"signUpDate\":\"" + signUpDate + 
        			"\",\"lastSignIn\":\"" + lastSignIn + 
        			"\",\"banned\":\"" + banned + 
        			"\",\"rank\":\"" + rank + 
        			"\"}";
    	}
    	
    	String rt = "{\"id\":\"" + id + 
    			"\",\"username\":\"" + username + 
    			"\",\"elo\":\"" + rating + 
    			"\",\"rd\":\"" + rd + 
    			"\",\"vol\":\"" + volatility + 
    			"\",\"signUpDate\":\"" + signUpDate + 
    			"\",\"lastSignIn\":\"" + lastSignIn + 
    			"\",\"banned\":\"" + banned + 
    			"\",\"rank\":\"" + rank + "\"";
    	
    	if (withHistories) {
    		List<OGlicko> glickos = CGlicko.findByUserId(id);
    		String glickoHist = "[";
		    for (OGlicko glicko:glickos) {
		    	glickoHist += glicko.toPublicJSON();
		    	if (glickos.indexOf(glicko) != glickos.size() - 1) {
		    		glickoHist += ",";
		    	}
		    }
		    glickoHist += "]";
	    	rt +=	",\"glickoHist\":" + glickoHist;
    	}
    	
    	if (numGamesPlayedInCycle) {
    		rt +=	",\"gamesPlayedInCycle\":\"" + CGames.getNumOfGamesForUser(id, StatusEnum.Status.ACCEPTED, true) + "\"";
    	}
    	
    	rt += "}";
    	return rt;
    	
    }
    
    /**
	 * Retrieves the user's rank. Sets it as the variable {@value rank} of this object.
	 */
    public void getRank() {
    	rank = CUser.getUserRank(id);
    }
}
