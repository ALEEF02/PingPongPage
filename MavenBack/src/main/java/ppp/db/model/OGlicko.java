package ppp.db.model;

import ppp.db.AbstractModel;
import ppp.db.controllers.CGames;
import ppp.meta.GlickoTwo;
import ppp.meta.StatusEnum;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class OGlicko extends AbstractModel {
    public int id = 0;
    public int userId = 0;
    public Timestamp date = new Timestamp(new Date().getTime());
    public double rating = GlickoTwo.BASE_RATING; // TODO: Update this to double
    public double rd = GlickoTwo.BASE_RD; // TODO: Update this to double
    public double volatility = GlickoTwo.BASE_VOLATILITY; // TODO: Update this to double
    public int ratingCycle = 0;
    
    public void checkRatingCycle() {
    	if (ratingCycle != 0) return;
    	List<OGame> lastCalculatedGame = CGames.getLatestGamesByStatus(StatusEnum.Status.CALCULATED, 1);
    	ratingCycle = lastCalculatedGame.isEmpty() ? 1 : lastCalculatedGame.get(0).ratingCycle;
    }
    
    public double getMu() {
    	return (rating - GlickoTwo.BASE_RATING) / GlickoTwo.GLICKO2_CONV;
    }
    
    public double getPhi() {
    	return rd / GlickoTwo.GLICKO2_CONV;
    }
    
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
