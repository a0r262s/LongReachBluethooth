package se.st.cs.uni_saarland.de.longreachbluethooth.GUI.user;

import se.st.cs.uni_saarland.de.longreachbluethooth.Constants;
import se.st.cs.uni_saarland.de.longreachbluethooth.UserInterface;
import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.Service;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceFactory;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceName;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.ServiceException;
import se.st.cs.uni_saarland.de.longreachbluethooth.util.DebugPrint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by simkoc on 2/2/14.
 */
public class UserGui extends JFrame implements UserInterface {

    public static void main(String args[]) throws IOException, InterruptedException {
        DebugPrint.DEBUG = true;
        new UserGui();
    }

    private final String conID = "CONNECTED";
    private UserGuiConnected con;
    private final String unconID = "UNCONNECTED";
    private UserGuiUnconnected ncon;
    private JPanel main;
    private ZombieConnection connection;
    private Map<ServiceName,Service> serviceHandler;


    public UserGui() throws IOException {
        super("Longreach Bluetooth UserInterface");
        Logger.formInstance(Constants.USER_LOG_FILE);
        serviceHandler = new HashMap<>();
        main = new JPanel();
        main.setLayout(new CardLayout());
        ncon = new UserGuiUnconnected(this);
        main.add(ncon,unconID);
        con = new UserGuiConnected(this);
        main.add(con,conID);
        this.add(main);
        this.setSize(ncon.WIDTH,ncon.HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    Logger.stopLogger();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        });
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
    }


    @Override
    public void notifyServiceStop(Service e) {
        con.serviceHasStopped(e.getName());
        serviceHandler.put(e.getName(), null);
    }

    @Override
    public synchronized void stopService(ServiceName e) throws ServiceException {
        if(serviceHandler.get(e) != null) {
            serviceHandler.get(e).stop();
            serviceHandler.put(e,null);
        }
    }

    @Override
    public synchronized void startService(ServiceName sn) throws Exception {
        if(connection != null) {
            if(serviceHandler.get(sn) != null)
                throw new ServiceException("this service is already running");
            int port = connection.startService(sn);
            serviceHandler.put(sn,ServiceFactory.startService(sn,connection.getIp(),port,this));
        }
    }

    @Override
    public synchronized void connectTo(String ip, int port) throws Exception {
        connection = new ZombieConnection(ip,port,this);
        new Thread(connection).start();
        startService(ServiceName.ServiceDiscovery);
        CardLayout cl = (CardLayout)main.getLayout();
        this.setSize(con.WIDTH, con.HEIGHT);
        cl.show(main, conID);
    }

    @Override
    public synchronized void disconnect() throws Exception {
        Logger.getInstance().log("GUI","got disconnect order");
        for(ServiceName sn : serviceHandler.keySet())
            if(serviceHandler.get(sn) != null) {
                serviceHandler.get(sn).stop();
                serviceHandler.put(sn,null);
            }

        connection.resetConnection();
        connection = null;
        ncon.reset();
        CardLayout cl = (CardLayout)main.getLayout();
        this.setSize(ncon.WIDTH,ncon.HEIGHT);
        cl.show(main,unconID);
    }

    @Override
    public void logMessage(String message) {
        Logger.getInstance().log("GUI",message);
    }

    @Override
    public synchronized boolean isConnectedToZombie() {
        return connection == null;
    }
}
