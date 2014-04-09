package se.st.cs.uni_saarland.de.longreachbluethooth.util;

import se.st.cs.uni_saarland.de.longreachbluethooth.logger.Logger;

/**
 * Created by simkoc on 1/24/14.
 * */
 public class FoundServer {
    String name;
    String IPv4;
    String port;

    public FoundServer(String ident,String name) throws Exception {
        Logger.getInstance().log("TT",ident);
        String[] attr = ident.split("\\|");
        for(String s : attr)
            Logger.getInstance().log("TT",s);
        if(attr.length < 3)
            throw new Exception("NotEnoughArgumentsForServer");
        if(attr[0].compareTo(name) != 0)
            throw new Exception("expected " + name + " as ident not" + attr[0]);
        String[] ip = attr[2].split(":");
        if(ip.length != 2)
            throw new Exception("what is this IP supposed to be");
        this.name = attr[1];
        this.IPv4 = ip[0];
        this.port = ip[1];
    }

    public FoundServer(String name,String IPv4,String port) {
        this.name = name;
        this.IPv4 = IPv4;
        this.port = port;
    }

    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public String getIPv4() {
        return IPv4;
    }

    public String getPort() {
        return port;
    }
 }
