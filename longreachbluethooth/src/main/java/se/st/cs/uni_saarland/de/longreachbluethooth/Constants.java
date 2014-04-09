package se.st.cs.uni_saarland.de.longreachbluethooth;

/**
 * this class basically does what the names suggest
 * it contains all constants used in the project
 * for easy access and manipulation
 */
public class Constants {

    //constants used in the main
    public static final String DEBUG_FLAG = "--debug";
    public static final String ZOMBIE_FLAG = "--zombie";
    public static final String USER_FLAG = "--user";
    public static final String BROADCAST_FLAG = "--broadcast";
    public static final String BROADCAST_NAME_PARAM = "--broadcastName";
    public static final String BROADCAST_PORT_PARAM = "--broadcastPort";


    public static final int    DEFAULT_ADVERTISEMENT_PORT = 42424;
    public static final String ZOMBIE_LOG_FILE = "./zombie.log";
    public static final String USER_LOG_FILE = "./user.log";

    //constants used in the zombie
    public static final int START_SERVICE_COMMAND = 0x001;
    public static final int NOT_AVAILABLE = 0x002;
    public static final int STARTED = 0x003;
    public static final int ERROR = 0x004;
    public static final int DISCONNECT = 0x005;

    //advertisement Server
    public static final String ZOMBIE_ADVERTISMENT_IDENT = "ZOMBIE";

    //GUI
    public static final int SCAN_TIME = 20000;

    //services
    public static final int MAX_LAST_SEEN_TIME = 60000;
    public static final int MAX_JOIN_WAIT_MS = 5000;
    public static final int OK = 0x07;
    public static final int TARGET_DEVICE_NAME_TRANSFER = 0x10;
    public static final int DEVICE_NOT_AVAILABLE_STOP = 0x11;
    public static final int TRANSFER_SIZE = 0x14;
    public static final int BAD_ARGUMENT_STOP = 0x15;
    public static final int SEND_FILE_START = 0x17;
    public static final int FILE_TRANSFER_REQ = 0x18;
    public static final int FILE_TRANSFER_BAD_COMMAND = 0x19;
    public static final int FILE_TRANSFER_ABOARD = 0x20;
    public static final int FILE_TRANSFER_START = 0x21;
    public static final int FILE_TRANSFER_SUCCESS = 0x22;

    public static final int PROBE = 0x42;
    public static final int POKE = 0x23;
    public static final int STOP = 0x33;
}
