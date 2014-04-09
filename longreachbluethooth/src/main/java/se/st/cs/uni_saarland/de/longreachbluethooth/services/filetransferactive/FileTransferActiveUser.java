package se.st.cs.uni_saarland.de.longreachbluethooth.services.filetransferactive;

import se.st.cs.uni_saarland.de.longreachbluethooth.UserInterface;
import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.*;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.ServiceException;
import se.st.cs.uni_saarland.de.longreachbluethooth.util.DebugPrint;

import java.io.IOException;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: simkoc
 * Date: 16.12.13
 * Time: 15:00
 * To change this template use File | Settings | File Templates.
 */
public class FileTransferActiveUser implements Service, ServiceThreadHandler {



    private Thread sendFileThread;
    private UserInterface iface;

    public FileTransferActiveUser(UserInterface iface) {
        this.iface = iface;
    }

    @Override
    public synchronized boolean start(String ip, int port) throws ServiceException {
        try {
            sendFileThread = new Thread(new SendFile(new Socket(ip,port),this));
            sendFileThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean stop() throws ServiceException {
        throw new ServiceException("this service is fireforget");
    }

    @Override
    public int port() throws ServiceException {
        throw new ServiceException("this is the client side, no port known");
    }

    @Override
    public ServiceName getName() {
        return ServiceName.FileTransferActive;
    }

    @Override
    public boolean running() {
        return sendFileThread.isAlive();
    }

    @Override
    public void notifyUnhandledException(Exception e) {

    }

    @Override
    public void notifyConnectionEstablished() {

    }

    @Override
    public void notifyStop(Object notifier) {
       iface.notifyServiceStop(this);
    }
}
