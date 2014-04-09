package se.st.cs.uni_saarland.de.longreachbluethooth.services.keyboardinteraction;

import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.Service;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.ServiceException;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceName;

import java.io.IOException;
import java.net.*;
import java.util.Random;

/**
 * Created by he on 12/8/13.
 */

public class KeyboardInteractionZombie implements Service{

    Thread thread;
    private int portNumber;
    ServerSocket zombieSocket;
    boolean running;

    @Override
    public boolean start(String ip, int port) throws ServiceException{
        try {
            running=true;
            ServerSocket server;
            for(int remainingTries = 10;;remainingTries--) { //try ten times to find working port
                try {
                    if(remainingTries == 0)
                        throw new ServiceException("no open port found");
                    port = 10000 + ((new Random()).nextInt() % 1112);
                    ServerSocket buff = new ServerSocket(port);
                    Logger.getInstance().log("FTAZ","established server");
                    this.portNumber = port;
                    server = buff;
                    break;
                } catch (IOException e) {
                    Logger.getInstance().log("FTAZ","seems chosen port didn't work out");
                }
            }
            Logger.getInstance().log("KIZ","port incremented");
            zombieSocket= server;
            Logger.getInstance().log("KIZ","Zombie Started");
            PacketSender ps= new PacketSender(zombieSocket);
            Logger.getInstance().log("KIZ","PacketSender fired");
            thread=new Thread(ps);
            Logger.getInstance().log("KIZ","Thread fired");
            thread.start();
            return true;
        } catch (IOException e) {
            Logger.getInstance().log("KIZ","zeah baby exception:" + e.getMessage());
            return false;
        }
    }


    @Override
    public boolean stop() throws ServiceException {
        try {
        zombieSocket.close();
       // BluetoothDeviceWrapper.getInstance().BTKeyboard_L2CAPCONNECTION();
        System.out.println("Zombie side connection closed");

        return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public int port() {
        return portNumber;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public ServiceName getName() {
        return ServiceName.KeyboardInteraction;
    }

    @Override
    public boolean running() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
