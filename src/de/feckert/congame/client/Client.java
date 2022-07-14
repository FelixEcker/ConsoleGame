package de.feckert.congame.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.json.JSONObject;

import de.feckert.congame.common.World;

public class Client {
	public static World world;
	public static Socket server;
	public static PrintWriter out;
	public static BufferedReader in;
	public static ObjectInputStream oiStream;
	
	public static JSONObject messageStrings;
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		init();
		
		server = new Socket("localhost", 3103);
		out = new PrintWriter(server.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(server.getInputStream()));
        oiStream = new ObjectInputStream(server.getInputStream());
        
        while (true) {
        	String msg = in.readLine();
        	String type = msg.split("#")[0];
        	String cont = msg.split("#")[1];
        	
        	switch (type) {
        	case "msg":
        		String[] split = cont.split(";");
        		String id = split[0];
        		split = Arrays.copyOfRange(split, 1, split.length-1);
        		System.out.println(String.format(fetchMessage(id), (Object[])split));
        		break;
        	case "raw":
        		System.out.println(cont);
        		break;
        	case "cmd":
        		break;
        	}
        }
	}
	
	public static void init() {
		// TODO: Load Message Strings
	}
	
	public static String fetchMessage(String id) {
		return messageStrings.getString(id);
	}
}