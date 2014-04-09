package se.st.cs.uni_saarland.de.longreachbluethooth.services.keyboardinteraction;

import se.st.cs.uni_saarland.de.longreachbluethooth.UserInterface;
import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.Service;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.ServiceException;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceName;

import java.io.*;
import java.net.*;

/**
 * Created by he on 12/8/13.
 */
public class KeyboardInteractionUser implements Service{


    private Socket userSocket;
    PacketHandler ph;
    Thread thread;
    boolean running;

    public KeyboardInteractionUser(UserInterface iface) {

    }

    @Override
    public boolean start(String ip, int port) throws ServiceException {
        try {
        running=true;
        Logger.getInstance().log("KIU","User Started");
        userSocket = new Socket(ip,port);
        Logger.getInstance().log("KIU","Connected to Zombie");
        ph= new PacketHandler(userSocket);
        thread=new Thread(ph);
        thread.start();
        return true;
        } catch (UnknownHostException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }


    @Override
    public boolean stop() throws ServiceException{

        try {
        //TODO: there should actually no way that stop does not stop the whole stuff
        ph.stop();
        thread.join(2000);
        System.out.println("User side connection closed");
        running=false;
         return true;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return thread.isAlive();
    }

    @Override
    public int port() {
        return -1;
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
        return running;  //To change body of implemented methods use File | Settings | File Templates.
    }


}
