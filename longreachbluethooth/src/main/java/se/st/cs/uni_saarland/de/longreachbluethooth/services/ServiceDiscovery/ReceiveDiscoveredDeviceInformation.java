package se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceDiscovery;

import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.UnexpectedCommunicationBehaviorException;
import se.st.cs.uni_saarland.de.longreachbluethooth.util.DebugPrint;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by simkoc on 2/1/14.
 */
public class ReceiveDiscoveredDeviceInformation implements Runnable {

    private DeviceDiscoveryUser handler;
    private Socket connection;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean isStopped;

    public ReceiveDiscoveredDeviceInformation(Socket connection, DeviceDiscoveryUser handler) throws IOException {
        this.connection = connection;
        this.dis = new DataInputStream(this.connection.getInputStream());
        this.dos = new DataOutputStream(this.connection.getOutputStream());
        this.handler = handler;
        isStopped = false;
    }

    private void cleanUp() throws IOException {
        DebugPrint.print("cleaning");
        dis.close();
        dos.close();
        connection.close();
    }

    public synchronized void stop() {
        DebugPrint.print("called to staap");
        isStopped = true;
        try {
            cleanUp();
        } catch (IOException e) {
            //TODO: what to do now?
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                synchronized (this) {
                    if(dis.available() != 0) {
                        int command = dis.readInt();
                        if(command == TransferDeviceInformation.NEXT_DISCOVERED_DEVICE) {
                            DebugPrint.print("reveicing device information");
                            handler.addDiscoveredDevice(DiscoveredBluetoothDevice.receiveDeviceInformation(dis));
                        } else {
                            throw new UnexpectedCommunicationBehaviorException("expected NEXT_DISCOVERED_DEVICE flag but got different " + command);
                        }
                        if(isStopped)
                            break;
                    } else {
                        this.wait(5000);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            handler.notifyUnhandledException(e);
        } catch (UnexpectedCommunicationBehaviorException e) {
            e.printStackTrace();
            handler.notifyUnhandledException(e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
            handler.notifyUnhandledException(e);
        } finally {
            handler.notifyStop(this);
        }
    }
}
