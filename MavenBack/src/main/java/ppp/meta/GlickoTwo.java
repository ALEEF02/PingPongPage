package ppp.meta;

import java.util.ArrayList;
import java.util.List;

import ppp.db.controllers.CGames;
import ppp.db.controllers.CGlicko;
import ppp.db.controllers.CUser;
import ppp.db.model.OGame;
import ppp.db.model.OGlicko;
import ppp.db.model.OUser;

public class GlickoTwo {
	
	// http://www.glicko.net/glicko/glicko2.pdf
	
	public static final int BASE_RATING = 1400; // Paper recommends 1500
	public static final int BASE_RD = 350; // Rating deviation
	public static final double BASE_VOLATILITY = 0.06; // The degree of expected fluctuation in a player’s rating. Paper recommends 0.6
	public static final int RATING_PERIOD = 35; // This is the number of TOTAL ACCEPTED GAMES that need to be played before we run Glicko2 Analysis. Paper recommends 10-15 games PER PLAYER! Rating period = avg number of games per player * num of players / 2
	public static final double TAU = 1.0; // Constraint of the change in volatility over time. Lower number is more constrained. Paper recommends 0.3-1.2
	public static final double GLICKO2_CONV = 173.7178; // A value used in converting between standard and Glicko2 values
	public static final double EPSILON = 0.000001; // Convergence Tolerance
	
	/*
	 * User Explanation:
	 * 
	 * We use the Glicko2 system to rank players. It has been used in countless ranking applications and stands as one of the best ranking systems currently available
	 * Each player has 3 values:
	 * 		Rating: or your 'elo'. It's how skilled the system currently thinks you are. Every player starts at 1400.
	 * 		Rating Deviation: The standard deviation or confidence of your rating. Lower numbers means that your rating is more accurate.
	 * 		Volatility: How consistent your play is. If you have lots of on and off days, you'll have a higher volatility. If your gameplay is consistent, it will lower
	 * Using these 3 values we can gather the skill of your gameplay.
	 * 
	 * From each game we can calculate a "score" against each opponent. Score varies from 0-1, where 0 is a loss, 0.5 is a draw, and 1 is a win. In games like chess, score would be purely one of these 3 possibilities. At PingPongPage, we've decided to use a spectrum instead.
	 * 		We start by calculating the game "diff". Diff is (winner's score - loser's score) / total points played.
	 * 		We then plug diff into 0.5 - ([ (1 / log(21)) * log((20 * diff) + 1) ] / 2), which is the "loss rate"
	 * 			If the player won, simply subtract loss rate from 1 to reciprocate it 
	 * 		This score value can be visualized here: https://www.desmos.com/calculator/h2efschvsv
	 * 
	 * Every "Rating Period", currently 25 games, we'll batch evaluate the games that have been played. This is why your values will not change after each game. Having a Rating Period prevents "racing" conditions where, if say 2 games are submitted but the second game is accepted first, the 3 values of the players in the first game would have changed, even though when the game was played they were different.
	 */
	
