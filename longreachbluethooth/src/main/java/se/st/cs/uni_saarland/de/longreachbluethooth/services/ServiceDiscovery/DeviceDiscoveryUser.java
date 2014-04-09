package se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceDiscovery;

import se.st.cs.uni_saarland.de.longreachbluethooth.Constants;
import se.st.cs.uni_saarland.de.longreachbluethooth.UserInterface;
import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.Service;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceFactory;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceThreadHandler;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.ServiceException;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceName;
import se.st.cs.uni_saarland.de.longreachbluethooth.util.DebugPrint;

import java.io.IOException;
import java.net.Socket;
import java.util.*;


/**
 * Created by he on 12/17/13.
 */
public class DeviceDiscoveryUser implements Service, ServiceThreadHandler {


    private static DeviceDiscoveryUser instance;

    public static DeviceDiscoveryUser getInstance(UserInterface iface) {
        if(instance == null)
            instance = new DeviceDiscoveryUser(iface);
        return instance;
    }

    public static ArrayList<String> getAllDevices() {
        ArrayList<String> devices  = new ArrayList<>();
        for(DiscoveredBluetoothDevice dbd : getInstance(null).getDiscoveredDevices())  {
            DebugPrint.print("adding " + dbd.getName() + " as avail device");
            devices.add(dbd.getName());
        }
        return devices;
    }

    UserInterface iface;

    protected DeviceDiscoveryUser(UserInterface iface) {
        this.iface = iface;
    }

    class DiscoveredBluetoothDeviceImage extends DiscoveredBluetoothDevice {

        long lastSeen;

        public DiscoveredBluetoothDeviceImage(String name) {
            super(name);
            lastSeen = new Date().getTime();
        }

        public DiscoveredBluetoothDeviceImage(DiscoveredBluetoothDevice dbd) {
            this(dbd.getName());
        }

        public boolean isOverdue() {
            return ((new Date().getTime() - lastSeen) > Constants.MAX_LAST_SEEN_TIME);
        }

        public void resetSeen() {
            lastSeen = new Date().getTime();
        }

        public DiscoveredBluetoothDevice clone() {
            return new DiscoveredBluetoothDevice(this.getName());
        }
    }


    private ReceiveDiscoveredDeviceInformation rdi;
    private Thread rdiThread;
    private ArrayList<DiscoveredBluetoothDeviceImage> knownDevices;


    public synchronized void addDiscoveredDevice(DiscoveredBluetoothDevice dbd) {
        Logger.getInstance().log("DeviceDiscoveryU","device update on " + dbd.getName());
        boolean found = false;
        for(DiscoveredBluetoothDeviceImage di : knownDevices) {
            if(di.isEqual(dbd)) {
                di.resetSeen();
                found = true;
                break;
            }
        }
        if(!found)
            knownDevices.add(new DiscoveredBluetoothDeviceImage(dbd));
    }

    public synchronized ArrayList<DiscoveredBluetoothDeviceImage> getOverdueDevices() {
        ArrayList<DiscoveredBluetoothDeviceImage> overdue = new ArrayList<>();
        for(DiscoveredBluetoothDeviceImage dimage : knownDevices) {
            if(dimage.isOverdue())
                overdue.add(dimage);
        }
        return overdue;
    }

    public synchronized void cleanKnownDevicesList() {
        ArrayList<DiscoveredBluetoothDeviceImage> overdue = getOverdueDevices();
        for(DiscoveredBluetoothDeviceImage dimage : overdue) {
            Logger.getInstance().log("DeviceDiscoveryU","device " + dimage.getName() + "is overdue -> removal from list");
            knownDevices.remove(dimage);
        }
    }

    public synchronized ArrayList<DiscoveredBluetoothDevice> getDiscoveredDevices() {
        cleanKnownDevicesList();
        ArrayList<DiscoveredBluetoothDevice> list = new ArrayList<>();
        DebugPrint.print("current amount of avail devices #" + knownDevices.size());
        for(DiscoveredBluetoothDeviceImage dimage : knownDevices)
            list.add(dimage.clone());
        return list;
    }

    @Override
    public boolean start(String ip, int port) throws ServiceException {
        try {
            knownDevices = new ArrayList<>();
            rdi = new ReceiveDiscoveredDeviceInformation(new Socket(ip,port),this);
            rdiThread = new Thread(rdi);
            rdiThread.start();
        } catch (IOException e) {
            throw new ServiceException("unable to connect to given zombie service");
        }
        return true;
    }

    @Override
    public boolean stop() throws ServiceException {
        rdi.stop();
        try {
            rdiThread.join(Constants.MAX_JOIN_WAIT_MS); //TODO: define public constant for this
            if(rdiThread.isAlive()) {
                rdiThread.interrupt();
                rdiThread.join();
        }
        } catch (InterruptedException e) {
            throw new ServiceException("unable to stop service properly:" + e.getMessage());
        }
        return true;
    }

    @Override
    public int port() throws ServiceException {
        throw new ServiceException("this is the user side - no port information needed");
    }

    @Override
    public ServiceName getName() {
        return ServiceName.ServiceDiscovery;
    }

    @Override
    public boolean running() {
        return rdiThread.isAlive();
    }

    @Override
    public void notifyUnhandledException(Exception e) {
        Logger.getInstance().log("DeviceDiscoveryU","ERROR:" + e.getMessage());
        try {
            stop();
        } catch (ServiceException e1) {
            //TODO: well this'd be weird
        }
    }

    @Override
    public void notifyConnectionEstablished() {
        //this is not exactly needed in a user service as connection is obligatory
    }

    @Override
    public void notifyStop(Object notifier) {
        iface.notifyServiceStop(this);
    }
}
