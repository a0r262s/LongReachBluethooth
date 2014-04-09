package se.st.cs.uni_saarland.de.longreachbluethooth.services.filetransferpassive;

import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceThreadHandler;
import se.st.cs.uni_saarland.de.longreachbluethooth.util.DebugPrint;

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.obex.*;
import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: simkoc
 * Date: 18.12.13
 * Time: 00:12
 * To change this template use File | Settings | File Templates.
 */
public class OBEXServer implements Runnable  {

    public static final UUID OBEX_OBJECT_PUSH = new UUID(0x1105);
    public static final String SERVER_NAME = "OBEX Object Push";


    boolean isStopped;
    FileTransferToUser ftu;
    ServiceThreadHandler handler;
    SessionNotifier serverConnection;

    public OBEXServer(ServiceThreadHandler handler,FileTransferToUser ftu) {
        isStopped = false;
        this.handler = handler;
        this.ftu = ftu;
    }

    public void transmitFileToUser(byte[] file,String name,String sender) throws IOException {
        ftu.transferFileToUser(file,name,sender);
    }

    private SessionNotifier setUpServer() throws IOException {
        SessionNotifier serverConnection = (SessionNotifier) Connector.open("btgoep://localhost:" + OBEX_OBJECT_PUSH + ";name=" + SERVER_NAME);
        Logger.getInstance().log("FTPZ","await connection");

        LocalDevice localDevice = LocalDevice.getLocalDevice();
        ServiceRecord record = localDevice.getRecord(serverConnection);
        String url = record.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
        Logger.getInstance().log("FTPZ","BT server url: " + url);

        final int OBJECT_TRANSFER_SERVICE = 0x100000;

        //record.setDeviceServiceClasses(OBJECT_TRANSFER_SERVICE);

        DataElement bluetoothProfileDescriptorList = new DataElement(DataElement.DATSEQ);
        DataElement obbexPushProfileDescriptor = new DataElement(DataElement.DATSEQ);
        obbexPushProfileDescriptor.addElement(new DataElement(DataElement.UUID, OBEX_OBJECT_PUSH));
        obbexPushProfileDescriptor.addElement(new DataElement(DataElement.U_INT_2, 0x100));
        bluetoothProfileDescriptorList.addElement(obbexPushProfileDescriptor);
        record.setAttributeValue(0x0009, bluetoothProfileDescriptorList);

        final short ATTR_SUPPORTED_FORMAT_LIST_LIST = 0x0303;
        DataElement supportedFormatList = new DataElement(DataElement.DATSEQ);
        // any type of object.
        supportedFormatList.addElement(new DataElement(DataElement.U_INT_1, 0xFF));
        record.setAttributeValue(ATTR_SUPPORTED_FORMAT_LIST_LIST, supportedFormatList);

        final short UUID_PUBLICBROWSE_GROUP = 0x1002;
        final short ATTR_BROWSE_GRP_LIST = 0x0005;
        DataElement browseClassIDList = new DataElement(DataElement.DATSEQ);
        UUID browseClassUUID = new UUID(UUID_PUBLICBROWSE_GROUP);
        browseClassIDList.addElement(new DataElement(DataElement.UUID, browseClassUUID));
        record.setAttributeValue(ATTR_BROWSE_GRP_LIST, browseClassIDList);

        localDevice.updateRecord(record);

        return serverConnection;
    }

    private void waitForAndHandleIncomingOBEXConnections() throws IOException {
        while(true) {
            RequestHandler rh = new RequestHandler(this);
            try {
                DebugPrint.print("waiting for incoming bt connection");
                rh.connectionAccepted(serverConnection.acceptAndOpen(rh));
                DebugPrint.print("bt came");
            } catch(InterruptedIOException e) {
                isStopped = true;
                break;
            }
            synchronized (this) {
                if(isStopped)
                    break;
            }
        }
    }

    public synchronized void stop() {
        isStopped = true;
        cleanUp();
    }

    public void cleanUp() {
        try {
            if(serverConnection != null)
                serverConnection.close();
            DebugPrint.print("stopped server connection");
        } catch (Exception e) {
            handler.notifyUnhandledException(e);
        }
    }

    @Override
    public void run() {
        try {
            serverConnection = setUpServer();
            waitForAndHandleIncomingOBEXConnections();
        } catch(Exception e) {
            handler.notifyUnhandledException(e);
        } finally {
           cleanUp();
        }
    }
}
