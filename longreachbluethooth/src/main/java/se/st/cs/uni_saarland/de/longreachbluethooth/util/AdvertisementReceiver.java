package se.st.cs.uni_saarland.de.longreachbluethooth.util;

import se.st.cs.uni_saarland.de.longreachbluethooth.Constants;
import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by simkoc on 1/24/14.
 */
public class AdvertisementReceiver implements Runnable {

    public static void main(String args[]) throws Exception {
        Logger.formInstance("./test.log");
        for(FoundServer s : AdvertisementReceiver.scanForZombies(42424,5000)) {
            System.out.println(s);
        }
        Logger.getInstance().stop();
    }

    int port;
    int time;
    public Exception lastException;

    ArrayList<FoundServer> foundZombies;

    private ArrayList<FoundServer> getFoundZombies() {
        return foundZombies;
    }

    private AdvertisementReceiver(int port,int time) {
        this.port = port;
        this.time = time;
        foundZombies = new ArrayList<>();
    }

    private void addZombieIfNotAlreadyFound(String s) {
        FoundServer fs;
        try {
            fs = new FoundServer(s, Constants.ZOMBIE_ADVERTISMENT_IDENT);
        } catch (Exception e) {
            Logger.getInstance().log("AR","ERR:" + e.getMessage());
            return;
        }
        for(FoundServer elem : foundZombies)
            if(elem.toString().compareTo(fs.toString()) == 0)
                return;
        foundZombies.add(fs);
    }

    public static ArrayList<FoundServer> scanForZombies(int port, int time) throws Exception {
        Logger.getInstance().log("ADV-REC","scan order received");
        AdvertisementReceiver receiver = new AdvertisementReceiver(port,time);
        Thread scanner = new Thread(receiver);
        scanner.start();
        try {
            scanner.join();
        } catch (InterruptedException e) {

        }
        if(receiver.lastException != null)
            throw receiver.lastException;
        ArrayList<FoundServer> fsl = receiver.getFoundZombies();
        scanner = null;
        receiver = null;
        return fsl;
    }

    @Override
    public void run() {
        DatagramSocket socket = null;
        try {
            Logger.getInstance().log("ADV-REC","start scanning");
            long endTime = new Date().getTime() + time;

            try {
                socket = new DatagramSocket(new InetSocketAddress("255.255.255.255", port));
                socket.setSoTimeout(4000);
            } catch (SocketException e) {
                Logger.getInstance().log("ADV-REC","ERR:" + e.getMessage());
                lastException = e;
                return;
            }
            while (new Date().getTime() <= endTime) {
                try {
                    byte[] buf = new byte[1000];
                    DatagramPacket packet = new DatagramPacket(buf,
                            buf.length);
                    socket.receive(packet);
                    int size = 0;
                    for(int i = 0; i != buf.length; ++i)
                        if(buf[i] == '\0')
                            break;
                        else
                            size++;
                    byte actualString[] = new byte[size];
                    for(int i = 0; i != size; ++i)
                        actualString[i] = buf[i];
                    String message = new String(actualString);
                    addZombieIfNotAlreadyFound(message);
                } catch (IOException e) {
                    Logger.getInstance().log("ADV-REC","ERR:" + e.getMessage());
                }
            }
        } finally {
            if(socket != null)
                socket.close();
        }
    }
}
