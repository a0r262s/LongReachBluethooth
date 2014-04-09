package se.st.cs.uni_saarland.de.longreachbluethooth.services.filetransferpassive;

import se.st.cs.uni_saarland.de.longreachbluethooth.Constants;
import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.*;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.ServiceException;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.UnableToEstablishServerException;
import se.st.cs.uni_saarland.de.longreachbluethooth.util.DebugPrint;


public class FileTransferPassiveZombie implements Service, ServiceThreadHandler {

    OBEXServer obexServer;
    Thread OBEXServerThread;

    FileTransferToUser fileTransfer;
    Thread fileTransferThread;


    public FileTransferPassiveZombie() throws ServiceException {
        try {
            fileTransfer = new FileTransferToUser(this);
        } catch (UnableToEstablishServerException e) {
            throw new ServiceException(e.getMessage());
        }
        fileTransferThread = new Thread(fileTransfer);
        obexServer = new OBEXServer(this,fileTransfer);
        OBEXServerThread = new Thread(obexServer);
    }

    @Override
    public boolean start(String ip, int port) throws ServiceException {
        fileTransferThread.start();
        DebugPrint.print("started server");
        return true;
    }

    private void stopOBEXServer() throws InterruptedException {
        Logger.getInstance().log("FileTransferPassiveZ","stopping OBEX server");
        if(OBEXServerThread.isAlive())
            obexServer.stop();
        OBEXServerThread.join(Constants.MAX_JOIN_WAIT_MS); //try to stop gently
        if(OBEXServerThread.isAlive()) {
            Logger.getInstance().log("FileTransferPassiveZ","stopping OBEX server by force");
            OBEXServerThread.interrupt(); //do it by force
            OBEXServerThread.join(); //terminate only if thread is dead already
        }
        Logger.getInstance().log("FileTransferPassiveZ", "stopped OBEX succ: " + !OBEXServerThread.isAlive());
    }

    private void stopUserConnection() throws InterruptedException {
        Logger.getInstance().log("FileTransferPassiveZ","stopping User connection");
        if(fileTransferThread.isAlive())
            fileTransfer.stop();
        fileTransferThread.join(Constants.MAX_JOIN_WAIT_MS);
        if(fileTransferThread.isAlive()) {
            Logger.getInstance().log("FileTransferPassiveZ","stopping User connection by force");
            fileTransferThread.interrupt();
            fileTransferThread.join();
        }
        Logger.getInstance().log("FileTransferPassiveZ","stopping User connection succ:" + !fileTransferThread.isAlive());
    }

    public void cleanShutdown() throws InterruptedException {
        stopOBEXServer();
        stopUserConnection();
    }

    @Override
    public synchronized boolean stop() throws ServiceException {
        Logger.getInstance().log("FileTransferPassiveZ","stopping...");
        try {
            cleanShutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        Logger.getInstance().log("FileTransferPassiveZ","stopping done");
        return true;
    }

    @Override
    public int port() throws ServiceException {
        int buff = fileTransfer.getPort();
        DebugPrint.print("return port nb " + buff);
        return buff;
    }

    @Override
    public ServiceName getName() {
        return ServiceName.FileTransferPassive;
    }

    @Override
    public boolean running() {
        return OBEXServerThread.isAlive() || fileTransferThread.isAlive();
    }

    @Override
    public void notifyUnhandledException(Exception e) {
        Logger.getInstance().log("FileTransferPassiveZ","ERROR:" + e.getMessage());
        try {
            stop();
        } catch (ServiceException e1) {
            Logger.getInstance().log("FileTransferPassiveZ","ShutdownFailed:" + e.getMessage());
        }
    }

    @Override
    public void notifyConnectionEstablished() {
        Logger.getInstance().log("FileTransferPassiveZ","connection established");
        OBEXServerThread.start();
    }

    @Override
    public void notifyStop(Object notifier) {
        DebugPrint.print("got da stop");
        try {
            stop();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }
}
