package se.st.cs.uni_saarland.de.longreachbluethooth.util;

/**
 * Created by simkoc on 1/31/14.
 */
public class DebugPrint {

    public static boolean DEBUG = false;

    public static void print(String message) {
        if(DEBUG)
            System.out.println(message);
    }

}
