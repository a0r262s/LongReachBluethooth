package se.st.cs.uni_saarland.de.longreachbluethooth.services;

import se.st.cs.uni_saarland.de.longreachbluethooth.UserInterface;
import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceDiscovery.DeviceDiscoveryUser;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceDiscovery.DeviceDiscoveryZombie;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.ServiceException;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.filetransferactive.FileTransferActiveUser;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.filetransferactive.FileTransferActiveZombie;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.filetransferpassive.FileTransferPassiveUser;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.filetransferpassive.FileTransferPassiveZombie;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.keyboardinteraction.KeyboardInteractionUser;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.keyboardinteraction.KeyboardInteractionZombie;

import static se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceName.*;


/** Factory class to start a Service
 * User: Simon Koch
 * Date: 06.12.13
 * Time: 11:20
 *
 * This Factory class is used to start given service and to retrieve
 * a list of all available services. Any action to start a service should
 * be done using this Factory.
 */
public class ServiceFactory {

    /** get a list of all Available Services
     *
     * returns an Array of all available services
     *
     * @return the array of all available services
     */
    public static ServiceName[] getAvailableServices() {
        return new ServiceName[]{FileTransferActive,
                                 KeyboardInteraction,
                                 ServiceDiscovery,
                                 FileTransferPassive};
    }

    public static boolean serviceStartableByUser(ServiceName sn) {
        switch (sn) {
            case FileTransferActive : return true;
            case FileTransferPassive : return true;
            case KeyboardInteraction : return true;
            case ServiceDiscovery : return false;
            default : return false;
        }
    }

    public static boolean serviceStopableByUser(ServiceName sn) {
        switch (sn) {
            case FileTransferActive : return false;
            case FileTransferPassive : return true;
            case KeyboardInteraction : return true;
            case ServiceDiscovery : return false;
            default : return false;
        }
    }

    public static int getServiceID(ServiceName sn) {
        switch (sn) {
            case FileTransferActive : return 1;
            case KeyboardInteraction : return 2;
            case ServiceDiscovery : return 3;
            case FileTransferPassive : return 4;
            default : return -1;
        }
    }

    /** start a given service
     *
     * starts the specified service on with ip on port
     *
     * @param name the service to start
     * @param ip the ip to which the service should connect (if null it will wait for connection)
     * @param port the port on which the service should connect (if -1 it will wait for connection on random port)
     * @return the started service
     */
    public static Service startService(ServiceName name,String ip,int port,UserInterface iface) throws ServiceException {
        Logger.getInstance().log("SF","start user service " + name);
        switch(name) {
            case FileTransferActive:
                Service ftau = new FileTransferActiveUser(iface);
                ftau.start(ip, port);
                return ftau;

            case KeyboardInteraction:
                Service kizu  = new KeyboardInteractionUser(iface);
                kizu.start(ip,port);
                return kizu;

            case ServiceDiscovery: //TODO: i do not like the possibility that this service is not started when accessed!
                Service sdu = DeviceDiscoveryUser.getInstance(iface);
                sdu.start(ip,port);
                return sdu;

            case FileTransferPassive :
                Service fta = new FileTransferPassiveUser(iface);
                fta.start(ip,port);
                return fta;

            default : throw new ServiceException("this service does not even exist man");
        }
    }

    public static Service startService(ServiceName name) throws ServiceException {
        Logger.getInstance().log("SF","start zombie service " + name);
        switch(name) {
            case FileTransferActive :
                Service ftaz = new FileTransferActiveZombie();
                ftaz.start(null,-1);
                return ftaz;

            case KeyboardInteraction :
                Service kizz = new KeyboardInteractionZombie();
                kizz.start(null,-1);
                return kizz;

            case ServiceDiscovery :
                Service sdz = DeviceDiscoveryZombie.getInstance();
                sdz.start(null,-1);
                return sdz;

            case FileTransferPassive :
                Service ftz = new FileTransferPassiveZombie();
                ftz.start(null,-1);
                return ftz;

            default : throw new ServiceException("this service does not even exist man");
        }
    }

    /** get a service description
     *
     * returns a description of the given service
     *
     * @param name the service the description of is wanted
     * @return a string representing the description
     */
    public static String getServiceDescription(ServiceName name) throws ServiceException {
        switch(name) {
            case FileTransferActive:
                return "lets you transfer a file to a previously defined device";

            case KeyboardInteraction:
                return "lets you remotely pair with a remote keyboard";

            case ServiceDiscovery:
                return "keeps a list of discovered device names from zombie(DEFAULTSTART)";

            default : throw new ServiceException("this service does not even exist man");
        }
    }

}
