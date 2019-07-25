package javatcp.jsoncom;

import java.io.*;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

public class ClientTcpJson {

	private String host;
	private int port;
	private Socket socket;
	private final String DEFAULT_HOST = "localhost";

	public void connect(String host, int port) throws IOException {
		this.host = host;
		this.port = port;
		socket = new Socket(host, port);
		System.out.println("Client has been connected..");
	}

	/**
	 * use the JSON Protocol to receive a json object as from the client and
	 * reconstructs that object
	 *
	 * @return JSONObejct with the same state (data) as the JSONObject the client
	 *         sent as a String msg.
	 * @throws IOException
	 */
	public JSONObject receiveJSON() throws IOException {
		InputStream in = socket.getInputStream();
//		ObjectInputStream i = new ObjectInputStream(in);
		DataInputStream inputStream = new DataInputStream(in);
//		String line = null; 
//		try {
//			line = (String) i.readObject();
		byte[] messageLength = null;
		byte[] readedByte = new byte[1];
		String stringIntLengthString = new String();
		char inChar;
		do {
			inputStream.read(readedByte, 0, 1);
			stringIntLengthString = stringIntLengthString + new String(readedByte, StandardCharsets.UTF_8);
			inChar = stringIntLengthString.charAt(stringIntLengthString.length() - 1);
		} while (inChar != '{');
		stringIntLengthString = stringIntLengthString.substring(0, stringIntLengthString.length() - 1);
		int bufferLenght = Integer.parseInt(stringIntLengthString);
		byte[] bufferedMessage = new byte[bufferLenght - 1];
		int count = inputStream.read(bufferedMessage);
		String receivedMessageString = new String('{' + new String(bufferedMessage));
		System.out.println("|Bytes: " + count + " |Received message: " + receivedMessageString);
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		JSONObject jsonObject = new JSONObject(receivedMessageString);
		System.out.println("Got from server on port " + socket.getPort() + " " + jsonObject.get("key").toString());
		System.out.println("Received JSON: " + jsonObject.toString());
		/*
		 * Decoding
		 */

		return jsonObject;

	}

	public void sendJSON(JSONObject jsonObject) throws IOException {
		JSONObject jsonObject2 = new JSONObject();

		jsonObject2.put("key", new Paper(250, 333));
		jsonObject2.put("key1", new Paper(256, 333));
		OutputStream out = socket.getOutputStream();
		DataOutputStream o = new DataOutputStream(out);
//		o.writeObject(jsonObject2.toString());
//		o.writeBytes(jsonObject2.toString());
//		o.writeChars(jsonObject2.toString());
//		o.writeUTF(jsonObject2.toString());
//		o.writeBytes("");
//		o.writeUTF(jsonObject2.toString());
//		byte[] byteBuf = {'1','2','3','4'};
//		o.write(byteBuf);
		o.write(jsonObject2.toString().getBytes());
//		try {
//			Thread.sleep(100000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		out.flush();

		Paper paperObjPaper = (Paper) jsonObject.get("key");
		System.out.println("Sent to server: " + " " + jsonObject2.get("key").toString());
		System.out.println("JSON String: " + jsonObject2.toString());
		System.out.println("JSON key Object: " + paperObjPaper.toString());
	}

	public static void main(String[] args) {
		ClientTcpJson client = new ClientTcpJson();
		try {
			client.connect("localhost", 8888);
			// For JSON call sendJSON(JSON json) & receiveJSON();
			JSONObject jsonObject2 = new JSONObject();
			jsonObject2.put("key", new Paper(250, 333));

			client.sendJSON(jsonObject2);
			client.receiveJSON();
		} catch (ConnectException e) {
			System.err.println(client.host + " connect refused");
			return;
		} catch (UnknownHostException e) {
			System.err.println(client.host + " Unknown host");
			client.host = client.DEFAULT_HOST;
			return;
		} catch (NoRouteToHostException e) {
			System.err.println(client.host + " Unreachable");
			return;
		} catch (IllegalArgumentException e) {
			System.err.println(client.host + " wrong port");
			return;
		} catch (IOException e) {
			System.err.println(client.host + ' ' + e.getMessage());
			System.err.println(e);
		} finally {
			try {
				client.socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}