package se.st.cs.uni_saarland.de.longreachbluethooth.services.keyboardinteraction;

import se.st.cs.uni_saarland.de.longreachbluethooth.Constants;
import se.st.cs.uni_saarland.de.longreachbluethooth.bluetooth.BluetoothDeviceWrapper;
import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceDiscovery.DeviceDiscoveryZombie;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceDiscovery.ServiceIDs;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.ServiceException;

import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by he on 12/18/13.
 */
public class PacketSender implements Runnable {

    private ServerSocket zombieSocket;

    private Socket userSocket;

    private boolean serviceLock=true;

    private DataOutputStream out;

    private DataInputStream in;

    private static int pLen=10;

    private String kbAddr;

    boolean running = true;

    public PacketSender(ServerSocket serversocket) throws IOException {

        zombieSocket=serversocket;







    }

    public void FetchPacket() throws IOException, ServiceException {
        Logger.getInstance().log("PS","waiting for device name");
        String deviceName = getDeviceName();
        Logger.getInstance().log("PS","got device name " + deviceName);
        if(!checkIfDeviceAvailable(deviceName)) {
            out.writeInt(Constants.DEVICE_NOT_AVAILABLE_STOP);
            throw new ServiceException("specified device " + deviceName + " is not available for service");
        } else {
            out.writeInt(Constants.OK);
        }
        RemoteDevice rd = DeviceDiscoveryZombie.getRemoteDevice(deviceName);

        kbAddr=rd.getBluetoothAddress();

        byte hidReport[]=new byte[pLen];
         System.setProperty("bluecove.jsr82.psm_minimum_off", "true");
        DataOutputStream dos=new DataOutputStream(out);
         int packetLen;
        System.out.println("Connecting to "+kbAddr);
        try {
            BluetoothDeviceWrapper.getInstance().BTKeyboard_L2CAPCONNECTION(kbAddr);
            System.out.println("Keyboard Connected!");}
        catch (IOException e){

            System.out.println("Cannot connect to the keyboard!");
            serviceLock=false;
            hidReport[1]=104;
            dos.write(hidReport,0,pLen);

    }

    while (serviceLock){
        // get packet from L2CAP connection
        synchronized(this) {
            if(!running) {
                break;
            }
        }
        try{
            BluetoothDeviceWrapper.getInstance().BTKeyboard_RECEIVE_HID_REPORT(hidReport);
            dos.write(hidReport,0,pLen);
        } catch (IOException e){

            System.out.println("Cannot get the keystroke information!");
            hidReport[1]=104;
            dos.write(hidReport,0,pLen);
            serviceLock=false;

        }

    }
    }


    private String getDeviceName() throws ServiceException {
        Logger.getInstance().log("HID","waiting for device name");
        try {
            int first =  in.readInt();
            Logger.getInstance().log("HID","got first");
            int second = in.readInt();
            Logger.getInstance().log("HID","got second");
            if(first == Constants.TARGET_DEVICE_NAME_TRANSFER &&
                    second == Constants.TRANSFER_SIZE) {
                Logger.getInstance().log("HID","starting to read device name size");
                int size = in.readInt();
                byte[] name = new byte[size];
                Logger.getInstance().log("HID","reading device name");
                in.read(name);
                Logger.getInstance().log("HID","read " + new String(name,"UTF-8"));
                return new String(name,"UTF-8");
            } else {
                closeConnection(Constants.BAD_ARGUMENT_STOP);
                throw new ServiceException("retrieved bad arguments form user, closed connection");
            }
        } catch (IOException e) {
            throw new ServiceException("unable to retrieve device name: " + e.toString());
        }
    }

    private boolean checkIfDeviceAvailable(String deviceName) throws ServiceException {
        RemoteDevice rd = DeviceDiscoveryZombie.getRemoteDevice(deviceName);
        if(rd == null)
            return false;
        ServiceRecord sr = null;
        try {
            sr = getHIDServiceOfDevice(rd.getBluetoothAddress());
        } catch (ServiceException e) {
            return false;
        }
        if(sr == null)
            return false;
        return true;
    }

    private ServiceRecord getHIDServiceOfDevice(String mac) throws ServiceException {
        ServiceRecord sr = DeviceDiscoveryZombie.getServiceOf(mac, ServiceIDs.HID);
        if (sr == null)
            throw new ServiceException("no service found for specified device");
        else
            return sr;
    }

    private void closeConnection(int key) throws IOException {
        out.writeInt(key);
        zombieSocket.close();
        userSocket.close();
    }

    public synchronized void  stop() {
        running = false;
    }


    @Override
    public void run() {

        try {
            Logger.getInstance().log("PS","waiting for connection");
            userSocket=zombieSocket.accept();
            Logger.getInstance().log("PS","connection established");
            out=new DataOutputStream(userSocket.getOutputStream());
            in=new DataInputStream(userSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Logger.getInstance().log("PS","about to start main procedure");
            FetchPacket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

    }
}
