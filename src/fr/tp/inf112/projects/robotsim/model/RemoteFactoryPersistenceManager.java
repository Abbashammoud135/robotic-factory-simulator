package fr.tp.inf112.projects.robotsim.model;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.impl.AbstractCanvasPersistenceManager;

public class RemoteFactoryPersistenceManager extends AbstractCanvasPersistenceManager {

	private static final Logger LOGGER = Logger.getLogger(RemoteFactoryPersistenceManager.class.getName());

	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = 8080;

	private final String host;
	private final int port;

	public RemoteFactoryPersistenceManager(final CanvasChooser canvasChooser) {
		this(canvasChooser, DEFAULT_HOST, DEFAULT_PORT);
	}

	public RemoteFactoryPersistenceManager(final CanvasChooser canvasChooser,
										   final String host,
										   final int port) {
		super(canvasChooser);
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	@Override
	public Canvas read(final String canvasId) throws IOException {
		LOGGER.info("Reading canvas remotely with id: " + canvasId + " from " + host + ":" + port);
		try (
			final Socket socket = new Socket(host, port);
			final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			final ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		) {
			out.flush();
			out.writeObject(canvasId);
			out.flush();

			final Object response = in.readObject();
			if (response instanceof Canvas) {
				LOGGER.info("Successfully received canvas: " + canvasId);
				return (Canvas) response;
			} else if (response instanceof IOException) {
				throw (IOException) response;
			} else if (response instanceof Throwable) {
				throw new IOException("Remote exception during read", (Throwable) response);
			} else {
				throw new IOException("Unexpected response from server: " + response);
			}
		} catch (final ClassNotFoundException ex) {
			LOGGER.log(Level.SEVERE, "Failed to deserialize Canvas object", ex);
			throw new IOException("Failed to deserialize Canvas object", ex);
		}
	}

	@Override
	public void persist(final Canvas canvasModel) throws IOException {
		LOGGER.info("Persisting canvas remotely with id: " + canvasModel.getId() + " to " + host + ":" + port);
		try (
			final Socket socket = new Socket(host, port);
			final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			final ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		) {
			out.flush();
			out.writeObject(canvasModel);
			out.flush();

			try {
				final Object response = in.readObject();
				if (response instanceof IOException) {
					throw (IOException) response;
				} else if (response instanceof Throwable) {
					throw new IOException("Remote exception during persist", (Throwable) response);
				}
			} catch (final EOFException ex) {
				// Server may have closed the connection upon persisting without sending back a response
				LOGGER.fine("Connection closed by server after persist");
			}
			LOGGER.info("Successfully persisted canvas remotely: " + canvasModel.getId());
		} catch (final ClassNotFoundException ex) {
			LOGGER.log(Level.SEVERE, "Failed to deserialize server response", ex);
			throw new IOException("Failed to deserialize server response", ex);
		}
	}

	@Override
	public boolean delete(final Canvas canvasModel) throws IOException {
		return false;
	}
}
