package se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceDiscovery;

import se.st.cs.uni_saarland.de.longreachbluethooth.services.ConnectionHandler;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceThreadHandler;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.UnableToEstablishServerException;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.UnexpectedCommunicationBehaviorException;
import se.st.cs.uni_saarland.de.longreachbluethooth.util.DebugPrint;

import java.io.IOException;
import java.util.Stack;

/**
 * Created by simkoc on 2/1/14.
 */
public class TransferDeviceInformation extends ConnectionHandler implements  Runnable {

    public static final int NEXT_DISCOVERED_DEVICE = 0x001;

    private ServiceThreadHandler handler;
    private Stack<DiscoveredBluetoothDevice> discoveredDevices;
    private boolean isStopped;

    protected TransferDeviceInformation(ServiceThreadHandler handler) throws UnableToEstablishServerException {
        super();
        this.handler = handler;
        this.isStopped = false;
        discoveredDevices = new Stack<>();
    }

    public synchronized void transmitDiscoveredDevice(String name) {
        discoveredDevices.add(new DiscoveredBluetoothDevice(name));
    }

    private void cleanUp() throws IOException {
        shutDownServer();
        shutDownClientConnection();
    }

    public synchronized void stop() {
        isStopped = true;
        try {
            cleanUp();
        } catch (IOException e) {
            handler.notifyUnhandledException(e);
        }
    }

    @Override
    public void run() {
        try {
            waitForConnection();
            shutDownServer();
            while(true) {
                synchronized (this) {
                    if(dis.available() != 0) {
                        throw new UnexpectedCommunicationBehaviorException("I did not expect any message from client");
                    }
                    if(discoveredDevices.isEmpty())
                        this.wait(5000);
                    else {
                        DebugPrint.print("sending device information");
                        dos.writeInt(NEXT_DISCOVERED_DEVICE);
                        DebugPrint.print("send flag");
                        discoveredDevices.pop().transmitYourself(dos);
                    }
                    if(isStopped)
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            handler.notifyUnhandledException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            handler.notifyUnhandledException(e);
        } catch (UnexpectedCommunicationBehaviorException e) {
            e.printStackTrace();
            handler.notifyUnhandledException(e);
        }
    }
}
