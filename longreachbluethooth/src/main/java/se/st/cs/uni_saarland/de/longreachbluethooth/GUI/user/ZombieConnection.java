package se.st.cs.uni_saarland.de.longreachbluethooth.GUI.user;

import se.st.cs.uni_saarland.de.longreachbluethooth.Constants;
import se.st.cs.uni_saarland.de.longreachbluethooth.UserInterface;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceFactory;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceName;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.ServiceException;
import se.st.cs.uni_saarland.de.longreachbluethooth.util.DebugPrint;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by simkoc on 2/2/14.
 */
public class ZombieConnection implements Runnable {

    private Socket connection;
    private DataInputStream dis;
    private DataOutputStream dos;
    UserInterface iface;
    private String ip;
    Object com = new Object();

    public String getIp() {
        return ip;
    }

    public ZombieConnection(String ip,int port,UserInterface iface) throws IOException {
        this.ip = ip;
        this.iface = iface;
        connection = new Socket(ip,port);
        dis = new DataInputStream(connection.getInputStream());
        dos = new DataOutputStream(connection.getOutputStream());
    }


    public int startService(ServiceName sn) throws IOException, ServiceException {
        synchronized (com) {
        dos.writeInt(Constants.START_SERVICE_COMMAND);
        dos.writeInt(ServiceFactory.getServiceID(sn));
        int response = dis.readInt();
        if(response == Constants.STARTED) {
            return dis.readInt();
        }

        else if(response == Constants.NOT_AVAILABLE) {
            throw new ServiceException("connected zombie does not support that service");
        }
        else {
            throw new ServiceException("unable to start service - error on zombie side");
        }
        }
    }

    public void resetConnection() {
        synchronized (com) {
        try {
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dis = null;
        try {
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dos = null;
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection = null;
        }
    }


    @Override
    public void run() {
        while (true) {
                try {
                    synchronized (com) {
                        connection.setSoTimeout(10000);
                        dos.writeInt(Constants.PROBE);
                        dis.readInt();
                        if(!connection.isConnected())
                            break;
                        com.wait(10000);
                        if(dis == null)
                            break;
                    }
                } catch (IOException e) {
                    try {
                        e.printStackTrace();
                        DebugPrint.print("calling disconnect");
                        iface.disconnect();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    break;
                } catch (InterruptedException e) {
                    try {
                        e.printStackTrace();
                        DebugPrint.print("calling disconnect");
                        iface.disconnect();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                }
                break;
                }
        }
    }
}
