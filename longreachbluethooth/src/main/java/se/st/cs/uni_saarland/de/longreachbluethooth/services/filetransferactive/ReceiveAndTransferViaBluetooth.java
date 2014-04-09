package se.st.cs.uni_saarland.de.longreachbluethooth.services.filetransferactive;


import se.st.cs.uni_saarland.de.longreachbluethooth.Constants;
import se.st.cs.uni_saarland.de.longreachbluethooth.bluetooth.BluetoothDeviceWrapper;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ConnectionHandler;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceDiscovery.DeviceDiscoveryZombie;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceDiscovery.ServiceIDs;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceThreadHandler;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.ServiceException;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.UnableToEstablishServerException;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.UnexpectedCommunicationBehaviorException;
import se.st.cs.uni_saarland.de.longreachbluethooth.util.DebugPrint;

import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: simkoc
 * Date: 17.12.13
 * Time: 21:24
 * To change this template use File | Settings | File Templates.
 */
public class ReceiveAndTransferViaBluetooth extends ConnectionHandler implements Runnable {

    ServiceThreadHandler handler;


    public ReceiveAndTransferViaBluetooth(ServiceThreadHandler handler) throws UnableToEstablishServerException {
        super();
        this.handler = handler;
    }


    //bad that checking whether device exists and sending file is timelaps as device might get lost due
    //to on going scan but this might happen anyways as scans are not udd
    private void fileTransfer() throws ServiceException, IOException, InterruptedException {
        String deviceName = readStringFromInput();
        if(!checkIfDeviceAvailable(deviceName)) {
            dos.writeInt(Constants.DEVICE_NOT_AVAILABLE_STOP);
            throw new ServiceException("specified device " + deviceName + " is not available for service");
        } else {
            dos.writeInt(Constants.OK);
        }
        //get type
        String fileType = readStringFromInput();
        //get name
        String fileName = readStringFromInput();
        //receive file
        int size = dis.readInt();
        byte file[] = new byte[size];
        dis.read(file);
        //send via bt
        RemoteDevice rd = DeviceDiscoveryZombie.getRemoteDevice(deviceName);
        BluetoothDeviceWrapper.getInstance().sentFile(getOBEXServiceOfDevice(rd.getBluetoothAddress()).getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT,false),file,fileName,fileType);
    }

    private void serviceProcedure() throws ServiceException, IOException, InterruptedException, UnexpectedCommunicationBehaviorException {
        waitForConnection();
        int command = dis.readInt();
        if(command !=  Constants.SEND_FILE_START)
            throw new UnexpectedCommunicationBehaviorException("expected SEND_FILE_START but received " + command);
        fileTransfer();
        DebugPrint.print("notify OK");
        dos.writeInt(Constants.OK);
    }

    private String readStringFromInput() throws ServiceException, IOException {
        int size = dis.readInt();
        byte name[] = new byte[size];
        dis.read(name);
        return new String(name);
    }

    private boolean checkIfDeviceAvailable(String deviceName) throws ServiceException {
        RemoteDevice rd = DeviceDiscoveryZombie.getRemoteDevice(deviceName);
        if(rd == null)
            return false;
        return null !=  DeviceDiscoveryZombie.getServiceOf(rd.getBluetoothAddress(), ServiceIDs.OBEX_PUT);
    }

    private ServiceRecord getOBEXServiceOfDevice(String mac) throws ServiceException {
        ServiceRecord sr = DeviceDiscoveryZombie.getServiceOf(mac, ServiceIDs.OBEX_PUT);
        if (sr == null)
            throw new ServiceException("no service found for specified device");
        else
            return sr;
    }

    @Override
    public void run() {
        try {
            serviceProcedure();
        } catch (Exception e) {
            handler.notifyUnhandledException(e);
        } finally {
            handler.notifyStop(this);
        }
    }
}
