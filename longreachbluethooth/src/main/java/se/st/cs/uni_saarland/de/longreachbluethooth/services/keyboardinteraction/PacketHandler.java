package se.st.cs.uni_saarland.de.longreachbluethooth.services.keyboardinteraction;

import se.st.cs.uni_saarland.de.longreachbluethooth.Constants;
import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceDiscovery.DeviceDiscoveryUser;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.ServiceException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by he on 12/18/13.
 */
public class PacketHandler implements Runnable {

    private static int pLen=10;

    private Socket userSocket;

    private DataOutputStream out;

    private DataInputStream in;
    private DataInputStream dis;
    boolean running = true;



    public PacketHandler(Socket socket) throws IOException {

        userSocket=socket;

        in =new DataInputStream( userSocket.getInputStream());
        out=new DataOutputStream(userSocket.getOutputStream());
        dis = new DataInputStream(in);
    }

    public synchronized void stop() {

        Logger.getInstance().log("PH","Got request to shut down");
        running = false;
    }

    public void ReceivePacket() throws IOException, ServiceException {

        boolean serviceLock=true;

        Logger.getInstance().log("HID","getting device name from user");
        String targetDevice = getTarget("device name");
        Logger.getInstance().log("HID","getting device name " + targetDevice);
        if(!checkDeviceName(targetDevice)) {
            errorPopup("targeted device either does not exist \n or is not a keyboard");
            throw new ServiceException("targeted device not avail");
        }
        Logger.getInstance().log("HID","target device approved");
        Logger.getInstance().log("HID","getting keystrokes");
        byte hidReport[]=new byte[pLen];
        int packetLen;

    while(serviceLock){

        synchronized(this) {
          //  Logger.getInstance().log("PH","running: "+running);
            if(!running) {
                return;
            }
        }

        dis.read(hidReport);
        packetLen=hidReport.length;


       // dumpBuffer(hidReport,pLen);
        if(hidReport[1]==1)
            try{
                handlePacket(hidReport,pLen);}
            catch(Exception e){

                Logger.getInstance().log("PH","Too many keys pressed or touchpad touched");

            }
        else if(hidReport[1]==104)
        {
            errorPopup("The keyboard connection is lost.");
            serviceLock=false;

        }

    }
    }
    void handlePacket(byte[] hidReport, int packetLen) throws
            IOException,
            AWTException {

        // decode keys, only process up to NUM_KEYS
        int modifier = hidReport[2];
        int nrKeys = packetLen - 4;
        if (nrKeys > NUM_KEYS){

            nrKeys = NUM_KEYS;

        }

        // modifier keys
        boolean ctrl_pressed    = (modifier & 0x11) != 0;
        boolean shift_pressed   = (modifier & 0x22) != 0;
        boolean alt_pressed     = (modifier & 0x44) != 0;
        boolean command_pressed = (modifier & 0x88) != 0;

        // process events
        for (int i=0; i< nrKeys; i++){
            // find key in last state
            int new_event = hidReport[4+i];
            if (new_event == 0) continue;
            for (int j=0; j<NUM_KEYS; j++){
                if (new_event == last_keyboard_state[j]){
                    new_event = 0;
                    break;
                }
            }
            if (new_event == 0) continue;

            // get from table
            int new_key = 0;
            //     System.out.println("New Event:"+new_event);
            if (new_event <= 57){
                new_key = keytable_us_none[new_event];
            } else if (new_event >= 0x4f && new_event <= 0x52) {
                new_key = cursor_keys[ new_event - 0x4f];
            } else {
                continue;
            }

            injectKey(new_key, shift_pressed, ctrl_pressed,
                    alt_pressed, command_pressed);

        }

        // store keyboard state
        System.arraycopy(hidReport, 4, last_keyboard_state, 0, NUM_KEYS);
        last_modifier = modifier;
    }

