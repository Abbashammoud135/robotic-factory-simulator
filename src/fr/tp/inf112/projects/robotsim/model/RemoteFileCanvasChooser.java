package fr.tp.inf112.projects.robotsim.model;

import java.awt.Component;

public class RemoteFileCanvasChooser extends fr.tp.inf112.projects.robotsim.app.RemoteFileCanvasChooser {

	public RemoteFileCanvasChooser(final String fileExtension,
								  final String documentTypeLabel) {
		super(fileExtension, documentTypeLabel);
	}

	public RemoteFileCanvasChooser(final String fileExtension,
								  final String documentTypeLabel,
								  final String host,
								  final int port) {
		super(fileExtension, documentTypeLabel, host, port);
	}

	public RemoteFileCanvasChooser(final Component viewer,
								  final String fileExtension,
								  final String documentTypeLabel) {
		super(viewer, fileExtension, documentTypeLabel);
	}

	public RemoteFileCanvasChooser(final Component viewer,
								  final String fileExtension,
								  final String documentTypeLabel,
								  final String host,
								  final int port) {
		super(viewer, fileExtension, documentTypeLabel, host, port);
	}
}
