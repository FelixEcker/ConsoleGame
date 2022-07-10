package de.feckert.congame;

import de.feckert.congame.troops.Scout;
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
		capturePoints[2][2] = new CapturePoint(1, 2, 2, .3f);
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
	public static void updateWorld() {
		// Troops
		for (int y = 0; y < troops.length; y++) {
			for (int x = 0; x < troops[y].length; x++) {
				if (troops[y][x] !=  null) {
					troops[y][x].update();
				}
			}
		}
		
		// Capture Points
		for (int y = 0; y < capturePoints.length; y++) {
			for (int x = 0; x < capturePoints[y].length; x++) {
				capturePoints[y][x].update();
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

	public static int createTroopByName(String name, int team, int x, int y) {
		// 0 Success
		// 1 Invalid Troop
		// 2 No Valid Fields
		if (!Troop.NAMES.contains(name)) return 1;
		
		switch (name) {
		case "scout":
			Scout temp = new Scout(team == 1);
			int[] newCoords = findNewField(x,y, temp);
			x = newCoords[0];
			y = newCoords[1];
			
			if (isFieldCP(x, y) || map[y][x] == '^' || (map[y][x] == '~' && !temp.waterTravel)) {
				return 2;
			}
			
			placeTroop(temp, x, y);
			break;
		default:
			System.err.println(World.class.getClass().getName()+"#createTroopByName switch statement defaulted! Something went wrong; Invalid troop name passed through check!");
			System.exit(-1);
			break;
		}
		
		return 0;
	}
	
	// THERE HAS TO BE A BETTER WAY TO DO THIS
	// I REFUSE TO KEEP THIS PIECE OF CODE IN HERE
	// BUT I CANT THINK OF A BETTER WAY RIGHT NOW
	private static int[] findNewField(int x, int y, Troop temp) {
		x -= 1;
		if (troopAt(x, y) || isFieldCP(x, y) || map[y][x] == '^' || (map[y][x] == '~' && !temp.waterTravel)) {
			y -= 1;
			if (troopAt(x, y) || isFieldCP(x, y) || map[y][x] == '^' || (map[y][x] == '~' && !temp.waterTravel)) {
				x++;
				if (troopAt(x, y) || isFieldCP(x, y) || map[y][x] == '^' || (map[y][x] == '~' && !temp.waterTravel)) {
					x++;
					if (troopAt(x, y) || isFieldCP(x, y) || map[y][x] == '^' || (map[y][x] == '~' && !temp.waterTravel)) {
						y++;
						if (troopAt(x, y) || isFieldCP(x, y) || map[y][x] == '^' || (map[y][x] == '~' && !temp.waterTravel)) {
							y++;
							if (troopAt(x, y) || isFieldCP(x, y) || map[y][x] == '^' || (map[y][x] == '~' && !temp.waterTravel)) {
								x++;
								if (troopAt(x, y) || isFieldCP(x, y) || map[y][x] == '^' || (map[y][x] == '~' && !temp.waterTravel)) {
									x++;
								}
							}
						}
					}
				}
			}
		}
		
		return new int[] {x,y};
	}
}
