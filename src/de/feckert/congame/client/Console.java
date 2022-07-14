package de.feckert.congame.client;

import java.util.HashMap;

public class Console {
	public static HashMap<Character, String> ansiFormats = new HashMap<>();
	public static HashMap<String, String> messages = new HashMap<>();

	static {
		ansiFormats.put('#', Ansi.GREEN);
		ansiFormats.put('~', Ansi.BLUE);
		ansiFormats.put('^', Ansi.WHITE);

		messages.put("attack.target_defends", "The target of your attack attacks back!");
		messages.put("attack.cp_defends", "The Capture Point defends itself!");
		messages.put("troop.attack.used", "You cannot attack using this troup, it has already executed an attack this round!");
	}

	/**
	 * Prints the map to console
	 * */
	public static void drawMap() {
		// Assemble "header" which is just the column numbers
		String headerLine1 = "  ";
		String headerLine2 = "  ";
		for (int i = 0; i < Client.world.map[0].length; i++) {
			String[] num = String.valueOf(i).split("");
			headerLine1 += num[0];

			if (num.length > 1) {
				headerLine2 += num[1];
			} else {
				headerLine2 += "/";
			}
		}

		System.out.println(headerLine1);
		System.out.println(headerLine2);

		// print map
		for (int y = 0; y < Client.world.map.length; y++) {
			System.out.print((char) (65+y) + " "); // Print row char
			for (int x = 0; x < Client.world.map[y].length; x++) {
				if (!Client.world.troopAt(x,y)) {
					if (Client.world.isFieldCP(y, x)) {
						// Formatting, should make this look prettier some time
						System.out.print(Ansi.RESET + (Client.world.capturePoint(x, y).owner == 0 ? Ansi.RED_BACKGROUND :
							Client.world.capturePoint(x, y).owner == 1 ? Ansi.CYAN_BACKGROUND : Ansi.GREEN_BACKGROUND));
						System.out.print("*");
					} else {
						System.out.print(ansiFormats.get(Client.world.map[y][x]));
						System.out.print(Client.world.map[y][x]);
					}
				} else {
					System.out.print(Ansi.YELLOW);
					System.out.print(Client.world.troop(x,y).team ? Ansi.CYAN_BACKGROUND : Ansi.RED_BACKGROUND);
					System.out.print(Client.world.troop(x,y));
				}
			}
			System.out.println(Ansi.RESET);
		}
		System.out.print(Ansi.RESET);
	}

	public static void message(String s) {
		System.out.println(messages.get(s));
	}

	public static class Ansi {
		// Reset
		public static final String RESET = "\033[0m";  // Text Reset

		// Regular Colors
		public static final String BLACK = "\033[0;30m";   // BLACK
		public static final String RED = "\033[0;31m";     // RED
		public static final String GREEN = "\033[0;32m";   // GREEN
		public static final String YELLOW = "\033[0;33m";  // YELLOW
		public static final String BLUE = "\033[0;34m";    // BLUE
		public static final String PURPLE = "\033[0;35m";  // PURPLE
		public static final String CYAN = "\033[0;36m";    // CYAN
		public static final String WHITE = "\033[0;37m";   // WHITE

		// Bold
		public static final String BLACK_BOLD = "\033[1;30m";  // BLACK
		public static final String RED_BOLD = "\033[1;31m";    // RED
		public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
		public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
		public static final String BLUE_BOLD = "\033[1;34m";   // BLUE
		public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
		public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
		public static final String WHITE_BOLD = "\033[1;37m";  // WHITE

		// Underline
		public static final String BLACK_UNDERLINED = "\033[4;30m";  // BLACK
		public static final String RED_UNDERLINED = "\033[4;31m";    // RED
		public static final String GREEN_UNDERLINED = "\033[4;32m";  // GREEN
		public static final String YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
		public static final String BLUE_UNDERLINED = "\033[4;34m";   // BLUE
		public static final String PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
		public static final String CYAN_UNDERLINED = "\033[4;36m";   // CYAN
		public static final String WHITE_UNDERLINED = "\033[4;37m";  // WHITE

		// Background
		public static final String BLACK_BACKGROUND = "\033[40m";  // BLACK
		public static final String RED_BACKGROUND = "\033[41m";    // RED
		public static final String GREEN_BACKGROUND = "\033[42m";  // GREEN
		public static final String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
		public static final String BLUE_BACKGROUND = "\033[44m";   // BLUE
		public static final String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
		public static final String CYAN_BACKGROUND = "\033[46m";   // CYAN
		public static final String WHITE_BACKGROUND = "\033[47m";  // WHITE

		// High Intensity
		public static final String BLACK_BRIGHT = "\033[0;90m";  // BLACK
		public static final String RED_BRIGHT = "\033[0;91m";    // RED
		public static final String GREEN_BRIGHT = "\033[0;92m";  // GREEN
		public static final String YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
		public static final String BLUE_BRIGHT = "\033[0;94m";   // BLUE
		public static final String PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
		public static final String CYAN_BRIGHT = "\033[0;96m";   // CYAN
		public static final String WHITE_BRIGHT = "\033[0;97m";  // WHITE

		// Bold High Intensity
		public static final String BLACK_BOLD_BRIGHT = "\033[1;90m"; // BLACK
		public static final String RED_BOLD_BRIGHT = "\033[1;91m";   // RED
		public static final String GREEN_BOLD_BRIGHT = "\033[1;92m"; // GREEN
		public static final String YELLOW_BOLD_BRIGHT = "\033[1;93m";// YELLOW
		public static final String BLUE_BOLD_BRIGHT = "\033[1;94m";  // BLUE
		public static final String PURPLE_BOLD_BRIGHT = "\033[1;95m";// PURPLE
		public static final String CYAN_BOLD_BRIGHT = "\033[1;96m";  // CYAN
		public static final String WHITE_BOLD_BRIGHT = "\033[1;97m"; // WHITE

		// High Intensity backgrounds
		public static final String BLACK_BACKGROUND_BRIGHT = "\033[0;100m"; // BLACK
		public static final String RED_BACKGROUND_BRIGHT = "\033[0;101m";   // RED
		public static final String GREEN_BACKGROUND_BRIGHT = "\033[0;102m"; // GREEN
		public static final String YELLOW_BACKGROUND_BRIGHT = "\033[0;103m";// YELLOW
		public static final String BLUE_BACKGROUND_BRIGHT = "\033[0;104m";  // BLUE
		public static final String PURPLE_BACKGROUND_BRIGHT = "\033[0;105m";// PURPLE
		public static final String CYAN_BACKGROUND_BRIGHT = "\033[0;106m";  // CYAN
		public static final String WHITE_BACKGROUND_BRIGHT = "\033[0;107m"; // WHITE
	}
}
