package fr.tp.inf112.projects.robotsim.app;

import java.awt.Component;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;
import fr.tp.inf112.projects.robotsim.model.RemoteFactoryPersistenceManager;

public class RemoteFileCanvasChooser extends FileCanvasChooser {

	private static final Logger LOGGER = Logger.getLogger(RemoteFileCanvasChooser.class.getName());

	private final String host;
	private final int port;

	public RemoteFileCanvasChooser(final String fileExtension,
								  final String documentTypeLabel) {
		this(null, fileExtension, documentTypeLabel, RemoteFactoryPersistenceManager.DEFAULT_HOST, RemoteFactoryPersistenceManager.DEFAULT_PORT);
	}

	public RemoteFileCanvasChooser(final String fileExtension,
								  final String documentTypeLabel,
								  final String host,
								  final int port) {
		this(null, fileExtension, documentTypeLabel, host, port);
	}

	public RemoteFileCanvasChooser(final Component viewer,
								  final String fileExtension,
								  final String documentTypeLabel) {
		this(viewer, fileExtension, documentTypeLabel, RemoteFactoryPersistenceManager.DEFAULT_HOST, RemoteFactoryPersistenceManager.DEFAULT_PORT);
	}

	public RemoteFileCanvasChooser(final Component viewer,
								  final String fileExtension,
								  final String documentTypeLabel,
								  final String host,
								  final int port) {
		super(viewer, fileExtension, documentTypeLabel);
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
	protected String browseCanvases(final boolean open) throws IOException {
		if (open) {
			final String[] modelFiles = fetchModelFilesFromServer();
			if (modelFiles == null || modelFiles.length == 0) {
				JOptionPane.showMessageDialog(getViewer(),
											  "No model files found on server.",
											  "Open Canvas",
											  JOptionPane.INFORMATION_MESSAGE);
				return null;
			}

			final Object selected = JOptionPane.showInputDialog(
				getViewer(),
				"Select a model to open:",
				"Open Canvas",
				JOptionPane.QUESTION_MESSAGE,
				null,
				modelFiles,
				modelFiles[0]
			);

			return selected != null ? selected.toString() : null;
		}
		else {
			final String fileName = JOptionPane.showInputDialog(
				getViewer(),
				"Enter the name of the model file:",
				"Save Canvas",
				JOptionPane.PLAIN_MESSAGE
			);

			return fileName;
		}
	}

	protected String[] fetchModelFilesFromServer() throws IOException {
		LOGGER.info("Fetching model file names from server at " + host + ":" + port);
		try (
			final Socket socket = new Socket(host, port);
			final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			final ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		) {
			out.flush();
			out.writeObject("LIST");
			out.flush();

			final Object response = in.readObject();
			if (response instanceof String[]) {
				return (String[]) response;
			}
			else if (response instanceof List<?>) {
				final List<?> list = (List<?>) response;
				return list.toArray(new String[0]);
			}
			else if (response instanceof IOException) {
				throw (IOException) response;
			}
			else {
				throw new IOException("Unexpected response from server: " + response);
			}
		} catch (final ClassNotFoundException ex) {
			LOGGER.log(Level.SEVERE, "Failed to deserialize server response", ex);
			throw new IOException("Failed to deserialize server response", ex);
		}
	}
}
