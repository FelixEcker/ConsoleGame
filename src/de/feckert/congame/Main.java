package de.feckert.congame;

import de.feckert.congame.troops.Scout;
import de.feckert.congame.troops.Troop;
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
            	// NOTE FOR ATTACKING CAPTURE POINTS: Attacking a CP should never capture them
            	// to capture one, the "capture" action is to be used, which can intern attack
            	// a capture point.
                // Attacker
            	coords = translateCoordinates(parameters[0]);
                int attX = coords[0];
                int attY = coords[1];
                if (!World.troopAt(attX, attY)) {
                	System.out.println("No troop at "+parameters[0]); 
                	break;
                }

                troop = World.troop(attX, attY);
                if (!troop.team) {
                    System.out.println("The troop at "+parameters[0]+" does not belong to you!");
                    return;
                }

            	coords = translateCoordinates(parameters[1]);
                int defX = coords[0];
                int defY = coords[1];
                if (!World.troopAt(defX, defY)) {
                	if (World.isFieldCP(defX, defY)) {
                		attackCP(attX, attY, troop, defX, defY);
                    	return;
                	}
                	System.out.println("No troop or capture point at "+parameters[1]);
                	return;
                }

                Troop defender = World.troop(defX, defY);
                if (defender.team) {
                    System.out.println("The troop at "+parameters[0]+" can't be attacked, it belongs to you!");
                    return;
                }

                // Execute Attack & Give attack information
                switch (troop.attack(defender)) {
                case SUCCESS:
                    System.out.printf("Successfully attacked! Attacker is at %.2f%% health, defender at %.2f%% health!\n",
                            troop.health*100, defender.health*100);
                	break;
                case FAILED:
                    System.out.printf("Attack failed! Your troop is now at %.2f%% health!\n", troop.health*100);
                	break;
                case INVALID:
                    System.out.println("This troop is incapable of attacking!");
                	break;
                case TROOP_DIED:
                    System.out.printf("Your troop died! Defender is at %.2f%% health\n",
                            defender.health*100);
                    World.removeTroop(attX, attY);
                	break;
                case TARGET_DIED:
                    World.removeTroop(defX, defY);
                    troop.health += 0.04; // Award health for killing
                    if (troop.health > 1.0f) troop.health = 1.0f; // Make sure troop hasnt got more than 1.0f health
                    System.out.printf("You killed your target! Attacker is at %.2f%% health, your troop was awarded 4%% health back!\n",
                            troop.health*100);
                	break;
				default:
					System.err.println(Main.class.getClass().getName()+"#doAction (attack) switch statement defaulted! This wasn't supposed to happen!");
					System.exit(-1);
					break;
                }

                redrawMapPostAction = true;
                break;
            case "capture":
            	coords = translateCoordinates(parameters[0]);
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
            	
            	coords = translateCoordinates(parameters[1]);
            	int pX = coords[0];
                int pY = coords[1];
                if (!World.isFieldCP(pX, pY)) {
                	System.out.println("That field is not a Capture Point!");
                	return;
                }
                CapturePoint cp = World.capturePoint(pX, pY);

                if (troop.canTravelTo(x, y, pX, pY)) {
                	if (CapturePoint.capturable(cp)) {
                		redrawMapPostAction = true;
                		cp.owner = 1;
                		cp.health = .3f;
                		
                		System.out.println("You successfully captured this point! It's health was restored to 30%!");
                		return;
                	} else {
                		System.out.printf("The Capture Point is not in a capturable state!\n (Health: %.2f%% Defense Health: %.2f%%\n",
                				cp.health*100, cp.defenseHealth*100);
                		System.out.println("Do you want to attack it?");
                		if (chooseYesNo()) {
                			doAction("attack "+parameters[0]+" "+parameters[1]);
                		}
                	}
                	return;
                } else {
                	System.out.println("The troop can't the travel to that Capture Point!");
                }
                
            	break;
            case "deploy":
            	coords = translateCoordinates(parameters[0]);
            	x = coords[0];
            	y = coords[1];
            	if (!World.isFieldCP(x,y)) {
            		System.out.println("Can't deploy troop here; Field is not a Capture Point!");
            		return;
            	}
            	
            	cp = World.capturePoint(x,y);
            	if (cp.owner != 1) {
            		System.out.println("Can't deploy troop here; Capture Point does not belong to you!");
            		return;
            	}
            	
            	int code = World.createTroopByName(parameters[1].toLowerCase(), 1, x, y);
            	switch (code) {
            	case 0:
            		System.out.println("Deployed Troop!");
            		break;
            	case 1:
            		System.out.println("There is no Troop by that name!");
            		break;
            	case 2:
            		System.out.println("Could not find a Valid field around the Capture Point to deploy your troop!");
            		break;
            	}
            	// TODO: Cost dedeuctionasnoa
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
    
    // Own function in attempt to declutter the doAction function
    public static void attackCP(int attX, int attY, Troop attacker, int defX, int defY) {
    	CapturePoint point = World.capturePoint(defX, defY);
    	ActionResult result = attacker.attackCP(point);
    	
    	switch (result) {
    	case POINT_CAPTURABLE:
    		System.out.println("You've sunken the Point's core health to 0% and its defense health below 5%, you can now capture it!");
    		System.out.printf("Your troop is now at %.2f%% health!\n", attacker.health*100);
        	redrawMapPostAction = true;
    		break;
    	case TROOP_DIED:
            System.out.printf("Your troop died! Capture Point is at %.2f%% core health and %.2f%% defense health\n",
                    point.health*100, point.defenseHealth*100);
            World.removeTroop(attX, attY);
        	redrawMapPostAction = true;
    		break;
    	case SUCCESS:
            System.out.printf("The Capture Point is at %.2f%% core health and %.2f%% defense health\n",
                    point.health*100, point.defenseHealth*100);
    		System.out.printf("Your troop is now at %.2f%% health!\n", attacker.health*100);
    		break;
    	case FAILED:
    		System.out.println("Attack Failed!");
    		break;
    	default:
			System.err.println(Main.class.getClass().getName()+"#doAction -> "+
    		Main.class.getClass().getName()+"#attackCP ActionResult message switch statement defaulted! This wasn't supposed to happen!");
			System.exit(-1);
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
