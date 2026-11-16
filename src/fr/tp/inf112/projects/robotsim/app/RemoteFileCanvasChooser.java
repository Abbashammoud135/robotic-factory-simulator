
package fr.tp.inf112.projects.robotsim.app;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JOptionPane;

import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;

public class RemoteFileCanvasChooser extends FileCanvasChooser {

    private final String serverHost;
    private final int serverPort;
    private final String fileExtension;

    public RemoteFileCanvasChooser(
            Component viewer,
            String fileExtension,
            String documentTypeLabel,
            String serverHost,
            int serverPort) {

        super(viewer, fileExtension, documentTypeLabel);

        this.fileExtension = fileExtension.startsWith(".")
                ? fileExtension.substring(1)
                : fileExtension;

        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public RemoteFileCanvasChooser(
            String fileExtension,
            String documentTypeLabel) {

        this(null, fileExtension, documentTypeLabel,
                "localhost", 5000);
    }

    @Override
    protected String browseCanvases(boolean open) throws IOException {

        if (open) {
            String[] canvasFiles = requestCanvasFiles();

            if (canvasFiles.length == 0) {
                JOptionPane.showMessageDialog(
                        getViewer(),
                        "No saved canvases were found on the server.");
                return null;
            }

            Object selected = JOptionPane.showInputDialog(
                    getViewer(),
                    "Select a canvas to open:",
                    "Open Canvas",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    canvasFiles,
                    canvasFiles[0]);

            return selected == null ? null : selected.toString();
        }

        String fileName = JOptionPane.showInputDialog(
                getViewer(),
                "Enter a name for the new canvas:",
                "Save Canvas",
                JOptionPane.QUESTION_MESSAGE);

        if (fileName == null || fileName.trim().isEmpty()) {
            return null;
        }

        fileName = fileName.trim();

        String extension = "." + fileExtension;

        if (!fileName.endsWith(extension)) {
            fileName += extension;
        }

        return fileName;
    }

    private String[] requestCanvasFiles() throws IOException {

        try (Socket socket = new Socket(serverHost, serverPort);
             ObjectOutputStream output = new ObjectOutputStream(
                     new BufferedOutputStream(socket.getOutputStream()))) {

            output.flush();

            try (ObjectInputStream input = new ObjectInputStream(
                    new BufferedInputStream(socket.getInputStream()))) {

                output.writeObject("LIST");
                output.flush();

                Object response = readResponse(input);

                if (response instanceof String[]) {
                    return (String[]) response;
                }

                if (response instanceof IOException) {
                    throw (IOException) response;
                }

                throw new IOException(
                        "The server returned an invalid file list.");
            }
        }
    }

    private Object readResponse(ObjectInputStream input)
            throws IOException {

        try {
            return input.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(
                    "Could not read the server response.", e);
        }
    }
}
