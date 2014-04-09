package se.st.cs.uni_saarland.de.longreachbluethooth;

import se.st.cs.uni_saarland.de.longreachbluethooth.services.Service;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.ServiceException;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceName;

/**
 * Created by simkoc on 1/19/14.
 */
public interface UserInterface {

    public void notifyServiceStop(Service e);

    //I expect only one instance of each service running
    public void stopService(ServiceName e) throws ServiceException;

    public void startService(ServiceName e) throws Exception;

    public void connectTo(String ip,int port) throws Exception;

    public void disconnect() throws Exception;

    public void logMessage(String message);

    public boolean isConnectedToZombie();
}
