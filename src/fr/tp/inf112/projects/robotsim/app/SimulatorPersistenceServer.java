package fr.tp.inf112.projects.robotsim.app;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.FactoryPersistenceManager;

public class SimulatorPersistenceServer extends HelloWorldServer {

	private static final Logger LOGGER = Logger.getLogger(SimulatorPersistenceServer.class.getName());

	private final FactoryPersistenceManager persistenceManager;

	public SimulatorPersistenceServer() {
		this(DEFAULT_PORT);
	}

	public SimulatorPersistenceServer(final int port) {
		super(port);
		this.persistenceManager = new FactoryPersistenceManager(null);
	}

	@Override
	protected Object processRequest(final Object request) throws Exception {
		if (request instanceof Factory) {
			final Factory factoryModel = (Factory) request;
			LOGGER.info("Server persisting Factory model: " + factoryModel.getId());
			persistenceManager.persist(factoryModel);
			return Boolean.TRUE;
		}

		if (request instanceof String) {
			final String message = (String) request;
			LOGGER.info("Server received String request: '" + message + "'");

			if (isListRequest(message)) {
				final String[] fileNames = listModelFiles();
				LOGGER.info("Returning " + fileNames.length + " model file(s) on server.");
				return fileNames;
			}

			if (isReadRequest(message)) {
				LOGGER.info("Reading model file: " + message);
				return persistenceManager.read(message);
			}

			// If it's another string (e.g. from Hello World practice), echo back
			return super.processRequest(request);
		}

		LOGGER.warning("Unknown request type: " + (request != null ? request.getClass().getName() : "null"));
		return super.processRequest(request);
	}

	private boolean isListRequest(final String message) {
		return "LIST".equalsIgnoreCase(message)
			|| "".equals(message)
			|| "LIST_FILES".equalsIgnoreCase(message)
			|| message.startsWith("LIST:");
	}

	private boolean isReadRequest(final String message) {
		return message.endsWith(".factory")
			|| new File(message).exists()
			|| new File(message + ".factory").exists();
	}

	public static String[] listModelFiles() {
		final File currentDir = new File(".");
		final File[] files = currentDir.listFiles((dir, name) -> name.endsWith(".factory"));
		if (files == null || files.length == 0) {
			return new String[0];
		}
		final String[] names = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			names[i] = files[i].getName();
		}
		Arrays.sort(names);
		return names;
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

		final SimulatorPersistenceServer server = new SimulatorPersistenceServer(port);
		try {
			server.start();
			LOGGER.info("SimulatorPersistenceServer is running on port " + port + ". Ready for requests.");
			while (server.isRunning()) {
				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
					break;
				}
			}
		} catch (final IOException ex) {
			LOGGER.log(Level.SEVERE, "Could not start SimulatorPersistenceServer on port " + port, ex);
		}
	}
}
