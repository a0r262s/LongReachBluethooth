package se.st.cs.uni_saarland.de.longreachbluethooth.GUI.user;

import se.st.cs.uni_saarland.de.longreachbluethooth.UserInterface;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.Service;
import se.st.cs.uni_saarland.de.longreachbluethooth.util.AdvertisementReceiver;
import se.st.cs.uni_saarland.de.longreachbluethooth.util.FoundServer;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created by simkoc on 2/2/14.
 */
public class UserGuiUnconnected extends JPanel implements ActionListener{

    public final int WIDTH = 300;
    public final int HEIGHT = 130;


    private void performNetworkScan() {
        try {
            knownZombies.removeAllItems();
            ArrayList<FoundServer> found = AdvertisementReceiver.scanForZombies(Integer.parseInt(portScan.getText()),10000);
            for(FoundServer item : found)
                knownZombies.addItem(item);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,e.getClass().getName() + ":" + e.getMessage(),"Scanning Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    public void reset() {
        knownZombies.removeAllItems();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if(actionEvent.getSource() == knownZombies) {
            FoundServer found = (FoundServer)knownZombies.getSelectedItem();
            if(found != null) {
                String connectAddr = found.getIPv4() + ":" + found.getPort();
                addr.setText(connectAddr);
            }
        }
        if(actionEvent.getSource() == scan) {
            performNetworkScan();
        }
        if(actionEvent.getSource() == connect) {
            try {
                String[] conAdr = addr.getText().split(":");
                if(conAdr.length != 2)
                    throw new Exception("the ip does not seem to be proper");
                iface.connectTo(conAdr[0],Integer.parseInt(conAdr[1]));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,e.getClass().getName() + ":" +  e.getMessage(),"Connection Error",JOptionPane.ERROR_MESSAGE);
            }
        }
    }



    JButton scan;
    JTextField portScan;
    JComboBox<FoundServer> knownZombies;

    JButton connect;
    JTextField addr;

    UserInterface iface;


    private JPanel makeScanningPanel() {

        //setting up zombie scanning
        JPanel scanner = new JPanel();
        scanner.setLayout(new BoxLayout(scanner,BoxLayout.X_AXIS));
        JPanel smallBuff = new JPanel();
        smallBuff.setMaximumSize(new Dimension(10, 25));
        JPanel smallBuff2 = new JPanel();
        smallBuff2.setMaximumSize(new Dimension(10,25));
        knownZombies = new JComboBox<>();
        knownZombies.setMaximumSize(new Dimension(150,25));
        knownZombies.addActionListener(this);
        try {
            knownZombies.setPrototypeDisplayValue(new FoundServer("ZOMBIE|NONE|XXX.XXX.XXX.XXX:XXX","ZOMBIE"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        scanner.add(knownZombies);
        scanner.add(smallBuff);
        portScan = new JTextField("42424");
        portScan.setMaximumSize(new Dimension(60,25));
        scanner.add(portScan);
        scanner.add(smallBuff2);
        scan = new JButton("scan");
        scan.addActionListener(this);
        scanner.add(scan);
        return scanner;
    }


    private JPanel makeZombieConnectionPanel() {
        JPanel connection = new JPanel();
        connection.setLayout(new BoxLayout(connection, BoxLayout.X_AXIS));
        connection.add(new JPanel());
        addr = new JTextField("127.0.0.1:4242");
        addr.setMaximumSize(new Dimension(100, 25));
        connection.add(addr);
        connection.add(new JPanel());
        connect = new JButton("connect");
        connect.setMaximumSize(new Dimension(30,25));
        connect.addActionListener(this);
        connection.add(connect);
        connection.add(new JPanel());
        return connection;
    }

    private JPanel makeCenter() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center,BoxLayout.Y_AXIS));
        center.add(makeScanningPanel());
        center.add(new JPanel());
        center.add(makeZombieConnectionPanel());
        return center;
    }

    public UserGuiUnconnected(UserInterface iface) {
        this.iface = iface;
        this.setLayout(new BorderLayout());
        JPanel topBuffer = new JPanel();
        //topBuffer.setPreferredSize(new Dimension(300,10));
        this.add(topBuffer,BorderLayout.PAGE_START);
        JPanel bottomBuffer = new JPanel();
        //bottomBuffer.setPreferredSize(new Dimension(300,10));
        this.add(bottomBuffer,BorderLayout.PAGE_END);
        JPanel leftBuffer = new JPanel();
        //leftBuffer.setPreferredSize(new Dimension(10,180));
        this.add(leftBuffer,BorderLayout.LINE_START);
        JPanel rightBuffer = new JPanel();
        //rightBuffer.setPreferredSize(new Dimension(10,180));
        this.add(rightBuffer,BorderLayout.LINE_END);
        this.add(makeCenter(),BorderLayout.CENTER);
        //this.add(makeZombieConnectionPanel(),BorderLayout.CENTER);

        this.setVisible(true);
    }

}
