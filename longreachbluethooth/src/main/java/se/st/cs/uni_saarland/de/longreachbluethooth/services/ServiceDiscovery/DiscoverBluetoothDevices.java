package se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceDiscovery;

import se.st.cs.uni_saarland.de.longreachbluethooth.Constants;
import se.st.cs.uni_saarland.de.longreachbluethooth.bluetooth.BluetoothDeviceWrapper;
import se.st.cs.uni_saarland.de.longreachbluethooth.bluetooth.DiscoveryScanHandler;
import se.st.cs.uni_saarland.de.longreachbluethooth.util.DebugPrint;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.RemoteDevice;
import java.io.IOException;
import java.util.Date;

/**
 * Created by simkoc on 2/1/14.
 */
public class DiscoverBluetoothDevices implements Runnable {

    public static final int TIME_BETWEEN_SCANS = 30000;

    private DeviceDiscoveryZombie handler;
    private long lastScan;
    private boolean isStopped;


    public DiscoverBluetoothDevices(DeviceDiscoveryZombie handler) {
        this.handler = handler;
        isStopped = false;
    }

    private void discoverBluetoothDevices() throws BluetoothStateException, InterruptedException {
        DebugPrint.print("start scanning for devices");
        DiscoveryScanHandler listener = new DiscoveryScanHandler();
        BluetoothDeviceWrapper.getInstance().discoverDevices(listener);
        for(RemoteDevice rd : listener.getFoundDevices()) {
            try {
                DebugPrint.print("found:" + rd.getFriendlyName(false));
                handler.updateDeviceRecord(rd.getFriendlyName(false), rd);
            } catch(IOException e) {
                DebugPrint.print("found:" + rd.getBluetoothAddress());
                handler.updateDeviceRecord(rd.getBluetoothAddress(),rd);
            }
        }
    }

    private void discoverDeviceServices() throws BluetoothStateException, InterruptedException {
        DebugPrint.print("start scanning for services");
        for(RemoteDevice rd : handler.getDevices()) {
            DiscoveryScanHandler listener = new DiscoveryScanHandler();
            BluetoothDeviceWrapper.getInstance().discoverServices(rd,listener,new int[]{ServiceIDs.HID,ServiceIDs.OBEX_PUT});
            DebugPrint.print("found:" + listener.getFoundServices().size());
            handler.updateDeviceServiceRecords(rd.getBluetoothAddress(),listener.getFoundServices());
        }
    }

    public synchronized void stop() {
        isStopped = true;
    }

    @Override
    public void run() {
        try {
            while(true) {
                discoverBluetoothDevices();
                discoverDeviceServices();
                synchronized (this) {
                    this.wait(Constants.SCAN_TIME);
                    if(isStopped)
                        break;

                }
            }
        } catch (InterruptedException e) {
            handler.notifyUnhandledException(e);
        } catch (BluetoothStateException e) {
            handler.notifyUnhandledException(e);
        }
    }
}