	public static void run() {
		List<OUser> players = CUser.getAllNotBannedUsers();
		List<OGame> games = CGames.getLatestGamesByStatus(StatusEnum.Status.ACCEPTED, RATING_PERIOD + 1);
		List<OUser> updatedPlayers = new ArrayList<>();
		System.out.println("Running Glicko2 Period...");
		
		for (OUser me : players) {
			System.out.println("--------------------------------\nProcessing for " + me.username);
			List<OUser> opponents = new ArrayList<>();
			List<OGame> gamesPlayed = new ArrayList<>();
			int m = 0;
			for (int i = 0; i < games.size(); i++) {
				if (games.get(i).sender != me.id && games.get(i).receiver != me.id) continue; // Current player is not in this game, continue
				int opponentID = games.get(i).sender == me.id ? games.get(i).receiver : games.get(i).sender;
				opponents.add(CUser.findById(opponentID, false));
				gamesPlayed.add(games.get(i));
				m++;
			}
			
			System.out.println("\tOf the " + games.size() + " games, we're in " + m);
			if (m == 0) { // Current player played no games in the Rating Period
				double rdPrime = GLICKO2_CONV * (Math.sqrt(Math.pow(me.getPhi(), 2) + Math.pow(me.volatility, 2)));
				System.out.println("\tRD: " + me.rd + " -> " + rdPrime);
				System.out.println("\tRating does not change");
				System.out.println("\tVolatility does not change");
				me.rd = rdPrime;
				updatedPlayers.add(me);
				continue;
			}
			
			// Step 2 in Glicko2
			double mu = me.getMu();
			double phi = me.getPhi();
			System.out.println("\tµ: " + mu + " φ: " + phi);
			System.out.println("\tid\tµj\t\t\t\t\tφj\t\t\t\t\tg(φ)j\t\t\t\tE\t\t\t\t\tsj");
			for (int i = 0; i < opponents.size(); i++) {
				OUser opponent = opponents.get(i);
				System.out.println("\t" + gamesPlayed.get(i).id + "\t" + opponent.getMu() + "\t" + opponent.getPhi() + "\t" + g(opponent.getPhi()) + "\t" + E(mu, opponent.getMu(), opponent.getPhi()) + "\t" + gamesPlayed.get(i).calcScore(me.id));
			}
			
			// Step 3
			double vSum = 0;
			for (int j = 1; j <= m; j++) {
				int oppInd = j-1;
				vSum += ( Math.pow(GlickoTwo.g(opponents.get(oppInd).getPhi()), 2) * GlickoTwo.E(mu, opponents.get(oppInd).getMu(), opponents.get(oppInd).getPhi()) * (1 - GlickoTwo.E(mu, opponents.get(oppInd).getMu(), opponents.get(oppInd).getPhi())) );
			}
			double v = 1 / vSum;
			
			// Step 4
			double deltaSum = 0;
			for (int j = 1; j <= m; j++) {
				int oppInd = j-1;
				deltaSum += ( GlickoTwo.g(opponents.get(oppInd).getPhi()) * (gamesPlayed.get(oppInd).calcScore(me.id) - GlickoTwo.E(mu, opponents.get(oppInd).getMu(), opponents.get(oppInd).getPhi())) );
			}
			double delta = v * deltaSum;
			System.out.println("\tv: " + v + "\t∆Σ: " + deltaSum + "\t∆: " + delta);
			
			// Step 5
			final double a = Math.log(Math.pow(me.volatility, 2));
			double A = a;
			double B = Math.log(Math.pow(delta, 2) - Math.pow(phi, 2) - v);
			if (Math.pow(delta, 2) <= Math.pow(phi, 2) + v) {
				double k = 1;
				while (GlickoTwo.f((a - (k*GlickoTwo.TAU)), delta, phi, v, a) < 0) {
					k += 1;
				}
				B = a - (k*GlickoTwo.TAU);
			}
			double fA = GlickoTwo.f(A, delta, phi, v, a);
			double fB = GlickoTwo.f(B, delta, phi, v, a);
			System.out.println("\tA\t\t\t\t\tB\t\t\t\t\tf(A)\t\t\t\t\tf(B)");
			System.out.println("\t" + A + "\t" + B + "\t" + fA + "\t" + fB);
			while (Math.abs(B - A) > GlickoTwo.EPSILON) {
				double C = A + (((A - B) * fA) / (fB - fA));
				double fC = GlickoTwo.f(C, delta, phi, v, a);
				if (fC * fB <= 0) {
					A = B;
					fA = fB; 
				} else {
					fA /= 2;
				}
				B = C;
				fB = fC;
				System.out.println("\t" + A + "\t" + B + "\t" + fA + "\t" + fB);
			}
			double volatilityPrime = Math.exp(A/2);
			
			// Step 6
			double phiAsterisk = Math.sqrt(Math.pow(phi, 2) + Math.pow(volatilityPrime, 2));
			System.out.println("\tσ': " + volatilityPrime + " φ*: " + phiAsterisk);
			
			// Step 7
			double phiPrime = 1.0 / (Math.sqrt( (1.0 / Math.pow(phiAsterisk, 2)) + (1.0 / v) ));
			double muPrime = mu + (Math.pow(phiPrime, 2) * deltaSum);
			System.out.println("\tφ': " + phiPrime + " µ': " + muPrime);
			
			// Step 8
			double ratingPrime = (GlickoTwo.GLICKO2_CONV * muPrime) + GlickoTwo.BASE_RATING;
			double rdPrime = GlickoTwo.GLICKO2_CONV * phiPrime;
			System.out.println("\tr': " + ratingPrime + " RD': " + rdPrime);
			
			System.out.println("\tRating: " + me.rating + " -> " + ratingPrime);
			System.out.println("\tRD: " + me.rd + " -> " + rdPrime);
			System.out.println("\tVolatility: " + me.volatility + " -> " + volatilityPrime);
			me.rating = ratingPrime;
			me.rd = rdPrime;
			me.volatility = volatilityPrime;
			updatedPlayers.add(me);
		}
		
		// Now that we've processed the games for all players, let's mark them as CALCULATED
		for (OGame game : games) {
			game.status = StatusEnum.Status.CALCULATED;
			CGames.update(game);
		}
		
		// Let's also record the new Glicko2 values to the db
		for (OUser updatedPlayer : updatedPlayers) {
			OGlicko glickoRecord = new OGlicko();
			glickoRecord.userId = updatedPlayer.id;
			glickoRecord.rating = updatedPlayer.rating;
			glickoRecord.rd = updatedPlayer.rd;
			glickoRecord.volatility = updatedPlayer.volatility;
			CGlicko.insert(glickoRecord);
			CUser.update(updatedPlayer);
		}
		
		System.out.println("-------------DONE!---------------");
	}
	
