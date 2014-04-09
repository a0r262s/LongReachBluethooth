package se.st.cs.uni_saarland.de.longreachbluethooth.services;

/**
 * Created by simkoc on 1/31/14.
 */
public interface ServiceThreadHandler {

    public void notifyUnhandledException(Exception e);

    public void notifyConnectionEstablished();

    public void notifyStop(Object notifier);
}
