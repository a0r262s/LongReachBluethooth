package se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceDiscovery;

import se.st.cs.uni_saarland.de.longreachbluethooth.Constants;
import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.Service;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceThreadHandler;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.ServiceException;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceName;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.UnableToEstablishServerException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

/**
 * Created by he on 12/17/13.
 */
public class DeviceDiscoveryZombie implements Service, ServiceThreadHandler {


    private static DeviceDiscoveryZombie instance;

    public static DeviceDiscoveryZombie getInstance() throws ServiceException {
        if(instance == null)
            try {
                instance = new DeviceDiscoveryZombie();
            } catch (UnableToEstablishServerException e) {
                throw new ServiceException("encountered error while retrieving instance " + e.getMessage());
            }
        return instance;
    }

    /**
     * This function returns the corresponding device to a name
     *
     * @param name the name of the device of interest
     * @return he RemoteDevice corresponding to the name
     * @throws ServiceException if the service is not yet started
     */
    public static RemoteDevice getRemoteDevice(String name) throws ServiceException {
        if(instance == null || !instance.running())
            throw new ServiceException("service not running");
        else
            return instance.getDevice(name);
    }

    /**
     *
     * @param mac
     * @return
     * @throws ServiceException
     */
    public static ArrayList<ServiceRecord> getServicesOf(String mac) throws ServiceException {
        if(instance == null && !instance.running())
            throw new ServiceException("service not running");
        else
            return instance.getServices(mac);
    }

    /**
     * this function returns all the names of the available devices
     *
     * @return a arrayList of all available devices
     * @throws ServiceException if the service is not yet started
     */
    public static Set<String> getAllDeviceNames() throws ServiceException {
        if(instance == null)
            throw new ServiceException("service not yet started");
        else
            return instance.nameToDevice.keySet();
    }

    /**
     * This function is for retrieving service information of a certain device
     *
     * @param mac the mac address of the device
     * @param service the service id of the service
     * @return a SeviceRecord for future useage
     * @throws ServiceException if the service is not yet started
     */
    public static ServiceRecord getServiceOf(String mac,int service) throws ServiceException {
        if(instance == null || !instance.running())
            throw new ServiceException("service not running");
        else
        {
            Logger.getInstance().log("getServiceOf",mac+service);
            return instance.getServiceRecord(mac, service);

        }
    }


    DiscoverBluetoothDevices discover;
    Thread discoverThread;

    TransferDeviceInformation transfer;
    Thread transferThread;

    Map<String,RemoteDevice> nameToDevice;
    Map<String,ArrayList<ServiceRecord>> macToServices;

    private DeviceDiscoveryZombie() throws UnableToEstablishServerException {
        nameToDevice = new HashMap<>();
        macToServices = new HashMap<>();

    }


    public synchronized void updateDeviceRecord(String name,RemoteDevice rd) {
        Logger.getInstance().log("DeviceDiscoveryZ","discovered device " + name);
        nameToDevice.put(name,rd);
        transfer.transmitDiscoveredDevice(name);
    }

    public synchronized void updateDeviceServiceRecords(String mac,ArrayList<ServiceRecord> records) {
        Logger.getInstance().log("DeviceDiscoveryZ","update #" + records.size() + " for " + mac);
        macToServices.put(mac,records);
    }

    public synchronized RemoteDevice getDevice(String name) {
        return nameToDevice.get(name);
    }

    public synchronized ArrayList<ServiceRecord> getServices(String mac) {
        return macToServices.get(mac);
    }

    public synchronized  ArrayList<RemoteDevice> getDevices() {
        return new ArrayList(nameToDevice.values());
    }

    public synchronized ServiceRecord getServiceRecord(String mac,int service) {
        if(null == macToServices.get(mac))
            return null;
        for(ServiceRecord sr : macToServices.get(mac)) { //TODO: there has to be a way to get those services identified
            switch(service) {
                case ServiceIDs.OBEX_PUT :
                    return sr;

                case ServiceIDs.HID :
                    return sr;

                default :  return null;
            }
        }
        return null;
    }

    private void stopDiscovery() throws InterruptedException {
        if(discoverThread.isAlive())
            discover.stop();
        discoverThread.join(Constants.MAX_JOIN_WAIT_MS);
        if(discoverThread.isAlive()) {
            discoverThread.interrupt();
            discoverThread.join();
        }
        discover = null;
    }

    private void stopTransfer() throws InterruptedException {
        if(transferThread.isAlive())
            transfer.stop();
        transferThread.join(Constants.MAX_JOIN_WAIT_MS);
        if(transferThread.isAlive()) {
            transferThread.interrupt();
            transferThread.join();
        }
        transfer = null;
    }

    @Override
    public boolean start(String ip, int port) throws ServiceException {
        discover = new DiscoverBluetoothDevices(this);
        discoverThread = new Thread(discover);
        try {
            transfer = new TransferDeviceInformation(this);
        } catch (UnableToEstablishServerException e) {
            throw new ServiceException(e.getMessage());
        }
        transferThread = new Thread(transfer);
        transferThread.start();
        discoverThread.start();
        return true;
    }

    @Override
    public boolean stop() throws ServiceException {
        try {
            stopDiscovery();
            stopTransfer();
        return true;
        } catch (InterruptedException e) {
            throw new ServiceException("unable to shut down properly:" + e.getMessage());
        }
    }

    @Override
    public synchronized int port() throws ServiceException {
        return transfer.getPort();
    }

    @Override
    public ServiceName getName() {
        return ServiceName.ServiceDiscovery;
    }

    @Override
    public synchronized boolean running() {
        return transferThread.isAlive() || discoverThread.isAlive();
    }

    @Override
    public void notifyUnhandledException(Exception e) {
        try {
            Logger.getInstance().log("DeviceDiscoveryZ","ERROR:" + e.getMessage());
            stop();
        } catch (ServiceException e1) {
            //TODO: what now?
        }
    }

    @Override
    public void notifyConnectionEstablished() {
        //not necessary as the discovery will take place anyways
    }

    @Override
    public void notifyStop(Object notifier) {

    }
}
