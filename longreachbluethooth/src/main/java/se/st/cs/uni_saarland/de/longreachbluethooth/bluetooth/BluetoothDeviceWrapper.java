package se.st.cs.uni_saarland.de.longreachbluethooth.bluetooth;

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.Connection;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/** wrapper for the BT device
 * User: Simon Koch
 * Date: 06.12.13
 * Time: 11:54
 *
 * This Class represents a singleton wrapper for interacting
 * with the bluetooth device on the machine, to ensure
 * thread safety if it is needed later on
 */
public class BluetoothDeviceWrapper {


    public static BluetoothDeviceWrapper getInstance() throws BluetoothStateException {
        if(instance != null)
            return instance;

        instance = new BluetoothDeviceWrapper();
        return instance;
    }

    private static BluetoothDeviceWrapper instance = null;
    private LocalDevice localDevice;


    private BluetoothDeviceWrapper() throws BluetoothStateException {
        localDevice = LocalDevice.getLocalDevice();
    }


    public void discoverDevices(DiscoveryListener dl) throws BluetoothStateException, InterruptedException {
        synchronized (dl) {
            if(LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, dl)) {
                dl.wait();
            }
        }
    }

    public void discoverServices(RemoteDevice rd,DiscoveryListener dl,int[] services) throws BluetoothStateException, InterruptedException {
        synchronized (dl) {
            int[] attrIDs = new int[] {0x0000,0x0001, 0x0100,0x0003,0x0004 }; //TODO: figure out what this actually does
            for(int id : services) {
                UUID[] searchUuidSet = new UUID[]{new UUID(id)};
                LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs,searchUuidSet,rd,dl);
                dl.wait();
            }
        }
    }

    public void sentFile(String serviceURL,byte[] file,String name,String type) throws IOException {
            Connection connection = Connector.open(serviceURL);
            ClientSession cs = (ClientSession) connection;
            HeaderSet hs = cs.createHeaderSet();
            cs.connect(hs);
            hs.setHeader(HeaderSet.NAME,name);
            hs.setHeader(HeaderSet.TYPE,type);
            hs.setHeader(HeaderSet.LENGTH,new Long(file.length));
            Operation putOperation = cs.put(hs);
            OutputStream outputStream = putOperation.openOutputStream();
            outputStream.write(file);
            outputStream.close();
            putOperation.close();
            cs.disconnect(null);
            connection.close();
    }


    //TODO: whatever this is - don't do it that way as the local variable does not serve any purpose but ensuring a bug
    private L2CAPConnection con;

    public void BTKeyboard_L2CAPCONNECTION(String kbAddr) throws IOException{
        con = (L2CAPConnection) Connector.open("btl2cap://"
                + kbAddr + ":13;authenticate=true;");
    }

    public int BTKeyboard_RECEIVE_HID_REPORT (byte[] hidReport)throws IOException {
        return con.receive(hidReport);
    }

    public boolean BTKeyboard_L2CAPCONNECTION() throws IOException{
        try{
            con.close();
            return true;
        } catch (IOException e){
            System.out.println("Connection failed");
            return false;
        }
    }
}
