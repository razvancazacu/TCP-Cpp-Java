package javatcp.jsoncom;

import java.io.*;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

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

	public void receiveJSON() throws IOException {

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
		Paper paperObjPaper = new Paper();
		Gson gson = new Gson();
		gson.fromJson(receivedMessageString, Paper.class);
		System.out.println(paperObjPaper.toString());

	}

	public void sendJSON() throws IOException {
		OutputStream out = socket.getOutputStream();
		DataOutputStream o = new DataOutputStream(out);

		Gson gson = new Gson();
		String jsonString = gson.toJson(new Paper(340,234));
//		JsonObject jsonObject3 = gson.fromJson(jsonString, JsonObject.class);
//		System.out.println(jsonObject3.toString());
		jsonString = jsonString.length() + jsonString;
//		o.write(sendingJson.getBytes());
		o.write(jsonString.getBytes());
		out.flush();
	}

	public static void main(String[] args) {
		ClientTcpJson client = new ClientTcpJson();
		try {
			client.connect("localhost", 8888);

			client.sendJSON();
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