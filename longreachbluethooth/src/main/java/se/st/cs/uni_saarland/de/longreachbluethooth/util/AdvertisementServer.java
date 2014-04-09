package se.st.cs.uni_saarland.de.longreachbluethooth.util;

import se.st.cs.uni_saarland.de.longreachbluethooth.Constants;
import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by simkoc on 1/24/14.
 */
public class AdvertisementServer implements Runnable {

    DatagramSocket socket;
    String advertisement;
    int currentPort;
    String currentIP;
    boolean running;
    Thread spamBot;

    public AdvertisementServer(int spamPort,String message) throws Exception {
        if(message.getBytes().length > 500) {
            throw new Exception("advertisement name is too long");
        }
        socket = new DatagramSocket(spamPort + 1);
        socket.setBroadcast(true);
        socket.connect(InetAddress.getByName("255.255.255.255"),spamPort);
        advertisement = message;
        Logger.getInstance().log("ZOMBIE-AD","Established SpamBot for " + spamPort);
        running = false;
    }

    public synchronized void advertise(int port,String ip) {
        running = true;
        currentPort = port;
        currentIP = ip;
        spamBot = new Thread(this);
        spamBot.start();
    }

    public void stopAdvertisement() throws InterruptedException {
        synchronized (this) {
            running = false;
        }
        spamBot.join();
        spamBot = null;
        Logger.getInstance().log("ZOMBIE-AD","stopped");
    }

    @Override
    public void run() {
        String broadCastMessage = (Constants.ZOMBIE_ADVERTISMENT_IDENT + "|" + advertisement + "|" + currentIP + ":" + currentPort + "\0");
        while(true) {
            byte[] message = broadCastMessage.getBytes();
            Logger.getInstance().log("ZOMBIE-AD","about to broadcast:'" + broadCastMessage + "'");
            DatagramPacket packet = new DatagramPacket(message,message.length);
            try {
                socket.send(packet);
            } catch (IOException e) {
                Logger.getInstance().log("ZOMBIE-AD","ERROR:" + e.getMessage());
            }
            synchronized (this) {
                try {
                    this.wait(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(!running) {
                    Logger.getInstance().log("ZOMBIE-AD","stopping advertisement");
                    return;
                }
            }
        }
    }
}