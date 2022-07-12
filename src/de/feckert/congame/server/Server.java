package de.feckert.congame.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import de.feckert.congame.Main;
import de.feckert.congame.common.CapturePoint;
import de.feckert.congame.common.World;
import de.feckert.congame.common.troops.Troop;
import de.feckert.congame.util.ActionResult;
import de.feckert.congame.util.Console;

public class Server {
	public static ServerSocket socket;
	
	public static Socket[] clients;
	public static PrintWriter[] clientWriter;
	public static BufferedReader[] clientReader;
	
	public static int roundNumber;
	public static World world;
	public static int whoseTurn;
	public static boolean redrawMapPostAction = false;
	
	public static void main(String[] args) {
		try {
			// Setup
			clients = new Socket[2];
			clientWriter = new PrintWriter[clients.length];
			clientReader = new BufferedReader[clients.length];
		
			socket = new ServerSocket(Integer.parseInt(args[0]));
			
			// Accept Players
			for (int i = 0; i < clients.length; i++) {
				clients[i] = socket.accept();
				clientWriter[i]  = new PrintWriter(new OutputStreamWriter(clients[i].getOutputStream()));
				clientReader[i]  = new BufferedReader(new InputStreamReader(clients[i].getInputStream()));
			}
			
			startGame();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void startGame() {
		whoseTurn = 0;
		roundNumber = 0;
		world = new World();
		world.generate(12, 12);
		
		while (world.winningPlayer() == -1) {
			
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
                whoseTurn = whoseTurn == 0 ? 1 : 0;
                break;
            case "move":
            	int[] coords;
            	coords = translateCoordinates(parameters[0]);
                int originX = coords[0];
                int originY = coords[1];
                if (!world.troopAt(originX, originY)) {clientWriter[whoseTurn].println("msg#action.move.no_troop");}
                Troop troop = world.troop(originX, originY);

            	coords = translateCoordinates(parameters[1]);
                int destX = coords[0];
                int destY = coords[1];

                if (!troop.canTravelTo(originX, originY, destX, destY)) {
                	clientWriter[whoseTurn].println("msg#action.move.distance_too_far");
                    return;
                }

                if (!world.troopAt(destX, destY)) {
                    world.moveTroop(originX, originY, destX, destY);
                    troop.movementThisTurn -= Troop.movementDistance(originX, originY, destX, destY); // Deduct movement this round
                    redrawMapPostAction = true;
                } else { // A Troop is already on the destination tile, if its an enemy offer to attack
                	clientWriter[whoseTurn].println("msg#action.move.field_occupied");
                    if (!world.troop(destX, destY).team) {
                    	clientWriter[whoseTurn].println("msg#action.move.wish_attack");
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
                if (!world.troopAt(attX, attY)) {
                	clientWriter[whoseTurn].println("msg#action.attack.no_troop;"+parameters[0]);
                	break;
                }

                troop = world.troop(attX, attY);
                if (!troop.team) {
                    clientWriter[whoseTurn].println("msg#action.attack.attacker_not_yours;"+parameters[0]);
                    return;
                }

            	coords = translateCoordinates(parameters[1]);
                int defX = coords[0];
                int defY = coords[1];
                if (!world.troopAt(defX, defY)) {
                	if (world.isFieldCP(defX, defY)) {
                		attackCP(attX, attY, troop, defX, defY);
                    	return;
                	}
                    clientWriter[whoseTurn].println("msg#action.attack.no_targets;"+parameters[1]);
                	return;
                }

                Troop defender = world.troop(defX, defY);
                if (defender.team) {
                    clientWriter[whoseTurn].println("msg#action.attack.target_same_team;"+parameters[0]);
                    return;
                }
                
                // Execute Attack & Give attack information
                switch (troop.attack(defender)) {
                case SUCCESS:
                    clientWriter[whoseTurn].println(
                    		String.format("msg#action.attack.success;%.2f", troop.health*100, defender.health*100));
                	break;
                case FAILED:
                    clientWriter[whoseTurn].println(
                    		String.format("msg#action.attack.failed;%.2f", troop.health*100));
                	break;
                case INVALID:
                    clientWriter[whoseTurn].println("msg#action.attack.invalid");
                	break;
                case TROOP_DIED:
                    clientWriter[whoseTurn].println(
                        			String.format("msg#action.attack.troopdied;%.2f",defender.health*100));
                    world.removeTroop(attX, attY);
                	break;
                case TARGET_DIED:
                    world.removeTroop(defX, defY);
                    troop.health += 0.action.capture.no_troop04; // Award health for killing
                    if (troop.health > 1.0f) troop.health = 1.0f; // Make sure troop hasnt got more than 1.0f health
                    System.out.printf("",
                            troop.health*100);
                	clientWriter[whoseTurn].println(
                			String.format("msg#action.attack.targetdied;%.2f",troop.health*100));
                    break;
				default:
					String errln = Main.class.getClass().getName()+"#doAction (attack) switch statement defaulted! This wasn't supposed to happen!";
					System.err.println(errln);
					broadcast("A Server error has occured! The server is now exiting.");
					broadcast("("+errln+")");
					System.exit(-1);
					break;
                }

                redrawMapPostAction = true;
                break;
            case "capture":
            	coords = translateCoordinates(parameters[0]);
                int x = coords[0];
                int y = coords[1];
                if (!world.troopAt(x,y)) {
                	clientWriter[whoseTurn].println("msg#action.capture.no_troop");
                }
                troop = world.troop(x, y);
                if (!troop.team || !troop.canCapture) {
                	clientWriter[whoseTurn].println("msg#action.capture.troop_cant_capture");
                	return;
                }
            	
            	coords = translateCoordinates(parameters[1]);
            	int pX = coords[0];
                int pY = coords[1];
                if (!world.isFieldCP(pX, pY)) {
                	clientWriter[whoseTurn].println("msg#action.capture.field_not_cp");
                	return;
                }
                CapturePoint cp = world.capturePoint(pX, pY);

                if (troop.canTravelTo(x, y, pX, pY)) {
                	if (CapturePoint.capturable(cp)) {
                		redrawMapPostAction = true;
                		cp.captured(whoseTurn);
                		
                		clientWriter[whoseTurn].println("msg#action.capture.success");
                		return;
                	} else {
                		clientWriter[whoseTurn].println(
                				String.format("msg#action.capture.point_uncapturable;%.2f;%.2f", cp.health*100, cp.defenseHealth*100));
                		clientWriter[whoseTurn].println("msg#action.capture.wish_attack");
                		if (chooseYesNo()) {
                			doAction("attack "+parameters[0]+" "+parameters[1]);
                		}
                	}
                	return;
                } else {
            		clientWriter[whoseTurn].println("msg#action.capture.troop_cant_travel");
                }
                
            	break;
            case "deploy":
            	coords = translateCoordinates(parameters[0]);
            	x = coords[0];
            	y = coords[1];
            	if (!world.isFieldCP(x,y)) {
            		clientWriter[whoseTurn].println("msg#action.deploy.field_not_cp");
            		return;
            	}
            	
            	cp = world.capturePoint(x,y);
            	if (cp.owner != 1) {
            		clientWriter[whoseTurn].println("msg#action.deploy.capture_point_unowned");
            		return;
            	}
            	
            	int code = world.createTroopByName(parameters[1].toLowerCase(), 1, x, y);
            	switch (code) {
            	case 0:
            		clientWriter[whoseTurn].println("msg#action.deploy.success");
            		break;
            	case 1:
            		clientWriter[whoseTurn].println("msg#action.deploy.invalid_troop");
            		break;
            	case 2:
            		clientWriter[whoseTurn].println("msg#action.deploy.no_valid_field");
            		break;
            	}
            	// TODO: Cost dedeuctionasnoa
            	break;
            case "troop": // Prints out information/stats of a troop at x,y coordinates
            	coords = translateCoordinates(parameters[0]);
                x = coords[0];
                y = coords[1];
                if (world.troopAt(x, y)) {
                    troop = world.troop(x, y);
                    clientWriter[whoseTurn].println("cmd#startTextBlock");
                    clientWriter[whoseTurn].print(Console.Ansi.YELLOW_BACKGROUND);
                    clientWriter[whoseTurn].printf("Stats for %s at %s:%s\n", troop.name, parameters[0], Console.Ansi.RESET);
                    clientWriter[whoseTurn].printf("     Team: %s\n", troop.team ? "You" : "Enemy");
                    clientWriter[whoseTurn].printf("     Health: %.2f%%\n", troop.health*100);
                    clientWriter[whoseTurn].printf("     Movement: %s/%s\n", troop.movementThisTurn, troop.movement);
                    clientWriter[whoseTurn].printf("     Attack Damage: %.2f%%\n", troop.attackDmg*100);
                    clientWriter[whoseTurn].printf("     Attack Absorption: %.2f%%\n", troop.dmgAbsorption*100);
                    clientWriter[whoseTurn].printf("     Defense Absorption: %.2f%%\n", troop.defDmgAbsorption*100);
                    clientWriter[whoseTurn].println("cmd#endtextBlock");
                } else {
                	clientWriter[whoseTurn].println("msg#action.notroop");
                }
                break;
            case "commands":
                clientWriter[whoseTurn].println("cmd#startTextBlock");
                clientWriter[whoseTurn].println("List of Commands:");
                clientWriter[whoseTurn].println("    move   <origin-coords>   <destination-coords> Move a troop");
                clientWriter[whoseTurn].println("    attack <attacker-coords> <defender-coords>    Attack another troop");
                clientWriter[whoseTurn].println("    troop  <troop-coords> Get Information about a troop");
                clientWriter[whoseTurn].println("cmd#endtextBlock");
                break;
            default:
                break;
        }
    }
    
    // Own function in attempt to declutter the doAction function
    public static void attackCP(int attX, int attY, Troop attacker, int defX, int defY) {
    	CapturePoint point = world.capturePoint(defX, defY);
    	ActionResult result = attacker.attackCP(point);
    	
    	switch (result) {
    	case POINT_CAPTURABLE:
    		clientWriter[whoseTurn].println("msg#action.point_capturable");
    		clientWriter[whoseTurn].println(
    				String.format("msg#player.troop.newhealth;%.2f", attacker.health*100));
        	redrawMapPostAction = true;
    		break;
    	case TROOP_DIED:
            System.out.printf("Your troop died! Capture Point is at %.2f%% core health and %.2f%% defense health\n",
                    point.health*100, point.defenseHealth*100);
            clientWriter[whoseTurn].println(
            		String.format("msg#action.attack_cp.troopdied;%.2f;%.2f", point.health*100, point.defenseHealth*100));
            world.removeTroop(attX, attY);
        	redrawMapPostAction = true;
    		break;
    	case SUCCESS:
    		clientWriter[whoseTurn].println(
    				String.format("msg#action.attack_cp.success;%.2f;%.2f", point.health*100, point.defenseHealth*100));
    		clientWriter[whoseTurn].println(
    				String.format("msg#player.troop.newhealth;%.2f", attacker.health*100));
    		break;
    	case FAILED:
    		clientWriter[whoseTurn].println("msg#action.attack_cp.failed");
    		break;
    	default:
    		String errln = Main.class.getClass().getName()+"#doAction -> "+
    	    		Main.class.getClass().getName()+"#attackCP ActionResult message switch statement defaulted! This wasn't supposed to happen!";
			System.err.println(errln);
			broadcast("A Server error has occured! The server is now exiting.");
			broadcast("("+errln+")");
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
    	clientWriter[whoseTurn].println("msg#menu.yesno");
        String in;
		try {
			in = getInput(whoseTurn);
	        if (in.startsWith("y")) {
	            return true;
	        } else if (in.startsWith("n")) {
	            return false;
	        } else {
	            clientWriter[whoseTurn].println("msg#menu.yesno.invalid");
	            return chooseYesNo();
	        }
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
    }
    
    public static String getInput(int client) throws IOException {
    	clientWriter[client].println("cmd#reqInput");
    	return clientReader[client].readLine();
    }
    
    public static void broadcast(String msg) {
    	for (PrintWriter writer : clientWriter) {
    		writer.println(msg);
    	}
    }
}