    void injectKey(int new_key, boolean shift_pressed,
                   boolean ctrl_pressed, boolean alt_pressed,
                   boolean command_pressed) throws AWTException {

        // create Robot instance if necessary
        if (robot == null){
            robot = new Robot();
        }

        // use upper case chars
        char key = Character.toUpperCase((char) new_key);

        // set up modifier keys
        if (shift_pressed) {
            robot.keyPress(KeyEvent.VK_SHIFT);
        }
        if (ctrl_pressed) {
            robot.keyPress(KeyEvent.VK_CONTROL);
        }
        if (alt_pressed) {
            robot.keyPress(KeyEvent.VK_ALT);
        }
        if (command_pressed){
            robot.keyPress(KeyEvent.VK_META);
        }

        // inject single press
        robot.keyPress(key);
        robot.keyRelease(key);

        // unset modifier keys
        if (command_pressed){
            robot.keyRelease(KeyEvent.VK_META);
        }
        if (alt_pressed) {
            robot.keyRelease(KeyEvent.VK_ALT);
        }
        if (ctrl_pressed) {
            robot.keyRelease(KeyEvent.VK_CONTROL);
        }
        if (shift_pressed) {
            robot.keyRelease(KeyEvent.VK_SHIFT);
        }
    }

    public void dumpBuffer(byte[] data, int len){

        Logger.getInstance().log("PH",data.toString());
/*        System.out.print("DATA: ");
        for (int i=0;i<len;i++){
            int value = data[i];
            if (value<0){
                value += 256;
            }
            System.out.print( Integer.toString(value) + " ");
        }
        System.out.println();*/
    }

    public static final int cursor_keys[] = {
            KeyEvent.VK_RIGHT,
            KeyEvent.VK_LEFT,
            KeyEvent.VK_DOWN,
            KeyEvent.VK_UP
    };

    public static int keytable_us_none [] = {
            0, 0, 0, 0, /* 0-3 */
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', /*  4 - 13 */
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', /* 14 - 23 */
            'u', 'v', 'w', 'x', 'y', 'z',                     /* 24 - 29 */
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', /* 30 - 39 */
            KeyEvent.VK_ENTER, 27, KeyEvent.VK_BACK_SPACE,    /* 40 - 42 */
            KeyEvent.VK_TAB, ' ',                             /* 42 - 44 */
            '-', '=', '[', ']', '\\', KeyEvent.VK_BACK_SLASH, ';', KeyEvent.VK_QUOTE , '`', ',', /* 45 - 54 */
            '.', '/', KeyEvent.VK_CAPS_LOCK                      /* 55 - 57 */
    };

    public static final int NUM_KEYS = 5;
    Robot robot;

    byte last_keyboard_state[] = new byte[NUM_KEYS];
    int last_modifier = 0;
    @Override
    public void run() {

        try {
            ReceivePacket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        finally{


            try {
                Logger.getInstance().log("PH","Shutting down");
                userSocket.close();
            } catch (IOException e) {
                Logger.getInstance().log("PH","Error:"+e.getMessage());
            }
        }

    }

    public String getTarget(String target) throws ServiceException {
        String selected;
        ArrayList<String> liste = DeviceDiscoveryUser.getAllDevices();
        String[] descriptions = new String[liste.size()];
        int i = 0;
        for(String s : liste)
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
        popup.add(new JLabel(target + ":"));
        popup.add(listScroller);
        int ret = JOptionPane.showConfirmDialog(null,popup,"Select Device",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
        if(ret == 1)
            throw new ServiceException("service aborted by user");

        return descriptions[listener.selected];
    }

    class SL implements ListSelectionListener {

        public int selected;

        @Override
        public void valueChanged(ListSelectionEvent listSelectionEvent) {
            selected = listSelectionEvent.getFirstIndex();
        }
    }

    private boolean checkDeviceName(String deviceName) throws IOException {
        Logger.getInstance().log("PH","starting to check device - transmit bytemasks");
        out.writeInt(Constants.TARGET_DEVICE_NAME_TRANSFER);
        byte[] stringData = deviceName.getBytes();
        out.writeInt(Constants.TRANSFER_SIZE);
        Logger.getInstance().log("PH","starting to check device - transmit length");
        out.writeInt(stringData.length);
        Logger.getInstance().log("PH","starting to check device - transmit name");
        out.write(stringData);
        Logger.getInstance().log("PH","starting to check device - wait for response");
        int response = in.readInt();
        if(response != Constants.OK)
            return false;
        else
            return true;
    }

    public void errorPopup(String message) {
        JPanel popup = new JPanel(new GridLayout(0,1));
        popup.add(new JLabel(message));
        JOptionPane.showConfirmDialog(null,popup,"ERROR",JOptionPane.CLOSED_OPTION,JOptionPane.ERROR_MESSAGE);
    }
}
