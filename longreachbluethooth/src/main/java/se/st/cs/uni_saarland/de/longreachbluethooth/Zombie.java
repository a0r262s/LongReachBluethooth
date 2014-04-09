package se.st.cs.uni_saarland.de.longreachbluethooth;

import se.st.cs.uni_saarland.de.longreachbluethooth.util.AdvertisementServer;
import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.Service;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.exceptions.ServiceException;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceFactory;
import se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceName;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: simkoc
 * Date: 19.12.13
 * Time: 20:15
 * To change this template use File | Settings | File Templates.
 */
public class Zombie {

    private static String getIpAddress() {
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = (NetworkInterface)en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress)enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()&&inetAddress instanceof Inet4Address) {
                        String ipAddress=inetAddress.getHostAddress().toString();
                        return ipAddress;
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return null;
    }



    ServerSocket server;
    int port;
    Socket clientCon;
    DataInputStream bufIn;
    DataOutputStream bufOut;
    ArrayList<Service> running = new ArrayList<Service>();
    AdvertisementServer as;

    public Zombie() {
        as = null;
    }

    public Zombie(String name,int port) throws Exception {
        as = new AdvertisementServer(port,name);
    }

    private void cleanUp() {
        try {
            bufOut.writeInt(Constants.DISCONNECT);
        } catch (IOException e) {

        }
        Logger.getInstance().log("ZOMBIE","performing clean up for shutdown");
        try {
            clientCon.close();
            bufIn = null;
            bufOut = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        synchronized (running) {
            for(Service s : running) {
                try {
                    Logger.getInstance().log("ZOMBIE","stopping" + s.getName());
                    s.stop();
                } catch (ServiceException e) {
                    Logger.getInstance().log("ZOMBIE","stopping failed:" + e.getMessage() );
                }
            }
        }
        System.out.println("bye bye");
    }

    private void establishServer() throws Exception {
        for(int remainingTries = 0;;remainingTries++) { //try ten times to find working port
            if(remainingTries >= 10)
                throw new Exception("no open port found");
            port = 10000 + ((new Random()).nextInt() % 1112);
            ServerSocket buff = null;
            try {
                buff = new ServerSocket(port);
                System.out.println("now running on port " + port);
            } catch (IOException e) {
                continue;
            }
            Logger.getInstance().log("ZOMBIE","established server");
            server = buff;
            return;
        }
    }

    private void cleanServices() {
        synchronized (running) { //TODO:this is a weird work around for the concurrent modification problem
            ArrayList<Service> newRunning = new ArrayList<Service>();
            for(Service s : running) {
                if(s.running())
                    newRunning.add(s);
            }
            running = newRunning;
        }
    }

    private void startService() throws IOException {
        cleanServices();
        int serviceID = 0;
        serviceID = bufIn.readInt();
        ServiceName[] availableServices = ServiceFactory.getAvailableServices();
        System.out.println("requested to start service " + serviceID);
        Logger.getInstance().log("ZOMBIE", "request to start service:" + serviceID);
        for(ServiceName sn : availableServices) {
            if(ServiceFactory.getServiceID(sn) == serviceID) {
                try {
                    Service started = ServiceFactory.startService(sn);
                    running.add(started);
                    bufOut.writeInt(Constants.STARTED);
                    Logger.getInstance().log("ZOMBIE", "started telling user app the port:" + started.port());
                    int port = started.port();
                    bufOut.writeInt(port);
                } catch (ServiceException e) {
                    System.out.println("exeption " + e.getMessage());
                    bufOut.writeInt(Constants.ERROR);
                }
                return;
            }
        }
        bufOut.writeInt(Constants.NOT_AVAILABLE);
    }

    public void mainLoop() {
        try {
            establishServer();
        } catch (Exception e) {
            System.out.println("not able to establish server " + e.getMessage());
            return;
        }
        try {
        while(true) {
            try {
                if(clientCon == null) {
                    for(Service s : running) {
                        try {
                            Logger.getInstance().log("ZOMBIE","stopping " + s.getName() + " as prep for next user");
                            if(s.running())
                                s.stop();
                        } catch (ServiceException e) {
                            Logger.getInstance().log("ZOMBIE","encountered error while cleanup " + e.getMessage());
                        }
                    }
                    running.clear();
                    if(as != null) {
                        System.out.println("start advertising");
                        as.advertise(this.port,getIpAddress());
                    }
                    System.out.println("waiting for connection...");
                    clientCon = server.accept();
                    System.out.println("accepted...");
                    if(as != null) {
                        System.out.println("stop advertising");
                        try {
                            as.stopAdvertisement();
                        } catch (InterruptedException e) {
                            Logger.getInstance().log("ZOMBIE","ERROR:" + e.getMessage());
                        }
                    }
                    bufOut = new DataOutputStream(clientCon.getOutputStream());
                    bufIn = new DataInputStream(clientCon.getInputStream());
                    System.out.println("connected to " + clientCon.getInetAddress().toString());
                }
                Logger.getInstance().log("ZOMBIE","waiting for command");
                int command = bufIn.readInt();
                Logger.getInstance().log("ZOMBIE", "got command:" + command);
                switch(command) {
                    case Constants.START_SERVICE_COMMAND:
                        Logger.getInstance().log("ZOMBIE", "got command start service");
                        startService();
                        break;
                    case Constants.DISCONNECT :
                        Logger.getInstance().log("ZOMBIE", "got command disconnect");
                        clientCon.close();
                        clientCon = null;
                        break;
                    case Constants.PROBE :
                        Logger.getInstance().log("ZOMBIE","been probed - gonna poke back");
                        bufOut.writeInt(Constants.POKE);
                        break;
                    default:
                        Logger.getInstance().log("ZOMBIE", "got command unknown");
                        bufOut.writeInt(Constants.ERROR);
                        break;
                }
            } catch (IOException e) {
                //e.printStackTrace();
                Logger.getInstance().log("ZOMBIE","encountered problem with connection:" + e.getMessage());
                System.out.println("something went wrong with the connection: " + e.getMessage());
                if(clientCon != null)
                    try {
                        Logger.getInstance().log("ZOMBIE","shut down connection");
                        clientCon.close();
                    } catch (IOException e1) {
                        Logger.getInstance().log("ZOMBIE","connection shutdown unsuccessful - stop program");
                        System.out.println("general failure during closing of client con - shutdown");
                        break;
                    }
                clientCon = null;
            }
        }
        }finally {
            System.out.println("yeah I was executed alright");
            cleanUp();
        }

    }

}
