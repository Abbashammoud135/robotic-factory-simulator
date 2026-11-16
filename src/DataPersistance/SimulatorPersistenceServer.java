
package fr.tp.inf112.projects.robotsim.app;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.robotsim.model.FactoryPersistenceManager;

public class SimulatorPersistenceServer {

    private static final int PORT = 5000;
    private static final String FILE_EXTENSION = ".ser";

    private static final Logger LOGGER =
            Logger.getLogger(SimulatorPersistenceServer.class.getName());

    private static final FactoryPersistenceManager manager =
            new FactoryPersistenceManager(null);

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            LOGGER.info("Persistence server listening on port " + PORT);

            while (true) {
                try {
                    handleClient(serverSocket.accept());
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING,
                            "Client request failed.", e);
                }
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,
                    "Could not start the persistence server.", e);
        }
    }

    private static void handleClient(Socket socket) throws IOException {

        try (Socket client = socket;
             ObjectOutputStream output = new ObjectOutputStream(
                     new BufferedOutputStream(client.getOutputStream()))) {

            output.flush();

            try (ObjectInputStream input = new ObjectInputStream(
                    new BufferedInputStream(client.getInputStream()))) {

                try {
                    Object request = input.readObject();
                    Object response;

                    if (request instanceof String) {
                        String requestString = (String) request;

                        if ("LIST".equals(requestString)) {
                            response = listCanvasFiles();
                        } else {
                            response = manager.read(requestString);
                        }

                    } else if (request instanceof Canvas) {
                        manager.persist((Canvas) request);
                        response = Boolean.TRUE;

                    } else {
                        throw new IOException("Unsupported request type.");
                    }

                    output.writeObject(response);

                } catch (Exception e) {
                    LOGGER.log(Level.WARNING,
                            "Could not process client request.", e);

                    output.writeObject(new IOException(
                            "Server could not process the request: "
                                    + e.getMessage()));
                }

                output.flush();
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,
                    "Could not start the persistence server.", e);
        }
    }

    private static String[] listCanvasFiles() {

        File[] files = new File(".").listFiles(File::isFile);

        if (files == null) {
            return new String[0];
        }

        return Arrays.stream(files)
                .map(File::getName)
                .filter(name -> name.endsWith(FILE_EXTENSION))
                .sorted()
                .toArray(String[]::new);
    }
}
