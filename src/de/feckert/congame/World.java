package de.feckert.congame;

import de.feckert.congame.troops.Troop;

public class World {
	public static char[][] map = { // I need to make a map gen
			{'#', '#', '~', '~', '^', '^', '^', '#', '~', '#', '^', '#'},
			{'#', '#', '#', '~', '~', '^', '^', '#', '~', '^', '^', '#'},
			{'#', '#', '#', '#', '~', '#', '^', '~', '~', '#', '^', '#'},

			{'#', '#', '#', '#', '~', '#', '~', '~', '#', '#', '#', '#'},
			{'#', '#', '#', '#', '~', '#', '~', '#', '#', '#', '#', '#'},
			{'#', '#', '^', '#', '~', '~', '~', '#', '#', '#', '#', '#'},

			{'#', '#', '^', '^', '~', '#', '#', '#', '#', '#', '#', '#'},
			{'#', '#', '^', '^', '~', '#', '#', '#', '#', '#', '#', '#'},
			{'#', '#', '#', '^', '~', '#', '#', '#', '#', '#', '#', '#'},

			{'#', '#', '#', '#', '~', '#', '#', '^', '#', '#', '#', '#'},
			{'#', '#', '#', '#', '~', '#', '#', '^', '^', '#', '#', '#'},
			{'#', '#', '#', '#', '~', '#', '#', '#', '#', '#', '#', '#'},
	};

	// -1 Not a CP ; 0 Enemy ; 1 Player ; 2 Uncaptured
	public static CapturePoint[][]     capturePoints;
	public static Troop[][]   troops;
	
	static {
		troops        = new Troop[map.length][map[0].length];
		capturePoints = new CapturePoint[map.length][map[0].length];
	}
	
	public static void generate(int width, int height) {
		// TODO: World Generation
		capturePoints[2][2] = new CapturePoint(2, 2, 2, .3f);
	}
	
	/**
	 * Checks if the given team has captured all points
	 * */
	public static boolean hasTeamWon(boolean team) {
		int nTeam = team ? 1 : 0;
		
		for (int y = 0; y < capturePoints.length; y++) {
			for (int x = 0; x < capturePoints[y].length; x++) {
				if (capturePoints[y][x] != null && capturePoints[y][x].owner != nTeam) return false;
			}
		}
		
		return true;
	}
	
	public static CapturePoint capturePoint(int x, int y) {
		return capturePoints[y][x];
	}
	
	public static boolean isFieldCP(int x, int y) {
		return capturePoints[y][x] != null;
	}
	

	/**
	 * Check if a troop is at specified Coordinates
	 * */
	public static boolean troopAt(int x, int y) {
		return troops[y][x] != null;
	}

	/**
	 * Get the troop at specified coordinates
	 * */
	public static Troop troop(int x, int y) {
		return troops[y][x];
	}

	/**
	 * Place a Troop on the Map
	 * */
	public static boolean placeTroop(Troop troop, int x, int y) {
		if (troopAt(x, y)) return false;

		troops[y][x] = troop;
		return true;
	}

	/**
	 * Updates all troops (Round Healing, Movement Restoration,...)
	 * */
	public static void updateTroops() {
		for (int y = 0; y < troops.length; y++) {
			for (int x = 0; x < troops[y].length; x++) {
				if (troops[y][x] !=  null) {
					troops[y][x].update();
				}
			}
		}
	}

	/**
	 * Move a troop on the map, does not do movement checks or movement deduction!
	 */
	public static void moveTroop(int originX, int originY, int destX, int destY) {
		Troop troop = troop(originX, originY);
		troops[originY][originX] = null;
		troops[destY][destX] = troop;
	}

	/**
	 * Remove a troop from the map
	 * */
	public static void removeTroop(int attX, int attY) {
		troops[attY][attX] = null;
	}
}
