package ppp.meta;

import java.util.ArrayList;
import java.util.List;

import ppp.db.controllers.CGames;
import ppp.db.controllers.CUser;
import ppp.db.model.OGame;
import ppp.db.model.OUser;

public class GlickoTwo {
	
	// http://www.glicko.net/glicko/glicko2.pdf
	
	public static final int BASE_RATING = 1400; // Paper recommends 1500
	public static final int BASE_RD = 350; // Rating deviation
	public static final double BASE_VOLATILITY = 0.055; // The degree of expected fluctuation in a player’s rating. Paper recommends 0.6
	public static final int RATING_PERIOD = 10; // This is the number of TOTAL ACCEPTED GAMES that need to be played before we run Glicko2 Analysis. Paper recommends 10-15 games PER PLAYER!
	public static final double TAU = 0.4; // Constraint of the change in volatility over time. Paper recommends 0.3-1.2
	public static final double GLICKO2_CONV = 173.7178; // A value used in converting between standard and Glicko2 values
	public static final double EPSILON = 0.000001; // Convergence Tolerance
	
	public static void run() {
		List<OUser> players = CUser.getAllNotBannedUsers();
		List<OGame> games = CGames.getLatestGamesByStatus(StatusEnum.Status.ACCEPTED);
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
			System.out.println("\tid\tµj\tφj\tg(φ)j\tE\tsj");
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
			System.out.println("\tA\tB\tf(A)\tf(B)\n\t");
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
			double phiPrime = 1 / (Math.sqrt( (1 / Math.pow(phiAsterisk, 2)) + (1 / v) ));
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
			CUser.update(updatedPlayer);
		}
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
		return 1 / (1 + Math.exp(-1 * g(phij) * (mu - muj)));
	}
	
	/**
	 * Step 3 Helper formula
	 *
	 * @param phij Opponent phi (rating deviation)
	 * @return double g value
	 */
	public static double g(double phij) {
		return 1 / Math.sqrt(1 + ((3 * Math.pow(phij, 2)) / Math.pow(Math.PI, 2)) );
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
