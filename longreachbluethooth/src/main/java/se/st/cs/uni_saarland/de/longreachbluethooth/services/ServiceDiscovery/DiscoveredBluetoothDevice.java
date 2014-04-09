package se.st.cs.uni_saarland.de.longreachbluethooth.services.ServiceDiscovery;

import se.st.cs.uni_saarland.de.longreachbluethooth.util.DebugPrint;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by simkoc on 2/1/14.
 */
public class DiscoveredBluetoothDevice {
    private String name;

    public DiscoveredBluetoothDevice(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void transmitYourself(DataOutputStream dos) throws IOException {
        DebugPrint.print("send name length");
        dos.writeInt(name.getBytes().length);
        DebugPrint.print("send name");
        dos.write(name.getBytes());
    }

    public static DiscoveredBluetoothDevice receiveDeviceInformation(DataInputStream dis) throws IOException {
        int nameLength = dis.readInt();
        byte name[] = new byte[nameLength];
        dis.read(name);
        return new DiscoveredBluetoothDevice(new String(name));
    }

    public boolean isEqual(DiscoveredBluetoothDevice dbd) {
        return name.compareTo(dbd.getName()) == 0;
    }
}
