package se.st.cs.uni_saarland.de.longreachbluethooth.services.filetransferactive;

import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.Service;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceThreadHandler;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.ServiceException;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceName;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.UnableToEstablishServerException;

import java.io.*;
import java.net.ServerSocket;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: simkoc
 * Date: 16.12.13
 * Time: 15:01
 * To change this template use File | Settings | File Templates.
 */
public class FileTransferActiveZombie implements Service,ServiceThreadHandler {

    ReceiveAndTransferViaBluetooth ratvb;
    Thread ratvbThread;

    public FileTransferActiveZombie() throws ServiceException{
        try {
            ratvb = new ReceiveAndTransferViaBluetooth(this);
            ratvbThread = new Thread(ratvb);
        } catch (UnableToEstablishServerException e) {
            throw new ServiceException("unable to establish server: " + e.getMessage());
        }
    }

    @Override
    public synchronized boolean start(String ip, int port) throws ServiceException {
        ratvbThread.start();
        return true;
    }

    @Override
    public synchronized boolean stop() throws ServiceException {
       throw new ServiceException("this is a fire and forget service - no aboard possible (so far)");
    }

    @Override
    public synchronized int port() throws ServiceException {
        return ratvb.getPort();
    }

    @Override
    public ServiceName getName() {
        return ServiceName.FileTransferActive;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean running() {
        return ratvbThread.isAlive();
    }

    @Override
    public void notifyUnhandledException(Exception e) {
        Logger.getInstance().log("FileTransferActiveZ","ERROR:" + e.getMessage());
        //well it will stop then anyways
    }

    @Override
    public void notifyConnectionEstablished() {
        Logger.getInstance().log("FileTransferActiveZ","established connection");
        //this does not matter in this context
    }

    @Override
    public void notifyStop(Object notifier) {
        //yeah he stopped will tell GUI when gui will be finished
    }
}
