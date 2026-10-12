package fr.tp.inf112.projects.robotsim.app;

import fr.tp.inf112.projects.canvas.model.CanvasChooser;

public class RemoteFactoryPersistenceManager extends fr.tp.inf112.projects.robotsim.model.RemoteFactoryPersistenceManager {

	public RemoteFactoryPersistenceManager(final CanvasChooser canvasChooser) {
		super(canvasChooser);
	}

	public RemoteFactoryPersistenceManager(final CanvasChooser canvasChooser,
										   final String host,
										   final int port) {
		super(canvasChooser, host, port);
	}
}