	/**
	 * Calculate the chance of winning / expected score.
	 *
	 * @param winMu Winner's mu (rating)
	 * @param loseMu Loser's mu (rating)
	 * @param phi Player phi (rating deviation)
	 * @param phiOp Opponent phi (rating deviation)
	 * @return double Expected score, 0.0 - 1.0
	 */
	public static double chanceOfWinning(double winMu, double loseMu, double phi, double phiOp) {
		return 1.0 / (1.0 + Math.exp(-1.0 * g( Math.sqrt(Math.pow(phi, 2) + Math.pow(phiOp, 2))) * (winMu - loseMu)));
	}
	
	/**
	 * Calculate the 2 player's expected scores.
	 * Ex: score = 0.653 -> [21, 18]
	 *
	 * @param score from {@link #chanceOfWinning() chanceOfWinning()}
	 * @return Integer[] with [winner, loser] scores
	 */
	public static Integer[] expectedScore(double score) {
		if (score == 0.5) {
			Integer ret[] = {21, 21};
			return ret;
		}
		if (score > 0.5) {
			score = 1-score; // Our score is now always 0.0-0.5
		}
		int winnerScore = 21; // We'll first start by calculating for a 21 win
		double loserScore = ((winnerScore * 21) * (Math.pow(441.0, score) - 1)) / ((19 * Math.pow(441.0, score)) + 21); //https://www.desmos.com/calculator/layst0l1pm  // https://www.wolframalpha.com/input?i2d=true&i=y%3D0.5-Divide%5BDivide%5B1%2Clog%5C%2840%2921%5C%2841%29%5D*log%5C%2840%29%5C%2840%2920*Divide%5B26-t%2C26%2Bt%5D%5C%2841%29%2B1%5C%2841%29%2C2%5D%5C%2844%29+solve+for+t
		while (winnerScore - loserScore <= 1.0 && winnerScore < 30) { // While the winner is not predicted to win by at least 2 points
			winnerScore++;
			loserScore = ((winnerScore * 21) * (Math.pow(441.0, score) - 1)) / ((19 * Math.pow(441.0, score)) + 21);
		}
		if (winnerScore - loserScore <= 1.0) { // These players are essentially a toss-up
			Integer ret[] = {21, 21};
			return ret;
		}
		Integer ret[] = {Integer.valueOf(winnerScore), Integer.valueOf((int)loserScore)}; // loserScore cast to int ensures that it's a 2-point win
		return ret;
	}
	
	/**
	 * Step 3 Helper formula
	 *
	 * @param mu Player's mu (rating)
	 * @param muj Opponent mu (rating)
	 * @param phij Opponent phi (rating deviation)
	 * @return double E value
	 */
	public static double E(double mu, double muj, double phij) {
		return 1.0 / (1.0 + Math.exp(-1.0 * g(phij) * (mu - muj)));
	}
	
	/**
	 * Step 3 Helper formula
	 *
	 * @param phij Opponent phi (rating deviation)
	 * @return double g value
	 */
	public static double g(double phij) {
		return 1.0 / Math.sqrt(1.0 + ((3.0 * Math.pow(phij, 2)) / Math.pow(Math.PI, 2)) );
	}
	
	/**
	 * Step 5 Helper formula
	 *
	 * @param x to test
	 * @param delta
	 * @param phi Player's phi (rating deviation)
	 * @param v
	 * @param a
	 * @return double f value
	 */
	public static double f(double x, double delta, double phi, double v, double a) {
		return ( (Math.exp(x) * (Math.pow(delta, 2) - Math.pow(phi, 2) - v - Math.exp(x))) / (2 * Math.pow((Math.pow(phi, 2) + v + Math.exp(x)), 2)) ) - ((x - a) / Math.pow(TAU, 2));
	}
}
