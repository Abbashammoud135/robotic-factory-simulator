package fr.tp.inf112.projects.robotsim.app;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloWorldServer implements Runnable {

	private static final Logger LOGGER = Logger.getLogger(HelloWorldServer.class.getName());

	public static final int DEFAULT_PORT = 8080;

	private final int port;

	private ServerSocket serverSocket;

	private volatile boolean running;

	public HelloWorldServer() {
		this(DEFAULT_PORT);
	}

	public HelloWorldServer(final int port) {
		this.port = port;
		this.running = false;
	}

	public int getPort() {
		return port;
	}

	public boolean isRunning() {
		return running;
	}

	public synchronized void start() throws IOException {
		if (!running) {
			serverSocket = new ServerSocket(port);
			running = true;
			LOGGER.info("HelloWorldServer started on port " + port + ".");
			final Thread serverThread = new Thread(this, "HelloWorldServer-Listener");
			serverThread.setDaemon(true);
			serverThread.start();
		}
	}

	public synchronized void stop() {
		if (running) {
			running = false;
			if (serverSocket != null && !serverSocket.isClosed()) {
				try {
					serverSocket.close();
				} catch (final IOException ex) {
					LOGGER.log(Level.WARNING, "Error closing server socket", ex);
				}
			}
			LOGGER.info("HelloWorldServer stopped.");
		}
	}

	@Override
	public void run() {
		while (running) {
			try {
				final Socket clientSocket = serverSocket.accept();
				LOGGER.info("Connection accepted from " + clientSocket.getRemoteSocketAddress());
				final Thread worker = new Thread(() -> handleClient(clientSocket), "HelloWorldServer-Worker");
				worker.setDaemon(true);
				worker.start();
			} catch (final IOException ex) {
				if (running) {
					LOGGER.log(Level.SEVERE, "Exception while accepting connection", ex);
				}
			}
		}
	}

	protected void handleClient(final Socket socket) {
		try (
			final Socket clientSocket = socket;
			final ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
			final ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
		) {
			out.flush();
			final Object request = in.readObject();
			LOGGER.info("Received request: " + request);
			try {
				final Object response = processRequest(request);
				out.writeObject(response);
				out.flush();
				LOGGER.info("Sent response: " + response);
			} catch (final Exception ex) {
				LOGGER.log(Level.WARNING, "Error processing request: " + request, ex);
				out.writeObject(ex);
				out.flush();
			}
		} catch (final Exception ex) {
			LOGGER.log(Level.SEVERE, "Error handling client request", ex);
		}
	}

	protected Object processRequest(final Object request) throws Exception {
		if (request instanceof String) {
			final String message = (String) request;
			return "I received " + message + "! ";
		}
		return "I received " + request + "! ";
	}

	public static void main(String[] args) {
		int port = DEFAULT_PORT;
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (final NumberFormatException ex) {
				LOGGER.warning("Invalid port parameter, using default: " + DEFAULT_PORT);
			}
		}

		final HelloWorldServer server = new HelloWorldServer(port);
		try {
			server.start();
			LOGGER.info("Hello World Server is running. Press Enter or Ctrl+C to terminate.");
			// Keep main thread alive if running as standalone application
			server.serverSocket.getInetAddress();
			// Wait on the current thread
			while (server.isRunning()) {
				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
					break;
				}
			}
		} catch (final IOException ex) {
			LOGGER.log(Level.SEVERE, "Could not start HelloWorldServer on port " + port, ex);
		}
	}
}
