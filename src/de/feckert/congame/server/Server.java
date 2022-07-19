package de.feckert.congame.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import de.feckert.congame.client.Console;
import de.feckert.congame.common.CapturePoint;
import de.feckert.congame.common.World;
import de.feckert.congame.common.troops.Troop;
import de.feckert.congame.util.ActionResult;
import de.feckert.congame.util.Logger;

public class Server {
	public static ServerSocket socket;
	
	public static Socket[] clients;
	public static ObjectOutputStream[] ooStreams;
	public static BufferedReader[] clientReader;
	
	public static int roundNumber;
	public static World world;
	public static int whoseTurn;
	public static int oppositePlayer;
	public static boolean redrawMapPostAction = false;

	public static final Logger logger = Logger.create("CON_GAME_SERVER");

	public static void main(String[] args) {
		logger.info("Server started!");
		try {
			// Setup
			clients = new Socket[2];
			ooStreams = new ObjectOutputStream[clients.length];
			clientReader = new BufferedReader[clients.length];
		
			socket = new ServerSocket(Integer.parseInt(args[0]));
			
			// Accept Players
			for (int i = 0; i < clients.length; i++) {
				clients[i] = socket.accept();
				ooStreams[i]     = new ObjectOutputStream(clients[i].getOutputStream());
				clientReader[i]  = new BufferedReader(new InputStreamReader(clients[i].getInputStream()));
				logger.infof("Accepted client %s\n", i);
			}
			
			startGame();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void startGame() throws IOException {
		whoseTurn = 0;
		oppositePlayer = 1;
		roundNumber = 0;

		logger.info("Generating world...");
		world = new World();
		world.generate(12, 12);
		logger.info("World generated, starting game!");

		// Wait until both clients ready
		for (int i = 0; i < clients.length; i++) {
			if (clientReader[i].readLine().matches("cmd#ready")) {
				ooStreams[i].writeObject("cmd#assignPlayerNum:"+i);
				continue;
			}
		}

		broadcast("Round: "+roundNumber);
		redrawWorld(); // Send initial World to Clients

		int lastTurn = whoseTurn;
		while (world.winningPlayer() == -1) {
			broadcast(String.format("Player %s's turn", whoseTurn));
			while (whoseTurn == lastTurn) {
				doAction(getInput(whoseTurn));
				if (redrawMapPostAction) {
					redrawWorld();
					redrawMapPostAction = false;
				}
			}
			ooStreams[oppositePlayer].writeObject("cmd#endTurn");
			lastTurn = whoseTurn;

			// Make sure to exit game loop if someone has won before next player gets to play
			if (world.winningPlayer() != -1) break;

			broadcast(String.format("Player %s's turn", whoseTurn));
			while (whoseTurn == lastTurn) {
				doAction(getInput(whoseTurn));
				if (redrawMapPostAction) {
					redrawWorld();
					redrawMapPostAction = false;
				}
			}
			ooStreams[oppositePlayer].writeObject("cmd#endTurn");
			lastTurn = whoseTurn;
			
			roundNumber++;
			broadcast("Round: "+roundNumber);
		}
		broadcast(String.format("Player %s won after %s rounds!", world.winningPlayer(), roundNumber));
	}
	
	 /**
     * Execute a players action
     * */
    public static void doAction(String action) throws IOException {
        // Format command
        String[] split = action.split(" ");
        String   invoke = split[0];
        String[] parameters = Arrays.copyOfRange(split, 1, split.length);

        switch (invoke) {
            case "finish":
            	oppositePlayer = whoseTurn;
                whoseTurn = whoseTurn == 0 ? 1 : 0;
                break;
            case "move":
            	int[] coords;

				if (parameters.length != 2) {
					ooStreams[whoseTurn].writeObject("msg#action.move.missing_parameters");
					return;
				}

            	coords = translateCoordinates(parameters[0]);
                int originX = coords[0];
                int originY = coords[1];
                if (!world.troopAt(originX, originY)) {ooStreams[whoseTurn].writeObject("msg#action.no_troop");}
                Troop troop = world.troop(originX, originY);

            	coords = translateCoordinates(parameters[1]);
                int destX = coords[0];
                int destY = coords[1];

                if (!troop.canTravelTo(originX, originY, destX, destY)) {
                	ooStreams[whoseTurn].writeObject("msg#action.move.distance_too_far");
                    return;
                }

                if (!world.troopAt(destX, destY)) {
                    world.moveTroop(originX, originY, destX, destY);
                    troop.movementThisTurn -= Troop.movementDistance(originX, originY, destX, destY); // Deduct movement this round
                    redrawMapPostAction = true;
                } else { // A Troop is already on the destination tile, if its an enemy offer to attack
                	ooStreams[whoseTurn].writeObject("msg#action.move.field_occupied");
                    if (world.troop(destX, destY).team != whoseTurn) {
                    	ooStreams[whoseTurn].writeObject("msg#action.move.wish_attack");
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

				if (parameters.length != 2) {
					ooStreams[whoseTurn].writeObject("msg#action.attack.missing_parameters");
					return;
				}

                // Attacker
            	coords = translateCoordinates(parameters[0]);
                int attX = coords[0];
                int attY = coords[1];
                if (!world.troopAt(attX, attY)) {
                	ooStreams[whoseTurn].writeObject("msg#action.attack.no_troop;"+parameters[0]);
                	break;
                }

                troop = world.troop(attX, attY);
                if (troop.team != whoseTurn) {
                    ooStreams[whoseTurn].writeObject("msg#action.attack.attacker_not_yours;"+parameters[0]);
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
                    ooStreams[whoseTurn].writeObject("msg#action.attack.no_targets;"+parameters[1]);
                	return;
                }

                Troop defender = world.troop(defX, defY);
                if (defender.team == whoseTurn) {
                    ooStreams[whoseTurn].writeObject("msg#action.attack.target_same_team;"+parameters[0]);
                    return;
                }
                
                // Execute Attack & Give attack information
                switch (troop.attack(defender)) {
                case SUCCESS:
                    ooStreams[whoseTurn].writeObject(
                    		String.format("msg#action.attack.success;%.2f%%", troop.health*100, defender.health*100));
                	ooStreams[oppositePlayer].writeObject(
                			String.format("msg#opplayer.troop_attacked.success;%s;%.2f%%", parameters[1], defender.health*100, troop.health*100));
                	break;
                case FAILED:
                    ooStreams[whoseTurn].writeObject(
                    		String.format("msg#action.attack.failed;%.2f%%", troop.health*100));
                	ooStreams[oppositePlayer].writeObject(
                			String.format("msg#opplayer.troop_attacked.failed;%s;%.2f%%", parameters[1], troop.health*100));
                	break;
                case INVALID:
                    ooStreams[whoseTurn].writeObject("msg#action.attack.invalid");
                	break;
                case TROOP_DIED:
                    ooStreams[whoseTurn].writeObject(
                        			String.format("msg#action.attack.troopdied;%.2f%%",defender.health*100));
                	ooStreams[oppositePlayer].writeObject(
                			String.format("msg#opplayer.troop_attacked.attacker_died;%s;%.2f%%", parameters[1], defender.health*100));
                    world.removeTroop(attX, attY);
                	break;
                case TARGET_DIED:
                    world.removeTroop(defX, defY);
                    troop.health += 0.04f; // Award health for killing
                    if (troop.health > 1.0f) troop.health = 1.0f; // Make sure troop hasnt got more than 1.0f health
                	ooStreams[whoseTurn].writeObject(
                			String.format("msg#action.attack.targetdied;%.2f%%",troop.health*100));
                	ooStreams[oppositePlayer].writeObject(
                			String.format("msg#opplayer.troop_attacked.died;%s;%.2f%%", parameters[1], troop.health*100));
                    break;
				default:
					String errln = Server.class.getClass().getName()+"#doAction (attack) switch statement defaulted! This wasn't supposed to happen!";
					System.err.println(errln);
					broadcast("A Server error has occured! The server is now exiting.");
					broadcast("("+errln+")");
					System.exit(-1);
					break;
                }

                redrawMapPostAction = true;
                break;
            case "capture":
				if (parameters.length != 2) {
					ooStreams[whoseTurn].writeObject("msg#action.capture.missing_parameters");
					return;
				}

            	coords = translateCoordinates(parameters[0]);
                int x = coords[0];
                int y = coords[1];
                if (!world.troopAt(x,y)) {
                	ooStreams[whoseTurn].writeObject("msg#action.capture.no_troop");
                }
                troop = world.troop(x, y);
                if (troop.team != whoseTurn || !troop.canCapture) {
                	ooStreams[whoseTurn].writeObject("msg#action.capture.troop_cant_capture");
                	return;
                }
            	
            	coords = translateCoordinates(parameters[1]);
            	int pX = coords[0];
                int pY = coords[1];
                if (!world.isFieldCP(pX, pY)) {
                	ooStreams[whoseTurn].writeObject("msg#action.capture.field_not_cp");
                	return;
                }
                CapturePoint cp = world.capturePoint(pX, pY);

                if (troop.canTravelTo(x, y, pX, pY)) {
                	if (CapturePoint.capturable(cp)) {
                		redrawMapPostAction = true;
                		int prevOwner = cp.owner;
                		cp.captured(whoseTurn);
                		
                		ooStreams[whoseTurn].writeObject("msg#action.capture.success");
                		if (prevOwner == oppositePlayer) {
                			ooStreams[oppositePlayer].writeObject(
                					String.format("msg#opplayer.owned_cp.captured;%s",parameters[1]));
                		} else {
                			ooStreams[oppositePlayer].writeObject(
                					String.format("msg#opplayer.unowned_cp.captured;%s",parameters[1]));
                		}
                		return;
                	} else {
                		ooStreams[whoseTurn].writeObject(
                				String.format("msg#action.capture.point_uncapturable;%.2f%%;%.2f%%", cp.health*100, cp.defenseHealth*100));
                		ooStreams[whoseTurn].writeObject("msg#action.capture.wish_attack");
                		if (chooseYesNo()) {
                			doAction("attack "+parameters[0]+" "+parameters[1]);
                		}
                	}
                	return;
                } else {
            		ooStreams[whoseTurn].writeObject("msg#action.capture.troop_cant_travel");
                }
                
            	break;
            case "deploy":
				if (parameters.length != 2) {
					ooStreams[whoseTurn].writeObject("msg#action.deploy.missing_parameters");
					return;
				}

            	coords = translateCoordinates(parameters[0]);
            	x = coords[0];
            	y = coords[1];
            	if (!world.isFieldCP(x,y)) {
            		ooStreams[whoseTurn].writeObject("msg#action.deploy.field_not_cp");
            		return;
            	}
            	
            	cp = world.capturePoint(x,y);
            	if (cp.owner != whoseTurn) {
            		ooStreams[whoseTurn].writeObject("msg#action.deploy.capture_point_unowned");
            		return;
            	}
            	
            	int code = world.createTroopByName(parameters[1].toLowerCase(), whoseTurn, x, y);
            	switch (code) {
            	case 0:
            		ooStreams[whoseTurn].writeObject("msg#action.deploy.success");
            		ooStreams[oppositePlayer].writeObject(
            				String.format("msg#opplayer.enemy.deployed_troop;%s;%s", parameters[1].toLowerCase().replace(";", ":"), parameters[0]));
            		break;
            	case 1:
            		ooStreams[whoseTurn].writeObject("msg#action.deploy.invalid_troop");
            		break;
            	case 2:
            		ooStreams[whoseTurn].writeObject("msg#action.deploy.no_valid_field");
            		break;
            	}
            	redrawMapPostAction = true;
            	// TODO: Cost dedeuctionasnoa
            	break;
            case "troop": // Prints out information/stats of a troop at x,y coordinates
				if (parameters.length == 0) {
					ooStreams[whoseTurn].writeObject("msg#action.troop.missing_parameters");
					return;
				}

            	coords = translateCoordinates(parameters[0]);
                x = coords[0];
                y = coords[1];
                if (world.troopAt(x, y)) {
                    troop = world.troop(x, y);
                    ooStreams[whoseTurn].writeObject(String.format("raw#Stats for %s at %s:", troop.name, parameters[0]));
                    ooStreams[whoseTurn].writeObject(String.format("raw#     Team: %s", troop.team == whoseTurn ? "You" : "Enemy"));
                    ooStreams[whoseTurn].writeObject(String.format("raw#     Health: %.2f%%", troop.health*100));
                    ooStreams[whoseTurn].writeObject(String.format("raw#     Movement: %s/%s", troop.movementThisTurn, troop.movement));
                    ooStreams[whoseTurn].writeObject(String.format("raw#     Attack Damage: %.2f%%", troop.attackDmg*100));
                    ooStreams[whoseTurn].writeObject(String.format("raw#     Attack Absorption: %.2f%%", troop.dmgAbsorption*100));
                    ooStreams[whoseTurn].writeObject(String.format("raw#     Defense Absorption: %.2f%%", troop.defDmgAbsorption*100));
                } else {
                	ooStreams[whoseTurn].writeObject("msg#action.no_troop");
                }
                break;
            case "commands":
                ooStreams[whoseTurn].writeObject("raw#List of Commands:");
                ooStreams[whoseTurn].writeObject("raw#    move    <origin-coords>   <destination-coords> Move a troop");
                ooStreams[whoseTurn].writeObject("raw#    attack  <attacker-coords> <defender-coords>    Attack another troop");
				ooStreams[whoseTurn].writeObject("raw#    capture <attacker-coords> <cp-coords>          Attempt to capture a point");
				ooStreams[whoseTurn].writeObject("raw#    deploy  <deploy-coords> <troop-name>           Attempt to deploy specified Troop at specified (owned) CP");
                ooStreams[whoseTurn].writeObject("raw#    troop   <troop-coords> Get Information about a troop");
                break;
			case "testCommand":
				// leave this here for testing in future
				break;
            default:
                break;
        }
    }
    
    // Own function in attempt to declutter the doAction function
    public static void attackCP(int attX, int attY, Troop attacker, int defX, int defY) {
		try {
			CapturePoint point = world.capturePoint(defX, defY);
			ActionResult result = attacker.attackCP(point);

			switch (result) {
				case POINT_CAPTURABLE:
					ooStreams[whoseTurn].writeObject("msg#action.point_capturable");
					ooStreams[whoseTurn].writeObject(
							String.format("msg#player.troop.newhealth;%.2f%%", attacker.health * 100));
					ooStreams[oppositePlayer].writeObject(
							String.format("msg#opplayer.cp.made_capturable;%s", coordString(defX, defY)));
					redrawMapPostAction = true;
					break;
				case TROOP_DIED:
					ooStreams[whoseTurn].writeObject(
							String.format("msg#action.attack_cp.troopdied;%.2f%%;%.2f%%", point.health * 100, point.defenseHealth * 100));
					ooStreams[oppositePlayer].writeObject(
							String.format("msg#opplayer.cp.att_troop_died;%s", coordString(defX, defY)));
					ooStreams[oppositePlayer].writeObject(
							String.format("msg#action.attack_cp.success;%.2f%%;%.2f%%", point.health * 100, point.defenseHealth * 100));

					world.removeTroop(attX, attY);
					redrawMapPostAction = true;
					break;
				case SUCCESS:
					ooStreams[whoseTurn].writeObject(
							String.format("msg#action.attack_cp.success;%.2f%%;%.2f%%", point.health * 100, point.defenseHealth * 100));
					ooStreams[whoseTurn].writeObject(
							String.format("msg#player.troop.newhealth;%.2f%%", attacker.health * 100));
					ooStreams[oppositePlayer].writeObject(
							String.format("msg#opplayer.cp.att_success;%s;%.2f%%;%.2f%%", coordString(defX, defY),
									point.health * 100, point.defenseHealth * 100));
					ooStreams[oppositePlayer].writeObject(
							String.format("msg#opplayer.cp.att_success2;%.2f%%", attacker.health * 100));
					break;
				case FAILED:
					ooStreams[whoseTurn].writeObject("msg#action.attack_cp.failed");
					ooStreams[whoseTurn].writeObject(
							String.format("msg#action.attack_cp.failed2;%.2f%%", attacker.health * 100));
					ooStreams[oppositePlayer].writeObject(
							String.format("msg#opplayer.cp.att_failed;%s", coordString(defX, defY)));
					ooStreams[oppositePlayer].writeObject(
							String.format("msg#opplayer.cp.att_success2;%.2f%%", attacker.health * 100));
					break;
				default:
					String errln = Server.class.getClass().getName() + "#doAction -> " +
							Server.class.getClass().getName() + "#attackCP ActionResult message switch statement defaulted! This wasn't supposed to happen!";
					System.err.println(errln);
					broadcast("A Server error has occured! The server is now exiting.");
					broadcast("(" + errln + ")");
					System.exit(-1);
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static String coordString(int x, int y) {
    	return x+";"+((char) 65+y);
    }
    
    public static int[] translateCoordinates(String raw) {
		try {
			return new int[]{
					Integer.parseInt(raw.split(";")[0]),
					((byte) raw.split(";")[1].toCharArray()[0]) - 65
			};
		} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
			logger.errf("Invalid format for translating coordinates: %s\n", raw);
			return new int[]{
					-1,
					-1
			};
		}
    }

    public static boolean chooseYesNo() {
		try {
			ooStreams[whoseTurn].writeObject("msg#menu.yesno");
			String in;
			in = getInput(whoseTurn);
			if (in.startsWith("y")) {
				return true;
			} else if (in.startsWith("n")) {
				return false;
			} else {
				ooStreams[whoseTurn].writeObject("msg#menu.yesno.invalid");
				return chooseYesNo();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
    }
    
    public static String getInput(int client) throws IOException {
    	ooStreams[client].writeObject("cmd#reqInput");
    	return clientReader[client].readLine();
    }
    
    public static void redrawWorld() throws IOException {
    	for (int i = 0; i < clients.length; i++) {
			ooStreams[i].reset();
    		ooStreams[i].writeObject(world);
    	}
    }
    
    public static void broadcast(String msg) throws IOException {
    	for (ObjectOutputStream oos : ooStreams) {
    		oos.writeObject("raw#"+msg);
    	}
    }
}
