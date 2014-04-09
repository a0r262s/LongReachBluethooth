package se.st.cs.uni_saarland.de.longreachbluethooth.services.filetransferpassive;

import se.st.cs.uni_saarland.de.longreachbluethooth.Constants;
import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ConnectionHandler;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceThreadHandler;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.AboardByUserException;
import se.st.cs.uni_saarland.de.longreachbluethooth.util.DebugPrint;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

/**
 * This Class represents the Thread for receiving a send file request
 * from the zombie node and notifying the user as well as handling
 * the saving of that file locally
 */
public class WaitAndSaveFile implements Runnable {
    //TODO:file.length > Integer.MAX_INTEGER -> BOOOM


    private boolean stop;
    private Socket serverConnection;
    private DataInputStream dis;
    private DataOutputStream dos;
    private ServiceThreadHandler handler;

    public WaitAndSaveFile(Socket serverConnection,ServiceThreadHandler handler) throws IOException {
        this.handler = handler;
        stop = false;
        this.serverConnection = serverConnection;
        dis = new DataInputStream(serverConnection.getInputStream());
        dos = new DataOutputStream(serverConnection.getOutputStream());
    }

    public synchronized void stop() {
        DebugPrint.print("soooping");
        try {
            dos.writeInt(Constants.STOP);
        } catch (IOException e) {

        }
        stop = true;
        cleanUp();
    }

    private synchronized void doFileTransfer() throws IOException {
        try {
            int lengthOfSenderName = dis.readInt(); //II
            byte[] senderNameB = new byte[lengthOfSenderName];
            dis.read(senderNameB); //III
            String sender = new String(senderNameB);
            int lengthOfFileName = dis.readInt(); //IV
            byte[] fileNameB = new byte[lengthOfFileName];
            dis.read(fileNameB); //V
            String fileName = new String(fileNameB);
            int option = JOptionPane.showConfirmDialog(null,sender + " wants to transfer file " + fileName,"FileTransferRequest",JOptionPane.YES_NO_OPTION);
            if(option != JOptionPane.YES_OPTION) {
                throw new AboardByUserException();
            } else {
                dos.writeInt(Constants.FILE_TRANSFER_START);
            }
            File f = null;
            do {
                String filePath = JOptionPane.showInputDialog(null, "please state path to save file to");
                try {
                f = new File(filePath);
                } catch(Exception e) {
                    JOptionPane.showMessageDialog(null,"FileTransferCancel:" + e.getMessage());
                    continue;
                }
                if(f == null) {
                    JOptionPane.showMessageDialog(null,"unable to open/create specified file");
                }
            } while (f == null);
            DebugPrint.print("receiving file");
            int fileSize = dis.readInt(); //VI
            DebugPrint.print("the file to be received is size " + fileSize);
            byte[] file = new byte[fileSize];
            DebugPrint.print("file receviced");
            for(int i = 0; i != fileSize;++i) {
                file[i] = dis.readByte();
            }
            //dis.read(file);
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(file);
            dos.writeInt(Constants.FILE_TRANSFER_SUCCESS);
        } catch (IOException e) {
            e.printStackTrace();
            dos.writeInt(Constants.FILE_TRANSFER_ABOARD);
            JOptionPane.showMessageDialog(null,"FileTransferCancel:" + e.getMessage());
        } catch (AboardByUserException e) {
            e.printStackTrace();
            dos.writeInt(Constants.FILE_TRANSFER_ABOARD);
            return;
        }
    }

    private synchronized void cleanUp() {
        try {
            if(dis != null) {
                dis.close();
                dis = null;
            }
            if(dos != null) {
                dos.close();
                dos = null;
            }
            if(serverConnection != null) {
                serverConnection.close();
                serverConnection = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
        while(true) {
            try {
                synchronized (this) {
                    if(stop)
                        break;
                }
                DebugPrint.print("wait for command");
                int command = dis.readInt(); //I
                DebugPrint.print("got command " + command);
                if(command == Constants.FILE_TRANSFER_REQ) {
                    doFileTransfer();
                } else {
                    dos.writeInt(Constants.FILE_TRANSFER_BAD_COMMAND);
                }
            } catch (IOException e) {
                handler.notifyUnhandledException(e);
                cleanUp();
                break;
            }
        }
    } finally {
            handler.notifyStop(this);
        }
    }
}
