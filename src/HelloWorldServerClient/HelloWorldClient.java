package fr.tp.inf112.projects.robotsim.app;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloWorldClient {

	private static final Logger LOGGER = Logger.getLogger(HelloWorldClient.class.getName());

	private final String host;
	private final int port;

	public HelloWorldClient() {
		this("localhost", HelloWorldServer.DEFAULT_PORT);
	}

	public HelloWorldClient(final String host, final int port) {
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String sendMessage(final String message) throws IOException {
		LOGGER.info("Connecting to server at " + host + ":" + port + " to send message: " + message);
		try (
			final Socket socket = new Socket(host, port);
			final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			final ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		) {
			out.flush();
			out.writeObject(message);
			out.flush();

			final Object response = in.readObject();
			LOGGER.info("Received reply from server: " + response);
			return response != null ? response.toString() : null;
		} catch (final ClassNotFoundException ex) {
			LOGGER.log(Level.SEVERE, "Failed to deserialize server response", ex);
			throw new IOException("Failed to deserialize response", ex);
		}
	}

	public static void main(String[] args) {
		final String message = (args.length > 0) ? args[0] : "my message";
		final String host = (args.length > 1) ? args[1] : "localhost";
		int port = HelloWorldServer.DEFAULT_PORT;
		if (args.length > 2) {
			try {
				port = Integer.parseInt(args[2]);
			} catch (final NumberFormatException ex) {
				LOGGER.warning("Invalid port parameter, using default: " + HelloWorldServer.DEFAULT_PORT);
			}
		}

		final HelloWorldClient client = new HelloWorldClient(host, port);
		try {
			final String response = client.sendMessage(message);
			LOGGER.info("Response received: " + response);
			System.out.println(response);
		} catch (final IOException ex) {
			LOGGER.log(Level.SEVERE, "Could not send message to server", ex);
		}
	}
}
