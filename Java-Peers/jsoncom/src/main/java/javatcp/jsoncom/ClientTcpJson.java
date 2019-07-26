package javatcp.jsoncom;

import java.io.*;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import com.google.gson.Gson;

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
		DataInputStream inputStream = new DataInputStream(in);

		byte[] readedByte = new byte[1];
		String stringLengthBuilder = new String();
		char inChar = '1';
		int count = 1;
		while (inChar != '{' && count != 0) {
			count = inputStream.read(readedByte, 0, 1);
			stringLengthBuilder = stringLengthBuilder + new String(readedByte, StandardCharsets.UTF_8);
			inChar = stringLengthBuilder.charAt(stringLengthBuilder.length() - 1);
		}

		stringLengthBuilder = stringLengthBuilder.substring(0, stringLengthBuilder.length() - 1);
		int bufferLenght = Integer.parseInt(stringLengthBuilder);
		byte[] receivedMsgBuf = new byte[bufferLenght - 1];
		count = inputStream.read(receivedMsgBuf) + 1;
		String receivedMessageString = new String('{' + new String(receivedMsgBuf));
		System.out.println("\nReceived message (" + "bytes " + count + "): " + receivedMessageString);
		System.out.println("Transforming into JSON \n----------------------");
		JSONObject jsonObject = new JSONObject(receivedMessageString);
		System.out.println("Got from server on port " + socket.getPort() + " " + jsonObject.toString());
		System.out.println("Received JSON: " + jsonObject.toString());
		Paper paperObjPaper = new Paper();
		Gson gson = new Gson();
		gson.fromJson(jsonObject.toString(), Paper.class);
		System.out.println(paperObjPaper.toString());
		return jsonObject;

	}

	public void sendJSON(JSONObject jsonObject) throws IOException {
		JSONObject jsonObject2 = new JSONObject();

		jsonObject2.put("x", 240);
		jsonObject2.put("y", 233);
		OutputStream out = socket.getOutputStream();
		DataOutputStream o = new DataOutputStream(out);

		String sendingJson = new String(jsonObject2.toString().length() + jsonObject2.toString());
		
		o.write(sendingJson.getBytes());
		out.flush();
		System.out.println("Sent to server: " + " " + jsonObject2.toString());
		System.out.println("JSON String: " + jsonObject2.toString());
	}

	public static void main(String[] args) {
		ClientTcpJson client = new ClientTcpJson();
		try {
			client.connect("localhost", 8888);
			// For JSON call sendJSON(JSON json) & receiveJSON();
			JSONObject jsonObject2 = new JSONObject();
			jsonObject2.put("x", 250);
			jsonObject2.put("y", 353);

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