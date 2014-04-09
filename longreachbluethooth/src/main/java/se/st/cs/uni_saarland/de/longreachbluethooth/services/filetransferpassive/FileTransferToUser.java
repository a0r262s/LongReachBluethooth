package se.st.cs.uni_saarland.de.longreachbluethooth.services.filetransferpassive;

import se.st.cs.uni_saarland.de.longreachbluethooth.Constants;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.*;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.ServiceAlreadyStartedException;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.UnableToEstablishServerException;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.UnexpectedCommunicationBehaviorException;
import se.st.cs.uni_saarland.de.longreachbluethooth.util.DebugPrint;

import java.io.IOException;
import java.util.Stack;

/**
 * Created by simkoc on 1/31/14.
 */
public class FileTransferToUser extends ConnectionHandler implements Runnable {

    public static final int WAITPERIOD_MS = 5000;

    class TransferFileRequest {
        public byte[] file;
        public String name;
        public String sender;

        public TransferFileRequest(byte[] f,String n,String s) {
            file = f;
            name = n;
            sender = s;
        }

        public void transferYourself() throws IOException {
            synchronized (com) {
                if(sender == null) {
                    sender = "anon";
                }
                dos.writeInt(Constants.FILE_TRANSFER_REQ);
                dos.writeInt(sender.getBytes().length);
                dos.write(sender.getBytes());
                dos.writeInt(name.getBytes().length);
                dos.write(name.getBytes());
                int rec = dis.readInt();
                if(rec == Constants.FILE_TRANSFER_START) {
                    DebugPrint.print("the length of the file is " + file.length);
                    dos.writeInt(file.length);
                    DebugPrint.print("writing file");
                    for(byte b : file)
                        dos.writeByte(b);
                    //dos.write(file);
                    DebugPrint.print("done writing da file");
                } else {
                    return;
                }
                dis.readInt();
            }
        }
    }

    ServiceThreadHandler handler;
    boolean isStopped;
    Stack<TransferFileRequest> transferList;
    Object com = new Object();

    public FileTransferToUser(ServiceThreadHandler handler) throws UnableToEstablishServerException {
        super();
        this.handler = handler;
        isStopped = false;
        transferList = new Stack<>();
    }

    public synchronized void transferFileToUser(byte[] file,String name,String sender) {
        transferList.add(new TransferFileRequest(file,name,sender));
    }

    private synchronized void cleanUp() {
        try {
            shutDownServer();
            shutDownClientConnection();
        } catch (IOException e) {
            handler.notifyUnhandledException(e);
        }
    }

    public synchronized void stop() {
        isStopped = true;
        cleanUp();
    }

    @Override
    public void run() {
        try {
            if(isStopped)
                throw new ServiceAlreadyStartedException("this service is either running or has been run before");
            try {
                this.waitForConnection();
                this.shutDownServer();
                handler.notifyConnectionEstablished();
            } catch (IOException e) {
                handler.notifyUnhandledException(e);
                return;
            }
            while(true) {
                synchronized(com) {
                    if(dis.available() != 0) {
                        int message = dis.readInt();
                        if(message == Constants.STOP)
                            break;
                            throw new UnexpectedCommunicationBehaviorException("did not expect message from client at this point  " + message);
                        }
                        if(!transferList.isEmpty()) {
                            transferList.pop().transferYourself();
                        }
                    }
                    synchronized (this) {
                        this.wait(WAITPERIOD_MS);
                        if(isStopped)
                            break;
                    }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            handler.notifyUnhandledException(e);
        } catch (IOException e) {
            e.printStackTrace();
            handler.notifyUnhandledException(e);
        } catch (UnexpectedCommunicationBehaviorException e) {
            e.printStackTrace();
            handler.notifyUnhandledException(e);
        } catch (ServiceAlreadyStartedException e) {
            e.printStackTrace();
            handler.notifyUnhandledException(e);
        } finally {
            handler.notifyStop(this);
            cleanUp();
        }
    }
}
