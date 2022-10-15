package ispd.application.terminal;

import ispd.motor.metricas.Metricas;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * A helper class for the server part of the terminal application simulation.
 */
public class Server {
    private final int serverPort;
    private int clientPort;
    private InetAddress clientAddress;

    public Server(final int serverPort) throws UnknownHostException {
        this.serverPort = serverPort;
        this.clientPort = 0;
        this.clientAddress = InetAddress.getByName("127.0.0.1");
    }

    /**
     * Open a port for incoming of a model from a client and returns it.
     *
     * @return A configuration file for setting up a simulation
     */
    public Document getMetricsFromClient() {
        try (
                final var serverSocket = new ServerSocket(this.serverPort)
        ) {

            final var inputSocket = serverSocket.accept();
            final var inputStream = new ObjectInputStream(inputSocket.getInputStream());

            this.clientPort = inputSocket.getPort();
            this.clientAddress = inputSocket.getInetAddress();

            return (Document) inputStream.readObject();

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Couldn't create the server socket.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Return metrics from a simulation to the client that asked for it.
     *
     * @param modelMetrics Metrics from a simulation result
     */
    public void returnMetricsToClient(Metricas modelMetrics) {
        try (
                final var outputSocket = new Socket(this.clientAddress, this.clientPort);
                final var outputStream = new ObjectOutputStream(outputSocket.getOutputStream())
        ) {

            outputStream.writeObject(modelMetrics);
        } catch (IOException e) {
            System.out.println("Couldn't create the client socket.");
        }
    }

}
