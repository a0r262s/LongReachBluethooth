package se.st.cs.uni_saarland.de.longreachbluethooth.logger;

import se.st.cs.uni_saarland.de.longreachbluethooth.GUI.user.UserGui;
import se.st.cs.uni_saarland.de.longreachbluethooth.util.DebugPrint;

import java.io.*;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: simkoc
 * Date: 16.12.13
 * Time: 17:39
 * To change this template use File | Settings | File Templates.
 */
public class Logger implements Runnable {

    private static Logger instance;
    private static Thread thread;

    LinkedList<String> messages;
    boolean running;
    PrintWriter pw;

    public static void formInstance(String file) throws IOException {
        File f = new File(file);
        if(!f.exists())
            f.createNewFile();
        System.out.println("logging into:" + f.getAbsolutePath());
        FileOutputStream fo = new FileOutputStream(f);
        formInstance(fo);
    }

    public static void formInstance(OutputStream os) {
        instance = new Logger(os);
        thread = new Thread(instance);
        thread.start();
    }

    public static Logger getInstance() {
        return instance;
    }

    private Logger(OutputStream os) {
        messages = new LinkedList();
        running = true;
        pw = new PrintWriter(os);
    }

    public void log(String service,String message) { //TODO: make it pretty using reflection for class name
        synchronized (messages) {
            messages.add("["+ new Date().toString() + "]" + "[" + service + "]" + message);
            messages.notifyAll();
        }
    }

    private void printMessage(String m) {
        pw.write(m);
        pw.write("\n");
        pw.flush();
    }

    public void stop() {
        synchronized (messages) {
            running = false;
            messages.notifyAll();
            System.out.println("#" + messages.size() + " left to be logged");
        }
    }

    public static void stopLogger() throws InterruptedException {
        instance.stop();
        thread.join();
        DebugPrint.print("logger is still active " + thread.isAlive());
    }

    @Override
    public void run() {
        try {
            while(running) {
                synchronized (messages) {
                    if(messages.isEmpty()) {
                        messages.wait();
                    }
                    while(!messages.isEmpty()) {
                        printMessage(messages.pollFirst());
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
