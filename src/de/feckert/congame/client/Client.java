package de.feckert.congame.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.json.JSONObject;

import de.feckert.congame.common.World;

public class Client {
	public static World world;
	public static Socket server;
	public static PrintWriter out;
	//public static BufferedReader in;
	public static ObjectInputStream in;
	
	public static JSONObject messageStrings;
	
	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		init();

		int conAttempts = 0;
		while (true) {
			conAttempts++;
			try {
				server = new Socket("localhost", 3103);
			} catch (IOException e){
				System.out.printf("Failed to reach server (%s attempts)\r", conAttempts);
				if (conAttempts > 10) System.exit(-1);
				Thread.sleep(1000);
				continue;
			}
			break;
		}
		out = new PrintWriter(server.getOutputStream(), true);
		in  = new ObjectInputStream(server.getInputStream());

		out.println("cmd#ready");
        while (true) {
			Object rmsg = in.readObject();
			if (rmsg instanceof String) {
				String msg = (String) rmsg;
				String type = msg.split("#")[0];
				String cont = msg.split("#")[1];

				switch (type) {
					case "msg":
						String[] split = cont.split(";");
						String id = split[0];
						split = Arrays.copyOfRange(split, 1, split.length - 1);
						System.out.println(String.format(fetchMessage(id), (Object[]) split));
						break;
					case "raw":
						System.out.println(cont);
						break;
					case "cmd":
						procCommand(cont);
						break;
				}

			} else if (rmsg instanceof World) {
				world = (World) rmsg;
				Console.drawMap();
			}
		}
	}

	public static void procCommand(String command) throws IOException, ClassNotFoundException {
		switch (command) {

		}
	}
	
	public static void init() {
		try {
			String lines = new String(Files.readAllBytes(
					Paths.get(Client.class.getResource("/de/feckert/congame/strings.json").getPath())));
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
}