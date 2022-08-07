package de.feckert.congame.client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Scanner;

import org.json.JSONObject;

import de.feckert.congame.common.World;

import javax.sound.sampled.*;

@SuppressWarnings("CanBeFinal")
public class Client {
	public static boolean running;
	public static World world;
	public static int playerNum = -1;
	public static Socket server;
	public static PrintWriter out;
	public static ObjectInputStream in;
	public static Scanner clientIn = new Scanner(System.in);
	public static JSONObject messageStrings;
	
	@SuppressWarnings("BusyWait")
	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException, LineUnavailableException, UnsupportedAudioFileException {
		for (String arg : args) {
			if (arg.matches("testWorldGen")) {
				world = new World(80, 46);
				world.generate();
				Console.drawMap();
				return;
			} else if (arg.matches("testmode1")) {
				String b64 = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Client.class.getResource("/de/feckert/congame/b64.txt")).getPath())));byte[] bin = Base64.getDecoder().decode(b64);AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(bin));DataLine.Info info = new DataLine.Info(Clip.class, audioInputStream.getFormat());Clip clip = (Clip) AudioSystem.getLine(info);clip.open(audioInputStream);clip.start();while (clip.isOpen()) {}
				return;
			}
		}

		init();

		int conAttempts = 0;
		while (true) {
			conAttempts++;
			try {
				System.out.print("IP> ");
				String ip = clientIn.nextLine();
				System.out.print("PORT> ");
				int port  = clientIn.nextShort();
				server = new Socket(ip, port);
			} catch (IOException e) {
				System.out.printf("Failed to reach server (%s attempts)\r", conAttempts);
				if (conAttempts > 10) System.exit(-1);
				Thread.sleep(1000);
				continue;
			}
			break;
		}
		out = new PrintWriter(server.getOutputStream(), true);
		in = new ObjectInputStream(server.getInputStream());

		out.println("cmd#ready");

		String msg = null;
		try {
			running = true;
			while (running) {
				Object rmsg = in.readObject();
				if (rmsg instanceof String) {
					msg = (String) rmsg;
					String type = msg.split("#")[0];
					String cont = msg.split("#")[1];

					switch (type) {
						case "msg":
							String[] split = cont.split(";");
							String id = split[0];
							if (split.length > 1) {
								split = Arrays.copyOfRange(split, 1, split.length);
								System.out.println(String.format(fetchMessage(id), (Object[]) split));
							} else {
								System.out.println(fetchMessage(id));
							}
							break;
						case "raw":
							System.out.println(cont);
							break;
						case "cmd":
							procCommand(cont);
							break;
						case "end":
							running = false;
							break;
					}

				} else if (rmsg instanceof World) {
					world = (World) rmsg;
					Console.drawMap();
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			System.out.println("MSG = " + msg);
		}
	}

	public static void procCommand(String command) {
		if (command.matches("reqInput")) {
			System.out.print("> ");
			out.println(clientIn.nextLine());
		} else if (command.startsWith("assignPlayerNum:")) {
			playerNum = Integer.parseInt(command.split(":")[1]);
		} else {
			System.out.printf("ERROR: Received invalid command \"%s\" from server!\n", command);
		}
	}
	
	public static void init() {
		try {
			String lines = new String(Files.readAllBytes(
					Paths.get(Objects.requireNonNull(Client.class.getResource("/de/feckert/congame/strings.json")).getPath())));
			messageStrings = new JSONObject(lines);
		} catch (IOException | NullPointerException e) {
			System.out.println("Failed to load message strings (expected at de.feckert.congame.strings.json)");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static String fetchMessage(String id) {
		return messageStrings.getString(id);
	}

	// Toan is stored in the balls, fenders dont have balls so fenders dont have toan
}