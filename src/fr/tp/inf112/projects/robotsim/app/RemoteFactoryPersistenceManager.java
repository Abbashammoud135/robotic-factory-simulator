
package fr.tp.inf112.projects.robotsim.app;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.impl.AbstractCanvasPersistenceManager;

public class RemoteFactoryPersistenceManager
        extends AbstractCanvasPersistenceManager {

    private final String serverHost;
    private final int serverPort;

    public RemoteFactoryPersistenceManager(
            CanvasChooser canvasChooser,
            String serverHost, int serverPort) {
        super(canvasChooser);
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    private Object sendRequest(Object request) throws IOException {
        try (Socket socket = new Socket(serverHost, serverPort);
             ObjectOutputStream output = new ObjectOutputStream(
                     new BufferedOutputStream(socket.getOutputStream()))) {

            output.flush();

            try (ObjectInputStream input = new ObjectInputStream(
                    new BufferedInputStream(socket.getInputStream()))) {

                output.writeObject(request);
                output.flush();

                try {
                    Object response = input.readObject();

                    if (response instanceof IOException) {
                        throw (IOException) response;
                    }

                    return response;
                } catch (ClassNotFoundException e) {
                    throw new IOException(
                            "Could not read the server response.", e);
                }
            }
        }
    }

    @Override
    public Canvas read(String canvasId) throws IOException {
        Object response = sendRequest(canvasId);

        if (response instanceof Canvas) {
            return (Canvas) response;
        }

        throw new IOException("Invalid canvas received from server.");
    }

    @Override
    public void persist(Canvas canvasModel) throws IOException {
        Object response = sendRequest(canvasModel);

        if (!Boolean.TRUE.equals(response)) {
            throw new IOException("The server did not confirm saving.");
        }
    }

    @Override
    public boolean delete(Canvas canvasModel) {
        // Delete is not required by this assignment.
        return false;
    }
}
