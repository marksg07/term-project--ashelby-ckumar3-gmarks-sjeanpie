/**
 * 
 */
package edu.brown.cs.database;

/**
 * A class that handles ELO calculations based on 
 * a K scale factor.
 * @author sebma
 *
 */
public class ELOUpdater {
	
	/**
	 * A rating floor that represents the base level elo.
	 * Discourages creating new accounts in the case that
	 *that a player would have fallen below the initial elo
	 * 
	 */	 
	public static final double ELOFLOOR = 1000;
	
	/**
	 * A factor that determines how much overall weight to give
	 * to a change in elo. Lower elos equate to a higher K, while
	 * higher elos have a lower K value, reflecting a slower experience
	 * gain for skilled players.
	 * 
	 * @param elo
	 * @return The K factor for the given elo
	 */
	public static Integer K(double elo) {
		if (elo < 1100) {
			return 75;
		}
		else if (elo >= 1100 && elo <= 1299) {
			return 50;
		}
		else if (elo >= 1300 && elo <= 1599) {
			return 40;
		}
		else
			return 32;
		}
	
	/**
	 * Generates a ratio which represents the expected win ratio of 
	 * a elo1 against elo2. This function models a logistic curve
	 * @param elo1
	 * @param elo2
	 * @return A value between 0 and 1 representing the expected win ratio
	 *         of elo1 against elo2
	 */
	public static double expectedResult(double elo1, double elo2) { 
		return 1 / (1 + Math.pow(10, (elo2 - elo1) / 700));
	}
	
	/**
	 * Calculates the change in elo for two players based on the result of winning,
	 * losing, or drawing a match. Note that the values in the array are the changes
	 * in elo and not the new elos themselves.
	 * @param result - The result of the match for elo1, 
	 *                 either "WIN", "LOSE", or "DRAW" 
	 * @param elo1
	 * @param elo2
	 * @return An array of length two representing the change in elo for player 1 and
	 *         player 2 respectively.
	 */
	public static double[] update(String result, double elo1, double elo2) {
		double[] changes = {0.0, 0.0};
		switch (result) {
		case "WIN":
			changes[0] = (K(elo1) * (1 - expectedResult(elo1, elo2)));
			changes[1] = (K(elo2) * (0 - expectedResult(elo2, elo1)));

		case "LOSE":
			changes[0] = (K(elo1) * (0 - expectedResult(elo1, elo2)));
			changes[1] = (K(elo2) * (1 - expectedResult(elo2, elo1)));
			
		case "DRAW":
			changes[0] =  (K(elo1) * (0.5 - expectedResult(elo1, elo2)));
			changes[0] =  (K(elo2) * (0.5 - expectedResult(elo2, elo1)));
		}
		
		return changes;
	}
}


