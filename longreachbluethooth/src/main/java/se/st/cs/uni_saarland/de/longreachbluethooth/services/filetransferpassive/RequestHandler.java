package se.st.cs.uni_saarland.de.longreachbluethooth.services.filetransferpassive;

import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;
import se.st.cs.uni_saarland.de.longreachbluethooth.util.DebugPrint;

import javax.microedition.io.Connection;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;
import javax.obex.ServerRequestHandler;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;


/**
 * this class handles the incoming request for a bluetooth
 * file transfer and buffers the content of that file
 * locally as a byte array
 */
class RequestHandler extends ServerRequestHandler {

    //Timer notConnectedTimer = new Timer();

    boolean isConnected = false;

    boolean receivedOk = false;

    Connection cconn;

    OBEXServer watf;

    public RequestHandler(OBEXServer parent) {
        this.watf = parent;
    }

    byte file[];
    String fileName;

    public byte[] getFile() {
        return file;
    }

    public String getFileName() {
        return fileName;
    }

    void connectionAccepted(Connection cconn) {
        DebugPrint.print("got connection");
        this.cconn = cconn;
    }

    void notConnectedClose() {
        if (!isConnected) {
            try {
                cconn.close();
            } catch (IOException e) {
            }
            if (!receivedOk) {
                Logger.getInstance().log("RH","received nok on connection close");
            }
        }
    }

    public int onConnect(HeaderSet request, HeaderSet reply) {
        isConnected = true;
        return ResponseCodes.OBEX_HTTP_OK;
    }

    public void onDisconnect(HeaderSet request, HeaderSet reply) {
        Logger.getInstance().log("RH", "disconnecting");
    }

    public int onSetPath(HeaderSet request, HeaderSet reply, boolean backup, boolean create) {
        Logger.getInstance().log("RH", "on set path");
        return super.onSetPath(request, reply, backup, create);
    }

    public int onDelete(HeaderSet request, HeaderSet reply) {
        Logger.getInstance().log("RH", "on delete");
        return super.onDelete(request, reply);
    }

    //TODO: check on too big files
    public int onPut(Operation op) {
        Logger.getInstance().log("RH", "on put");
        try {
            HeaderSet hs = op.getReceivedHeaders();
            String name = (String) hs.getHeader(HeaderSet.NAME);
            String sender = (String) hs.getHeader(HeaderSet.WHO);
            if (name != null) {
                Logger.getInstance().log("RH", "receiving " + name);
            } else {
                name = "xxx.xx";
                Logger.getInstance().log("RH", "receiving xxx.xx");
            }
            Long len = (Long) hs.getHeader(HeaderSet.LENGTH);
            if (len != null) {
                Logger.getInstance().log("RH", "size " + len);
            }
            file = new byte[(int)(long)len];
            InputStream is = op.openInputStream();

            is.read(file);

            op.close();

            Logger.getInstance().log("RH","received file");
            watf.transmitFileToUser(file,name,sender);
            return ResponseCodes.OBEX_HTTP_OK;
        } catch (IOException e) {
            Logger.getInstance().log("RH", "server on put error " + e.getMessage());
            return ResponseCodes.OBEX_HTTP_UNAVAILABLE;
        } finally {
            Logger.getInstance().log("RH", "done");
        }
    }

    public int onGet(Operation op) {
        Logger.getInstance().log("RH", "on Get");
        try {
            HeaderSet hs = op.getReceivedHeaders();
            String name = (String) hs.getHeader(HeaderSet.NAME);

            return ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED;

        } catch (IOException e) {
            Logger.getInstance().log("RH", "on get error " + e.getMessage());
            return ResponseCodes.OBEX_HTTP_UNAVAILABLE;
        } finally {
            Logger.getInstance().log("RH", "done");
        }
    }

    public void onAuthenticationFailure(byte[] userName) {
        Logger.getInstance().log("RH", "auth failure");
    }


    public boolean receivedFile() {
        return (file != null);
    }

}
