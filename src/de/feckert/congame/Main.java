package de.feckert.congame;

import de.feckert.congame.troops.Scout;
import de.feckert.congame.troops.Troop;
import de.feckert.congame.util.ActionResult;

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
    public static void main(String[] args) {
    	World.generate(12, 12);
    	
        Scout s = new Scout(true); // Test Troop
        World.placeTroop(s, 0, 0);

        Scout s1 = new Scout(false);
        World.placeTroop(s1, 0, 1);
        
        while (true) {
            World.updateTroops();
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

    /**
     * Execute a players action
     * */
    public static void doAction(String action) {
        // Format command
        String[] split = action.split(" ");
        String   invoke = split[0];
        String[] parameters = Arrays.copyOfRange(split, 1, split.length);

        switch (invoke) {
            case "nextTurn":
                playersTurn = false;
                break;
            case "move":
            	int[] coords;
            	coords = translateCoordinates(parameters[0]);
                int originX = coords[0];
                int originY = coords[1];
                if (!World.troopAt(originX, originY)) {System.out.println("No troop at those coordinates!");}
                Troop troop = World.troop(originX, originY);

            	coords = translateCoordinates(parameters[1]);
                int destX = coords[0];
                int destY = coords[1];

                if (!troop.canTravelTo(originX, originY, destX, destY)) {
                    System.out.println("Distance too far for troop to move!");
                    return;
                }

                if (!World.troopAt(destX, destY)) {
                    World.moveTroop(originX, originY, destX, destY);
                    troop.movementThisTurn -= Troop.movementDistance(originX, originY, destX, destY); // Deduct movement this round
                    redrawMapPostAction = true;
                } else { // A Troop is already on the destination tile, if its an enemy offer to attack
                    System.out.println("A Troop is already on this tile!");
                    if (!World.troop(destX, destY).team) {
                        System.out.println("Attack this troop?");
                        if (chooseYesNo()) {
                            doAction("attack "+parameters[0]+" "+parameters[1]);
                        }
                    }
                }
                break;
            case "attack": // Attacks
                // Attacker

            	coords = translateCoordinates(parameters[0]);
                int attX = coords[0];
                int attY = coords[1];
                if (!World.troopAt(attX, attY)) {System.out.println("No troop at "+parameters[0]);}

                troop = World.troop(attX, attY);
                if (!troop.team) {
                    System.out.println("The troop at "+parameters[0]+" does not belong to you!");
                    return;
                }

            	coords = translateCoordinates(parameters[1]);
                int defX = coords[0];
                int defY = coords[1];
                if (!World.troopAt(defX, defY)) {System.out.println("No troop at "+parameters[1]);}

                Troop defender = World.troop(defX, defY);
                if (defender.team) {
                    System.out.println("The troop at "+parameters[0]+" can't be attacked, it belongs to you!");
                    return;
                }

                // Execute Attack
                ActionResult actionResult = troop.attack(defender);

                // Give attack information
                if (actionResult == ActionResult.SUCCESS) {
                    System.out.printf("Successfully attacked! Attacker is at %.2f%% health, defender at %.2f%% health!\n",
                            troop.health*100, defender.health*100);
                } else if (actionResult == ActionResult.FAILED) {
                    System.out.printf("Attack failed! Your troop is now at %.2f%% health!\n", troop.health*100);
                } else if (actionResult == ActionResult.INVALID) {
                    System.out.println("This troop is incapable of attacking!");
                } else if (actionResult == ActionResult.TROOP_DIED) {
                    System.out.printf("Your troop died! Defender is at %.2f%% health\n",
                            defender.health*100);
                    World.removeTroop(attX, attY);
                } else if (actionResult == ActionResult.TARGET_DIED) {
                    World.removeTroop(defX, defY);
                    troop.health += 0.04; // Award health for killing
                    if (troop.health > 1.0f) troop.health = 1.0f; // Make sure troop hasnt got more than 1.0f health
                    System.out.printf("You killed your target! Attacker is at %.2f%% health, your troop was awarded 4%% health back!\n",
                            troop.health*100);
                }

                redrawMapPostAction = true;
                break;
            case "capture":
            	coords = translateCoordinates(parameters[0]);
            	int pX = coords[0];
                int pY = coords[1];
                if (!World.isFieldCP(pX, pY)) {
                	System.out.println("That field is not a Capture Point!");
                	return;
                }

            	coords = translateCoordinates(parameters[1]);
                int x = coords[0];
                int y = coords[1];
                if (!World.troopAt(x,y)) {
                	System.out.println("There is no troop at that location!");
                }
                troop = World.troop(x, y);
                if (!troop.team || !troop.canCapture) {
                	System.out.println("You cannot use this troop to capture!");
                	return;
                }
                
                if (troop.canTravelTo(x, y, pX, pY)) {
                	// TODO: Capturing Logic
                } else {
                	System.out.println("The troop can't the travel to that Capture Point!");
                }
                
            	break;
            case "troop": // Prints out information/stats of a troop at x,y coordinates
            	coords = translateCoordinates(parameters[0]);
                x = coords[0];
                y = coords[1];
                if (World.troopAt(x, y)) {
                    troop = World.troop(x, y);
                    System.out.print(Console.Ansi.YELLOW_BACKGROUND);
                    System.out.printf("Stats for %s at %s:%s\n", troop.name, parameters[0], Console.Ansi.RESET);
                    System.out.printf("     Team: %s\n", troop.team ? "You" : "Enemy");
                    System.out.printf("     Health: %.2f%%\n", troop.health*100);
                    System.out.printf("     Movement: %s/%s\n", troop.movementThisTurn, troop.movement);
                    System.out.printf("     Attack Damage: %.2f%%\n", troop.attackDmg*100);
                    System.out.printf("     Attack Absorption: %.2f%%\n", troop.dmgAbsorption*100);
                    System.out.printf("     Defense Absorption: %.2f%%\n", troop.defDmgAbsorption*100);
                } else {
                    System.out.println("No troop at those coordinates!");
                }
                break;
            case "commands":
                System.out.println("List of Commands:");
                System.out.println("    move   <origin-coords>   <destination-coords> Move a troop");
                System.out.println("    attack <attacker-coords> <defender-coords>    Attack another troop");
                System.out.println("    troop  <troop-coords> Get Information about a troop");
                break;
            default:
                break;
        }
    }
    
    public static int[] translateCoordinates(String raw) {
    	return new int[] {
    			 Integer.parseInt(raw.split(";")[0]),
                 ((byte) raw.split(";")[1].toCharArray()[0]) - 65
    	};
    }

    public static boolean chooseYesNo() {
        System.out.print("[Y]ES/[N]O? ");
        String in = userInput.nextLine().toLowerCase();
        if (in.startsWith("y")) {
            return true;
        } else if (in.startsWith("n")) {
            return false;
        } else {
            System.out.println("Invalid choice!");
            return chooseYesNo();
        }
    }
}
