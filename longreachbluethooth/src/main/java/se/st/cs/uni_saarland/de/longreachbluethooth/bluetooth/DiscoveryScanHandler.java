package se.st.cs.uni_saarland.de.longreachbluethooth.bluetooth;

import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import java.util.ArrayList;

/**
 * Created by simkoc on 2/1/14.
 */
public class DiscoveryScanHandler implements DiscoveryListener {

    ArrayList<RemoteDevice> foundDevices = new ArrayList();
    ArrayList<ServiceRecord> foundServices = new ArrayList();

    public ArrayList<RemoteDevice> getFoundDevices() {
        return foundDevices;
    }

    public ArrayList<ServiceRecord> getFoundServices() { return foundServices; }

    @Override
    public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
        foundDevices.add(remoteDevice);
    }
    @Override
    public void servicesDiscovered(int transID, ServiceRecord[] serviceRecords) {
        for(ServiceRecord sr : serviceRecords) {
            foundServices.add(sr);
        }
    }
    @Override
    public void serviceSearchCompleted(int i, int i2) {
        synchronized (this) {
            this.notifyAll();
        }
    }
    @Override
    public void inquiryCompleted(int i) {
        synchronized (this) {
            this.notifyAll();
        }
    }
}
