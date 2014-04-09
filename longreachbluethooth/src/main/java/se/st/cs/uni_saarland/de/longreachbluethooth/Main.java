package se.st.cs.uni_saarland.de.longreachbluethooth;


import se.st.cs.uni_saarland.de.longreachbluethooth.GUI.user.UserGui;
import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;
import se.st.cs.uni_saarland.de.longreachbluethooth.util.DebugPrint;

import javax.swing.*;

/**
 * Hello world!
 *
 */
public class Main
{
    private static boolean hasCommandLineArg(String[] args,String param) {
        for(int i = 0; i != args.length; ++i) {
            if(args[i].compareTo(param) == 0)
                return true;
        }
        return false;
    }

    private static boolean hasCommandLineParam(String[] args,String param) {
        for(int i = 0; i != args.length; ++i) {
            if(args[i].compareTo(param) == 0 && args.length != i+1)
                return true;
        }
        return false;
    }

    private static String getCommandLineParam(String[] args,String param) {
        for(int i = 0; i != args.length; ++i) {
            if(args[i].compareTo(param) == 0 && args.length != i+1)
                return args[i+1];
        }
        return null;
    }


    public static void main( String[] args ) throws Exception {
        if(hasCommandLineArg(args,Constants.DEBUG_FLAG)) {
            DebugPrint.DEBUG = true;
        }
        if(hasCommandLineArg(args,Constants.ZOMBIE_FLAG)) {
            Logger.formInstance(Constants.ZOMBIE_LOG_FILE);
            Zombie runner;
            if(hasCommandLineArg(args,Constants.BROADCAST_FLAG)) {
                System.out.println("broadcasting");
                String name = "OperationPetticoat";
                int port = Constants.DEFAULT_ADVERTISEMENT_PORT;
                if(hasCommandLineParam(args,Constants.BROADCAST_NAME_PARAM)) {
                    name = getCommandLineParam(args,Constants.BROADCAST_NAME_PARAM);
                }
                if(hasCommandLineParam(args,Constants.BROADCAST_PORT_PARAM)) {
                    port = Integer.getInteger(getCommandLineParam(args,Constants.BROADCAST_PORT_PARAM));
                }
                runner = new Zombie(name,port);
            } else {
                runner = new Zombie();
            }
            runner.mainLoop();
            Logger.getInstance().stop();
        }
        else if(hasCommandLineArg(args,Constants.USER_FLAG)) {
            UserGui g = new UserGui();
        } else {
            System.out.println("please start the program with either:");
            System.out.println("--zombie");
            System.out.println("  turning it into a zombie node able to receive connections");
            System.out.println("  and providing access to the surrounding bt devices");
            System.out.println("--user");
            System.out.println("  turning it into a user app to connect to a zombie");
        }

    }
}
