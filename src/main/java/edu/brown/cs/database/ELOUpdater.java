/**
 * 
 */
package edu.brown.cs.database;

public class ELOUpdater {
	
	public static final double BASE_ELO = 1000;
	
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
	
	public static double expectedResult(double elo1, double elo2) { //pay attention to 700
		return 1 / (1 + Math.pow(10, (elo2 - elo1) / 700));
	}
	
	public static double update(String result, double elo1, double elo2) {
		switch (result) {
		case "WIN":
			return (K(elo1) * (1 - expectedResult(elo1, elo2)));
			
		case "LOSE":
			return (K(elo1) * (0 - expectedResult(elo1, elo2)));
			
		case "DRAW":
			return (K(elo1) * (0.5 - expectedResult(elo1, elo2)));
		}
		
		return 0;
	}
}


