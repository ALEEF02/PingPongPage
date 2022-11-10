package ppp.db.model;

import ppp.db.AbstractModel;
import ppp.db.controllers.CGlicko;
import ppp.db.controllers.CUser;
import ppp.meta.GlickoTwo;
import ppp.meta.StatusEnum;
import ppp.meta.StatusEnum.Status;

import java.sql.Timestamp;

public class OGame extends AbstractModel {
    public int id = 0;
    public Timestamp date = null;
    public StatusEnum.Status status = Status.PENDING;
    public int sender = 0;
    public int receiver = 0;
    public int winner = 0;
    public int winnerScore = 0;
    public int loserScore = 0;
    
    private OUser senderUser = new OUser();
    private OUser receiverUser = new OUser();
    
    /**
	 * Calculate the score against an opponent.
	 * Where 0 is absolute loss, 0.5 is tie, 1 is absolute win
	 * @see <a href="https://www.desmos.com/calculator/h2efschvsv">desmos visualization</a>
	 * 
	 * @param playerId the userId of the player, NOT the opponent
	 * @return double Score From 0.0 - 1.0
	 */
    public double calcScore(int playerId) {
		// lossRate is a function of diff, as defined, from 0 - 0.5. 0 represents a tie, 0.5 represents absolute loss
		double diff = (double)(winnerScore - loserScore) / (double)(winnerScore + loserScore);
		double lossRate = ( (1.0 / Math.log10(21)) * Math.log10((20.0 * diff) + 1) ) / 2.0;
		double score = 0.5 - lossRate;
		
    	if (playerId == winner) {
    		score = 1 - score;
    	}
    	
    	return score;
    }
    
    public String toJSON() {
    	senderUser = CUser.getCachedUser(sender);
    	receiverUser = CUser.getCachedUser(receiver);
    	OGlicko senderGlicko = CGlicko.getAtTime(sender, date);
    	OGlicko receiverGlicko = CGlicko.getAtTime(receiver, date);
    	double matchup = 0;
    	if (senderGlicko.id == 0 || receiverGlicko.id == 0) { // Not getting anything valid from the cache, really weird. Log this.
    		System.out.println("Issue in OGame.toJSON()\n\tsender: " + sender + "\n\trec: " + receiver + "\n\tdate: " + date + "\n\tsGlick: " + senderGlicko.id + "\n\trGlick: " + receiverGlicko.id);
	    	if (sender == winner) {
	    		matchup = GlickoTwo.chanceOfWinning(senderUser.getMu(), receiverUser.getMu(), senderUser.getPhi(), receiverUser.getPhi());
	    	} else {
	    		matchup = GlickoTwo.chanceOfWinning(receiverUser.getMu(), senderUser.getMu(), receiverUser.getPhi(), senderUser.getPhi());    		
	    	}
    	} else {
    		if (sender == winner) {
	    		matchup = GlickoTwo.chanceOfWinning(senderGlicko.getMu(), receiverGlicko.getMu(), senderGlicko.getPhi(), receiverGlicko.getPhi());
	    	} else {
	    		matchup = GlickoTwo.chanceOfWinning(receiverGlicko.getMu(), senderGlicko.getMu(), receiverGlicko.getPhi(), senderGlicko.getPhi());    		
	    	}
    	}
    	Integer[] expected = GlickoTwo.expectedScore(matchup);
    	return "{\"id\":\"" + id + 
    			"\",\"date\":\"" + date.toString() + 
    			"\",\"status\":\"" + status + 
    			"\",\"sender\":\"" + sender + 
    			"\",\"receiver\":\"" + receiver + 
    			"\",\"senderName\":\"" + senderUser.username + 
    			"\",\"receiverName\":\"" + receiverUser.username + 
    			"\",\"winner\":\"" + winner + 
    			"\",\"winnerScore\":\"" + winnerScore + 
    			"\",\"loserScore\":\"" + loserScore +
    			"\",\"matchup\":\"" + matchup +
    			"\",\"pred\":[" + expected[0] + ", " + expected[1] + "]" +
    			"}";
    }
}
