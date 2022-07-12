package de.feckert.congame;

import de.feckert.congame.common.CapturePoint;
import de.feckert.congame.common.World;
import de.feckert.congame.common.troops.Scout;
import de.feckert.congame.common.troops.Troop;
import de.feckert.congame.util.ActionResult;
import de.feckert.congame.util.Console;

import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static boolean playersTurn = true;
    public static boolean redrawMapPostAction = false;
    public static int turnNum = 0;
    public static Scanner userInput = new Scanner(System.in);

    /*
    * # <- Land (Land Troops)
    * ~ <- Water (Marine & Swimmable Troops)
    * ^ <- Mountain (None)
    *
    * */

    // I attempted to use as little OOP shit as possible,
    // ideally the only use would be for Troops.
    // Multiplayer version is probably gonna use a lot more
    public static void main(String[] args) {
    	World.generate(12, 12);
    	
        Scout s = new Scout(true); // Test Troop
        World.placeTroop(s, 0, 0);

        Scout s1 = new Scout(false);
        World.placeTroop(s1, 0, 1);
        
        while (true) {
            World.updateWorld();
            System.out.println("====================");
            System.out.println("Turn "+turnNum+" //\n");
            Console.drawMap();

            // Players Turn
            playersTurn = true;
            while (playersTurn) {
                System.out.print("\nAction> ");
                String action = userInput.nextLine();
                doAction(action);
                if (redrawMapPostAction) Console.drawMap();
                redrawMapPostAction = false;
            }

            // Enemies turn
            Enemy.turn();
            turnNum++;
        }
    }
}
