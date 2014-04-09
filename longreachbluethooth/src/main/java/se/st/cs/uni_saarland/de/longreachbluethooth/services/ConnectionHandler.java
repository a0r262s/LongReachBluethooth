package se.st.cs.uni_saarland.de.longreachbluethooth.services;

import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.UnableToEstablishServerException;
import se.st.cs.uni_saarland.de.longreachbluethooth.util.DebugPrint;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * Created by simkoc on 1/31/14.
 */
public class ConnectionHandler {

    public static final int TRIES = 10;
    public static final int PORT_RANGE_START = 10000;
    public static final int PORT_RANGE_SIZE = 1111;

    protected ServerSocket server;
    protected Socket client;
    protected int port;

    protected DataOutputStream dos;
    protected DataInputStream dis;

    protected ConnectionHandler() throws UnableToEstablishServerException {
        setUpServer();
    }

    protected synchronized void setUpServer() throws UnableToEstablishServerException {
        for(int remainingTries = TRIES;;remainingTries--) { //try ten times to find working port
            try {
                this.port = 0;
                if(remainingTries == 0)
                    throw new UnableToEstablishServerException();
                this.port = PORT_RANGE_START + ((new Random()).nextInt(PORT_RANGE_SIZE));
                server = new ServerSocket(this.port);
                break;
            } catch (IOException e) {
                continue;
            }
        }
        DebugPrint.print("set up server on port " + port);
    }

    protected void shutDownServer() throws IOException {
        if(server != null) {
            server.close();
            server = null;
        }
    }

    protected void shutDownClientConnection() throws IOException {
        if(dos != null) {
            dos.close();
            dos = null;
        }
        if(dis != null) {
            dis.close();
            dis = null;
        }
        if(client != null) {
            client.close();
            client = null;
        }
    }

    protected void waitForConnection() throws IOException {
        try {
            DebugPrint.print("waiting for connection on " + port);
            client = server.accept();
            DebugPrint.print("connection established");
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());
        } catch (IOException e) {
            shutDownClientConnection();
            throw e;
        }
    }

    public synchronized int getPort() {
        DebugPrint.print("return port nb " + port);
        return port;
    }


}
