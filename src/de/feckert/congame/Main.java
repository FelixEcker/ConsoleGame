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
        Scout s = new Scout(true); // Test Troop
        World.placeTroop(s, 0, 0);

        Scout s1 = new Scout(false);
        //World.placeTroop(s1, 0, 1);

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
                int originX = Integer.parseInt(parameters[0].split(";")[0]);
                int originY = ((byte) parameters[0].split(";")[1].toCharArray()[0]) - 65;
                if (!World.troopAt(originX, originY)) {System.out.println("No troop at those coordinates!");}
                Troop troop = World.troop(originX, originY);

                int destX = Integer.parseInt(parameters[1].split(";")[0]);
                int destY = ((byte) parameters[1].split(";")[1].toCharArray()[0]) - 65;

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
                int attX = Integer.parseInt(parameters[0].split(";")[0]);
                int attY = ((byte) parameters[0].split(";")[1].toCharArray()[0]) - 65;
                if (!World.troopAt(attX, attY)) {System.out.println("No troop at "+parameters[0]);}

                troop = World.troop(attX, attY);
                if (!troop.team) {
                    System.out.println("The troop at "+parameters[0]+" does not belong to you!");
                    return;
                }

                int defX = Integer.parseInt(parameters[1].split(";")[0]);
                int defY = ((byte) parameters[1].split(";")[1].toCharArray()[0]) - 65;
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
                    System.out.printf("You killed your target! Attacker is at %.2f%% health\n",
                            troop.health*100);
                }

                redrawMapPostAction = true;
                break;
            case "troop": // Prints out information/stats of a troop at x,y coordinates
                int x = Integer.parseInt(parameters[0].split(";")[0]);
                int y = ((byte) parameters[0].split(";")[1].toCharArray()[0]) - 65;
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
            default:
                break;
        }
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
