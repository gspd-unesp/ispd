package ispd.application.terminal;

import ispd.motor.metricas.Metricas;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A helper class for the client part of the terminal application simulation.
 */
public class Client {
    private final InetAddress serverAddress;
    private int clientPort;
    private final int serverPort;

    public Client(final InetAddress serverAddress, final int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.clientPort = 0;
    }

    /**
     * Sends a model to a server for simulation.
     *
     * @param model A configuration file for setting up a simulation.
     */
    public void sendModelToServer(Document model) {
        try (
                final var socket = new Socket(this.serverAddress.getHostName(), this.serverPort);
                final var outputStream = new ObjectOutputStream(socket.getOutputStream())
        ) {
            socket.setReuseAddress(true);
            outputStream.writeObject(model);
            this.clientPort = socket.getLocalPort();


        } catch (IOException e) {
            System.out.println("Couldn't create the client socket.");
        }
    }

    /**
     * Get the metrics from the server and returns it.
     *
     * @return The metrics from a simulation
     */
    public Metricas receiveMetricsFromServer() {
        try (
                final var serverSocket = new ServerSocket(this.clientPort);
                final var inputStream = new ObjectInputStream(serverSocket.accept().getInputStream())
        ) {

            return (Metricas) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

}
