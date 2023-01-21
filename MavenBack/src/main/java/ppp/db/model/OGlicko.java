package ppp.db.model;

import ppp.db.AbstractModel;
import ppp.db.controllers.CGames;
import ppp.meta.GlickoTwo;
import ppp.meta.StatusEnum;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * A Glicko object
 */
public class OGlicko extends AbstractModel {
    public int id = 0;
    public int userId = 0;
    public Timestamp date = new Timestamp(new Date().getTime());
    public double rating = GlickoTwo.BASE_RATING;
    public double rd = GlickoTwo.BASE_RD;
    public double volatility = GlickoTwo.BASE_VOLATILITY;
    /**
     * The rating cycle this Glicko object was calculated at the end of. Should be AFTER every game with a matching {@link OGame#ratingCycle}
     */
    public int ratingCycle = 0;
    
    /**
     * Sets {@link OGlicko#ratingCycle} the current Rating Cycle. Defaults to 1 if the first rating cycle has not been calculated yet
     */
    public void checkRatingCycle() {
    	if (ratingCycle != 0) return;
    	List<OGame> lastCalculatedGame = CGames.getLatestGamesByStatus(StatusEnum.Status.CALCULATED, 1);
    	ratingCycle = lastCalculatedGame.isEmpty() ? 1 : lastCalculatedGame.get(0).ratingCycle;
    }
    
    /**
     * Gets the converted rating, Mu, of this record
     * @return the Mu value
     */
    public double getMu() {
    	return (rating - GlickoTwo.BASE_RATING) / GlickoTwo.GLICKO2_CONV;
    }
    
    /**
     * Gets the converted rating deviation, Phi, of this record
     * @return the Phi value
     */
    public double getPhi() {
    	return rd / GlickoTwo.GLICKO2_CONV;
    }
    
    /**
     * Formats the Glicko Object into a JSON. Can be served to an HTTP Request 
     * @return a JSON formatted string
     */
    public String toPublicJSON() {
    	return "{\"id\":\"" + id + 
    			"\",\"ratingCycle\":" + ratingCycle + 
    			",\"elo\":" + rating + 
    			",\"rd\":" + rd + 
    			",\"vol\":" + volatility + 
    			",\"date\":\"" + date + 
    			"\"}";
    }
}
