package ppp.db.model;

import ppp.db.AbstractModel;
import ppp.db.controllers.CUser;

import java.sql.Timestamp;

public class OUser extends AbstractModel {
    public int id = 0;
    public String username = "";
    public String email = "";
    public String token = "";
    public Timestamp tokenExpiryDate = null;
    public int elo = 1400;
    public Timestamp signUpDate = null;
    public Timestamp lastSignIn = null;
    public boolean banned = false;
    public int rank = -1;
    
	/**
	 * Returns a JSON Object as a String with insensitive info
	 *
	 * @return {@code String} JSON with insensitive info
	 */
    public String toPublicJSON() {
    	return "{\"id\":\"" + id + 
    			"\",\"username\":\"" + username + 
    			"\",\"elo\":\"" + elo + 
    			"\",\"signUpDate\":\"" + signUpDate + 
    			"\",\"lastSignIn\":\"" + lastSignIn + 
    			"\",\"banned\":\"" + banned + 
    			"\",\"rank\":\"" + rank + 
    			"\"}";
    }
    
    /**
	 * Retrieves the user's rank. Sets it as the variable {@value rank} of this object.
	 */
    public void getRank() {
    	rank = CUser.getUserRank(id);
    }
}
