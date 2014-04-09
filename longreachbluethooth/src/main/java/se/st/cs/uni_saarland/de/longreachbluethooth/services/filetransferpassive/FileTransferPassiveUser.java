package se.st.cs.uni_saarland.de.longreachbluethooth.services.filetransferpassive;

import se.st.cs.uni_saarland.de.longreachbluethooth.Constants;
import se.st.cs.uni_saarland.de.longreachbluethooth.UserInterface;
import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.Service;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceThreadHandler;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.ServiceException;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceFactory;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceName;
import se.st.cs.uni_saarland.de.longreachbluethooth.util.DebugPrint;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileTransferPassiveUser implements Service, ServiceThreadHandler {



    private Thread serviceThread;
    private WaitAndSaveFile wasf;
    UserInterface iface;

    public FileTransferPassiveUser(UserInterface iface) {
        this.iface = iface;
    }

    @Override
    public boolean start(String ip, int port) throws ServiceException {
        try {
            Socket serverConnection = null;
            try {
                DebugPrint.print("connect to " + ip + ":" + port);
                serverConnection = new Socket(ip,port);
                DebugPrint.print("connected to server");
                Logger.getInstance().log("FileTransferPassiveU","connected to server");
            } catch (IOException e) {
                throw new ServiceException(e.getMessage());
            }
            wasf = new WaitAndSaveFile(serverConnection,this);
            serviceThread = new Thread(wasf);
            serviceThread.start();
            return true;
            } catch (IOException e) {
                throw new ServiceException(e.getMessage());
            }
    }

    @Override
    public boolean stop() throws ServiceException {
        Logger.getInstance().log("FileTransferPassiveU","stopping...");
        try {
            wasf.stop();
            serviceThread.join(Constants.MAX_JOIN_WAIT_MS);
            if(serviceThread.isAlive()) {
                serviceThread.interrupt();
                serviceThread.join();
                if(serviceThread.isAlive())
                    throw new ServiceException("unable to shut down service properly");
            }
            Logger.getInstance().log("FileTransferPassiveU","stopped");
            return true;
        } catch (InterruptedException e) {
            throw new ServiceException("Service did not terminate in time");
        }

    }

    @Override
    public int port() throws ServiceException {
        throw new ServiceException("this is the user side service - no port required");
    }

    @Override
    public ServiceName getName() {
        return ServiceName.FileTransferPassive;
    }

    @Override
    public boolean running() {
        return serviceThread.isAlive();
    }

    @Override
    public void notifyUnhandledException(Exception e) {
        Logger.getInstance().log("FileTransferPassiveU","ERROR:" + e.getMessage());
    }

    @Override
    public void notifyConnectionEstablished() {

    }

    @Override
    public void notifyStop(Object notifier) {
        iface.notifyServiceStop(this);
    }
}
