package de.feckert.congame.common;

import de.feckert.congame.client.Console;
import de.feckert.congame.common.troops.*;
import de.feckert.congame.util.Direction;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class World implements Serializable {
	public int width, height;
	public char[][] map;
	public CapturePoint[][] capturePoints;
	public Troop[][] troops;

	private static char[] landTiles = {'▒', '▓', '█'};

	public World(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public void generate() throws InterruptedException {
		troops        = new Troop[height][width];
		capturePoints = new CapturePoint[height][width];
		map = new char[height][width];

		long startTime = System.nanoTime();
		// Initial Height Map
		int[][] heightMap = DSquare.generateHeightMapWithBound(new Random(ThreadLocalRandom.current().nextInt()), 0.003, 3, width, height, 9);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				map[y][x] = landTiles[heightMap[y][x]];
			}
		}

		// Sprinkle some mountains, they only generate on tiles with height of 2
		int[][] mountainMap = DSquare.generateHeightMapWithBound(new Random(ThreadLocalRandom.current().nextInt()), 0.01, 2, width, height, 4);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int cval = mountainMap[y][x];
				if (cval == 1 && heightMap[y][x] == 2) map[y][x] = '^';
			}
		}

		// Stray some rivers
		int[][] springs = new int[(int) ((width*height)*1/256)][2];
		for (int i = 0; i < springs.length; i++) {
			int x = 0, y = 0;
			while (map[y][x] == '^' || map[y][x] == '~') {
				x = ThreadLocalRandom.current().nextInt(width);
				y = ThreadLocalRandom.current().nextInt(height);
			}
			springs[i] = new int[]{x, y}; map[y][x] = '~';

		}

		// Make the rivers flow
		int fails = 0;
		for (int i = 0; i < springs.length; i++) {
			int sx = springs[i][0], sy = springs[i][1];
			Direction direction = Direction.randomDirection(false);
			Random riverRandom = new Random();

			int length = 0;
			int cx = sx, cy = sy, startElevation = heightMap[sy][sx];
			while (startElevation <= heightMap[cy][cx] && fails < 10) {

				int ocx = cx, ocy = cy;
				int divergence = riverRandom.nextInt(100); // The Divergence value is used to determine in what direction the river is to divert
				if (divergence > 50 && divergence < 70) { // If above 50 and below 70, depending on the initial direction, go either south or east
					if (Direction.isHorizontal(direction)) cy++;
					if (!Direction.isHorizontal(direction)) cx++;
					length++;
				} else if (divergence >= 70) {  // If n >=70, go either north or west
					if (Direction.isHorizontal(direction)) cy--;
					if (!Direction.isHorizontal(direction)) cx--;
					length++;
				} else { // If no divergence, just continue in the initial direction
					switch (direction) {
						case NORTH -> cy--;
						case SOUTH -> cy++;
						case EAST -> cx++;
						case WEST -> cx--;
					}
					length++;
				}

				// Keep river in bounds
				if (cx < 0) { cx = 0; fails++; }
				if (cy < 0) { cy = 0; fails++; }
				if (cx >= width) { cx = width-1; fails++; }
				if (cy >= height) { cy = height-1; fails++; }

				// Make sure for it to land on valid tiles
				if (map[cy][cx] == '^' || map[cy][cx] == '~') {
					cx = ocx;
					cy = ocy;
					fails++;
					continue;
				}

				map[cy][cx] = '~';
			}
			if (length < 10 && fails < 20) {
				i--;
				fails++;
				continue;
			}
			fails = 0;
		}

		int nControlPoints = (int) ((width * height) * (.01));
		int[][] locations = new int[nControlPoints][];

		for (int i = 0; i < locations.length; i++) {
			int x = 0, y = 0;
			if (i == 0) {
				x = ThreadLocalRandom.current().nextInt(width);
				y = ThreadLocalRandom.current().nextInt(height);
			} else {
				boolean invalid = true;
				while (invalid) {
					invalid = false;
					x = ThreadLocalRandom.current().nextInt(width);
					y = ThreadLocalRandom.current().nextInt(height);

					for (int j = 0; j < locations.length; j++) {
						if (locations[j] != null && j != i) {
							if (Troop.movementDistance(x, y, locations[j][0], locations[j][1]) < 8) {
								invalid = true;
							}
						}
					}
				}
			}

			System.out.printf("point: %s; x: %s; y: %s\n", i, x, y);
			locations[i] = new int[] {x,y};
		}

		System.out.print(nControlPoints);
		for (int i = 0; i < locations.length; i++) {
			int x = locations[i][0], y = locations[i][1];

			int sx = x-1, sy = y-1;
			if (sx < 0) sx = 0;
			if (sy < 0) sy = 0;

			// Ensure that there is space around the CP by removing all mountains
			for (int y1 = sy; y1 < y+1 && y1 < height; y1++) {
				for (int x1 = sx; x1 < x+1 && x1 < width; x1++) {
					if (map[y1][x1] == '^') map[y1][x1] = landTiles[heightMap[y1][x1]];
				}
			}

			capturePoints[y][x] = new CapturePoint(2, x, y, 1f);
		}

		//System.out.printf("Generation took %s ms\n", (System.nanoTime()-startTime)/1000000);
	}

	/**
	 * Checks if the given team has captured all points
	 * */
	public int winningPlayer() {
		int lastOwner = 0;
		
		for (int y = 0; y < capturePoints.length; y++) {
			for (int x = 0; x < capturePoints[y].length; x++) {
				if (capturePoints[y][x] != null) {
					if (capturePoints[y][x].owner == 2) return -1;
					if (x != 0 && y != 0 && capturePoints[y][x].owner != lastOwner) return -1;
					lastOwner = capturePoints[y][x].owner;
				}
			}
		}
		
		return lastOwner;
	}
	
	public CapturePoint capturePoint(int x, int y) {
		return capturePoints[y][x];
	}
	
	public boolean isFieldCP(int x, int y) {
		return capturePoints[y][x] != null;
	}
	

	/**
	 * Check if a troop is at specified Coordinates
	 * */
	public boolean troopAt(int x, int y) {
		return troops[y][x] != null;
	}

	/**
	 * Get the troop at specified coordinates
	 * */
	public Troop troop(int x, int y) {
		return troops[y][x];
	}

	public int[] troopCoords(Troop troop) {
		int[] coords = {-1, -1};

		for (int y = 0; y < map.length; y++) {
			for (int x = 0; x < map[y].length; x++) {
				if (troop(x, y) == troop) return new int[] {x, y};
			}
		}

		return coords;
	}

	/**
	 * Place a Troop on the Map
	 * */
	public boolean placeTroop(Troop troop, int x, int y) {
		if (troopAt(x, y)) return false;

		troops[y][x] = troop;
		return true;
	}

	/**
	 * Updates all troops (Round Healing, Movement Restoration,...)
	 * */
	public void updateWorld() {
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
				if (!isFieldCP(x ,y)) continue;
				capturePoints[y][x].update();
			}
		}
	}

	/**
	 * Move a troop on the map, does not do movement checks or movement deduction!
	 */
	public void moveTroop(int originX, int originY, int destX, int destY) {
		Troop troop = troop(originX, originY);
		troops[originY][originX] = null;
		troops[destY][destX] = troop;
	}

	/**
	 * Remove a troop from the map
	 * */
	public void removeTroop(int attX, int attY) {
		troops[attY][attX] = null;
	}

	public int createTroopByName(String name, int team, int x, int y) {
		// 0 Success
		// 1 Invalid Troop
		// 2 No Valid Fields
		if (!Troop.NAMES.contains(name)) return 1;

		Troop temp = null;
		switch (name) {
			case "scout":
				temp = new Scout(team);
				break;
			case "artillery":
				temp = new Artillery(team);
				break;
			case "heavyartillery":
				temp = new HeavyArtillery(team);
				break;
			case "infantry":
				temp = new Infantry(team);
				break;
			case "medic":
				temp = new Medic(team);
				break;
		default:
			System.err.println(World.class.getClass().getName()+"#createTroopByName switch statement defaulted! Something went wrong; Invalid troop name passed through check!");
			System.exit(-1);
			break;
		}
		if (temp == null) {
			System.err.println(World.class.getClass().getName()+"#createTroopByName temporary troop variable is null after switch! This shouldnt be possible!");
			System.exit(-1);
		}

		// Determine coords for new troop
		int[] newCoords = findNewField(x,y, temp);
		x = newCoords[0];
		y = newCoords[1];

		if (isFieldCP(x, y) || map[y][x] == '^' || (map[y][x] == '~' && !temp.waterTravel)) {
			return 2;
		}

		// Place troop
		if (placeTroop(temp, x, y)) return 0;
		return 2;
	}

	private int[] findNewField(int x, int y, Troop temp) {
		for (int y1 = y - 1; y1 < y + 3; y1++) {
			for (int x1 = x - 1; x1 < x + 3; x1++) {
				if (!troopAt(x1, y1) && !isFieldCP(x1, y1) && map[y1][x1] != '^'){
					if (map[y1][x1] == '~' && !temp.waterTravel) continue;
					return new int[]{x1, y1};
				}
			}
		}

		return new int[] {x, y};
	}
}
