package se.st.cs.uni_saarland.de.longreachbluethooth.services.filetransferactive;



import se.st.cs.uni_saarland.de.longreachbluethooth.Constants;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceDiscovery.DeviceDiscoveryUser;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceThreadHandler;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.ServiceException;
import se.st.cs.uni_saarland.de.longreachbluethooth.util.DebugPrint;

import java.io.File;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;


public class SendFile implements Runnable {



    Socket serverConnection;
    DataInputStream dis;
    DataOutputStream dos;

    ServiceThreadHandler handler;

    /**
     *
     *
     * @param connection
     * @throws IOException
     */
    public SendFile(Socket connection,ServiceThreadHandler handler) throws IOException {
        this.handler = handler;
        this.serverConnection = connection;
        dis = new DataInputStream(connection.getInputStream());
        dos = new DataOutputStream(connection.getOutputStream());
    }

    private void cleanUp() throws IOException {
        if(dis != null)
            dis.close();
        if(dos != null)
            dos.close();
        if(serverConnection != null)
            serverConnection.close();
    }

    class SL implements ListSelectionListener {

        public int selected;

        @Override
        public void valueChanged(ListSelectionEvent listSelectionEvent) {
            selected = listSelectionEvent.getFirstIndex();
        }
    }

    private String getTargetDevice() throws ServiceException {
        ArrayList<String> listOfPossibleDevices = DeviceDiscoveryUser.getAllDevices();
        String[] descriptions = new String[listOfPossibleDevices.size()];
        int i = 0;
        for(String s : listOfPossibleDevices)
            descriptions[i++] = s;

        JList names = new JList(descriptions);
        names.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        names.setLayoutOrientation(JList.VERTICAL);
        names.setVisibleRowCount(-1);
        SL listener = new SL();
        names.addListSelectionListener(listener);
        JScrollPane listScroller = new JScrollPane(names);
        listScroller.setPreferredSize(new Dimension(250,80));
        JPanel popup = new JPanel(new GridLayout(0,1));
        popup.add(new JLabel("Target Device"));
        popup.add(listScroller);
        int ret = JOptionPane.showConfirmDialog(null,popup,"Select Device",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
        if(ret == 1)
            throw new ServiceException("service aboarded by user");

        if(listener.selected >= 0 && listener.selected < descriptions.length) {
            return descriptions[listener.selected];
        } else {
            throw new ServiceException("no device selected");
        }
    }

    public void errorPopup(String message) {
        JPanel popup = new JPanel(new GridLayout(0,1));
        popup.add(new JLabel(message));
        JOptionPane.showConfirmDialog(null,popup,"ERROR",JOptionPane.CLOSED_OPTION,JOptionPane.ERROR_MESSAGE);
    }

    private boolean checkDeviceName(String deviceName) throws IOException {
        dos.writeInt(deviceName.getBytes().length);
        dos.write(deviceName.getBytes());
        int response = dis.readInt();
        if(response !=  Constants.OK)
            return false;
        else
            return true;
    }

    private File getFile() throws ServiceException {
        final JFileChooser fc = new JFileChooser(); //TODO: restrain files on certain size -> MAXINT
        JPanel panel = new JPanel();
        int returnVal = fc.showOpenDialog(panel);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
        } else {
            throw new ServiceException("service aborded by user");
        }
    }


    private void sendFileProcedure() throws IOException, ServiceException, InterruptedException {
        dos.writeInt(Constants.SEND_FILE_START);
        if(!checkDeviceName(getTargetDevice())) {
            throw new ServiceException("targeted device not available for specified service");
        }
        File f = getFile();
        String ft = Files.probeContentType(f.toPath());
        //send filetype
        dos.writeInt(ft.getBytes().length);
        dos.write(ft.getBytes());
        //send file name
        dos.writeInt(f.getName().getBytes().length);
        dos.write(f.getName().getBytes());
        //send file
        FileInputStream handler = new FileInputStream(f);
        byte[] file = new byte[(int)f.length()];
        handler.read(file);
        dos.writeInt(file.length);
        dos.write(file);
        DebugPrint.print("file sent");
        if(dis.readInt() != Constants.OK) {
            throw new ServiceException("file was not send successfully");
        }
        DebugPrint.print("GOTOK");
    }

    @Override
    public void run() {
        try {
            sendFileProcedure();
        } catch (Exception e) {
            errorPopup("sending file not successful:" + e.getMessage());
        } finally {
            try {
                cleanUp();
            } catch (IOException e) {
                //TODO: what now?
            }
            handler.notifyStop(this);
        }
    }
}
